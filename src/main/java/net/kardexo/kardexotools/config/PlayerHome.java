package net.kardexo.kardexotools.config;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

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
