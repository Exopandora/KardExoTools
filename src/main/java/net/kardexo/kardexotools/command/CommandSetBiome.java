package net.kardexo.kardexotools.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.commands.LocateBiomeCommand;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.LevelChunk;

public class CommandSetBiome
{
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		dispatcher.register(Commands.literal("setbiome")
				.requires(source -> source.hasPermission(4))
					.then(Commands.argument("from", ColumnPosArgument.columnPos())
						.then(Commands.argument("to", ColumnPosArgument.columnPos())
							.then(Commands.argument("biome", ResourceLocationArgument.id())
								.suggests(SuggestionProviders.AVAILABLE_BIOMES)
								.executes(context -> CommandSetBiome.setBiome(context.getSource(), ColumnPosArgument.getColumnPos(context, "from"), ColumnPosArgument.getColumnPos(context, "to"), ResourceLocationArgument.getId(context, "biome")))))));
	}
	
	private static int setBiome(CommandSourceStack source, ColumnPos from, ColumnPos to, ResourceLocation resource) throws CommandSyntaxException
	{
		ServerLevel level = source.getLevel();
		Biome biome = source.getServer().registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).getOptional(resource).orElseThrow(() -> LocateBiomeCommand.ERROR_INVALID_BIOME.create(resource));
		
		int minX = Math.min(from.x, to.x);
		int maxX = Math.max(from.x, to.x);
		int minZ = Math.min(from.z, to.z);
		int maxZ = Math.max(from.z, to.z);
		
		int chunkMinX = minX >> 4;
		int chunkMaxX = maxX >> 4;
		int chunkMinZ = minZ >> 4;
		int chunkMaxZ = maxZ >> 4;
		
		int chunkClampMinX = Mth.clamp(chunkMinX, -1875000, 1875000);
		int chunkClampMaxX = Mth.clamp(chunkMaxX, -1875000, 1875000);
		int chunkClampMinZ = Mth.clamp(chunkMinZ, -1875000, 1875000);
		int chunkClampMaxZ = Mth.clamp(chunkMaxZ, -1875000, 1875000);
		
		for(int chunkX = chunkClampMinX; chunkX <= chunkClampMaxX; chunkX++)
		{
			for(int chunkZ = chunkClampMinZ; chunkZ <= chunkClampMaxZ; chunkZ++)
			{
				LevelChunk chunk = level.getChunk(chunkX, chunkZ);
				Biome[] biomes = chunk.getBiomes().biomes;
				boolean flag = false;
				
				int subChunkMinX = minChunkOffset(chunkX, chunkMinX, minX) >> 2;
				int subChunkMinZ = minChunkOffset(chunkZ, chunkMinZ, minZ) >> 2;
				int subChunkMaxX = maxChunkOffset(chunkX, chunkMaxX, maxX) >> 2;
				int subChunkMaxZ = maxChunkOffset(chunkZ, chunkMaxZ, maxZ) >> 2;
				
				for(int y = 0; y < 64; y++)
				{
					for(int z = subChunkMinZ; z <= subChunkMaxZ; z++)
					{
						for(int x = subChunkMinX; x <= subChunkMaxX; x++)
						{
							int index = (y << 4) + (z << 2) + x;
							
							if(!biomes[index].equals(biome))
							{
								biomes[index] = biome;
								flag = true;
							}
						}
					}
				}
				
				if(flag)
				{
					chunk.markUnsaved();
					ServerChunkCache chunkCache = level.getChunkSource();
					chunkCache.chunkMap.getPlayers(chunk.getPos(), false).forEach(player ->
					{
						player.connection.send(new ClientboundLevelChunkPacket(chunk));
					});
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
