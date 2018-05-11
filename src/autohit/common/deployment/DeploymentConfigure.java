/**
 * AUTOHIT 2003
 * Copyright Erich P Gatejen (c) 1989,1997,2003,2004
 * 
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Additional license information can be found in the documentation.
 * @author Erich P Gatejen
 */
package autohit.common.deployment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.StringTokenizer;

import org.apache.commons.collections.ExtendedProperties;

import autohit.common.AutohitProperties;
import autohit.common.Constants;
import autohit.common.Utils;

/**
 * A deployment manager.  It will handle configuration, installation, and checkpointing.
 *
 * @author Erich P. Gatejen
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - Initial - 25Apr03 
 * 
 */
public class DeploymentConfigure {

	/**
	 * Command system
	 */
	public ExtendedProperties props;

	/**
	 *  Default constructor. 
	 */
	public DeploymentConfigure() throws Exception {

	}

	/**
	 *  Checkpoint the system
	 * @param name checkpoint name
	 * @param root directory root
	 * @param cf configuration file
	 * @param special unlock factory.  ALWAYS use FALSE
	 * @return true if successful, false if there is an error
	 */
	public boolean checkpoint(
		String name,
		String root,
		String cf,
		boolean special) {

		String[] currentLine;
		String lf;
		File lFile;
		int count = 0;
		int lineCount = 0;

		// Is it trying to checkpoint to the factory settings?
		if ((special == false)
			&& (name.equals(AutohitProperties.vFACTORY_STORE))) {
			System.out.println(
				"You may not checkpoint to the factory settings.  Use a checkpoint name other than 'factory'.");
			return false;
		}

		String configPath =
			new String(root + AutohitProperties.vCONFIG_ROOT + name + "/");

		// See if already saved.
		File fdpath = new File(configPath);
		if (fdpath.exists()) {
			System.out.println(
				"There is already a " + name + " checkpoint set.");
			System.out.println("You can use the delete command to remove it.");
			return false;
		}

		// Set the config file
		if (cf == null) {
			cf = AutohitProperties.vDEFAULT_CHECKPOINT;
		}
		lf = new String(root + AutohitProperties.vCONFIG_ROOT + cf);

		// Get to work		
		try {
			// Make the dir
			fdpath.mkdirs();

			// Open config file
			lFile = new File(lf);
			BufferedReader inBR = new BufferedReader(new FileReader(lFile));

			// example line: n bin/default.prop factory/standard/default.prop
			currentLine = parseConfigLine(inBR);
			while (currentLine != null) {
				lineCount++;
				switch (currentLine[0].charAt(0)) {

					case Constants.CONFIG_DIR :
						System.out.println(
							Utils.copyDir(
								root + "/" + currentLine[1],
								configPath + Integer.toString(count),
								false));
						count++;
						break;

					case Constants.CONFIG_COMMENT :
						// ignore
						break;

					case Constants.CONFIG_CHKPNT :
					case Constants.CONFIG_CONFIG_YES :
					case Constants.CONFIG_CONFIG_NO :
						System.out.println(
							Utils.copy(
								root + "/" + currentLine[1],
								configPath + Integer.toString(count)));
						count++;
						break;

					default :
						System.out.println(
							"Bad command.  Line #"
								+ lineCount
								+ ".  Command ="
								+ currentLine[0].charAt(0));
				}
				currentLine = parseConfigLine(inBR);
			}

			// Move the config file
			inBR.close();
			System.out.println(
				Utils.copy(lf, configPath + AutohitProperties.vCONFIG_FILE));

		} catch (Exception e) {
			System.out.println("CHECKPOINT failed!");
			e.printStackTrace();
			return false;
		}

		System.out.println("CHECKPOINT is done.");
		return true;
	}

	/**
	 * Delete a checkpoint
	 * @param name checkpoint name
	 * @param root directory root
	 * @return true if successful, false if there is an error
	 */
	public boolean delete(String name, String root) {

		String[] currentLine;
		String lf;
		File lFile;
		int count = 0;
		int lineCount = 0;

		String configPath =
			new String(root + AutohitProperties.vCONFIG_ROOT + name);

		// Is it trying to remove the factory settings?
		if (name.equals(AutohitProperties.vFACTORY_STORE)) {
			System.out.println("You may not delete the factory settings.");
		}

		// See if already saved.
		File fdpath = new File(configPath);
		if (!fdpath.exists()) {
			System.out.println("Checkpoint set does not exist.");
			return false;
		}

		System.out.println(Utils.wipeDir(configPath));
		System.out.println("DELETE is done.  Check messages for errors.");
		return true;
	}

