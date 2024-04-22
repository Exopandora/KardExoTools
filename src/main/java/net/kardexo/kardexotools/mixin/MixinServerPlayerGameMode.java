package net.kardexo.kardexotools.mixin;

import net.kardexo.kardexotools.util.PropertyUtils;
import net.kardexo.kardexotools.veinminer.Veinminer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public class MixinServerPlayerGameMode
{
	@Shadow
	protected ServerLevel level;
	
	@Shadow
	protected ServerPlayer player;
	
	@Unique
	private boolean isVeinMining = false;
	
	@Unique
	private boolean dropAtPlayer = false;
	
	@Inject
	(
		method = "destroyBlock",
		at = @At("HEAD"),
		cancellable = true
	)
	public void destroyBlock(BlockPos blockPos, CallbackInfoReturnable<Boolean> info)
	{
		if(PropertyUtils.canHarvestBlock(this.player, blockPos))
		{
			if(!this.isVeinMining)
			{
				this.isVeinMining = true;
				this.dropAtPlayer = false;
				info.setReturnValue(Veinminer.mine((ServerPlayerGameMode) (Object) this, blockPos, this.player, this.level, dropAtPlayer ->
				{
					this.dropAtPlayer = dropAtPlayer;
				}));
				this.isVeinMining = false;
				info.cancel();
			}
		}
		else
		{
			info.setReturnValue(false);
			info.cancel();
		}
	}
	
	@ModifyArg
	(
		method = "destroyBlock",
		at = @At
		(
			value = "INVOKE",
			target = "net/minecraft/world/level/block/Block.playerDestroy(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/item/ItemStack;)V"
		),
		index = 2
	)
	private BlockPos changeDropPos(BlockPos blockPos)
	{
		return this.dropAtPlayer ? this.player.blockPosition() : blockPos;
	}
}
