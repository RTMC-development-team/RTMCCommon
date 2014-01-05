package com.rushteamc.plugin.common.Events;

import com.rushteamc.plugin.common.FormattedString.FormattedString;
import com.rushteamc.plugin.common.Player;
import com.rushteamc.plugin.common.World;
import java.io.Serializable;

public class ChatEvent
	implements Serializable
{
	private static final long serialVersionUID = 8039760789766755244L;
	private final Player player;
	private final String world;
	private final FormattedString message;

	public ChatEvent(Player player, World world, FormattedString message)
	{
		this(player, world.getName(), message);
	}

	public ChatEvent(Player player, String world, FormattedString message)
	{
		this.player = player;
		this.world = world;
		this.message = message;
	}

	public FormattedString getMessage()
	{
		return this.message;
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

