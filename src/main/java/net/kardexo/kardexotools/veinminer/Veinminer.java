package net.kardexo.kardexotools.veinminer;

import com.google.common.collect.Sets;
import net.kardexo.kardexotools.KardExo;
import net.kardexo.kardexotools.config.VeinConfig;
import net.kardexo.kardexotools.util.BlockPredicateWrapper;
import net.kardexo.kardexotools.util.PropertyUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class Veinminer
{
	private static final PlayerHistory<Vein> HISTORY = new PlayerHistory<Vein>(KardExo.CONFIG.getData().getVeinminerHistorySize());
	
	public static boolean mine(ServerPlayerGameMode gameMode, BlockPos pos, ServerPlayer player, ServerLevel level, Consumer<Boolean> dropAtPlayer)
	{
		UUID uuid = player.getUUID();
		ItemStack item = player.getMainHandItem();
		BlockState state = level.getBlockState(pos);
		boolean isEffectiveTool = item.getDestroySpeed(state) > 1.0F;
		
		if(KardExo.PLAYERS.getData().containsKey(uuid) && KardExo.PLAYERS.get(uuid).isVeinminerEnabled() && player.isShiftKeyDown() && !player.onClimbable() && (!item.isDamageableItem() || item.getMaxDamage() - item.getDamageValue() > 1))
		{
			for(Entry<BlockPredicateWrapper, VeinConfig> entry : KardExo.VEINMINER.getData().entrySet())
			{
				BlockPredicateWrapper predicate = entry.getKey();
				VeinConfig config = entry.getValue();
				
				if(predicate.matches(level, pos) && (isEffectiveTool || !config.doesRequireTool()))
				{
					PriorityQueue<BlockPos> queue = Veinminer.calculateVein(player, KardExo.CONFIG.getData().getVeinminerBlockLimit(), predicate, config, pos, level);
					Map<BlockState, Set<BlockPos>> stateMap = new HashMap<BlockState, Set<BlockPos>>();
					Vein undo = new Vein(player.level().dimension(), stateMap);
					queue.poll();
					
					if(!queue.isEmpty())
					{
						dropAtPlayer.accept(true);
						BlockState next = state;
						boolean harvest = gameMode.destroyBlock(pos);
						
						if(harvest)
						{
							stateMap.put(next, Sets.newHashSet(pos));
							
							for(int x = 0; x < KardExo.CONFIG.getData().getVeinminerBlockLimit(); x++)
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
								
								if(!gameMode.destroyBlock(queue.peek()))
								{
									break;
								}
								
								stateMap.computeIfAbsent(next, key -> Sets.newHashSet()).add(queue.poll());
							}
							
							if(Veinminer.count(stateMap.values()) > 1)
							{
								Veinminer.HISTORY.add(uuid, undo);
							}
						}
						
						return harvest;
					}
				}
			}
		}
		
		return gameMode.destroyBlock(pos);
	}
	
	private static PriorityQueue<BlockPos> calculateVein(Player player, int limit, BlockPredicateWrapper predicate, VeinConfig config, BlockPos pos, ServerLevel level)
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
				final int[] positions = {-1, 0, 1};
				
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
							
							if(nextBlock.distSqr(pos) >= config.getRadius() * config.getRadius())
							{
								continue;
							}
							
							if(!predicate.matches(level, nextBlock))
							{
								continue;
							}
							
							if(queue.contains(nextBlock) || pending.contains(nextBlock) || next.contains(nextBlock))
							{
								continue;
							}
							
							if(!PropertyUtils.canHarvestBlock(player, nextBlock))
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
	
	private static Comparator<BlockPos> comparator(BlockPos origin)
	{
		return (a, b) -> (int) (a.distSqr(origin) - b.distSqr(origin));
	}
	
	public static int undo(ServerPlayer player, MinecraftServer server) throws Exception
	{
		Vein undo = Veinminer.HISTORY.peek(player.getUUID());
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
			
			Veinminer.HISTORY.pop(player.getUUID());
			
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
		
		int count = player.getInventory().countItem(item);
		
		if(count < amount)
		{
			throw new Exception("You do not have enough items");
		}
		
		return true;
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
	
	public static boolean hasUndo(UUID uuid)
	{
		return Veinminer.HISTORY.hasUndo(uuid);
	}
}
