/*
 * Copyright 2017 Takao Nakaguchi
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
package edu.kcg.futurelab.middleman.sample;

import edu.kcg.futurelab.middleman.Room;
import edu.kcg.futurelab.middleman.service.BroadCastWithHistoryRoom;
import edu.kcg.futurelab.middleman.service.DefaultService;

public class SimplePaintService extends DefaultService{
	public SimplePaintService(String serviceId) {
		super(serviceId);
	}
	@Override
	protected Room newRoom(String roomId) {
		return new BroadCastWithHistoryRoom(100);
	}
}
