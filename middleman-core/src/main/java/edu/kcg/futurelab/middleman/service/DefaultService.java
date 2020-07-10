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
package edu.kcg.futurelab.middleman.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.Session;

import edu.kcg.futurelab.middleman.Room;
import edu.kcg.futurelab.middleman.Service;

public class DefaultService implements Service {
	public DefaultService(String serviceId) {
		this.serviceId = serviceId;
	}

	public String getServiceId() {
		return serviceId;
	}

	@Override
	public void onOpen(String roomId, Session session) {
		getRoom(roomId).onOpen(session);
	}

	@Override
	public boolean onClose(String roomId, Session session) {
		if(getRoom(roomId).onClose(session)) {
			rooms.remove(roomId);
		}
		return rooms.size() == 0;
	}

	@Override
	public void onMessage(String roomId, Session session, String message) {
		getRoom(roomId).onMessage(session, message);
	}

	protected Room getRoom(String roomId){
		return rooms.computeIfAbsent(roomId, this::newRoom);
	}

	protected Room newRoom(String roomId){
		return new DefaultRoom(roomId);
	}

	private String serviceId;
	private static Map<String, Room> rooms = new ConcurrentHashMap<>();
}
