package net.kardexo.kardexotools.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.ColumnPosArgument;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.network.play.server.SChunkDataPacket;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.ColumnPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;

public class CommandSetBiome
{
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(Commands.literal("setbiome")
				.requires(source -> source.hasPermissionLevel(4))
					.then(Commands.argument("from", ColumnPosArgument.columnPos())
						.then(Commands.argument("to", ColumnPosArgument.columnPos())
							.then(Commands.argument("biome", ResourceLocationArgument.resourceLocation())
								.suggests(CommandBase.ALL_BIOMES)
								.executes(context -> CommandSetBiome.setBiome(context.getSource(), ColumnPosArgument.fromBlockPos(context, "from"), ColumnPosArgument.fromBlockPos(context, "to"), ResourceLocationArgument.getResourceLocation(context, "biome")))))));
	}
	
	private static int setBiome(CommandSource source, ColumnPos from, ColumnPos to, ResourceLocation resource) throws CommandSyntaxException
	{
		ServerWorld world = source.getWorld();
		Biome biome = Registry.BIOME.getValue(resource).orElseThrow(() -> CommandBase.BIOME_NOT_FOUND.create(resource));
		
		int minX = Math.min(from.x, to.x);
		int maxX = Math.max(from.x, to.x);
		int minZ = Math.min(from.z, to.z);
		int maxZ = Math.max(from.z, to.z);
		
		int chunkMinX = minX >> 4;
		int chunkMaxX = maxX >> 4;
		int chunkMinZ = minZ >> 4;
		int chunkMaxZ = maxZ >> 4;
		
		int chunkClampMinX = MathHelper.clamp(chunkMinX, -1875000, 1875000);
		int chunkClampMaxX = MathHelper.clamp(chunkMaxX, -1875000, 1875000);
		int chunkClampMinZ = MathHelper.clamp(chunkMinZ, -1875000, 1875000);
		int chunkClampMaxZ = MathHelper.clamp(chunkMaxZ, -1875000, 1875000);
		
		for(int chunkX = chunkClampMinX; chunkX <= chunkClampMaxX; chunkX++)
		{
			for(int chunkZ = chunkClampMinZ; chunkZ <= chunkClampMaxZ; chunkZ++)
			{
				Chunk chunk = world.getChunk(chunkX, chunkZ);
				Biome[] biomes = chunk.getBiomes().biomes;
				
				int subChunkMinX = minChunkOffset(chunkX, chunkMinX, minX) >> 2;
				int subChunkMinZ = minChunkOffset(chunkZ, chunkMinZ, minZ) >> 2;
				
				int subChunkMaxX = maxChunkOffset(chunkX, chunkMaxX, maxX) >> 2;
				int subChunkMaxZ = maxChunkOffset(chunkZ, chunkMaxZ, maxZ) >> 2;
				
				boolean flag = false;
				
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
					chunk.markDirty();
					world.getChunkProvider().chunkManager.getTrackingPlayers(chunk.getPos(), false).forEach(player ->
					{
						player.connection.sendPacket(new SChunkDataPacket(chunk, 65535, true));
					});
				}
			}
		}
		
		source.sendFeedback(new StringTextComponent("Set biome to " + Registry.BIOME.getKey(biome)), false);
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
