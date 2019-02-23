package exopandora.kardexo.kardexotools.command;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.util.text.TextComponentTranslation;

public class CommandBase
{
	public static CommandSyntaxException createException(String message)
	{
		return new SimpleCommandExceptionType(new TextComponentTranslation(message)).create();
	}
}
