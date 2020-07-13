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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.websocket.RemoteEndpoint.Basic;
import javax.websocket.Session;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.EvictingQueue;

import edu.kcg.futurelab.middleman.Room;
import jp.go.nict.langrid.commons.io.FileUtil;

public class StandardRoom implements Room{
	public StandardRoom(String roomId) {
		this.roomId = roomId;
	}

	@Override
	public synchronized void onOpen(Session session) {
		sessions.add(session);
		if(sessions.size() == 1) {
			onRoomStarted();
		}
		Basic b = session.getBasicRemote();
		for(Collection<String> c : keepInvocations.values()) {
			for(String m : c) {
				try {
					b.sendText(m);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	@Override
	public synchronized boolean onClose(Session session) {
		sessions.remove(session);
		if(sessions.size() == 0) {
			onRoomEnded();
			return true;
		}
		return false;
	}
	private Map<Integer, EvictingQueue<String>> keepInvocations;
	@Override
	public synchronized void onMessage(Session session, String message) {
		JsonNode n;
		try {
			n = om.readValue(message, JsonNode.class);
		} catch(JsonProcessingException e) {
			throw new RuntimeException(e);
		}
		String type = n.get("type").asText();
		JsonNode body = n.get("body");
		if(type.equals("invocationConfig")){
			int targetIndex = body.get("index").asInt();
			JsonNode option = n.get("option");
			String keep = option.get("keep").asText();
			if(keep.equals("log")) {
				keepInvocations.put(targetIndex,
						EvictingQueue.<String>create(option.get("maxLog").asInt(1000)));
			}
		} else if(type.equals("invocation")) {
			int index = body.get("index").asInt();
			keepInvocations.get(index).add(message);
		}
		for(Session s : sessions){
			try {
				roomLog.printf(",%n{\"time\": %d, \"sender\": \"%s\", \"message\": %s}",
						new Date().getTime(), session.getId(), message);
				s.getBasicRemote().sendText(message);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void onRoomStarted() {
		Date now = new Date();
		String dates = new SimpleDateFormat("yyyyMMdd").format(now);
		String times = new SimpleDateFormat("HHmmss").format(now);
		File dir = new File(new File("logs"), dates);
		dir.mkdirs();
		try {
			File f = FileUtil.createUniqueFile(
					dir,
					getClass().getSimpleName() + "-" + roomId + "-" + times + "-", ".json");
			roomLog = new PrintWriter(Files.newBufferedWriter(f.toPath()));
			roomLog.print("[{}");
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void onRoomEnded() {
		roomLog.printf("%n]%n");
		roomLog.close();
	}

	private String roomId;
	private PrintWriter roomLog;
	private ObjectMapper om = new ObjectMapper();

	private Set<Session> sessions = new LinkedHashSet<>();
}
