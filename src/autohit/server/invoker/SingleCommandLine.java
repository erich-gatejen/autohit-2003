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
package autohit.server.invoker;

import autohit.server.BasicBootstrap;
import autohit.server.command.CommandAtom;
import autohit.server.command.CommandServerLocal;

/**
 * A single command invoker.
 * 
 * @author Erich P. Gatejen
 * @version 1.0 <i>Version History</i><code>EPG - Initial - 19Jul03
 */
public class SingleCommandLine extends BasicBootstrap {

	/**
	 * Command system */
	public SimTextCommand cmd;
	public CommandServerLocal cserver;

	/**
	 *  Default constructor. */
	public SingleCommandLine() throws Exception {
		throw new Exception("Dont use the default constructor!");
	}

	/**
	 *  Properties constructor. Give it a full path to the properties file. */
	public SingleCommandLine(String rootProps) throws Exception {

		// CRITICAL TO CALL SUPER
		super(rootProps);

		// command muncher
		cmd = new SimTextCommand();
		cmd.init(sc);

		// command server
		cserver = new CommandServerLocal();
		cserver.init(sc);
	}

	/**
	 * main interface */
	public static void usage() {
		System.out.println("Single Command Line for Autohit (2003):");
		System.out.println("This will issue a command to the SimTextCommand processor.  The arguments");
		System.out.println("include the SimTextCommand and it's arguments packed as a single String.");
		System.out.println("   SingleCommandLine [prop file] [argument string]");
		System.out.println("   the classpath must be set properly.  (See documentation)");
		System.out.println("   [prop file] configuration property file, such as");
		System.out.println("               etc/default.prop");
		System.out.println("   [argument string] command parameters as a single string.");
		System.out.println("                     If invoking this from a script, be sure to");
		System.out.println("                     collect the parameters into a single string using");
		System.out.println("                     quotes.");
		System.out.println("If running this from a platform helper script, such as");
		System.out.println("/bin/command.bat, the usage may be different.  See the");
		System.out.println("Autohit User guide.");
	}

	/**
	 * logic */
	public void go(String commandString) {

		try {
			CommandAtom a = cmd.create(commandString);
			cserver.execute(a);
		} catch (Exception ee) {
			System.out.println("COMMAND FAILED due to exception.  message=" + ee.getMessage());
			ee.printStackTrace();
		}
	}

	/**
	 * main interface */
	public static void main(String[] args) {

		// handle arguments
		if (args.length == 0) {
			System.out.println("No .prop specified");
			usage();
			return;
		}

		// handle arguments
		if (args.length == 1) {
			System.out.println("No argument string given");
			usage();
			return;
		}

		try {
			SingleCommandLine me = new SingleCommandLine(args[0]);
			me.go(args[1]);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
