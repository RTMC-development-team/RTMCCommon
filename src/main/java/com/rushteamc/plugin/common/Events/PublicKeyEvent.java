package com.rushteamc.plugin.common.Events;

import java.io.Serializable;
import java.security.Key;

public class PublicKeyEvent
	implements Serializable
{
	private static final long serialVersionUID = 1412925130692465599L;
	private final String groupname;
	private final Key key;

	public PublicKeyEvent(String groupname, Key key)
	{
		this.groupname = groupname;
		this.key = key;
	}

	public Key getKey()
	{
		return this.key;
	}

	public String getGroupname()
	{
		return this.groupname;
	}
}

