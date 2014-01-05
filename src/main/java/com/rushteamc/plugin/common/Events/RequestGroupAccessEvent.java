package com.rushteamc.plugin.common.Events;

import java.io.Serializable;
import java.security.Key;

public class RequestGroupAccessEvent
	implements Serializable
{
	private static final long serialVersionUID = 4479359729727812542L;
	private final byte[] data;
	private final Key key;

	public RequestGroupAccessEvent(byte[] data, Key key)
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

