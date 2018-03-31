package exopandora.kardexo.kardexotools;

public class Util
{
	public static int getDimension(String name) throws InvalidDimensionException
	{
		if(name.equalsIgnoreCase("overworld") || name.equals("0"))
		{
			return 0;
		}
		else if(name.equalsIgnoreCase("nether") || name.equals("-1"))
		{
			return -1;
		}
		else if(name.equalsIgnoreCase("end") || name.equals("1"))
		{
			return 1;
		}
		
		throw new InvalidDimensionException("Invalid Dimension", new Object[0]);
	}
	
	public static String getDimension(int id)
	{
		switch(id)
		{
			case -1:
				return "Nether";
			case 0:
				return "Overworld";
			case 1:
				return "The End";
			default:
				return "Unknown";
		}
	}
}
