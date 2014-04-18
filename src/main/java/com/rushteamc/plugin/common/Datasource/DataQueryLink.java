package com.rushteamc.plugin.common.Datasource;

public class DataQueryLink<T> extends DataQuery
{
	private static final long serialVersionUID = 4180927352611995472L;
	
	private final DataObject obj;
	private final DataField<T> field1;
	private final DataField<T> field2;
	private final ContraintType type;

	public DataQueryLink(DataQuery dataQuery, DataObject obj, DataField<T> field1, DataField<T> field2, ContraintType type)
	{
		super(dataQuery);
		
		this.obj = obj;
		this.field1 = field1;
		this.field2 = field2;
		this.type = type;
	}

	public DataObject getObj() {
		return obj;
	}

	public DataField<T> getField1() {
		return field1;
	}

	public DataField<T> getField2() {
		return field2;
	}

	public ContraintType getType() {
		return type;
	}
}
