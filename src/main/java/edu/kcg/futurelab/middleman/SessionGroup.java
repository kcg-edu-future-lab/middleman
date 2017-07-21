package edu.kcg.futurelab.middleman;

import javax.websocket.Session;

public interface SessionGroup {
	void add(Session session);
	void remove(Session session);
	void onMessage(Session sender, String message);
}
