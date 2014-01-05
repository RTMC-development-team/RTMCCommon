package com.rushteamc.plugin.common;

import com.rushteamc.plugin.common.Database.DatabaseManager;
import com.rushteamc.plugin.common.Database.DatabaseRunnable;
import com.rushteamc.plugin.common.Events.ChangeGroupDetailsEvent;
import com.rushteamc.plugin.common.Events.ChangeGroupParentEvent;
import com.rushteamc.plugin.common.Events.ChangeGroupPermissionEvent;
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
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Group
	implements Serializable
{
	private static final long serialVersionUID = 2262732353112055329L;
	private static PreparedStatement loadIDSearchStatement;
	private static PreparedStatement loadIDInsertStatement;
	private static PreparedStatement loadNameSearchStatement;
	private static PreparedStatement loadPermissionsSearchStatement;
	private static PreparedStatement savePermissionsInsertStatement;
	private static PreparedStatement savePermissionsDeleteStatement;
	private static PreparedStatement loadParentsSearchStatement;
	private static PreparedStatement saveParentsInsertStatement;
	private static PreparedStatement saveParentsDeleteStatement;
	private static PreparedStatement saveGroupDetailsStatement;
	private static final Map<Long, Group> groupList = new HashMap<Long, Group>();
	private static final Map<String, Long> groupIDs = new HashMap<String, Long>();
	private final GroupDetails groupDetails;
	private final PermissionSet permissions;
	private final Set<Group> parentGroups;

	public static Group getGroup(String name)
	{
		return getGroup(getID(name).longValue());
	}

	public static Group getGroup(long ID)
	{
		Group group = (Group)groupList.get(Long.valueOf(ID));
		if (group == null)
		{
			GroupDetails groupDetails = loadGroupDetails(ID);
			PermissionSet permissions = loadPermissions(groupDetails.ID);
			Set<Group> parentGroups = loadParents(groupDetails.ID);
			group = new Group(groupDetails, permissions, parentGroups);
			groupList.put(Long.valueOf(ID), group);
		}
		return group;
	}

	public Group(long ID)
	{
		this(getGroup(ID));
	}

	public Group(String name)
	{
		this(getGroup(name));
	}

	private Group(Group group)
	{
		this.groupDetails = group.groupDetails;
		this.parentGroups = group.parentGroups;
		this.permissions = group.permissions;
	}

	private Group(GroupDetails groupDetails, PermissionSet permissions, Set<Group> parentGroups)
	{
		if (groupDetails == null) {
			throw new IllegalArgumentException("groupDetails cannot be null.");
		}
		if (permissions == null) {
			throw new IllegalArgumentException("permissions cannot be null.");
		}
		if (parentGroups == null) {
			throw new IllegalArgumentException("parentGroups cannot be null.");
		}
		this.groupDetails = groupDetails;
		this.permissions = permissions;
		this.parentGroups = parentGroups;
	}

	private static Long getID(final String groupName)
	{
		final String name = groupName.toLowerCase();
		Long ID = (Long)groupIDs.get(name);
		if (ID == null)
		{
			if (loadIDSearchStatement == null)
			{
				if (loadIDSearchStatement == null) {
					try
					{
						loadIDSearchStatement = DatabaseManager.createPreparedStatement("SELECT ID FROM " + DatabaseManager.getTablePrefix() + "Groups WHERE Groupname LIKE ?");
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}

			if (loadIDInsertStatement == null)
			{
				if (loadIDInsertStatement == null) {
					try
					{
						loadIDInsertStatement = DatabaseManager.createPreparedInsertStatement("INSERT INTO " + DatabaseManager.getTablePrefix() + "Groups (Groupname) VALUES (?)");
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}

			ID = DatabaseManager.addDatabaseRunnable(new DatabaseRunnable<Long>(true)
			{
				public Long run()
				{
					try {
						Group.loadIDSearchStatement.setString(1, name);
						Group.loadIDSearchStatement.execute();
						ResultSet result = Group.loadIDSearchStatement.getResultSet();
						if (result.first())
						{
							return Long.valueOf(result.getLong(1));
						}

						result.close();

						Group.loadIDInsertStatement.setString(1, groupName);
						Group.loadIDInsertStatement.executeUpdate();
						result = Group.loadIDInsertStatement.getGeneratedKeys();
						if (result.first())
						{
							return Long.valueOf(result.getLong(1));
						}

						result.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}

					return null;
				}
			});
			groupIDs.put(name, ID);
		}
		return ID;
	}

	private static GroupDetails loadGroupDetails(final long ID)
	{
		if (loadNameSearchStatement == null)
		{
			if (loadNameSearchStatement == null) {
				try
				{
					loadNameSearchStatement = DatabaseManager.createPreparedStatement("SELECT ID, Groupname, Prefix, Suffix, `Default` FROM " + DatabaseManager.getTablePrefix() + "Groups WHERE ID=?");
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

		return DatabaseManager.addDatabaseRunnable(new DatabaseRunnable<GroupDetails>(true)
		{
			public Group.GroupDetails run()
			{
				try {
					Group.loadNameSearchStatement.setLong(1, ID);
					Group.loadNameSearchStatement.execute();
					ResultSet result = Group.loadNameSearchStatement.getResultSet();
					if (result.first())
					{
						String buf = result.getString(3);
						FormattedString prefix = null;
						if (buf != null)
							try {
								prefix = new FormattedString("save", buf);
							} catch (FormattedString.ParseErrorException e) {
								Logger.getLogger(getClass().getName()).log(Level.INFO, "Error while decode string from database: \"" + buf + "\"");
								e.printStackTrace();
							}
						buf = result.getString(4);
						FormattedString suffix = null;
						if (buf != null)
							try {
								suffix = new FormattedString("save", buf);
							} catch (FormattedString.ParseErrorException e) {
								Logger.getLogger(getClass().getName()).log(Level.INFO, "Error while decode string from database: \"" + buf + "\"");
								e.printStackTrace();
							}
						return new Group.GroupDetails(result.getLong(1), result.getString(2), prefix, suffix, result.getBoolean(5));
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}

				return null;
			}
		});
	}

	private static PermissionSet loadPermissions(final long ID)
	{
		if (loadPermissionsSearchStatement == null)
		{
			if (loadPermissionsSearchStatement == null) {
				try
				{
					loadPermissionsSearchStatement = DatabaseManager.createPreparedStatement("SELECT Permission, Granded FROM " + DatabaseManager.getTablePrefix() + "GroupPermissions WHERE GroupID=?");
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

		return DatabaseManager.addDatabaseRunnable(new DatabaseRunnable<PermissionSet>(true)
		{
			public PermissionSet run()
			{
				PermissionSet permissions = new PermissionSet();
				try {
					Group.loadPermissionsSearchStatement.setLong(1, ID);
					Group.loadPermissionsSearchStatement.execute();
					ResultSet result = Group.loadPermissionsSearchStatement.getResultSet();
					if (result.first())
					{
						do
						{
							Permission p = new Permission(result.getString(1), Boolean.valueOf(result.getBoolean(2)));
							permissions.setPermission(p);
						}
						while (result.next());
					}
					result.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				return permissions;
			}
		});
	}

	private static Set<Group> loadParents(final long ID)
	{
		if (loadParentsSearchStatement == null)
		{
			if (loadParentsSearchStatement == null) {
				try
				{
					loadParentsSearchStatement = DatabaseManager.createPreparedStatement("SELECT ParentGroupID FROM " + DatabaseManager.getTablePrefix() + "GroupParents WHERE GroupID=?");
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

		Set<Long> parentNames = DatabaseManager.addDatabaseRunnable(new DatabaseRunnable<Set<Long>>(true)
		{
			public Set<Long> run()
			{
				Set<Long> parentGroup = new HashSet<Long>();
				try {
					Group.loadParentsSearchStatement.setLong(1, ID);
					Group.loadParentsSearchStatement.execute();
					ResultSet result = Group.loadParentsSearchStatement.getResultSet();
					if (result.first())
					{
						do
						{
							parentGroup.add(Long.valueOf(result.getLong(1)));
						}
						while (result.next());
					}
					result.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				return parentGroup;
			}
		});
		Set<Group> parentGroup = new HashSet<Group>();

		for (Long parentID : parentNames) {
			parentGroup.add(new Group(parentID.longValue()));
		}
		return parentGroup;
	}

	public void savePermissions(Permission[] permissions)
	{
		if (savePermissionsInsertStatement == null)
		{
			if (savePermissionsInsertStatement == null) {
				try
				{
					savePermissionsInsertStatement = DatabaseManager.createPreparedInsertStatement("REPLACE INTO " + DatabaseManager.getTablePrefix() + "GroupPermissions (GroupID, Permission, Granded) VALUES (?, ?, ?)");
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

		if (savePermissionsDeleteStatement == null)
		{
			if (savePermissionsDeleteStatement == null) {
				try
				{
					savePermissionsDeleteStatement = DatabaseManager.createPreparedStatement("DELETE FROM " + DatabaseManager.getTablePrefix() + "GroupPermissions WHERE GroupID=? AND Permission LIKE ?");
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

		for (final Permission permission : permissions)
		{
			if (permission.isGranded() == null)
			{
				DatabaseManager.addDatabaseRunnable(new DatabaseRunnable<Void>()
				{
					public Void run()
					{
						try {
							Group.savePermissionsDeleteStatement.setLong(1, Group.this.groupDetails.getID());
							Group.savePermissionsDeleteStatement.setString(2, permission.toString(false));
							Group.savePermissionsDeleteStatement.execute();
						} catch (SQLException e) {
							e.printStackTrace();
						}
						return null;
					}
				});
			}
			else
			{
				DatabaseManager.addDatabaseRunnable(new DatabaseRunnable<Void>()
				{
					public Void run()
					{
						try {
							Group.savePermissionsInsertStatement.setLong(1, Group.this.groupDetails.getID());
							Group.savePermissionsInsertStatement.setString(2, permission.toString(false));
							Group.savePermissionsInsertStatement.setBoolean(3, permission.isGranded().booleanValue());
							Group.savePermissionsInsertStatement.execute();
						} catch (SQLException e) {
							e.printStackTrace();
						}
						return null;
					}
				});
			}
		}
	}

	private final void writeObject(ObjectOutputStream out)
		throws IOException
	{
		out.writeLong(this.groupDetails.ID);
	}

	private final void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		long ID = in.readLong();
		Group group = getGroup(ID);
		try
		{
			Field field = getClass().getDeclaredField("groupDetails");
			field.setAccessible(true);
			field.set(this, group.groupDetails);
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
			Field field = getClass().getDeclaredField("parentGroups");
			field.setAccessible(true);
			field.set(this, group.parentGroups);
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
			field.set(this, group.permissions);
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
		setup.getSharedEventBus().postGroupEvent(setup.getEventBusGroupName(), new ChangeGroupPermissionEvent(this, permission));
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
		for (Group parentGroup : this.parentGroups)
		{
			has = parentGroup.hasPermission(permission);
			if (has != null) {
				return has;
			}
		}
		return has;
	}

	public void removeParent(Group parent, Assembler setup)
	{
		for (final Group parentGroup : this.parentGroups)
		{
			if (parentGroup.equals(parent))
			{
				this.parentGroups.remove(parentGroup);

				if (saveParentsDeleteStatement == null)
				{
					if (saveParentsDeleteStatement == null) {
						try
						{
							saveParentsDeleteStatement = DatabaseManager.createPreparedStatement("DELETE FROM " + DatabaseManager.getTablePrefix() + "GroupParents WHERE GroupID=? AND ParentGroupID=?");
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}
				}

				if (setup == null) {
					return;
				}
				setup.getSharedEventBus().postGroupEvent(setup.getEventBusGroupName(), new ChangeGroupParentEvent(this, parent, false));

				DatabaseManager.addDatabaseRunnable(new DatabaseRunnable<Void>(true)
				{
					public Void run()
					{
						try {
							Group.saveParentsDeleteStatement.setLong(1, Group.this.groupDetails.getID());
							Group.saveParentsDeleteStatement.setLong(2, parentGroup.groupDetails.getID());
							Group.saveParentsDeleteStatement.execute();
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

	public void addParent(final Group parent, Assembler setup)
	{
		for (Group parentGroup : this.parentGroups)
		{
			if (parentGroup.equals(parent))
				return;
		}
		this.parentGroups.add(parent);

		if (setup == null) {
			return;
		}
		setup.getSharedEventBus().postGroupEvent(setup.getEventBusGroupName(), new ChangeGroupParentEvent(this, parent, true));

		if (saveParentsInsertStatement == null) {
			try
			{
				saveParentsInsertStatement = DatabaseManager.createPreparedInsertStatement("REPLACE INTO " + DatabaseManager.getTablePrefix() + "GroupParents (GroupID, ParentGroupID) VALUES (?, ?)");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		DatabaseManager.addDatabaseRunnable(new DatabaseRunnable<Void>()
		{
			public Void run()
			{
				try {
					Group.saveParentsInsertStatement.setLong(1, Group.this.groupDetails.getID());
					Group.saveParentsInsertStatement.setLong(2, parent.groupDetails.getID());
					Group.saveParentsInsertStatement.execute();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				return null;
			}
		});
	}

	private void saveGroupDetails()
	{
		if (saveGroupDetailsStatement == null) {
			try
			{
				saveGroupDetailsStatement = DatabaseManager.createPreparedInsertStatement("UPDATE " + DatabaseManager.getTablePrefix() + "Groups SET Prefix=?, Suffix=?, `Default`=? WHERE ID=?;");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		DatabaseManager.addDatabaseRunnable(new DatabaseRunnable<Void>()
		{
			public Void run()
			{
				try {
					Group.saveGroupDetailsStatement.setString(1, Group.this.groupDetails.getPrefix() == null ? null : Group.this.groupDetails.getPrefix().toString("save"));
					Group.saveGroupDetailsStatement.setString(2, Group.this.groupDetails.getSuffix() == null ? null : Group.this.groupDetails.getSuffix().toString("save"));
					Group.saveGroupDetailsStatement.setBoolean(3, Group.this.groupDetails.isDefault());

					Group.saveGroupDetailsStatement.setLong(4, Group.this.groupDetails.getID());

					Group.saveGroupDetailsStatement.execute();
				} catch (SQLException e) {
					e.printStackTrace();
				}

				return null;
			}
		});
	}

	private void updateGroupDetails(Assembler setup)
	{
		if (setup == null) {
			return;
		}
		saveGroupDetails();
		setup.getSharedEventBus().postGroupEvent(setup.getEventBusGroupName(), new ChangeGroupDetailsEvent(this, this.groupDetails));
	}

	public boolean equals(Group group)
	{
		return group != null;
	}

	public boolean equals(long ID)
	{
		return this.groupDetails.ID == ID;
	}

	public boolean equals(String name)
	{
		return this.groupDetails.name.equalsIgnoreCase(name);
	}

	public long getID()
	{
		return this.groupDetails.getID();
	}

	public String getName()
	{
		return this.groupDetails.getName();
	}

	public FormattedString getPrefix()
	{
		FormattedString prefix = this.groupDetails.getPrefix();
		if (prefix == null)
		{
			for (Group parent : this.parentGroups)
			{
				prefix = parent.getPrefix();
				if (prefix != null)
					return prefix;
			}
		}
		return prefix;
	}

	public void setPrefix(FormattedString perfix, Assembler setup)
	{
		this.groupDetails.setPrefix(perfix);
		updateGroupDetails(setup);
	}

	public FormattedString getSuffix()
	{
		FormattedString suffix = this.groupDetails.getSuffix();
		if (suffix == null)
		{
			for (Group parent : this.parentGroups)
			{
				suffix = parent.getSuffix();
				if (suffix != null)
					return suffix;
			}
		}
		return suffix;
	}

	public void setSuffix(FormattedString suffix, Assembler setup)
	{
		this.groupDetails.setSuffix(suffix);
		updateGroupDetails(setup);
	}

	public boolean isDefault()
	{
		return this.groupDetails.isDefault();
	}

	public void setDefault(boolean Default)
	{
		if (isDefault() == Default) {
			return;
		}
		this.groupDetails.setDefault(Default);
		saveGroupDetails();
	}

	public PermissionSet getPermissions()
	{
		PermissionSet resultingPermissions = new PermissionSet();

		for (Group parentGroup : this.parentGroups)
		{
			resultingPermissions.setPermissions(parentGroup.getPermissions());
		}

		resultingPermissions.setPermissions(this.permissions);

		return resultingPermissions;
	}

	public PermissionSet getGroupPermissions()
	{
		return this.permissions;
	}

	public Group[] getParents()
	{
		Group[] groups = new Group[this.parentGroups.size()];
		return (Group[])this.parentGroups.toArray(groups);
	}

	public boolean inheritsFrom(Group group)
	{
		for (Group parent : this.parentGroups)
		{
			if (parent.equals(group))
				return true;
			if (parent.inheritsFrom(group))
				return true;
		}
		return false;
	}

	public boolean inheritsFrom(Set<Group> groupList)
	{
		for (Group group : groupList)
		{
			if (inheritsFrom(group))
				return true;
		}
		return false;
	}

	public static void deinit()
	{
		groupList.clear();
		groupIDs.clear();
	}

	public static class GroupDetails
		implements Serializable
	{
		private static final long serialVersionUID = -9104231237070140821L;
		private final long ID;
		private final String name;
		private FormattedString prefix;
		private FormattedString suffix;
		private boolean Default;

		public GroupDetails(long ID, String name, FormattedString prefix, FormattedString suffix, boolean Default)
		{
			this.ID = ID;
			this.name = name;
			setPrefix(prefix);
			setSuffix(suffix);
			this.Default = Default;
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

		public boolean isDefault()
		{
			return this.Default;
		}

		public void setDefault(boolean Default)
		{
			this.Default = Default;
		}
	}
}

