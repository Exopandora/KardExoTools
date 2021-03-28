package net.kardexo.kardexotools.command;

import java.util.EnumSet;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.command.CommandSource;
import net.minecraft.command.impl.TeleportCommand;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.network.play.server.SSetExperiencePacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;

public class CommandBase
{
	public static CommandSyntaxException exception(String message)
	{
		return new SimpleCommandExceptionType(new TranslationTextComponent(message)).create();
	}
	
	public static int teleport(CommandSource source, ServerPlayerEntity player, ServerWorld world, BlockPos position) throws CommandSyntaxException
	{
		TeleportCommand.performTeleport(source, player, world, position.getX() + 0.5F, position.getY(), position.getZ() + 0.5F, EnumSet.noneOf(SPlayerPositionLookPacket.Flags.class), player.yRot, player.xRot, null);
		player.connection.send(new SSetExperiencePacket(player.experienceProgress, player.totalExperience, player.experienceLevel));
		return 1;
	}
}
