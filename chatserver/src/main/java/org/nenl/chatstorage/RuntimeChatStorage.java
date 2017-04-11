package org.nenl.chatstorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RuntimeChatStorage implements ChatStorage {

	Map<String, List<String>> messages = new HashMap<>();
	
	@Override
	public void addMessage(String chatroomName, String message) {
		messages.get(chatroomName).add(message);
	}

	@Override
	public List<String> getPreviousMessages(String chatroomName) {
		return messages.get(chatroomName);
	}

	@Override
	public void addChatroom(String chatroomName) {
		messages.put(chatroomName, new ArrayList<String>());
	}
	
	@Override
	public void removeChatroom(String chatroomName) {
		messages.remove(chatroomName);
	}
	
	@Override
	public List<String> getChatrooms() {
		
		return new ArrayList<>();
	}

}
