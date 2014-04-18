package com.rushteamc.plugin.common.Datasource;

public class DataQuerySelect<T> extends DataQuery
{
	private static final long serialVersionUID = 6770339972086288944L;
	
	private final DataField<T> field;
	
	public DataQuerySelect(DataQuery query, DataField<T> field)
	{
		super(query);
		this.field = field;
	}
	
	public DataField<T> getField()
	{
		return field;
	}
}
