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

    public partial class MainWindow : Window
    {
        private Chat Chat;

        public MainWindow()
        {
            InitializeComponent();
            DataContext = this;

            Chat = new Chat();

            ItemList.ItemsSource = Chat.Messages;

            ChooseNicknameWindow ChooseNicknameWindow = new ChooseNicknameWindow(ref Chat.Nickname);

            if(ChooseNicknameWindow.ShowDialog() == true)
            {
                Chat.ChooseNickname();

                Chat.GetChatroomList();

                ChooseChatroomWindow ChooseChatroomWindow = new ChooseChatroomWindow(ref Chat.Chatrooms);

                if(ChooseChatroomWindow.ShowDialog() == true)
                {
                    if(ChooseChatroomWindow.NewChatroom)
                    {
                        Chat.CreateChatroom(ChooseChatroomWindow.NewChatroomName);
                    }
                    else
                    {
                        Chat.JoinChatroom(ChooseChatroomWindow.ChatroomName);
                    }
                }
                else
                {
                    Close();
                }
            }
            else
            {
                Close();
            }
        }

        private void SendMessage(object sender, RoutedEventArgs e)
        {
            Chat.SendMessage(Message.Text);
            Message.Text = "";
        }
    }
}
