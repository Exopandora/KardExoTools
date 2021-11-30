package net.kardexo.kardexotools.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.kardexo.kardexotools.patches.CropsPatch;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

@Mixin(NetherWartBlock.class)
public abstract class MixinNetherWartBlock extends BushBlock
{
	protected MixinNetherWartBlock(Properties properties)
	{
		super(properties);
	}
	
	@Override
	public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult)
	{
		return CropsPatch.use(blockState, level, blockPos, player, interactionHand, blockHitResult, (NetherWartBlock) (Object) this, this.asItem(), NetherWartBlock.AGE, NetherWartBlock.MAX_AGE, this.defaultBlockState().setValue(NetherWartBlock.AGE, Integer.valueOf(0)));
	}
}
