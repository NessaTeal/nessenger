using System;
using System.Collections.ObjectModel;
using System.Windows;

namespace chatclient_windows
{
    /// <summary>
    /// Interaction logic for ChooseChatroomWindow.xaml
    /// </summary>
    public partial class ChooseChatroomWindow : Window
    {
        public string ChatroomName { get; set; }
        public string NewChatroomName { get; set; }
        public bool NewChatroom { get; set; }

        public ChooseChatroomWindow(ref ObservableCollection<Chatroom> Chatrooms)
        {
            InitializeComponent();
            DataContext = this;
            ChatroomList.ItemsSource = Chatrooms;
        }

        private void CreateChatroom(object sender, RoutedEventArgs e)
        {
            NewChatroom = true;
            DialogResult = true;
            Close();
        }

        private void ChooseChatroom(object sender, RoutedEventArgs e)
        {
            if (ChatroomList.SelectedItem != null)
            {
                ChatroomName = (ChatroomList.SelectedItem as Chatroom).chatroomName;
                DialogResult = true;
                NewChatroom = false;
                Close();
            }
        }
    }
}
