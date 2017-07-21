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
