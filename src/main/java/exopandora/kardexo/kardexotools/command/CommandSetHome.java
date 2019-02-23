package exopandora.kardexo.kardexotools.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import exopandora.kardexo.kardexotools.base.Home;
import exopandora.kardexo.kardexotools.data.Config;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

public class CommandSetHome
{
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(Commands.literal("sethome")
				.executes(context -> setHome(context.getSource())));
	}
	
	private static int setHome(CommandSource source) throws CommandSyntaxException
	{
		EntityPlayerMP sender = source.asPlayer();
		BlockPos pos = sender.getPosition();
		
		Config.HOME.getData().put(source.getName(), new Home(pos, source.getName(), sender.dimension.getId()));
		Config.HOME.save();
		
		source.sendFeedback(new TextComponentString("Home set to " + pos.getX() + " " + pos.getY() + " " + pos.getZ()), false);
		return 1;
	}
}
