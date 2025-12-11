package net.kardexo.kardexotools.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.kardexo.kardexotools.KardExo;
import net.kardexo.kardexotools.property.Property;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.players.UserNameToIdResolver;
import net.minecraft.world.entity.player.Player;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

public class WhereIsCommand
{
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		dispatcher.register(Commands.literal("whereis")
			.requires(source -> KardExo.CONFIG.getData().isWhereisCommandEnabled())
			.then(Commands.argument("target", EntityArgument.player())
				.executes(context -> whereIs(context.getSource(), EntityArgument.getPlayer(context, "target")))));
	}
	
	private static int whereIs(CommandSourceStack source, Player target) throws CommandSyntaxException
	{
		BlockPos pos = target.blockPosition();
		String dimension = target.level().dimension().identifier().toString();
		UserNameToIdResolver userNameToIdResolver = source.getServer().services().nameToIdCache();
		Set<PropertyEntry> properties = new HashSet<PropertyEntry>();
		
		for(Entry<String, Property> entry : KardExo.BASES.getData().entrySet())
		{
			if(entry.getValue().isInside(target))
			{
				properties.add(new PropertyEntry(entry));
			}
		}
		
		for(Entry<String, Property> entry : KardExo.PLACES.getData().entrySet())
		{
			if(entry.getValue().isInside(target))
			{
				properties.add(new PropertyEntry(entry));
			}
		}
		
		MutableComponent formattedProperties = (MutableComponent) ComponentUtils.formatList(properties, entry -> entry.getDisplayName(userNameToIdResolver));
		MutableComponent query = Component.translatable("%s: x: %s y: %s z: %s d: %s", target.getDisplayName(), pos.getX(), pos.getY(), pos.getZ(), dimension);
		
		if(!properties.isEmpty())
		{
			query.append(" (").append(formattedProperties).append(")");
		}
		
		source.getServer().sendSystemMessage(Component.literal("Query: ").append(query));
		source.sendSuccess(() -> query, false);
		return 1;
	}
	
	private final record PropertyEntry(String id, Property property)
	{
		public PropertyEntry(Entry<String, Property> entry)
		{
			this(entry.getKey(), entry.getValue());
		}
		
		public Component getDisplayName(UserNameToIdResolver userNameToIdResolver)
		{
			return this.property.getDisplayName(this.id, userNameToIdResolver);
		}
	}
}
