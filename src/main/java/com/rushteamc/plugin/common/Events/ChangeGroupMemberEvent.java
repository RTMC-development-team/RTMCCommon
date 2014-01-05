package com.rushteamc.plugin.common.Events;

import com.rushteamc.plugin.common.Group;
import com.rushteamc.plugin.common.Player;
import java.io.Serializable;

public class ChangeGroupMemberEvent
	implements Serializable
{
	private static final long serialVersionUID = 6302002626264071823L;
	private final Player player;
	private final Group group;
	private final boolean member;

	public ChangeGroupMemberEvent(Player player, Group group, boolean member)
	{
		this.player = player;
		this.group = group;
		this.member = member;
	}

	public boolean isMember()
	{
		return this.member;
	}

	public Group getGroup()
	{
		return this.group;
	}

	public Player getPlayer()
	{
		return this.player;
	}
}

