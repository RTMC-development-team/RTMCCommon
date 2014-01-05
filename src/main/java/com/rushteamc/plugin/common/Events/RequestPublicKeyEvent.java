package com.rushteamc.plugin.common.Events;

import java.io.Serializable;

public class RequestPublicKeyEvent
	implements Serializable
{
	private static final long serialVersionUID = 6548993287130939757L;
	private final String groupname;

	public RequestPublicKeyEvent(String groupname)
	{
		this.groupname = groupname;
	}

	public String getGroupname()
	{
		return this.groupname;
	}
}

