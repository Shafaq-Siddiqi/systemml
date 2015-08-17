/**
 * (C) Copyright IBM Corp. 2010, 2015
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package com.ibm.bi.dml.runtime.functionobjects;

import com.ibm.bi.dml.runtime.DMLRuntimeException;
import com.ibm.bi.dml.runtime.matrix.MatrixCharacteristics;
import com.ibm.bi.dml.runtime.matrix.data.MatrixIndexes;
import com.ibm.bi.dml.runtime.matrix.data.MatrixValue.CellIndex;


public class OffsetColumnIndex extends IndexFunction
{

	private static final long serialVersionUID = 1523769994005450946L;

	//private static OffsetColumnIndex singleObj = null;
	private int offset, numRowsInOutput, numColumnsInOutput;
	
	private OffsetColumnIndex(int offset) {
		this.offset = offset;
	}
	
	public static OffsetColumnIndex getOffsetColumnIndexFnObject(int offset) {
		return new OffsetColumnIndex(offset);
		//if ( singleObj == null )
		//	singleObj = new OffsetColumnIndex(offset);
		//return singleObj;
	}
	
	public void setOutputSize(int rows, int columns){
		numRowsInOutput = rows;
		numColumnsInOutput = columns;
	}
	
	public void setOffset(int offset){
		this.offset = offset;
	}
	
	public Object clone() throws CloneNotSupportedException {
		// cloning is not supported for singleton classes
		throw new CloneNotSupportedException();
	}
	
	@Override
	public void execute(MatrixIndexes in, MatrixIndexes out) {
		out.setIndexes(in.getRowIndex(), in.getColumnIndex()+offset);
	}

	@Override
	public void execute(CellIndex in, CellIndex out) {
		out.row=in.row;
		out.column=offset+in.column;
	}

	@Override
	public boolean computeDimension(int row, int col, CellIndex retDim) {
		retDim.set(numRowsInOutput, numColumnsInOutput);
		return false;
	}

	public boolean computeDimension(MatrixCharacteristics in, MatrixCharacteristics out) throws DMLRuntimeException
	{
		out.set(numRowsInOutput, numColumnsInOutput, in.getRowsPerBlock(), in.getColsPerBlock());
		return false;
	}
}
