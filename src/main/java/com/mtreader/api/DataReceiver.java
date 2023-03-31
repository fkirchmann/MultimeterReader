/*
 * Copyright (c) 2012-2023 Felix Kirchmann.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package com.mtreader.api;

/**
 * 
 * @author Felix Kirchmann
 */
public interface DataReceiver
{
	public void onData(Measurement data);
}
