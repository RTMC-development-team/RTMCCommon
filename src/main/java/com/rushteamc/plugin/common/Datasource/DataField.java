package com.rushteamc.plugin.common.Datasource;

public class DataField<T>
{
	private final DataObject object;
	private final String fieldName;
	
	public DataField(DataObject object, String fieldName)
	{
		this.object = object;
		this.fieldName = fieldName;
	}

	public DataObject getDataObject() {
		return object;
	}

	public String getFieldName() {
		return fieldName;
	}
}
