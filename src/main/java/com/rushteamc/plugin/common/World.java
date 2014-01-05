package com.rushteamc.plugin.common;

import java.util.LinkedHashSet;
import java.util.Set;

public class World
{
	private static final Set<World> worlds = new LinkedHashSet<World>();

	private final Set<Player> onlinePlayers = new LinkedHashSet<Player>();
	private final String name;

	public World(String name)
	{
		this.name = name;
	}

	public void playerJoin(Player player)
	{
		this.onlinePlayers.add(player);
	}

	public void playerLeave(Player player)
	{
		this.onlinePlayers.remove(player);
	}

	public String getName()
	{
		return this.name;
	}

	public Set<Player> getOnlinePlayers()
	{
		return this.onlinePlayers;
	}

	public boolean match(String name)
	{
		return this.name.equalsIgnoreCase(name);
	}

	public static World getWorld(String name)
	{
		for (World world : worlds)
		{
			if (world.match(name)) {
				return world;
			}
		}
		World world = new World(name);
		worlds.add(world);
		return world;
	}

	public static World[] getWorlds()
	{
		World[] arr = new World[worlds.size()];
		return (World[])worlds.toArray(arr);
	}

	public void updateOnlinePlayers(Set<Player> onlinePlayers)
	{
		this.onlinePlayers.clear();
		for (Player player : onlinePlayers)
			this.onlinePlayers.add(player);
	}
}

