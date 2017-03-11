package org.nenl;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import org.bson.Document;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

public class ChatServer extends WebSocketServer {

	private static Logger logger = LoggerFactory.getLogger(ChatServer.class); 

	protected Map<String, Chatroom> chatrooms;
	protected Map<WebSocket, User> users;
	protected MongoCollection<Document> messageCollection;
	protected MongoCollection<Document> chatroomCollection;
	
	public static void main(String args[]) {
		
		ChatServer chatServer = new ChatServer(new InetSocketAddress(61111));
		
		chatServer.start();
		
		logger.info("Server started, listening to connects");
	}
	
	protected ChatServer(InetSocketAddress address) {
		super(address);
		
		chatrooms = new HashMap<>();
		users = new HashMap<>();
		
		connectToDB();
		retrieveChatrooms();
		startDatabaseCleanerDaemon();
	}

	@Override
	public void onOpen(WebSocket ws, ClientHandshake arg1) {
		users.put(ws, new User());
		
		logger.info("User connects");
	}

	@Override
	public void onMessage(WebSocket ws, String message) {
		logger.info("Received message: " + message);
		
		JSONObject parsedData = new JSONObject(message);
		
		String type = parsedData.getString("type");
		
		switch (type) {
		
			case "chooseNickname":
				
				chooseNickname(ws, parsedData);
				
				break;
		
			case "createChatroom":
				
				createChatroom(ws, parsedData);
				
				break;
				
			case "joinChatroom":
				
				joinChatroom(ws, parsedData);
				
				break;
				
			case "quitChatroom":
				
				quitChatroom(ws, parsedData);
				
				break;
				
			case "getChatroomList":
				
				getChatroomList(ws);
				
				break;
				
			case "message":
				
				sendMessageToAll(ws, parsedData);
				
				break;
				
			case "disconnect":
				
				disconnect(ws);
				
				return;
				
			case "chatroomExist":
				
				chatroomExist(ws, parsedData);
				
				break;
			//For now cannot be called from chat client
			case "removeChatroom":
				
				removeChatroom(parsedData);
				
				break;
				
			default:
				
				logger.error("Invalid type");
				logger.error(parsedData.toString());
				
				break;
		}
	}

	@Override
	public void onClose(WebSocket ws, int arg1, String arg2, boolean arg3) {
		disconnect(ws);
	}

	@Override
	public void onError(WebSocket ws, Exception e) {
		logger.error(e.getMessage());
	}

	@SuppressWarnings("resource")
	protected void connectToDB() {
		MongoClient mongoClient = new MongoClient();
		
		MongoDatabase mongoDatabase = mongoClient.getDatabase("chatDatabase");
		
		messageCollection = mongoDatabase.getCollection("messages");
		chatroomCollection = mongoDatabase.getCollection("chatrooms");
	}

	protected void retrieveChatrooms() {
		for(Document chatroom : messageCollection.find()) {
			chatrooms.put(chatroom.getString("chatroomName"), new Chatroom());
		}
	}
	
	protected void startDatabaseCleanerDaemon() {
		Thread databaseCleanerDaemon = new Thread(new DatabaseCleanerDaemon(messageCollection), "DatabaseCleanerDaemon");
		
		databaseCleanerDaemon.setDaemon(true);
		
		databaseCleanerDaemon.start();
	}
	
	protected void chooseNickname(WebSocket ws, JSONObject parsedData) {
		
		User user = users.get(ws);

		if(user.nickname != null && user.chatroomName != null) {
			JSONObject response = new JSONObject();
			String changeNickname = "User " + user.nickname + " changes nickname to "
					+ parsedData.getString("nickname");
			
			response.put("type", "message");
			response.put("message", changeNickname);
			response.put("origin", "Server");
			response.put("date", System.currentTimeMillis());
			
			chatrooms.get(user.chatroomName).writeToChat(response.toString());
		}
		
		user.nickname = parsedData.getString("nickname");
		
		logger.info("User chose nickname: " + user.nickname);
	}
	
	protected void joinChatroom(WebSocket ws, JSONObject parsedData) {
		
		User user = users.get(ws);
		
		String chatroomName = parsedData.getString("chatroomName");
		
		user.chatroomName = chatroomName;
		
		JSONObject response = new JSONObject();
		String message = "User " + user.nickname + " joins chat.";
		
		response.put("type", "message");
		response.put("message", message);
		response.put("origin", "Server");
		response.put("chatroomName", user.chatroomName);
		response.put("date", System.currentTimeMillis());
		
		chatrooms.get(chatroomName).writeToChat(response.toString());
		chatrooms.get(chatroomName).addUser(ws);
		
		getPreviousMessages(ws, chatroomName);
		
		logger.info("User " + user.nickname + " joined chat " + chatroomName);
	}
	
