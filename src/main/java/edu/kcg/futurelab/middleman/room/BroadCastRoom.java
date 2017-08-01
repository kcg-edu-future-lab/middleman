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
package edu.kcg.futurelab.middleman.room;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.websocket.Session;

import edu.kcg.futurelab.middleman.Room;

public class BroadCastRoom implements Room{
	@Override
	public synchronized void add(Session session) {
		sessions.add(session);
	}
	@Override
	public synchronized void remove(Session session) {
		sessions.remove(session);
	}
	@Override
	public synchronized void onMessage(Session sender, String message) {
		for(Session s : sessions){
			try {
				s.getBasicRemote().sendText(message);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	private Set<Session> sessions = new LinkedHashSet<>();
}
