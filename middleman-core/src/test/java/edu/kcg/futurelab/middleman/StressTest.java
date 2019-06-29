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

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CountDownLatch;

import javax.websocket.ClientEndpoint;
import javax.websocket.ContainerProvider;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

@ClientEndpoint
public class StressTest {
	public static void main(String[] args) throws Throwable{
		int clientCount = 100;
		int messageCount = 100;
		Object gate = new Object();
		CountDownLatch goal = new CountDownLatch(clientCount);
		WebSocketContainer container =
				ContainerProvider.getWebSocketContainer();
		for(int i = 0; i < clientCount; i++){
			container.connectToServer(
					new StressTest(gate, goal, clientCount, messageCount),
					new URI("ws://localhost:8080/middleman/default/lskro4jr"));
		}
		synchronized(gate){
			gate.notifyAll();
		}
		goal.await();
		System.exit(0);
	}

	private Session session;
	private CountDownLatch goal;
	private int clientCount;
	private int messageCount;

	public StressTest(Object gate, CountDownLatch goal, int clientCount, int messageCount) {
		this.goal = goal;
		this.clientCount = clientCount;
		this.messageCount = messageCount;
		new Thread(() -> {
			synchronized(gate){
				try {
					gate.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			try {
				for(int i = 0; i < messageCount; i++){
					this.session.getBasicRemote().sendText("hello");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();
	}

	@OnOpen
	public void onOpen(Session session){
		this.session = session;
	}

	@OnClose
	public void onClose(Session session){
		goal.countDown();
	}

	@OnError
	public void onError(Session session, Throwable exception){
		exception.printStackTrace();
	}

	@OnMessage
	public void onMessage(Session session, String text) throws IOException{
		recvCount++;
		if(recvCount >= messageCount * clientCount){
			System.out.println(session.getId() + ": all messages received.");
			session.close();
		}
	}
	private int recvCount;
}
