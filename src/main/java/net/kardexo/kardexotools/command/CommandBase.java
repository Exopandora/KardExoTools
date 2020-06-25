package net.kardexo.kardexotools.command;

import java.util.EnumSet;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.SuggestionProviders;
import net.minecraft.command.impl.TeleportCommand;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.network.play.server.SSetExperiencePacket;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
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
		TeleportCommand.teleport(source, player, world, position.getX() + 0.5F, position.getY(), position.getZ() + 0.5F, EnumSet.noneOf(SPlayerPositionLookPacket.Flags.class), player.rotationYaw, player.rotationPitch, null);
		player.connection.sendPacket(new SSetExperiencePacket(player.experience, player.experienceTotal, player.experienceLevel));
		return 1;
	}
	
	public static final DynamicCommandExceptionType BIOME_NOT_FOUND = new DynamicCommandExceptionType(biome -> new TranslationTextComponent("Unknown biome %s", new Object[]{biome}));
	
	public static final SuggestionProvider<CommandSource> ALL_BIOMES = SuggestionProviders.register(new ResourceLocation("all_biomes"), (context, builder) ->
	{
		return ISuggestionProvider.suggestIterable(Registry.BIOME.keySet(), builder);
	});
}
