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
import autohit.common.AutohitProperties;
import autohit.server.ServerException;
import autohit.universe.UniverseException;
import autohit.vm.VMExecutableWrapper;

/**
 * The DUMP command.  It expects a single string that points
 * to the universe object to dump.
 * <p>
 * <code>
 * COMMAND LIST
 * this.assert(false,false,false,false,true,false)
 * 0-UNI 		- OPTIONAL
 * 1-RESPONSE 	- OPTIONAL
 * 2-TARGET		- OPTIONAL
 * 3-CLASS		- UNUSED
 * 4-COMMAND	- REQUIRED	- Name of object in universe to dump
 * 5-OBJECT		- UNUSED
 * </code>
 * 
 * @author Erich P. Gatejen
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - Initial - 24Jul03<
 * 
 */
public class CommandDump extends Command {

	final static long serialVersionUID = 1;
	
	/**
	 * My name
	 */
	public final static String MY_NAME = "dump";

	/**
	 * Execute the command.
	 * @throws ServerException
	 * @return return the manreadable message for success.
	 */
	public String execute() throws ServerException {

		InputStream is;
		VMExecutableWrapper ob;

		try {

			// find it
			is =
				uni.getStream(
					AutohitProperties.literal_UNIVERSE_CACHE
						+ AutohitProperties.literal_NAME_SEPERATOR
						+ command);

			// Load it
			ob = new VMExecutableWrapper();
			ob.load(is);

			// dump it
			String data = ob.toString();
			sendTarget("DUMP (from cache) for " + command, AutohitErrorCodes.EVENT_COMMAND_PARTIAL_RESULTS, null);
			sendTarget(data, AutohitErrorCodes.EVENT_COMMAND_FINAL_RESULTS, null);			

		} catch (ServerException sse) {
			throw sse;
		} catch (UniverseException ue) {
			switch (ue.numeric) {
				case UniverseException.UE_OBJECT_DOESNT_EXIST :
					throw new ServerException(
						"Could not find source in the specified universe.  Source="
							+ command,
						ue.numeric);
				case UniverseException.UE_OBJECT_LOCKED :
				case UniverseException.UE_DONT_OWN_THE_LOCK :
				case UniverseException.UE_CANNOT_STREAM :
					throw new ServerException(
						"Could not find source in the specified universe.   Universe won't let us stream to the object.",
						ue.numeric);
				default :
					throw new ServerException(
						"Failed to serious Universe exception.  message="
							+ ue.getMessage(),
						ue.numeric);
			}
		} catch (Exception eee) {
			throw new ServerException(
				"Failed to general exception.  message=" + eee.getMessage(),
				AutohitErrorCodes.CODE_COMMAND_FAULT);
		}

		// return 
		return "Dump completed with no errors";
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
