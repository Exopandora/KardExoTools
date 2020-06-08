package net.kardexo.kardexotools.command;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.kardexo.kardexotools.config.Config;
import net.kardexo.kardexotools.property.Property;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
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
		String dimension = Objects.toString(Registry.DIMENSION_TYPE.getKey(target.dimension));
		
		Set<Property> bases = new HashSet<Property>();
		
		for(Property base : Config.BASES.getData().values())
		{
			if(base.isInside(target))
			{
				bases.add(base);
			}
		}
		
		for(Property place : Config.PLACES.getData().values())
		{
			if(place.isInside(target))
			{
				bases.add(place);
			}
		}
		
		ITextComponent textComponent = null;
		
		for(Property place : bases)
		{
			if(textComponent == null)
			{
				textComponent = place.getDisplayName();
			}
			else
			{
				textComponent = new TranslationTextComponent("%s, %s", textComponent, place.getDisplayName());
			}
		}
		
		String result = "%s: d: " + dimension + " x: " + pos.getX() + " y: " + pos.getY() + " z: " + pos.getZ() + (textComponent != null ? " (%s)" : "");
		source.getServer().logInfo("Query: " + String.format(result, target.getName().getString(), String.join(", ", bases.parallelStream().map(Property::getName).collect(Collectors.toList()))));
		source.sendFeedback(new TranslationTextComponent(result, target.getDisplayName(), textComponent), false);
		
		return 1;
	}
}
