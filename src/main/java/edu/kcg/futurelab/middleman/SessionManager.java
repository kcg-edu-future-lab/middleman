package edu.kcg.futurelab.middleman;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
	public static SessionManager instance(){
		return instance;
	}
	public SessionGroup getGroup(String groupId){
		return groups.computeIfAbsent(groupId, key -> new DefaultSessionGroup());
	}

	private Map<String, SessionGroup> groups = new ConcurrentHashMap<>();
	private static SessionManager instance = new SessionManager();
}
