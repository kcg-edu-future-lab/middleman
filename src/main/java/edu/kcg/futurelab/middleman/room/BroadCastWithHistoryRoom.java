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
import java.util.Deque;
import java.util.LinkedList;

import javax.websocket.Session;

public class BroadCastWithHistoryRoom extends BroadCastRoom{
	@Override
	public synchronized void add(Session session) {
		synchronized(session){
			try {
				for(String m : log){
					session.getBasicRemote().sendText(m);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		super.add(session);
	}

	@Override
	public synchronized void onMessage(Session sender, String message) {
		if(log.size() == 100) log.pollFirst();
		log.offerLast(message);
		super.onMessage(sender, message);
	}

	private Deque<String> log = new LinkedList<>();
}
