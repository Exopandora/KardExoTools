package net.kardexo.kardexotools.config;

import javax.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class VeinBlockConfig
{
	@SerializedName("block")
	private final ResourceLocation block;
	@SerializedName("radius")
	private int radius;
	@SerializedName("requires_tool")
	private boolean requiresTool;
	
	public VeinBlockConfig(ResourceLocation block, int radius, boolean requiresTool)
	{
		this.block = block;
		this.radius = radius;
		this.requiresTool = requiresTool;
	}
	
	public VeinBlockConfig(Block block, int radius, boolean requiresTool)
	{
		this(Registry.BLOCK.getKey(block), radius, requiresTool);
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
	
	public boolean doesRequireTool()
	{
		return requiresTool;
	}
	
	public void setRequiresTool(boolean requiresTool)
	{
		this.requiresTool = requiresTool;
	}
	
	@Nullable
	public static Block toBlock(VeinBlockConfig option)
	{
		if(option != null)
		{
			return Registry.BLOCK.get(option.getBlock());
		}
		
		return Blocks.AIR;
	}
}
