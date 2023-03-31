/*
 * Copyright (c) 2012-2023 Felix Kirchmann.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package com.mtreader;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 
 * @author Felix Kirchmann
 */
public class Func
{
	public static String toString(final Object object)
	{
		if (object == null) { return "null"; }
		if (object instanceof String)
		{
			return (String) object;
		}
		else if (object instanceof Throwable)
		{
			final StringWriter stringWriter = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(stringWriter);
			((Throwable) object).printStackTrace(printWriter);
			return stringWriter.toString();
		}
		else if (object instanceof byte[])
		{
			final byte[] array = (byte[]) object;
			final StringBuilder sb = new StringBuilder();
			sb.append('{');
			for (int i = 0; i < array.length; i++)
			{
				if (i > 0)
				{
					sb.append(", ");
				}
				sb.append("0x");
				sb.append(Integer.toHexString(array[i]).toUpperCase());
			}
			sb.append('}');
			return sb.toString();
		}
		else
		{
			return object.toString();
		}
	}
}
