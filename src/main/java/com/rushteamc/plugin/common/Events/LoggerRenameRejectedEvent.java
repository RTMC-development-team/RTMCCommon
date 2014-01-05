package com.rushteamc.plugin.common.Events;

import java.io.Serializable;

public class LoggerRenameRejectedEvent
	implements Serializable
{
	private static final long serialVersionUID = -6523060320419854563L;
	private final String loggerName;

	public LoggerRenameRejectedEvent(String loggerName)
	{
		this.loggerName = loggerName;
	}

	public String getLoggerName()
	{
		return this.loggerName;
	}
}

