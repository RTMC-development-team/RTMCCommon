package com.rushteamc.plugin.common.Datasource;

public class DataQueryConstraint<T> extends DataQuery
{
	private static final long serialVersionUID = 4664181488930953067L;
	
	private final DataField<T> field;
	private final T value;
	private final ContraintType type;

	public DataQueryConstraint(DataQuery dataQuery, DataField<T> field, T value, ContraintType type)
	{
		super(dataQuery);
		
		this.field = field;
		this.value = value;
		this.type = type;
	}

	public DataField<T> getField()
	{
		return field;
	}

	public T getValue()
	{
		return value;
	}

	public ContraintType getType()
	{
		return type;
	}
}
