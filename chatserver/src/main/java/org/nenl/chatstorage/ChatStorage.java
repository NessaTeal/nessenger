package org.nenl.chatstorage;

import java.util.List;

public interface ChatStorage {
	public void addMessage(String chatroomName, String message);
	public List<String> getPreviousMessages(String chatroomName);
	
	public void addChatroom(String chatroomName);
	public void removeChatroom(String chatroomName);
	public List<String> getChatrooms();
}
