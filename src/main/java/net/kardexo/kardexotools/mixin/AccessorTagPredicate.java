package net.kardexo.kardexotools.mixin;

import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.core.HolderSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(BlockPredicateArgument.TagPredicate.class)
public interface AccessorTagPredicate
{
	@Accessor
	HolderSet<Block> getTag();
	
	@Accessor
	Map<String, String> getVagueProperties();
	
	@Accessor
	@Nullable CompoundTag getNbt();
}
