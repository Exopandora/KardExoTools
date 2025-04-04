package net.kardexo.kardexotools.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerGamePacketListenerImpl.class)
public class MixinServerGamePacketListenerImpl
{
	@Redirect
	(
		method = "handleUseItemOn",
		at = @At
		(
			value = "INVOKE",
			target = "Lnet/minecraft/server/level/ServerLevel;mayInteract(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/BlockPos;)Z"
		)
	)
	public boolean handleUseItemOn_mayInteract(ServerLevel level, Entity entity, BlockPos pos, ServerboundUseItemOnPacket packet)
	{
		return level.mayInteract(entity, pos) && level.mayInteract(entity, pos.relative(packet.getHitResult().getDirection()));
	}
}
