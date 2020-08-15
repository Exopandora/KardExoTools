package net.kardexo.kardexotools.command;

import java.util.HashSet;
import java.util.Set;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.kardexo.kardexotools.config.Config;
import net.kardexo.kardexotools.property.Property;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class CommandWhereIs
{
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(Commands.literal("whereis")
				.then(Commands.argument("target", EntityArgument.player())
					.executes(context -> whereIs(context.getSource(), EntityArgument.getPlayer(context, "target")))));
	}
	
	private static int whereIs(CommandSource source, PlayerEntity target) throws CommandSyntaxException
	{
		BlockPos pos = target.getPosition();
		String dimension = target.world.func_234923_W_().func_240901_a_().toString();
		
		Set<Property> properties = new HashSet<Property>();
		
		for(Property base : Config.BASES.getData().values())
		{
			if(base.isInside(target))
			{
				properties.add(base);
			}
		}
		
		for(Property place : Config.PLACES.getData().values())
		{
			if(place.isInside(target))
			{
				properties.add(place);
			}
		}
		
		IFormattableTextComponent formattedProperties = null;
		
		for(Property place : properties)
		{
			if(formattedProperties == null)
			{
				formattedProperties = place.getDisplayName();
			}
			else
			{
				formattedProperties = new TranslationTextComponent("%s, %s", place.getDisplayName());
			}
		}
		
		IFormattableTextComponent query = new TranslationTextComponent("%s: x: %s y: %s z: %s d: %s", new Object[] {target.getDisplayName(), pos.getX(), pos.getY(), pos.getZ(), dimension});
		
		if(formattedProperties != null)
		{
			query = new TranslationTextComponent("%s (%s)", new Object[] {query, formattedProperties});
		}
		
		source.getServer().sendMessage(new StringTextComponent("Query: ").append(query), Util.DUMMY_UUID);
		source.sendFeedback(query, false);
		
		return 1;
	}
}
