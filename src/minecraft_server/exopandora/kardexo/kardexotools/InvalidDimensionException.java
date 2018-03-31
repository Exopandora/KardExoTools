package exopandora.kardexo.kardexotools;

import net.minecraft.command.CommandException;

public class InvalidDimensionException extends CommandException
{
	public InvalidDimensionException(String message, Object[] objects)
	{
		super(message, objects);
	}
}
