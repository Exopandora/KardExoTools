package exopandora.kardexo.kardexotools.veinminer;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.IRegistry;

public class VeinminerEntry
{
	private final ResourceLocation block;
	private int radius;
	
	public VeinminerEntry(ResourceLocation block, int radius)
	{
		this.block = block;
		this.radius = radius;
	}
	
	public VeinminerEntry(Block block, int radius)
	{
		this(IRegistry.BLOCK.getKey(block), radius);
	}
	
	public ResourceLocation getBlock()
	{
		return this.block;
	}
	
	public int getRadius()
	{
		return this.radius;
	}
	
	public void setRadius(int radius)
	{
		this.radius = radius;
	}
	
	@Nullable
	public static Block toBlock(VeinminerEntry option)
	{
		if(option != null)
		{
			return IRegistry.BLOCK.get(option.getBlock());
		}
		
		return Blocks.AIR;
	}
}
