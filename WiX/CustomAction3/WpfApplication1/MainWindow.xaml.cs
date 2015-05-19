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
using System.Windows.Navigation;
using System.Windows.Shapes;
using SwagdInstallAction;
using Microsoft.Win32;

namespace WpfApplication1
{
    /// <summary>
    /// Interaction logic for MainWindow.xaml
    /// </summary>
    public partial class MainWindow : Window
    {
        const string registry_key = @"SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall";
        const string mrSidLink = "http://download.gisinternals.com/sdk/downloads/release-1400-x64-gdal-1-11-1-mapserver-6-4-1/gdal-111-1400-x64-mrsid.msi";
        const string gdalLink = "http://download.gisinternals.com/sdk/downloads/release-1400-x64-gdal-1-11-1-mapserver-6-4-1/gdal-111-1400-x64-core.msi";

        const string gdalRegexPattern = "gdal";
        const string javaRegexPattern = "java 8";
        const string mrsidRegexPattern = "gdal.*mrsid";

        const string installMessageMrSid = "GDAL MrSID Extension 111(MSVC 2010 Win64) is not installed." +
                                           "This is necessary to use SWAGD's application. Would you like to install this application?";
        const string installMessageGDAL = "GDAL 111(MSVC 2010 Win64) is not installed." +
                                          "This is necessary to use SWAGD's application. Would you like to install this application?";


        const uint gdalVersion = 16777216;
        const uint mrsidVersion = 16777216;
        const uint javaVersion = 134218178;

        public MainWindow()
        {
            InitializeComponent();
        }

        private void Button1_Click(object sender, RoutedEventArgs e)
        {
            try
            {

                bool isGdalInstalled = CustomActions.IsApplicationInstalledRegex(gdalRegexPattern, gdalVersion, RegistryView.Registry64);
                bool isGdalMrSidInstalled = CustomActions.IsApplicationInstalledRegex(mrsidRegexPattern, mrsidVersion, RegistryView.Registry64);
                bool isJdkInstalled = CustomActions.IsApplicationInstalledRegex(javaRegexPattern, javaVersion, RegistryView.Registry64);//64 bit or 32 bit? does it matter?? version: 8.0.250.18
                bool install = false;

                if (isGdalInstalled && isGdalMrSidInstalled)
                {
                    install = true;
                }
                else if (isGdalInstalled == false)
                {
                    install = CustomActions.askToInstallApplication(installMessageGDAL, gdalLink);
                }
                else if (isGdalMrSidInstalled == false)
                {
                    install = CustomActions.askToInstallApplication(installMessageMrSid, mrSidLink);
                }

                if (install == false)
                {
                    Textbox1.Text = "failure";
                }

                CustomActions.setEnvironmentVariables();

            }
            catch (Exception ex)
            {
                MessageBox.Show(ex.Message, "error");
            }
        }
    }
}
