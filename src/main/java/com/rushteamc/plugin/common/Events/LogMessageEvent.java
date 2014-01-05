package com.rushteamc.plugin.common.Events;

import java.io.Serializable;
import java.util.logging.LogRecord;

public class LogMessageEvent
	implements Serializable
{
	private static final long serialVersionUID = 7323945289958402552L;
	private final LogRecord record;

	public LogMessageEvent(LogRecord record)
	{
		this.record = record;
	}

	public LogRecord getLogRecord()
	{
		return this.record;
	}
}

