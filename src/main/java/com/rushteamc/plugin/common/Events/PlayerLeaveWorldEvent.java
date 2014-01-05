package com.rushteamc.plugin.common.Events;

import com.rushteamc.plugin.common.Player;
import com.rushteamc.plugin.common.World;
import java.io.Serializable;

public class PlayerLeaveWorldEvent
	implements Serializable
{
	private static final long serialVersionUID = -3027475296078659462L;
	private final Player player;
	private final String world;

	public PlayerLeaveWorldEvent(Player player, World world)
	{
		this(player, world.getName());
	}

	public PlayerLeaveWorldEvent(Player player, String world)
	{
		this.player = player;
		this.world = world;
	}

	public Player getPlayer()
	{
		return this.player;
	}

	public World getWorld()
	{
		return World.getWorld(this.world);
	}

	public String getWorldName()
	{
		return this.world;
	}
}

