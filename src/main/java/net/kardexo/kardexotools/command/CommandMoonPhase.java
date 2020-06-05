package net.kardexo.kardexotools.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;

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
		MinecraftServer server = source.getServer();
		ServerWorld overworld = server.getWorld(DimensionType.OVERWORLD);
		int phase = overworld.getDimension().getMoonPhase(overworld.getDayTime());
		
		source.sendFeedback(new StringTextComponent(PHASES[phase]), false);
		
		return phase;
	}
}
