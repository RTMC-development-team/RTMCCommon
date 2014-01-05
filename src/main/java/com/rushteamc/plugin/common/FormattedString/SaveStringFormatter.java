package com.rushteamc.plugin.common.FormattedString;

import java.awt.Color;
import java.util.List;

public class SaveStringFormatter
	implements StringFormatter
{
	public void append(List<FormattedString.FormattedStringPiece> formattedStringPieces, String text)
		throws FormattedString.ParseErrorException
	{
		try
		{
			int i = 0;
			while (i < text.length())
			{
				Color col = null;
				if (text.charAt(i) != '-')
				{
					col = new Color(Integer.valueOf(text.substring(i, i + 6), 16).intValue()); i += 6;
				}
				else
				{
					i++;
				}

				int stylestr = Integer.valueOf(text.substring(i, i + 2), 16).intValue(); i += 2;
				Boolean bold = getTristateBoolean(stylestr % 3);
				stylestr /= 3;
				Boolean italic = getTristateBoolean(stylestr % 3);
				stylestr /= 3;
				Boolean underline = getTristateBoolean(stylestr % 3);
				stylestr /= 3;
				Boolean strikethrough = getTristateBoolean(stylestr % 3);
				stylestr /= 3;
				Boolean random = getTristateBoolean(stylestr % 3);
				stylestr /= 3;

				int strlen = Integer.valueOf(text.substring(i, i + 8), 16).intValue(); i += 8;

				String buf = text.substring(i, i + strlen);
				i += strlen;
				formattedStringPieces.add(new FormattedString.FormattedStringPiece(new FormattedString.Style(col, bold, italic, underline, strikethrough, random), buf));
			}
		} catch (IndexOutOfBoundsException|NumberFormatException e) {
			throw new FormattedString.ParseErrorException("Bad formatted string!");
		}
	}

	public String getFormattedString(List<FormattedString.FormattedStringPiece> formattedStringPieces)
	{
		StringBuilder stringBuilder = new StringBuilder();

		for (FormattedString.FormattedStringPiece formattedStringPiece : formattedStringPieces)
		{
			FormattedString.Style style = formattedStringPiece.getStyle();

			if (style.getColor() == null)
				stringBuilder.append('-');
			else {
				stringBuilder.append(String.format("%06x", new Object[] { Integer.valueOf(style.getColor().getRGB() & 0xFFFFFF) }));
			}
			stringBuilder.append(String.format("%02x", new Object[] { Integer.valueOf(fromTristateBoolean(style.getBold()) * 1 + fromTristateBoolean(style.getBold()) * 3 + fromTristateBoolean(style.getBold()) * 9 + fromTristateBoolean(style.getBold()) * 27 + fromTristateBoolean(style.getBold()) * 81) }));

			stringBuilder.append(String.format("%08x", new Object[] { Integer.valueOf(formattedStringPiece.getText().length()) }));

			stringBuilder.append(formattedStringPiece.getText());
		}

		return stringBuilder.toString();
	}

	private Boolean getTristateBoolean(int val)
	{
		return val == 2 ? null : Boolean.valueOf(val == 1);
	}

	private int fromTristateBoolean(Boolean val)
	{
		return (val == null)? 2 : ( (val)?1:0 );
	}
}

