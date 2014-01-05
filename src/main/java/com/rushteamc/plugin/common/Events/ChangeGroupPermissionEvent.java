package com.rushteamc.plugin.common.Events;

import com.rushteamc.plugin.common.Group;
import com.rushteamc.plugin.common.Permissions.Permission;
import java.io.Serializable;

public class ChangeGroupPermissionEvent
	implements Serializable
{
	private static final long serialVersionUID = 5252020340487329221L;
	private final Group group;
	private final Permission permission;

	public ChangeGroupPermissionEvent(Group group, Permission permission)
	{
		this.group = group;
		this.permission = permission;
	}

	public Permission getPermission()
	{
		return this.permission;
	}

	public Group getGroup()
	{
		return this.group;
	}
}

