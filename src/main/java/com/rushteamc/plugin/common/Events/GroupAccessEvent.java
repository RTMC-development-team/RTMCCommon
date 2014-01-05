package com.rushteamc.plugin.common.Events;

import java.io.Serializable;
import java.security.Key;

public class GroupAccessEvent
	implements Serializable
{
	private static final long serialVersionUID = 8908429923071285162L;
	private final byte[] data;
	private final Key key;

	public GroupAccessEvent(byte[] data, Key key)
	{
		this.data = data;
		this.key = key;
	}

	public byte[] getData()
	{
		return this.data;
	}

	public Key getKey()
	{
		return this.key;
	}
}

