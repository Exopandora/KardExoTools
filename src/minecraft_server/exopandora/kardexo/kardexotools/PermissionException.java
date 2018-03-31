package exopandora.kardexo.kardexotools;

import net.minecraft.command.CommandException;

public class PermissionException extends CommandException
{
	public PermissionException()
	{
		super("commands.generic.permission", new Object[0]);
	}
	
	public PermissionException(String message, Object... objects)
	{
		super(message, objects);
	}
}
