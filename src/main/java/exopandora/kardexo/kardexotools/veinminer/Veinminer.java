package exopandora.kardexo.kardexotools.veinminer;

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Stack;
import java.util.function.BiFunction;

import com.google.common.collect.Lists;

import exopandora.kardexo.kardexotools.data.Config;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.stats.StatList;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.dimension.DimensionType;

public class Veinminer
{
	private static final Map<String, Stack<Entry<Integer, Map<IBlockState, List<BlockPos>>>>> HISTORY = new HashMap<String, Stack<Entry<Integer,Map<IBlockState, List<BlockPos>>>>>();
	
	public static boolean mine(BlockPos pos, EntityPlayerMP player, World world, BiFunction<BlockPos, Boolean, Boolean> harvestBlock)
	{
		String name = player.getName().getString();
		
		if(Config.PLAYERS.getData().containsKey(name) && Config.PLAYERS.getData().get(name).isVeinminerEnabled() && player.isSneaking())
		{
			IBlockState state = world.getBlockState(pos);
			
			for(Block block : Config.VEINMINER.getData().keySet())
			{
				ItemStack item = player.getHeldItemMainhand();

				if(isEqual(state, block.getDefaultState()) && (item.getDestroySpeed(state) > 1.0F || block.getDefaultState().getMaterial().isToolNotRequired()))
				{
					PriorityQueue<BlockPos> queue = calculateVein(Config.BLOCK_LIMIT, Config.VEINMINER.getData().get(block).getRadius(), state, pos, world);
					
					queue.stream().peek(BlockPos::toString);
					
					Map<IBlockState, List<BlockPos>> statemap = new HashMap<IBlockState, List<BlockPos>>();
					Entry<Integer, Map<IBlockState, List<BlockPos>>> undo = new SimpleEntry<Integer, Map<IBlockState, List<BlockPos>>>(player.dimension.getId(), statemap);
					queue.poll();
					
					if(!queue.isEmpty())
					{
						IBlockState next = state;
						boolean harvest = harvestBlock.apply(pos, true);
						
						if(harvest)
						{
							statemap.put(next, Lists.newArrayList(pos));
							
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
								
								List<BlockPos> list = statemap.get(next);
								
								if(list != null)
								{
									list.add(queue.poll());
								}
								else
								{
									statemap.put(next, Lists.newArrayList(queue.poll()));
								}
							}
							
							if(getFlatMapSize(statemap.values()) > 1)
							{
								Stack<Entry<Integer, Map<IBlockState, List<BlockPos>>>> history = Veinminer.HISTORY.get(name);
								
								if(history != null)
								{
									if(history.size() == Config.HISTORY_SIZE)
									{
										history.remove(0);
									}
									
									history.push(undo);
								}
								else
								{
									history = new Stack<Entry<Integer, Map<IBlockState, List<BlockPos>>>>();
									history.add(undo);
									Veinminer.HISTORY.put(name, history);
								}
							}
						}
						
						return harvest;
					}
				}
			}
		}
		
		return harvestBlock.apply(pos, false);
	}
	
	private static PriorityQueue<BlockPos> calculateVein(int limit, int radius, IBlockState state, BlockPos pos, World world)
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
	
	private static boolean isEqual(IBlockState a, IBlockState b)
	{
		return a.getBlock().equals(b.getBlock());
	}
	
	private static final Comparator<BlockPos> getComparator(BlockPos origin)
	{
		return (a, b) -> (int) (a.distanceSq(origin) - b.distanceSq(origin));
	}
	
	public static int undo(EntityPlayerMP player, MinecraftServer server) throws Exception
	{
		Stack<Entry<Integer, Map<IBlockState, List<BlockPos>>>> history = Veinminer.HISTORY.get(player.getName().getString());
		Entry<Integer, Map<IBlockState, List<BlockPos>>> undo = history.peek();
		Map<IBlockState, List<BlockPos>> statemap = undo.getValue();
		WorldServer world = server.getWorld(DimensionType.getById(undo.getKey()));
		IBlockState state = statemap.keySet().iterator().next();
		Item item = state.getBlock().getItemDropped(state, null, null, 1).asItem();
		int count = getFlatMapSize(statemap.values());
		
		if(playerHasItems(player, item, count) && hasSpace(world, statemap) && hasNoCollidingEntities(world, statemap))
		{
			for(Entry<IBlockState, List<BlockPos>> entry : statemap.entrySet())
			{
				for(BlockPos pos : entry.getValue())
				{
					world.setBlockState(pos, entry.getKey());
					player.addStat(StatList.BLOCK_MINED.get(state.getBlock()));
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
	
	private static boolean playerHasItems(EntityPlayerMP player, Item item, int amount) throws Exception
	{
		if(player.interactionManager.isCreative())
		{
			return true;
		}
		
		InventoryPlayer inventory = player.inventory;
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
	
	private static boolean hasSpace(World world, Map<IBlockState, List<BlockPos>> statemap) throws Exception
	{
		for(Entry<IBlockState, List<BlockPos>> entry : statemap.entrySet())
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
	
	private static boolean hasNoCollidingEntities(World world, Map<IBlockState, List<BlockPos>> statemap) throws Exception
	{
		for(Entry<IBlockState, List<BlockPos>> entry : statemap.entrySet())
		{
			for(BlockPos pos : entry.getValue())
			{
				if(!world.checkNoEntityCollision(null, new AxisAlignedBB(pos)))
				{
					throw new Exception("Space is being occupied by other entities");
				}
			}
		}
		
		return true;
	}
	
	public static boolean hasUndo(String player)
	{
		return Veinminer.HISTORY.containsKey(player);
	}
}
