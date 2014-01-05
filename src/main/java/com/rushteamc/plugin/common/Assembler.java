package com.rushteamc.plugin.common;

import com.rushteamc.lib.SharedEventBus.SharedEventBus;
import com.rushteamc.plugin.common.Authentication.Authenticator;
import com.rushteamc.plugin.common.Authentication.GroupAccessChecker;
import com.rushteamc.plugin.common.Database.DatabaseManager;
import com.rushteamc.plugin.common.Events.CommonSharedEventHandler;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class Assembler
{
	private SharedEventBus eventBus;
	private final String eventBusGroupName;
	private final String eventBusGroupPass;
	private final ShareHandler loggerHandler;
	private final Authenticator authenticator;

	public Assembler(Collection<String> addresses, byte[] publicKey, byte[] privateKey, GroupAccessChecker groupAccessChecker)
	{
		this(addresses, null, null, null, null, null, null, publicKey, privateKey, groupAccessChecker);
	}

	public Assembler(Collection<String> addresses, String host, String database, String username, String password, String eventBusGroupName, String eventBusGroupPass, byte[] publicKey, byte[] privateKey, GroupAccessChecker groupAccessChecker)
	{
		this.eventBusGroupName = eventBusGroupName;
		this.eventBusGroupPass = eventBusGroupPass;

		Set<InetSocketAddress> addessList = new LinkedHashSet<InetSocketAddress>();
		for (String address : addresses)
		{
			int portind = address.lastIndexOf(':');
			try
			{
				InetSocketAddress adr = new InetSocketAddress(address.substring(0, portind), Integer.parseInt(address.substring(portind + 1)));
				addessList.add(adr);
			} catch (IndexOutOfBoundsException e) {
				e.printStackTrace();
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		}
		this.eventBus = new SharedEventBus(addessList);
		this.eventBus.addHandler(new CommonSharedEventHandler(this));

		if ((eventBusGroupName != null) && (eventBusGroupPass != null)) {
			this.eventBus.addGroup(eventBusGroupName, eventBusGroupPass);
		}
		if ((host != null) && (database != null) && (username != null) && (password != null)) {
			DatabaseManager.setup(host, database, username, password);
		}
		this.authenticator = new Authenticator(this, publicKey, privateKey, groupAccessChecker);

		this.loggerHandler = new ShareHandler(this);
	}

	public void deinit()
	{
		this.eventBus.close();
		this.eventBus = null;

		Player.deinint();
		Group.deinit();

		DatabaseManager.close();
	}

	public SharedEventBus getSharedEventBus()
	{
		return this.eventBus;
	}

	public String getEventBusGroupName()
	{
		return this.eventBusGroupName;
	}

	public String getEventBusGroupPass()
	{
		return this.eventBusGroupPass;
	}

	public void publisEvent(Serializable event)
	{
		getSharedEventBus().postEvent(event);
	}

	public void publisSecureEvent(Serializable event)
	{
		getSharedEventBus().postGroupEvent(getEventBusGroupName(), event);
	}

	public boolean verifySecureEvent(String groupname)
	{
		return getEventBusGroupName().equals(groupname);
	}

	public ShareHandler getLoggerHandler()
	{
		return this.loggerHandler;
	}

	public Authenticator getAuthenticator()
	{
		return this.authenticator;
	}
}

