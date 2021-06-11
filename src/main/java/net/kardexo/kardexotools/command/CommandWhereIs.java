package net.kardexo.kardexotools.command;

import java.util.HashSet;
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
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;

public class CommandWhereIs
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
		
		Set<Property> properties = new HashSet<Property>();
		
		for(Property base : KardExo.BASES.values())
		{
			if(base.isInside(target))
			{
				properties.add(base);
			}
		}
		
		for(Property place : KardExo.PLACES.values())
		{
			if(place.isInside(target))
			{
				properties.add(place);
			}
		}
		
		MutableComponent formattedProperties = null;
		
		for(Property place : properties)
		{
			if(formattedProperties == null)
			{
				formattedProperties = place.getDisplayName();
			}
			else
			{
				formattedProperties = new TranslatableComponent("%s, %s", place.getDisplayName());
			}
		}
		
		MutableComponent query = new TranslatableComponent("%s: x: %s y: %s z: %s d: %s", new Object[] {target.getDisplayName(), pos.getX(), pos.getY(), pos.getZ(), dimension});
		
		if(formattedProperties != null)
		{
			query = new TranslatableComponent("%s (%s)", new Object[] {query, formattedProperties});
		}
		
		source.getServer().sendMessage(new TextComponent("Query: ").append(query), Util.NIL_UUID);
		source.sendSuccess(query, false);
		
		return 1;
	}
}
