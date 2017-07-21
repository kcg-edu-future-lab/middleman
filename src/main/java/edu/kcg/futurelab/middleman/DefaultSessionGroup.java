package edu.kcg.futurelab.middleman;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.websocket.Session;

public class DefaultSessionGroup implements SessionGroup{
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
