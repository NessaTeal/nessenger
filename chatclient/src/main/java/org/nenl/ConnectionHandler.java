package org.nenl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ConnectionHandler {
	
	private static Logger logger = LoggerFactory.getLogger(ChatClient.class);
	
    protected Socket socket;
    protected PrintWriter out;
    protected BufferedReader in;
    protected BufferedReader managementIn;
    protected volatile boolean stopStreamDividerThread = false;
    
    BufferedReader messageIn;
    
    void connect() throws IOException {
    	
    	//IP address of my server 34.248.239.43
    	
        socket = new Socket("localhost", 61111);

        out = new PrintWriter(socket.getOutputStream(), true);

        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.forName("UTF-8")));
		
		logger.info("Connection to server is successful.");
        
        createStreamDividerThread();
    }
    
    protected void createStreamDividerThread() {
        new Thread(new Runnable() {
			public void run() {
				
				try {
			        PipedInputStream pipeMessageIn = new PipedInputStream();
			        PipedOutputStream pipeMessageOut = new PipedOutputStream(pipeMessageIn);
			        
			        PrintWriter messageOut = new PrintWriter(pipeMessageOut, true);
					messageIn = new BufferedReader(new InputStreamReader(pipeMessageIn, Charset.forName("UTF-8")));
						
			        PipedInputStream pipeManagementIn = new PipedInputStream();
			        PipedOutputStream pipeManagementOut = new PipedOutputStream(pipeManagementIn);
			        
			        PrintWriter managementOut = new PrintWriter(pipeManagementOut, true);
			        managementIn = new BufferedReader(new InputStreamReader(pipeManagementIn, Charset.forName("UTF-8")));
					
					while(!stopStreamDividerThread) {
						
						if(!in.ready()) {
							TimeUnit.MILLISECONDS.sleep(20);
							continue;
						}
						
						String line = in.readLine();
						
						JSONObject receivedMessage = new JSONObject(line);
						
						String type = receivedMessage.getString("type");
						
						switch (type) {
							case "message":
								
								messageOut.println(receivedMessage.getString("message"));
								
								break;
	
							case "management":
								
								managementOut.println(receivedMessage);
								
								break;
								
							default:
								break;
						}
					}
					
					logger.info("Closing internal resources.");

					messageIn.close();
					messageOut.close();
					managementIn.close();
					managementOut.close();
					pipeMessageIn.close();
					pipeMessageOut.close();
					pipeManagementIn.close();
					pipeManagementOut.close();
				}
				catch(Exception e) {
					logger.error(e.getMessage());
				}
			}
		}, "stream divider").start();
        
        logger.info("Stream divider thread is started");
    }

    void disconnect() {
        try {
            JSONObject disconnectMsg = new JSONObject();

            disconnectMsg.put("type", "disconnect");

            out.println(disconnectMsg.toString());
            
            logger.info("Disconnect message is sent");
            
            stopStreamDividerThread = true;
            
            socket.shutdownInput();
            socket.shutdownOutput();
        } catch (IOException e) {
        	logger.error(e.getMessage());
        }
    }

    void setNickname(String nickname) {

        try {
            JSONObject connect = new JSONObject();

            connect.put("type", "chooseNickname");
            connect.put("nickname", nickname);

            out.println(connect.toString());
            
            logger.info("Sent nickname message is sent");
        } catch (JSONException e) {
        	logger.error(e.getMessage());
        }
    }

    void sendMessage(String message) {

        try {
            JSONObject JSONmessage = new JSONObject();

            JSONmessage.put("type", "message");
            JSONmessage.put("message", message);

            out.println(JSONmessage.toString());
            
            logger.info("Message to chatroom is sent");
        } catch (JSONException e) {
            logger.error(e.getMessage());
        }
    }

    void createChatroom(String chatroomName) {
        try {
            JSONObject message = new JSONObject();

            message.put("type", "createChatroom");
            message.put("chatroomName", chatroomName);

            out.println(message.toString());
            
            logger.info("Create chatroom message is sent");
        } catch (JSONException e) {
            logger.error(e.getMessage());
        }
    }
    
    void joinChatroom(String chatroomName) {
    	try {
            JSONObject message = new JSONObject();

            message.put("type", "joinChatroom");
            message.put("chatroomName", chatroomName);

            out.println(message.toString());
            
            logger.info("Join chatroom message is sent");
        } catch (JSONException e) {
            logger.error(e.getMessage());
        }
    }
    
    void quitChatroom() {
    	try {
            JSONObject message = new JSONObject();

            message.put("type", "quitChatroom");

            out.println(message.toString());
            
            logger.info("Quit chatroom message is sent");
        } catch (JSONException e) {
            logger.error(e.getMessage());
        }
    }

    List<Object> getChatroomList() {
        try {
            JSONObject request = new JSONObject();

            request.put("type", "getChatroomList");

            out.println(request.toString());

            logger.info("Chatroom list is requested");

            String answer = managementIn.readLine();
            JSONObject response = new JSONObject(answer);
            
            logger.info("Chatroom list is retrieved");
            
            JSONArray chatroomArray = response.getJSONArray("chatrooms");

            return chatroomArray.toList();

        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        //Should never happen as there is always General chatroom
        
        return new ArrayList<Object>();
    }
    
    boolean chatroomExists(String chatroomName) {
    	try {
            JSONObject request = new JSONObject();
            
            request.put("type", "chatroomExist");
            request.put("chatroomName", chatroomName);

            out.println(request.toString());

            logger.info("Chatroom existance is requested");

            String answer = managementIn.readLine();
            JSONObject response = new JSONObject(answer);
            
            return response.getBoolean("exist");
    		
    	} catch (Exception e) {
            logger.error(e.getMessage());
        }
    	
    	return false;
    }
}
