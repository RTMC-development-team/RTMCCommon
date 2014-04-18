package com.rushteamc.plugin.common.Datasource;

import java.util.LinkedList;
import java.util.List;

public class StaticDataQueryMySQL implements StaticDataQuery
{
	private static final long serialVersionUID = 4589431690585497192L;
	
	private final String query;

	public StaticDataQueryMySQL(DataQuery dataQuery) throws Exception
	{
		DataQueryBase base = null;
		List<DataQuerySelect<?>> selects = new LinkedList<DataQuerySelect<?>>();
		List<DataQueryLink<?>> links = new LinkedList<DataQueryLink<?>>();
		List<DataQueryConstraint<?>> constraints = new LinkedList<DataQueryConstraint<?>>();
		
		DataQuery current = dataQuery;
		
		while(current != null)
		{
			if(current instanceof DataQuerySelect)
				selects.add((DataQuerySelect<?>)current);
			else if(current instanceof DataQueryLink)
				links.add((DataQueryLink<?>)current);
			else if(current instanceof DataQueryConstraint)
				constraints.add((DataQueryConstraint<?>)current);
			else if(current instanceof DataQueryBase)
			{
				if(base != null)
					throw new Exception("Multiple bases found!"); // TODO: custom exception
				base = (DataQueryBase)current;
			}
			else
				throw new Exception("Unknown type found!"); // TODO: custom exception
		}
		
		if(base == null)
			throw new Exception("No base found!"); // TODO: custom exception
		
		StringBuilder sb = new StringBuilder("SELECT ");
		
		boolean notFirst = false;
		for( DataQuerySelect<?> select : selects )
		{
			if(notFirst)
				sb.append(", `");
			else
				notFirst = true;
			
			sb.append(select.getField().getDataObject().getName());
			sb.append("`.`");
			sb.append(select.getField().getFieldName());
			sb.append('`');
		}
		
		sb.append(" FROM `");
		sb.append(base.getDataObject().getName());
		sb.append("` ");

		for( DataQueryLink<?> link : links )
		{
			sb.append(" JOIN `");
			sb.append(link.getObj().getName());
			sb.append("` ON `");
			sb.append(link.getField1().getDataObject().getName());
			sb.append("`.`");
			sb.append(link.getField1().getFieldName());
			sb.append('`');
			sb.append(type2SqlString(link.getType()));
			sb.append('`');
			sb.append(link.getField2().getDataObject().getName());
			sb.append("`.`");
			sb.append(link.getField2().getFieldName());
			sb.append("` ");
		}
		
		if(!constraints.isEmpty())
		{
			boolean first = true;
			for( DataQueryConstraint<?> constraint : constraints )
			{
				if(first)
				{
					sb.append(" WHERE ");
					first = false;
				}
				else
					sb.append(" AND ");

				sb.append(constraint.getField().getDataObject().getName());
				sb.append("`.`");
				sb.append(constraint.getField().getFieldName());
				sb.append('`');
				sb.append(type2SqlString(constraint.getType()));
				sb.append('\"');
				sb.append(constraint.getValue().toString());
				sb.append('\"');
			}
		}
		
		sb.append(';');
		query = sb.toString();
	}

	private String type2SqlString(ContraintType type)
	{
		switch(type)
		{
		case EQUAL:
			return "=";
		case NOT_EQUAL:
			return "!=";
		case LESS_THEN:
			return "<";
		case GREATER_THEN:
			return ">";
		case LESS_THEN_EQUAL_TO:
			return "<=";
		case GREATER_THEN_EQUAL_TO:
			return ">=";
		case LIKE:
			return " LIKE ";
		}
		return "";
	}

	public String getQuery() {
		return query;
	}
}
