package net.kardexo.kardexotools.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

public class SetBiomeCommand
{
	public static final SuggestionProvider<CommandSourceStack> AVAILABLE_BIOMES = SuggestionProviders.register(new ResourceLocation("available_biomes"), (context, builder) ->
	{
		return SharedSuggestionProvider.suggestResource(context.getSource().registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).keySet(), builder);
	});
	private static final DynamicCommandExceptionType ERROR_BIOME_NOT_FOUND = new DynamicCommandExceptionType((p_137846_) ->
	{
		return new TranslatableComponent("commands.locatebiome.notFound", p_137846_);
	});
	
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		dispatcher.register(Commands.literal("setbiome")
				.requires(source -> source.hasPermission(4))
					.then(Commands.argument("from", BlockPosArgument.blockPos())
						.then(Commands.argument("to", BlockPosArgument.blockPos())
							.then(Commands.argument("biome", ResourceLocationArgument.id())
								.suggests(AVAILABLE_BIOMES)
								.executes(context -> SetBiomeCommand.setBiome(context.getSource(), BlockPosArgument.getLoadedBlockPos(context, "from"), BlockPosArgument.getLoadedBlockPos(context, "to"), ResourceLocationArgument.getId(context, "biome")))))));
	}
	
	private static int setBiome(CommandSourceStack source, BlockPos from, BlockPos to, ResourceLocation resource) throws CommandSyntaxException
	{
		ServerLevel level = source.getLevel();
		Biome biome = source.getServer().registryAccess().registryOrThrow(Registry.BIOME_REGISTRY)
				.getOptional(resource)
				.orElseThrow(() -> ERROR_BIOME_NOT_FOUND.create(resource));
		Holder<Biome> holder = Holder.direct(biome);
		
		int minX = Mth.clamp(Math.min(from.getX(), to.getX()), -30_000_000, 30_000_000);
		int maxX = Mth.clamp(Math.max(from.getX(), to.getX()), -30_000_000, 30_000_000);
		int minY = Mth.clamp(Math.min(from.getY(), to.getY()), level.getMinBuildHeight(), level.getMaxBuildHeight());
		int maxY = Mth.clamp(Math.max(from.getY(), to.getY()), level.getMinBuildHeight(), level.getMaxBuildHeight());
		int minZ = Mth.clamp(Math.min(from.getZ(), to.getZ()), -30_000_000, 30_000_000);
		int maxZ = Mth.clamp(Math.max(from.getZ(), to.getZ()), -30_000_000, 30_000_000);
		
		int chunkMinX = minX >> 4;
		int chunkMaxX = maxX >> 4;
		int chunkMinY = minY >> 4;
		int chunkMaxY = maxY >> 4;
		int chunkMinZ = minZ >> 4;
		int chunkMaxZ = maxZ >> 4;
		
		for(int chunkX = chunkMinX; chunkX <= chunkMaxX; chunkX++)
		{
			int subChunkMinX = minChunkOffset(chunkX, chunkMinX, minX) >> 2;
			int subChunkMaxX = maxChunkOffset(chunkX, chunkMaxX, maxX) >> 2;
			
			for(int chunkZ = chunkMinZ; chunkZ <= chunkMaxZ; chunkZ++)
			{
				int subChunkMinZ = minChunkOffset(chunkZ, chunkMinZ, minZ) >> 2;
				int subChunkMaxZ = maxChunkOffset(chunkZ, chunkMaxZ, maxZ) >> 2;
				LevelChunk chunk = level.getChunk(chunkX, chunkZ);
				
				for(int sectionY = chunkMinY; sectionY <= chunkMaxY; sectionY++)
				{
					int subChunkMinY = minChunkOffset(sectionY, chunkMinY, minY) >> 2;
					int subChunkMaxY = maxChunkOffset(sectionY, chunkMaxY, maxY) >> 2;
					LevelChunkSection section = chunk.getSection(chunk.getSectionIndexFromSectionY(sectionY));
					section.acquire();
					boolean save = false;
					
					for(int y = subChunkMinY; y <= subChunkMaxY; y++)
					{
						for(int z = subChunkMinZ; z <= subChunkMaxZ; z++)
						{
							for(int x = subChunkMinX; x <= subChunkMaxX; x++)
							{
								if(!section.getBiomes().get(x, y, z).equals(biome))
								{
									section.getBiomes().getAndSet(x, y, z, holder);
									save = true;
								}
							}
						}
					}
					
					section.release();
					
					if(save)
					{
						chunk.setUnsaved(true);
						ServerChunkCache chunkCache = level.getChunkSource();
						chunkCache.chunkMap.getPlayers(chunk.getPos(), false).forEach(player ->
						{
							player.connection.send(new ClientboundLevelChunkWithLightPacket(chunk, chunkCache.getLightEngine(), null, null, true));
						});
					}
				}
			}
		}
		
		source.sendSuccess(new TextComponent("Set biome to " + resource), false);
		return (maxX - minX) * (maxZ - minZ);
	}
	
	private static int chunkOffset(int chunk, int borderChunk, int position, int def)
	{
		if(chunk == borderChunk)
		{
			return Math.floorMod(position, 16);
		}
		
		return def;
	}
	
	private static int minChunkOffset(int chunk, int borderChunk, int position)
	{
		return chunkOffset(chunk, borderChunk, position, 0);
	}
	
	private static int maxChunkOffset(int chunk, int borderChunk, int position)
	{
		return chunkOffset(chunk, borderChunk, position, 15);
	}
}
