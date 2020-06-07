package net.kardexo.kardexotools.config;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;

public class PlayerHome
{
	private final BlockPos position;
	private final int dimension;
	
	public PlayerHome(BlockPos position, int dimension)
	{
		this.position = position;
		this.dimension = dimension;
	}
	
	public BlockPos getPosition()
	{
		return this.position;
	}
	
	public int getDimension()
	{
		return this.dimension;
	}
	
	public DimensionType getDimensionType()
	{
		return DimensionType.getById(this.dimension);
	}
}
