package com.rushteamc.plugin.common.Permissions;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

public class Permission
	implements Serializable
{
	private static final long serialVersionUID = 6894980256724387314L;
	public static final char PERMISSION_SEPERATOR = '.';
	private static final Collection<PermissionNode> permissionNodeList = new ArrayList<PermissionNode>();
	private final PermissionNode[] permissionNodes;
	private Boolean granded;

	public Permission(String permission)
	{
		this(getPermissionNodes(permission), Boolean.valueOf(true));
	}

	public Permission(String permission, Boolean granded)
	{
		this(getPermissionNodes(permission), granded);
	}

	protected Permission(PermissionNode[] permissionNodes, Boolean granded)
	{
		this.permissionNodes = permissionNodes;
		this.granded = granded;
	}

	private static PermissionNode[] getPermissionNodes(String permission)
	{
		Collection<PermissionNode> collection = new LinkedList<PermissionNode>();

		int permissionStart = 0;
		int permissionEnd = permission.indexOf('.');
		while (permissionEnd != -1)
		{
			collection.add(getPermissionNode(permission.substring(permissionStart, permissionEnd)));
			permissionStart = permissionEnd + 1;
			permissionEnd = permission.indexOf('.', permissionStart);
		}
		collection.add(getPermissionNode(permission.substring(permissionStart)));

		PermissionNode[] result = new PermissionNode[collection.size()];
		return (PermissionNode[])collection.toArray(result);
	}

	private static PermissionNode getPermissionNode(String name)
	{
		for (PermissionNode permissionNode : permissionNodeList)
		{
			if (permissionNode.match(name)) {
				return permissionNode;
			}
		}
		PermissionNode permissionNode = new PermissionNode(name);
		permissionNodeList.add(permissionNode);

		return permissionNode;
	}

	public PermissionNode[] getPermissionNodes()
	{
		return this.permissionNodes;
	}

	public String toString()
	{
		return toString(true);
	}

	public String toString(boolean printMin)
	{
		StringBuilder stringBuilder = new StringBuilder();

		if (printMin)
		{
			if (this.granded == null)
				stringBuilder.append('*');
			else if (!this.granded.booleanValue())
				stringBuilder.append('-');
			else {
				stringBuilder.append('+');
			}
		}
		for (PermissionNode permissionNode : this.permissionNodes)
		{
			stringBuilder.append(permissionNode.getName());
			stringBuilder.append('.');
		}

		return stringBuilder.deleteCharAt(stringBuilder.length() - 1).toString();
	}

	private final void writeObject(ObjectOutputStream out) throws IOException
	{
		String[] serialize = new String[this.permissionNodes.length];

		for (int i = 0; i < this.permissionNodes.length; i++) {
			serialize[i] = this.permissionNodes[i].getName();
		}
		out.writeObject(serialize);
		out.writeObject(this.granded);
	}

	private final void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		Object obj = in.readObject();

		if (!(obj instanceof String[])) {
			throw new IOException("Expected object of type String[] but got object of type " + obj.getClass().getCanonicalName() + " dusing deserialization");
		}
		String[] serialize = (String[])obj;
		PermissionNode[] result = new PermissionNode[serialize.length];

		for (int i = 0; i < serialize.length; i++)
			result[i] = getPermissionNode(serialize[i]);
		try
		{
			Field field = getClass().getDeclaredField("permissionNodes");
			field.setAccessible(true);
			field.set(this, result);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}

		obj = in.readObject();

		if (!(obj instanceof Boolean)) {
			throw new IOException("Expected object of type Boolean but got object of type " + obj.getClass().getCanonicalName() + " dusing deserialization");
		}
		this.granded = ((Boolean)obj);
	}

	public Boolean isGranded()
	{
		return this.granded;
	}

	public void setGranded(Boolean granded)
	{
		this.granded = granded;
	}

	public static class PermissionNode
	{
		private final String name;

		public PermissionNode(String name)
		{
			this.name = name;
		}

		public boolean match(String comp)
		{
			return this.name.equalsIgnoreCase(comp);
		}

		public String getName()
		{
			return this.name;
		}
	}
}

