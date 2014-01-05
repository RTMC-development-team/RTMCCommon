package com.rushteamc.plugin.common.FormattedString;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FormattedString
	implements Serializable
{
	private static final long serialVersionUID = -8561883821857658682L;
	private static final Map<String, StringFormatter> stringFormatters = new HashMap<String, StringFormatter>();
	protected final List<FormattedStringPiece> formattedStringPieces;

	static
	{
		PlainStringFormatter plainStringFormatter = new PlainStringFormatter();
		stringFormatters.put(null, plainStringFormatter);
		stringFormatters.put("plain", plainStringFormatter);
		stringFormatters.put("save", new SaveStringFormatter());
	}

	public static void addFormatter(String name, StringFormatter formatter)
	{
		stringFormatters.put(name, formatter);
	}

	public static void removeFormatter(String name)
	{
		stringFormatters.remove(name);
	}

	public static void setFormatter(String name, StringFormatter formatter)
	{
		addFormatter(name, formatter);
	}

	public FormattedString()
	{
		this.formattedStringPieces = new ArrayList<FormattedStringPiece>();
	}

	public FormattedString(FormattedString formattedString)
	{
		this();

		for (FormattedStringPiece formattedStringPiece : formattedString.formattedStringPieces)
			this.formattedStringPieces.add(new FormattedStringPiece(new Style(formattedStringPiece.style), formattedStringPiece.text));
	}

	public FormattedString(String str) throws FormattedString.ParseErrorException
	{
		this(null, str);
	}

	public FormattedString(String formatterName, String str) throws FormattedString.ParseErrorException
	{
		this();

		StringFormatter formatter = (StringFormatter)stringFormatters.get(formatterName);

		if (formatter == null) {
			throw new ParseErrorException("No string formatter specified");
		}
		formatter.append(this.formattedStringPieces, str);
	}

	public void append(FormattedString formattedString)
	{
		for (FormattedStringPiece formattedStringPiece : formattedString.formattedStringPieces)
		{
			this.formattedStringPieces.add(formattedStringPiece);
		}
	}

	public void append(String formatterName, String str) throws FormattedString.ParseErrorException
	{
		StringFormatter formatter = (StringFormatter)stringFormatters.get(formatterName);

		if (formatter == null) {
			throw new ParseErrorException("No string formatter specified");
		}
		formatter.append(this.formattedStringPieces, str);
	}

	public void replace(String patern, FormattedString replacement)
	{
		if (replacement == null) {
			replacement = new FormattedString();
		}
		int ind = 0;
		int paternStart = -1;
		int paternStartPiece = 0;
		for (int piece = 0; piece < this.formattedStringPieces.size(); piece++)
		{
			String text = ((FormattedStringPiece)this.formattedStringPieces.get(piece)).getText();
			for (int i = 0; i < text.length(); i++)
			{
				if (text.charAt(i) == patern.charAt(ind))
				{
					ind++;
					if (ind == patern.length())
					{
						if (paternStart + 1 == ((FormattedStringPiece)this.formattedStringPieces.get(paternStartPiece)).getText().length())
						{
							paternStart = -1;
							paternStartPiece++;
						}

						FormattedStringPiece startPiece = (FormattedStringPiece)this.formattedStringPieces.get(paternStartPiece);
						String startText = startPiece.getText();

						int shift = 1 - (piece - paternStartPiece) + replacement.formattedStringPieces.size();

						if (shift > 0)
						{
							for (int n = 0; n < shift; n++)
								this.formattedStringPieces.add(null);
							for (int n = this.formattedStringPieces.size() - 1; n > piece + shift - 1; n--)
								this.formattedStringPieces.set(n, (FormattedStringPiece)this.formattedStringPieces.get(n - shift));
						}
						else
						{
							for (int n = 0; n < this.formattedStringPieces.size(); n++)
								this.formattedStringPieces.set(n + shift, (FormattedStringPiece)this.formattedStringPieces.get(n));
							for (int n = 0; n > shift; n--) {
								this.formattedStringPieces.remove(n);
							}
						}
						startPiece.text = startText.substring(0, paternStart + 1);

						for (int n = 0; n < replacement.formattedStringPieces.size(); n++)
						{
							this.formattedStringPieces.set(n + paternStartPiece + 1, (FormattedStringPiece)replacement.formattedStringPieces.get(n));
						}

						if (piece == paternStartPiece)
						{
							if (startText.length() > i + 1)
							{
								this.formattedStringPieces.set(paternStartPiece + shift, new FormattedStringPiece(new Style(), startText.substring(i + 1)));
							}
							else
							{
								for (int n = paternStartPiece + shift; n < this.formattedStringPieces.size() - 1; n++)
									this.formattedStringPieces.set(n, (FormattedStringPiece)this.formattedStringPieces.get(n + 1));
								this.formattedStringPieces.remove(this.formattedStringPieces.size() - 1);
							}
						}
						else
						{
							FormattedStringPiece p = (FormattedStringPiece)this.formattedStringPieces.get(piece + shift);
							if (p.getText().length() > i + 1) {
								p.text = p.getText().substring(i + 1);
							}
							else
							{
								for (int n = paternStartPiece + shift + 1; n < this.formattedStringPieces.size() - 2; n++)
									this.formattedStringPieces.set(n, (FormattedStringPiece)this.formattedStringPieces.get(n + 1));
								this.formattedStringPieces.remove(this.formattedStringPieces.size() - 1);
							}
						}

						piece = paternStartPiece = piece + shift;
						if (piece >= this.formattedStringPieces.size())
							break;
						ind = 0;
						paternStart = i = 0;
						text = ((FormattedStringPiece)this.formattedStringPieces.get(piece)).getText();
					}
				}
				else
				{
					paternStart = i;
					paternStartPiece = piece;
					ind = 0;
				}
			}
		}
	}

	public static FormattedString Format(FormattedString format, FormattedString... args)
	{
		if (format == null) {
			throw new IllegalArgumentException("Argument format cannot be null");
		}
		FormattedString result = new FormattedString(format);

		for (int i = 0; i < args.length; i++)
		{
			result.replace("$" + (i + 1), args[i]);
		}

		return result;
	}

	public String toString()
	{
		return toString(null);
	}

	public String toString(String formatterName)
	{
		if (formatterName == null)
		{
			StringBuilder stringBuilder = new StringBuilder();
			for (FormattedStringPiece formattedStringPiece : this.formattedStringPieces)
			{
				stringBuilder.append(formattedStringPiece.getText());
			}
			return stringBuilder.toString();
		}

		StringFormatter formatter = (StringFormatter)stringFormatters.get(formatterName);

		if (formatter == null) {
			return null;
		}
		return formatter.getFormattedString(this.formattedStringPieces);
	}

	public static class FormattedStringPiece
		implements Serializable
	{
		private static final long serialVersionUID = 8375139066806472694L;
		private String text;
		private FormattedString.Style style;

		public FormattedStringPiece(FormattedString.Style style, String text)
		{
			this.style = style;
			this.text = text;
		}

		public String getText()
		{
			return this.text;
		}

		public FormattedString.Style getStyle()
		{
			return this.style;
		}
	}

	public static class ParseErrorException extends Exception
	{
		private static final long serialVersionUID = 5603185746406852261L;

		public ParseErrorException(String text)
		{
			super();
		}
	}

	public static class Style
		implements Serializable
	{
		private static final long serialVersionUID = 6634877874007700090L;
		private Color color;
		private Boolean bold;
		private Boolean italic;
		private Boolean underline;
		private Boolean strikeThrough;
		private Boolean random;

		public Style()
		{
			this(null, null, null, null, null, null);
		}

		public Style(Style style)
		{
			this(style.color, style.bold, style.italic, style.underline, style.strikeThrough, style.random);
		}

		public Style(Color color, Boolean bold, Boolean italic, Boolean underline, Boolean strikeThrough, Boolean random)
		{
			this.color = color;
			this.bold = bold;
			this.italic = italic;
			this.underline = underline;
			this.strikeThrough = strikeThrough;
			this.random = random;
		}

		public Color getColor()
		{
			return this.color;
		}

		public void setColor(Color color)
		{
			this.color = color;
		}

		public Boolean getBold()
		{
			return this.bold;
		}

		public void setBold(Boolean bold)
		{
			this.bold = bold;
		}

		public Boolean getItalic()
		{
			return this.italic;
		}

		public void setItalic(Boolean italic)
		{
			this.italic = italic;
		}

		public Boolean getUnderline()
		{
			return this.underline;
		}

		public void setUnderline(Boolean underline)
		{
			this.underline = underline;
		}

		public Boolean getStrikeThrough()
		{
			return this.strikeThrough;
		}

		public void setStrikeThrough(Boolean strikeThrough)
		{
			this.strikeThrough = strikeThrough;
		}

		public Boolean getRandom()
		{
			return this.random;
		}

		public void setRandom(Boolean random)
		{
			this.random = random;
		}
	}
}

