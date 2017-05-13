package org.nenl.chatserver;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.nenl.chatstorage.ChatStorage;
import org.nenl.chatstorage.DatabaseChatStorage;
import org.nenl.chatstorage.RuntimeChatStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChatServer extends WebSocketServer {

	private static Logger logger = LoggerFactory.getLogger(ChatServer.class); 

	private Map<String, Chatroom> chatrooms;
	private Map<WebSocket, User> users;
	
	private ChatStorage chatStorage;
	
	public static void main(String args[]) {
		
		CommandLine cmd = null;
		
		try {
			cmd = parseArguments(args);
		} catch(ParseException e) {
			System.out.println("Error parsing arguments");
		}
		
		ChatServer chatServer = new ChatServer(new InetSocketAddress(61111), cmd.hasOption("d"));
		
		chatServer.start();
		
		logger.info("Server started, listening to connects");
	}
	
	protected static CommandLine parseArguments(String args[]) throws ParseException {
		Options options = new Options();
		
		options.addOption("d", "database", false, "Use to start with MongoDB usage");
		
		CommandLineParser parser = new BasicParser();
		
		return parser.parse(options, args);
	}
	
	protected ChatServer(InetSocketAddress address, boolean useDatabase) {
		super(address);
		
		chatrooms = new HashMap<>();
		users = new HashMap<>();
		
		if(useDatabase) {
			chatStorage = new DatabaseChatStorage();
		} else {
			chatStorage = new RuntimeChatStorage();
		}
		
		getChatrooms();
	}

	@Override
	public void onOpen(WebSocket ws, ClientHandshake arg1) {
		users.put(ws, new User());
		
		logger.debug("User connects");
	}

	@Override
	public void onMessage(WebSocket ws, String message) {
		logger.debug("Received message: " + message);
		
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
				returnChatroomList(ws);
				
				break;
				
			case "message":
				sendMessageToAll(ws, parsedData);
				
				break;
				
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

	protected void getChatrooms() {
		for(String chatroomName : chatStorage.getChatrooms()) {
			chatrooms.put(chatroomName, new Chatroom());
		}
	}
	
	protected void chooseNickname(WebSocket ws, JSONObject parsedData) {
		
		User user = users.get(ws);

		if(user.nickname != null) {
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
		
		logger.debug("User chose nickname: " + user.nickname);
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
		
		logger.debug("User " + user.nickname + " joined chat " + chatroomName);
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
		
		logger.debug(message + " chat " + user.chatroomName);
		
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
		
		logger.debug("User " + user.nickname + " disconnects");
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
		
		chatStorage.addMessage(user.chatroomName, response.toString());
		
		logger.debug("User " + user.nickname + " in chat " + user.chatroomName + " wrote message: " + receivedMessage);
	}
	
	protected void returnChatroomList(WebSocket ws) {
		
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
		
		logger.debug("User retrieved chatroom list");
	}
	
	protected void createChatroom(WebSocket ws, JSONObject parsedData) {

		String chatroomName = parsedData.getString("chatroomName");
		
		User user = users.get(ws);
		user.chatroomName = chatroomName;
		
		if(!chatrooms.containsKey(chatroomName)) {
			chatrooms.put(chatroomName, new Chatroom());
			
			chatStorage.addChatroom(chatroomName);
		}
		
		chatrooms.get(user.chatroomName).addUser(ws);
		
		logger.debug("User " + user.nickname + " created and joined chat " + user.chatroomName);
	}
	
	protected void getPreviousMessages(WebSocket ws, String chatroomName) {
		for(String message : chatStorage.getPreviousMessages(chatroomName)) {
			
			JSONObject fixedMessage = new JSONObject(message);
			
			fixedMessage.put("date", fixedMessage.getJSONObject("date").getLong("$numberLong"));
			fixedMessage.remove("_id");
			
			ws.send(fixedMessage.toString());
		}
	}
	
	protected void removeChatroom(JSONObject parsedData) {
		
		String chatroomName = parsedData.getString("chatroomName");
		
		chatStorage.removeChatroom(chatroomName);
		chatrooms.remove(chatroomName);
	}
}