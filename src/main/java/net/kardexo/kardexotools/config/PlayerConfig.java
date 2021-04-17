package net.kardexo.kardexotools.config;

public class PlayerConfig
{
	private final String player;
	private boolean veinminer;
	private PlayerHome home;
	
	public PlayerConfig(String player)
	{
		this.player = player;
	}
	
	public String getPlayer()
	{
		return this.player;
	}
	
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
