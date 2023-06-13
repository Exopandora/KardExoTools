package net.kardexo.kardexotools.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess;

@Mixin(MinecraftServer.class)
public interface AccessorMinecraftServer
{
	@Accessor
	LevelStorageAccess getStorageSource();
}
