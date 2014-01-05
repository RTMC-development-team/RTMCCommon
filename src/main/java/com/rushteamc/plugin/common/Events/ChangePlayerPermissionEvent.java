package com.rushteamc.plugin.common.Events;

import com.rushteamc.plugin.common.Permissions.Permission;
import com.rushteamc.plugin.common.Player;
import java.io.Serializable;

public class ChangePlayerPermissionEvent
	implements Serializable
{
	private static final long serialVersionUID = 9219392996212246214L;
	private final Player player;
	private final Permission permission;

	public ChangePlayerPermissionEvent(Player player, Permission permission)
	{
		this.player = player;
		this.permission = permission;
	}

	public Player getPlayer()
	{
		return this.player;
	}

	public Permission getPermission()
	{
		return this.permission;
	}
}

