package net.kardexo.kardexotools.mixin;

import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(BlockPredicateArgument.BlockPredicate.class)
public interface AccessorBlockPredicate
{
	@Accessor
	BlockState getState();
	
	@Accessor
	Set<Property<?>> getProperties();
	
	@Accessor
	@Nullable CompoundTag getNbt();
}
