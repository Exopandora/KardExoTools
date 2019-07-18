package exopandora.kardexo.kardexotools.veinminer;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.function.BiFunction;

import com.google.common.collect.Lists;

import exopandora.kardexo.kardexotools.data.Config;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
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
import net.minecraft.world.ServerWorld;
import net.minecraft.world.World;

public class Veinminer
{
	private static final PlayerHistory<VeinminerHistoryEntry> HISTORY = new PlayerHistory<VeinminerHistoryEntry>(Config.HISTORY_SIZE);
	
	public static boolean mine(BlockPos pos, ServerPlayerEntity player, World world, BiFunction<BlockPos, Boolean, Boolean> harvestBlock)
	{
		String name = player.getName().getString();
		
		if(Config.PLAYERS.getData().containsKey(name) && Config.PLAYERS.getData().get(name).isVeinminerEnabled() && player.isSneaking())
		{
			BlockState state = world.getBlockState(pos);
			
			for(Block block : Config.VEINMINER.getData().keySet())
			{
				ItemStack item = player.getHeldItemMainhand();
				
				if(isEqual(state, block.getDefaultState()) && (item.getDestroySpeed(state) > 1.0F || block.getDefaultState().getMaterial().isToolNotRequired()))
				{
					PriorityQueue<BlockPos> queue = calculateVein(Config.BLOCK_LIMIT, Config.VEINMINER.getData().get(block).getRadius(), state, pos, world);
					
					queue.stream().peek(BlockPos::toString);
					
					Map<BlockState, List<BlockPos>> stateMap = new HashMap<BlockState, List<BlockPos>>();
					VeinminerHistoryEntry undo = new VeinminerHistoryEntry(player.dimension, stateMap);
					queue.poll();
					
					if(!queue.isEmpty())
					{
						BlockState next = state;
						boolean harvest = harvestBlock.apply(pos, true);
						
						if(harvest)
						{
							stateMap.put(next, Lists.newArrayList(pos));
							
							for(int x = 0; x < Config.BLOCK_LIMIT; x++)
							{
								if(item.getMaxDamage() > 0 ? item.getMaxDamage() == item.getDamage() : false)
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
								
								List<BlockPos> list = stateMap.get(next);
								
								if(list != null)
								{
									list.add(queue.poll());
								}
								else
								{
									stateMap.put(next, Lists.newArrayList(queue.poll()));
								}
							}
							
							if(getFlatMapSize(stateMap.values()) > 1)
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
	
	private static PriorityQueue<BlockPos> calculateVein(int limit, int radius, BlockState state, BlockPos pos, World world)
	{
		PriorityQueue<BlockPos> queue = new PriorityQueue<BlockPos>(getComparator(pos));
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
			
			PriorityQueue<BlockPos> next = new PriorityQueue<BlockPos>(getComparator(pos));
			
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
							
							if(Math.sqrt(nextBlock.distanceSq(pos)) >= radius)
							{
								continue;
							}
							
							if(!isEqual(state, world.getBlockState(nextBlock)))
							{
								continue;
							}
							
							if(queue.contains(nextBlock) || pending.contains(nextBlock) || next.contains(nextBlock))
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
		History<VeinminerHistoryEntry> history = Veinminer.HISTORY.getHistory(player.getName().getString());
		VeinminerHistoryEntry undo = history.peek();
		Map<BlockState, List<BlockPos>> stateMap = undo.getStateMap();
		ServerWorld world = server.getWorld(undo.getDimension());
		BlockState state = stateMap.keySet().iterator().next();
		Item item = state.getBlock().asItem();
		int count = getFlatMapSize(stateMap.values());
		
		if(playerHasItems(player, item, count) && hasSpace(world, stateMap) && hasNoCollidingEntities(world, stateMap))
		{
			for(Entry<BlockState, List<BlockPos>> entry : stateMap.entrySet())
			{
				for(BlockPos pos : entry.getValue())
				{
					world.setBlockState(pos, entry.getKey());
					player.addStat(Stats.BLOCK_MINED.get(state.getBlock()));
				}
			}
			
			if(!player.interactionManager.isCreative())
			{
				player.inventory.clearMatchingItems(stack -> stack.getItem().equals(item), count);
			}
			
			history.pop();
			
			if(history.empty())
			{
				Veinminer.HISTORY.remove(player.getName().getString());
			}
			
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
	
	private static boolean hasSpace(World world, Map<BlockState, List<BlockPos>> stateMap) throws Exception
	{
		for(Entry<BlockState, List<BlockPos>> entry : stateMap.entrySet())
		{
			for(BlockPos pos : entry.getValue())
			{
				Block block = world.getBlockState(pos).getBlock();
				
				if(!block.equals(Blocks.AIR) && !block.equals(Blocks.WATER)  && !block.equals(Blocks.LAVA) && !block.equals(Blocks.VOID_AIR) && !block.equals(Blocks.CAVE_AIR))
				{
					throw new Exception("Space is being occupied by other blocks");
				}
			}
		}
		
		return true;
	}
	
	private static <T> int getFlatMapSize(Collection<List<T>> collection)
	{
		return (int) collection.parallelStream().flatMap(List::stream).count();
	}
	
	private static boolean hasNoCollidingEntities(World world, Map<BlockState, List<BlockPos>> statemap) throws Exception
	{
		for(Entry<BlockState, List<BlockPos>> entry : statemap.entrySet())
		{
			for(BlockPos pos : entry.getValue())
			{
				if(!world.checkNoEntityCollision(null, VoxelShapes.create(new AxisAlignedBB(pos))))
				{
					throw new Exception("Space is being occupied by other entities");
				}
			}
		}
		
		return true;
	}
	
	public static boolean hasUndo(String player)
	{
		return Veinminer.HISTORY.hasUndo(player);
	}
}
