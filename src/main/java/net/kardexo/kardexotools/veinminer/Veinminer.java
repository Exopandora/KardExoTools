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

import net.kardexo.kardexotools.KardExo;
import net.kardexo.kardexotools.config.VeinBlockConfig;
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
	private static final PlayerHistory<Vein> HISTORY = new PlayerHistory<Vein>(KardExo.CONFIG.getVeinminerHistorySize());
	
	public static boolean mine(BlockPos pos, ServerPlayerEntity player, World world, BiFunction<BlockPos, Boolean, Boolean> harvestBlock)
	{
		String name = player.getGameProfile().getName();
		ItemStack item = player.getMainHandItem();
		BlockState state = world.getBlockState(pos);
		boolean isEffectiveTool = item.getDestroySpeed(state) > 1.0F;
		
		if(KardExo.PLAYERS.containsKey(name) && KardExo.PLAYERS.get(name).isVeinminerEnabled() && player.isShiftKeyDown() && !player.onClimbable() && (!item.isDamageableItem() || item.getMaxDamage() - item.getDamageValue() > 1))
		{
			for(Entry<Block, VeinBlockConfig> entry : KardExo.VEINMINER.entrySet())
			{
				Block block = entry.getKey();
				VeinBlockConfig config = entry.getValue();
				
				if(Veinminer.isEqual(state, block.defaultBlockState()) && (isEffectiveTool || !config.doesRequireTool()))
				{
					PriorityQueue<BlockPos> queue = Veinminer.calculateVein(player, KardExo.CONFIG.getVeinminerBlockLimit(), config.getRadius(), state, pos, world);
					Map<BlockState, Set<BlockPos>> stateMap = new HashMap<BlockState, Set<BlockPos>>();
					Vein undo = new Vein(player.level.dimension(), stateMap);
					queue.poll();
					
					if(!queue.isEmpty())
					{
						BlockState next = state;
						boolean harvest = harvestBlock.apply(pos, true);
						
						if(harvest)
						{
							stateMap.put(next, Sets.newHashSet(pos));
							
							for(int x = 0; x < KardExo.CONFIG.getVeinminerBlockLimit(); x++)
							{
								if(item.isDamageableItem() && item.getMaxDamage() > 0 && item.getMaxDamage() - item.getDamageValue() == 1)
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
								
								stateMap.computeIfAbsent(next, key -> Sets.newHashSet()).add(queue.poll());
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
		PriorityQueue<BlockPos> queue = new PriorityQueue<BlockPos>(Veinminer.comparator(pos));
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
			
			PriorityQueue<BlockPos> next = new PriorityQueue<BlockPos>(Veinminer.comparator(pos));
			
			for(BlockPos block : pending)
			{
				final int positions[] = {-1, 0, 1};
				
				for(int x : positions)
				{
					for(int y : positions)
					{
						for(int z : positions)
						{
							if(x == 0 && y == 0 && z == 0)
							{
								continue;
							}
							
							BlockPos nextBlock = block.offset(x, y, z);
							
							if(nextBlock.distSqr(pos) >= radius * radius)
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
	
	private static final Comparator<BlockPos> comparator(BlockPos origin)
	{
		return (a, b) -> (int) (a.distSqr(origin) - b.distSqr(origin));
	}
	
	public static int undo(ServerPlayerEntity player, MinecraftServer server) throws Exception
	{
		Vein undo = Veinminer.HISTORY.peek(player.getGameProfile().getName());
		Set<BlockPos> positions = undo.getAllPositions();
		ServerWorld world = server.getLevel(undo.getWorld());
		Block block = undo.getBlock();
		int count = positions.size();
		
		if(Veinminer.playerHasItems(player, block.asItem(), count) && Veinminer.hasSpace(world, positions) && Veinminer.hasNoCollidingEntities(world, positions))
		{
			for(Entry<BlockState, Set<BlockPos>> entry : undo.getStateMap().entrySet())
			{
				Set<BlockPos> blocks = entry.getValue();
				
				for(BlockPos pos : blocks)
				{
					world.setBlockAndUpdate(pos, entry.getKey());
				}
				
				if(!blocks.isEmpty())
				{
					player.awardStat(Stats.BLOCK_MINED.get(block), blocks.size());
				}
			}
			
			if(!player.gameMode.isCreative())
			{
				player.inventory.clearOrCountMatchingItems(stack -> stack.getItem().equals(block.asItem()), count, player.inventoryMenu.getCraftSlots());
			}
			
			Veinminer.HISTORY.pop(player.getName().getString());
			
			return count;
		}
		
		return 0;
	}
	
	private static boolean playerHasItems(ServerPlayerEntity player, Item item, int amount) throws Exception
	{
		if(player.gameMode.isCreative())
		{
			return true;
		}
		
		PlayerInventory inventory = player.inventory;
		List<NonNullList<ItemStack>> inventories = Arrays.<NonNullList<ItemStack>>asList(inventory.items, inventory.armor, inventory.offhand);
		
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
		return collection.stream().flatMap(Set::stream);
	}
	
	private static boolean hasNoCollidingEntities(World world, Set<BlockPos> positions) throws Exception
	{
		for(BlockPos pos : positions)
		{
			if(!world.isUnobstructed(null, VoxelShapes.create(new AxisAlignedBB(pos))))
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
