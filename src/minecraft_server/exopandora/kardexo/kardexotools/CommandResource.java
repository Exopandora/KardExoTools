package exopandora.kardexo.kardexotools;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class CommandResource extends CommandBase
{

	@Override
	public String getCommandName()
	{
		return "resource";
	}

	@Override
	public String getCommandUsage(ICommandSender sender)
	{
		return "resource <x1> <y1> <z1> <x2> <y2> <z2>";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
		if(args.length == 6)
		{
			try
			{
				Map<String, Integer> map = new HashMap<String, Integer>();
				
				int x1 = Integer.parseInt(args[0]);
				int y1 = Integer.parseInt(args[1]);
				int z1 = Integer.parseInt(args[2]);
				int x2 = Integer.parseInt(args[3]);
				int y2 = Integer.parseInt(args[4]);
				int z2 = Integer.parseInt(args[5]);
				
				int xMin = Math.min(x1, x2);
				int yMin = Math.min(y1, y2);
				int zMin = Math.min(z1, z2);
				
				int xMax = Math.max(x1, x2);
				int yMax = Math.max(y1, y2);
				int zMax = Math.max(z1, z2);
				
				World world;
				
				if(sender.getCommandSenderEntity() != null)
				{
					world = server.worldServerForDimension(sender.getCommandSenderEntity().dimension);
				}
				else
				{
					world = server.worldServerForDimension(0);
				}
				
				for(int x = xMin; x <= xMax; x++)
				{
					for(int y = yMin; y <= yMax; y++)
					{
						for(int z = zMin; z <= zMax; z++)
						{
							String location = world.getBlockState(new BlockPos(x, y, z)).getBlock().getLocalizedName();
							
							if(!location.equals(Blocks.AIR.getLocalizedName()))
							{
								if(!map.containsKey(location))
								{
									map.put(location, 1);
								}
								else
								{
									map.put(location, map.get(location) + 1);
								}
							}
						}
					}
				}
				
				for(Entry<String, Integer> entry : map.entrySet())
				{
					sender.addChatMessage(new TextComponentString("x" + entry.getValue() + " " + entry.getKey()));
				}
			}
			catch(Exception e)
			{
				throw new CommandException("Invalid position");
			}
		}
		else
		{
            throw new WrongUsageException(this.getCommandUsage(sender));
		}
	}
	
	@Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
		if(args.length == 1 || args.length == 4)
		{
			return this.getListOfStringsMatchingLastWord(args, String.valueOf((int) sender.getCommandSenderEntity().posX));
		}
		else if(args.length == 2 || args.length == 5)
		{
			return this.getListOfStringsMatchingLastWord(args, String.valueOf((int) sender.getCommandSenderEntity().posY));
		}
		else if(args.length == 3 || args.length == 6)
		{
			return this.getListOfStringsMatchingLastWord(args, String.valueOf((int) sender.getCommandSenderEntity().posZ));
		}
		
		return Collections.<String>emptyList();
    }
}
