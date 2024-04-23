package net.kardexo.kardexotools.mixin;

import net.kardexo.kardexotools.patches.CropsPatch;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = NetherWartBlock.class, priority = 1001)
public abstract class MixinNetherWartBlock extends BushBlock
{
	protected MixinNetherWartBlock(Properties properties)
	{
		super(properties);
	}
	
	@Override
	protected @NotNull InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult)
	{
		return CropsPatch.useWithoutItem(blockState, level, blockPos, player, this, this.asItem(), NetherWartBlock.AGE, NetherWartBlock.MAX_AGE, this.defaultBlockState().setValue(NetherWartBlock.AGE, 0));
	}
}
