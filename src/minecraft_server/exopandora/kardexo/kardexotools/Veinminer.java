package exopandora.kardexo.kardexotools;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
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

import net.minecraft.block.BlockNewLog;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockSand;
import net.minecraft.block.BlockStone;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Veinminer
{
	private static final Map<IBlockState, Integer> BLOCKS = new HashMap<IBlockState, Integer>();
	private static final Map<String, Stack<List<Entry<BlockPos, IBlockState>>>> HISTORY = new HashMap<String, Stack<List<Entry<BlockPos, IBlockState>>>>();
	
	static
	{
		BLOCKS.put(Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.OAK), 12);
		BLOCKS.put(Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.SPRUCE), 25);
		BLOCKS.put(Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.JUNGLE), 25);
		BLOCKS.put(Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.BIRCH), 15);
		
		BLOCKS.put(Blocks.LOG2.getDefaultState().withProperty(BlockNewLog.VARIANT, BlockPlanks.EnumType.DARK_OAK), 10);
		BLOCKS.put(Blocks.LOG2.getDefaultState().withProperty(BlockNewLog.VARIANT, BlockPlanks.EnumType.ACACIA), 10);
		
		BLOCKS.put(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.ANDESITE), 15);
		BLOCKS.put(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.DIORITE), 15);
		BLOCKS.put(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.GRANITE), 15);
		
		BLOCKS.put(Blocks.GLOWSTONE.getDefaultState(), 10);
		BLOCKS.put(Blocks.GRAVEL.getDefaultState(), 10);
		BLOCKS.put(Blocks.SAND.getDefaultState().withProperty(BlockSand.VARIANT, BlockSand.EnumType.SAND), 5);
		BLOCKS.put(Blocks.SAND.getDefaultState().withProperty(BlockSand.VARIANT, BlockSand.EnumType.RED_SAND), 5);
		
		BLOCKS.put(Blocks.COAL_ORE.getDefaultState(), 17);
		BLOCKS.put(Blocks.IRON_ORE.getDefaultState(), 9);
		BLOCKS.put(Blocks.GOLD_ORE.getDefaultState(), 9);
		BLOCKS.put(Blocks.DIAMOND_ORE.getDefaultState(), 9);
		BLOCKS.put(Blocks.LAPIS_ORE.getDefaultState(), 7);
		BLOCKS.put(Blocks.REDSTONE_ORE.getDefaultState(), 8);
		BLOCKS.put(Blocks.QUARTZ_ORE.getDefaultState(), 14);
		
		BLOCKS.put(Blocks.PACKED_ICE.getDefaultState(), 10);
		BLOCKS.put(Blocks.BONE_BLOCK.getDefaultState(), 10);
	}
	
	public static boolean mine(BlockPos pos, EntityPlayerMP player, World world, BiFunction<BlockPos, Boolean, Boolean> harvestBlock)
	{
		if(Config.VEINMINER.getData().containsKey(player.getName()) && Config.VEINMINER.getData().get(player.getName()).isEnabled() && player.isSneaking())
		{
			IBlockState state = world.getBlockState(pos);
			
			for(IBlockState block : BLOCKS.keySet())
			{
				if(isEqualVariant(state, block))
				{
					if(player.getHeldItemMainhand().getDestroySpeed(state) > 1.0F)
					{
						PriorityQueue<BlockPos> queue = calcVein(Config.BLOCK_LIMIT, BLOCKS.get(block), world.getBlockState(pos), pos, pos, world);
						List<Entry<BlockPos, IBlockState>> undo = new ArrayList<Entry<BlockPos, IBlockState>>();
						queue.poll();
						
						if(queue.size() > 1)
						{
							Entry<BlockPos, IBlockState> next = new SimpleEntry<BlockPos, IBlockState>(pos, world.getBlockState(pos));
							boolean harvest = harvestBlock.apply(pos, true);
							
							if(harvest)
							{
								undo.add(next);
								
								for(int x = 0; x < Config.BLOCK_LIMIT; x++)
								{
									if(player.getHeldItemMainhand().getMaxDamage() - player.getHeldItemMainhand().getItemDamage() == 0 || queue.isEmpty())
									{
										break;
									}
									
									next = new SimpleEntry<BlockPos, IBlockState>(queue.peek(), world.getBlockState(queue.peek()));
									
									if(!harvestBlock.apply(queue.poll(), true))
									{
										break;
									}
									
									undo.add(next);
								}
								
								if(undo.size() > 1)
								{
									Stack<List<Entry<BlockPos, IBlockState>>> history = HISTORY.get(player.getName());
									
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
										history = new Stack<List<Entry<BlockPos, IBlockState>>>();
										history.add(undo);
										HISTORY.put(player.getName(), history);
									}
								}
							}
							
							return harvest;
						}
					}
				}
			}
		}
		
		return harvestBlock.apply(pos, false);
	}
	
	private static PriorityQueue<BlockPos> calcVein(int limit, int radius, IBlockState state, BlockPos origin, BlockPos pos, World world)
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
							if(x != 0 || y != 0 || z != 0)
							{
								BlockPos nextBlock = block.add(x, y, z);
								
								if(Math.sqrt(nextBlock.distanceSq(origin.getX(), origin.getY(), origin.getZ())) < radius)
								{
									if(isEqualVariant(state, world.getBlockState(nextBlock)))
									{
										if(!queue.contains(nextBlock) && !pending.contains(nextBlock) && !next.contains(nextBlock))
										{
											next.add(nextBlock);
										}
									}
								}
							}
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
		return (BlockPos a, BlockPos b) -> (int) (a.distanceSq(origin.getX(), origin.getY(), origin.getZ()) - b.distanceSq(origin.getX(), origin.getY(), origin.getZ()));
	}
	
	public static boolean undo(EntityPlayerMP player, World world)
	{
		Stack<List<Entry<BlockPos, IBlockState>>> history = HISTORY.get(player.getName());
		List<Entry<BlockPos, IBlockState>> undo = history.peek();
		IBlockState state = undo.get(0).getValue();
		Item item = Item.getItemFromBlock(state.getBlock());
		int metadata = state.getBlock().damageDropped(state);
		int count = undo.size();
		
		if(playerHasItems(player, item, metadata, count) && hasSpace(world, undo) && hasNoCollidingEntities(world, undo))
		{
			for(Entry<BlockPos, IBlockState> entry : undo)
			{
				world.setBlockState(entry.getKey(), entry.getValue());
			}
			
			player.addStat(StatList.getBlockStats(state.getBlock()), count);
			
			if(!player.interactionManager.isCreative())
			{
				player.inventory.clearMatchingItems(item, metadata, count, null);
			}
			
			history.pop();
			
			if(history.empty())
			{
				HISTORY.remove(player.getName());
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
	
	private static boolean hasSpace(World world, List<Entry<BlockPos, IBlockState>> list)
	{
		IBlockState air = Blocks.AIR.getDefaultState();
		
		for(Entry<BlockPos, IBlockState> entry : list)
		{
			IBlockState state = world.getBlockState(entry.getKey());
			
			if(!state.equals(air) && !state.getBlock().equals(Blocks.WATER) && !state.getBlock().equals(Blocks.LAVA))
			{
				return false;
			}
		}
		
		return true;
	}
	
	private static boolean hasNoCollidingEntities(World world, List<Entry<BlockPos, IBlockState>> list)
	{
		for(Entry<BlockPos, IBlockState> entry : list)
		{
			if(!world.checkNoEntityCollision(new AxisAlignedBB(entry.getKey())))
			{
				return false;
			}
		}
		
		return true;
	}
	
	public static boolean hasUndo(String player)
	{
		return HISTORY.containsKey(player);
	}
}
