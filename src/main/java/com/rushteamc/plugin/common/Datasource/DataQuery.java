package com.rushteamc.plugin.common.Datasource;

import java.io.Serializable;

public abstract class DataQuery implements Serializable
{
	private static final long serialVersionUID = -2964069318915573958L;
	
	private final DataQuery baseQuery;
	
	protected DataQuery(DataQuery query)
	{
		baseQuery = query;
	}
	
	public <T> DataQuery select(DataField<T> field)
	{
		return new DataQuerySelect<T>(this, field);
	}
	
	public <T> DataQuery fieldConstraint(DataField<T> field, T value, ContraintType type)
	{
		return new DataQueryConstraint<T>(this, field, value, type);
	}
	
	public <T> DataQuery link(DataObject obj, DataField<T> field1, DataField<T> field2, ContraintType type)
	{
		return new DataQueryLink<T>(this, obj, field1, field2, type);
	}

	protected DataQuery getBaseQuery()
	{
		return baseQuery;
	}
}
