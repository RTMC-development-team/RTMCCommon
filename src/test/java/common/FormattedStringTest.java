package common;

import com.rushteamc.lib.SharedEventBus.SharedEventBus;
import com.rushteamc.lib.SharedEventBus.Subscribe;
import com.rushteamc.plugin.common.Authentication.Authenticator;
import com.rushteamc.plugin.common.FormattedString.FormattedString;
import com.rushteamc.plugin.common.FormattedString.FormattedString.FormattedStringPiece;
import com.rushteamc.plugin.common.FormattedString.FormattedString.ParseErrorException;
import com.rushteamc.plugin.common.FormattedString.FormattedString.Style;
import com.rushteamc.plugin.common.FormattedString.StringFormatter;
import java.awt.Color;
import java.io.PrintStream;
import java.util.List;
import org.junit.Test;

public class FormattedStringTest
{
	int continueCounter = 0;

	@Test
	public void formattedStringTest()
	{
		byte[] testHash = Authenticator.stringToHash("c6001d5b2ac3df314204a8f9d7a00e1503c9aba0fd4538645de4bf4cc7e2555cfe9ff9d0236bf327ed3e907849a98df4d330c4bea551017d465b4c1d9b80bcb0");
		System.out.println("c6001d5b2ac3df314204a8f9d7a00e1503c9aba0fd4538645de4bf4cc7e2555cfe9ff9d0236bf327ed3e907849a98df4d330c4bea551017d465b4c1d9b80bcb0");
		System.out.println(Authenticator.hashToString(testHash));

		FormattedString.addFormatter("format1", new StringFormatter1());
		FormattedString.addFormatter("format2", new StringFormatter2());
		try
		{
			FormattedString test = new FormattedString("save", "203040f200000007Knight ");
			System.out.println("Result: " + test.toString("save"));
		} catch (FormattedString.ParseErrorException e) {
			e.printStackTrace();
		}
		try
		{
			FormattedString format = new FormattedString("format1", "&f[&9$3&f][&9$4&f][&9$1&f]: $2");
			FormattedString text1 = new FormattedString("format1", "text1");
			FormattedString text2 = new FormattedString("format1", "text2");
			FormattedString text3 = new FormattedString("format1", "text3");
			FormattedString text4 = new FormattedString("format1", "text4");
			System.out.println(format.toString());
			FormattedString result = FormattedString.Format(format, new FormattedString[] { text1, text2, text3, text4 });
			System.out.println(result.toString("format1"));
		} catch (FormattedString.ParseErrorException e2) {
			e2.printStackTrace();
		}

		try
		{
			FormattedString format = new FormattedString("format1", "&4[$1&4]: $6");

			FormattedString text1 = new FormattedString("format1", "text1");
			FormattedString text2 = new FormattedString();
			FormattedString text3 = new FormattedString();
			FormattedString text4 = new FormattedString("format1", "text4");
			FormattedString text5 = new FormattedString("format1", "text3");
			FormattedString text6 = new FormattedString("format1", "text4");

			FormattedString result = FormattedString.Format(format, new FormattedString[] { text1, text2, text3, text4, text5, text6 });
			System.out.println(result.toString("format1"));
		} catch (FormattedString.ParseErrorException e) {
			e.printStackTrace();
		}

		FormattedString formattedString1;
		try
		{
			formattedString1 = new FormattedString("format1", "&1This &8is a test string!");
		}
		catch (FormattedString.ParseErrorException e1)
		{
			e1.printStackTrace();
			return;
		}
		SharedEventBus eventbus = new SharedEventBus();
		eventbus.addHandler(new SharedEventBusHandler());
		eventbus.postEvent(formattedString1);

		while (this.continueCounter < 1)
			try {
				Thread.sleep(10L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	}

	public class SharedEventBusHandler {
		public SharedEventBusHandler() {
		}

		@Subscribe(instanceOf=true)
		public void onFormattedString(FormattedString formattedString) {
			System.out.println(formattedString.toString());
			System.out.println(formattedString.toString("format2"));
			System.out.println(formattedString.toString("format1"));

			FormattedStringTest.this.continueCounter += 1;
		}
	}

	public static class StringFormatter1 implements StringFormatter
	{
		private static final FormattedString.Style defaultStyle = new FormattedString.Style(Color.white, Boolean.valueOf(false), Boolean.valueOf(false), Boolean.valueOf(false), Boolean.valueOf(false), Boolean.valueOf(false));

		public void append(List<FormattedString.FormattedStringPiece> formattedStringPieces, String text) throws FormattedString.ParseErrorException
		{
			int i = 0;
			int startPiece = 0;
			boolean foundFormatter = false;
			FormattedString.Style style = new FormattedString.Style();
			while (i < text.length())
			{
				if (text.charAt(i) == '&')
				{
					foundFormatter = true;
					i++;
				}
				else if (foundFormatter)
				{
					foundFormatter = false;

					if (startPiece < i - 1)
					{
						FormattedString.FormattedStringPiece formattedStringPiece = new FormattedString.FormattedStringPiece(style, text.substring(startPiece, i - 1));
						formattedStringPieces.add(formattedStringPiece);
						style = new FormattedString.Style();
					}

					char chr = text.charAt(i);
					if (('0' <= chr) && (chr <= 'f'))
					{
						int scale = Integer.parseInt(String.valueOf(chr), 16);
						scale = scale << 4 | scale;
						scale = scale << 16 | scale << 8 | scale;
						style.setColor(new Color(scale));
					}
					else if (chr == 'g')
					{
						style.setBold(Boolean.valueOf(true));
					}
					else if (chr == 'h')
					{
						style.setItalic(Boolean.valueOf(true));
					}
					else if (chr == 'r')
					{
						style.setBold(Boolean.valueOf(false));
						style.setItalic(Boolean.valueOf(false));
					}

					i++;
					startPiece = i;
				}
				else
				{
					i++;
				}
			}
			if (startPiece < i - 1)
			{
				FormattedString.FormattedStringPiece formattedStringPiece = new FormattedString.FormattedStringPiece(style, text.substring(startPiece, i));
				formattedStringPieces.add(formattedStringPiece);
			}
		}

		public String getFormattedString(List<FormattedString.FormattedStringPiece> formattedStringPieces)
		{
			FormattedString.Style currentStyle = defaultStyle;
			StringBuilder stringBuilder = new StringBuilder();
			for (FormattedString.FormattedStringPiece formattedStringPiece : formattedStringPieces)
			{
				FormattedString.Style style = formattedStringPiece.getStyle();

				if (style.getColor() != null)
				{
					stringBuilder.append('&');
					stringBuilder.append(Integer.toHexString((style.getColor().getBlue() + style.getColor().getRed() + style.getColor().getBlue()) / 48).charAt(0));
				}

				if (style.getBold() != null) {
					if ((style.getBold().booleanValue()) && (!currentStyle.getBold().booleanValue()))
					{
						stringBuilder.append('&');
						stringBuilder.append('g');
						currentStyle.setBold(Boolean.valueOf(true));
					}
					else if ((!style.getBold().booleanValue()) && (currentStyle.getBold().booleanValue()))
					{
						stringBuilder.append('&');
						stringBuilder.append('r');
						currentStyle.setBold(Boolean.valueOf(false));
						printCurrent(stringBuilder, currentStyle);
					}
				}
				if (style.getItalic() != null) {
					if ((style.getItalic().booleanValue()) && (!currentStyle.getItalic().booleanValue()))
					{
						stringBuilder.append('&');
						stringBuilder.append('h');
						currentStyle.setItalic(Boolean.valueOf(true));
					}
					else if ((!style.getItalic().booleanValue()) && (currentStyle.getItalic().booleanValue()))
					{
						stringBuilder.append('&');
						stringBuilder.append('r');
						currentStyle.setItalic(Boolean.valueOf(false));
						printCurrent(stringBuilder, currentStyle);
					}
				}
				stringBuilder.append(formattedStringPiece.getText());
			}

			return stringBuilder.toString();
		}

		private void printCurrent(StringBuilder stringBuilder, FormattedString.Style style)
		{
			if (style.getBold().booleanValue())
			{
				stringBuilder.append('&');
				stringBuilder.append('g');
			}

			if (style.getItalic().booleanValue())
			{
				stringBuilder.append('&');
				stringBuilder.append('h');
			}
		}
	}

	public static class StringFormatter2 implements StringFormatter
	{
		public void append(List<FormattedString.FormattedStringPiece> formattedStringPieces, String text) throws FormattedString.ParseErrorException
		{
			formattedStringPieces.add(new FormattedString.FormattedStringPiece(new FormattedString.Style(), text));
		}

		public String getFormattedString(List<FormattedString.FormattedStringPiece> formattedStringPieces)
		{
			String str = "";
			for (FormattedString.FormattedStringPiece formattedStringPiece : formattedStringPieces)
				str = str + formattedStringPiece.getText();
			return str;
		}
	}
}

