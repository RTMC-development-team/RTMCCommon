package com.rushteamc.plugin.common.Datasource;

public class DataQueryBase extends DataQuery
{
	private static final long serialVersionUID = -2859939720662242060L;
	
	private final DataObject dataObject;
	
	protected DataQueryBase(final DataObject dataObject)
	{
		super(null);
		this.dataObject = dataObject;
	}

	public DataObject getDataObject() {
		return dataObject;
	}
}
