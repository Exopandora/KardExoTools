package exopandora.kardexo.kardexotools.veinminer;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;

public class VeinminerHistoryEntry
{
	private final DimensionType dimension;
	private final Map<BlockState, Set<BlockPos>> stateMap;
	
	public VeinminerHistoryEntry(DimensionType dimension, Map<BlockState, Set<BlockPos>> stateMap)
	{
		this.dimension = dimension;
		this.stateMap = stateMap;
	}
	
	public DimensionType getDimension()
	{
		return this.dimension;
	}
	
	public Map<BlockState, Set<BlockPos>> getStateMap()
	{
		return this.stateMap;
	}
	
	@Nullable
	public Set<BlockPos> getPositions(BlockState state)
	{
		return this.stateMap.get(state);
	}
	
	public Set<BlockPos> getAllPositions()
	{
		return Veinminer.flatten(this.stateMap.values()).collect(Collectors.toSet());
	}
	
	@Nullable
	public Block getBlock()
	{
		if(!this.stateMap.isEmpty())
		{
			return this.stateMap.keySet().iterator().next().getBlock();
		}
		
		return null;
	}
}
