package exopandora.kardexo.kardexotools.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import exopandora.kardexo.kardexotools.command.arguments.BiomeArgument;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.ColumnPosArgument;
import net.minecraft.network.play.server.SChunkDataPacket;
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
							.then(Commands.argument("biome", BiomeArgument.biome())
								.executes(context -> CommandSetBiome.setBiome(context.getSource(), ColumnPosArgument.func_218101_a(context, "from"), ColumnPosArgument.func_218101_a(context, "to"), BiomeArgument.getBiome(context, "biome")))))));
	}
	
	private static int setBiome(CommandSource source, ColumnPos from, ColumnPos to, Biome biome) throws CommandSyntaxException
	{
		ServerWorld world = source.getWorld();
		
		int minX = Math.min(from.x, to.x);
		int maxX = Math.max(from.x, to.x);
		int minZ = Math.min(from.z, to.z);
		int maxZ = Math.max(from.z, to.z);
		
		int chunkMinX = MathHelper.floor(minX / 16F);
		int chunkMaxX = MathHelper.floor(maxX / 16F);
		int chunkMinZ = MathHelper.floor(minZ / 16F);
		int chunkMaxZ = MathHelper.floor(maxZ / 16F);
		
		for(int chunkX = chunkMinX; chunkX <= chunkMaxX; chunkX++)
		{
			for(int chunkZ = chunkMinZ; chunkZ <= chunkMaxZ; chunkZ++)
			{
				Chunk chunk = world.getChunk(chunkX, chunkZ);
				Biome[] biomes = chunk.func_225549_i_().getBiomeArray();
				
				int subChunkMinX = MathHelper.floor(minChunkOffset(chunkX, chunkMinX, minX) / 4);
				int subChunkMinZ = MathHelper.floor(minChunkOffset(chunkZ, chunkMinZ, minZ) / 4);
				
				int subChunkMaxX = MathHelper.floor(maxChunkOffset(chunkX, chunkMaxX, maxX) / 4);
				int subChunkMaxZ = MathHelper.floor(maxChunkOffset(chunkZ, chunkMaxZ, maxZ) / 4);
				
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
						player.connection.sendPacket(new SChunkDataPacket(chunk, 65535));
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
