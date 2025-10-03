package net.kardexo.kardexotools.patches;

import net.kardexo.kardexotools.KardExo;
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

import java.util.List;

public class CropsPatch
{
	public static InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, Block block, Item seed, IntegerProperty age, int maxAge, BlockState defaultState)
	{
		if(level.isClientSide() || !KardExo.CONFIG.getData().doHarvestCropsWithRightClick() || state.getValue(age) < maxAge)
		{
			return InteractionResult.PASS;
		}
		
		List<ItemStack> drops = Block.getDrops(state, (ServerLevel) level, pos, null);
		
		for(ItemStack drop : drops)
		{
			if(drop.getItem().equals(seed))
			{
				drop.shrink(1);
			}
			
			if(drop.getCount() > 0)
			{
				Block.popResource(level, pos, drop);
			}
		}
		
		player.awardStat(Stats.BLOCK_MINED.get(block));
		player.awardStat(Stats.ITEM_USED.get(seed));
		level.setBlock(pos, defaultState, 2);
		return InteractionResult.CONSUME;
	}
}
