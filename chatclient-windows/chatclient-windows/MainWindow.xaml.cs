using Newtonsoft.Json;
using System;
using System.Collections.ObjectModel;
using System.Windows;
using System.Windows.Threading;
using WebSocket4Net;

namespace chatclient_windows
{
    /// <summary>
    /// Interaction logic for MainWindow.xaml
    /// </summary>

    public class Data
    {
        public Data(string Name)
        {
            this.Name = Name;
        }

        public string Name { get; set; }
    }

    public partial class MainWindow : Window
    {
        WebSocket ws = new WebSocket("ws://nenlmessenger.tk/chatserver");

        private ObservableCollection<MessageObject> messages = new ObservableCollection<MessageObject>();

        public ObservableCollection<MessageObject> Messages
        {
            get
            {
                return messages;
            }
        }

        public MainWindow()
        {
            InitializeComponent();
            this.DataContext = this;

            ws.MessageReceived += new EventHandler<MessageReceivedEventArgs>(WebSocketReceivedMessage);

            ws.Open();
        }

        private void SendMessage(object sender, RoutedEventArgs e)
        {
            ws.Send("{'type':'chooseNickname','nickname':'Nenl'}");
            ws.Send("{'type':'joinChatroom','chatroomName':'General'}");
            //ws.Send("{'type':'message','message':'ping'}");
            //Words.Add(new Data("third"));
        }

        private void WebSocketReceivedMessage(object sender, MessageReceivedEventArgs e)
        {
            MessageObject messageObject = JsonConvert.DeserializeObject<MessageObject>(e.Message);

            Action action = () => messages.Add(messageObject);

            Application.Current.Dispatcher.BeginInvoke(DispatcherPriority.Normal, action);
        }

        private void WebSocketSendMessage(object sender, EventArgs e)
        {

        }
    }
}
