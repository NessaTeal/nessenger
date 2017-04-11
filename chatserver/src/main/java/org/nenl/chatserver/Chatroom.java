package org.nenl.chatserver;

import java.util.ArrayList;
import java.util.List;

import org.java_websocket.WebSocket;

public class Chatroom {
	
	protected List<WebSocket> userOutputs;
	
	public Chatroom() {
		userOutputs = new ArrayList<>();
	}
	
	public void addUser(WebSocket ws) {
		userOutputs.add(ws);
	}
	
	public void removeUser(WebSocket ws) {
		userOutputs.remove(ws);
	}
	
	public void writeToChat(String message) {
		for(WebSocket ws : userOutputs) {
			try {
				ws.send(message);
			}
			catch(Exception e) {
				removeUser(ws);
			}
		}
	}
}
