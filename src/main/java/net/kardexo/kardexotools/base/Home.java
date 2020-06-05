package net.kardexo.kardexotools.base;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;

public class Home
{
	private final BlockPos home;
	private final String player;
	private final int dimension;
	
	public Home(BlockPos home, String player, int dimension)
	{
		this.home = home;
		this.player = player;
		this.dimension = dimension;
	}
	
	public BlockPos getPosition()
	{
		return this.home;
	}
	
	public String getPlayer()
	{
		return this.player;
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
