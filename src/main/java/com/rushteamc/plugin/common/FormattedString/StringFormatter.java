package com.rushteamc.plugin.common.FormattedString;

import java.util.List;

public abstract interface StringFormatter
{
	public abstract void append(List<FormattedString.FormattedStringPiece> paramList, String paramString)
		throws FormattedString.ParseErrorException;

	public abstract String getFormattedString(List<FormattedString.FormattedStringPiece> paramList);
}

