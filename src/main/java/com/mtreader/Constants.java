/*
 * Copyright (c) 2012-2023 Felix Kirchmann.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package com.mtreader;

import java.nio.charset.Charset;

/**
 * 
 * @author Felix Kirchmann
 */
public class Constants
{
	public static final String	productName				= "MultimeterReader";
	
	public static final int[]	connectWindowSize		= new int[] { 250, 77 };
	public static final int[]	displayWindowSize		= new int[] { 336, 91 };
	public static final int[]	datalogWindowSize		= new int[] { 190, 300 };
	public static final int[]	datalogSetupWindowSize	= new int[] { 400, 82 };
	public static final int[]	messageBoxSize			= new int[] { 160, 80 };
	
	public static final Charset	charset					= Charset.forName("UTF-8");
}
