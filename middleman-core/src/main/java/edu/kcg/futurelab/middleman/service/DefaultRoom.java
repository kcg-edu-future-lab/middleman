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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.websocket.Session;

import edu.kcg.futurelab.middleman.Room;
import jp.go.nict.langrid.commons.io.FileUtil;

public class DefaultRoom implements Room{
	public DefaultRoom(String roomId) {
		this.roomId = roomId;
	}

	@Override
	public synchronized void onOpen(Session session) {
		sessions.add(session);
		if(sessions.size() == 1) {
			onRoomStarted();
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

	@Override
	public synchronized void onMessage(Session session, String message) {
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

	private Set<Session> sessions = new LinkedHashSet<>();
}
