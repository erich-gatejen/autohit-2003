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

import java.io.InputStream;

import autohit.common.AutohitErrorCodes;
import autohit.server.ServerException;
import autohit.vm.VM;
import autohit.vm.VMProcess;

/**
 * The RUN command.  It expects a single string that points
 * to the universe object to run. 
 * <p>
 * <code>
 * COMMAND LIST
 * this.assert(false,false,false,true,true,false)
 * 0-UNI 		- OPTIONAL
 * 1-RESPONSE 	- OPTIONAL
 * 2-TARGET		- OPTIONAL
 * 3-CLASS		- REQUIRED  - VM implementation (ie. autohit.vm.SimVM)
 * 4-COMMAND	- REQUIRED	- Name of object in universe to run
 * 5-OBJECT		- UNUSED
 * </code>
 * 
 * @author Erich P. Gatejen
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - Initial - 24Jul03<
 * 
 */
public class CommandRun extends Command {

	final static long serialVersionUID = 1;
	
	/**
	 * My name
	 */
	public final static String MY_NAME = "run";

	/**
	 * Execute the command.
	 * @throws ServerException
	 * @return return the manreadable message for success.
	 */
	public String execute() throws ServerException {

		InputStream is;
		VM myVM;
		String victory = "failed.";

		try {

			// Get the VM instance
			Class t = Class.forName(classobject);
			myVM = (VM) t.newInstance();
			myVM.init(target, command);

			// build the process and unleash
			VMProcess pcb = (sc.getKernel()).get();
			pcb.execute(myVM);

			victory =
				" 'run' dispatched.  Program is starting under pid="
					+ pcb.getPID();

		} catch (ClassNotFoundException e) {
			throw new ServerException(
				" failed because it could not load the class for the vm.  VM specified ="
					+ classobject,
				AutohitErrorCodes.CODE_COMMAND_FAULT);

		} catch (Exception e) {
			throw new ServerException(
				" failed.  There was a general Exception.  message="
					+ e.getMessage(),
				AutohitErrorCodes.CODE_COMMAND_FAULT);
		}

		// return 
		return victory;
	}

	/**
	 * Verify the Compile command. 
	 * @throws ServerException
	 * @return return the manreadable message for accepting the command.
	 */
	public String verify() throws ServerException {
		this.assertparam(false, false, false, true, true, false);
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
