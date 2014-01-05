package com.rushteamc.plugin.common.Events;

import com.rushteamc.plugin.common.Player;
import com.rushteamc.plugin.common.World;
import java.io.Serializable;
import java.util.Set;

public class SetOnlinePlayersEvent
	implements Serializable
{
	private static final long serialVersionUID = 223139207703700357L;
	private final String worldname;
	private final Set<Player> onlinePlayers;

	public SetOnlinePlayersEvent(World world, Set<Player> onlinePlayers)
	{
		this(world.getName(), onlinePlayers);
	}

	public SetOnlinePlayersEvent(String worldname, Set<Player> onlinePlayers)
	{
		this.worldname = worldname;
		this.onlinePlayers = onlinePlayers;
	}

	public String getWorldname()
	{
		return this.worldname;
	}

	public World getWorld()
	{
		return World.getWorld(this.worldname);
	}

	public Set<Player> getOnlinePlayers()
	{
		return this.onlinePlayers;
	}
}

