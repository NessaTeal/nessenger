using Newtonsoft.Json;
using System;
using System.Collections.ObjectModel;
using System.Windows;
using System.Windows.Threading;
using WebSocket4Net;

namespace chatserver_windows
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

        private ObservableCollection<Data> words = new ObservableCollection<Data>();

        public ObservableCollection<Data> Words
        {
            get
            {
                return words;
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
            dynamic messageObject = JsonConvert.DeserializeObject(e.Message);

            Action action = () => Words.Add(new Data(messageObject.message.ToString()));

            Application.Current.Dispatcher.BeginInvoke(DispatcherPriority.Normal, action);
        }

        private void WebSocketSendMessage(object sender, EventArgs e)
        {

        }
    }
}
