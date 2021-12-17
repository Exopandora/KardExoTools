package net.kardexo.kardexotools.config;

import com.google.gson.annotations.SerializedName;

public class PlayerConfig
{
	@SerializedName("veinminer")
	private boolean veinminer;
	@SerializedName("home")
	private PlayerHome home;
	
	public boolean isVeinminerEnabled()
	{
		return this.veinminer;
	}
	
	public void setVeinminerEnabled(boolean enabled)
	{
		this.veinminer = enabled;
	}
	
	public PlayerHome getHome()
	{
		return this.home;
	}
	
	public void setHome(PlayerHome home)
	{
		this.home = home;
	}
}
