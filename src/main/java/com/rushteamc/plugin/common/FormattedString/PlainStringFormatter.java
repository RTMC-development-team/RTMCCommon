package com.rushteamc.plugin.common.FormattedString;

import java.util.List;

public class PlainStringFormatter
	implements StringFormatter
{
	public void append(List<FormattedString.FormattedStringPiece> formattedStringPieces, String text)
		throws FormattedString.ParseErrorException
	{
		formattedStringPieces.add(new FormattedString.FormattedStringPiece(new FormattedString.Style(), text));
	}

	public String getFormattedString(List<FormattedString.FormattedStringPiece> formattedStringPieces)
	{
		StringBuilder stringBuilder = new StringBuilder();
		for (FormattedString.FormattedStringPiece formattedStringPiece : formattedStringPieces)
			stringBuilder.append(formattedStringPiece.getText());
		return stringBuilder.toString();
	}
}

