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

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.collections.ExtendedProperties;

import autohit.common.AutohitErrorCodes;
import autohit.server.ServerException;
import autohit.universe.UniverseException;

/**
 * The LOADPROPS command.  It will load properties to a specified
 * disk locations.  The load may or may not be destructive.  
 * <p>
 * <code>
 * COMMAND LIST
 * this.assert(false,false,false,false,true,false)
 * 0-UNI 		- OPTIONAL
 * 1-RESPONSE 	- OPTIONAL
 * 2-TARGET		- OPTIONAL
 * 3-CLASS		- UNUSED
 * 4-COMMAND	- REQUIRED
 * 5-OBJECT		- UNUSED
 * </code>
 * 
 * @author Erich P. Gatejen
 * @version 1.0 <i>Version History</i><code>EPG - Initial - 4Dec03</code>
 */
public class CommandLoadProps extends Command {

	final static long serialVersionUID = 1;
	
	/**
	 * My name */
	public final static String MY_NAME = "props";
	public final static String LIST_HEADER = "LOAD PROPERTIES" + RESPONSE_ELEMENT_SEPERATOR;

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

			// Get the properties and try to save them.
			ExtendedProperties iprops = sc.getInvokerProperties();
			InputStream is = uni.getStream(command);
			iprops.load(is);
			is.close();
			
			// Ok, it's working
			victory = "saved.";

		} catch (IOException ioe) {	
			throw new ServerException(
					"failed.  There was an IO error.  message=" + ioe.getMessage(),
					AutohitErrorCodes.EVENT_COMMAND_FAILED, ioe);
			
		} catch (UniverseException ue) {	
	
			throw new ServerException(
					"failed.  There was a Universe Exception.  message=" + ue.getMessage(),
					AutohitErrorCodes.EVENT_COMMAND_FAILED, ue);
			
		} catch (Exception e) {

			throw new ServerException(
				"failed.  There was a general Exception.  message=" + e.getMessage(),
				AutohitErrorCodes.EVENT_COMMAND_FAILED,e);
		}

		// return
		return victory;
	}

	/**
	 * Verify the Save Props command.
	 * 
	 * @throws ServerException
	 * @return return the manreadable message for accepting the command.
	 */
	public String verify() throws ServerException {
		this.assertparam(false, false, false, false, true, false);
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
