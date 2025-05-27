package net.kardexo.kardexotools.mixin;

import net.kardexo.kardexotools.mixinducks.IChair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Display;
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
	private void readAdditionalSaveData(CompoundTag compoundTag, CallbackInfo ci)
	{
		this.isChair = compoundTag.getBooleanOr(TAG_IS_CHAIR, false);
	}
	
	@Inject
	(
		method = "addAdditionalSaveData",
		at = @At("TAIL")
	)
	private void addAdditionalSaveData(CompoundTag compoundTag, CallbackInfo ci)
	{
		compoundTag.putBoolean(TAG_IS_CHAIR, this.isChair);
	}
	
	@Override
	public void kardexotools$setChair(boolean isChair)
	{
		this.isChair = true;
	}
}
