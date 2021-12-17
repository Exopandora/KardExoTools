package net.kardexo.kardexotools.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.kardexo.kardexotools.util.PropertyUtils;
import net.kardexo.kardexotools.veinminer.Veinminer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.level.GameType;

@Mixin(ServerPlayerGameMode.class)
public abstract class MixinServerPlayerGameMode
{
	@Shadow
	protected ServerLevel level;
	
	@Shadow
	protected ServerPlayer player;
	
	@Shadow
	private GameType gameModeForPlayer;
	
	@Shadow
	public abstract boolean isCreative();
	
	/**
	 * Destroys the vein at the given position and drops the resulting items at the position.
	 * 
	 * @author Exopandora
	 * @reason Used to implement veinminer functionality
	 * @return <code>true</code> if the block was harvested, otherwise <code>false</code>
	 */
	@Overwrite
	public boolean destroyBlock(BlockPos blockPos)
	{
		return PropertyUtils.canHarvestBlock(this.player, blockPos) && Veinminer.mine(blockPos, this.player, this.level, this.gameModeForPlayer);
	}
}
