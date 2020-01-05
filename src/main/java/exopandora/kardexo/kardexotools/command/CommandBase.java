package exopandora.kardexo.kardexotools.command;

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
	
	public static void teleport(CommandSource source, ServerPlayerEntity player, ServerWorld world, BlockPos position)
	{
		TeleportCommand.teleport(source, player, world, position.getX(), position.getY(), position.getZ(), EnumSet.noneOf(SPlayerPositionLookPacket.Flags.class), player.rotationYaw, player.rotationPitch, null);
		player.connection.sendPacket(new SSetExperiencePacket(player.experience, player.experienceTotal, player.experienceLevel));
	}
}
