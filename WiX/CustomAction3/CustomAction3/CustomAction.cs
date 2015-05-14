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
        const string registry_key = @"SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall";

        [CustomAction]
        public static ActionResult CustomAction1(Session session)
        {
            try
            {
                session.Log("Begin Configure EWS Filter Custom Action");
                // TODO: Make changes to config file
                MessageBox.Show("Running The Custom Action", "Action");

                bool isGdalInstalled = IsApplicationInstalled("gdal 111 (msvc 2010 win64)");
                //bool isGdalInstalled = IsApplicationInstalled("gdal 111");//1.0.0.0
                //gdal 111 (MSVC 2010 Win64)

                if (isGdalInstalled)
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

        /// <summary>
        /// checks if program with the name 'name' is installed
        /// </summary>
        /// <param name="name"></param>
        /// <returns></returns>
        public static bool IsApplicationInstalled(string name)
        {
            foreach(string value in GetInstalledPrograms())
            {
                if (value.Contains("gdal"))
                {
                    MessageBox.Show(value, "display name");
                }
                if (value.Contains(name.ToLower()))
                {
                    MessageBox.Show(name, "display name");
                    return true;
                }
            }
            return false;
        }


        /// <summary>
        /// returns a list of all 32 and 64 bit installed programs (lowercase names)
        /// </summary>
        /// <returns></returns>
        private static List<string> GetInstalledPrograms()
        {
            var result = new List<string>();
            //result.AddRange(GetInstalledProgramsFromRegistry(RegistryView.Registry32));
           // result.AddRange(GetInstalledProgramsFromRegistry(RegistryView.Registry64));
            return GetInstalledProgramsFromRegistry(RegistryView.Registry64);
        }

        /// <summary>
        /// Gets list of all programs in the registry, returns them in all lowercase as a string enumberable
        /// </summary>
        /// <param name="registryView"></param>
        /// <returns></returns>
        private static List<string> GetInstalledProgramsFromRegistry(RegistryView registryView)
        {
            var result = new List<string>();

            using (RegistryKey key = RegistryKey.OpenBaseKey(RegistryHive.LocalMachine, registryView).OpenSubKey(registry_key))
            {
                foreach (string subkey_name in key.GetSubKeyNames())
                {
                    using (RegistryKey subkey = key.OpenSubKey(subkey_name))
                    {
                        var name = (string)subkey.GetValue("DisplayName");
                        string appVersion = subkey.GetValue("DisplayVersion") as string;
                        if (!string.IsNullOrEmpty(name))
                        {
                            result.Add(name.ToLower());
                        }
                    }
                }
            }
            return result;
        }
    }
}
