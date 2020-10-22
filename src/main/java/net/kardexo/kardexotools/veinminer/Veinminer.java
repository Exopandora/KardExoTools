package net.kardexo.kardexotools.veinminer;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import com.google.common.collect.Sets;

import net.kardexo.kardexotools.config.Config;
import net.kardexo.kardexotools.property.PropertyHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.stats.Stats;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class Veinminer
{
	private static final PlayerHistory<VeinminerHistoryEntry> HISTORY = new PlayerHistory<VeinminerHistoryEntry>(Config.HISTORY_SIZE);
	
	public static boolean mine(BlockPos pos, ServerPlayerEntity player, World world, BiFunction<BlockPos, Boolean, Boolean> harvestBlock)
	{
		String name = player.getGameProfile().getName();
		ItemStack item = player.getHeldItemMainhand();
		BlockState state = world.getBlockState(pos);
		boolean isEffectiveTool = item.getDestroySpeed(state) > 1.0F;
		
		if(Config.PLAYERS.getData().containsKey(name) && Config.PLAYERS.getData().get(name).isVeinminerEnabled() && player.isSneaking() && !player.isOnLadder() && (!item.isDamageable() || item.getMaxDamage() - item.getDamage() > 1))
		{
			for(Entry<Block, VeinminerConfigEntry> entry : Config.VEINMINER.getData().entrySet())
			{
				Block block = entry.getKey();
				VeinminerConfigEntry config = entry.getValue();
				
				if(Veinminer.isEqual(state, block.getDefaultState()) && (isEffectiveTool || !config.doesRequireTool()))
				{
					PriorityQueue<BlockPos> queue = Veinminer.calculateVein(player, Config.BLOCK_LIMIT, config.getRadius(), state, pos, world);
					Map<BlockState, Set<BlockPos>> stateMap = new HashMap<BlockState, Set<BlockPos>>();
					VeinminerHistoryEntry undo = new VeinminerHistoryEntry(player.world.getDimensionKey(), stateMap);
					queue.poll();
					
					if(!queue.isEmpty())
					{
						BlockState next = state;
						boolean harvest = harvestBlock.apply(pos, true);
						
						if(harvest)
						{
							stateMap.put(next, Sets.newHashSet(pos));
							
							for(int x = 0; x < Config.BLOCK_LIMIT; x++)
							{
								if(item.isDamageable() && item.getMaxDamage() > 0 && item.getMaxDamage() - item.getDamage() == 1)
								{
									break;
								}
								
								if(queue.isEmpty())
								{
									break;
								}
								
								next = world.getBlockState(queue.peek());
								
								if(!harvestBlock.apply(queue.peek(), true))
								{
									break;
								}
								
								Set<BlockPos> list = stateMap.get(next);
								
								if(list != null)
								{
									list.add(queue.poll());
								}
								else
								{
									stateMap.put(next, Sets.newHashSet(queue.poll()));
								}
							}
							
							if(Veinminer.count(stateMap.values()) > 1)
							{
								Veinminer.HISTORY.add(name, undo);
							}
						}
						
						return harvest;
					}
				}
			}
		}
		
		return harvestBlock.apply(pos, false);
	}
	
	private static PriorityQueue<BlockPos> calculateVein(PlayerEntity player, int limit, int radius, BlockState state, BlockPos pos, World world)
	{
		PriorityQueue<BlockPos> queue = new PriorityQueue<BlockPos>(Veinminer.getComparator(pos));
		Collection<BlockPos> pending = Collections.singleton(pos);
		
		while(!pending.isEmpty())
		{
			for(BlockPos block : pending)
			{
				if(queue.size() < limit)
				{
					queue.add(block);
				}
				else
				{
					return queue;
				}
			}
			
			PriorityQueue<BlockPos> next = new PriorityQueue<BlockPos>(Veinminer.getComparator(pos));
			
			for(BlockPos block : pending)
			{
				final int delta[] = {-1, 0, 1};
				
				for(int x : delta)
				{
					for(int y : delta)
					{
						for(int z : delta)
						{
							if(x == 0 && y == 0 && z == 0)
							{
								continue;
							}
							
							BlockPos nextBlock = block.add(x, y, z);
							
							if(nextBlock.distanceSq(pos) >= radius * radius)
							{
								continue;
							}
							
							if(!Veinminer.isEqual(state, world.getBlockState(nextBlock)))
							{
								continue;
							}
							
							if(queue.contains(nextBlock) || pending.contains(nextBlock) || next.contains(nextBlock))
							{
								continue;
							}
							
							if(!PropertyHelper.canHarvestBlock(player, nextBlock))
							{
								continue;
							}
							
							next.add(nextBlock);
						}
					}
				}
			}
			
			pending = next;
		}
		
		return queue;
	}
	
	private static boolean isEqual(BlockState a, BlockState b)
	{
		return a.getBlock().equals(b.getBlock());
	}
	
	private static final Comparator<BlockPos> getComparator(BlockPos origin)
	{
		return (a, b) -> (int) (a.distanceSq(origin) - b.distanceSq(origin));
	}
	
	public static int undo(ServerPlayerEntity player, MinecraftServer server) throws Exception
	{
		VeinminerHistoryEntry undo = Veinminer.HISTORY.peek(player.getGameProfile().getName());
		Set<BlockPos> positions = undo.getAllPositions();
		ServerWorld world = server.getWorld(undo.getWorld());
		Block block = undo.getBlock();
		int count = positions.size();
		
		if(Veinminer.playerHasItems(player, block.asItem(), count) && Veinminer.hasSpace(world, positions) && Veinminer.hasNoCollidingEntities(world, positions))
		{
			for(Entry<BlockState, Set<BlockPos>> entry : undo.getStateMap().entrySet())
			{
				for(BlockPos pos : entry.getValue())
				{
					world.setBlockState(pos, entry.getKey());
					player.addStat(Stats.BLOCK_MINED.get(block));
				}
			}
			
			if(!player.interactionManager.isCreative())
			{
				player.inventory.func_234564_a_(stack -> stack.getItem().equals(block.asItem()), count, player.container.func_234641_j_());
			}
			
			Veinminer.HISTORY.pop(player.getName().getString());
			
			return count;
		}
		
		return 0;
	}
	
	private static boolean playerHasItems(ServerPlayerEntity player, Item item, int amount) throws Exception
	{
		if(player.interactionManager.isCreative())
		{
			return true;
		}
		
		PlayerInventory inventory = player.inventory;
		List<NonNullList<ItemStack>> inventories = Arrays.<NonNullList<ItemStack>>asList(inventory.mainInventory, inventory.armorInventory, inventory.offHandInventory);
		
		int count = 0;
		
		for(List<ItemStack> list : inventories)
		{
			for(ItemStack stack : list)
			{
				if(stack.getItem().equals(item) /*&& stack.func_77976_d() == metadata*/)
				{
					count += stack.getCount();
				}
			}
		}
		
		if(count >= amount)
		{
			return true;
		}
		
		throw new Exception("You do not have enough items");
	}
	
	private static boolean hasSpace(World world, Set<BlockPos> positions) throws Exception
	{
		for(BlockPos pos : positions)
		{
			Block block = world.getBlockState(pos).getBlock();
			
			if(!block.equals(Blocks.AIR) && !block.equals(Blocks.WATER) && !block.equals(Blocks.LAVA) && !block.equals(Blocks.VOID_AIR) && !block.equals(Blocks.CAVE_AIR))
			{
				throw new Exception("Space is being occupied by other blocks");
			}
		}
		
		return true;
	}
	
	private static <T> int count(Collection<Set<T>> collection)
	{
		return (int) Veinminer.flatten(collection).count();
	}
	
	public static <T> Stream<T> flatten(Collection<Set<T>> collection)
	{
		return collection.parallelStream().flatMap(Set::stream);
	}
	
	private static boolean hasNoCollidingEntities(World world, Set<BlockPos> positions) throws Exception
	{
		for(BlockPos pos : positions)
		{
			if(!world.checkNoEntityCollision(null, VoxelShapes.create(new AxisAlignedBB(pos))))
			{
				throw new Exception("Space is being occupied by other entities");
			}
		}
		
		return true;
	}
	
	public static boolean hasUndo(String player)
	{
		return Veinminer.HISTORY.hasUndo(player);
	}
}
