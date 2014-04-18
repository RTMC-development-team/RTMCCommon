package com.rushteamc.plugin.common.Datasource;

import java.util.Collection;
import java.util.Iterator;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class DataCollection implements Collection<DataCollection.DataRow>
{
	//private final String[] columnNames;
	//private final Class[] types;
	
	private final DataColumn[] data;
	
	public DataCollection(FieldDescription<?>... fieldDescription)
	{
		data = new DataColumn[fieldDescription.length];
		for( int i = 0; i < fieldDescription.length; i++)
		{
			data[i] = new DataColumn(fieldDescription[i]);
		}
	}
	
	@SuppressWarnings("unchecked")
	private <T> T[] getArray(Class<T> type, int size)
	{
		return (T[])Array.newInstance(type, size);
	}
	
	private class DataColumn<T>
	{
		private final FieldDescription<T> fieldDescription;
		private T[] data;
		
		@SuppressWarnings("unchecked")
		public DataColumn(FieldDescription<T> fieldDescription)
		{
			this.fieldDescription = fieldDescription;
			data = (T[]) getArray(fieldDescription.type, 0);
		}
	}
	
	public static class FieldDescription<T>
	{
		private final Class<T> type;
		private final String name;
		
		public FieldDescription(String name)
		{
			this.type = getType();
			this.name = name;
		}

		@SuppressWarnings("unchecked")
		private Class<T> getType()
		{
			return (Class<T>)((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
		}
	}
	
	
	
	
	
	public int getColumn(String name)
	{
		for(int i = 0; i < columnNames.length; i++)
		{
			if(columnNames[i].equalsIgnoreCase(name))
				return i;
		}
		return -1;
	}
	
	public <T> T getField(int row, String columnName)
	{
		return getField(row, getColumn(columnName));
	}
	
	public <T> T getField(int row, int column)
	{
		if(row >= size() || row < 0)
			throw new IndexOutOfBoundsException("Invalid row " + row + "! DataCollection only contains " + size() + "rows.");
		
		if(column >= columnNames.length || column < 0)
			throw new IndexOutOfBoundsException("Invalid column " + column + "! Datacollection only contains " + columnNames.length + " columns.");

		Object obj = null;
		
		Class<T> type = getType();
		
		if(type.isInstance(obj) )
			throw new TypeMismatchException(type , obj.getClass());
		
		return type.cast(obj);
	}

	@SuppressWarnings("unchecked")
	private <T> Class<T> getType()
	{
		return (Class<T>)((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
	}
	
	
	
	@Override
	public int size()
	{
		return (data.length > 0) ? data[0].size() : 0;
	}

	@Override
	public boolean isEmpty()
	{
		return (data.length <= 0);
	}

	@Override
	public boolean contains(Object o)
	{
		;
	}

	@Override
	public Iterator<DataRow> iterator()
	{
		;
	}

	@Override
	public Object[] toArray()
	{
		;
	}

	@Override
	public <T> T[] toArray(T[] a)
	{
		;
	}

	@Override
	public boolean add(DataRow e)
	{
		;
	}

	@Override
	public boolean remove(Object o)
	{
		;
	}

	@Override
	public boolean containsAll(Collection<?> c)
	{
		;
	}

	@Override
	public boolean addAll(Collection<? extends DataRow> c)
	{
		;
	}

	@Override
	public boolean removeAll(Collection<?> c)
	{
		;
	}

	@Override
	public boolean retainAll(Collection<?> c)
	{
		;
	}

	@Override
	public void clear()
	{
		;
	}
	
	public class DataRow
	{
		private final long row;
		
		private DataRow(final long row)
		{
			this.row = row;
		}
		
		public <T> T getField(String name)
		{
			return getField(getColumn(name));
		}
		
		public <T> T getField(int column)
		{
			return null; // TODO: Get actual column data
		}
		
		public int getColumn(String name)
		{
			return 0; // TODO: get actual column num
		}
	}
	
	public static class TypeMismatchException extends RuntimeException
	{
		private static final long serialVersionUID = -5209926640856321976L;

		public TypeMismatchException(final Class<?> type1, final Class<?> type2)
		{
			super("Type mismatch. Type " + type1.getCanonicalName() + " does not match type " +type2.getCanonicalName() + "!");
		}
	}
}
