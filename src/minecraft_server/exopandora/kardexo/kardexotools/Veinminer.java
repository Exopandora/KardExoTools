package exopandora.kardexo.kardexotools;

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

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
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

public class Veinminer
{
	private static final Map<String, Stack<Entry<Integer, Map<IBlockState, List<BlockPos>>>>> HISTORY = new HashMap<String, Stack<Entry<Integer,Map<IBlockState, List<BlockPos>>>>>();
	
	public static boolean mine(BlockPos pos, EntityPlayerMP player, World world, BiFunction<BlockPos, Boolean, Boolean> harvestBlock)
	{
		if(Config.PLAYERS.getData().containsKey(player.getName()) && Config.PLAYERS.getData().get(player.getName()).isVeinminerEnabled() && player.isSneaking())
		{
			IBlockState state = world.getBlockState(pos);
			
			for(IBlockState block : Config.VEINMINER.getData().keySet())
			{
				ItemStack item = player.getHeldItemMainhand();
				
				if(isEqualVariant(state, block) && (item.getDestroySpeed(state) > 1.0F || block.getMaterial().isToolNotRequired()))
				{
					PriorityQueue<BlockPos> queue = calculateVein(Config.BLOCK_LIMIT, Config.VEINMINER.getData().get(block).getRadius(), world.getBlockState(pos), pos, pos, world);
					Map<IBlockState, List<BlockPos>> statemap = new HashMap<IBlockState, List<BlockPos>>();
					Entry<Integer, Map<IBlockState, List<BlockPos>>> undo = new SimpleEntry(player.dimension, statemap);
					queue.poll();
					
					if(!queue.isEmpty())
					{
						IBlockState next = world.getBlockState(pos);
						boolean harvest = harvestBlock.apply(pos, true);
						
						if(harvest)
						{
							statemap.put(next, Lists.newArrayList(pos));
							
							for(int x = 0; x < Config.BLOCK_LIMIT; x++)
							{
								if(item.getMaxDamage() > 0 ? item.getMaxDamage() == item.getItemDamage() : false)
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
								Stack<Entry<Integer, Map<IBlockState, List<BlockPos>>>> history = Veinminer.HISTORY.get(player.getName());
								
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
									Veinminer.HISTORY.put(player.getName(), history);
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
	
	private static PriorityQueue<BlockPos> calculateVein(int limit, int radius, IBlockState state, BlockPos origin, BlockPos pos, World world)
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
							
							if(Math.sqrt(nextBlock.distanceSq(origin.getX(), origin.getY(), origin.getZ())) >= radius)
							{
								continue;
							}
							
							if(!isEqualVariant(state, world.getBlockState(nextBlock)))
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
	
	private static boolean isEqualVariant(IBlockState a, IBlockState b)
	{
		IProperty<?> propertyA = getProperty("variant", a);
		IProperty<?> propertyB = getProperty("variant", b);
		
		boolean equalBlock = a.getBlock().equals(b.getBlock());
		
		if(propertyA != null && propertyB != null)
		{
			return equalBlock && a.getProperties().get(propertyA).equals(b.getProperties().get(propertyB));
		}
		
		return equalBlock;
	}
	
	private static IProperty<?> getProperty(String name, IBlockState state)
	{
		for(IProperty<?> property : state.getPropertyKeys())
		{
			if(property.getName().equals(name))
			{
				return property;
			}
		}
		
		return null;
	}
	
	private static final Comparator<BlockPos> getComparator(BlockPos origin)
	{
		return (a, b) -> (int) (a.distanceSq(origin.getX(), origin.getY(), origin.getZ()) - b.distanceSq(origin.getX(), origin.getY(), origin.getZ()));
	}
	
	public static boolean undo(EntityPlayerMP player, MinecraftServer server)
	{
		Stack<Entry<Integer, Map<IBlockState, List<BlockPos>>>> history = Veinminer.HISTORY.get(player.getName());
		Entry<Integer, Map<IBlockState, List<BlockPos>>> undo = history.peek();
		Map<IBlockState, List<BlockPos>> statemap = undo.getValue();
		WorldServer world = server.getWorld(undo.getKey());
		IBlockState state = statemap.keySet().iterator().next();
		Item item = Item.getItemFromBlock(state.getBlock());
		int metadata = state.getBlock().damageDropped(state);
		int count = getFlatMapSize(statemap.values());
		
		if(playerHasItems(player, item, metadata, count) && hasSpace(world, statemap) && hasNoCollidingEntities(world, statemap))
		{
			for(Entry<IBlockState, List<BlockPos>> entry : statemap.entrySet())
			{
				for(BlockPos pos : entry.getValue())
				{
					world.setBlockState(pos, entry.getKey());
				}
			}
			
			player.addStat(StatList.getBlockStats(state.getBlock()), count);
			
			if(!player.interactionManager.isCreative())
			{
				player.inventory.clearMatchingItems(item, metadata, count, null);
			}
			
			history.pop();
			
			if(history.empty())
			{
				Veinminer.HISTORY.remove(player.getName());
			}
			
			return true;
		}
		
		return false;
	}
	
	private static boolean playerHasItems(EntityPlayerMP player, Item item, int metadata, int amount)
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
				if(stack.getItem().equals(item) && stack.getMetadata() == metadata)
				{
					count += stack.getCount();
				}
			}
		}
		
		return count >= amount;
	}
	
	private static boolean hasSpace(World world, Map<IBlockState, List<BlockPos>> statemap)
	{
		for(Entry<IBlockState, List<BlockPos>> entry : statemap.entrySet())
		{
			for(BlockPos pos : entry.getValue())
			{
				Block block = world.getBlockState(pos).getBlock();
				
				if(!block.equals(Blocks.AIR) && !block.equals(Blocks.WATER) && !block.equals(Blocks.LAVA))
				{
					return false;
				}
			}
		}
		
		return true;
	}
	
	private static <T> int getFlatMapSize(Collection<List<T>> collection)
	{
		return (int) collection.parallelStream().flatMap(List::stream).count();
	}
	
	private static boolean hasNoCollidingEntities(World world, Map<IBlockState, List<BlockPos>> statemap)
	{
		for(Entry<IBlockState, List<BlockPos>> entry : statemap.entrySet())
		{
			for(BlockPos pos : entry.getValue())
			{
				if(!world.checkNoEntityCollision(new AxisAlignedBB(pos)))
				{
					return false;
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
