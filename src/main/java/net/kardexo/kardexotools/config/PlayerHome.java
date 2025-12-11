package net.kardexo.kardexotools.config;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

public class PlayerHome
{
	private final BlockPos position;
	private final Identifier dimension;
	
	public PlayerHome(BlockPos position, Identifier dimension)
	{
		this.position = position;
		this.dimension = dimension;
	}
	
	public BlockPos getPosition()
	{
		return this.position;
	}
	
	public Identifier getDimension()
	{
		return this.dimension;
	}
}
