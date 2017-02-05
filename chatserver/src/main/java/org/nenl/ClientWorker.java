package org.nenl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public class ClientWorker implements Runnable {
	
	private static Logger logger = Logger.getLogger(ClientWorker.class);
	
	private Socket clientSocket;
	private String nickname;
	private Map<String, Chatroom> chatrooms;
	private String chatroomName;
	
	public ClientWorker(Socket clientSocket, Map<String, Chatroom> chatrooms) {
		this.clientSocket = clientSocket;
		this.chatrooms = chatrooms;
	}
	
	@Override
	public void run() {
		BufferedReader in = null;
		PrintWriter out = null;

		try {
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			out = new PrintWriter(clientSocket.getOutputStream(), true);
		}
		catch(IOException e) {
			logger.error("Stream is closed");
			return;
		}

		String line = "";
		
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		
		while(true) {
			try {
				if((line = in.readLine()) != null) {
					
					logger.info("Received: " + line);
					
					JSONObject parsedData = new JSONObject(line);
					
					logger.info("Received from client " + clientSocket.getInetAddress());
					
					String type = parsedData.getString("type"); 
					
					switch (type) {
						case "chooseNickname":
						
							nickname = parsedData.getString("nickname");
							
							logger.info("User chose nickname: " + nickname);
							
							break;
					
						case "createChatroom":
							
							chatroomName = parsedData.getString("chatroomName");
							
							if(!chatrooms.containsKey(chatroomName)) {
								chatrooms.put(chatroomName, new Chatroom(chatroomName));
							}
							
							chatrooms.get(chatroomName).addUser(nickname, out);
							
							logger.info("User " + nickname + " created and joined chat " + chatroomName);
							
							break;
							
						case "joinChatroom":
							
							chatroomName = parsedData.getString("chatroomName");
							
							chatrooms.get(chatroomName).addUser(nickname, out);
							
							logger.info("User" + nickname + " joined chat " + chatroomName);
							
							break;
							
						case "quitChatroom":
							
							String quitMessage = "User " + nickname + " left";

							chatrooms.get(chatroomName).writeToChat(quitMessage);
							
							chatrooms.get(chatroomName).removeUser(nickname);
							
							logger.info(quitMessage + " chat " + chatroomName);
							
							chatroomName = null;
							
							break;
							
						case "getChatroomList":
							
							JSONObject chatroomListMessage = new JSONObject();
							
							JSONArray chatroomList = new JSONArray(chatrooms.keySet());
							
							chatroomListMessage.put("chatrooms", chatroomList);
							
							out.println(chatroomListMessage.toString());
							
							logger.info("User " + nickname + " retrieved chatroom list");
							
							break;
							
						case "message":
							
							String message = parsedData.getString("message");

							Date date = Calendar.getInstance().getTime();
							
							chatrooms.get(chatroomName).writeToChat("[" + sdf.format(date) + "] " + nickname + ": " + message);
							
							logger.info("User " + nickname + " in chat " + chatroomName + " wrote message: " + message);
							
							break;
							
						case "disconnect":
							
							if(chatroomName != null) {
								
								String disconnectMessage = "User " + nickname + " disconnects";
								
								chatrooms.get(chatroomName).writeToChat(disconnectMessage);
							}
							
							clientSocket.shutdownOutput();
							clientSocket.shutdownInput();
							
							logger.info("User " + nickname + " disconnects");
							
							return;
							
						default:
							break;
					}
				}
			}
			catch(Exception e) {
				e.printStackTrace();
				return;
			}
		}
	}
}
