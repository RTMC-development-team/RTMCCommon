package com.rushteamc.plugin.common.Events;

import com.rushteamc.plugin.common.Group;
import java.io.Serializable;

public class ChangeGroupParentEvent
	implements Serializable
{
	private static final long serialVersionUID = 992197583104286728L;
	private final Group group;
	private final Group parent;
	private final boolean member;

	public ChangeGroupParentEvent(Group group, Group parent, boolean member)
	{
		this.group = group;
		this.parent = parent;
		this.member = member;
	}

	public boolean isMember()
	{
		return this.member;
	}

	public Group getParent()
	{
		return this.parent;
	}

	public Group getGroup()
	{
		return this.group;
	}
}

