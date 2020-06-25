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
import net.minecraft.util.text.ITextComponent;
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
		BlockPos pos = target.func_233580_cy_();
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
		
		ITextComponent formattedProperties = null;
		
		for(Property place : properties)
		{
			if(formattedProperties == null)
			{
				formattedProperties = place.getDisplayName();
			}
			else
			{
				formattedProperties = new TranslationTextComponent("%s, %s", formattedProperties, place.getDisplayName());
			}
		}
		
		StringTextComponent result = new StringTextComponent(target.getGameProfile().getName() + ": d: " + dimension + " x: " + pos.getX() + " y: " + pos.getY() + " z: " + pos.getZ());
		
		if(formattedProperties != null)
		{
			result.func_240702_b_(" (").func_230529_a_(formattedProperties).func_240702_b_(")");
		}
		
		source.getServer().sendMessage(new StringTextComponent("Query: ").func_230529_a_(result), Util.field_240973_b_);
		source.sendFeedback(result, false);
		
		return 1;
	}
}
