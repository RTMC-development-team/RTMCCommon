package com.rushteamc.plugin.common.Database;

public abstract class DatabaseRunnable<returnType>
	implements Comparable<DatabaseRunnable<?>>
{
	private final Priority priority;
	private boolean blocking;
	private returnType returnValue;

	public DatabaseRunnable()
	{
		this(Priority.MEDIUM, false);
	}

	public DatabaseRunnable(boolean blocking)
	{
		this(Priority.MEDIUM, blocking);
	}

	public DatabaseRunnable(Priority priority)
	{
		this(priority, false);
	}

	public DatabaseRunnable(Priority priority, boolean blocking)
	{
		this.priority = priority;
		setBlocking(blocking);
	}

	public int compareTo(DatabaseRunnable<?> databaseRunnable)
	{
		return this.priority.compareTo(databaseRunnable.priority);
	}

	public abstract returnType run();

	boolean isBlocking()
	{
		return this.blocking;
	}

	void setBlocking(boolean blocking)
	{
		this.blocking = blocking;
	}

	returnType getReturnValue()
	{
		return this.returnValue;
	}

	void setReturnValue(returnType returnValue)
	{
		this.returnValue = returnValue;
	}

	public static enum Priority
	{
		LOWEST, 
		LOW, 
		MEDIUM, 
		HEIGH, 
		HIGHEST;
	}
}

