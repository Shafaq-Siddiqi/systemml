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

package com.ibm.bi.dml.test.integration.applications;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.ibm.bi.dml.runtime.matrix.MatrixCharacteristics;
import com.ibm.bi.dml.runtime.matrix.data.MatrixValue.CellIndex;
import com.ibm.bi.dml.test.integration.AutomatedTestBase;
import com.ibm.bi.dml.test.integration.TestConfiguration;
import com.ibm.bi.dml.test.utils.TestUtils;


@RunWith(value = Parameterized.class)
public class MDABivariateStatsTest extends AutomatedTestBase 
{

	private final static String TEST_DIR = "applications/mdabivar/";
	private final static String TEST_MDABivar = "MDABivariateStats";
	
	private int n, m, label_index, label_measurement_level;
	
	public MDABivariateStatsTest(int n, int m, int li, int lml) {
		this.n = n; 
		this.m = m; 
		this.label_index = li;
		this.label_measurement_level = lml;
	}
	
	@Parameters
	 public static Collection<Object[]> data() {
	   Object[][] data = new Object[][] { { 10000, 100, 1, 1 }, { 10000, 100, 100, 0}, 
			                              { 100000, 100, 1, 1 }, { 100000, 100, 100, 0}
			   							  };
	   return Arrays.asList(data);
	 }
	 
	@Override
	public void setUp() {
	    setUpBase();
		addTestConfiguration(TEST_MDABivar, 
							 new TestConfiguration(TEST_DIR, TEST_MDABivar, new String[] { "stats" }));
	}
	
	@Test
	public void testMDABivarWithRDMLAndJava() {
		TestConfiguration config = getTestConfiguration(TEST_MDABivar);
		
		String MDABivar_HOME = SCRIPT_DIR + TEST_DIR;
		fullDMLScriptName = MDABivar_HOME + TEST_MDABivar + ".dml";

		programArgs = new String[]{"-args", MDABivar_HOME + INPUT_DIR + "X", 
											Integer.toString(label_index), 
											MDABivar_HOME + INPUT_DIR + "feature_indices", 
											Integer.toString(label_measurement_level), 
											MDABivar_HOME + INPUT_DIR + "feature_measurement_levels",
											MDABivar_HOME + OUTPUT_DIR + "stats", 
                							MDABivar_HOME + OUTPUT_DIR + "tests", 
                							MDABivar_HOME + OUTPUT_DIR + "covariances",
                							MDABivar_HOME + OUTPUT_DIR + "standard_deviations",
                							MDABivar_HOME + OUTPUT_DIR + "contingency_tables_counts",
                							MDABivar_HOME + OUTPUT_DIR + "contingency_tables_label_values",
                							MDABivar_HOME + OUTPUT_DIR + "contingency_tables_feature_values",
                							MDABivar_HOME + OUTPUT_DIR + "feature_values",
                							MDABivar_HOME + OUTPUT_DIR + "feature_counts",
                							MDABivar_HOME + OUTPUT_DIR + "feature_means",
                							MDABivar_HOME + OUTPUT_DIR + "feature_standard_deviations"};
		
		fullRScriptName = MDABivar_HOME + TEST_MDABivar + ".R";
		rCmd = "Rscript" + " " + fullRScriptName + " " + 
		       MDABivar_HOME + INPUT_DIR + " " + Integer.toString(label_index) + " " 
		       + Integer.toString(label_measurement_level) + " " + MDABivar_HOME + EXPECTED_DIR;
		
		loadTestConfiguration(config);

		double[][] X = getRandomMatrix(n, m, 0, 1, 1, System.currentTimeMillis());
		for(int i=0; i<X.length; i++)
			for(int j=m/2; j<X[i].length; j++){
				//generating a 5-valued categorical random variable
				if(X[i][j] < 0.2) X[i][j] = 1;
				else if(X[i][j] < 0.4) X[i][j] = 2; 
				else if(X[i][j] < 0.6) X[i][j] = 3; 
				else if(X[i][j] < 0.8) X[i][j] = 4;
				else X[i][j] = 5;	
			}
		
		double[][] feature_indices = new double[m-1][1];
		double[][] feature_measurement_levels = new double[m-1][1];
		int pos = 0;
		for(int i=1; i<=m; i++)
			if(i != label_index){
				feature_indices[pos][0] = i;
				feature_measurement_levels[pos][0] = (i > m/2) ? 0 : 1;
				pos++;
			}
		
		MatrixCharacteristics mcX = new MatrixCharacteristics(n, m, -1, -1);
		writeInputMatrixWithMTD("X", X, true, mcX);
		
		MatrixCharacteristics mc_features = new MatrixCharacteristics(m-1, 1, -1, -1);
		writeInputMatrixWithMTD("feature_indices", feature_indices, true, mc_features);
		writeInputMatrixWithMTD("feature_measurement_levels", feature_measurement_levels, true, mc_features);
		
		boolean exceptionExpected = false;
		int expectedNumberOfJobs = -1;
		
		runTest(true, exceptionExpected, null, expectedNumberOfJobs); 
		
		runRScript(true);
		disableOutAndExpectedDeletion();

		HashMap<CellIndex, Double> statsDML = readDMLMatrixFromHDFS("stats");
		HashMap<CellIndex, Double> statsR = readRMatrixFromFS("stats");
		
		TestUtils.compareMatrices(statsDML, statsR, 0.000001, "statsDML", "statsR");
	}
}
