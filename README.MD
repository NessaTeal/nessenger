Simple chat messenger with basic functionality. Contains both server and client implementations.

Server by default listen on port 61111.

Client by default will try to connect to my server on 34.248.239.43.

Server API:

Server and client talk to each other using JSON. Type of request are set by setting variable type to appropriate value.

Type of messages server accepts:

1. Choose nickname, type should be set to "chooseNickname" and variable "nickname" with desired nickname.
2. Create Chatroom, type should be set to "createChatroom" and variable "chatroom" should be present, also user joins newly created chatroom immediately.
3. Join Chatroom,type should be set to "joinChatroom" and variable "chatroom" should be present.
4. Quit Chatroom, type should be set to "quitChatroom".
5. Message to Chatroom, type should be set to "message" and variable "message" should be set to message user wants to send to current chatroom.
6. Get Chatroom List, type should be set to "getChatroomList", response as JSON with variable "chatroomList".
7. Disconnect, type should be set to "disconnect" required to free system resources.