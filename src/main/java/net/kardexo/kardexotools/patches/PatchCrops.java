package net.kardexo.kardexotools.patches;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

public class PatchCrops
{
	public static InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult, Block block, Item seed, IntegerProperty age, int maxAge, BlockState defaultState)
	{
		if(!level.isClientSide && state.getValue(age) >= maxAge)
		{
			List<ItemStack> drops = Block.getDrops(state, (ServerLevel) level, pos, null);
			
			for(ItemStack stack : drops)
			{
				if(stack.getItem().equals(seed))
				{
					stack.setCount(stack.getCount() - 1);
				}
			}
			
			player.awardStat(Stats.BLOCK_MINED.get(block));
			player.awardStat(Stats.ITEM_USED.get(seed));
			
			drops.forEach(drop ->
			{
				if(drop.getCount() > 0)
				{
					Block.popResource((ServerLevel) level, pos, drop);
				}
			});
			
			level.setBlock(pos, defaultState, 2);
			
			return InteractionResult.CONSUME;
		}
		
		return InteractionResult.PASS;
	}
}
