package exopandora.kardexo.kardexotools.command;

import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import exopandora.kardexo.kardexotools.base.Property;
import exopandora.kardexo.kardexotools.config.Config;
import exopandora.kardexo.kardexotools.tasks.TickableBases;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.ColumnPosArgument;
import net.minecraft.command.arguments.DimensionArgument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.ColumnPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.dimension.DimensionType;

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
									.executes(context -> add(context.getSource(), StringArgumentType.getString(context, "id"), DimensionArgument.func_212592_a(context, "dimension"), ColumnPosArgument.func_218101_a(context, "from"), ColumnPosArgument.func_218101_a(context, "to"), null))
									.then(Commands.argument("title", StringArgumentType.greedyString())
										.executes(context -> add(context.getSource(), StringArgumentType.getString(context, "id"), DimensionArgument.func_212592_a(context, "dimension"), ColumnPosArgument.func_218101_a(context, "from"), ColumnPosArgument.func_218101_a(context, "to"), StringArgumentType.getString(context, "title")))))))))
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
											.executes(context -> addChild(context.getSource(), StringArgumentType.getString(context, "id"), StringArgumentType.getString(context, "child"), DimensionArgument.func_212592_a(context, "dimension"), ColumnPosArgument.func_218101_a(context, "from"), ColumnPosArgument.func_218101_a(context, "to"), null)))))))
											.then(Commands.argument("title", StringArgumentType.greedyString())
												.executes(context -> addChild(context.getSource(), StringArgumentType.getString(context, "id"), StringArgumentType.getString(context, "child"), DimensionArgument.func_212592_a(context, "dimension"), ColumnPosArgument.func_218101_a(context, "from"), ColumnPosArgument.func_218101_a(context, "to"), StringArgumentType.getString(context, "title"))))
						.then(Commands.literal("remove")
							.then(Commands.argument("child", StringArgumentType.word())
								.suggests(CommandPlaces::getChildSuggestions)
								.executes(context -> removeChild(context.getSource(), StringArgumentType.getString(context, "id"), StringArgumentType.getString(context, "child"))))))));
	}
	
	private static int add(CommandSource source, String id, DimensionType dimension, ColumnPos from, ColumnPos to, String title) throws CommandSyntaxException
	{
		try
		{
			CommandProperty.add(id, dimension, from, to, source.getName(), title, Config.PLACES);
			source.sendFeedback(new StringTextComponent("Added base with id " + id), false);
		}
		catch(IllegalStateException e)
		{
			CommandBase.createException("Place with id " + id + " already exists");
		}
		
		return 1;
	}
	
	private static int remove(CommandSource source, String id) throws CommandSyntaxException
	{
		ensurePermission(source, id, null);
		
		try
		{
			CommandProperty.remove(id, Config.PLACES);
			source.sendFeedback(new StringTextComponent("Removed base with id " + id), false);
		}
		catch(NoSuchElementException e)
		{
			throw CommandBase.createException("No such place with id " + id);
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
			throw CommandBase.createException("There are no places");
		}
	}
	
	private static int reload(CommandSource source) throws CommandSyntaxException
	{
		TickableBases.BASE_VISITORS.clear();
		
		try
		{
			Config.PLACES.read();
			source.sendFeedback(new StringTextComponent("Successfully reloaded places"), false);
		}
		catch(Exception e)
		{
			throw CommandBase.createException("Could not reload places");
		}
		
		return Config.PLACES.getData().size();
	}
	
	private static int addChild(CommandSource source, String id, String child, DimensionType dimension, ColumnPos from, ColumnPos to, String title) throws CommandSyntaxException
	{
		ensurePermission(source, id, null);
		Property parent = getProperty(id);
		
		try
		{
			CommandProperty.addChild(parent, child, dimension, from, to, title, Config.PLACES);
			source.sendFeedback(new StringTextComponent("Added child with id " + child + " to place with id " + id), false);
		}
		catch(IllegalStateException e)
		{
			throw CommandBase.createException("Child with id " + child + " already exists for place with id " + id);
		}
		
		return 1;
	}
	
	private static int removeChild(CommandSource source, String id, String child) throws CommandSyntaxException
	{
		ensurePermission(source, id, null);
		Property parent = getProperty(id);
		
		try
		{
			CommandProperty.removeChild(parent, child, Config.PLACES);
			source.sendFeedback(new StringTextComponent("Removed child with id " + child + " from place with id " + id), false);
		}
		catch(NoSuchElementException e)
		{
			throw CommandBase.createException("No child with id " + child + " for place with id " + id);
		}
		
		return 1;
	}
	
	private static void ensurePermission(CommandSource source, String id, PlayerEntity target) throws CommandSyntaxException
	{
		if(!CommandProperty.hasPermission(source, id, target, Config.PLACES))
		{
			throw CommandBase.createException("You must be a creator of place with id " + id);
		}
	}
	
	private static Property getProperty(String id) throws CommandSyntaxException
	{
		try
		{
			return CommandProperty.getProperty(id, Config.PLACES);
		}
		catch(NoSuchElementException e)
		{
			throw CommandBase.createException("No such place with id " + id);
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
