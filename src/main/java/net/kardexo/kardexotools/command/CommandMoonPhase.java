package net.kardexo.kardexotools.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

public class CommandMoonPhase
{
	private static final String[] PHASES = new String[]
	{
		"Full Moon",
		"Waning Gibbous",
		"Last Quarter",
		"Waning Crescent",
		"New Moon",
		"Waxing Crescent",
		"First Quarter",
		"Waxing Gibbous"
	};
	
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(Commands.literal("moonphase")
				.executes(context -> moonPhase(context.getSource())));
	}
	
	private static int moonPhase(CommandSource source) throws CommandSyntaxException
	{
		int phase = source.getServer().getWorld(World.OVERWORLD).getMoonPhase();
		source.sendFeedback(new StringTextComponent(PHASES[phase]), false);
		return phase;
	}
}
