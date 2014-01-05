package com.rushteamc.plugin.common.Events;

import com.rushteamc.lib.SharedEventBus.Subscribe;
import com.rushteamc.plugin.common.Assembler;
import com.rushteamc.plugin.common.Group;
import com.rushteamc.plugin.common.Player;
import com.rushteamc.plugin.common.World;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.Set;

public class CommonSharedEventHandler
{
	private final Assembler assembler;

	public CommonSharedEventHandler(Assembler assembler)
	{
		this.assembler = assembler;
	}

	@Subscribe(priority=Subscribe.Priority.HIGHEST, instanceOf=true)
	public void onSetOnlinePlayersEvent(String eventGroup, SetOnlinePlayersEvent event)
	{
		if (!this.assembler.getEventBusGroupName().equals(eventGroup)) {
			return;
		}
		System.out.println("Update online players for world " + event.getWorldname());

		World world = event.getWorld();
		Set<Player> onlinePlayers = event.getOnlinePlayers();
		world.updateOnlinePlayers(onlinePlayers);
	}

	@Subscribe(priority=Subscribe.Priority.HIGHEST, instanceOf=true)
	public void onUpdatePlayerPermission(String eventGroup, ChangePlayerPermissionEvent event)
	{
		if (!this.assembler.getEventBusGroupName().equals(eventGroup)) {
			return;
		}
		event.getPlayer().getUserPermissions().setPermission(event.getPermission());
	}

	@Subscribe(priority=Subscribe.Priority.HIGHEST, instanceOf=true)
	public void onChangeGroupPermission(String eventGroup, ChangeGroupPermissionEvent event)
	{
		if (!this.assembler.getEventBusGroupName().equals(eventGroup)) {
			return;
		}
		event.getGroup().getGroupPermissions().setPermission(event.getPermission());
	}

	@Subscribe(priority=Subscribe.Priority.HIGHEST, instanceOf=true)
	public void onCangeGroupMember(String eventGroup, ChangeGroupMemberEvent event)
	{
		if (!this.assembler.getEventBusGroupName().equals(eventGroup)) {
			return;
		}
		if (event.isMember())
			event.getPlayer().addGroup(event.getGroup(), null);
		else
			event.getPlayer().removeGroup(event.getGroup(), null);
	}

	@Subscribe(priority=Subscribe.Priority.HIGHEST, instanceOf=true)
	public void onChangeGroupParent(String eventGroup, ChangeGroupParentEvent event)
	{
		if (!this.assembler.getEventBusGroupName().equals(eventGroup)) {
			return;
		}
		if (event.isMember())
			event.getGroup().addParent(event.getParent(), null);
		else
			event.getGroup().removeParent(event.getParent(), null);
	}

	@Subscribe(priority=Subscribe.Priority.HIGHEST, instanceOf=true)
	public void onChangePlayerDetailsEvent(String eventGroup, ChangePlayerDetailsEvent event)
	{
		if (!this.assembler.getEventBusGroupName().equals(eventGroup)) {
			return;
		}
		Player player = event.getPlayer();
		Player.PlayerDetails playerDetails = event.getPlayerDetails();

		player.setPasswordHash(playerDetails.getPasswordHash(), null);
		player.setPrefix(playerDetails.getPrefix(), null);
		player.setSuffix(playerDetails.getSuffix(), null);
	}

	@Subscribe(priority=Subscribe.Priority.HIGHEST, instanceOf=true)
	public void onChangeGroupDetailsEvent(String eventGroup, ChangeGroupDetailsEvent event)
	{
		if (!this.assembler.getEventBusGroupName().equals(eventGroup)) {
			return;
		}
		Group group = event.getGroup();
		Group.GroupDetails groupDetails = event.getGroupDetails();

		group.setPrefix(groupDetails.getPrefix(), null);
		group.setSuffix(groupDetails.getSuffix(), null);
	}

	@Subscribe(priority=Subscribe.Priority.HIGHEST, instanceOf=true)
	public void onLoggerRenameEvent(LoggerRenameEvent event)
	{
		if (event.getLoggerName().equals(this.assembler.getLoggerHandler().getLogEventGroupName()))
			this.assembler.getSharedEventBus().postEvent(new LoggerRenameRejectedEvent(event.getLoggerName()));
	}

	@Subscribe(priority=Subscribe.Priority.HIGHEST, instanceOf=true)
	public void onLoggerRenameRejectedEvent(LoggerRenameRejectedEvent event)
	{
		if (event.getLoggerName().equals(this.assembler.getLoggerHandler().getLogEventGroupName()))
		{
			int num;
			try {
				num = Integer.parseInt(event.getLoggerName().replaceAll("^[\\d\\D]*\\D+(\\d+)$", "$1"));
			}
			catch (NumberFormatException e)
			{
				num = new Random().nextInt();
			}
			this.assembler.getLoggerHandler().renameLogger(num + 1);
		}
	}

	@Subscribe(priority=Subscribe.Priority.HIGHEST, instanceOf=true)
	public void onGroupAccessEvent(GroupAccessEvent event)
	{
		try {
			this.assembler.getAuthenticator().handleGroupAccess(event.getData(), event.getKey());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
}

