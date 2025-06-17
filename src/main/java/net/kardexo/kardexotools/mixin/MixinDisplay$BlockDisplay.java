package net.kardexo.kardexotools.mixin;

import net.kardexo.kardexotools.mixinducks.IChair;
import net.minecraft.world.entity.Display;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Display.BlockDisplay.class)
public class MixinDisplay$BlockDisplay implements IChair
{
	@Unique
	private static final String TAG_IS_CHAIR = "kardexotools$is_chair";
	
	@Unique
	private boolean isChair;
	
	@Inject
	(
		method = "readAdditionalSaveData",
		at = @At("TAIL")
	)
	private void readAdditionalSaveData(ValueInput valueInput, CallbackInfo ci)
	{
		this.isChair = valueInput.getBooleanOr(TAG_IS_CHAIR, false);
	}
	
	@Inject
	(
		method = "addAdditionalSaveData",
		at = @At("TAIL")
	)
	private void addAdditionalSaveData(ValueOutput valueOutput, CallbackInfo ci)
	{
		valueOutput.putBoolean(TAG_IS_CHAIR, this.isChair);
	}
	
	@Override
	public void kardexotools$setChair(boolean isChair)
	{
		this.isChair = true;
	}
}
