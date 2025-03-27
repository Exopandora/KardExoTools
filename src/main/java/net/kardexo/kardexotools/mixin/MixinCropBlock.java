package net.kardexo.kardexotools.mixin;

import net.kardexo.kardexotools.patches.CropsPatch;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CropBlock.class)
public abstract class MixinCropBlock extends VegetationBlock implements BonemealableBlock
{
	protected MixinCropBlock(Properties properties)
	{
		super(properties);
	}
	
	@Shadow
	abstract ItemLike getBaseSeedId();
	
	@Shadow
	abstract IntegerProperty getAgeProperty();
	
	@Shadow
	abstract int getMaxAge();
	
	@Shadow
	abstract BlockState getStateForAge(int age);
	
	@Override
	protected @NotNull InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult)
	{
		return CropsPatch.useWithoutItem(blockState, level, blockPos, player, this, this.getBaseSeedId().asItem(), this.getAgeProperty(), this.getMaxAge(), this.getStateForAge(0));
	}
}
