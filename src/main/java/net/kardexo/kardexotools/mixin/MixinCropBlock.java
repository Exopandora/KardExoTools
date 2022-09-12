package net.kardexo.kardexotools.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.kardexo.kardexotools.patches.CropsPatch;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

@Mixin(CropBlock.class)
public abstract class MixinCropBlock extends BushBlock implements BonemealableBlock
{
	protected MixinCropBlock(Properties properties)
	{
		super(properties);
	}
	
	@Shadow
	protected abstract ItemLike getBaseSeedId();
	
	@Shadow
	public abstract IntegerProperty getAgeProperty();
	
	@Shadow
	public abstract int getMaxAge();
	
	@Shadow
	public abstract BlockState getStateForAge(int age);
	
	@Override
	public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand hand, BlockHitResult hitResult)
	{
		return CropsPatch.use(blockState, level, blockPos, player, hand, hitResult, (CropBlock) (Object) this, this.getBaseSeedId().asItem(), this.getAgeProperty(), this.getMaxAge(), this.getStateForAge(0));
	}
}
