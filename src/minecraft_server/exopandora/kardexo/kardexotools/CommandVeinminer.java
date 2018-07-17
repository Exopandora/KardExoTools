package exopandora.kardexo.kardexotools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

public class CommandVeinminer extends CommandBase
{
	@Override
	public String getName()
	{
		return "veinminer";
	}
	
	@Override
	public String getUsage(ICommandSender sender)
	{
		return "/veinminer <on|off|list>";
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
		if(args.length > 0)
		{
			if(args[0].equals("on"))
			{
				Config.PLAYERS.getData().put(sender.getName(), new PlayerData(sender.getName(), true));
				sender.sendMessage(new TextComponentString("Veinminer enabled"));
			}
			else if(args[0].equals("off"))
			{
				Config.PLAYERS.getData().put(sender.getName(), new PlayerData(sender.getName(), false));
				sender.sendMessage(new TextComponentString("Veinminer disabled"));
			}
			else if(args[0].equals("list"))
			{
				List<ITextComponent> list = new ArrayList<ITextComponent>(Config.VEINMINER.getData().size());
				
				for(Entry<IBlockState, VeinminerEntry> entry : Config.VEINMINER.getData().entrySet())
				{
					IBlockState state = entry.getKey();
					Block block = state.getBlock();
					ItemStack stack = new ItemStack(block, 1, block.getMetaFromState(state));
					list.add(new TextComponentTranslation("%s = %s", stack.getTextComponent(), entry.getValue().getRadius()));
				}
				
				list.sort((a, b) -> a.toString().compareTo(b.toString()));
				list.forEach(sender::sendMessage);
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
	public boolean checkPermission(MinecraftServer server, ICommandSender sender)
	{
		return true;
	}
	
	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos)
	{
		return args.length == 1 ? this.getListOfStringsMatchingLastWord(args, new String[] {"on", "off", "list"}) : Collections.<String>emptyList();
	}
}
