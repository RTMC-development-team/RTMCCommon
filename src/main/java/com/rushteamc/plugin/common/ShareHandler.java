package com.rushteamc.plugin.common;

import com.rushteamc.plugin.common.Events.LogMessageEvent;
import com.rushteamc.plugin.common.Events.LoggerRenameEvent;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Random;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class ShareHandler extends Handler
{
	private static final String NAME_PREFIX = "logger_";
	private final Assembler setup;
	private String logEventGroupName;
	private String logEventGroupPassword;
	private FileOutputStream outputStream;

	public ShareHandler(Assembler setup)
	{
		this.setup = setup;

		Random rand = new Random();
		byte[] byteArr = new byte[256];
		rand.nextBytes(byteArr);
		this.logEventGroupPassword = String.valueOf(byteArr);

		renameLogger(rand.nextInt());
	}

	public void deinit()
	{
		this.logEventGroupName = null;
		try
		{
			if (this.outputStream != null)
				this.outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getLogEventGroupName()
	{
		return this.logEventGroupName;
	}

	public void renameLogger(int num)
	{
		if (this.logEventGroupName != null) {
			this.setup.getSharedEventBus().removeGroup(this.logEventGroupName);
		}
		this.logEventGroupName = ("logger_" + num);
		this.setup.getSharedEventBus().addGroup(this.logEventGroupName, this.logEventGroupPassword);

		this.setup.getSharedEventBus().postEvent(new LoggerRenameEvent(this.logEventGroupName), false);
	}

	public void publish(LogRecord record)
	{
		if (this.logEventGroupName != null) {
			this.setup.getSharedEventBus().postGroupEvent(this.logEventGroupName, new LogMessageEvent(record));
		}
		if (this.outputStream == null) {
			return;
		}
		try
		{
			this.outputStream.write(String.format("[%s][%s]: %s\n", new Object[] { record.getLevel().getName(), record.getLoggerName(), record.getMessage() }).getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void flush()
	{
		if (this.outputStream == null)
			return;
		try
		{
			this.outputStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void close()
		throws SecurityException
	{
		deinit();
	}

	public boolean isLoggable(LogRecord record)
	{
		return record != null;
	}
}

