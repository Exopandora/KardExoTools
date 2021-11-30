package net.kardexo.kardexotools.veinminer;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class Vein
{
	private final ResourceKey<Level> level;
	private final Map<BlockState, Set<BlockPos>> stateMap;
	
	public Vein(ResourceKey<Level> level, Map<BlockState, Set<BlockPos>> stateMap)
	{
		this.level = level;
		this.stateMap = stateMap;
	}
	
	public ResourceKey<Level> getLevel()
	{
		return this.level;
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
