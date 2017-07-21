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

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/rooms/{roomName}")
public class Endpoint {
	@OnOpen
	public void onOpen(Session session, @PathParam("roomName") String roomName) {
		SessionManager.instance().getGroup(roomName).add(session);
	}

	@OnClose
	public void onClose(Session session, @PathParam("roomName") String roomName) {
		SessionManager.instance().getGroup(roomName).remove(session);
	}

	@OnMessage
	public void onMessage(Session session, @PathParam("roomName") String roomName, String text) {
		SessionManager.instance().getGroup(roomName).onMessage(session, text);
	}
}
