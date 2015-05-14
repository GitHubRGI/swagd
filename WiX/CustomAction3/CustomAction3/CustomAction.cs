using System;
using System.Collections.Generic;
using System.Text;
using WixToolset.Dtf.WindowsInstaller;
using System.Windows.Forms;
using Microsoft.Win32;

namespace CustomAction3
{
    public class CustomActions
    {
        [CustomAction]
        public static ActionResult CustomAction1(Session session)
        {
            try
            {
                session.Log("Begin Configure EWS Filter Custom Action");
                                            // TODO: Make changes to config file
                MessageBox.Show("Running The Custom Action", "Action");

                bool isGdalInstalled = IsApplictionInstalled("OSGEO");


                if(isGdalInstalled)
                {
                    MessageBox.Show("GDAL is installed", "Action");
                }
                else
                {
                    MessageBox.Show("GDAL is not installed", "Action");
                }

                session.Log("End Configure EWS Filter Custom Action");
            }
            catch (Exception ex)
            {
                session.Log("ERROR in custom action ConfigureEwsFilter {0}", 

                ex.ToString());

                return ActionResult.Failure;
            }
             return ActionResult.Success;
        }

        public static bool IsApplictionInstalled(string p_name)
        {
            string displayName;
            StringBuilder displayNames = new StringBuilder();
            RegistryKey key;
            MessageBox.Show("In method", "method");
            // search in: CurrentUser
            key = Registry.CurrentUser.OpenSubKey(@"SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall");
            foreach (String keyName in key.GetSubKeyNames())
            {
                
                RegistryKey subkey = key.OpenSubKey(keyName);
                displayName = subkey.GetValue("DisplayName") as string;
               
                {
                    displayNames.Append(displayName);
                    displayNames.Append(", ");
                }
                if (p_name.Equals(displayName, StringComparison.OrdinalIgnoreCase) == true)
                {
                    return true;
                }
            }
            MessageBox.Show(displayNames.ToString(), "display Names");

            // search in: LocalMachine_32
            key = Registry.LocalMachine.OpenSubKey(@"SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall");
            foreach (String keyName in key.GetSubKeyNames())
            {
                RegistryKey subkey = key.OpenSubKey(keyName);
                displayName = subkey.GetValue("DisplayName") as string;

                
                {
                    displayNames.Append(displayName);
                    displayNames.Append(", ");
                }
                if (p_name.Equals(displayName, StringComparison.OrdinalIgnoreCase) == true)
                {
                    return true;
                }
            }
            MessageBox.Show(displayNames.ToString(), "display Names");

            // search in: LocalMachine_64
            key = Registry.LocalMachine.OpenSubKey(@"SOFTWARE\Wow6432Node\Microsoft\Windows\CurrentVersion\Uninstall");
            foreach (String keyName in key.GetSubKeyNames())
            {
                RegistryKey subkey = key.OpenSubKey(keyName);
                displayName = subkey.GetValue("DisplayName") as string;
                
                {
                    displayNames.Append(displayName);
                    displayNames.Append(", ");
                }
                if (p_name.Equals(displayName, StringComparison.OrdinalIgnoreCase) == true)
                {
                    return true;
                }
            }

            MessageBox.Show(displayNames.ToString(), "display Names" );

            // NOT FOUND
            return false;
        }
    }
}
