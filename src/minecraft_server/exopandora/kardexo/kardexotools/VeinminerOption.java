package exopandora.kardexo.kardexotools;

public class VeinminerOption
{
	private final String player;
	private boolean enabled;
	
	public VeinminerOption(String player, boolean enabled)
	{
		this.player = player;
		this.enabled = enabled;
	}
	
	public String getPlayer()
	{
		return this.player;
	}
	
	public boolean isEnabled()
	{
		return this.enabled;
	}
	
	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}
}
