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

package com.ibm.bi.dml.runtime.instructions.spark;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.function.PairFunction;

import scala.Tuple2;

import com.ibm.bi.dml.runtime.DMLRuntimeException;
import com.ibm.bi.dml.runtime.DMLUnsupportedOperationException;
import com.ibm.bi.dml.runtime.controlprogram.context.ExecutionContext;
import com.ibm.bi.dml.runtime.controlprogram.context.SparkExecutionContext;
import com.ibm.bi.dml.runtime.functionobjects.OffsetColumnIndex;
import com.ibm.bi.dml.runtime.instructions.Instruction;
import com.ibm.bi.dml.runtime.instructions.InstructionUtils;
import com.ibm.bi.dml.runtime.instructions.cp.CPOperand;
import com.ibm.bi.dml.runtime.matrix.MatrixCharacteristics;
import com.ibm.bi.dml.runtime.matrix.data.MatrixBlock;
import com.ibm.bi.dml.runtime.matrix.data.MatrixIndexes;
import com.ibm.bi.dml.runtime.matrix.operators.Operator;
import com.ibm.bi.dml.runtime.matrix.operators.ReorgOperator;

public class AppendGAlignedSPInstruction extends BinarySPInstruction
{
	
	public AppendGAlignedSPInstruction(Operator op, CPOperand in1, CPOperand in2, CPOperand in3, CPOperand out, String opcode, String istr)
	{
		super(op, in1, in2, out, opcode, istr);
		_sptype = SPINSTRUCTION_TYPE.GAppend;
	}
	
	public static Instruction parseInstruction ( String str ) 
		throws DMLRuntimeException
	{
		//4 parts to the instruction besides opcode and execlocation
		//two input args, one output arg and offset = 4
		InstructionUtils.checkNumFields ( str, 4 );
		
		String[] parts = InstructionUtils.getInstructionPartsWithValueType(str);
		String opcode = parts[0];
		CPOperand in1 = new CPOperand(parts[1]);
		CPOperand in2 = new CPOperand(parts[2]);
		CPOperand in3 = new CPOperand(parts[3]);
		CPOperand out = new CPOperand(parts[4]);
		
		if(!opcode.equalsIgnoreCase("galignedappend"))
			throw new DMLRuntimeException("Unknown opcode while parsing a AppendGSPInstruction: " + str);
		else
			return new AppendGAlignedSPInstruction(new ReorgOperator(OffsetColumnIndex.getOffsetColumnIndexFnObject(-1)), 
										   in1, in2, in3, out, opcode, str);
	}
	
	@Override
	public void processInstruction(ExecutionContext ec)
		throws DMLUnsupportedOperationException, DMLRuntimeException 
	{
		// general case append (map-extend, aggregate)

		SparkExecutionContext sec = (SparkExecutionContext)ec;
		MatrixCharacteristics mc1 = sec.getMatrixCharacteristics(input1.getName());
		MatrixCharacteristics mc2 = sec.getMatrixCharacteristics(input2.getName());
		
		if(!mc1.dimsKnown() || !mc2.dimsKnown()) {
			throw new DMLRuntimeException("The dimensions unknown for inputs");
		}
		else if(mc1.getRows() != mc2.getRows()) {
			throw new DMLRuntimeException("The number of rows of inputs should match for append instruction");
		}
		else if(mc1.getRowsPerBlock() != mc2.getRowsPerBlock() || mc1.getColsPerBlock() != mc2.getColsPerBlock()) {
			throw new DMLRuntimeException("The block sizes donot match for input matrices");
		}
		
		JavaPairRDD<MatrixIndexes,MatrixBlock> in1 = sec.getBinaryBlockRDDHandleForVariable( input1.getName() );
		JavaPairRDD<MatrixIndexes,MatrixBlock> in2 = sec.getBinaryBlockRDDHandleForVariable( input2.getName() );
		JavaPairRDD<MatrixIndexes,MatrixBlock> out = null;
		
		if(mc1.getCols() % mc1.getColsPerBlock() == 0) {
			// Simple changing of matrix indexes of RHS
			long shiftBy = (long) Math.ceil((double)mc1.getCols() / mc1.getColsPerBlock());
			out = in1.union(
						in2.mapToPair(new ShiftColumnIndex(shiftBy))
					);
		}
		else {
			throw new DMLRuntimeException("Incorrect append instruction when mc1.getCols() % mc1.getColsPerBlock() != 0. Should have used AppendGSP");
		}
		
		//put output RDD handle into symbol table
		updateBinaryAppendOutputMatrixCharacteristics(sec);
		sec.setRDDHandleForVariable(output.getName(), out);
		sec.addLineageRDD(output.getName(), input1.getName());
		sec.addLineageRDD(output.getName(), input2.getName());
	}
	
	/**
	 * 
	 */
	public static class ShiftColumnIndex implements PairFunction<Tuple2<MatrixIndexes,MatrixBlock>, MatrixIndexes,MatrixBlock> 
	{
		private static final long serialVersionUID = -5185023611319654242L;
		
		private long _shiftBy;
		
		public ShiftColumnIndex(long shiftBy) {
			_shiftBy = shiftBy;
		}

		@Override
		public Tuple2<MatrixIndexes, MatrixBlock> call(Tuple2<MatrixIndexes, MatrixBlock> kv) 
			throws Exception 
		{	
			return new Tuple2<MatrixIndexes, MatrixBlock>(new MatrixIndexes(kv._1.getRowIndex(), kv._1.getColumnIndex()+_shiftBy), kv._2);
		}
	}
}