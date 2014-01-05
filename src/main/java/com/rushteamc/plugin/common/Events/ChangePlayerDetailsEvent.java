package com.rushteamc.plugin.common.Events;

import com.rushteamc.plugin.common.Player;
import java.io.Serializable;

public class ChangePlayerDetailsEvent
	implements Serializable
{
	private static final long serialVersionUID = -1757714680076856822L;
	private final Player player;
	private final Player.PlayerDetails playerDetails;

	public ChangePlayerDetailsEvent(Player player, Player.PlayerDetails playerDetails)
	{
		this.player = player;
		this.playerDetails = playerDetails;
	}

	public Player getPlayer()
	{
		return this.player;
	}

	public Player.PlayerDetails getPlayerDetails()
	{
		return this.playerDetails;
	}
}

