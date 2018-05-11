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

import java.util.Enumeration;
import java.util.Hashtable;

import autohit.common.AutohitErrorCodes;
import autohit.server.ServerException;

/**
 * The PROPS command. It will dump the invoker propertiess list. It will be in
 * the form of cmdid|name|value.
 * <p>
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
 * 
 * @author Erich P. Gatejen
 * @version 1.0 <i>Version History</i><code>EPG - Initial - 27Jul03<
 */
public class CommandProps extends Command {

	final static long serialVersionUID = 1;
	
	/**
	 * My name */
	public final static String MY_NAME = "props";
	public final static String LIST_HEADER = "INVOKER PROP LIST" + RESPONSE_ELEMENT_SEPERATOR;

	/**
	 * Execute the command.
	 * 
	 * @throws ServerException
	 * @return return the manreadable message for success.
	 */
	public String execute() throws ServerException {

		String victory = "failed.";
		boolean started = false;

		// Trap all non-critical errors and just log them to the
		// responseChannel
		try {

			// Get the properties list and chug through it
			// Send FINAL_RESULTS on the last item or if the list is empty;
			Hashtable iprops = sc.getInvokerProperties();
			if (iprops.isEmpty()) {
				sendTarget(
					LIST_HEADER
						+ uniqueID
						+ RESPONSE_ELEMENT_SEPERATOR
						+ "X"
						+ RESPONSE_ELEMENT_SEPERATOR
						+ "No properties are set.",
					AutohitErrorCodes.EVENT_COMMAND_FINAL_RESULTS,
					null);

			} else {

				String key;
				String value;
				Enumeration listi = iprops.keys();
				StringBuffer text;
				while (listi.hasMoreElements()) {

					// build entry
					key = (String) listi.nextElement();
					value = (String) iprops.get(key);
					text = new StringBuffer(LIST_HEADER);
					text.append(key);
					text.append(RESPONSE_ELEMENT_SEPERATOR);
					text.append(value);

					// is it the last one?
					if (listi.hasMoreElements()) {
						sendTarget(text.toString(), AutohitErrorCodes.EVENT_COMMAND_PARTIAL_RESULTS, null);
					} else {
						sendTarget(text.toString(), AutohitErrorCodes.EVENT_COMMAND_FINAL_RESULTS, null);
					}

					started = true;

				} // end while

			} // end if empty

			// Ok, it's working
			victory = "dispatched.";

		} catch (Exception e) {

			if (started == true) {
				// We got some results out. Send a message to the target
				try {
					sendTarget(
						LIST_HEADER + uniqueID + RESPONSE_ELEMENT_SEPERATOR + "ABORTED!  Exception while listing.",
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
