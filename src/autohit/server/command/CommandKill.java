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
package autohit.server.command;

import autohit.common.AutohitErrorCodes;
import autohit.server.ServerException;
import autohit.vm.VMProcess;

/**
 * The KILL command.  It expects a single string that is the PID of the 
 * process to kill.  It must be a parsable integer.
 * <p>
 * <code>
 * COMMAND LIST
 * this.assert(false,false,false,false,true,false)
 * 0-UNI 		- OPTIONAL
 * 1-RESPONSE 	- OPTIONAL
 * 2-TARGET		- UNUSED
 * 3-CLASS		- UNUSED
 * 4-COMMAND	- REQUIRED	- PID of the process to kill.  Must be a parsable Ingeter.
 * 5-OBJECT		- UNUSED
 * </code>
 * 
 * @author Erich P. Gatejen
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - Initial - 27Jul03<
 * 
 */
public class CommandKill extends Command {

	final static long serialVersionUID = 1;
	
	/**
	 * My name
	 */
	public final static String MY_NAME = "kill";

	/**
	 * Execute the command.
	 * @throws ServerException
	 * @return return the manreadable message for success.
	 */
	public String execute() throws ServerException {

		String victory = " failed.";
		int pid;

		// Trap all non-critical errors and just log them to the
		// responseChannel
		try {

			// See if the PID is an integer we can use
			pid = Integer.parseInt(command);

			// Kill it
			VMProcess currentProcess = (sc.getKernel()).getProcess(pid);
			if (currentProcess == null) {
				victory =
					"kill completed.  Process did not exist anyway.  PID="
						+ command;
			} else {
				// dont actually KILL the process, just stop the running VM
				currentProcess.vmStop();
				victory =
					"kill completed.  The process will die when it is done executing current instruction.  PID="
						+ command;
			}

		} catch (NumberFormatException nne) {
			throw new ServerException(
				"failed.  PID was not a valid number format.",
				AutohitErrorCodes.CODE_COMMAND_ERROR);
		} catch (Exception e) {
			throw new ServerException(
				"failed.  There was a general Exception.  message="
					+ e.getMessage(),
				AutohitErrorCodes.CODE_COMMAND_FAULT);
		}

		// return the receipt
		return victory;
	}

	/**
	 * Verify the Compile command. 
	 * @throws ServerException
	 * @return return the manreadable message for accepting the command.
	 */
	public String verify() throws ServerException {
		this.assertparam(false, false, false, false, true, false);
		return "parameters are good.";
	}

	/**
	 * Get the textual name for the command.
	 * @return return the manreadable name of this command.
	 */
	public String getName() {
		return MY_NAME;
	}
}
