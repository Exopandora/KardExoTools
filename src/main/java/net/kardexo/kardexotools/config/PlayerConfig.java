package net.kardexo.kardexotools.config;

public class PlayerConfig
{
	private boolean veinminer;
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
