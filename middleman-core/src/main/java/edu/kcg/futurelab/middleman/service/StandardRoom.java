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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.websocket.RemoteEndpoint.Basic;
import javax.websocket.Session;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

		ObjectNode bulk = om.createObjectNode();
		bulk.put("type", "bulk");
		ArrayNode bbody = om.createArrayNode();
		bulk.set("body", bbody);
		// statesから状態を送信
		System.out.println("states len: " + states.size());
		for(Map.Entry<Integer, String> e : states.entrySet()) {
			System.out.println("states added");
			ObjectNode state = om.createObjectNode();
			bbody.add(state);
			state.put("type", "state");
			ObjectNode sbody = om.createObjectNode();
			state.set("body", sbody);
			sbody.put("objectIndex", e.getKey());
			sbody.put("state", e.getValue());
		}
		for(Collection<JsonNode> c : invocationLogs.values()) {
			if(c.size() == 0) continue;
			for(JsonNode n : c) bbody.add(n);
		}
		try {
			b.sendText(bulk.toString());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public synchronized long onClose(Session session) {
		sessions.remove(session);
		if(sessions.size() == 0) {
			onRoomEnded();
			return 0;
		}
		return -1;
	}
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
		boolean bcast = true;
		switch(type) {
			default:
			case "connectionConfig":{
				System.out.println(message);
				bcast = false;
				break;
			}
			case "objectConfig":{
				System.out.println(message);
				Iterator<JsonNode> it = body.get("methodIdexes").elements();
				Set<Integer> idxs = new LinkedHashSet<>();
				while(it.hasNext()) {
					idxs.add(it.next().asInt());
				}
				objectMethods.put(body.get("objectIndex").asInt(), idxs);
				bcast = false;
				break;
			}
			case "methodConfig":{
				System.out.println(message);
				int targetIndex = body.get("index").asInt();
				JsonNode option = body.get("option");
				String keep = option.get("keep").asText();
				if(keep.equals("log")) {
					invocationLogs.putIfAbsent(targetIndex,
							EvictingQueue.<JsonNode>create(option.get("maxLog").asInt(1000)));
				}
				bcast = false;
				break;
			}
			case "saveState": {
				System.out.println(message);
				int objIndex = body.get("objectIndex").asInt();
				String state = body.get("state").asText();
				states.put(objIndex, state);
				System.out.println(objIndex);
				System.out.println(state);
				for(int mi : objectMethods.get(objIndex)) {
					EvictingQueue<JsonNode> q = invocationLogs.get(mi);
					if(q != null) q.clear();
				}
				bcast = false;
				break;
			}
			case "invocation": {
				int index = body.get("index").asInt();
				EvictingQueue<JsonNode> q = invocationLogs.get(index);
				if(q != null) q.add(n);
				break;
			}
		}
		if(bcast) for(Session s : sessions){
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

	private Map<Integer, EvictingQueue<JsonNode>> invocationLogs = new HashMap<>();
	private Map<Integer, String> states = new LinkedHashMap<>();
	private Map<Integer, Set<Integer>> objectMethods = new HashMap<>();

	private Set<Session> sessions = new LinkedHashSet<>();
}
