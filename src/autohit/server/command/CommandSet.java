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

/**
 * The SET command.  This will set and invoker property.
 * It expects a name value pair, seperated by a '='.  Everything
 * to the right of the equals sign to the end of the string will be put in the
 * property.
 * <p>
 * <code>
 * COMMAND LIST
 * this.assert(false,false,false,false,true,false)
 * 0-UNI 		- OPTIONAL
 * 1-RESPONSE 	- OPTIONAL
 * 2-TARGET		- UNUSED
 * 3-CLASS		- UNUSED
 * 4-COMMAND	- REQUIRED	- The name/value specification (name=value text)
 * 5-OBJECT		- UNUSED
 * </code>
 * 
 * @author Erich P. Gatejen
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - Initial - 30Jul03</code>
 * 
 */
public class CommandSet extends Command {

	final static long serialVersionUID = 1;
	
	/**
	 * My name
	 */
	public final static String MY_NAME = "set";

	/**
	 * Execute the command.
	 * @throws ServerException
	 * @return return the manreadable message for success.
	 */
	public String execute() throws ServerException {

		String victory = "failed.";

		try {

			String name;
			String value;

			int pivot = command.indexOf('=');
			if (pivot < 1) {
				throw new ServerException(
					"Malformed name/value pair.",
					AutohitErrorCodes.CODE_COMMAND_ERROR);
			}

			name = command.substring(0, pivot).trim();
			value = command.substring(pivot + 1);

			// Add to invoker properties
			sc.getInvokerProperties().put(name, value);

			// Report it
			victory = "completed.  Property " + name + " added (or replaced).";

		} catch (IndexOutOfBoundsException ie) {
			throw new ServerException(
				"Malformed name/value pair.",
				AutohitErrorCodes.CODE_COMMAND_ERROR);
		} catch (Exception eee) {
			throw new ServerException(
				"Failed to general exception.  message=" + eee.getMessage(),
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