	protected void quitChatroom(WebSocket ws, JSONObject parsedData) {
		
		User user = users.get(ws);
		
		JSONObject response = new JSONObject();
		String message = "User " + user.nickname + " left";
		
		response.put("type", "message");
		response.put("message", message);
		response.put("origin", "Server");
		response.put("chatroomName", user.chatroomName);
		response.put("date", System.currentTimeMillis());

		chatrooms.get(user.chatroomName).removeUser(ws);
		chatrooms.get(user.chatroomName).writeToChat(response.toString());
		
		logger.info(message + " chat " + user.chatroomName);
		
		user.chatroomName = null;
	}
	
	protected void disconnect(WebSocket ws) {
		
		User user = users.get(ws);

		if(user.chatroomName != null) {
			
			JSONObject response = new JSONObject();
			String message = "User " + user.nickname + " disconnects";
			
			response.put("type", "message");
			response.put("message", message);
			response.put("origin", "Server");
			response.put("chatroomName", user.chatroomName);
			response.put("date", System.currentTimeMillis());
			
			chatrooms.get(user.chatroomName).removeUser(ws);
			chatrooms.get(user.chatroomName).writeToChat(response.toString());
			
			users.remove(ws);
		}
		
		logger.info("User " + user.nickname + " disconnects");
	}
	
	protected void sendMessageToAll(WebSocket ws, JSONObject parsedData) {
		
		User user = users.get(ws);
		
		String receivedMessage = parsedData.getString("message");
		JSONObject response = new JSONObject();
		
		response.put("type", "message");
		response.put("message", receivedMessage);
		response.put("origin", user.nickname);
		response.put("chatroomName", user.chatroomName);
		response.put("date", System.currentTimeMillis());
		
		chatrooms.get(user.chatroomName).writeToChat(response.toString());
		
		messageCollection.insertOne(Document.parse(response.toString()));
		
		logger.info("User " + user.nickname + " in chat " + user.chatroomName + " wrote message: " + receivedMessage);
	}
	
	protected void getChatroomList(WebSocket ws) {
		
		JSONObject response = new JSONObject();
		JSONArray chatroomArray = new JSONArray();
		
		for(String chatroomName : chatrooms.keySet()) {
			JSONObject oneChatroom = new JSONObject();
			
			oneChatroom.put("chatroomName", chatroomName);
			
			oneChatroom.put("chatroomSize", chatrooms.get(chatroomName).userOutputs.size());
			
			chatroomArray.put(oneChatroom);
		}
		
		response.put("type", "chatroomList");
		response.put("chatrooms", chatroomArray);
		
		ws.send(response.toString());
		
		logger.info("User retrieved chatroom list");
	}
	
	protected void createChatroom(WebSocket ws, JSONObject parsedData) {

		String chatroomName = parsedData.getString("chatroomName");
		
		User user = users.get(ws);
		user.chatroomName = chatroomName;
		
		if(!chatrooms.containsKey(chatroomName)) {
			chatrooms.put(chatroomName, new Chatroom());
			
			Document chatroom = new Document("chatroomName", chatroomName);
			
			chatroomCollection.insertOne(chatroom);
		}
		
		chatrooms.get(user.chatroomName).addUser(ws);
		
		logger.info("User " + user.nickname + " created and joined chat " + user.chatroomName);
	}
	
	protected void chatroomExist(WebSocket ws, JSONObject parsedData) {
		
		User user = users.get(ws);
		
		JSONObject response = new JSONObject();

		response.put("type", "management");
		
		user.chatroomName = parsedData.getString("chatroomName");
		
		if(chatrooms.containsKey(user.chatroomName)) {
			response.put("exist", true);
		} else {
			response.put("exist", false);
		}
		
		ws.send(response.toString());
		
		logger.info("User " + user.nickname + " checking if chatroom exists");
	}
	
	protected void getPreviousMessages(WebSocket ws, String chatroomName) {
		for(Document message : messageCollection.find(Filters.eq("chatroomName", chatroomName))) {
			ws.send(message.toJson());
		}
	}
	
	protected void removeChatroom(JSONObject parsedData) {
		
		String chatroomName = parsedData.getString("chatroomName");
		
		chatroomCollection.deleteOne(Filters.eq("chatroomName", chatroomName));
		chatrooms.remove(chatroomName);
	}
}