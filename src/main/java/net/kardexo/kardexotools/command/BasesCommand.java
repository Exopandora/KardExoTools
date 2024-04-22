package net.kardexo.kardexotools.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.kardexo.kardexotools.KardExo;
import net.kardexo.kardexotools.config.OwnerConfig;
import net.kardexo.kardexotools.property.Property;
import net.kardexo.kardexotools.tasks.BasesTickable;
import net.kardexo.kardexotools.util.CommandUtils;
import net.kardexo.kardexotools.util.PropertyUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.spongepowered.include.com.google.common.base.Objects;

import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class BasesCommand
{
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		dispatcher.register(Commands.literal("bases")
			.requires(source -> KardExo.CONFIG.getData().isBasesCommandEnabled())
			.then(Commands.literal("add")
				.then(Commands.argument("id", StringArgumentType.word())
					.then(Commands.argument("dimension", DimensionArgument.dimension())
						.then(Commands.argument("from", BlockPosArgument.blockPos())
							.then(Commands.argument("to", BlockPosArgument.blockPos())
								.then(Commands.argument("owner", EntityArgument.player())
									.executes(context -> add(context.getSource(), StringArgumentType.getString(context, "id"), DimensionArgument.getDimension(context, "dimension"), BoundingBox.fromCorners(BlockPosArgument.getLoadedBlockPos(context, "from"), BlockPosArgument.getLoadedBlockPos(context, "to")), EntityArgument.getPlayer(context, "owner"), null))
									.then(Commands.argument("displayName", ComponentArgument.textComponent())
										.executes(context -> add(context.getSource(), StringArgumentType.getString(context, "id"), DimensionArgument.getDimension(context, "dimension"), BoundingBox.fromCorners(BlockPosArgument.getLoadedBlockPos(context, "from"), BlockPosArgument.getLoadedBlockPos(context, "to")), EntityArgument.getPlayer(context, "owner"), ComponentArgument.getComponent(context, "displayName"))))))))))
			.then(Commands.literal("remove")
				.then(Commands.argument("id", StringArgumentType.word())
					.suggests(BasesCommand::getSuggestions)
					.executes(context -> remove(context.getSource(), StringArgumentType.getString(context, "id")))))
			.then(Commands.literal("messages")
				.then(Commands.argument("id", StringArgumentType.word())
					.suggests(BasesCommand::getSuggestions)
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
					.suggests(BasesCommand::getSuggestions)
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
			.then(Commands.literal("children")
				.then(Commands.argument("id", StringArgumentType.word())
					.suggests(BasesCommand::getSuggestions)
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
							.suggests(BasesCommand::getChildSuggestions)
							.executes(context -> removeChild(context.getSource(), StringArgumentType.getString(context, "id"), StringArgumentType.getString(context, "child")))))
					.then(Commands.literal("list")
						.executes(context -> listChildren(context.getSource(), StringArgumentType.getString(context, "id"))))))
			.then(Commands.literal("protection")
				.then(Commands.argument("id", StringArgumentType.word())
					.suggests(BasesCommand::getSuggestions)
					.then(Commands.argument("enabled", BoolArgumentType.bool())
						.executes(context -> setProtection(context.getSource(), StringArgumentType.getString(context, "id"), BoolArgumentType.getBool(context, "enabled")))))));
	}
	
	private static int add(CommandSourceStack source, String id, ServerLevel dimension, BoundingBox boundingBox, Player owner, Component displayName) throws CommandSyntaxException
	{
		try
		{
			PropertyUtils.add(id, dimension, boundingBox, owner.getUUID(), displayName, KardExo.BASES);
			source.sendSuccess(() -> Component.literal("Added base with id " + id), false);
		}
		catch(IllegalStateException e)
		{
			throw CommandUtils.simpleException("Base with id " + id + " already exists");
		}
		
		return 1;
	}
	
	private static int remove(CommandSourceStack source, String id) throws CommandSyntaxException
	{
		ensurePermission(source, id, null);
		
		try
		{
			PropertyUtils.remove(id, KardExo.BASES);
			source.sendSuccess(() -> Component.literal("Removed base with id " + id), false);
		}
		catch(NoSuchElementException e)
		{
			throw CommandUtils.simpleException("No such base with id " + id);
		}
		
		return 1;
	}
	
	private static int setEnterMessage(CommandSourceStack source, String id, Player player, String message) throws CommandSyntaxException
	{
		ensuredForOwner(source, id, player, owner -> 
		{
			owner.setEnterMessage(message);
			source.sendSuccess(() -> Component.literal("Message upon entrance has been set to \"" + message + "\""), false);
		});
		
		return 1;
	}
	
	private static int setExitMessage(CommandSourceStack source, String id, Player player, String message) throws CommandSyntaxException
	{
		ensuredForOwner(source, id, player, owner -> 
		{
			owner.setExitMessage(message);
			source.sendSuccess(() -> Component.literal("Message upon exit has been set to \"" + message + "\""), false);
		});
		
		return 1;
	}
	
	private static int setBothMessages(CommandSourceStack source, String id, Player player, String message) throws CommandSyntaxException
	{
		ensuredForOwner(source, id, player, owner -> 
		{
			owner.setEnterMessage(message);
			owner.setExitMessage(message);
			source.sendSuccess(() -> Component.literal("Both messages have been set to \"" + message + "\""), false);
		});
		
		return 1;
	}
	
	private static void ensuredForOwner(CommandSourceStack source, String id, Player player, Consumer<OwnerConfig> callback) throws CommandSyntaxException
	{
		ensurePermission(source, id, player);
		ensureOwner(player, id);
		
		OwnerConfig owner = PropertyUtils.getOwnerConfig(id, player.getUUID(), KardExo.BASES.getData());
		
		if(owner != null)
		{
			callback.accept(owner);
			KardExo.BASES.save();
		}
	}
	
	private static int addOwner(CommandSourceStack source, String id, Player player, boolean creator) throws CommandSyntaxException
	{
		ensurePermission(source, id, null);
		
		if(PropertyUtils.isOwner(player.getUUID(), id, KardExo.BASES.getData()))
		{
			throw CommandUtils.simpleException(((MutableComponent) player.getDisplayName()).append(" already is an owner of base with id " + id));
		}
		
		KardExo.BASES.get(id).putOwner(player.getUUID(), new OwnerConfig(creator, true, null, null));
		KardExo.BASES.save();
		
		if(creator)
		{
			source.sendSuccess(() -> Component.literal("Added ").append(player.getDisplayName()).append(" as a creator to base with id " + id), false);
		}
		else
		{
			source.sendSuccess(() -> Component.literal("Added ").append(player.getDisplayName()).append(" as an owner to base with id " + id), false);
		}
		
		return 1;
	}
	
	private static int removeOwner(CommandSourceStack source, String id, Player player) throws CommandSyntaxException
	{
		ensurePermission(source, id, player);
		ensureOwner(player, id);
		Property property = getBase(id);
		property.removeOwner(player.getUUID());
		source.sendSuccess(() -> Component.literal("Removed ").append(player.getDisplayName()).append(" as an owner of the base with id " + id), false);
		KardExo.BASES.save();
		return 1;
	}
	
	private static int setOwner(CommandSourceStack source, String id, Player player, boolean creator) throws CommandSyntaxException
	{
		ensurePermission(source, id, null);
		ensureOwner(player, id);
		
		for(Entry<UUID, OwnerConfig> owner : getBase(id).getOwners().entrySet())
		{
			if(Objects.equal(owner.getKey(), player.getUUID()))
			{
				OwnerConfig config = owner.getValue();
				
				if(config.isCreator() && creator)
				{
					source.sendSuccess(() -> ((MutableComponent) player.getDisplayName()).append(" is already a creator of base with id " + id), false);
				}
				else if(!config.isCreator() && creator)
				{
					config.setCreator(true);
					source.sendSuccess(() -> ((MutableComponent) player.getDisplayName()).append(" is now a creator of base with id " + id), false);
					KardExo.BASES.save();
				}
				else if(config.isCreator() && !creator)
				{
					config.setCreator(false);
					source.sendSuccess(() -> ((MutableComponent) player.getDisplayName()).append(" is now an owner of base with id " + id), false);
					KardExo.BASES.save();
				}
				else if(!config.isCreator() && !creator)
				{
					source.sendSuccess(() -> ((MutableComponent) player.getDisplayName()).append(" is already an owner of base with id " + id), false);
				}
				
				return 1;
			}
		}
		
		return 0;
	}
	
	private static int setNotify(CommandSourceStack source, String id, Player player, boolean notify) throws CommandSyntaxException
	{
		ensurePermission(source, id, player);
		ensureOwner(player, id);
		Property property = getBase(id);
		
		for(Entry<UUID, OwnerConfig> owner : property.getOwners().entrySet())
		{
			if(Objects.equal(owner.getKey(), player.getUUID()))
			{
				owner.getValue().setNotify(notify);
				KardExo.BASES.save();
				
				if(notify)
				{
					source.sendSuccess(() -> ((MutableComponent) player.getDisplayName()).append(" will now be notified"), false);
				}
				else
				{
					source.sendSuccess(() -> ((MutableComponent) player.getDisplayName()).append(" will no longer be notified"), false);
				}
				
				return 1;
			}
		}
		
		return 0;
	}
	
	private static int addChild(CommandSourceStack source, String id, String child, ServerLevel dimension, BoundingBox boundingBox, Component displayName) throws CommandSyntaxException
	{
		ensurePermission(source, id, null);
		Property parent = getBase(id);
		
		try
		{
			PropertyUtils.addChild(parent, child, dimension, boundingBox, displayName, KardExo.BASES);
			source.sendSuccess(() -> Component.literal("Added child with id " + child + " to base with id " + id), false);
		}
		catch(IllegalStateException e)
		{
			throw CommandUtils.simpleException("Child with id " + child + " already exists for base with id " + id);
		}
		
		return 1;
	}
	
	private static int removeChild(CommandSourceStack source, String id, String child) throws CommandSyntaxException
	{
		ensurePermission(source, id, null);
		Property parent = getBase(id);
		
		try
		{
			PropertyUtils.removeChild(parent, child, KardExo.BASES);
			source.sendSuccess(() -> Component.literal("Removed child with id " + child + " from base with id " + id), false);
		}
		catch(NoSuchElementException e)
		{
			throw CommandUtils.simpleException("No child with id " + child + " for base with id " + id);
		}
		
		return 1;
	}
	
	private static int listChildren(CommandSourceStack source, String id) throws CommandSyntaxException
	{
		try
		{
			return PropertyCommand.list(source, getBase(id).getChildren());
		}
		catch(NoSuchElementException e)
		{
			throw CommandUtils.simpleException("There are no children for base with id " + id);
		}
	}
	
	private static int setProtection(CommandSourceStack source, String id, boolean enabled) throws CommandSyntaxException
	{
		ensurePermission(source, id, null);
		getBase(id).setProtected(enabled);
		KardExo.BASES.save();
		
		if(enabled)
		{
			source.sendSuccess(() -> Component.literal("Enabled protection for base with id " + id), false);
		}
		else
		{
			source.sendSuccess(() -> Component.literal("Disabled protection for base with id " + id), false);
		}
		
		return 1;
	}
	
	private static int list(CommandSourceStack source) throws CommandSyntaxException
	{
		try
		{
			return PropertyCommand.list(source, KardExo.BASES.getData());
		}
		catch(NoSuchElementException e)
		{
			throw CommandUtils.simpleException("There are no bases");
		}
	}
	
	private static int reload(CommandSourceStack source) throws CommandSyntaxException
	{
		BasesTickable.reload();
		
		try
		{
			KardExo.BASES.save();
			source.sendSuccess(() -> Component.literal("Successfully reloaded bases"), false);
		}
		catch(Exception e)
		{
			throw CommandUtils.simpleException("Could not reload bases");
		}
		
		return KardExo.BASES.getData().size();
	}
	
	private static void ensurePermission(CommandSourceStack source, String id, Player target) throws CommandSyntaxException
	{
		if(!PropertyUtils.hasPermission(source, id, target, KardExo.BASES.getData()))
		{
			throw CommandUtils.simpleException("You must be a creator of base with id " + id);
		}
	}
	
	private static void ensureOwner(Player player, String id) throws CommandSyntaxException
	{
		if(!PropertyUtils.isOwner(player.getUUID(), id, KardExo.BASES.getData()))
		{
			throw CommandUtils.simpleException(((MutableComponent) player.getDisplayName()).append(" is not an owner of base with id " + id));
		}
	}
	
	private static Property getBase(String id) throws CommandSyntaxException
	{
		try
		{
			return PropertyUtils.getProperty(id, KardExo.BASES.getData());
		}
		catch(NoSuchElementException e)
		{
			throw CommandUtils.simpleException("No such base with id " + id);
		}
	}
	
	private static CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) throws CommandSyntaxException
	{
		return SharedSuggestionProvider.suggest(KardExo.BASES.getData().keySet(), builder);
	}
	
	private static CompletableFuture<Suggestions> getChildSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) throws CommandSyntaxException
	{
		return PropertyCommand.getChildSuggestions(KardExo.BASES, context, builder, StringArgumentType.getString(context, "id"));
	}
}
