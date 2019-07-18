package exopandora.kardexo.kardexotools.config;

public class PlayerConfig
{
	private final String player;
	private boolean veinminer;
	
	public PlayerConfig(String player, boolean veinminer)
	{
		this.player = player;
		this.veinminer = veinminer;
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
}
