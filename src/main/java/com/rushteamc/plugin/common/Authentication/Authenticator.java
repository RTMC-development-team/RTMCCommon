package com.rushteamc.plugin.common.Authentication;

import com.rushteamc.plugin.common.Assembler;
import com.rushteamc.plugin.common.Events.GroupAccessEvent;
import com.rushteamc.plugin.common.Events.RequestGroupAccessEvent;
import com.rushteamc.plugin.common.Player;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class Authenticator
{
	private final Assembler setup;
	private final Key staticPublicKey;
	private final Key staticPrivateKey;
	private final Map<Key, Key> keyMap = new HashMap<Key, Key>();
	private final GroupAccessChecker groupAccessChecker;

	public Authenticator(Assembler setup, byte[] publicKey, byte[] privateKey, GroupAccessChecker groupAccessChecker)
	{
		this.setup = setup;

		if (publicKey != null)
		{
			Key staticPublicKey = null;
			try {
				KeyFactory keyFactory = KeyFactory.getInstance("DSA");
				X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKey);
				staticPublicKey = keyFactory.generatePublic(publicKeySpec);
			} catch (NoSuchAlgorithmException|InvalidKeySpecException e) {
				e.printStackTrace();
			}
			this.staticPublicKey = staticPublicKey;
		}
		else {
			this.staticPublicKey = null;
		}
		if (privateKey != null)
		{
			Key staticPrivateKey = null;
			try {
				KeyFactory keyFactory = KeyFactory.getInstance("DSA");
				X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(privateKey);
				staticPrivateKey = keyFactory.generatePublic(publicKeySpec);
			} catch (NoSuchAlgorithmException|InvalidKeySpecException e) {
				e.printStackTrace();
			}
			this.staticPrivateKey = staticPrivateKey;
		}
		else {
			this.staticPrivateKey = null;
		}
		this.groupAccessChecker = groupAccessChecker;
	}

	public static StorableKeyPair genStorableKeyPair() throws NoSuchAlgorithmException
	{
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
		kpg.initialize(3072);
		KeyPair kp = kpg.genKeyPair();
		Key pubKey = kp.getPublic();
		Key priKey = kp.getPrivate();
		return new StorableKeyPair(pubKey.getEncoded(), priKey.getEncoded());
	}

	public static Player authenticateUser(String playerName, String password) throws NoSuchAlgorithmException
	{
		Player player = Player.getExsistingPlayer(playerName);

		if (player == null) {
			return player;
		}
		if (authenticateUser(player, password)) {
			return player;
		}
		return null;
	}

	public static boolean authenticateUser(Player player, String password) throws NoSuchAlgorithmException
	{
		byte[] checkPassHash = getPasswordHash(password);
		byte[] playerPassHash = player.getPasswordHash();

		if (playerPassHash.length != checkPassHash.length) {
			return false;
		}
		for (int i = 0; i < playerPassHash.length; i++) {
			if (checkPassHash[i] != playerPassHash[i])
				return false;
		}
		return true;
	}

	public static byte[] getPasswordHash(String password) throws NoSuchAlgorithmException
	{
		MessageDigest md = MessageDigest.getInstance("SHA-512");
		md.update(password.getBytes());
		return md.digest();
	}

	public boolean requestGroupAccess(String groupname, String username, String password) throws NoSuchAlgorithmException
	{
		if (this.staticPublicKey == null) {
			return false;
		}
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
		kpg.initialize(3072);
		KeyPair kp = kpg.genKeyPair();
		Key publicKey = kp.getPublic();
		Key privateKey = kp.getPrivate();

		this.keyMap.put(publicKey, privateKey);
		try
		{
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);

			objectOutputStream.writeObject(groupname);
			objectOutputStream.writeObject(username);
			objectOutputStream.writeObject(password);
			objectOutputStream.writeObject(publicKey);

			objectOutputStream.flush();
			byte[] encodedMessage = RSAEncode(this.staticPublicKey, byteArrayOutputStream.toByteArray());
			this.setup.publisEvent(new RequestGroupAccessEvent(encodedMessage, publicKey));

			objectOutputStream.close();
			byteArrayOutputStream.close();

			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return false;
	}

	public void handleGroupAccess(byte[] data, Key key) throws NoSuchAlgorithmException
	{
		Key privateKey = (Key)this.keyMap.get(key);
		if (privateKey == null) {
			return;
		}
		this.keyMap.remove(key);

		byte[] decodedData = RSADecode(privateKey, data);
		try
		{
			ByteArrayInputStream byteArrayIntputStream = new ByteArrayInputStream(decodedData);
			ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayIntputStream);

			Object obj = objectInputStream.readObject();
			if (!(obj instanceof String)) {
				throw new ClassCastException("Expected object of type String, but got object of type " + obj.getClass().getCanonicalName());
			}
			String groupName = (String)obj;

			obj = objectInputStream.readObject();
			if (!(obj instanceof SecretKey)) {
				throw new ClassCastException("Expected object of type String, but got object of type " + obj.getClass().getCanonicalName());
			}
			SecretKey password = (SecretKey)obj;

			this.setup.getSharedEventBus().addGroup(groupName, password);

			objectInputStream.close();
			byteArrayIntputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void handleRequestGroupAccess(byte[] data, Key key)
	{
		if (this.groupAccessChecker == null) {
			return;
		}
		if (this.staticPrivateKey == null) {
			return;
		}
		if (!this.staticPublicKey.equals(key)) {
			return;
		}
		byte[] decodedData = RSADecode(key, data);
		try
		{
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(decodedData);
			ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);

			Object obj = objectInputStream.readObject();
			if (!(obj instanceof String))
				return;
			String groupname = (String)obj;

			obj = objectInputStream.readObject();
			if (!(obj instanceof String))
				return;
			String username = (String)obj;

			obj = objectInputStream.readObject();
			if (!(obj instanceof String))
				return;
			String password = (String)obj;

			obj = objectInputStream.readObject();
			if (!(obj instanceof Key))
				return;
			Key publicKey = (Key)obj;

			SecretKey groupKey = this.setup.getSharedEventBus().getGroupKey(groupname);
			if (groupKey == null) {
				return;
			}
			if (this.groupAccessChecker.grandAccess(username, password, groupname))
			{
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);

				objectOutputStream.writeObject(groupname);
				objectOutputStream.writeObject(groupKey);

				objectOutputStream.flush();
				byte[] encodedMessage = RSAEncode(publicKey, byteArrayOutputStream.toByteArray());
				this.setup.publisEvent(new GroupAccessEvent(encodedMessage, publicKey));

				objectOutputStream.close();
				byteArrayOutputStream.close();
			}

			objectInputStream.close();
			byteArrayInputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static byte[] RSAEncode(Key key, byte[] message)
	{
		Cipher cipher;
		try
		{
			cipher = Cipher.getInstance("RSA/ECB/NoPadding");
		}
		catch (NoSuchPaddingException e)
		{
			e.printStackTrace();
			return null;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
		try {
			cipher.init(1, key);
		} catch (InvalidKeyException e) {
			e.printStackTrace();
			return null;
		}

		byte[] result;
		try
		{
			result = cipher.doFinal(message);
		}
		catch (IllegalBlockSizeException e)
		{
			e.printStackTrace();
			return null;
		} catch (BadPaddingException e) {
			e.printStackTrace();
			return null;
		}
		return result;
	}

	public static byte[] RSADecode(Key key, byte[] message)
	{
			Cipher cipher;
		try
		{
			cipher = Cipher.getInstance("RSA/ECB/NoPadding");
		}
		catch (NoSuchPaddingException e)
		{
			e.printStackTrace();
			return null;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
		try {
			cipher.init(2, key);
		} catch (InvalidKeyException e) {
			e.printStackTrace();
			return null;
		}

		byte[] result;
		try
		{
			result = cipher.doFinal(message);
		}
		catch (IllegalBlockSizeException e)
		{
			e.printStackTrace();
			return null;
		} catch (BadPaddingException e) {
			e.printStackTrace();
			return null;
		}
		return result;
	}

	public static String hashToString(byte[] hash)
	{
		StringBuilder hexString = new StringBuilder();
		byte[] arrayOfByte = hash; int j = hash.length; for (int i = 0; i < j; i++) { int d = arrayOfByte[i];
			hexString.append(Character.forDigit(d >> 4 & 0xF, 16));
			hexString.append(Character.forDigit(d & 0xF, 16));
		}
		return hexString.toString();
	}

	public static byte[] stringToHash(String string) throws IllegalArgumentException
	{
		int len = string.length();
		byte[] data = new byte[len / 2];
		try
		{
			for (int i = 0; i < len; i += 2)
			{
				data[(i / 2)] = 
					((byte)((Character.digit(string.charAt(i), 16) << 4) + 
					Character.digit(string.charAt(i + 1), 16)));
			}
		} catch (IndexOutOfBoundsException e) {
			throw new IllegalArgumentException("String \"" + string + "\" is not a valid encoded hash!");
		}

		return data;
	}

	public static class StorableKeyPair implements Serializable
	{
		private static final long serialVersionUID = 2597162172093695126L;
		private final byte[] publicKey;
		private final byte[] privateKey;

		public StorableKeyPair(byte[] publicKey, byte[] privateKey) {
			this.publicKey = publicKey;
			this.privateKey = privateKey;
		}

		public byte[] getPublicKey()
		{
			return this.publicKey;
		}

		public byte[] getPrivateKey()
		{
			return this.privateKey;
		}
	}
}

