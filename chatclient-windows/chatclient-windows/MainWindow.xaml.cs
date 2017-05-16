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
        private Chat _chat;

        public MainWindow()
        {
            InitializeComponent();
            DataContext = this;

            _chat = new Chat();

            ItemList.ItemsSource = _chat.Messages;

            ChooseNicknameWindow ChooseNicknameWindow = new ChooseNicknameWindow(_chat.Nickname);

            if(ChooseNicknameWindow.ShowDialog() == true)
            {
                _chat.ChooseNickname(ChooseNicknameWindow.Nickname);

                _chat.GetChatroomList();

                ChooseChatroomWindow ChooseChatroomWindow = new ChooseChatroomWindow(ref _chat.Chatrooms);

                if(ChooseChatroomWindow.ShowDialog() == true)
                {
                    if(ChooseChatroomWindow.NewChatroom)
                    {
                        _chat.CreateChatroom(ChooseChatroomWindow.NewChatroomName);
                    }
                    else
                    {
                        _chat.JoinChatroom(ChooseChatroomWindow.ChatroomName);
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
            _chat.SendMessage(Message.Text);
            Message.Text = "";
        }

        private void ChangeNickname(object sender, RoutedEventArgs e)
        {
            ChooseNicknameWindow ChooseNicknameWindow = new ChooseNicknameWindow(_chat.Nickname);

            if(ChooseNicknameWindow.ShowDialog() == true)
            {
                _chat.ChooseNickname(ChooseNicknameWindow.Nickname);
            }
        }

        private void ChangeChatroom(object sender, RoutedEventArgs e)
        {
            ChooseChatroomWindow ChooseChatroomWindow = new ChooseChatroomWindow(ref _chat.Chatrooms);

            if (ChooseChatroomWindow.ShowDialog() == true)
            {
                _chat.QuitChatroom();

                if (ChooseChatroomWindow.NewChatroom)
                {
                    _chat.CreateChatroom(ChooseChatroomWindow.NewChatroomName);
                }
                else
                {
                    _chat.JoinChatroom(ChooseChatroomWindow.ChatroomName);
                }
            }
        }
    }
}
