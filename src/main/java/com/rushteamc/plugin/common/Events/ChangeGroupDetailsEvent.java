package com.rushteamc.plugin.common.Events;

import com.rushteamc.plugin.common.Group;
import java.io.Serializable;

public class ChangeGroupDetailsEvent
	implements Serializable
{
	private static final long serialVersionUID = -8926966887088420765L;
	private final Group group;
	private final Group.GroupDetails groupDetails;

	public ChangeGroupDetailsEvent(Group group, Group.GroupDetails groupDetails)
	{
		this.group = group;
		this.groupDetails = groupDetails;
	}

	public Group getGroup()
	{
		return this.group;
	}

	public Group.GroupDetails getGroupDetails()
	{
		return this.groupDetails;
	}
}

