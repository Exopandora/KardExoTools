package net.kardexo.kardexotools.config;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Nullable;

public class OwnerConfig
{
	@Nullable
	@SerializedName("enter_message")
	private String enterMessage;
	@Nullable
	@SerializedName("exit_message")
	private String exitMessage;
	@SerializedName("notify")
	private boolean notify;
	@SerializedName("creator")
	private boolean creator;
	
	public OwnerConfig()
	{
		this(false, true, null, null);
	}
	
	public OwnerConfig(boolean creator, boolean notify, @Nullable String enterMessage, @Nullable String exitMessage)
	{
		this.creator = creator;
		this.notify = notify;
		this.enterMessage = enterMessage;
		this.exitMessage = exitMessage;
	}
	
	public boolean doNotify()
	{
		return this.notify;
	}
	
	public void setNotify(boolean notify)
	{
		this.notify = notify;
	}
	
	@Nullable
	public String getEnterMessage()
	{
		return this.enterMessage;
	}
	
	public void setEnterMessage(@Nullable String message)
	{
		this.enterMessage = message;
	}
	
	@Nullable
	public String getExitMessage()
	{
		return this.exitMessage;
	}
	
	public void setExitMessage(@Nullable String message)
	{
		this.exitMessage = message;
	}
	
	public boolean isCreator()
	{
		return this.creator;
	}
	
	public void setCreator(boolean creator)
	{
		this.creator = creator;
	}
}
