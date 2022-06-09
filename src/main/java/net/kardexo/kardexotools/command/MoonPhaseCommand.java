package net.kardexo.kardexotools.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

public class MoonPhaseCommand
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
	
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		dispatcher.register(Commands.literal("moonphase")
				.executes(context -> moonPhase(context.getSource())));
	}
	
	private static int moonPhase(CommandSourceStack source) throws CommandSyntaxException
	{
		int phase = source.getServer().getLevel(Level.OVERWORLD).getMoonPhase();
		source.sendSuccess(Component.literal(PHASES[phase]), false);
		return phase;
	}
}
