/**
 * IBM Confidential
 * OCO Source Materials
 * (C) Copyright IBM Corp. 2010, 2014
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */

package com.ibm.bi.dml.yarn.ropt;

import java.util.ArrayList;

import com.ibm.bi.dml.runtime.DMLRuntimeException;
import com.ibm.bi.dml.runtime.controlprogram.ProgramBlock;

public class GridEnumerationEqui extends GridEnumeration
{
	
	public static final int DEFAULT_NSTEPS = 15;

	private int _nsteps = -1;
	
	public GridEnumerationEqui( ArrayList<ProgramBlock> prog, long min, long max ) 
		throws DMLRuntimeException
	{
		super(prog, min, max);
		
		_nsteps = DEFAULT_NSTEPS;
	}
	
	/**
	 * 
	 * @param steps
	 */
	public void setNumSteps( int steps )
	{
		_nsteps = steps;
	}
	
	@Override
	public ArrayList<Long> enumerateGridPoints() 
	{
		ArrayList<Long> ret = new ArrayList<Long>();
		long gap = (_max - _min) / (_nsteps-1);
		long v = _min;
		for (int i = 0; i < _nsteps; i++) {
			ret.add(v);
			v += gap;
		}
		return ret;
	}
}
