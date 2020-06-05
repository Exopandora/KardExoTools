package net.kardexo.kardexotools.base;

import com.google.gson.annotations.SerializedName;

public class PropertyOwner
{
	private static final String DEFAULT_ENTER_MESSAGE = "%1$s has entered your base (%2$s)";
	private static final String DEFAULT_EXIT_MESSAGE = "%1$s has left your base (%2$s)";
	
	private String name;
	
	@SerializedName("enter_message")
	private String enterMessage;
	
	@SerializedName("exit_message")
	private String exitMessage;
	
	private boolean notify;
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
		this.enterMessage = PropertyOwner.DEFAULT_ENTER_MESSAGE;
	}
	
	public void resetExitMessage()
	{
		this.exitMessage = PropertyOwner.DEFAULT_EXIT_MESSAGE;
	}
	
	public static String getDefaultEnterMessage()
	{
		return PropertyOwner.DEFAULT_ENTER_MESSAGE;
	}
	
	public static String getDefaultExitMessage()
	{
		return PropertyOwner.DEFAULT_EXIT_MESSAGE;
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
		if(obj != null)
		{
			if(obj instanceof PropertyOwner)
			{
				return this.name.equals(((PropertyOwner) obj).name);
			}
		}
		
		return false;
	}
}

