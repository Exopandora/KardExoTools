package exopandora.kardexo.kardexotools;

import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.init.Biomes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.layer.IntCache;

public class CommandLocateBiome extends CommandBase
{
	@Override
	public String getName()
	{
		return "locatebiome";
	}
	
	@Override
	public String getUsage(ICommandSender sender)
	{
		return "/locatebiome <biome>";
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
		sender.sendMessage(new TextComponentString("Currently not supported"));
//		BlockPos pos = server.getEntityWorld().getBiomeProvider().findBiomePosition(sender.getPosition().getX(), sender.getPosition().getZ(), 5, Lists.newArrayList(Biomes.DESERT), new Random());
//		this.test(sender.getPosition().getX(), sender.getPosition().getZ(), 20, Lists.newArrayList(Biomes.DESERT), new Random());
//		if(pos != null)
//		{
//			sender.sendMessage(new TextComponentString(pos.toString()));
//		}
//		else
//		{
//			sender.sendMessage(new TextComponentString("No result"));
//		}
	}
	
//	private BlockPos test(int x, int z, int range, List<Biome> biomes, Random random)
//    {
//        IntCache.resetIntCache();
//        int i = x - range >> 2;
//        int j = z - range >> 2;
//        int k = x + range >> 2;
//        int l = z + range >> 2;
//        int i1 = k - i + 1;
//        int j1 = l - j + 1;
//        
//        System.out.println(i1 + " " + j1);
//        
//        BlockPos blockpos = null;
//        int k1 = 0;
//
//        for (int l1 = 0; l1 < i1 * j1; ++l1)
//        {
//            int i2 = i + l1 % i1 << 2;
//            int j2 = j + l1 / i1 << 2;
//            System.out.println(l1 + " " + i2 + " " + j2);
//        }
//
//        return blockpos;
//    }
}
