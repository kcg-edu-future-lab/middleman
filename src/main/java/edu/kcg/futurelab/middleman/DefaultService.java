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
package edu.kcg.futurelab.middleman;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import edu.kcg.futurelab.middleman.room.BroadCastRoom;

@ServerEndpoint("/default/{roomId}")
public class DefaultService {
	@OnOpen
	public void onOpen(Session session,
			@PathParam("roomId") String roomId) {
		getRoom(roomId).add(session);
	}

	@OnClose
	public void onClose(Session session, @PathParam("roomId") String roomId) {
		getRoom(roomId).remove(session);
	}

	@OnMessage
	public void onMessage(Session session, @PathParam("roomId") String roomId,
			String message) {
		getRoom(roomId).onMessage(session, message);
	}

	protected Room getRoom(String roomId){
		return groups.computeIfAbsent(roomId, this::newRoom);
	}

	protected Room newRoom(String roomId){
		return new BroadCastRoom();
	}

	private static Map<String, Room> groups = new ConcurrentHashMap<>();
}
