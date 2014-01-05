package com.rushteamc.plugin.common.Events;

import com.rushteamc.plugin.common.FormattedString.FormattedString;
import com.rushteamc.plugin.common.Group;
import com.rushteamc.plugin.common.Player;
import com.rushteamc.plugin.common.World;
import java.io.Serializable;

public class GroupChatEvent
	implements Serializable
{
	private static final long serialVersionUID = -4681221131623990726L;
	private final Player player;
	private final String worldName;
	private final Group group;
	private final FormattedString message;

	public GroupChatEvent(Player player, World world, Group group, FormattedString message)
	{
		this(player, world.getName(), group, message);
	}

	public GroupChatEvent(Player player, String worldName, Group group, FormattedString message)
	{
		this.player = player;
		this.worldName = worldName;
		this.group = group;
		this.message = message;
	}

	public Player getPlayer()
	{
		return this.player;
	}

	public Group getGroup()
	{
		return this.group;
	}

	public FormattedString getMessage()
	{
		return this.message;
	}

	public World getWorld()
	{
		return World.getWorld(this.worldName);
	}

	public String getWorldName()
	{
		return this.worldName;
	}
}

