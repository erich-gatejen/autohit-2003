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
package autohit.server.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import autohit.common.AutohitProperties;
import autohit.common.channels.Channel;
import autohit.common.channels.ChannelException;
import autohit.common.channels.Controller;
import autohit.common.channels.Injector;
import autohit.common.channels.SimpleInjector;
import autohit.server.invoker.SimTextCommand;
import autohit.server.ServerException;
import autohit.server.command.CommandAtom;

/**
 * This as an interactive, STDIO based CLI. We'll use the AutohitLogManager for
 * targets and responses. There can be only one instance of this service, since
 * it hsa a unique injector.
 * <p>
 * The command channel (autohit.command) must be built before creating one of
 * these!!!! CommandService will do that for you.
 * <p>
 * 
 * @author Erich P. Gatejen
 * @version 1.0 <i>Version History</i><code>EPG - Initial - 19SEP03</code>
 */
public class CLIService extends Service {

	public final static String INJECTOR_NAME = AutohitProperties.COMMAND_SERVER_INJECTOR + ".CLIService";

	/**
	 *  Channel stuff */
	private Channel commandChannel;
	private Injector commandInjector;

	/**
	 *  Command stuff */
	SimTextCommand cmd;
	BufferedReader stdin = null;

	/**
	 *  Default constructor */
	public CLIService() {
		super();
	}

	/**
	 * Complete construction. This will be called when the VM is initialized.
	 */
	public void construct() throws ServiceException {
		try {

			// Build command injector
			commandInjector = new SimpleInjector();
			commandChannel = Controller.tune(AutohitProperties.COMMAND_SERVER_STATION);
			commandChannel.register(INJECTOR_NAME, commandInjector);

			// Build a text command invoker
			// local initialization
			cmd = new SimTextCommand();
			cmd.init(sc);

			// Attach streams
			stdin = new BufferedReader(new InputStreamReader(System.in));

		} catch (ChannelException cce) {
			throw new ServiceException(
				"Failed to start CLIService due to Channel Exception.  message=" + cce.getMessage(),
				ServiceException.CODE_SERVICE_STARTUP_FAULT);
		} catch (Exception ee) {
			throw new ServiceException(
				"Failed to start CLIService due to General Exception.  message=" + ee.getMessage(),
				ServiceException.CODE_SERVICE_STARTUP_FAULT);
		}
	}

	/**
	 * Fast loop. We spend most of our time waiting for connections. Cycle back
	 * to VM only after an accept or a timeout.
	 * @see autohit.vm.VMException
	 */
	public void execute() throws ServiceException {

		boolean cont = true;
		String currentLine;
		CommandAtom a;

		// open drain
		while (cont == true) {

			try {

				// Get line and see if we get a command
				currentLine = stdin.readLine();
				if (currentLine == null)
					break;
				a = cmd.create(currentLine);

				// loop if no command
				if (a == null)
					continue;

				// dispatch it
				commandInjector.post(a);

				// ok
				cont = false;

			} catch (ServerException se) {
				if (se.numeric == ServerException.CODE_SERVER_DONE) {
					// Done. Bust loop.
					throw new ServiceException(
						"CLIService ordered to stop.",
						ServiceException.CODE_SERVICE_INTENTIONAL_HALT,
						se);
				} else {
					throw new ServiceException(
						"CLIService Server error due to exception.  code=" + se.numeric + " message=" + se.getMessage(),
						ServiceException.CODE_SERVICE_GENERIC_ERROR);
				}
			} catch (ChannelException e) {
				throw new ServiceException(
					"CLIService Server HALTED because there is no command service running or command channel available.  This may be intentional during a shutdown.",
					ServiceException.CODE_SERVICE_PANIC);

			} catch (Exception e) {
				throw new ServiceException(
					"CLIService Server PANIC due to unexpected exception.  message=" + e.getMessage(),
					ServiceException.CODE_SERVICE_PANIC);
			}
		}
	}

	/**
	 * Complete destroy. This will be called when the VM is finalizing. */
	public void destruct() throws ServiceException {

	}
}
