package org.nenl;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class Chatroom {
	
	protected String name;

	protected Map<Integer, PrintWriter> userOutputs;
	
	public Chatroom(String name) {
		this.name = name;
		
		userOutputs = new HashMap<>();
	}
	
	public void addUser(int id, PrintWriter out) {
		userOutputs.put(id, out);
	}
	
	public void removeUser(int id) {
		userOutputs.remove(id);
	}
	
	public void writeToChat(String message) {
		for(PrintWriter out : userOutputs.values()) {
			out.println(message);
		}
	}
}
