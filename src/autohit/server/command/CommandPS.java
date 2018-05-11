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

import java.util.List;
import java.util.ListIterator;

import autohit.common.AutohitErrorCodes;
import autohit.server.ServerException;
import autohit.vm.VM;
import autohit.vm.VMProcess;

/**
 * The PS command. It will dump the process list. It will be in the form of
 * cmdid|pid|state numeric|state name|root program.
 * <p>
 * <code>
 * <code>
 * COMMAND LIST
 * this.assert(false,false,false,false,true,false)
 * 0-UNI 		- OPTIONAL
 * 1-RESPONSE 	- OPTIONAL
 * 2-TARGET		- OPTIONAL
 * 3-CLASS		- UNUSED
 * 4-COMMAND	- UNUSED
 * 5-OBJECT		- UNUSED
 * </code>
 * </code>
 * 
 * @author Erich P. Gatejen
 * @version 1.0 <i>Version History</i><code>EPG - Initial - 27Jul03<
 */
public class CommandPS extends Command {

	final static long serialVersionUID = 1;
	
	/**
	 * My name */
	public final static String MY_NAME = "ps";
	public final static String LIST_HEADER = "PROCESSLIST" + RESPONSE_ELEMENT_SEPERATOR;

	/**
	 * Execute the command.
	 * 
	 * @throws ServerException
	 * @return return the manreadable message for success.
	 */
	public String execute() throws ServerException {

		String victory = "failed.";

		VMProcess currentProcess;
		StringBuffer text;
		boolean started = false;

		// Trap all non-critical errors and just log them to the
		// responseChannel
		try {

			// Get the process list and chug through it
			// Send FINAL_RESULTS on the last item or if the list is empty;
			List pcbList = (sc.getKernel()).getProcessList();
			if ((pcbList == null) || (pcbList.size() <= 0)) {

				sendTarget(
					LIST_HEADER
						+ uniqueID
						+ RESPONSE_ELEMENT_SEPERATOR
						+ "X"
						+ RESPONSE_ELEMENT_SEPERATOR
						+ "No processes are running.",
					AutohitErrorCodes.EVENT_COMMAND_FINAL_RESULTS,
					null);

			} else {

				ListIterator listi = pcbList.listIterator();
				while (listi.hasNext()) {

					// build entry
					currentProcess = (VMProcess) listi.next();
					text = new StringBuffer(LIST_HEADER);
					text.append(uniqueID);
					text.append(RESPONSE_ELEMENT_SEPERATOR);
					text.append(currentProcess.getPID());
					text.append(RESPONSE_ELEMENT_SEPERATOR);
					switch (currentProcess.getState()) {
						case VM.STATE_NEW :
							text.append("new");
							break;
						case VM.STATE_PAUSED :
							text.append("paused");
							break;
						case VM.STATE_RUNNING :
							text.append("running");
							break;
						default :
							text.append("unknown");
							break;
					}
					text.append(RESPONSE_ELEMENT_SEPERATOR);
					text.append(currentProcess.getRootProgram());

					// is it the last one?
					if (listi.hasNext()) {
						sendTarget(text.toString(), AutohitErrorCodes.EVENT_COMMAND_PARTIAL_RESULTS, null);
					} else {
						sendTarget(text.toString(), AutohitErrorCodes.EVENT_COMMAND_FINAL_RESULTS, null);
					}
					started = true;

				} // end while

			} // end if empty

			// Ok, it's working
			victory = "ps dispatched.";

		} catch (Exception e) {

			if (started == true) {
				// We got some results out. Send a message to the target
				try {
					sendTarget(
						LIST_HEADER
							+ uniqueID
							+ RESPONSE_ELEMENT_SEPERATOR
							+ "ABORTED!  Exception while listing processes.",
						AutohitErrorCodes.EVENT_COMMAND_FAULTED,
						null);
				} catch (Exception eee) {
					// no point. it WILL happen again
				}
			}
			throw new ServerException(
				"failed.  There was a general Exception.  message=" + e.getMessage(),
				AutohitErrorCodes.EVENT_COMMAND_FAILED);
		}

		// return
		return victory;
	}

	/**
	 * Verify the Compile command.
	 * 
	 * @throws ServerException
	 * @return return the manreadable message for accepting the command.
	 */
	public String verify() throws ServerException {
		this.assertparam(false, false, false, false, false, false);
		return "parameters are good.";
	}

	/**
	 * Get the textual name for the command.
	 * 
	 * @return return the manreadable name of this command.
	 */
	public String getName() {
		return MY_NAME;
	}
}
