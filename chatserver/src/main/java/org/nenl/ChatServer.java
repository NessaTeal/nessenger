package org.nenl;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChatServer  {
	
	private static Logger logger = LoggerFactory.getLogger(ChatServer.class);

	protected ServerSocket serverSocket;
	protected Map<String, Chatroom> chatrooms;
	protected int id = 0;
	
	public static void main(String[] args ) {
		new ChatServer();
    }
	
	ChatServer() {
		chatrooms = new HashMap<>();
    	
    	chatrooms.put("General", new Chatroom("General"));
    	
    	try {
			serverSocket = new ServerSocket(61111);
			
			logger.info("Server socket is created, listening to connects");
			
			while(true) {
				
				Socket clientSocket;
				
				try {
					clientSocket = serverSocket.accept();
					
					new Thread(new OneConnectionWorker(clientSocket, chatrooms, id++)).start();
					
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
