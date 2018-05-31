package exopandora.kardexo.kardexotools;

import net.minecraft.util.math.BlockPos;

public class Home
{
	private final BlockPos pos;
	private final String player;
	private final int dimension;
	
	public Home(BlockPos home, String player, int dimension)
	{
		this.pos = home;
		this.player = player;
		this.dimension = dimension;
	}
	
	public BlockPos getPosition()
	{
		return this.pos;
	}
	
	public String getPlayer()
	{
		return this.player;
	}
	
	public int getDimension()
	{
		return this.dimension;
	}
}
