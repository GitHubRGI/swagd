using System;
using System.Collections.Generic;
using System.Text;
using WixToolset.Dtf.WindowsInstaller;
using System.Windows.Forms;
using Microsoft.Win32;
using System.IO;

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
                const string mrSidLink = "http://download.gisinternals.com/sdk/downloads/release-1800-gdal-1-11-1-mapserver-6-4-1/gdal-111-1800-mrsid.msi";
                const string gdalLink = "http://download.gisinternals.com/sdk/downloads/release-1800-gdal-1-11-1-mapserver-6-4-1/gdal-111-1800-core.msi";

                const string installMessageMrSid = "GDAL MrSID Extension 111(MSVC 2010 Win64) is not installed." +
                                                   "This is necessary to use SWAGD's application. Would you like to install this application?";
                const string installMessageGDAL = "GDAL 111(MSVC 2010 Win64) is not installed." +
                                                  "This is necessary to use SWAGD's application. Would you like to install this application?";

                session.Log("Begin Configure EWS Filter Custom Action");
                MessageBox.Show("Running The Custom Action", "Action");

                
                bool isGdalInstalled      = IsApplicationInstalled("GDAL 111 (MSVC 2010 Win64)",                 "1.0.0.0");
                bool isGdalMrSidInstalled = IsApplicationInstalled("GDAL MrSID Extension 111 (MSVC 2010 Win64)", "1.0.0.0");
                

                if (isGdalInstalled && isGdalMrSidInstalled)
                {
                    MessageBox.Show("GDAL and MR SID is installed", "Action");
                }
                else if (isGdalInstalled == false)
                {
                    askToInstallApplication(installMessageGDAL, gdalLink);
                }
                else if (isGdalMrSidInstalled == false)
                {
                    askToInstallApplication(installMessageMrSid, mrSidLink);
                }
                MessageBox.Show("Before environment vars", "Action");
                setEnvironmentVariables();

                session.Log("End Configure EWS Filter Custom Action");
            }
            catch (Exception ex)
            {
                MessageBox.Show(ex.Message, "error");
                session.Log("ERROR in custom action ConfigureEwsFilter {0}",
                            ex.ToString());

                return ActionResult.Failure;
            }
            return ActionResult.Success;
        }
        /// <summary>
        /// Asks user to install application
        /// if they click yes, it will process the link in the parameter
        /// </summary>
        /// <param name="message"></param> message to ask user if they would like to install the application
        /// <param name="link"></param>  link to the msi file to install
        public static void askToInstallApplication(string message, string link)
        {
            //if chooses to install
            DialogResult dialogResult = MessageBox.Show(message, "Missing Application", MessageBoxButtons.YesNo);
            if (dialogResult == DialogResult.Yes)
            {
                System.Diagnostics.Process.Start(link);
            }
            else if (dialogResult == DialogResult.No)
            {
                //do something else
            }
        }

        /// <summary>
        /// Sets the necessary environment Variables.  Will not write if the 
        /// environment variable exists
        /// </summary>
        public static void setEnvironmentVariables()
        {
            const string pathName = "PATH";
            const string gdalData = "GDAL_DATA";
            const string rgisuite = "RGISuite";
            const string gdal = "GDAL";
            const string swagd = "Swagd";
            const string lib = "lib";
            const string data = "data";
            string programFilesFolder = Environment.GetFolderPath(Environment.SpecialFolder.ProgramFiles);
            var target = EnvironmentVariableTarget.Machine;

            string gdalDataVar = System.Environment.GetEnvironmentVariable(gdalData);
            //check if gdal data variable is set
            if (gdalDataVar == null)
            {
                string gdalDataPath = Path.Combine(swagd, rgisuite, lib, data);
                System.Environment.SetEnvironmentVariable(gdalData, Path.Combine(programFilesFolder, gdalDataPath), target);
            }

            var swagDLibPath = Path.Combine(programFilesFolder, swagd, lib);
            //checks if swagd lib path is set
            if (pathVarContains(swagDLibPath) == false)
            {
                var pathVars = Environment.GetEnvironmentVariable(pathName, target);
                var value = pathVars + ";" + swagDLibPath;
                System.Environment.SetEnvironmentVariable(pathName, value, target);
            }

            var gdalPath = Path.Combine(programFilesFolder, gdal);
            //checks if gdal ("program files") is set
            if (pathVarContains(gdalPath) == false)
            {
                var pathVars = Environment.GetEnvironmentVariable(pathName, target);
                var value = pathVars + Path.Combine(";", programFilesFolder, gdal);
                System.Environment.SetEnvironmentVariable(pathName, value, target);
            }
        }
        /// <summary>
        /// Returns true if the "PATH" variable contains the path provided
        /// </summary>
        /// <param name="enviornmentPath"></param> the path that you wish to check is in the PATH environment variable
        /// <returns></returns>Returns true if the "PATH" variable contains the path provided
        public static bool pathVarContains(string enviornmentPath)
        {
            const string pathName = "PATH";
            var pathVars = Environment.GetEnvironmentVariable(pathName, EnvironmentVariableTarget.Machine);
            var paths = pathVars.Split(';');

            foreach (string path in paths)
            {
                if (path.ToLower().Equals(enviornmentPath.ToLower()))
                {
                    return true;
                }
            }
            return false;
        }

        /// <summary>
        /// checks if program with the name 'name' is installed
        /// </summary>
        /// <param name="name"></param>
        /// <returns></returns>
        public static bool IsApplicationInstalled(string name, string version)
        {
            Dictionary<string, string> programs = GetInstalledPrograms();

            foreach (KeyValuePair<string, string> program in programs)
            {
                if (program.Key.Contains(name.ToLower()))
                {
                    if (version.Equals(program.Value))
                    {
                        return true;
                    }
                }
            }
            return false;
        }


        /// <summary>
        /// returns a list of all 32 bit installed programs (lowercase names)
        /// </summary>
        /// <returns></returns>
        private static Dictionary<string, string> GetInstalledPrograms()
        {
            return GetInstalledProgramsFromRegistry(RegistryView.Registry64);
        }

        /// <summary>
        /// Gets list of all programs in the registry, returns them in all lowercase as a string enumberable
        /// </summary>
        /// <param name="registryView"></param>
        /// <returns></returns>
        private static Dictionary<string, string> GetInstalledProgramsFromRegistry(RegistryView registryView)
        {
            var result = new Dictionary<string, string>();
            try
            {
                using (RegistryKey key = RegistryKey.OpenBaseKey(RegistryHive.LocalMachine, registryView).OpenSubKey(registry_key))
                {
                    foreach (string subkey_name in key.GetSubKeyNames())
                    {
                        using (RegistryKey subkey = key.OpenSubKey(subkey_name))
                        {
                            var    name       = subkey.GetValue("DisplayName")    as string;
                            string appVersion = subkey.GetValue("DisplayVersion") as string;
                            if (!string.IsNullOrEmpty(name) && !string.IsNullOrEmpty(appVersion) && !result.ContainsKey(name.ToLower()))
                            {
                                result.Add(name.ToLower(), appVersion);
                            }
                        }
                    }
                }
                return result;
            }
            catch(Exception ex)
            {
                MessageBox.Show(ex.Message, "error");
                return new Dictionary<string, string>();
            }
        }
    }
}
