package org.nenl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientWorker implements Runnable {
	
	protected static Logger logger = LoggerFactory.getLogger(ClientWorker.class);
	protected static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
	
	protected Socket clientSocket;
	protected String nickname;
	protected Map<String, Chatroom> chatrooms;
	protected String chatroomName;
	protected PrintWriter out;
	
	ClientWorker(Socket clientSocket, Map<String, Chatroom> chatrooms) {
		this.clientSocket = clientSocket;
		this.chatrooms = chatrooms;
	}
	
	@Override
	public void run() {
		BufferedReader in = null;

		try {
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(),Charset.forName("UTF-8")));
			out = new PrintWriter(clientSocket.getOutputStream(), true);
		}
		catch(IOException e) {
			logger.error("Stream is closed");
			return;
		}

		String line = "";
		
		while(true) {
			try {
				if((line = in.readLine()) != null) {
					
					logger.info("Received: " + line);
					
					JSONObject parsedData = new JSONObject(line);
					
					String type = parsedData.getString("type"); 
					
					switch (type) {
						case "chooseNickname":
							
							chooseNickname(parsedData);
							
							break;
					
						case "createChatroom":
							
							createChatroom(parsedData);
							
							break;
							
						case "joinChatroom":
							
							joinChatroom(parsedData);
							
							break;
							
						case "quitChatroom":
							
							quitChatroom(parsedData);
							
							break;
							
						case "getChatroomList":
							
							getChatroomList(parsedData);
							
							break;
							
						case "message":
							
							sendMessageToAll(parsedData);
							
							break;
							
						case "disconnect":
							
							disconnect(parsedData);
							
							return;
							
						default:
							break;
					}
				}
			}
			catch(IOException e) {
				e.printStackTrace();
				return;
			}
		}
	}
	
	protected void chooseNickname(JSONObject parsedData) {

		if(nickname != null && chatroomName != null) {
			JSONObject changeNicknameMessage = new JSONObject();
			String changeNickname = "User " + nickname + " changes nickname to "
					+ parsedData.getString("nickname");
			
			changeNicknameMessage.put("type", "message");
			changeNicknameMessage.put("message", changeNickname);
			
			chatrooms.get(chatroomName).writeToChat(changeNicknameMessage.toString());
		}
		
		nickname = parsedData.getString("nickname");
		
		logger.info("User chose nickname: " + nickname);
	}
	
	protected void joinChatroom(JSONObject parsedData) {
		
		chatroomName = parsedData.getString("chatroomName");
		
		JSONObject response = new JSONObject();
		String message = "User " + nickname + " joins chat.";
		
		response.put("type", "message");
		response.put("message", message);
		
		chatrooms.get(chatroomName).writeToChat(response.toString());
		chatrooms.get(chatroomName).addUser(nickname, out);
		
		logger.info("User" + nickname + " joined chat " + chatroomName);
	}
	
	protected void quitChatroom(JSONObject parsedData) {
		
		JSONObject response = new JSONObject();
		String message = "User " + nickname + " left";
		
		response.put("type", "message");
		response.put("message", message);

		chatrooms.get(chatroomName).removeUser(nickname);
		chatrooms.get(chatroomName).writeToChat(response.toString());
		
		logger.info(message + " chat " + chatroomName);
		
		chatroomName = null;
	}
	
	protected void disconnect(JSONObject parsedData) throws IOException {

		if(chatroomName != null) {
			
			JSONObject response = new JSONObject();
			String message = "User " + nickname + " disconnects";
			
			response.put("type", "message");
			response.put("message", message);
			
			chatrooms.get(chatroomName).writeToChat(response.toString());
		}
		
		clientSocket.shutdownOutput();
		clientSocket.shutdownInput();
		
		logger.info("User " + nickname + " disconnects");
	}
	
	protected void sendMessageToAll(JSONObject parsedData) {
		
		String receivedMessage = parsedData.getString("message");
		JSONObject response = new JSONObject();
		Date date = Calendar.getInstance().getTime();
		String dateString = "[" + sdf.format(date) + "]";
		
		response.put("type", "message");
		response.put("message", dateString + " " + nickname + ": " + receivedMessage);
		
		chatrooms.get(chatroomName).writeToChat(response.toString());
		
		logger.info("User " + nickname + " in chat " + chatroomName + " wrote message: " + receivedMessage);
	}
	
	protected void getChatroomList(JSONObject parsedData) {
		
		JSONObject response = new JSONObject();
		JSONArray chatroomArray = new JSONArray(chatrooms.keySet());
		
		response.put("type", "management");
		response.put("chatrooms", chatroomArray);
		
		out.println(response.toString());
		
		logger.info("User " + nickname + " retrieved chatroom list");
	}
	
	protected void createChatroom(JSONObject parsedData) {

		chatroomName = parsedData.getString("chatroomName");
		
		if(!chatrooms.containsKey(chatroomName)) {
			chatrooms.put(chatroomName, new Chatroom(chatroomName));
		}
		
		chatrooms.get(chatroomName).addUser(nickname, out);
		
		logger.info("User " + nickname + " created and joined chat " + chatroomName);
	}
}
