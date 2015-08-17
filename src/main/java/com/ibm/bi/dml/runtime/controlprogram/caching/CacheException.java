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

package com.ibm.bi.dml.runtime.controlprogram.caching;

import com.ibm.bi.dml.runtime.DMLRuntimeException;

public class CacheException extends DMLRuntimeException
{


	private static final long serialVersionUID = 1L;

	public CacheException ()
	{
		super ("Cache Exception");
	}

	public CacheException (String message)
	{
		super (message);
	}

	public CacheException (Exception cause)
	{
		super (cause);
	}

	public CacheException (String message, Exception cause)
	{
		super (message, cause);
	}

}
