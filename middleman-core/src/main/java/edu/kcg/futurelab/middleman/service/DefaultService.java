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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.websocket.Session;

import edu.kcg.futurelab.middleman.Room;
import edu.kcg.futurelab.middleman.Service;

public class DefaultService implements Service {
	public DefaultService(String serviceId) {
		this.serviceId = serviceId;
		Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
			synchronized(roomTtls) {
				Iterator<Map.Entry<String, Long>> it = roomTtls.entrySet().iterator();
				long cur = System.currentTimeMillis();
				while(it.hasNext()) {
					Map.Entry<String, Long> e = it.next();
					if(cur > e.getValue()) {
						it.remove();
						rooms.remove(e.getKey());
					}
				}
			}
		}, 10000, 10000, TimeUnit.MILLISECONDS);
	}
	private Map<String, Long> roomTtls = new HashMap<>();

	public String getServiceId() {
		return serviceId;
	}

	@Override
	public void onOpen(String roomId, Session session) {
		getRoom(roomId).onSessionOpen(session);
	}

	@Override
	public boolean onClose(String roomId, Session session) {
		long ttl = getRoom(roomId).onSessionClose(session);
		if(ttl == 0) {
			rooms.remove(roomId).onRoomEnded();
		} else if(ttl > 0) {
			roomTtls.put(roomId, System.currentTimeMillis() + ttl);
		}
		return false;
	}

	@Override
	public void onMessage(String roomId, Session session, String message) {
		getRoom(roomId).onSessionMessage(session, message);
	}

	protected Room getRoom(String roomId){
		return rooms.computeIfAbsent(roomId, this::newRoom);
	}

	protected Room newRoom(String roomId){
		Room r = new DefaultRoom(roomId);
		r.onRoomStarted();
		return r;
	}

	private String serviceId;
	private static Map<String, Room> rooms = new ConcurrentHashMap<>();
}
