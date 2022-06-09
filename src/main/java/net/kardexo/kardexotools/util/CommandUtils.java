package net.kardexo.kardexotools.util;

import java.util.EnumSet;
import java.util.UUID;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.server.commands.TeleportCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class CommandUtils
{
	public static CommandSyntaxException simpleException(String message)
	{
		return CommandUtils.simpleException(Component.translatable(message));
	}
	
	public static CommandSyntaxException simpleException(Component message)
	{
		return new SimpleCommandExceptionType(message).create();
	}
	
	public static int teleport(CommandSourceStack source, ServerPlayer player, ServerLevel level, BlockPos position) throws CommandSyntaxException
	{
		TeleportCommand.performTeleport(source, player, level, position.getX() + 0.5F, position.getY(), position.getZ() + 0.5F, EnumSet.noneOf(ClientboundPlayerPositionPacket.RelativeArgument.class), player.getYRot(), player.getXRot(), null);
		player.connection.send(new ClientboundSetExperiencePacket(player.experienceProgress, player.totalExperience, player.experienceLevel));
		return 1;
	}
	
	public static UUID getUUID(CommandSourceStack source)
	{
		if(source.getEntity() == null)
		{
			return Util.NIL_UUID;
		}
		
		return source.getEntity().getUUID();
	}
}
