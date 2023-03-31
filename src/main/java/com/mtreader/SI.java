/*
 * Copyright (c) 2012-2023 Felix Kirchmann.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package com.mtreader;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 
 * @author Felix Kirchmann
 */
public abstract class SI
{
	public static BigDecimal convert(final BigDecimal number, final SI.Prefix from, final SI.Prefix to)
	{
		if (number == null) { throw new NullPointerException(); }
		if (from == to) { return number; }
		return number.multiply(from.factorBD).divide(to.factorBD, RoundingMode.HALF_UP).stripTrailingZeros();
	}
	
	public static enum Prefix
	{
		// @formatter:off
		
		giga  (1e+9, "G"     ),
		mega  (1e+6, "M"     ),
		kilo  (1e+3, "k"     ),
		none  (1e+0, ""      ),
		milli (1e-3, "m"     ),
		micro (1e-6, "\u03bc"), // lowercase greek mu
		nano  (1e-9, "n"     );
		
		public final double		factor;
		public final BigDecimal	factorBD;
		public final String		symbol;
		
		Prefix(final double factor, final String symbol)
		{
			this.factor   = factor;
			this.factorBD = BigDecimal.valueOf(factor);
			this.symbol   = symbol;
		}
		
		@Override
		public String toString()
		{
			if(this == SI.Prefix.none)
			{
				return this.name();
			}
			else
			{
				return this.name() + " (" + this.symbol + ")";
			}
		}

		// @formatter:on
	}
}
