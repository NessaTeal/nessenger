package org.nenl.chatstorage;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

public class DatabaseChatStorage implements ChatStorage {

	protected MongoCollection<Document> messageCollection;
	protected MongoCollection<Document> chatroomCollection;
	
	public DatabaseChatStorage() {
		@SuppressWarnings("resource")
		MongoClient mongoClient = new MongoClient();
		
		MongoDatabase mongoDatabase = mongoClient.getDatabase("chatDatabase");
		
		messageCollection = mongoDatabase.getCollection("messages");
		chatroomCollection = mongoDatabase.getCollection("chatrooms");
		
		startDatabaseCleanerDaemon();
	}
	
	@Override
	public void addMessage(String chatroomName, String message) {
		messageCollection.insertOne(Document.parse(message));
	}

	@Override
	public List<String> getPreviousMessages(String chatroomName) {
		List<String> messages = new ArrayList<>();
		
		for(Document message : messageCollection.find(Filters.eq("chatroomName", chatroomName))) {
			messages.add(message.toJson());
		}
		
		return messages;
	}

	@Override
	public void addChatroom(String chatroomName) {
		Document chatroom = new Document("chatroomName", chatroomName);
		
		chatroomCollection.insertOne(chatroom);
	}
	
	@Override
	public void removeChatroom(String chatroomName) {
		chatroomCollection.deleteOne(Filters.eq("chatroomName", chatroomName));
	}

	@Override
	public List<String> getChatrooms() {
		List<String> chatrooms = new ArrayList<>();
		
		for(Document chatroom : chatroomCollection.find()) {
			chatrooms.add(chatroom.getString("chatroomName"));
		}
		
		return chatrooms;
	}
	
	protected void startDatabaseCleanerDaemon() {
		Thread databaseCleanerDaemon = new Thread(new DatabaseCleanerDaemon(messageCollection), "DatabaseCleanerDaemon");
		
		databaseCleanerDaemon.setDaemon(true);
		
		databaseCleanerDaemon.start();
	}

}
