package org.nenl;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class ChatServer  {
	
	private static Logger logger = Logger.getLogger(ChatServer.class);
	
	public static void main(String[] args ) throws Exception {
    	
    	ServerSocket serverSocket = null;
    	
    	Map<String, Chatroom> chatrooms = new HashMap<>();
    	
    	chatrooms.put("General", new Chatroom("General"));
    	
    	try {
			serverSocket = new ServerSocket(61111);
			
			logger.info("Socket is created, listening to connects");
			
			while(true) {
				
				Socket clientSocket = null;
				
				try {
					clientSocket = serverSocket.accept();
					
					new Thread(new ClientWorker(clientSocket, chatrooms)).start();
					
					logger.info("Client connected from: " + clientSocket.getInetAddress());
				} catch (IOException e) {
	                logger.error("Error to get connect from client");
	            }
			}
    	}
		catch(IOException e) {
            logger.error("I/O error to open server socker");
		}
    }
}
