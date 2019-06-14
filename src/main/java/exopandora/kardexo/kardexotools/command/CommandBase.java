package exopandora.kardexo.kardexotools.command;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.util.text.TranslationTextComponent;

public class CommandBase
{
	public static CommandSyntaxException createException(String message)
	{
		return new SimpleCommandExceptionType(new TranslationTextComponent(message)).create();
	}
}
