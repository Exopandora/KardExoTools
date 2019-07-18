package exopandora.kardexo.kardexotools.veinminer;

import java.util.List;
import java.util.Map;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;

public class VeinminerHistoryEntry
{
	private final DimensionType dimension;
	private final Map<BlockState, List<BlockPos>> stateMap;
	
	public VeinminerHistoryEntry(DimensionType dimension, Map<BlockState, List<BlockPos>> stateMap)
	{
		this.dimension = dimension;
		this.stateMap = stateMap;
	}
	
	public DimensionType getDimension()
	{
		return this.dimension;
	}
	
	public Map<BlockState, List<BlockPos>> getStateMap()
	{
		return this.stateMap;
	}
	
	public void addState()
	{
		
	}
}
