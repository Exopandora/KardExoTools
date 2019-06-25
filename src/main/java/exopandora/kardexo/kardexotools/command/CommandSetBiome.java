package exopandora.kardexo.kardexotools.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import exopandora.kardexo.kardexotools.command.arguments.BiomeArgument;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.ColumnPosArgument;
import net.minecraft.network.play.server.SChunkDataPacket;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ColumnPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.ServerWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;

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
		
		System.out.println(minX + " " + minZ + " " + maxX + " " + maxZ);
		
		int chunkMinX = MathHelper.floor(minX / 16F);
		int chunkMaxX = MathHelper.floor(maxX / 16F);
		int chunkMinZ = MathHelper.floor(minZ / 16F);
		int chunkMaxZ = MathHelper.floor(maxZ / 16F);
		
		for(int chunkX = chunkMinX; chunkX <= chunkMaxX; chunkX++)
		{
			for(int chunkZ = chunkMinZ; chunkZ <= chunkMaxZ; chunkZ++)
			{
				Chunk chunk = world.func_212866_a_(chunkX, chunkZ);
				Biome[] biomes = chunk.getBiomes();
				int xStart = getMinChunkOffset(chunkX, chunkMinX, minX);
				int zStart = getMinChunkOffset(chunkZ, chunkMinZ, minZ);
				int xEnd = getMaxChunkOffset(chunkX, chunkMaxX, maxX);
				int zEnd = getMaxChunkOffset(chunkZ, chunkMaxZ, maxZ);
				boolean flag = false;
				
				for(int x = xStart; x <= xEnd; x++)
				{
					for(int z = zStart; z <= zEnd; z++)
					{
						int posX = toCoord(chunkX, x);
						int posZ = toCoord(chunkZ, z);								
						int index = (posZ & 0xF) << 4 | posX & 0xF;
						
						if(!biomes[index].equals(biome))
						{
							biomes[index] = biome;
							flag = true;
						}
					}
				}
				
				if(flag)
				{
					chunk.markDirty();
					world.func_72863_F().chunkManager.getTrackingPlayers(new ChunkPos(chunkX, chunkZ), false).forEach(player ->
					{
						player.connection.sendPacket(new SChunkDataPacket(chunk, 65535));
					});
				}
			}
		}
		
		source.sendFeedback(new StringTextComponent("Set biome to " + Registry.BIOME.getKey(biome)), false);
		return (maxX - minX) * (maxZ - minZ);
	}
	
	private static int toCoord(int chunk, int pos)
	{
		return (chunk * 16) + (pos % 16);
	}
	
	private static int getChunkOffset(int chunk, int borderChunk, int position, int def)
	{
		if(chunk == borderChunk)
		{
			return Math.floorMod(position, 16);
		}
		
		return def;
	}
	
	private static int getMinChunkOffset(int chunk, int borderChunk, int position)
	{
		return getChunkOffset(chunk, borderChunk, position, 0);
	}
	
	private static int getMaxChunkOffset(int chunk, int borderChunk, int position)
	{
		return getChunkOffset(chunk, borderChunk, position, 15);
	}
}
