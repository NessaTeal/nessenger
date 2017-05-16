using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Shapes;

namespace chatclient_windows
{
    /// <summary>
    /// Interaction logic for ChooseNicknameWindow.xaml
    /// </summary>
    public partial class ChooseNicknameWindow : Window
    {
        public string Nickname { get; set; }

        public ChooseNicknameWindow(ref string Nickname)
        {
            InitializeComponent();
            DataContext = this;
            this.Nickname = Nickname;
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
