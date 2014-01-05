package common;

import com.rushteamc.lib.SharedEventBus.SharedEventBus;
import com.rushteamc.plugin.common.Assembler;
import com.rushteamc.plugin.common.Events.PlayerJoinWorldEvent;
import com.rushteamc.plugin.common.FormattedString.FormattedString;
import com.rushteamc.plugin.common.FormattedString.FormattedString.ParseErrorException;
import com.rushteamc.plugin.common.Group;
import com.rushteamc.plugin.common.Player;
import com.rushteamc.plugin.common.World;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;

public class DatabaseTest
{
	@Test
	public void databaseTest()
	{
		try
		{
			FormattedString str = new FormattedString(null, "some test str");
			System.out.println(str);
			str.replace("test", new FormattedString(null, "..."));
			System.out.println(str);
		} catch (FormattedString.ParseErrorException e1) {
			e1.printStackTrace();
		}

		Set hosts = new HashSet();
		hosts.add("localhost:8081");
		Assembler setup = new Assembler(hosts, "localhost", "RTMCCommon", "root", "2112", "group", "pass", null, null, null);

		Player playerSTS = new Player("STS");
		setup.getSharedEventBus().postEvent(new PlayerJoinWorldEvent(playerSTS, World.getWorld("testworld")));

		Player player = new Player("STS");
		System.out.println("Player ID: " + player.getID());

		player.addPermission("plugin.test", setup);
		player.addPermission("plugin.tester", setup);
		player.unsetPermission("plugin.test", setup);

		Player player2 = new Player("STSc");
		System.out.println("Player ID: " + player2.getID());

		System.out.println("STS " + player.getPermissions());
		System.out.println("STSc " + player2.getPermissions());

		System.out.println("Get group Admins...");
		Group group1 = new Group("Admins");
		System.out.println("Get group Users...");
		Group group2 = new Group("Users");

		group2.addPermission("plugin.use", setup);
		group1.addPermission("plugin.administrate", setup);

		group1.addParent(group2, setup);

		System.out.println("Admins group " + group1.getPermissions());

		System.out.println("STS " + player.getPermissions());
		System.out.println("STSc " + player2.getPermissions());
		try
		{
			Thread.sleep(500L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}

