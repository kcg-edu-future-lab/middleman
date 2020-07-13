/*
 * Copyright 2020 Takao Nakaguchi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.kcg.futurelab.middleman.service;

import edu.kcg.futurelab.middleman.Room;

public class StandardService extends DefaultService {
	public StandardService(String serviceId) {
		super(serviceId);
	}

	protected Room newRoom(String roomId){
		return new StandardRoom(roomId);
	}
}
