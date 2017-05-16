using System.Windows;

namespace chatclient_windows
{
    /// <summary>
    /// Interaction logic for ChooseNicknameWindow.xaml
    /// </summary>
    public partial class ChooseNicknameWindow : Window
    {
        public string Nickname { get; set; }

        public ChooseNicknameWindow(string OldNickname)
        {
            InitializeComponent();
            DataContext = this;
            Nickname = OldNickname;
        }

        public void ChooseNickname(object sender, RoutedEventArgs e)
        {
            DialogResult = true;
            Close();
        }

        public void CancelOperation(object sender, RoutedEventArgs e)
        {
            Close();
        }
    }
}
