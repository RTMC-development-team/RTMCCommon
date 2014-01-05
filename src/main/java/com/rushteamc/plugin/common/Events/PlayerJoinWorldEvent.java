package com.rushteamc.plugin.common.Events;

import com.rushteamc.plugin.common.Player;
import com.rushteamc.plugin.common.World;
import java.io.Serializable;

public class PlayerJoinWorldEvent
	implements Serializable
{
	private static final long serialVersionUID = -4694611645358054690L;
	private final Player player;
	private final String world;

	public PlayerJoinWorldEvent(Player player, World world)
	{
		this(player, world.getName());
	}

	public PlayerJoinWorldEvent(Player player, String world)
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

