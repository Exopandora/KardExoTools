package net.kardexo.kardexotools.util;

import net.minecraft.world.entity.Entity;

public class SittingState
{
	private boolean isShiftKeyDownO;
	private long lastShiftDownTime = -1;
	private long sitDownTime = -1;
	private Entity vehicle;
	
	public Entity getVehicle()
	{
		return this.vehicle;
	}
	
	public void setVehicle(Entity vehicle)
	{
		this.vehicle = vehicle;
	}
	
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
