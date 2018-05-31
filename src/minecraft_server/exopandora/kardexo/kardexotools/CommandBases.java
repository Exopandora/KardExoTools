package exopandora.kardexo.kardexotools;

import java.util.Arrays;
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

public class CommandBases extends CommandProperty
{
	public CommandBases()
	{
		super(Config.BASES);
	}
	
	@Override
	public String getName()
	{
		return "bases";
	}
	
	@Override
	public String getUsage(ICommandSender sender)
	{
		return "/bases <add|remove|messages|owner|list|reload> ...";
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
		if(args.length > 0)
		{
			if(args[0].equals("list"))
			{
				try
				{
					this.list(sender);
				}
				catch(NoSuchElementException e)
				{
					throw new CommandException("There are no bases", new Object[0]);
				}
			}
			else if(args[0].equals("add"))
			{
				if(args.length > 7)
				{
					try
					{
						this.add(args, new PropertyOwner(args[7], true, true, null, null), 1, 2, 3, 4, 5, 6, 8);
						sender.sendMessage(new TextComponentString("Added base with id " + args[1]));
					}
					catch(InvalidDimensionException | NumberInvalidException e)
					{
						throw e;
					}
					catch(IllegalStateException e)
					{
						throw new CommandException("Base with id " + args[1] + " already exists");
					}
				}
				else
				{
					throw new WrongUsageException("/bases add <name> <dimension> <x1> <z1> <x2> <z2> <player> [title]");
				}
			}
			else if(args[0].equals("remove"))
			{
				if(args.length >= 2)
				{
					try
					{
						this.remove(args[1], sender, server);
						sender.sendMessage(new TextComponentString("Removed base with id " + args[1]));
					}
					catch(PermissionException e)
					{
						throw new CommandException("You have to be a creator of base with id " + args[1], new Object[0]);
					}
					catch(NoSuchElementException e)
					{
						throw new NumberInvalidException("No such base with id " + args[1]);
					}
				}
				else
				{
					throw new WrongUsageException("/bases remove <name>");
				}
			}
			else if(args[0].equals("messages"))
			{
				if(args.length > 1)
				{
					this.verifyBase(args[1]);
				}
				
				if(args.length > 2)
				{
					if(args[2].equals("set"))
					{
						if(args.length > 3)
						{
							if(this.verifyOwner(args[1], args[3], server))
							{
								if(!this.verifyCreator(args[1], sender.getName(), server))
								{
									if(!args[3].equals(sender.getName()))
									{
										throw new CommandException("You must be a creator of the base with id " + args[1] + " to do this");
									}
								}
								
								if(args.length > 5)
								{
									String message = String.join(" ", Arrays.copyOfRange(args, 5, args.length));
									
									if(args[4].equals("enter"))
									{
										this.forOwnerOfBase(args[1], args[3], owner -> 
										{
											owner.setEnterMessage(message);
										});
										
										sender.sendMessage(new TextComponentString("Message upon entrance has been set to \"" + message + "\""));
										
										this.file.save();
									}
									else if(args[4].equals("exit"))
									{
										this.forOwnerOfBase(args[1], args[3], owner -> 
										{
											owner.setExitMessage(message);
										});
										
										sender.sendMessage(new TextComponentString("Message upon exit has been set to \"" + message + "\""));
										
										this.file.save();
									}
									else if(args[4].equals("both"))
									{
										this.forOwnerOfBase(args[1], args[3], owner -> 
										{
											owner.setEnterMessage(message);
											owner.setExitMessage(message);
										});
										
										sender.sendMessage(new TextComponentString("Both messages have been set to \"" + message + "\""));
										
										this.file.save();
									}
									else
									{
										throw new CommandException("/bases messages <name> set <player> <enter|exit|both> <message>");
									}
								}
								else
								{
									throw new CommandException("/bases messages <name> set <player> <enter|exit|both> <message>");
								}
							}
						}
						else
						{
							throw new CommandException("/bases messages <name> set <player> <enter|exit|both> <message>");
						}
					}
					else if(args[2].equals("reset"))
					{
						if(args.length > 4)
						{
							if(this.verifyOwner(args[1], args[3], server))
							{
								if(args[4].equals("enter"))
								{
									this.forOwnerOfBase(args[1], args[3], owner -> 
									{
										owner.setEnterMessage(null);
									});
									
									sender.sendMessage(new TextComponentString("Message upon entrance has been reset"));
									
									this.file.save();
								}
								else if(args[4].equals("exit"))
								{
									this.forOwnerOfBase(args[1], args[3], owner -> 
									{
										owner.setExitMessage(null);
									});
									
									sender.sendMessage(new TextComponentString("Message upon exit has been reset"));
									
									this.file.save();
								}
								else if(args[4].equals("both"))
								{
									this.forOwnerOfBase(args[1], args[3], owner -> 
									{
										owner.setEnterMessage(null);
										owner.setExitMessage(null);
									});
									
									sender.sendMessage(new TextComponentString("Both messages have been reset"));
									
									this.file.save();
								}
								else
								{
									throw new CommandException("/bases messages <name> reset <player> <enter|exit|both>");
								}
							}
						}
						else
						{
							throw new CommandException("/bases messages <name> reset <player> <enter|exit|both>");
						}
					}
					else if(args[2].equals("notify"))
					{
						if(this.verifyOwner(args[1], args[3], server) && (this.verifyOwner(args[1], sender.getName(), server) || this.checkPermission(args[1], sender.getName(), server)))
						{
							for(PropertyOwner owner : this.file.getData().get(args[1]).getAllOwners())
							{
								if(owner.getName().equals(args[3]))
								{
									boolean notify = super.parseBoolean(args[4]);
									
									owner.setNotify(notify);
									
									if(notify)
									{
										sender.sendMessage(new TextComponentString(owner.getName() + " will now be notified"));
									}
									else
									{
										sender.sendMessage(new TextComponentString(owner.getName() + " will no longer be notified"));
									}
									
									this.file.save();
									
									break;
								}
							}
						}
					}
					else
					{
						throw new WrongUsageException("/bases messages <name> <set|reset|notify> ...");
					}
				}
				else
				{
					throw new WrongUsageException("/bases messages <name> <set|reset|notify> ...");
				}
			}
			else if(args[0].equals("owner"))
			{
				if(args.length > 1)
				{
					this.verifyBase(args[1]);
				}
				
				if(args.length > 2)
				{
					if(args[2].equals("add"))
					{
						if(args.length > 3)
						{
							boolean flag = false;
							
							if(args.length > 4)
							{
								if(args[4].equals("owner"))
								{
									flag = false;
								}
								else if(args[4].equals("creator"))
								{
									flag = true;
								}
								else
								{
									throw new WrongUsageException("/bases owner <name> add <player> [owner|creator]");
								}
							}
							
							if(this.checkPermission(args[1], sender.getName(), server))
							{
								if(!this.verifyOwnerSilently(args[1], args[3], server))
								{
									this.file.getData().get(args[1]).getAllOwners().add(new PropertyOwner(args[3], flag, true, null, null));
									
									if(flag)
									{
										sender.sendMessage(new TextComponentString("Added " + args[3] +  " as a creator to base with id " + args[1]));
									}
									else
									{
										sender.sendMessage(new TextComponentString("Added " + args[3] +  " as an owner to base with id " + args[1]));
									}
									
									this.file.save();
								}
								else
								{
									sender.sendMessage(new TextComponentString(args[3] + " already is an owner of the base with id " + args[1]));
								}
							}
						}
						else
						{
							throw new WrongUsageException("/bases owner <name> add <player> [owner|creator]");
						}
					}
					else if(args[2].equals("remove"))
					{
						if(args.length > 3)
						{
							if(this.checkPermission(args[1], sender.getName(), server))
							{
								if(!(this.verifyCreator(args[1], args[3], server) && this.file.getData().get(args[1]).getCreators().size() == 0))
								{
									PropertyOwner owner = new PropertyOwner(args[3]);
									Property base = this.file.getData().get(args[1]);
									
									if(base.getAllOwners().contains(owner))
									{
										base.getAllOwners().remove(owner);
										sender.sendMessage(new TextComponentString("Removed " + owner.getName() +  " as an owner of the base with id " + base.getName()));
										
										this.file.save();
									}
									else
									{
										sender.sendMessage(new TextComponentString(owner.getName() + " already does not own base with id " + base.getName()));
									}
								}
								else
								{
									sender.sendMessage(new TextComponentString(args[3] + " is the only creator of base with id " + args[1] + " and therefor cannot be removed"));
								}
							}
						}
						else
						{
							throw new WrongUsageException("/bases owner <name> remove <player>");
						}
					}
					else if(args[2].equals("set"))
					{
						if(args.length > 3)
						{
							if(this.checkPermission(args[1], sender.getName(), server))
							{
								if(this.verifyOwner(args[1], args[3], server))
								{
									if(args.length > 4)
									{
										boolean flag = false;
										
										if(args[4].equals("owner"))
										{
											flag = false;
										}
										else if(args[4].equals("creator"))
										{
											flag = true;
										}
										else
										{
											throw new WrongUsageException("/bases owner <name> set <player> <owner|creator>");
										}
										
										if(!flag && this.file.getData().get(args[1]).getCreators().size() == 1)
										{
											sender.sendMessage(new TextComponentString(args[3] + " is the only creator of base with id " + args[1] + " and therefor cannot be removed"));
										}
										else
										{
											for(PropertyOwner owner : this.file.getData().get(args[1]).getAllOwners())
											{
												if(owner.getName().equals(args[3]))
												{
													if(owner.isCreator() && flag)
													{
														sender.sendMessage(new TextComponentString(owner.getName() + " is already a creator of base with id " + args[1]));
													}
													else if(!owner.isCreator() && flag)
													{
														owner.setCreator(true);
														sender.sendMessage(new TextComponentString(owner.getName() + " is now a creator of base with id " + args[1]));
														
														this.file.save();
													}
													else if(owner.isCreator() && !flag)
													{
														owner.setCreator(false);
														sender.sendMessage(new TextComponentString(owner.getName() + " is now an owner of base with id " + args[1]));
														
														this.file.save();
													}
													else if(!owner.isCreator() && !flag)
													{
														sender.sendMessage(new TextComponentString(owner.getName() + " is already an owner of base with id " + args[1]));
													}
													
													break;
												}
											}
										}
									}
									else
									{
										throw new WrongUsageException("/bases owner <name> set <player> <owner|creator>");
									}
								}
								else
								{
									sender.sendMessage(new TextComponentString(args[3] + " must be an owner of base with id " + args[1]));
								}
							}
						}
						else
						{
							throw new WrongUsageException("/bases owner <name> set <player> <owner|creator>");
						}
					}
					else
					{
						throw new WrongUsageException("/bases owner <name> <add|remove|set> ...");
					}
				}
				else
				{
					throw new WrongUsageException("/bases owner <name> <add|remove|set> ...");
				}
			}
			else if(args[0].equals("reload"))
			{
				this.reload(sender, server);
				sender.sendMessage(new TextComponentString("Bases have been reloaded"));
			}
			else
			{
				throw new WrongUsageException(this.getUsage(sender));
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
			return this.getListOfStringsMatchingLastWord(args, new String[]{"add", "remove", "messages", "owner", "list", "reload"});
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
			else if(args[0].equals("messages"))
			{
				if(args.length == 2)
				{
					return this.getListOfStringsMatchingLastWord(args, this.file.getData().keySet());
				}
				else if(args.length == 3)
				{
					return this.getListOfStringsMatchingLastWord(args, new String[]{"set", "reset", "notify"});
				}
				else if(args.length == 4)
				{
					return this.getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
				}
				else if(args.length == 5)
				{
					if(args[2].equals("set") || args[2].equals("reset"))
					{
						return this.getListOfStringsMatchingLastWord(args, new String[]{"enter", "exit", "both"});
					}
					else if(args[2].equals("notify"))
					{
						return this.getListOfStringsMatchingLastWord(args, new String[]{"true", "false"});
					}
				}
			}
			else if(args[0].equals("owner"))
			{
				if(args.length == 2)
				{
					return this.getListOfStringsMatchingLastWord(args, this.file.getData().keySet());
				}
				else if(args.length == 3)
				{
					return this.getListOfStringsMatchingLastWord(args, new String[]{"add", "remove"});
				}
				else if(args.length == 4)
				{
					return this.getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
				}
				else if(args.length == 5)
				{
					if(args[2].equals("add") || args[2].equals("set"))
					{
						return this.getListOfStringsMatchingLastWord(args, new String[]{"owner", "creator"});
					}
				}
			}
		}
		
		return Collections.<String>emptyList();
    }
	
	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender)
    {
		return true;
    }
	
	private  boolean verifyBase(String base) throws CommandException
	{
		if(this.file.getData().containsKey(base))
		{
			return true;
		}
		
		throw new CommandException("No such base with id " + base);
	}
}