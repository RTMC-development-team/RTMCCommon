package common;

import com.rushteamc.plugin.common.Permissions.Permission;
import com.rushteamc.plugin.common.Permissions.PermissionSet;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import org.junit.Test;

public class PermissionTest
{
	@Test
	public void permissionTest()
	{
		PermissionSet permissionSet = new PermissionSet();

		permissionSet.setPermission(new Permission("AntiBot.admin.root", Boolean.valueOf(true)));
		permissionSet.setPermission(new Permission("bukkit.command.gamemode", Boolean.valueOf(true)));
		permissionSet.setPermission(new Permission("bukkit.command.save", Boolean.valueOf(true)));
		permissionSet.setPermission(new Permission("disabler", Boolean.valueOf(true)));
		permissionSet.setPermission(new Permission("dynmap", Boolean.valueOf(true)));
		permissionSet.setPermission(new Permission("eliteblocker", Boolean.valueOf(true)));
		permissionSet.setPermission(new Permission("entitymanager", Boolean.valueOf(true)));
		permissionSet.setPermission(new Permission("essentials", Boolean.valueOf(true)));
		permissionSet.setPermission(new Permission("logblock", Boolean.valueOf(true)));
		permissionSet.setPermission(new Permission("mineconomy", Boolean.valueOf(true)));
		permissionSet.setPermission(new Permission("modifyworld", Boolean.valueOf(true)));
		permissionSet.setPermission(new Permission("multiverse", Boolean.valueOf(true)));
		permissionSet.setPermission(new Permission("OpenInv.exempt", Boolean.valueOf(true)));
		permissionSet.setPermission(new Permission("OpenInv.override", Boolean.valueOf(true)));
		permissionSet.setPermission(new Permission("rtmc", Boolean.valueOf(true)));
		permissionSet.setPermission(new Permission("worldborder", Boolean.valueOf(true)));
		permissionSet.setPermission(new Permission("worldedit", Boolean.valueOf(true)));
		permissionSet.setPermission(new Permission("worldguard", Boolean.valueOf(true)));

		System.out.println(permissionSet);

		Permission perm = new Permission("com.rushteamc.test");
		permissionSet.addPermission(perm);

		perm = new Permission("com.rushteamc.testmore");
		permissionSet.addPermission(perm);

		perm = new Permission("rtmc.adminchat");
		permissionSet.addPermission(perm);

		perm = new Permission("rtmc");
		permissionSet.addPermission(perm);

		perm = new Permission("net.testing");
		permissionSet.addPermission(perm);

		perm = new Permission("com.rushteamc");
		Permission[] permissionChanges = permissionSet.removePermission(perm);
		for (Permission permissionChange : permissionChanges) {
			System.out.println("Changed: " + permissionChange);
		}
		System.out.println(permissionSet);

		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
ObjectOutputStream objectOutputStream;
		try
		{
			objectOutputStream = new ObjectOutputStream(byteArrayOutputStream); } catch (IOException e) {
			e.printStackTrace();
			return;
		}
		try { objectOutputStream.writeObject(permissionSet);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try
		{
			objectOutputStream.flush();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			byteArrayOutputStream.flush();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		byte[] result = byteArrayOutputStream.toByteArray();
		try
		{
			objectOutputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			byteArrayOutputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(result);
ObjectInputStream objectInputStream;
		try
		{
			objectInputStream = new ObjectInputStream(byteArrayInputStream); } catch (IOException e) {
			e.printStackTrace();
			return;
		}
		try { Object obj = objectInputStream.readObject();

			if (!(obj instanceof PermissionSet)) {
				return;
			}
			PermissionSet permSet = (PermissionSet)obj;

			System.out.println(permSet);
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try
		{
			objectInputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			byteArrayInputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

