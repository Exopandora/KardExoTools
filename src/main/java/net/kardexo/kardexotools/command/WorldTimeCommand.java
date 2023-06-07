package net.kardexo.kardexotools.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.kardexo.kardexotools.KardExo;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class WorldTimeCommand
{
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		dispatcher.register(Commands.literal("worldtime")
			.requires(source -> KardExo.CONFIG.getData().isWorldtimeCommandEnabled())
			.executes(context -> worldtime(context.getSource())));
	}
	
	private static int worldtime(CommandSourceStack source) throws CommandSyntaxException
	{
		source.sendSuccess(() -> Component.literal("World time: " + toWorldTime(source.getLevel().getDayTime())), false);
		return 1;
	}
	
	private static int toHour(long tick)
	{
		return Mth.floor((tick + 6000) / 1000F) % 24;
	}
	
	private static int toMinute(long tick)
	{
		int hour = Mth.floor((tick + 6000F) / 1000F);
		int minute = Mth.floor((tick + 6000F - hour * 1000) * 6 / 100);
		
		return minute;
	}
	
	private static String toWorldTime(long tick)
	{
		return String.format("%02d:%02d", toHour(tick), toMinute(tick));
	}
}
