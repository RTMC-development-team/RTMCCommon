package com.rushteamc.plugin.common.NewDatasource;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

public interface Datasource
{
	public <T extends ReceivableObject> T getReceivableObject(Class<T> cls, Serializable identifier) throws IOException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException;
	
	public class InvalidIdentifierException extends Exception
	{
		private static final long serialVersionUID = 1662456093844096661L;

		public InvalidIdentifierException(final String message)
		{
			super(message);
		}
	}
}
