package org.nenl;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class Chatroom {
	
	String name;

	Map<String, PrintWriter> userOutputs;
	
	public Chatroom(String name) {
		this.name = name;
		
		userOutputs = new HashMap<>();
	}
	
	public void addUser(String userName, PrintWriter out) {
		userOutputs.put(userName, out);
	}
	
	public void removeUser(String userName) {
		userOutputs.remove(userName);
	}
	
	public void writeToChat(String message) {
		for(PrintWriter out : userOutputs.values()) {
			out.println(message);
		}
	}
}
