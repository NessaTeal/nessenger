package org.nenl;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class Chatroom {
	
	String name;

	List<String> users = new ArrayList<>();
	List<PrintWriter> userOutputs = new ArrayList<>();
	
	public Chatroom(String name) {
		this.name = name;
	}
	
	public void addUser(String userName, PrintWriter out) {
		users.add(userName);
		userOutputs.add(out);
	}
	
	public void removeUser(String userName) {
		users.remove(userName);
	}
	
	public void writeToChat(String message) {
		for(PrintWriter out : userOutputs) {
			out.println(message);
		}
	}
}
