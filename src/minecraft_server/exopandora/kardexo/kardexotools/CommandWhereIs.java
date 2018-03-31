package exopandora.kardexo.kardexotools;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;

public class CommandWhereIs extends CommandBase
{
	@Override
	public String getCommandName()
	{
		return "whereis";
	}
	
	@Override
	public String getCommandUsage(ICommandSender sender)
	{
		return "/whereis <player>";
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
		if(args.length > 0)
		{
			for(EntityPlayerMP player : server.getPlayerList().getPlayerList())
			{
				if(player != null)
				{
					if(player.getName().equals(args[0]))
					{
						BlockPos pos = player.getPosition();
						String dimension = null;
						
						switch(player.dimension)
						{
							case -1:
								dimension = "Nether";
								break;
							case 0:
								dimension = "Overworld";
								break;
							case 1:
								dimension = "The End";
								break;
							default:
								dimension = "Unknown Dimension";
								break;
						}
						
						List<Property> bases = new ArrayList<Property>();
						
						for(Entry<Property, Set<String>> entry : TickableBases.BASE_VISITORS.entrySet())
						{
							if(entry.getValue().contains(player.getName()))
							{
								bases.add(entry.getKey());
							}
						}
						
						for(Property place : Config.PLACES.getData().values())
						{
							if(place.isInside(player))
							{
								bases.add(place);
							}
						}
						
						ITextComponent textComponent = null;
						
						for(Property place : bases)
						{
							if(textComponent == null)
							{
								textComponent = place.getDisplayName();
							}
							else
							{
								textComponent = new TextComponentTranslation("%s, %s", textComponent, place.getDisplayName());
							}
						}
						
						String result = "%s: d: " + dimension + " x: " + pos.getX() + " y: " + pos.getY() + " z: " + pos.getZ() + (textComponent != null ? " (%s)" : "");
						server.logInfo("Query: " + String.format(result, player.getName(), String.join(", ", bases.parallelStream().map(base -> base.getName()).collect(Collectors.toList()))));
						sender.addChatMessage(new TextComponentTranslation(result, player.getDisplayName(), textComponent));
						return;
					}
				}
			}
			
			throw new CommandException("Player " + args[0] + " could not be found");
		}
		else
		{
			throw new WrongUsageException(this.getCommandUsage(sender));
		}
	}
	
	@Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        return args.length == 1 ? this.getListOfStringsMatchingLastWord(args, server.getAllUsernames()) : Collections.<String>emptyList();
    }
    
	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender)
    {
		return true;
    }
}
