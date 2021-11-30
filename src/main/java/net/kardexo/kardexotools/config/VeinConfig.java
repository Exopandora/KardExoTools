package net.kardexo.kardexotools.config;

import com.google.gson.annotations.SerializedName;

public class VeinConfig
{
	@SerializedName("radius")
	private int radius;
	@SerializedName("requires_tool")
	private boolean requiresTool;
	
	public VeinConfig(int radius, boolean requiresTool)
	{
		this.radius = radius;
		this.requiresTool = requiresTool;
	}
	
	public int getRadius()
	{
		return this.radius;
	}
	
	public void setRadius(int radius)
	{
		this.radius = radius;
	}
	
	public boolean doesRequireTool()
	{
		return requiresTool;
	}
	
	public void setRequiresTool(boolean requiresTool)
	{
		this.requiresTool = requiresTool;
	}
}
