package net.kardexo.kardexotools.command;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.kardexo.kardexotools.KardExo;
import net.kardexo.kardexotools.util.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class UptimeCommand
{
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		dispatcher.register(Commands.literal("uptime")
			.requires(source -> KardExo.CONFIG.getData().isUptimeCommandEnabled())
			.executes(context -> uptime(context.getSource())));
	}
	
	private static int uptime(CommandSourceStack source) throws CommandSyntaxException
	{
		RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
		Duration duration = Duration.ofMillis(bean.getUptime());
		String fomattedDuration = Util.format(duration);
		Instant start = Instant.ofEpochMilli(bean.getStartTime());
		String formattedStart = DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss")
			.withZone(ZoneId.systemDefault())
			.format(start);
		source.sendSuccess(() -> Component.literal("Uptime: " + fomattedDuration + ", Start time: " + formattedStart), false);
		return (int) duration.toSeconds();
	}
}
