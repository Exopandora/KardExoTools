package net.kardexo.kardexotools.veinminer;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class VeinminerHistoryEntry
{
	private final RegistryKey<World> world;
	private final Map<BlockState, Set<BlockPos>> stateMap;
	
	public VeinminerHistoryEntry(RegistryKey<World> world, Map<BlockState, Set<BlockPos>> stateMap)
	{
		this.world = world;
		this.stateMap = stateMap;
	}
	
	public RegistryKey<World> getWorld()
	{
		return this.world;
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
