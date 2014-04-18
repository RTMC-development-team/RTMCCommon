package com.rushteamc.plugin.common.NewDatasource;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

import com.rushteamc.plugin.common.NewDatasource.DataSourceNetwork.DataRequest;
import com.rushteamc.plugin.common.NewDatasource.DataSourceNetwork.DataReturn;

public class DataProviderNetwork
{
	private final ClientListener dataListener;
	
	public DataProviderNetwork(final int port) throws IOException
	{
		dataListener = new ClientListener(port);
	}
	
	public void close() throws InterruptedException
	{
		dataListener.close();
	}
	
	private class ClientListener extends Thread
	{
		private boolean running = true;
		private final ServerSocket socket;
		private final Set<DataListener> dataListeners = new HashSet<DataListener>();
		
		public ClientListener(final int port) throws IOException
		{
			socket = new ServerSocket(port);
			start();
		}
		
		public void close() throws InterruptedException
		{
			running = false;
			
			for(DataListener dataListener : dataListeners)
				dataListener.close();
			for(DataListener dataListener : dataListeners)
				dataListener.join();
			
			join();
		}
		
		public void run()
		{
			while(running)
			{
				try {
					Socket clientSocket = socket.accept();
					dataListeners.add(new DataListener(this, clientSocket));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		public void removeListener(DataListener dataListener)
		{
			dataListeners.remove(dataListener);
		}
	}
	
	private class DataListener extends Thread
	{
		private boolean running = true;
		private final ClientListener clientListener;
		
		private ObjectInputStream inputStream = null;
		private ObjectOutputStream outputStream = null;
		private final Socket socket;
		
		public DataListener(final ClientListener clientListener, final Socket socket) throws IOException
		{
			System.out.println("Server connecting to client...");
			this.clientListener = clientListener;
			this.socket = socket;
			outputStream = new ObjectOutputStream(socket.getOutputStream());
			inputStream = new ObjectInputStream(socket.getInputStream());
			start();
		}
		
		public void close()
		{
			running = false;
		}
		
		public void run()
		{
			while(running)
			{
				try {
					Object obj = inputStream.readObject();
					
					if( !(obj instanceof DataRequest) )
					{
						try {
							sleep(100);
						} catch (InterruptedException e2) {
							e2.printStackTrace();
						}
						continue;
					}
					
					DataRequest<?> dataRequest = (DataRequest<?>)obj;
					try {
						ReceivableObject receivableObject = (ReceivableObject)dataRequest.cls.getConstructor(dataRequest.identifier.getClass()).newInstance(dataRequest.identifier);
						DataReturn<?> dataReturn = dataRequest.getDataReturn();
						dataReturn.data = receivableObject.getObjectData();
					} catch (InstantiationException | IllegalAccessException
							| IllegalArgumentException
							| InvocationTargetException | NoSuchMethodException
							| SecurityException e) {
						e.printStackTrace();
					}
					
					outputStream.writeObject(dataRequest);
					
				} catch (ClassNotFoundException | NullPointerException e) {
					e.printStackTrace();
				} catch (IOException e) {
					clientListener.removeListener(this);
				}
			}
		}
	}
}
