package exopandora.kardexo.kardexotools.veinminer;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

public class VeinminerConfigEntry
{
	private final ResourceLocation block;
	private int radius;
	
	public VeinminerConfigEntry(ResourceLocation block, int radius)
	{
		this.block = block;
		this.radius = radius;
	}
	
	public VeinminerConfigEntry(Block block, int radius)
	{
		this(Registry.BLOCK.getKey(block), radius);
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
	public static Block toBlock(VeinminerConfigEntry option)
	{
		if(option != null)
		{
			return Registry.BLOCK.getOrDefault(option.getBlock());
		}
		
		return Blocks.AIR;
	}
}
