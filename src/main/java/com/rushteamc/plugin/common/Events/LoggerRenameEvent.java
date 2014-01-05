package com.rushteamc.plugin.common.Events;

import java.io.Serializable;

public class LoggerRenameEvent
	implements Serializable
{
	private static final long serialVersionUID = -2577391764752518567L;
	private final String loggerName;

	public LoggerRenameEvent(String loggerName)
	{
		this.loggerName = loggerName;
	}

	public String getLoggerName()
	{
		return this.loggerName;
	}
}

