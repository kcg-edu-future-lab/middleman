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

import edu.kcg.futurelab.middleman.service.DefaultService;
import edu.kcg.futurelab.middleman.service.StandardService;

@ServerEndpoint("/sessions/{serviceId}/{roomId}")
public class WebsocketServer {
	@OnOpen
	public void onOpen(
			Session session,
			@PathParam("serviceId") String serviceId,
			@PathParam("roomId") String roomId) {
		getService(serviceId).onOpen(roomId, session);
	}

	@OnClose
	public void onClose(
			Session session,
			@PathParam("serviceId") String serviceId,
			@PathParam("roomId") String roomId) {
		if(getService(serviceId).onClose(roomId, session)) {
			services.remove(serviceId);
		}
	}

	@OnMessage(maxMessageSize = 8192*1024)
	public void onMessage(
			Session session,
			@PathParam("serviceId") String serviceId,
			@PathParam("roomId") String roomId,
			String message) {
		getService(serviceId).onMessage(roomId, session, message);
	}

	protected Service getService(String serviceId){
		return services.computeIfAbsent(serviceId, this::newService);
	}

	protected Service newService(String serviceId){
		switch(serviceId) {
		case "standard": return new StandardService(serviceId);
		default: return new DefaultService(serviceId);
		}
	}

	private static Map<String, Service> services = new ConcurrentHashMap<>();
}