	/**
	 *  Restore a checkpoint
	 * @param name checkpoint name
	 * @param root directory root
	 * @param wipe destructive restore
	 * @return true if successful, false if there is an error
	 */
	public boolean restore(String name, String root, boolean wipe) {

		String[] currentLine;
		String lf;
		File fdfile;
		int count = 0;
		int lineCount = 0;
		BufferedReader inBR;

		String configPath =
			new String(root + AutohitProperties.vCONFIG_ROOT + name + "/");
		String configFile =
			new String(configPath + AutohitProperties.vCONFIG_FILE);

		// See if it is there and get the configuration file
		try {

			fdfile = new File(configPath);
			if (!fdfile.exists())
				throw new Exception();
			inBR = new BufferedReader(new FileReader(fdfile));

		} catch (Exception e) {
			System.out.println(
				"Checkpoint does not exist or is invalid for " + name);
			return false;
		}

		// Get to work		
		try {

			// example line: n bin/default.prop factory/standard/default.prop
			currentLine = parseConfigLine(inBR);
			while (currentLine != null) {
				lineCount++;
				switch (currentLine[1].charAt(0)) {

					case Constants.CONFIG_DIR :
						System.out.println(
							Utils.copyDir(
								configPath + Integer.toString(count),
								root + "/" + currentLine[1],
								wipe));
						count++;
						break;

					case Constants.CONFIG_COMMENT :
						// ignore
						break;

					case Constants.CONFIG_CHKPNT :
					case Constants.CONFIG_CONFIG_YES :
					case Constants.CONFIG_CONFIG_NO :
						System.out.println(
							Utils.copy(
								root + "/" + currentLine[1],
								configPath + Integer.toString(count)));
						count++;
						break;

					default :
						System.out.println(
							"Bad command.  Line #"
								+ lineCount
								+ ".  Command ="
								+ currentLine[1].charAt(0));
				}
				currentLine = parseConfigLine(inBR);
			}

		} catch (Exception e) {
			System.out.println("RESTORE failed!");
			e.printStackTrace();
			return false;
		}
		System.out.println("RESTORE is done.");
		return true;
	}

	/**config config [config name] [root] [prop file]
	 * Process a configuration
	 * @param config config file name
	 * @param root directory root
	 * @param vars variable replacement set.  a properties file.
	 * @return true if successful, false if there is an error
	 */
	public boolean configure(String config, String root, String vars) {

		ExtendedProperties varprops;

		// Get variables file and load into varprops
		String varPath = new String(root + AutohitProperties.vVAR_ROOT + vars);
		File varFile = new File(varPath);
		if (!varFile.exists()) {
			System.out.println(
				"Configuration values file does not exist= "
					+ vars
					+ ".  It should be under the /etc directory.");
			return false;
		}
		try {
			varprops = new ExtendedProperties(varPath);
		} catch (Exception e) {
			System.out.println("Configuration values file is unreadable.");
			return false;
		}
		return configure(config, root, varprops);
	}

	/**config config [config name] [root] [prop file]
	 * Process a configuration
	 * @param config config file name
	 * @param root directory root
	 * @param varprops an ExtendedProperties set of configuration name/values.
	 * @return true if successful, false if there is an error
	 */
	public boolean configure(
		String config,
		String root,
		ExtendedProperties varprops) {

		String[] currentLine;
		String lf;
		File fdfile;
		int count = 0;
		int lineCount = 0;
		BufferedReader inBR;

		String configPath;
		String configPathFile;
		File fConfigFile;

		// See if it is there and get the configuration file
		try {
			// try the root
			configPath =
				new String(
					root + AutohitProperties.vCONFIG_ROOT + config + "/");
			configPathFile =
				new String(configPath + AutohitProperties.vCONFIG_FILE);
			fConfigFile = new File(configPathFile);
			if (!fConfigFile.exists()) {
				// see if there are any hanger-ons
				configPathFile = new String(configPath + config);
				fConfigFile = new File(configPathFile);
				if (!fConfigFile.exists())
					throw new Exception();
			}
			inBR = new BufferedReader(new FileReader(fConfigFile));
		} catch (Exception e) {
			System.out.println(
				"Configuration descriptor is invalid for " + config);
			return false;
		}

		// Get to work		
		try {

			// example line: n bin/default.prop factory/standard/default.prop
			currentLine = parseConfigLine(inBR);
			while (currentLine != null) {
				lineCount++;
System.out.println("linecount="+lineCount+" count=" + count + " line="+ currentLine[0]);				
				switch (currentLine[0].charAt(0)) {

					case Constants.CONFIG_DIR :
						System.out.println(
							Utils.copyDir(
								configPath + Integer.toString(count),
								root + "/" + currentLine[1],
								false));
						count++;
						break;

					case Constants.CONFIG_COMMENT :
					case Constants.CONFIG_CHKPNT :
						// ignore
						break;

					case Constants.CONFIG_CONFIG_YES :
						System.out.println(
							Utils.merge(
								configPath + Integer.toString(count),
								root + "/" + currentLine[1],
								varprops));
						count++;
						break;

					case Constants.CONFIG_CONFIG_NO :
						System.out.println(
							Utils.copy(
								configPath + Integer.toString(count),
								root + "/" + currentLine[1]));
						count++;
						break;

					default :
						System.out.println(
							"Bad command.  Line #"
								+ lineCount
								+ ".  Command ="
								+ currentLine[0].charAt(0));
				}
				currentLine = parseConfigLine(inBR);
			}

		} catch (Exception e) {
			System.out.println("CONFIGURE failed!");
			e.printStackTrace();
			return false;
		}
		System.out.println("CONFIGURE is done.");
		return true;
	}

