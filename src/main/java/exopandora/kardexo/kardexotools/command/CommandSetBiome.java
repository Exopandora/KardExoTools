package exopandora.kardexo.kardexotools.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import exopandora.kardexo.kardexotools.command.arguments.BiomeArgument;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.ColumnPosArgument;
import net.minecraft.command.arguments.ColumnPosArgument.ColumnPos;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.IRegistry;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.WorldServer;
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
								.executes(context -> CommandSetBiome.setBiome(context.getSource(), ColumnPosArgument.getColumnPos(context, "from"), ColumnPosArgument.getColumnPos(context, "to"), BiomeArgument.getBiome(context, "biome")))))));
	}
	
	private static int setBiome(CommandSource source, ColumnPos from, ColumnPos to, Biome biome) throws CommandSyntaxException
	{
		WorldServer world = source.getWorld();
		
		int minX = Math.min(from.field_212600_a, to.field_212600_a);
		int maxX = Math.max(from.field_212600_a, to.field_212600_a);
		int minZ = Math.min(from.field_212601_b, to.field_212601_b);
		int maxZ = Math.max(from.field_212601_b, to.field_212601_b);
		
		int chunkMinX = MathHelper.floor(minX / 16F);
		int chunkMaxX = MathHelper.floor(maxX / 16F);
		int chunkMinZ = MathHelper.floor(minZ / 16F);
		int chunkMaxZ = MathHelper.floor(maxZ / 16F);
		
		for(int chunkX = chunkMinX; chunkX <= chunkMaxX; chunkX++)
		{
			for(int chunkZ = chunkMinZ; chunkZ <= chunkMaxZ; chunkZ++)
			{
				Chunk chunk = world.getChunk(chunkX, chunkZ);
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
					PlayerChunkMapEntry entry = world.getPlayerChunkMap().getEntry(chunkX, chunkZ);
					
					if(entry != null)
					{
						entry.hasPlayerMatching(entityPlayerMP ->
						{
							entityPlayerMP.connection.sendPacket(new SPacketChunkData(chunk, 65535));
							world.getEntityTracker().sendLeashedEntitiesInChunk(entityPlayerMP, chunk);
							return false;
						});
					}
				}
			}
		}
		
		source.sendFeedback(new TextComponentString("Set biome to " + IRegistry.BIOME.getKey(biome)), false);
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
