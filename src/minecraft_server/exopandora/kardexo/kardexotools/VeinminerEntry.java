package exopandora.kardexo.kardexotools;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.util.ResourceLocation;

public class VeinminerEntry
{
	private final String blockstate;
	private int radius;
	
	public VeinminerEntry(String blockstate, int radius)
	{
		this.blockstate = blockstate;
		this.radius = radius;
	}
	
	public String getBlockstate()
	{
		return blockstate;
	}
	
	public int getRadius()
	{
		return radius;
	}
	
	public void setRadius(int radius)
	{
		this.radius = radius;
	}
	
	@Nullable
	public static IBlockState toBlockState(VeinminerEntry option)
	{
		try
		{
			String[] split = option.getBlockstate().split("\\[", 2);
			return CommandBase.convertArgToBlockState(Block.REGISTRY.getObject(new ResourceLocation(split[0])), split.length > 1 ? split[1].replaceAll("\\]$", "") : "0");
		}
		catch(Exception e)
		{
			return null;
		}
	}
	
	public static VeinminerEntry fromBlockState(IBlockState state, int radius)
	{
		return new VeinminerEntry(state.toString(), radius);
	}
}
