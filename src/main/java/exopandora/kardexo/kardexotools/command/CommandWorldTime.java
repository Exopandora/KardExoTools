package exopandora.kardexo.kardexotools.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;

public class CommandWorldTime
{
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(Commands.literal("worldtime")
				.executes(context -> worldtime(context.getSource())));
	}
	
	private static int worldtime(CommandSource source) throws CommandSyntaxException
	{
		source.sendFeedback(new StringTextComponent("World time: " + getWorldTime(source.getWorld().getDayTime())), false);
		return 1;
	}
	
	private static int getHour(long tick)
	{
		return MathHelper.floor((tick + 6000) / 1000F) % 24;
	}
	
	private static int getMinute(long tick)
	{
		int hour = MathHelper.floor((tick + 6000F) / 1000F);
		int minute = MathHelper.floor((tick + 6000F - hour * 1000) * 6 / 100);
		
		return minute;
	}
	
	private static String getWorldTime(long tick)
	{
		return String.format("%02d:%02d", getHour(tick), getMinute(tick));
	}
}