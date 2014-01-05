package com.rushteamc.plugin.common;
import com.rushteamc.plugin.common.Authentication.Authenticator;
import com.rushteamc.plugin.common.Database.DatabaseManager;
import com.rushteamc.plugin.common.Database.DatabaseRunnable;
import com.rushteamc.plugin.common.Events.ChangeGroupMemberEvent;
import com.rushteamc.plugin.common.Events.ChangePlayerDetailsEvent;
import com.rushteamc.plugin.common.Events.ChangePlayerPermissionEvent;
import com.rushteamc.plugin.common.FormattedString.FormattedString;
import com.rushteamc.plugin.common.Permissions.Permission;
import com.rushteamc.plugin.common.Permissions.PermissionSet;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Player
	implements Serializable
{
	private static final long serialVersionUID = -4241832978722954841L;
	private static final Map<Long, Player> playerList = new HashMap<Long, Player>();
	private static final Map<String, Long> playerIDs = new HashMap<String, Long>();
	private static PreparedStatement playerIDSearchStatement;
	private static PreparedStatement playerNameSearchStatement;
	private static PreparedStatement playerNameInsertStatement;
	private static PreparedStatement playerPermissionsSearchStatement;
	private static PreparedStatement playerPermissionsInsertStatement;
	private static PreparedStatement playerPermissionsDeleteStatement;
	private static PreparedStatement playerGroupSearchStatement;
	private static PreparedStatement playerGroupInsertStatement;
	private static PreparedStatement playerGroupDeleteStatement;
	private static PreparedStatement playerDefaultGroupStatement;
	private static PreparedStatement savePlayerDetailsStatement;
	private final PlayerDetails playerDetails;
	private final Set<World> worlds;
	private final PermissionSet permissions;
	private final Set<Group> groups;

	public static Player getPlayer(String name)
	{
		return getPlayer(getID(name));
	}

	public static Player getPlayer(long ID)
	{
		Player player = (Player)playerList.get(Long.valueOf(ID));
		if (player == null)
		{
			PlayerDetails playerDetails = loadPlayerDetails(ID);
			PermissionSet permissions = loadPermissions(playerDetails.ID);
			Set<Group> groups = loadGroups(playerDetails.ID);
			Set<World> worlds = new HashSet<World>(); // TODO: Look at this!
			player = new Player(playerDetails, worlds, permissions, groups);
			playerList.put(Long.valueOf(ID), player);
		}
		return player;
	}

	public static Player getExsistingPlayer(String name)
	{
		Long ID = getExsistingID(name);

		if (ID == null) {
			return null;
		}
		return getPlayer(ID.longValue());
	}

	public static Player getExsistingPlayer(long ID)
	{
		Player player = (Player)playerList.get(Long.valueOf(ID));
		if (player == null)
		{
			PlayerDetails playerDetails = loadPlayerDetails(ID);

			if (playerDetails == null) {
				return null;
			}
			PermissionSet permissions = loadPermissions(playerDetails.ID);
			Set<Group> groups = loadGroups(playerDetails.ID);
			Set<World> worlds = new HashSet<World>(); // TODO: Look at this!
			player = new Player(playerDetails, worlds, permissions, groups);
			playerList.put(Long.valueOf(ID), player);
		}
		return player;
	}

	public Player(long ID)
	{
		this(getPlayer(ID));
	}

	public Player(String name)
	{
		this(getPlayer(name));
	}

	public Player(Player player)
	{
		this.groups = player.groups;
		this.permissions = player.permissions;
		this.playerDetails = player.playerDetails;
		this.worlds = player.worlds;
	}

	public Player(PlayerDetails playerDetails, Set<World> worlds, PermissionSet permissions, Set<Group> groups)
	{
		if (playerDetails == null)
			throw new IllegalArgumentException("Argument playerDetails cannot be null");
		if (worlds == null)
			throw new IllegalArgumentException("Argument worlds cannot be null");
		if (permissions == null)
			throw new IllegalArgumentException("Argument permissions cannot be null");
		if (groups == null) {
			throw new IllegalArgumentException("Argument groups cannot be null");
		}
		this.playerDetails = playerDetails;
		this.worlds = worlds;
		this.permissions = permissions;
		this.groups = groups;
	}

	private static Long getExsistingID(String playerName)
	{
		final String name = playerName.toLowerCase();
		Long ID = (Long)playerIDs.get(name);

		if (ID == null)
		{
			if (playerNameSearchStatement == null) {
				try
				{
					playerNameSearchStatement = DatabaseManager.createPreparedStatement("SELECT ID FROM " + DatabaseManager.getTablePrefix() + "Users WHERE Username LIKE ?");
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

			ID = DatabaseManager.addDatabaseRunnable(new DatabaseRunnable<Long>(true)
			{
				public Long run()
				{
					try {
						Player.playerNameSearchStatement.setString(1, name);
						Player.playerNameSearchStatement.execute();

						ResultSet resultSet = Player.playerNameSearchStatement.getResultSet();
						if (resultSet.first())
						{
							return Long.valueOf(resultSet.getLong(1));
						}
					}
					catch (SQLException e) {
						e.printStackTrace();
					}
					return null;
				}
			});
			if (ID != null) {
				playerIDs.put(name, ID);
			}
		}
		return ID;
	}

	private static long getID(final String playerName)
	{
		Long ID = getExsistingID(playerName);

		if (ID == null)
		{
			if (playerNameInsertStatement == null) {
				try
				{
					playerNameInsertStatement = DatabaseManager.createPreparedInsertStatement("INSERT INTO " + DatabaseManager.getTablePrefix() + "Users (Username) VALUES (?);");
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

			if (playerDefaultGroupStatement == null) {
				try
				{
					playerDefaultGroupStatement = DatabaseManager.createPreparedStatement("INSERT INTO UserGroups (UserID, GroupID) SELECT ? AS `UserID`, ID AS GroupID FROM Groups WHERE `Default`=1;");
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

			ID = DatabaseManager.addDatabaseRunnable(new DatabaseRunnable<Long>(true)
			{
				public Long run()
				{
					try {
						Player.playerNameInsertStatement.setString(1, playerName);
						Player.playerNameInsertStatement.executeUpdate();
						ResultSet result = Player.playerNameInsertStatement.getGeneratedKeys();
						if (result.first())
						{
							long ID = result.getLong(1);

							Player.playerDefaultGroupStatement.setLong(1, ID);
							Player.playerDefaultGroupStatement.execute();

							return Long.valueOf(ID);
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
					return null;
				}
			});
		}

		return ID.longValue();
	}

	private void savePlayerDetails()
	{
		if (savePlayerDetailsStatement == null) {
			try
			{
				savePlayerDetailsStatement = DatabaseManager.createPreparedInsertStatement("UPDATE " + DatabaseManager.getTablePrefix() + "Users SET Password=?, Prefix=?, Suffix=? WHERE ID=?;");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		DatabaseManager.addDatabaseRunnable(new DatabaseRunnable<Void>()
		{
			public Void run()
			{
				try {
					Player.savePlayerDetailsStatement.setString(1, Authenticator.hashToString(Player.this.playerDetails.getPasswordHash()));
					Player.savePlayerDetailsStatement.setString(2, Player.this.playerDetails.getPrefix() == null ? null : Player.this.playerDetails.getPrefix().toString("save"));
					Player.savePlayerDetailsStatement.setString(3, Player.this.playerDetails.getSuffix() == null ? null : Player.this.playerDetails.getSuffix().toString("save"));

					Player.savePlayerDetailsStatement.setLong(4, Player.this.playerDetails.getID());

					Player.savePlayerDetailsStatement.execute();
				} catch (SQLException e) {
					e.printStackTrace();
				}

				return null;
			}
		});
	}

	private void updatePlayerDetails(Assembler setup)
	{
		if (setup == null) {
			return;
		}
		savePlayerDetails();
		setup.getSharedEventBus().postGroupEvent(setup.getEventBusGroupName(), new ChangePlayerDetailsEvent(this, this.playerDetails));
	}

	public long getID()
	{
		return this.playerDetails.getID();
	}

	public String getDisplayName()
	{
		return this.playerDetails.getName();
	}

	public FormattedString getPrefix()
	{
		if (this.playerDetails.prefix == null)
		{
			Set<Group> mainGroups = getMainGroup();
			for (Group group : mainGroups)
			{
				FormattedString prefix = group.getPrefix();
				if (prefix != null)
					return prefix;
			}
		}
		return this.playerDetails.getPrefix();
	}

	public void setPrefix(FormattedString perfix, Assembler setup)
	{
		this.playerDetails.setPrefix(perfix);
		updatePlayerDetails(setup);
	}

	public FormattedString getSuffix()
	{
		if (this.playerDetails.suffix == null)
		{
			Set<Group> mainGroups = getMainGroup();
			for (Group group : mainGroups)
			{
				FormattedString suffix = group.getSuffix();
				if (suffix != null)
					return suffix;
			}
		}
		return this.playerDetails.getSuffix();
	}

	public void setSuffix(FormattedString suffix, Assembler setup)
	{
		this.playerDetails.setSuffix(suffix);
		updatePlayerDetails(setup);
	}

	public Set<World> getWorlds()
	{
		return this.worlds;
	}

	public Group[] getGroups()
	{
		Group[] arr = new Group[this.groups.size()];
		return (Group[])this.groups.toArray(arr);
	}

	public Set<Group> getMainGroup()
	{
		Set<Group> result = new HashSet<Group>();

		result.addAll(this.groups);
		Iterator<Group> iterator = result.iterator();
		for (Iterator<Group> localIterator1 = this.groups.iterator(); localIterator1.hasNext();)
		{
			Group group = localIterator1.next();

			//iterator = result.iterator();
			//continue;

			Group chkGroup = iterator.next();

			if (group.inheritsFrom(chkGroup)) {
				iterator.remove();
			}
		}

		return result;
	}

	public PermissionSet getPermissions()
	{
		PermissionSet resultingPermissions = new PermissionSet();

		for (Group group : this.groups)
		{
			resultingPermissions.setPermissions(group.getPermissions());
		}

		resultingPermissions.setPermissions(this.permissions);

		return resultingPermissions;
	}

	public PermissionSet getUserPermissions()
	{
		return this.permissions;
	}

	private final void writeObject(ObjectOutputStream out) throws IOException
	{
		out.writeLong(this.playerDetails.ID);
	}

	private final void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		long ID = in.readLong();
		Player player = getPlayer(ID);
		try
		{
			Field field = getClass().getDeclaredField("groups");
			field.setAccessible(true);
			field.set(this, player.groups);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		try
		{
			Field field = getClass().getDeclaredField("permissions");
			field.setAccessible(true);
			field.set(this, player.permissions);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		try
		{
			Field field = getClass().getDeclaredField("playerDetails");
			field.setAccessible(true);
			field.set(this, player.playerDetails);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		try
		{
			Field field = getClass().getDeclaredField("worlds");
			field.setAccessible(true);
			field.set(this, player.worlds);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
	}

	private static PlayerDetails loadPlayerDetails(final long ID)
	{
		if (playerIDSearchStatement == null)
		{
			PreparedStatement statement = null;
			try {
				statement = DatabaseManager.createPreparedStatement("SELECT ID, Username, Password, Prefix, Suffix FROM " + DatabaseManager.getTablePrefix() + "Users WHERE ID=?");
			} catch (SQLException e) {
				e.printStackTrace();
			}
			playerIDSearchStatement = statement;
		}

		return DatabaseManager.addDatabaseRunnable(new DatabaseRunnable<PlayerDetails>(true)
		{
			public Player.PlayerDetails run()
			{
				try {
					Player.playerIDSearchStatement.setLong(1, ID);
				} catch (SQLException e) {
					e.printStackTrace();
				}
				try
				{
					Player.playerIDSearchStatement.execute();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				try
				{
					ResultSet resultSet = Player.playerIDSearchStatement.getResultSet();

					if (resultSet.first())
					{
						String buf = resultSet.getString(4);
						FormattedString prefix = null;
						if (buf != null)
							prefix = new FormattedString("save", buf);
						buf = resultSet.getString(5);
						FormattedString suffix = null;
						if (buf != null)
							suffix = new FormattedString("save", buf);
						byte[] hash;
						try {
							hash = Authenticator.stringToHash(resultSet.getString(3));
						}
						catch (IllegalArgumentException e)
						{
							e.printStackTrace();
							hash = new byte[0];
						}
						return new Player.PlayerDetails(resultSet.getInt(1), resultSet.getString(2), hash, prefix, suffix);
					}

					return null;
				}
				catch (SQLException|FormattedString.ParseErrorException e)
				{
					e.printStackTrace();
				}
				return null;
			}
		});
	}

	public static PermissionSet loadPermissions(final long playerID)
	{
		if (playerPermissionsSearchStatement == null)
		{
			PreparedStatement statement = null;
			try {
				statement = DatabaseManager.createPreparedStatement("SELECT * FROM " + DatabaseManager.getTablePrefix() + "UserPermissions WHERE UserID=?");
			} catch (SQLException e) {
				e.printStackTrace();
			}
			playerPermissionsSearchStatement = statement;
		}

		return DatabaseManager.addDatabaseRunnable(new DatabaseRunnable<PermissionSet>(true)
		{
			public PermissionSet run()
			{
				PermissionSet playerPermission = new PermissionSet();
				try
				{
					Player.playerPermissionsSearchStatement.setLong(1, playerID);
				} catch (SQLException e) {
					e.printStackTrace();
				}
				try
				{
					Player.playerPermissionsSearchStatement.execute();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				try
				{
					ResultSet resultSet = Player.playerPermissionsSearchStatement.getResultSet();

					if (resultSet.first())
						do
						{
							Permission permission = new Permission(resultSet.getString(2), Boolean.valueOf(resultSet.getBoolean(3)));
							playerPermission.setPermission(permission);
						}
						while (resultSet.next());
				}
				catch (SQLException e)
				{
					e.printStackTrace();
				}

				return playerPermission;
			}
		});
	}

	public void savePermissions(final Permission[] permissions)
	{
		if (playerPermissionsInsertStatement == null)
		{
			PreparedStatement statement = null;
			try {
				statement = DatabaseManager.createPreparedStatement("REPLACE INTO " + DatabaseManager.getTablePrefix() + "UserPermissions (UserID, Permission, Granded) VALUES (?, ?, ?)");
			} catch (SQLException e) {
				e.printStackTrace();
			}
			playerPermissionsInsertStatement = statement;
		}

		if (playerPermissionsDeleteStatement == null)
		{
			PreparedStatement statement = null;
			try {
				statement = DatabaseManager.createPreparedStatement("DELETE FROM " + DatabaseManager.getTablePrefix() + "UserPermissions WHERE UserID=? AND Permission=?;");
			} catch (SQLException e) {
				e.printStackTrace();
			}
			playerPermissionsDeleteStatement = statement;
		}

		DatabaseManager.addDatabaseRunnable(new DatabaseRunnable<Void>()
		{
			public Void run()
			{
				for (Permission permission : permissions)
				{
					if (permission.isGranded() == null) {
						try
						{
							playerPermissionsDeleteStatement.setLong(1, Player.this.playerDetails.getID());
							playerPermissionsDeleteStatement.setString(2, permission.toString(false));

							playerPermissionsDeleteStatement.execute();
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}
					else {
						try
						{
							Player.playerPermissionsInsertStatement.setLong(1, Player.this.playerDetails.getID());
							Player.playerPermissionsInsertStatement.setString(2, permission.toString(false));
							Player.playerPermissionsInsertStatement.setBoolean(3, permission.isGranded().booleanValue());

							Player.playerPermissionsInsertStatement.execute();
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}
				}
				return null;
			}
		});
	}

	private static Set<Group> loadGroups(final long ID)
	{
		if (playerGroupSearchStatement == null)
		{
			if (playerGroupSearchStatement == null) {
				try
				{
					playerGroupSearchStatement = DatabaseManager.createPreparedStatement("SELECT GroupID FROM " + DatabaseManager.getTablePrefix() + "UserGroups WHERE UserID=?");
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

		Set<Group> groups = new HashSet<Group>();

		Set<Long> groupIDs = DatabaseManager.addDatabaseRunnable(new DatabaseRunnable<Set<Long>>(true)
		{
			public Set<Long> run()
			{
				Set<Long> IDlist = new HashSet<Long>();
				try
				{
					Player.playerGroupSearchStatement.setLong(1, ID);
					Player.playerGroupSearchStatement.execute();
					ResultSet results = Player.playerGroupSearchStatement.getResultSet();
					if (results.first())
					{
						do
						{
							IDlist.add(Long.valueOf(results.getLong(1)));
						}while (results.next());
					}
					results.close();
				}
				catch (SQLException e) {
					e.printStackTrace();
				}

				return IDlist;
			}
		});
		for (Iterator<Long> localIterator = groupIDs.iterator(); localIterator.hasNext(); ) { long groupID = ((Long)localIterator.next()).longValue();
			groups.add(new Group(groupID));
		}
		return groups;
	}

	public void addPermission(String permission, Assembler setup)
	{
		addPermission(new Permission(permission), setup);
	}

	public void addPermission(Permission permission, Assembler setup)
	{
		permission.setGranded(Boolean.valueOf(true));
		setPermission(permission, setup);
	}

	public void removePermission(String permission, Assembler setup)
	{
		removePermission(new Permission(permission), setup);
	}

	public void removePermission(Permission permission, Assembler setup)
	{
		permission.setGranded(Boolean.valueOf(false));
		setPermission(permission, setup);
	}

	public void setPermission(String permission, Assembler setup)
	{
		setPermission(new Permission(permission), setup);
	}

	public void setPermission(Permission permission, Assembler setup)
	{
		setup.getSharedEventBus().postGroupEvent(setup.getEventBusGroupName(), new ChangePlayerPermissionEvent(this, permission));
		savePermissions(this.permissions.setPermission(permission));
	}

	public void unsetPermission(String permission, Assembler setup)
	{
		unsetPermission(new Permission(permission), setup);
	}

	public void unsetPermission(Permission permission, Assembler setup)
	{
		permission.setGranded(null);
		setPermission(permission, setup);
	}

	public Boolean hasPermission(String permission)
	{
		return hasPermission(new Permission(permission));
	}

	public Boolean hasPermission(Permission permission)
	{
		Boolean has = this.permissions.hasPermission(permission);

		if (has != null) {
			return has;
		}
		for (Group group : this.groups)
		{
			has = group.hasPermission(permission);
			if (has != null) {
				return has;
			}
		}
		return has;
	}

	public void removeGroup(Group parent, Assembler setup)
	{
		for (final Group parentGroup : this.groups)
		{
			if (parentGroup.equals(parent))
			{
				this.groups.remove(parentGroup);

				if (setup == null) {
					return;
				}
				setup.publisSecureEvent(new ChangeGroupMemberEvent(this, parent, false));

				if (playerGroupDeleteStatement == null)
				{
					if (playerGroupDeleteStatement == null) {
						try
						{
							playerGroupDeleteStatement = DatabaseManager.createPreparedStatement("DELETE FROM " + DatabaseManager.getTablePrefix() + "UserGroups WHERE UserID=? AND GroupID=?");
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}
				}

				DatabaseManager.addDatabaseRunnable(new DatabaseRunnable<Void>(true)
				{
					public Void run()
					{
						try {
							Player.playerGroupDeleteStatement.setLong(1, Player.this.playerDetails.getID());
							Player.playerGroupDeleteStatement.setLong(2, parentGroup.getID());
							Player.playerGroupDeleteStatement.execute();
						} catch (SQLException e) {
							e.printStackTrace();
						}
						return null;
					}
				});
				return;
			}
		}
	}

	public void addGroup(final Group parent, Assembler setup)
	{
		for (Group group : this.groups)
		{
			if (group.equals(parent))
				return;
		}
		this.groups.add(parent);

		if (setup == null) {
			return;
		}
		setup.publisSecureEvent(new ChangeGroupMemberEvent(this, parent, true));

		if (playerGroupInsertStatement == null)
		{
			if (playerGroupInsertStatement == null) {
				try
				{
					playerGroupInsertStatement = DatabaseManager.createPreparedInsertStatement("REPLACE INTO " + DatabaseManager.getTablePrefix() + "UserGroups (UserID, GroupID) VALUES (?, ?)");
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

		DatabaseManager.addDatabaseRunnable(new DatabaseRunnable<Void>()
		{
			public Void run()
			{
				try {
					Player.playerGroupInsertStatement.setLong(1, Player.this.playerDetails.getID());
					Player.playerGroupInsertStatement.setLong(2, parent.getID());
					Player.playerGroupInsertStatement.execute();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				return null;
			}
		});
	}

	public boolean inGroup(Group group)
	{
		for (Group grp : this.groups)
		{
			if (grp.equals(group))
				return true;
			if (grp.inheritsFrom(group))
				return true;
		}
		return false;
	}

	public static void deinint()
	{
		playerList.clear();
		playerIDs.clear();
	}

	public byte[] getPasswordHash()
	{
		return this.playerDetails.getPasswordHash();
	}

	public void setPasswordHash(byte[] hash, Assembler setup)
	{
		if (hash == null) {
			return;
		}
		if (compareByteArray(hash, this.playerDetails.getPasswordHash())) {
			return;
		}
		this.playerDetails.setPasswordHash(hash);
		updatePlayerDetails(setup);
	}

	public static boolean compareByteArray(byte[] arr1, byte[] arr2)
	{
		if (arr1.length != arr2.length) {
			return false;
		}
		for (int i = 0; i < arr2.length; i++) {
			if (arr1[i] != arr2[i])
				return false;
		}
		return true;
	}

	public boolean equals(Object obj)
	{
		if (!(obj instanceof Player)) {
			return false;
		}
		Player other = (Player)obj;

		return other.getID() == this.playerDetails.ID;
	}

	public int hashCode()
	{
		return Long.valueOf(this.playerDetails.ID).hashCode();
	}

	public static class PlayerDetails
		implements Serializable
	{
		private static final long serialVersionUID = 7275493656390896602L;
		private final long ID;
		private final String name;
		private byte[] passwordHash;
		private FormattedString prefix;
		private FormattedString suffix;

		public PlayerDetails(long ID, String name, String passwordHash, FormattedString prefix, FormattedString suffix)
		{
			this(ID, name, passwordHash.getBytes(), prefix, suffix);
		}

		public PlayerDetails(long ID, String name, byte[] passwordHash, FormattedString prefix, FormattedString suffix)
		{
			this.ID = ID;
			this.name = name;
			this.passwordHash = passwordHash;
			this.prefix = prefix;
			this.suffix = suffix;
		}

		public long getID()
		{
			return this.ID;
		}

		public String getName()
		{
			return this.name;
		}

		public FormattedString getPrefix()
		{
			return this.prefix;
		}

		public void setPrefix(FormattedString prefix)
		{
			this.prefix = prefix;
		}

		public FormattedString getSuffix()
		{
			return this.suffix;
		}

		public void setSuffix(FormattedString suffix)
		{
			this.suffix = suffix;
		}

		public byte[] getPasswordHash()
		{
			return this.passwordHash;
		}

		public void setPasswordHash(byte[] passwordHash)
		{
			this.passwordHash = passwordHash;
		}
	}
}

