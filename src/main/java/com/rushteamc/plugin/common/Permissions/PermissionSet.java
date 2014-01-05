package com.rushteamc.plugin.common.Permissions;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.rushteamc.plugin.common.Permissions.Permission.PermissionNode;

public class PermissionSet
	implements Serializable
{
	private static final long serialVersionUID = -4014094362665290717L;
	private final Collection<PermissionSetNode> rootPermission;

	public PermissionSet()
	{
		this.rootPermission = new ArrayList<PermissionSetNode>();
	}

	public Boolean hasPermission(Permission permission)
	{
		Permission.PermissionNode[] permissionNodes = permission.getPermissionNodes();
		for (PermissionSetNode child : this.rootPermission)
		{
			Boolean result = child.hasPermission(permissionNodes, 0);
			if (result != null)
				return result;
		}
		return null;
	}

	public Permission[] addPermission(Permission permissions)
	{
		permissions.setGranded(Boolean.valueOf(true));
		return setPermission(permissions);
	}

	public Permission[] removePermission(Permission permissions)
	{
		permissions.setGranded(Boolean.valueOf(false));
		return setPermission(permissions);
	}

	public Permission[] unsetPermission(Permission permissions)
	{
		permissions.setGranded(null);
		return setPermission(permissions);
	}

	public Permission[] setPermission(Permission permission)
	{
		return setPermission(permission, false);
	}

	private Permission[] setPermission(Permission permission, boolean clear)
	{
		Permission.PermissionNode[] permissionNodes = permission.getPermissionNodes();

		for (PermissionSetNode child : this.rootPermission)
		{
			Permission[] result = child.setPermission(permission, permissionNodes, 0, clear);
			if (result != null)
				return result;
		}
		PermissionSetNode permissionNode;
		if (permissionNodes.length == 1)
		{
			permissionNode = new PermissionSetNode(permissionNodes[0], permission.isGranded());
		}
		else
		{
			permissionNode = new PermissionSetNode(permissionNodes[0]);
			if (permissionNodes.length > 1)
				permissionNode.createPermission(permissionNodes, 1, permission.isGranded());
		}
		this.rootPermission.add(permissionNode);

		return new Permission[] { permission };
	}

	public void setPermissions(PermissionSet permissions)
	{
		Collection<Permission> perms = permissions.getPermissions();
		for (Permission perm : perms)
		{
			setPermission(perm, true);
		}
	}

	public Collection<Permission> getPermissions()
	{
		Collection<Permission> permissions = new ArrayList<Permission>();
		Collection<PermissionNode> permissionNodes = new ArrayList<PermissionNode>();

		for (PermissionSetNode child : this.rootPermission)
		{
			child.getEndNodes(permissions, permissionNodes);
		}

		return permissions;
	}

	public String toString()
	{
		Collection<Permission> permissions = getPermissions();

		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("Permission set:");
		for (Permission permission : permissions)
		{
			stringBuilder.append("\n\t");
			stringBuilder.append(permission.toString());
		}

		return stringBuilder.toString();
	}

	private final void writeObject(ObjectOutputStream out) throws IOException
	{
		Collection<Permission> permissions = getPermissions();
		Permission[] serialized = new Permission[permissions.size()];
		out.writeObject(permissions.toArray(serialized));
	}

	private final void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		try {
			Field field = getClass().getDeclaredField("rootPermission");
			field.setAccessible(true);
			field.set(this, new ArrayList<PermissionSetNode>());
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}

		Object obj = in.readObject();

		if (!(obj instanceof Permission[])) {
			throw new IOException("Expected object of type Permission[] but got object of type " + obj.getClass().getCanonicalName() + " dusing deserialization");
		}
		Permission[] permissions = (Permission[])obj;

		for (Permission permission : permissions)
		{
			if (permission == null)
				Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Got null permission while deserializing permission!");
			setPermission(permission);
		}
	}

	private static class PermissionSetNode implements Serializable {
		private static final long serialVersionUID = 5728156187406166552L;
		private final Collection<PermissionSetNode> children = new ArrayList<PermissionSetNode>();
		private final Permission.PermissionNode permissionNode;
		private Boolean granded;

		public PermissionSetNode(Permission.PermissionNode permissionNode) {
			this(permissionNode, null);
		}

		public void getEndNodes(Collection<Permission> permissions, Collection<Permission.PermissionNode> permissionNodes)
		{
			permissionNodes.add(this.permissionNode);
			Permission p;
			if (this.granded != null)
			{
				Permission.PermissionNode[] arr = new Permission.PermissionNode[permissionNodes.size()];
				p = new Permission((Permission.PermissionNode[])permissionNodes.toArray(arr), this.granded);
				permissions.add(p);

				permissionNodes.remove(this.permissionNode);
				return;
			}

			for (PermissionSetNode child : this.children)
			{
				child.getEndNodes(permissions, permissionNodes);
			}
			permissionNodes.remove(this.permissionNode);
		}

		public PermissionSetNode(Permission.PermissionNode permissionNode, Boolean granded)
		{
			this.permissionNode = permissionNode;
			this.granded = granded;
		}

		public Boolean hasPermission(Permission.PermissionNode[] permissionNodes, int level)
		{
			if (permissionNodes[level] != this.permissionNode) {
				return null;
			}
			if (level + 1 >= permissionNodes.length) {
				return this.granded;
			}
			for (PermissionSetNode child : this.children)
			{
				Boolean result = child.hasPermission(permissionNodes, level + 1);
				if (result != null) {
					return result;
				}
			}
			return this.granded;
		}

		public Permission[] setPermission(Permission permission, Permission.PermissionNode[] permissionNodes, int level, boolean clear)
		{
			if (!permissionNodes[level].getName().equalsIgnoreCase(this.permissionNode.getName())) {
				return null;
			}
			if (permissionNodes.length == level + 1)
			{
				Permission[] result;
				if (this.granded == null)
				{
					if (permission.isGranded() == null)
						result = new Permission[0];
					else
						result = new Permission[] { permission };
				}
				else
				{
					if (permission.isGranded() == null) {
						result = new Permission[] { permission };
					}
					else
					{
						if (this.granded == permission.isGranded())
							result = new Permission[0];
						else
							result = new Permission[] { permission }; 
					}
				}
				this.granded = permission.isGranded();
				if (clear) {
					this.children.clear();
				}
				return result;
			}

			for (PermissionSetNode child : this.children)
			{
				Permission[] result = child.setPermission(permission, permissionNodes, level + 1, clear);
				if (result != null) {
					return result;
				}
			}
			createPermission(permissionNodes, level + 1, permission.isGranded());
			return new Permission[] { permission };
		}

		public void createPermission(Permission.PermissionNode[] permissionNodes, int level, Boolean grand)
		{
			PermissionSetNode permissionNode;
			if (permissionNodes.length <= level + 1)
			{
				permissionNode = new PermissionSetNode(permissionNodes[level], grand);
			}
			else
			{
				permissionNode = new PermissionSetNode(permissionNodes[level]);
				permissionNode.createPermission(permissionNodes, level + 1, grand);
			}
			this.children.add(permissionNode);
		}
	}
}

