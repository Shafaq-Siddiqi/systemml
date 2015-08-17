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

package com.ibm.bi.dml.test.integration.functions.reorg;

import java.util.HashMap;
import java.util.Random;

import org.junit.Test;

import com.ibm.bi.dml.api.DMLScript;
import com.ibm.bi.dml.api.DMLScript.RUNTIME_PLATFORM;
import com.ibm.bi.dml.runtime.matrix.data.MatrixValue.CellIndex;
import com.ibm.bi.dml.test.integration.AutomatedTestBase;
import com.ibm.bi.dml.test.integration.TestConfiguration;
import com.ibm.bi.dml.test.utils.TestUtils;

public class DiagV2MTest extends AutomatedTestBase
{
	
	private final static String TEST_DIR = "functions/reorg/";

	private final static double epsilon=0.0000000001;
	private final static int rows = 1059;
	private final static int min=0;
	private final static int max=100;
	
	@Override
	public void setUp() {
		addTestConfiguration("DiagV2MTest", new TestConfiguration(TEST_DIR, "DiagV2MTest", 
				new String[] {"C"}));
	}
	
	public void commonReorgTest(RUNTIME_PLATFORM platform)
	{
		TestConfiguration config = getTestConfiguration("DiagV2MTest");
	    
		RUNTIME_PLATFORM prevPlfm=rtplatform;
		
	    rtplatform = platform;
		boolean sparkConfigOld = DMLScript.USE_LOCAL_SPARK_CONFIG;
		if( rtplatform == RUNTIME_PLATFORM.SPARK )
			DMLScript.USE_LOCAL_SPARK_CONFIG = true;

		try {
	        config.addVariable("rows", rows);
	          
			/* This is for running the junit test the new way, i.e., construct the arguments directly */
			String RI_HOME = SCRIPT_DIR + TEST_DIR;
			fullDMLScriptName = RI_HOME + "DiagV2MTest" + ".dml";
			programArgs = new String[]{"-explain", "-args",  RI_HOME + INPUT_DIR + "A" , 
					Long.toString(rows), RI_HOME + OUTPUT_DIR + "C" };
			fullRScriptName = RI_HOME + "DiagV2MTest" + ".R";
			rCmd = "Rscript" + " " + fullRScriptName + " " + 
			       RI_HOME + INPUT_DIR + " "+ RI_HOME + EXPECTED_DIR;
	
			Random rand=new Random(System.currentTimeMillis());
			loadTestConfiguration(config);
			double sparsity=0.599200924665577;//rand.nextDouble();
			double[][] A = getRandomMatrix(rows, 1, min, max, sparsity, 1397289950533L); // System.currentTimeMillis()
	        writeInputMatrix("A", A, true);
	        sparsity=rand.nextDouble();   
			
	        //boolean exceptionExpected = false;
			//int expectedNumberOfJobs = 12;
			//runTest(exceptionExpected, null, expectedNumberOfJobs);
	        boolean exceptionExpected = false;
			int expectedNumberOfJobs = -1;
			runTest(true, exceptionExpected, null, expectedNumberOfJobs);
			
			runRScript(true);
			//disableOutAndExpectedDeletion();
		
			for(String file: config.getOutputFiles())
			{
				HashMap<CellIndex, Double> dmlfile = readDMLMatrixFromHDFS(file);
				HashMap<CellIndex, Double> rfile = readRMatrixFromFS(file);
			//	System.out.println(file+"-DML: "+dmlfile);
			//	System.out.println(file+"-R: "+rfile);
				TestUtils.compareMatrices(dmlfile, rfile, epsilon, file+"-DML", file+"-R");
			}
		}
		finally {
			rtplatform = prevPlfm;
			DMLScript.USE_LOCAL_SPARK_CONFIG = sparkConfigOld;
		}
	}
	
	@Test
	public void testDiagV2MMR() {
		commonReorgTest(RUNTIME_PLATFORM.HADOOP);
	}   
	
	@Test
	public void testDiagV2MCP() {
		commonReorgTest(RUNTIME_PLATFORM.SINGLE_NODE);
	}
	
	@Test
	public void testDiagV2MSP() {
		commonReorgTest(RUNTIME_PLATFORM.SPARK);
	}
}

