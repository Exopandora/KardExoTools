package exopandora.kardexo.kardexotools;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import javax.annotation.Nullable;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

public class CommandPlaces extends CommandProperty
{
	public CommandPlaces()
	{
		super(Config.PLACES);
	}
	
	@Override
	public String getName()
	{
		return "places";
	}
	
	@Override
	public String getUsage(ICommandSender sender)
	{
		return "/places <add|remove|list|reload|child> ...";
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
		if(args.length > 0)
		{
			if(args[0].equals("add"))
			{
				if(args.length > 6)
				{
					try
					{
						this.add(args, new PropertyOwner(sender.getName(), true, false, null, null), 1, 2, 3, 4, 5, 6, 7);
						sender.sendMessage(new TextComponentString("Added place with id " + args[1]));
					}
					catch(InvalidDimensionException | NumberInvalidException e)
					{
						throw e;
					}
					catch(IllegalStateException e)
					{
						throw new CommandException("Place with id " + args[1] + " already exists");
					}
				}
				else
				{
					throw new WrongUsageException("/places add <name> <dimension> <x1> <z1> <x2> <z2> [title]");
				}
			}
			else if(args[0].equals("remove"))
			{
				if(args.length >= 2)
				{
					try
					{
						this.remove(args[1], sender, server);
						sender.sendMessage(new TextComponentString("Removed place with id " + args[1]));
					}
					catch(PermissionException e)
					{
						throw new CommandException("You have to be a creator of place with id " + args[1], new Object[0]);
					}
					catch(NoSuchElementException e)
					{
						throw new NumberInvalidException("No such place with id " + args[1]);
					}
				}
				else
				{
					throw new WrongUsageException("/places remove <name>");
				}
			}
			else if(args[0].equals("list"))
			{
				try
				{
					this.list(sender);
				}
				catch(NoSuchElementException e)
				{
					throw new CommandException("There are no places", new Object[0]);
				}
			}
			else if(args[0].equals("reload"))
			{
				this.reload(sender, server);
				sender.sendMessage(new TextComponentString("Places have been reloaded"));
			}
			else if(args[0].equals("child"))
			{
				if(args.length > 1)
				{
					this.verifyPlace(args[1]);
				}
				
				if(args.length > 2)
				{
					if(args[2].equals("add"))
					{
						if(args.length > 9)
						{
							if(this.verifyCreator(args[1], sender.getName(), server))
							{
								try
								{
									this.addChild(this.file.getData().get(args[1]), args, 3, 4, 5, 6, 7, 8, 9);
									sender.sendMessage(new TextComponentString("Added child with id " + args[3]));
								}
								catch(InvalidDimensionException | NumberInvalidException e)
								{
									throw e;
								}
								catch(IllegalStateException e)
								{
									throw new CommandException("Child with id " + args[3] + " already exists");
								}
							}
							else
							{
								throw new CommandException("You must be a creator of the place with id " + args[1]);
							}
						}
						else
						{
							throw new WrongUsageException("/places child <name> add <child> <dimension> <x1> <z1> <x2> <z2> <player> [title]");
						}
					}
					else if(args[2].equals("remove"))
					{
						if(args.length > 3)
						{
							if(this.verifyCreator(args[1], sender.getName(), server))
							{
								Property base = this.file.getData().get(args[1]);
								Property child = base.getChild(args[3]);
								
								if(child != null)
								{
									base.removeChild(child);
									sender.sendMessage(new TextComponentString("Removed child with id " + args[3] + " from place with id " + args[1]));
								}
								else
								{
									throw new CommandException("No child with id " + args[3]);
								}
							}
							else
							{
								throw new CommandException("You must be a creator of the place with id " + args[1]);
							}
						}
						else
						{
							throw new WrongUsageException("/places child <name> remove <child>");
						}
					}
					else
					{
						throw new WrongUsageException("/places child <name> <add|remove> ...");
					}
				}
				else
				{
					throw new WrongUsageException("/places child <name> <add|remove> ...");
				}
			}
		}
		else
		{
			throw new WrongUsageException(this.getUsage(sender));
		}
	}
	
	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
	{
		if(args.length == 1)
		{
			return this.getListOfStringsMatchingLastWord(args, new String[]{"add", "remove", "list", "child"});
		}
		else if(args.length > 1)
		{
			if(args[0].equals("add"))
			{
				if(args.length == 4 || args.length == 6)
				{
					return this.getListOfStringsMatchingLastWord(args, String.valueOf((int) sender.getCommandSenderEntity().posX));
				}
				else if(args.length == 5 || args.length == 7)
				{
					return this.getListOfStringsMatchingLastWord(args, String.valueOf((int) sender.getCommandSenderEntity().posZ));
				}
				else if(args.length == 3)
				{
					return this.getListOfStringsMatchingLastWord(args, "overworld", "nether", "end");
				}
				else if(args.length == 8)
				{
					return this.getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
				}
			}
			else if(args[0].equals("remove"))
			{
				if(args.length == 2)
				{
					return this.getListOfStringsMatchingLastWord(args, this.file.getData().keySet());
				}
			}
			else if(args[0].equals("child"))
			{
				if(args.length == 2)
				{
					return this.getListOfStringsMatchingLastWord(args, this.file.getData().keySet());
				}
				else if(args.length == 3)
				{
					return this.getListOfStringsMatchingLastWord(args, new String[] {"add", "remove"});
				}
				else if(args.length == 4 && args[2].equals("remove"))
				{
					Property property = this.file.getData().get(args[1]);
					
					if(property != null)
					{
						return this.getListOfStringsMatchingLastWord(args, property.getChildrenNames());
					}
				}
				else if(args.length > 3 && args[2].equals("add"))
				{
					if(args.length == 6 || args.length == 8)
					{
						return this.getListOfStringsMatchingLastWord(args, String.valueOf((int) sender.getCommandSenderEntity().posX));
					}
					else if(args.length == 7 || args.length == 9)
					{
						return this.getListOfStringsMatchingLastWord(args, String.valueOf((int) sender.getCommandSenderEntity().posZ));
					}
					else if(args.length == 5)
					{
						return this.getListOfStringsMatchingLastWord(args, "overworld", "nether", "end");
					}
				}
			}
		}
		
		return Collections.<String>emptyList();
	}
	
	private boolean verifyPlace(String place) throws CommandException
	{
		if(this.file.getData().containsKey(place))
		{
			return true;
		}
		
		throw new CommandException("No such place with id " + place);
	}
}