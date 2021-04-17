package net.kardexo.kardexotools.property;

import com.google.gson.annotations.SerializedName;

import net.kardexo.kardexotools.KardExo;

public class PropertyOwner
{
	@SerializedName("name")
	private String name;
	@SerializedName("enter_message")
	private String enterMessage;
	@SerializedName("exit_message")
	private String exitMessage;
	@SerializedName("notify")
	private boolean notify;
	@SerializedName("creator")
	private boolean creator;
	
	public PropertyOwner(String name)
	{
		this(name, false, true, null, null);
	}
	
	public PropertyOwner(String name, boolean creator, boolean notify, String enterMessage, String exitMessage)
	{
		this.name = name;
		this.creator = creator;
		this.notify = notify;
		this.enterMessage = enterMessage;
		this.exitMessage = exitMessage;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public boolean doNotify()
	{
		return this.notify;
	}
	
	public void setNotify(boolean notify)
	{
		this.notify = notify;
	}
	
	public String getEnterMessage()
	{
		return this.enterMessage;
	}
	
	public void setEnterMessage(String message)
	{
		this.enterMessage = message;
	}
	
	public String getExitMessage()
	{
		return this.exitMessage;
	}
	
	public void setExitMessage(String message)
	{
		this.exitMessage = message;
	}
	
	public void resetEnterMessage()
	{
		this.enterMessage = KardExo.CONFIG.getPropertyDefaultEnterMessage();
	}
	
	public void resetExitMessage()
	{
		this.exitMessage = KardExo.CONFIG.getPropertyDefaultExitMessage();
	}
	
	public boolean isCreator()
	{
		return this.creator;
	}
	
	public void setCreator(boolean creator)
	{
		this.creator = creator;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if(obj != null && obj instanceof PropertyOwner)
		{
			return this.name.equals(((PropertyOwner) obj).name);
		}
		
		return false;
	}
}
