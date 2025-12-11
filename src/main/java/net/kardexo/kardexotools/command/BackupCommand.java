package net.kardexo.kardexotools.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.kardexo.kardexotools.KardExo;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class BackupCommand
{
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		dispatcher.register(Commands.literal("backup")
			.requires(source -> KardExo.CONFIG.getData().isBackupCommandEnabled() && Commands.LEVEL_GAMEMASTERS.check(source.permissions()))
				.executes(context -> backup(context.getSource())));
	}
	
	private static int backup(CommandSourceStack source) throws CommandSyntaxException
	{
		KardExo.TASK_BACKUP.execute(source.getServer(), true);
		return 1;
	}
}
