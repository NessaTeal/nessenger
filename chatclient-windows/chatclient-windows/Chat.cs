using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Windows;
using System.Windows.Threading;
using WebSocket4Net;

namespace chatclient_windows
{
    public class Chat
    {
        public string Nickname = "Guest" + new Random().Next(10, 1000);

        public string ChatroomName;

        public ObservableCollection<MessageObject> Messages = new ObservableCollection<MessageObject>();

        public ObservableCollection<Chatroom> Chatrooms = new ObservableCollection<Chatroom>();

        private WebSocket _websocket = new WebSocket("ws://nenlmessenger.tk/chatserver");

        public Chat()
        {

            _websocket.Open();

            _websocket.MessageReceived += new EventHandler<MessageReceivedEventArgs>(WebSocketReceivedMessage);
        }

        private void WebSocketReceivedMessage(object sender, MessageReceivedEventArgs e)
        {
            dynamic IncomingMessage = JsonConvert.DeserializeObject(e.Message);

            if(IncomingMessage.type == "message")
            {
                MessageObject MessageObject = JsonConvert.DeserializeObject<MessageObject>(e.Message);

                Action AddMessage = () => Messages.Add(MessageObject);
                Application.Current.Dispatcher.BeginInvoke(DispatcherPriority.Normal, AddMessage);
            }
            else
            {
                Action ClearChatrooms = () => Chatrooms.Clear();
                Application.Current.Dispatcher.BeginInvoke(DispatcherPriority.Normal, ClearChatrooms);
                //Awful but working way of serialization and I'm really ashamed of doing it such way...
                foreach (Chatroom Chatroom in JsonConvert.DeserializeObject<List<Chatroom>>(JsonConvert.SerializeObject(IncomingMessage.chatrooms)))
                {
                    Action AddChatroom = () => Chatrooms.Add(Chatroom);
                    Application.Current.Dispatcher.BeginInvoke(DispatcherPriority.Normal, AddChatroom);
                }
            }
        }

        public void ChooseNickname()
        {
            _websocket.Send("{'type':'chooseNickname','nickname':'" + Nickname + "'}");
        }
        
        public void GetChatroomList()
        {
            _websocket.Send("{'type':'getChatroomList'}");
        }


        public void SendMessage(string Message)
        {
            _websocket.Send("{ 'type':'message', 'message':'" + Message + "'}");
        }


        public void CreateChatroom(string ChatroomName)
        {
            _websocket.Send("{'type':'createChatroom', 'chatroomName':'" + ChatroomName + "'}");
        }


        public void JoinChatroom(string ChatroomName)
        {
            _websocket.Send("{ 'type':'joinChatroom', 'chatroomName':'" + ChatroomName + "'}");
        }


        public void QuitChatroom()
        {
            _websocket.Send("'type':'quitChatroom'}");
        }
    }
}
