package com.rushteamc.plugin.common.NewDatasource;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

public class DataSourceNetwork implements Datasource
{
	private final DataListener dataListener;
	private final Socket socket;
	private ObjectOutputStream outputStream;
	private final ConcurrentHashMap<DataRequest<?>, Semaphore> results = new ConcurrentHashMap<DataRequest<?>, Semaphore>();
	private DataReturn<?> data;
	
	public DataSourceNetwork(final InetSocketAddress host) throws IOException
	{
		socket = new Socket();
		dataListener = new DataListener(socket, host);
	}
	
	@Override
	public <T extends ReceivableObject> T getReceivableObject(Class<T> cls, Serializable identifier) throws IOException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		DataRequest<T> dataRequest = new DataRequest<T>(cls, identifier);
		Semaphore semaphore = new Semaphore(0, true);
		while(true)
		{
			Semaphore oldSemaphore = results.putIfAbsent(dataRequest, semaphore);
			if(oldSemaphore == null)
			{
				while(true)
				{
					while(outputStream == null)
					{
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					
					try {
						System.out.print("Sending data request...");
						outputStream.writeObject(dataRequest);
						System.out.print(" Done!\n");
						break;
					} catch (IOException e) {
						outputStream = null;
					} catch (NullPointerException e) { }
					
				}
				
				try {
					semaphore.acquire(); // TODO: Set max waiting time. Throw exception when waiting time exceeded
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				DataReturn<?> ret = this.data;
				
				results.remove(dataRequest);
				semaphore.release(Integer.MAX_VALUE);

				Class<?> classes[] = new Class<?>[ret.data.length];
				
				for(int i = 0; i < ret.data.length; i++ )
					classes[i] = ret.data[i].getClass();
				
				Constructor<T> constructor = cls.getConstructor(classes);
				constructor.setAccessible(true);
				return constructor.newInstance((Object[])ret.data);
			}
			else
			{
				try {
					oldSemaphore.acquire(); // TODO: Set max waiting time. Throw exception when waiting time exceeded
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private class DataListener extends Thread
	{
		private final Socket socket;
		private final InetSocketAddress host;
		private final boolean running = true;
		
		private ObjectInputStream inputStream = null;
		
		public DataListener(final Socket socket, final InetSocketAddress host)
		{
			this.socket = socket;
			this.host = host;
			
			start();
		}
		
		public void run()
		{
			if(!socket.isConnected())
				try {
					System.out.println("Client connecting to server...");
					socket.connect(host);
				} catch (IOException e3) {
					e3.printStackTrace();
				}
			
			if(inputStream == null)
				try {
					inputStream = new ObjectInputStream(socket.getInputStream());
				} catch (IOException e3) {
					e3.printStackTrace();
				}
			
			if(outputStream == null)
				try {
					outputStream = new ObjectOutputStream(socket.getOutputStream());
				} catch (IOException e3) {
					e3.printStackTrace();
				}
			
			while(running)
			{
				try {
					Object obj = inputStream.readObject();
					
					if( !(obj instanceof DataReturn) )
					{
						try {
							sleep(100);
						} catch (InterruptedException e2) {
							e2.printStackTrace();
						}
						continue;
					}
					
					DataReturn<?> data = (DataReturn<?>)obj;
					results.get(data.getDataRequest()).release();
					
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (IOException | NullPointerException e) {
					try {
						socket.close();
					} catch (IOException e1) { }
					try {
						System.out.println("Client reconnecting to server...");
						socket.connect(host);
						try {
							inputStream = new ObjectInputStream(socket.getInputStream());
						} catch (IOException e3) {
							e3.printStackTrace();
						}
						try {
							outputStream = new ObjectOutputStream(socket.getOutputStream());
						} catch (IOException e3) {
							e3.printStackTrace();
						}
					} catch (IOException e1) {
						e1.printStackTrace();
						try {
							sleep(100);
						} catch (InterruptedException e2) {
							e2.printStackTrace();
						}
					}
				}
			}
		}
	}
	
	protected static class DataRequest<T extends ReceivableObject> implements Serializable
	{
		private static final long serialVersionUID = 3188799185242242945L;
		
		public Class<T> cls;
		public Serializable identifier;
		
		public DataRequest(Class<T> cls, Serializable identifier)
		{
			this.cls = cls;
			this.identifier = identifier;
		}
		
		public boolean equals(Object obj)
		{
			if( !(obj instanceof DataRequest<?>) )
				return false;
			
			DataRequest<?> o = (DataRequest<?>)obj;
			
			if(!o.cls.equals(cls))
				return false;

			if(!o.identifier.equals(identifier))
				return false;
			
			return true;
		}
		
		public int hashCode()
		{
			return cls.hashCode() ^ identifier.hashCode();
		}
		
		public DataReturn<T> getDataReturn()
		{
			return new DataReturn<T>(this, null);
		}
	}
	
	protected static class DataReturn<T extends ReceivableObject> implements Serializable
	{
		private static final long serialVersionUID = 4390541964351048628L;
		
		public Class<T> cls;
		public Serializable identifier;
		public Serializable[] data;
		
		public DataReturn(DataRequest<T> dataRequest, Serializable[] data)
		{
			this(dataRequest.cls, dataRequest.identifier, data);
		}
		
		public DataReturn(Class<T> cls, Serializable identifier, Serializable[] data)
		{
			this.cls = cls;
			this.identifier = identifier;
			this.data = data;
		}
		
		public DataRequest<T> getDataRequest()
		{
			return new DataRequest<T>(cls, identifier);
		}
	}
}