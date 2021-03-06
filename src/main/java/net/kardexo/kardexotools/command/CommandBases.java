package net.kardexo.kardexotools.command;

import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

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
import net.kardexo.kardexotools.property.PropertyOwner;
import net.kardexo.kardexotools.tasks.TickableBases;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

public class CommandBases
{
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		dispatcher.register(Commands.literal("bases")
				.then(Commands.literal("add")
					.then(Commands.argument("id", StringArgumentType.word())
						.then(Commands.argument("dimension", DimensionArgument.dimension())
							.then(Commands.argument("from", ColumnPosArgument.columnPos())
								.then(Commands.argument("to", ColumnPosArgument.columnPos())
									.then(Commands.argument("owner", EntityArgument.player())
										.executes(context -> add(context.getSource(), StringArgumentType.getString(context, "id"), DimensionArgument.getDimension(context, "dimension"), ColumnPosArgument.getColumnPos(context, "from"), ColumnPosArgument.getColumnPos(context, "to"), EntityArgument.getPlayer(context, "owner"), null))
										.then(Commands.argument("title", StringArgumentType.greedyString())
											.executes(context -> add(context.getSource(), StringArgumentType.getString(context, "id"), DimensionArgument.getDimension(context, "dimension"), ColumnPosArgument.getColumnPos(context, "from"), ColumnPosArgument.getColumnPos(context, "to"), EntityArgument.getPlayer(context, "owner"), StringArgumentType.getString(context, "title"))))))))))
				.then(Commands.literal("remove")
					.then(Commands.argument("id", StringArgumentType.word())
						.suggests(CommandBases::getSuggestions)
						.executes(context -> remove(context.getSource(), StringArgumentType.getString(context, "id")))))
				.then(Commands.literal("messages")
					.then(Commands.argument("id", StringArgumentType.word())
						.suggests(CommandBases::getSuggestions)
						.then(Commands.literal("set")
							.then(Commands.argument("owner", EntityArgument.player())
								.then(Commands.literal("enter")
									.then(Commands.argument("message", StringArgumentType.greedyString())
										.executes(context -> setEnterMessage(context.getSource(), StringArgumentType.getString(context, "id"), EntityArgument.getPlayer(context, "owner"), StringArgumentType.getString(context, "message")))))
								.then(Commands.literal("exit")
									.then(Commands.argument("message", StringArgumentType.greedyString())
										.executes(context -> setExitMessage(context.getSource(), StringArgumentType.getString(context, "id"), EntityArgument.getPlayer(context, "owner"), StringArgumentType.getString(context, "message")))))
								.then(Commands.literal("both")
									.then(Commands.argument("message", StringArgumentType.greedyString())
										.executes(context -> setBothMessages(context.getSource(), StringArgumentType.getString(context, "id"), EntityArgument.getPlayer(context, "owner"), StringArgumentType.getString(context, "message")))))))
						.then(Commands.literal("reset")
							.then(Commands.argument("owner", EntityArgument.player())
								.then(Commands.literal("enter")
									.executes(context -> setEnterMessage(context.getSource(), StringArgumentType.getString(context, "id"), EntityArgument.getPlayer(context, "owner"), null)))
								.then(Commands.literal("exit")
									.executes(context -> setEnterMessage(context.getSource(), StringArgumentType.getString(context, "id"), EntityArgument.getPlayer(context, "owner"), null)))
								.then(Commands.literal("both")
									.executes(context -> setEnterMessage(context.getSource(), StringArgumentType.getString(context, "id"), EntityArgument.getPlayer(context, "owner"), null)))))
						.then(Commands.literal("notify")
							.then(Commands.argument("owner", EntityArgument.player())
								.then(Commands.argument("notify", BoolArgumentType.bool())
									.executes(context -> setNotify(context.getSource(), StringArgumentType.getString(context, "id"), EntityArgument.getPlayer(context, "owner"), BoolArgumentType.getBool(context, "notify"))))))))
				.then(Commands.literal("owners")
					.then(Commands.argument("id", StringArgumentType.word())
						.suggests(CommandBases::getSuggestions)
						.then(Commands.literal("add")
							.then(Commands.argument("player", EntityArgument.player())
								.executes(context -> addOwner(context.getSource(), StringArgumentType.getString(context, "id"), EntityArgument.getPlayer(context, "player"), false))
								.then(Commands.argument("creator", BoolArgumentType.bool())
									.executes(context -> addOwner(context.getSource(), StringArgumentType.getString(context, "id"), EntityArgument.getPlayer(context, "player"), BoolArgumentType.getBool(context, "creator"))))))
						.then(Commands.literal("remove")
							.then(Commands.argument("player", EntityArgument.player())
								.executes(context -> removeOwner(context.getSource(), StringArgumentType.getString(context, "id"), EntityArgument.getPlayer(context, "player")))))
						.then(Commands.literal("set")
							.then(Commands.argument("player", EntityArgument.player())
								.then(Commands.literal("owner")
									.executes(context -> setOwner(context.getSource(), StringArgumentType.getString(context, "id"), EntityArgument.getPlayer(context, "player"), false)))
								.then(Commands.literal("creator")
									.executes(context -> setOwner(context.getSource(), StringArgumentType.getString(context, "id"), EntityArgument.getPlayer(context, "player"), true)))))))
				.then(Commands.literal("list")
					.executes(context -> list(context.getSource())))
				.then(Commands.literal("reload")
					.requires(source -> source.hasPermission(4))
						.executes(context -> reload(context.getSource())))
				.then(Commands.literal("child")
					.then(Commands.argument("id", StringArgumentType.word())
						.suggests(CommandBases::getSuggestions)
						.then(Commands.literal("add")
							.then(Commands.argument("child", StringArgumentType.word())
								.then(Commands.argument("dimension", DimensionArgument.dimension())
									.then(Commands.argument("from", ColumnPosArgument.columnPos())
										.then(Commands.argument("to", ColumnPosArgument.columnPos())
											.executes(context -> addChild(context.getSource(), StringArgumentType.getString(context, "id"), StringArgumentType.getString(context, "child"), DimensionArgument.getDimension(context, "dimension"), ColumnPosArgument.getColumnPos(context, "from"), ColumnPosArgument.getColumnPos(context, "to"), null)))))))
											.then(Commands.argument("title", StringArgumentType.greedyString())
												.executes(context -> addChild(context.getSource(), StringArgumentType.getString(context, "id"), StringArgumentType.getString(context, "child"), DimensionArgument.getDimension(context, "dimension"), ColumnPosArgument.getColumnPos(context, "from"), ColumnPosArgument.getColumnPos(context, "to"), StringArgumentType.getString(context, "title"))))
						.then(Commands.literal("remove")
							.then(Commands.argument("child", StringArgumentType.word())
								.suggests(CommandBases::getChildSuggestions)
								.executes(context -> removeChild(context.getSource(), StringArgumentType.getString(context, "id"), StringArgumentType.getString(context, "child")))))))
				.then(Commands.literal("protection")
					.then(Commands.argument("id", StringArgumentType.word())
							.suggests(CommandBases::getSuggestions)
							.then(Commands.argument("enabled", BoolArgumentType.bool())
								.executes(context -> setProtection(context.getSource(), StringArgumentType.getString(context, "id"), BoolArgumentType.getBool(context, "enabled")))))));
	}
	
	private static int add(CommandSourceStack source, String id, ServerLevel dimension, ColumnPos from, ColumnPos to, Player owner, String title) throws CommandSyntaxException
	{
		try
		{
			PropertyHelper.add(id, dimension, from, to, owner.getGameProfile().getName(), title, KardExo.BASES_FILE);
			source.sendSuccess(new TextComponent("Added base with id " + id), false);
		}
		catch(IllegalStateException e)
		{
			CommandBase.exception("Base with id " + id + " already exists");
		}
		
		return 1;
	}
	
	private static int remove(CommandSourceStack source, String id) throws CommandSyntaxException
	{
		ensurePermission(source, id, null);
		
		try
		{
			PropertyHelper.remove(id, KardExo.BASES_FILE);
			source.sendSuccess(new TextComponent("Removed base with id " + id), false);
		}
		catch(NoSuchElementException e)
		{
			throw CommandBase.exception("No such base with id " + id);
		}
		
		return 1;
	}
	
	private static int setEnterMessage(CommandSourceStack source, String id, Player player, String message) throws CommandSyntaxException
	{
		ensuredForOwner(source, id, player, owner -> 
		{
			owner.setEnterMessage(message);
			source.sendSuccess(new TextComponent("Message upon entrance has been set to \"" + message + "\""), false);
		});
		
		return 1;
	}
	
	private static int setExitMessage(CommandSourceStack source, String id, Player player, String message) throws CommandSyntaxException
	{
		ensuredForOwner(source, id, player, owner -> 
		{
			owner.setExitMessage(message);
			source.sendSuccess(new TextComponent("Message upon exit has been set to \"" + message + "\""), false);
		});
		
		return 1;
	}
	
	private static int setBothMessages(CommandSourceStack source, String id, Player player, String message) throws CommandSyntaxException
	{
		ensuredForOwner(source, id, player, owner -> 
		{
			owner.setEnterMessage(message);
			owner.setExitMessage(message);
			source.sendSuccess(new TextComponent("Both messages have been set to \"" + message + "\""), false);
		});
		
		return 1;
	}
	
	private static void ensuredForOwner(CommandSourceStack source, String id, Player player, Consumer<PropertyOwner> callback) throws CommandSyntaxException
	{
		ensurePermission(source, id, player);
		ensureOwner(player.getGameProfile().getName(), id);
		
		PropertyHelper.forOwner(id, player, KardExo.BASES, owner ->
		{
			callback.accept(owner);
			KardExo.BASES_FILE.save();
		});
	}
	
	private static int addOwner(CommandSourceStack source, String id, Player player, boolean creator) throws CommandSyntaxException
	{
		ensurePermission(source, id, null);
		String name = player.getGameProfile().getName();
		
		if(PropertyHelper.isOwner(name, id, KardExo.BASES))
		{
			throw CommandBase.exception(name + " already is an owner of base with id " + id);
		}
		
		PropertyOwner owner = new PropertyOwner(player.getGameProfile().getName());
		KardExo.BASES.get(id).addOwner(owner);
		KardExo.BASES_FILE.save();
		
		if(creator)
		{
			source.sendSuccess(new TextComponent("Added " + name +  " as a creator to base with id " + id), false);
		}
		else
		{
			source.sendSuccess(new TextComponent("Added " + name +  " as an owner to base with id " + id), false);
		}
		
		return 1;
	}
	
	private static int removeOwner(CommandSourceStack source, String id, Player player) throws CommandSyntaxException
	{
		ensurePermission(source, id, player);
		ensureOwner(player.getGameProfile().getName(), id);
		Property property = getProperty(id);
		ensureCreatorSize(id, player, property, PropertyHelper.isCreator(player.getGameProfile().getName(), id, KardExo.BASES));
		
		PropertyOwner owner = new PropertyOwner(player.getGameProfile().getName());
		property.removeOwner(owner);
		source.sendSuccess(new TextComponent("Removed " + owner.getName() +  " as an owner of the base with id " + id), false);
		KardExo.BASES_FILE.save();
		
		return 1;
	}
	
	private static int setOwner(CommandSourceStack source, String id, Player player, boolean creator) throws CommandSyntaxException
	{
		ensurePermission(source, id, null);
		ensureOwner(player.getGameProfile().getName(), id);
		Property property = getProperty(id);
		ensureCreatorSize(id, player, property, !creator);
		
		for(PropertyOwner owner : property.getAllOwners())
		{
			if(owner.getName().equals(player.getGameProfile().getName()))
			{
				if(owner.isCreator() && creator)
				{
					source.sendSuccess(new TextComponent(owner.getName() + " is already a creator of base with id " + id), false);
				}
				else if(!owner.isCreator() && creator)
				{
					owner.setCreator(true);
					source.sendSuccess(new TextComponent(owner.getName() + " is now a creator of base with id " + id), false);
					KardExo.BASES_FILE.save();
				}
				else if(owner.isCreator() && !creator)
				{
					owner.setCreator(false);
					source.sendSuccess(new TextComponent(owner.getName() + " is now an owner of base with id " + id), false);
					KardExo.BASES_FILE.save();
				}
				else if(!owner.isCreator() && !creator)
				{
					source.sendSuccess(new TextComponent(owner.getName() + " is already an owner of base with id " + id), false);
				}
				
				return 1;
			}
		}
		
		return 0;
	}
	
	private static int setNotify(CommandSourceStack source, String id, Player player, boolean notify) throws CommandSyntaxException
	{
		ensurePermission(source, id, player);
		ensureOwner(player.getGameProfile().getName(), id);
		Property property = getProperty(id);
		
		for(PropertyOwner owner : property.getAllOwners())
		{
			if(owner.getName().equals(player.getGameProfile().getName()))
			{
				owner.setNotify(notify);
				KardExo.BASES_FILE.save();
				
				if(notify)
				{
					source.sendSuccess(new TextComponent(owner.getName() + " will now be notified"), false);
				}
				else
				{
					source.sendSuccess(new TextComponent(owner.getName() + " will no longer be notified"), false);
				}
				
				return 1;
			}
		}
		
		return 0;
	}
	
	private static int addChild(CommandSourceStack source, String id, String child, ServerLevel dimension, ColumnPos from, ColumnPos to, String title) throws CommandSyntaxException
	{
		ensurePermission(source, id, null);
		Property parent = getProperty(id);
		
		try
		{
			PropertyHelper.addChild(parent, child, dimension, from, to, title, KardExo.BASES_FILE);
			source.sendSuccess(new TextComponent("Added child with id " + child + " to base with id " + id), false);
		}
		catch(IllegalStateException e)
		{
			throw CommandBase.exception("Child with id " + child + " already exists for base with id " + id);
		}
		
		return 1;
	}
	
	private static int removeChild(CommandSourceStack source, String id, String child) throws CommandSyntaxException
	{
		ensurePermission(source, id, null);
		Property parent = getProperty(id);
		
		try
		{
			PropertyHelper.removeChild(parent, child, KardExo.BASES_FILE);
			source.sendSuccess(new TextComponent("Removed child with id " + child + " from base with id " + id), false);
		}
		catch(NoSuchElementException e)
		{
			throw CommandBase.exception("No child with id " + child + " for base with id " + id);
		}
		
		return 1;
	}
	
	private static int setProtection(CommandSourceStack source, String id, boolean enabled) throws CommandSyntaxException
	{
		ensurePermission(source, id, null);
		getProperty(id).setProtected(enabled);
		KardExo.BASES_FILE.save();
		
		if(enabled)
		{
			source.sendSuccess(new TextComponent("Enabled protection for base with id " + id), false);
		}
		else
		{
			source.sendSuccess(new TextComponent("Disabled protection for base with id " + id), false);
		}
		
		return 1;
	}
	
	private static int list(CommandSourceStack source) throws CommandSyntaxException
	{
		try
		{
			return CommandProperty.list(source, KardExo.BASES);
		}
		catch(NoSuchElementException e)
		{
			throw CommandBase.exception("There are no bases");
		}
	}
	
	private static int reload(CommandSourceStack source) throws CommandSyntaxException
	{
		TickableBases.reload();
		
		try
		{
			KardExo.BASES_FILE.save();
			source.sendSuccess(new TextComponent("Successfully reloaded bases"), false);
		}
		catch(Exception e)
		{
			throw CommandBase.exception("Could not reload bases");
		}
		
		return KardExo.BASES.size();
	}
	
	private static void ensurePermission(CommandSourceStack source, String id, Player target) throws CommandSyntaxException
	{
		if(!PropertyHelper.hasPermission(source, id, target, KardExo.BASES))
		{
			throw CommandBase.exception("You must be a creator of base with id " + id);
		}
	}
	
	private static void ensureOwner(String name, String id) throws CommandSyntaxException
	{
		if(!PropertyHelper.isOwner(name, id, KardExo.BASES))
		{
			throw CommandBase.exception(name + " is not an owner of base with id " + id);
		}
	}
	
	private static void ensureCreatorSize(String id, Player player, Property property, boolean condition) throws CommandSyntaxException
	{
		if(condition && property.getCreators().size() == 1)
		{
			throw CommandBase.exception(player.getGameProfile().getName() + " is the only creator of base with id " + id + " and therefor cannot be removed");
		}
	}
	
	private static Property getProperty(String id) throws CommandSyntaxException
	{
		try
		{
			return PropertyHelper.getProperty(id, KardExo.BASES);
		}
		catch(NoSuchElementException e)
		{
			throw CommandBase.exception("No such base with id " + id);
		}
	}
	
	private static CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) throws CommandSyntaxException
	{
		return SharedSuggestionProvider.suggest(KardExo.BASES.keySet(), builder);
	}
	
	private static CompletableFuture<Suggestions> getChildSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) throws CommandSyntaxException
	{
		return CommandProperty.getChildSuggestions(KardExo.BASES_FILE, context, builder, StringArgumentType.getString(context, "id"));
	}
}
