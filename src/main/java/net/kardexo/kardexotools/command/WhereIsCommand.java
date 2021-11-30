package net.kardexo.kardexotools.command;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.kardexo.kardexotools.KardExo;
import net.kardexo.kardexotools.property.Property;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.world.entity.player.Player;

public class WhereIsCommand
{
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		dispatcher.register(Commands.literal("whereis")
				.then(Commands.argument("target", EntityArgument.player())
					.executes(context -> whereIs(context.getSource(), EntityArgument.getPlayer(context, "target")))));
	}
	
	private static int whereIs(CommandSourceStack source, Player target) throws CommandSyntaxException
	{
		BlockPos pos = target.blockPosition();
		String dimension = target.level.dimension().location().toString();
		GameProfileCache profileCache = source.getServer().getProfileCache();
		Set<PropertyEntry> properties = new HashSet<PropertyEntry>();
		
		for(Entry<String, Property> entry : KardExo.BASES.entrySet())
		{
			if(entry.getValue().isInside(target))
			{
				properties.add(new PropertyEntry(entry));
			}
		}
		
		for(Entry<String, Property> entry : KardExo.PLACES.entrySet())
		{
			if(entry.getValue().isInside(target))
			{
				properties.add(new PropertyEntry(entry));
			}
		}
		
		MutableComponent formattedProperties = (MutableComponent) ComponentUtils.formatList(properties, entry -> entry.getDisplayName(profileCache));
		MutableComponent query = new TranslatableComponent("%s: x: %s y: %s z: %s d: %s", new Object[] {target.getDisplayName(), pos.getX(), pos.getY(), pos.getZ(), dimension});
		
		if(!formattedProperties.equals(TextComponent.EMPTY))
		{
			query.append(" (").append(formattedProperties).append(")");
		}
		
		source.getServer().sendMessage(new TextComponent("Query: ").append(query), Util.NIL_UUID);
		source.sendSuccess(query, false);
		return 1;
	}
	
	private static final record PropertyEntry(String id, Property property)
	{
		public PropertyEntry(Entry<String, Property> entry)
		{
			this(entry.getKey(), entry.getValue());
		}
		
		public Component getDisplayName(GameProfileCache profileCache)
		{
			return this.property.getDisplayName(this.id, profileCache);
		}
	}
}