	/**
	 *  Parse the config line
	 */
	private String[] parseConfigLine(BufferedReader inR) {

		String t[] = new String[3];
		String working = null;
		StringTokenizer st;

		try {

			working = inR.readLine();
			while ((working.length() == 0)
				|| (working.charAt(0) == '#')
				|| (!Character.isLetter(working.charAt(0)))) {
				working = inR.readLine();
			}

			// Working should be a valid line
			try {

				st = new StringTokenizer(working);
				t[0] = st.nextToken();
				t[1] = st.nextToken();
				// the third field doesn't matter for checkpointing
				try {
					t[2] = st.nextToken();
				} catch (Exception e) {
					t[2] = null;
				}

			} catch (Exception e) {
				System.out.println("BAD line in Files:\n" + working);
				throw e;
			}

		} catch (Exception e) {
			// Quit for ANY exception
			return null;
		}

		return t;
	}

	/**
	 *  Object main
	 */
	public void go(String[] args) {

		char cmd = args[0].charAt(0);
		switch (cmd) {
			case 'S' :
			case 's' :
				if (args.length == 4) {
					checkpoint(args[1], args[2], args[3], false);
				} else if (args.length == 3) {
					checkpoint(args[1], args[2], null, false);
				} else {
					System.out.println("ERROR: Bad number of parameters.");
					usage();
				}
				break;

			case 'F' :
			case 'f' :
				System.out.println("BUILD FACTORY SETTINGS");
				// CHECKPOINT THE FACTORY
				// YOU SHOULDN'T DO THIS!  IT'S FOR THE BUILD SYSTEM
				// The command is "config factory PLEASE [root]" (cases sensitive)
				if ((args.length == 3) && (args[1].charAt(0) == 'P')) {
					checkpoint(
						AutohitProperties.vFACTORY_STORE,
						args[2],
						AutohitProperties.vDEFAULT_CHECKPOINT,
						true);
				} else {
					System.out.println("ERROR: Unknown command.");
					usage();
				}
				break;

			case 'R' :
			case 'r' :
				if (args.length == 3) {
					restore(args[1], args[2], true);
				} else {
					System.out.println("ERROR: Bad number of parameters.");
					usage();
				}
				break;
			case 'M' :
			case 'm' :
				if (args.length == 3) {
					restore(args[1], args[2], false);
				} else {
					System.out.println("ERROR: Bad number of parameters.");
					usage();
				}
				break;
			case 'D' :
			case 'd' :
				if (args.length == 3) {
					delete(args[1], args[2]);
				} else {
					System.out.println("ERROR: Bad number of parameters.");
					usage();
				}

				break;
			case 'c' :
			case 'C' :
				if (args.length != 4) {
					System.out.println("ERROR: Bad parameters.");
				} else {
					configure(args[1], args[2], args[3]);
				}
				break;

			case 'H' :
			case 'h' :
			case '?' :
			case '-' :
				usage();
				break;

			default :
				System.out.println("ERROR: Unknown command.");
				usage();
				break;
		}
	}

	// Usage
	public void usage() {
		System.out.println("Deployment Configuration for Autohit (2003):");
		System.out.println("  config config [name] [root] [var file]");
		System.out.println("  config save [name] [root] (config file)");
		System.out.println("  ...... save a checkpoint by name.");
		System.out.println("  config delete [name] [root]");
		System.out.println("  config restore [name] [root]");
		System.out.println(
			"  ...... restore a checkpoint.  It is destructive.");
		System.out.println("  config merge [name] [root]");
		System.out.println(
			"  ...... merge a checkpoint.  It will only overwrite named files.");
		System.out.println(
			"  The configuration called 'factory' will restore to the system to");
		System.out.println("  ...... out-of-the-box.");
	}

	/**
	 * main interface
	 */
	public static void main(String[] args) {

		try {
			DeploymentConfigure me = new DeploymentConfigure();

			// handle arguments
			if (args.length == 0) {
				System.out.println("Huh?  What did you want me to do?");
				me.usage();
				return;
			}

			me.go(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}