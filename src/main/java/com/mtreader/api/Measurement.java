/*
 * Copyright (c) 2012-2023 Felix Kirchmann.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package com.mtreader.api;

import java.math.BigDecimal;

import com.mtreader.SI;

/**
 * 
 * @author Felix Kirchmann
 */
public class Measurement
{
	private final BigDecimal	data;
	private final String		unit;
	private final SI.Prefix		prefix;
	
	public Measurement(final BigDecimal data, final String unit, final SI.Prefix prefix)
	{
		if (data == null || unit == null || prefix == null) { throw new NullPointerException(); }
		this.data = data;
		this.unit = unit;
		this.prefix = prefix;
	}
	
	public BigDecimal getData()
	{
		return data;
	}
	
	public String getUnit()
	{
		return unit;
	}
	
	public SI.Prefix getPrefix()
	{
		return prefix;
	}
}