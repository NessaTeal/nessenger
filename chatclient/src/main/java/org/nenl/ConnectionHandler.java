package org.nenl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class ConnectionHandler {
    private Socket socket;
    private PrintWriter out;
    BufferedReader in;
    
    void connect() throws IOException {
        socket = new Socket("localhost", 61111);

        out = new PrintWriter(socket.getOutputStream(), true);

        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    void disconnect() {
        try {
            JSONObject disconnectMsg = new JSONObject();

            disconnectMsg.put("type", "disconnect");

            out.println(disconnectMsg.toString());

            socket.shutdownInput();
            socket.shutdownOutput();
        } catch (IOException e) {
        	e.printStackTrace();
        }
    }

    void setNickname(String nickname) {

        try {
            JSONObject connect = new JSONObject();

            connect.put("type", "chooseNickname");

            connect.put("nickname", nickname);

            out.println(connect.toString());
        } catch (JSONException e) {
        	e.printStackTrace();
        }
    }

    void sendMessage(String message) {

        try {
            JSONObject JSONmessage = new JSONObject();

            JSONmessage.put("type", "message");

            JSONmessage.put("message", message);

            out.println(JSONmessage.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void createChat(String chatroomName) {
        try {
            JSONObject message = new JSONObject();

            message.put("type", "createChatroom");

            message.put("chatroomName", chatroomName);

            out.println(message.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    void joinChat(String chatroomName) {
    	try {
            JSONObject message = new JSONObject();

            message.put("type", "joinChatroom");

            message.put("chatroomName", chatroomName);

            out.println(message.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    List<String> getChatroomList() {
        try {
            JSONObject request = new JSONObject();

            request.put("type", "getChatroomList");

            out.println(request.toString());

            String answer = in.readLine();
            
            JSONObject response = new JSONObject(answer);
            
            JSONArray chatroomArray = response.getJSONArray("chatrooms");

            List<String> chatroomList = new ArrayList<>();

            for(int i = 0; i < chatroomArray.length(); i++) {
                chatroomList.add(chatroomArray.getString(i));
            }

            return chatroomList;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }
}
