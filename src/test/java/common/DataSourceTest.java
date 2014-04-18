package common;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;

import org.junit.Test;

import com.rushteamc.plugin.common.NewDatasource.DataProviderNetwork;
import com.rushteamc.plugin.common.NewDatasource.DataSourceNetwork;
import com.rushteamc.plugin.common.NewDatasource.ReceivableObject;

public class DataSourceTest
{
	private final static int PORT = 4623;
	
	@Test
	public void dataSourceTest()
	{
		DataProviderNetwork dataProviderNetwork = null;
		try {
			dataProviderNetwork = new DataProviderNetwork(PORT);
		} catch (IOException e) {
			e.printStackTrace();
		}

		
		DataSourceNetwork dataSource = null;
		try {
			dataSource = new DataSourceNetwork(new InetSocketAddress("localhost", PORT));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			System.out.println("Requesting test object...");
			testClass result = dataSource.getReceivableObject(testClass.class, "some test");
			System.out.println("Recieved test object with text = " + result.text);
		} catch (NoSuchMethodException | SecurityException
				| InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| IOException e1) {
			e1.printStackTrace();
		}
		
		
		try {
			dataProviderNetwork.close();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		/*
		DataSourceNetwork dataSource = null;
		try {
			dataSource = new DataSourceNetwork(null);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		try {
			testClass obj = dataSource.getReceivableObject(testClass.class, 123);
			obj.test();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | IOException e) {
			e.printStackTrace();
		}
		*/
	}
	
	public static class testClass implements ReceivableObject
	{
		private static final long serialVersionUID = -1084558028441336969L;

		private String text = "-";
		
		public testClass(String x)
		{
			text = x;
		}
		
		public testClass(Integer x, String y, Integer z)
		{
			System.out.println("Argument x: " + x);
			System.out.println("Argument y: " + y);
			System.out.println("Argument z: " + z);
		}
		
		public void test()
		{
			System.out.println("Called test class!");
		}

		@Override
		public Serializable[] getObjectData()
		{
			return new Serializable[]{text + " (send)"};
		}
	}
}
