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
        const string mrSidLink = "http://download.gisinternals.com/sdk/downloads/release-1800-gdal-1-11-1-mapserver-6-4-1/gdal-111-1800-mrsid.msi";
        const string gdalLink = "http://download.gisinternals.com/sdk/downloads/release-1800-gdal-1-11-1-mapserver-6-4-1/gdal-111-1800-core.msi";

        const string installMessageMrSid = "GDAL MrSID Extension 111(MSVC 2010 Win64) is not installed." +
                                           "This is necessary to use SWAGD's application. Would you like to install this application?";
        const string installMessageGDAL = "GDAL 111(MSVC 2010 Win64) is not installed." +
                                          "This is necessary to use SWAGD's application. Would you like to install this application?";

        const string pathName = "PATH";
        const string gdalData = "GDAL_DATA";
        const string rgisuite = "RGISuite";
        const string gdal = "GDAL";
        const string swagd = "Swagd";
        const string lib = "lib";
        const string data = "data";

        [CustomAction]
        public static ActionResult CustomAction1(Session session)
        {
            try
            {
                session.Log("Begin Configure EWS Filter Custom Action");

                bool isGdalInstalled      = IsApplicationInstalled("GDAL 111 (MSVC 2010 Win64)",                   "1", RegistryView.Registry64);
                bool isGdalMrSidInstalled = IsApplicationInstalled("GDAL MrSID Extension 111 (MSVC 2010 Win64)",   "1", RegistryView.Registry64);
                bool isJdkInstalled       = IsApplicationInstalled("Java 8 Update 25 (64-bit)", "8", RegistryView.Registry64);//64 bit or 32 bit? does it matter?? version: 8.0.250.18
                bool install = false;

                if (isGdalInstalled && isGdalMrSidInstalled)
                {
                    install = true;
                }
                else if (isGdalInstalled == false)
                {
                    install = askToInstallApplication(installMessageGDAL, gdalLink);
                }
                else if (isGdalMrSidInstalled == false)
                {
                    install = askToInstallApplication(installMessageMrSid, mrSidLink);
                }
                
                if(install == false)
                {
                    return ActionResult.Failure;
                }

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
        public static bool askToInstallApplication(string message, string link)
        {
            //if chooses to install
            DialogResult dialogResult = MessageBox.Show(message, "Missing Application", MessageBoxButtons.YesNo);
            if (dialogResult == DialogResult.Yes)
            {
                System.Diagnostics.Process.Start(link);
                return true;
            }
            else if (dialogResult == DialogResult.No)
            {
                return false;
            }

            return false;
        }

        /// <summary>
        /// Sets the necessary environment Variables.  Will not write if the 
        /// environment variable exists
        /// </summary>
        public static void setEnvironmentVariables()
        {
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
        /// <param name="version"></param> only the main version number (i.e. 2.5.4 would be just '2')
        /// <returns></returns>
        public static bool IsApplicationInstalled(string name, string version, RegistryView registry)
        {
            Dictionary<string, string> programs = GetInstalledProgramsFromRegistry(registry);

            foreach (KeyValuePair<string, string> program in programs)
            {
                if (program.Key.Contains(name.ToLower()))
                {
                    string mainVersion = program.Value.Split('.')[0];
                    if (version.Equals(program.Value))
                    {
                        return true;
                    }
                }
            }
            return false;
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
