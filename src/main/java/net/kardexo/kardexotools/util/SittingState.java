package net.kardexo.kardexotools.util;

public class SittingState
{
	private boolean isShiftKeyDownO;
	private long lastShiftDownTime = -1;
	private long sitDownTime = -1;
	
	public boolean isShiftKeyDownO()
	{
		return this.isShiftKeyDownO;
	}
	
	public void setIsShiftKeyDownO(boolean shiftKeyDownO)
	{
		this.isShiftKeyDownO = shiftKeyDownO;
	}
	
	public long getLastShiftDownTime()
	{
		return this.lastShiftDownTime;
	}
	
	public void setLastShiftDownTime(long lastShiftDownTime)
	{
		this.lastShiftDownTime = lastShiftDownTime;
	}
	
	public long getSitDownTime()
	{
		return this.sitDownTime;
	}
	
	public void setSitDownTime(long sitDownTime)
	{
		this.sitDownTime = sitDownTime;
	}
}
