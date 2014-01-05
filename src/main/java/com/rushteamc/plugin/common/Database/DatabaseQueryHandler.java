package com.rushteamc.plugin.common.Database;

import java.util.PriorityQueue;

public class DatabaseQueryHandler extends Thread
{
	private boolean running = true;
	private static final PriorityQueue<DatabaseRunnable<?>> queue = new PriorityQueue<DatabaseRunnable<?>>();

	public DatabaseQueryHandler()
	{
		start();
	}

	public void close()
	{
		this.running = false;
	}

	public void addDatabaseRunnable(DatabaseRunnable<?> databaseRunnable)
	{
		queue.add(databaseRunnable);
	}

	public void run()
	{
		while (this.running)
		{
			DatabaseRunnable databaseRunnable = queue.poll();
			if (databaseRunnable == null)
			{
				try {
					sleep(10L);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			else
			{
				try {
					databaseRunnable.setReturnValue(databaseRunnable.run());
				} catch (Throwable t) {
					t.printStackTrace();
				}
				databaseRunnable.setBlocking(false);
			}
		}
	}
}

