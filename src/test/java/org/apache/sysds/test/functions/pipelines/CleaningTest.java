/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.sysds.test.functions.pipelines;

import org.apache.sysds.common.Types;
import org.apache.sysds.test.AutomatedTestBase;
import org.apache.sysds.test.TestConfiguration;
import org.apache.sysds.test.TestUtils;
import org.junit.Assert;
import org.junit.Test;

public class CleaningTest extends AutomatedTestBase {
	private final static String TEST_NAME = "cleaning";
	private final static String TEST_DIR = "pipelines/";
	protected static final String SCRIPT_DIR = "./scripts/staging/";
	private final static String TEST_CLASS_DIR = TEST_DIR + CleaningTest.class.getSimpleName() + "/";


	protected static final String DATA_DIR = "D:/PhD TU Graz/Pipelines/experiments/scripts/data/";
	protected static final String PARAM_DIR = "./scripts/staging/pipelines/";
	private final static String DATASET = DATA_DIR+ "heart.csv";
	private final static String META = DATA_DIR+ "meta/meta_heart.csv";
	private final static String PARAM = PARAM_DIR+ "param.csv";
	private final static String PRIMITIVES = PARAM_DIR+ "primitives.csv";

	@Override
	public void setUp() {
		addTestConfiguration(TEST_NAME,new TestConfiguration(TEST_CLASS_DIR, TEST_NAME,new String[]{"R"}));
	}

	@Test
	public void testCP() {
		runCleaningTest(Types.ExecMode.SINGLE_NODE);
	}


	private void runCleaningTest(Types.ExecMode et) {
			String HOME = SCRIPT_DIR + TEST_DIR;
			Types.ExecMode modeOld = setExecMode(et);
			try {
				loadTestConfiguration(getTestConfiguration(TEST_NAME));
			fullDMLScriptName = HOME + TEST_NAME + ".dml";

			programArgs = new String[] {"-stats", "-exec", "singlenode", "-args", DATASET, META, PRIMITIVES,
				PARAM, output("R")};

			runTest(true, EXCEPTION_NOT_EXPECTED, null, -1);

			//expected loss smaller than default invocation
			Assert.assertTrue(TestUtils.readDMLBoolean(output("R")));
		}
		finally {
			resetExecMode(modeOld);
		}
	}
}
