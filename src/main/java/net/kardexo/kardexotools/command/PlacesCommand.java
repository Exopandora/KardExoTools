package net.kardexo.kardexotools.command;

import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.kardexo.kardexotools.KardExo;
import net.kardexo.kardexotools.property.Property;
import net.kardexo.kardexotools.property.PropertyHelper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class PlacesCommand
{
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		dispatcher.register(Commands.literal("places")
				.then(Commands.literal("add")
					.requires(source -> source.hasPermission(4))
					.then(Commands.argument("id", StringArgumentType.word())
						.then(Commands.argument("dimension", DimensionArgument.dimension())
							.then(Commands.argument("from", BlockPosArgument.blockPos())
								.then(Commands.argument("to", BlockPosArgument.blockPos())
									.executes(context -> add(context.getSource(), StringArgumentType.getString(context, "id"), DimensionArgument.getDimension(context, "dimension"), BoundingBox.fromCorners(BlockPosArgument.getLoadedBlockPos(context, "from"), BlockPosArgument.getLoadedBlockPos(context, "to")), null))
									.then(Commands.argument("displayName", ComponentArgument.textComponent())
										.executes(context -> add(context.getSource(), StringArgumentType.getString(context, "id"), DimensionArgument.getDimension(context, "dimension"), BoundingBox.fromCorners(BlockPosArgument.getLoadedBlockPos(context, "from"), BlockPosArgument.getLoadedBlockPos(context, "to")), ComponentArgument.getComponent(context, "displayName")))))))))
				.then(Commands.literal("remove")
					.requires(source -> source.hasPermission(4))
					.then(Commands.argument("id", StringArgumentType.word())
						.executes(context -> remove(context.getSource(), StringArgumentType.getString(context, "id")))))
				.then(Commands.literal("list")
					.executes(context -> list(context.getSource())))
				.then(Commands.literal("reload")
					.requires(source -> source.hasPermission(4))
					.executes(context -> reload(context.getSource())))
				.then(Commands.literal("children")
					.requires(source -> source.hasPermission(4))
					.then(Commands.argument("id", StringArgumentType.word())
						.suggests(PlacesCommand::getSuggestions)
						.then(Commands.literal("add")
							.then(Commands.argument("child", StringArgumentType.word())
								.then(Commands.argument("dimension", DimensionArgument.dimension())
									.then(Commands.argument("from", BlockPosArgument.blockPos())
										.then(Commands.argument("to", BlockPosArgument.blockPos())
											.executes(context -> addChild(context.getSource(), StringArgumentType.getString(context, "id"), StringArgumentType.getString(context, "child"), DimensionArgument.getDimension(context, "dimension"), BoundingBox.fromCorners(BlockPosArgument.getLoadedBlockPos(context, "from"), BlockPosArgument.getLoadedBlockPos(context, "to")), null))
											.then(Commands.argument("displayName", ComponentArgument.textComponent())
													.executes(context -> addChild(context.getSource(), StringArgumentType.getString(context, "id"), StringArgumentType.getString(context, "child"), DimensionArgument.getDimension(context, "dimension"), BoundingBox.fromCorners(BlockPosArgument.getLoadedBlockPos(context, "from"), BlockPosArgument.getLoadedBlockPos(context, "to")), ComponentArgument.getComponent(context, "displayName")))))))))
						.then(Commands.literal("remove")
							.then(Commands.argument("child", StringArgumentType.word())
								.suggests(PlacesCommand::getChildSuggestions)
								.executes(context -> removeChild(context.getSource(), StringArgumentType.getString(context, "id"), StringArgumentType.getString(context, "child")))))
						.then(Commands.literal("list")
								.executes(context -> listChildren(context.getSource(), StringArgumentType.getString(context, "id"))))))
				.then(Commands.literal("protection")
					.then(Commands.argument("id", StringArgumentType.word())
							.suggests(PlacesCommand::getSuggestions)
							.then(Commands.argument("enabled", BoolArgumentType.bool())
								.executes(context -> setProtection(context.getSource(), StringArgumentType.getString(context, "id"), BoolArgumentType.getBool(context, "enabled")))))));
	}
	
	private static int add(CommandSourceStack source, String id, ServerLevel dimension, BoundingBox boundingBox, Component displayName) throws CommandSyntaxException
	{
		try
		{
			PropertyHelper.add(id, dimension, boundingBox, null, displayName, KardExo.PLACES_FILE);
			source.sendSuccess(new TextComponent("Added base with id " + id), false);
		}
		catch(IllegalStateException e)
		{
			throw CommandUtils.simpleException("Place with id " + id + " already exists");
		}
		
		return 1;
	}
	
	private static int remove(CommandSourceStack source, String id) throws CommandSyntaxException
	{
		ensurePermission(source, id, null);
		
		try
		{
			PropertyHelper.remove(id, KardExo.PLACES_FILE);
			source.sendSuccess(new TextComponent("Removed base with id " + id), false);
		}
		catch(NoSuchElementException e)
		{
			throw CommandUtils.simpleException("No such place with id " + id);
		}
		
		return 1;
	}
	
	private static int list(CommandSourceStack source) throws CommandSyntaxException
	{
		try
		{
			return PropertyCommandUtils.list(source, KardExo.PLACES);
		}
		catch(NoSuchElementException e)
		{
			throw CommandUtils.simpleException("There are no places");
		}
	}
	
	private static int reload(CommandSourceStack source) throws CommandSyntaxException
	{
		try
		{
			KardExo.PLACES_FILE.read();
			source.sendSuccess(new TextComponent("Successfully reloaded places"), false);
		}
		catch(Exception e)
		{
			throw CommandUtils.simpleException("Could not reload places");
		}
		
		return KardExo.PLACES.size();
	}
	
	private static int addChild(CommandSourceStack source, String id, String child, ServerLevel dimension, BoundingBox boundingBox, Component displayName) throws CommandSyntaxException
	{
		ensurePermission(source, id, null);
		Property parent = getPlace(id);
		
		try
		{
			PropertyHelper.addChild(parent, child, dimension, boundingBox, displayName, KardExo.PLACES_FILE);
			source.sendSuccess(new TextComponent("Added child with id " + child + " to place with id " + id), false);
		}
		catch(IllegalStateException e)
		{
			throw CommandUtils.simpleException("Child with id " + child + " already exists for place with id " + id);
		}
		
		return 1;
	}
	
	private static int removeChild(CommandSourceStack source, String id, String child) throws CommandSyntaxException
	{
		ensurePermission(source, id, null);
		Property parent = getPlace(id);
		
		try
		{
			PropertyHelper.removeChild(parent, child, KardExo.PLACES_FILE);
			source.sendSuccess(new TextComponent("Removed child with id " + child + " from place with id " + id), false);
		}
		catch(NoSuchElementException e)
		{
			throw CommandUtils.simpleException("No child with id " + child + " for place with id " + id);
		}
		
		return 1;
	}
	
	private static int listChildren(CommandSourceStack source, String id) throws CommandSyntaxException
	{
		try
		{
			return PropertyCommandUtils.list(source, getPlace(id).getChildren());
		}
		catch(NoSuchElementException e)
		{
			throw CommandUtils.simpleException("There are no children for place with id " + id);
		}
	}
	
	private static int setProtection(CommandSourceStack source, String id, boolean enabled) throws CommandSyntaxException
	{
		ensurePermission(source, id, null);
		getPlace(id).setProtected(enabled);
		KardExo.PLACES_FILE.save();
		
		if(enabled)
		{
			source.sendSuccess(new TextComponent("Enabled protection for place with id " + id), false);
		}
		else
		{
			source.sendSuccess(new TextComponent("Disabled protection for place with id " + id), false);
		}
		
		return 1;
	}
	
	private static void ensurePermission(CommandSourceStack source, String id, Player target) throws CommandSyntaxException
	{
		if(!PropertyHelper.hasPermission(source, id, target, KardExo.PLACES))
		{
			throw CommandUtils.simpleException("You must be a creator of place with id " + id);
		}
	}
	
	private static Property getPlace(String id) throws CommandSyntaxException
	{
		try
		{
			return PropertyHelper.getProperty(id, KardExo.PLACES);
		}
		catch(NoSuchElementException e)
		{
			throw CommandUtils.simpleException("No such place with id " + id);
		}
	}
	
	private static CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) throws CommandSyntaxException
	{
		return SharedSuggestionProvider.suggest(KardExo.PLACES.keySet(), builder);
	}
	
	private static CompletableFuture<Suggestions> getChildSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) throws CommandSyntaxException
	{
		return PropertyCommandUtils.getChildSuggestions(KardExo.PLACES_FILE, context, builder, StringArgumentType.getString(context, "id"));
	}
}
