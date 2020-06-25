package net.kardexo.kardexotools.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
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
		ServerWorld overworld = server.getWorld(World.field_234918_g_);
		int phase = overworld.func_230315_m_().func_236035_c_(overworld.getDayTime());
		
		source.sendFeedback(new StringTextComponent(PHASES[phase]), false);
		
		return phase;
	}
}
