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

import java.util.Vector;

import autohit.common.channels.Receipt;
import autohit.server.ServerException;
import autohit.server.SystemContext;

/**
 * A LOCAL Command server. This one expects to be on the same system as the
 * issuing agent (though you may be able to cheat this.) It will cache the
 * command registry on load, so you'll need to dispose it and create a new one,
 * if you want to capture changes. It will use the root logger as the response
 * channel.
 * 
 * @author Erich P. Gatejen
 * @version 1.0 <i>Version History</i><code>EPG - Initial - 25Jul03 </code>
 */
public class CommandServerLocal implements CommandServer {

	private CommandRegistry commandRegistry;
	//private Hashtable commandCache; // Command cache not supported now
	private SystemContext sc = null;

	public final static String RESPOND_FAILED = "FAILED";

	/**
	 * Initialize. You can call this as often as you want, but must be called
	 * at least once.
	 * 
	 * @param c
	 *           the SystemContext
	 * @throws ServerException
	 */
	public void init(SystemContext c) throws ServerException {
		commandRegistry = c.getCommandRegistry();
		if (commandRegistry == null)
			throw new ServerException(
				"CommandServerLocal unable to get a CommandRegistry.  Unable to finish initialization and is invalid.",
				ServerException.CODE_COMMAND_REGISTRY_FAULT);

		// Always make this last. The sc is how we tell if this server is
		// valid.
		sc = c;
	}

	/**
	 * Execute a command. Use default channel for response. If a response
	 * injector is not specified in the CommandAtom, we'll use the
	 * SystemContext root logger for the response channel.
	 * 
	 * @param cmd
	 *           is a command atom
	 * @throws ServerException
	 * @return printable string of some form. not defined by the interface.
	 */
	public String execute(CommandAtom cmd) throws ServerException {

		String result = RESPOND_FAILED;
		if (sc == null)
			throw new ServerException(
				"CommandServerLocal not initialized before execute() called",
				ServerException.CODE_SW_DETECTED_FAULT);

		// Trap any spurious exceptions
		try {

			// instantiate it
			Command co = (Command) commandRegistry.instance(cmd.numeric);

			// Make sure it has a command list
			Receipt rr;
			if (cmd.thing instanceof Vector) {
				rr = co.call(sc, (Vector) cmd.thing);

			} else {
				throw new ServerException(
					"CommandServerLocal.execute() failed to start command, since the command did not have a command list (Vector).",
					ServerException.CODE_COMMAND_ERROR);
			}

			// if the result is acceptable, return it
			if (rr != null) {
				result = rr.toString();
			}

		} catch (ServerException se) {
			throw se;
		} catch (Exception ee) {
			throw new ServerException(
				"CommandServerLocal.execute() failed to gross exception.  message=" + ee.getMessage(),
				ServerException.CODE_COMMAND_FAULT);
		}
		return result;
	}

}
