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

import net.kardexo.kardexotools.config.Config;
import net.kardexo.kardexotools.property.Property;
import net.kardexo.kardexotools.property.PropertyHelper;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.ColumnPosArgument;
import net.minecraft.command.arguments.DimensionArgument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.ColumnPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;

public class CommandPlaces
{
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(Commands.literal("places")
				.then(Commands.literal("add")
					.requires(source -> source.hasPermissionLevel(4))
					.then(Commands.argument("id", StringArgumentType.word())
						.then(Commands.argument("dimension", DimensionArgument.getDimension())
							.then(Commands.argument("from", ColumnPosArgument.columnPos())
								.then(Commands.argument("to", ColumnPosArgument.columnPos())
									.executes(context -> add(context.getSource(), StringArgumentType.getString(context, "id"), DimensionArgument.getDimensionArgument(context, "dimension"), ColumnPosArgument.fromBlockPos(context, "from"), ColumnPosArgument.fromBlockPos(context, "to"), null))
									.then(Commands.argument("title", StringArgumentType.greedyString())
										.executes(context -> add(context.getSource(), StringArgumentType.getString(context, "id"), DimensionArgument.getDimensionArgument(context, "dimension"), ColumnPosArgument.fromBlockPos(context, "from"), ColumnPosArgument.fromBlockPos(context, "to"), StringArgumentType.getString(context, "title")))))))))
				.then(Commands.literal("remove")
					.requires(source -> source.hasPermissionLevel(4))
					.then(Commands.argument("id", StringArgumentType.word())
						.executes(context -> remove(context.getSource(), StringArgumentType.getString(context, "id")))))
				.then(Commands.literal("list")
					.executes(context -> list(context.getSource())))
				.then(Commands.literal("reload")
					.requires(source -> source.hasPermissionLevel(4))
					.executes(context -> reload(context.getSource())))
				.then(Commands.literal("child")
					.requires(source -> source.hasPermissionLevel(4))
					.then(Commands.argument("id", StringArgumentType.word())
						.suggests(CommandPlaces::getSuggestions)
						.then(Commands.literal("add")
							.then(Commands.argument("child", StringArgumentType.word())
								.then(Commands.argument("dimension", DimensionArgument.getDimension())
									.then(Commands.argument("from", ColumnPosArgument.columnPos())
										.then(Commands.argument("to", ColumnPosArgument.columnPos())
											.executes(context -> addChild(context.getSource(), StringArgumentType.getString(context, "id"), StringArgumentType.getString(context, "child"), DimensionArgument.getDimensionArgument(context, "dimension"), ColumnPosArgument.fromBlockPos(context, "from"), ColumnPosArgument.fromBlockPos(context, "to"), null)))))))
											.then(Commands.argument("title", StringArgumentType.greedyString())
												.executes(context -> addChild(context.getSource(), StringArgumentType.getString(context, "id"), StringArgumentType.getString(context, "child"), DimensionArgument.getDimensionArgument(context, "dimension"), ColumnPosArgument.fromBlockPos(context, "from"), ColumnPosArgument.fromBlockPos(context, "to"), StringArgumentType.getString(context, "title"))))
						.then(Commands.literal("remove")
							.then(Commands.argument("child", StringArgumentType.word())
								.suggests(CommandPlaces::getChildSuggestions)
								.executes(context -> removeChild(context.getSource(), StringArgumentType.getString(context, "id"), StringArgumentType.getString(context, "child")))))))
				.then(Commands.literal("protection")
					.then(Commands.argument("id", StringArgumentType.word())
							.suggests(CommandPlaces::getSuggestions)
							.then(Commands.argument("enabled", BoolArgumentType.bool())
								.executes(context -> setProtection(context.getSource(), StringArgumentType.getString(context, "id"), BoolArgumentType.getBool(context, "enabled")))))));
	}
	
	private static int add(CommandSource source, String id, ServerWorld dimension, ColumnPos from, ColumnPos to, String title) throws CommandSyntaxException
	{
		try
		{
			PropertyHelper.add(id, dimension, from, to, source.getName(), title, Config.PLACES);
			source.sendFeedback(new StringTextComponent("Added base with id " + id), false);
		}
		catch(IllegalStateException e)
		{
			CommandBase.exception("Place with id " + id + " already exists");
		}
		
		return 1;
	}
	
	private static int remove(CommandSource source, String id) throws CommandSyntaxException
	{
		ensurePermission(source, id, null);
		
		try
		{
			PropertyHelper.remove(id, Config.PLACES);
			source.sendFeedback(new StringTextComponent("Removed base with id " + id), false);
		}
		catch(NoSuchElementException e)
		{
			throw CommandBase.exception("No such place with id " + id);
		}
		
		return 1;
	}
	
	private static int list(CommandSource source) throws CommandSyntaxException
	{
		try
		{
			return CommandProperty.list(source, Config.PLACES);
		}
		catch(NoSuchElementException e)
		{
			throw CommandBase.exception("There are no places");
		}
	}
	
	private static int reload(CommandSource source) throws CommandSyntaxException
	{
		try
		{
			Config.PLACES.read();
			source.sendFeedback(new StringTextComponent("Successfully reloaded places"), false);
		}
		catch(Exception e)
		{
			throw CommandBase.exception("Could not reload places");
		}
		
		return Config.PLACES.getData().size();
	}
	
	private static int addChild(CommandSource source, String id, String child, ServerWorld dimension, ColumnPos from, ColumnPos to, String title) throws CommandSyntaxException
	{
		ensurePermission(source, id, null);
		Property parent = getProperty(id);
		
		try
		{
			PropertyHelper.addChild(parent, child, dimension, from, to, title, Config.PLACES);
			source.sendFeedback(new StringTextComponent("Added child with id " + child + " to place with id " + id), false);
		}
		catch(IllegalStateException e)
		{
			throw CommandBase.exception("Child with id " + child + " already exists for place with id " + id);
		}
		
		return 1;
	}
	
	private static int removeChild(CommandSource source, String id, String child) throws CommandSyntaxException
	{
		ensurePermission(source, id, null);
		Property parent = getProperty(id);
		
		try
		{
			PropertyHelper.removeChild(parent, child, Config.PLACES);
			source.sendFeedback(new StringTextComponent("Removed child with id " + child + " from place with id " + id), false);
		}
		catch(NoSuchElementException e)
		{
			throw CommandBase.exception("No child with id " + child + " for place with id " + id);
		}
		
		return 1;
	}
	
	private static int setProtection(CommandSource source, String id, boolean enabled) throws CommandSyntaxException
	{
		ensurePermission(source, id, null);
		getProperty(id).setProtected(enabled);
		Config.PLACES.save();
		
		if(enabled)
		{
			source.sendFeedback(new StringTextComponent("Enabled protection for place with id " + id), false);
		}
		else
		{
			source.sendFeedback(new StringTextComponent("Disabled protection for place with id " + id), false);
		}
		
		return 1;
	}
	
	private static void ensurePermission(CommandSource source, String id, PlayerEntity target) throws CommandSyntaxException
	{
		if(!PropertyHelper.hasPermission(source, id, target, Config.PLACES))
		{
			throw CommandBase.exception("You must be a creator of place with id " + id);
		}
	}
	
	private static Property getProperty(String id) throws CommandSyntaxException
	{
		try
		{
			return PropertyHelper.getProperty(id, Config.PLACES);
		}
		catch(NoSuchElementException e)
		{
			throw CommandBase.exception("No such place with id " + id);
		}
	}
	
	private static CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException
	{
		return ISuggestionProvider.suggest(Config.PLACES.getData().keySet(), builder);
	}
	
	private static CompletableFuture<Suggestions> getChildSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException
	{
		return CommandProperty.getChildSuggestions(Config.PLACES, context, builder, StringArgumentType.getString(context, "id"));
	}
}
