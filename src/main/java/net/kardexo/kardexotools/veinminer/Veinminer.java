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
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;

public class Veinminer
{
	private static final PlayerHistory<Vein> HISTORY = new PlayerHistory<Vein>(KardExo.CONFIG.getVeinminerHistorySize());
	
	public static boolean mine(BlockPos pos, ServerPlayer player, Level level, BiFunction<BlockPos, Boolean, Boolean> harvestBlock)
	{
		String name = player.getGameProfile().getName();
		ItemStack item = player.getMainHandItem();
		BlockState state = level.getBlockState(pos);
		boolean isEffectiveTool = item.getDestroySpeed(state) > 1.0F;
		
		if(KardExo.PLAYERS.containsKey(name) && KardExo.PLAYERS.get(name).isVeinminerEnabled() && player.isShiftKeyDown() && !player.onClimbable() && (!item.isDamageableItem() || item.getMaxDamage() - item.getDamageValue() > 1))
		{
			for(Entry<Block, VeinBlockConfig> entry : KardExo.VEINMINER.entrySet())
			{
				Block block = entry.getKey();
				VeinBlockConfig config = entry.getValue();
				
				if(Veinminer.isEqual(state, block.defaultBlockState()) && (isEffectiveTool || !config.doesRequireTool()))
				{
					PriorityQueue<BlockPos> queue = Veinminer.calculateVein(player, KardExo.CONFIG.getVeinminerBlockLimit(), config.getRadius(), state, pos, level);
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
								
								next = level.getBlockState(queue.peek());
								
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
	
	private static PriorityQueue<BlockPos> calculateVein(Player player, int limit, int radius, BlockState state, BlockPos pos, Level level)
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
							
							if(!Veinminer.isEqual(state, level.getBlockState(nextBlock)))
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
	
	public static int undo(ServerPlayer player, MinecraftServer server) throws Exception
	{
		Vein undo = Veinminer.HISTORY.peek(player.getGameProfile().getName());
		Set<BlockPos> positions = undo.getAllPositions();
		ServerLevel level = server.getLevel(undo.getLevel());
		Block block = undo.getBlock();
		int count = positions.size();
		
		if(Veinminer.playerHasItems(player, block.asItem(), count) && Veinminer.hasSpace(level, positions) && Veinminer.hasNoCollidingEntities(level, positions))
		{
			for(Entry<BlockState, Set<BlockPos>> entry : undo.getStateMap().entrySet())
			{
				Set<BlockPos> blocks = entry.getValue();
				
				for(BlockPos pos : blocks)
				{
					level.setBlockAndUpdate(pos, entry.getKey());
				}
				
				if(!blocks.isEmpty())
				{
					player.awardStat(Stats.BLOCK_MINED.get(block), blocks.size());
				}
			}
			
			if(!player.gameMode.isCreative())
			{
				player.getInventory().clearOrCountMatchingItems(stack -> stack.getItem().equals(block.asItem()), count, player.inventoryMenu.getCraftSlots());
			}
			
			Veinminer.HISTORY.pop(player.getName().getString());
			
			return count;
		}
		
		return 0;
	}
	
	private static boolean playerHasItems(ServerPlayer player, Item item, int amount) throws Exception
	{
		if(player.gameMode.isCreative())
		{
			return true;
		}
		
		Inventory inventory = player.getInventory();
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
	
	private static boolean hasSpace(Level level, Set<BlockPos> positions) throws Exception
	{
		for(BlockPos pos : positions)
		{
			Block block = level.getBlockState(pos).getBlock();
			
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
	
	private static boolean hasNoCollidingEntities(Level level, Set<BlockPos> positions) throws Exception
	{
		for(BlockPos pos : positions)
		{
			if(!level.isUnobstructed(null, Shapes.create(new AABB(pos))))
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
