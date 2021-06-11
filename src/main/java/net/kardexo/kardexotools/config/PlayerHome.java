package net.kardexo.kardexotools.config;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class PlayerHome
{
	private final BlockPos position;
	private final ResourceLocation dimension;
	
	public PlayerHome(BlockPos position, ResourceLocation dimension)
	{
		this.position = position;
		this.dimension = dimension;
	}
	
	public BlockPos getPosition()
	{
		return this.position;
	}
	
	public ResourceLocation getDimension()
	{
		return this.dimension;
	}
}
