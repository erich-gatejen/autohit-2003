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

import java.util.Enumeration;

import autohit.common.AutohitProperties;
import autohit.common.channels.Atom;
import autohit.common.channels.Channel;
import autohit.common.channels.ChannelException;
import autohit.common.channels.Controller;
import autohit.common.channels.QueuedDrain;
import autohit.common.channels.SimpleChannel;
import autohit.server.ServerException;
import autohit.server.command.CommandAtom;
import autohit.server.command.CommandServerLocal;

/**
 * Command service.
 * <p>
 * 
 * @author Erich P. Gatejen
 * @version 1.0 <i>Version History</i><code>EPG - Initial - 16SEP03</code>
 */
public class CommandService extends Service {

	final private static int ACCEPT_TIMEOUT = 1000;
	final private static int CLEANUP_THRESHOLD = 200;

	/**
	 *  Channel stuff */
	private Channel commandChannel;
	private QueuedDrain queue;

	/**
	 *  Server stuff */
	CommandServerLocal cserver;

	/**
	 *  Default constructor */
	public CommandService() {
		super();
	}

	/**
	 * Complete construction. This will be called when the VM is initialized. */
	public void construct() throws ServiceException {

		try {
			// Build the drain queue
			queue = new QueuedDrain();
			queue.init(AutohitProperties.COMMAND_SERVER_DRAIN_NAME);

			// Build the command channel
			commandChannel = new SimpleChannel();
			commandChannel.register(AutohitProperties.COMMAND_SERVER_DRAIN, queue);
			commandChannel.requestLevel(AutohitProperties.COMMAND_SERVER_DRAIN, Atom.P_ALL);
			commandChannel.requestType(AutohitProperties.COMMAND_SERVER_DRAIN, Atom.TYPE_CONTROL);

			// Register it
			Controller.register(AutohitProperties.COMMAND_SERVER_STATION, commandChannel);

			// command server
			cserver = new CommandServerLocal();
			cserver.init(sc);

		} catch (Exception ee) {
			throw new ServiceException(
				"Failed constructing CommandService.  message=" + ee.getMessage(),
				ServiceException.CODE_SERVICE_STARTUP_FAULT);
		}
	}

	/**
	 * Fast loop. We spend most of our time waiting for connections. Cycle back
	 * to VM only after an accept or a timeout.
	 * 
	 * @see autohit.vm.VMException
	 */
	public void execute() throws ServiceException {

		CommandAtom ca = null;
		boolean go = true;
		String response = "unknown";

		// live until the command server throws an exception
		while (go) {

			try {

				// Get command
				ca = (CommandAtom) queue.block();

				// Dispatch
				response = cserver.execute(ca);

				// Handle response
				// burn the response for now

			} catch (ChannelException ce) {
				if (ce.numeric==ChannelException.CODE_CHANNEL_INTERRUPTED) {
					throw new ServiceException(
						"Service intterupted by channel.  Halting.  code["
							+ ce.numeric
							+ "] "
							+ ce.getMessage(),
						ServiceException.CODE_SERVICE_INTENTIONAL_HALT,
						ce);	
				} else {
					throw new ServiceException(
						"Service FAULT in CommandService caused by Channel problem.  code["
							+ ce.numeric
							+ "] "
							+ ce.getMessage(),
						ServiceException.CODE_SERVICE_GENERAL_FAULT,
						ce);		
				}
			} catch (ServerException se) {
				switch (se.numeric) {

					case ServerException.CODE_COMMAND_FAULT :
						sc.getRootLogger().error(
							"Command Service FAULT.  The service is now defunct.  code["
								+ se.numeric
								+ "] "
								+ se.getMessage(),
							se.numeric);
						throw new ServiceException(
							"Service FAULT in CommandService.  code[" + se.numeric + "] " + se.getMessage(),
							ServiceException.CODE_SERVICE_GENERAL_FAULT,
							se);

					case ServerException.CODE_COMMAND_ERROR :
					default :
						sc.getRootLogger().error(
							"Command Service ERROR.  Trying to continue.  code[" + se.numeric + "] " + se.getMessage(),
							se.numeric);
						break;
				}

			} // end catch
		} // end while

	} // end execute()

	/**
	 * Complete destroy. This will be called when the VM is finalizing. */
	public void destruct() throws ServiceException {

		try {
			// remove injectors and drains
			commandChannel.removeDrain(AutohitProperties.COMMAND_SERVER_DRAIN);

			Object thingthang;
			for (Enumeration e = commandChannel.enumInjector(); e.hasMoreElements();) {
				thingthang = e.nextElement();
				commandChannel.removeInjector((String) thingthang);
			}
		} catch (Exception efc) { //dont care
		}

		// Unregister channel
		try {
			Controller.remove(AutohitProperties.COMMAND_SERVER_STATION);
		} catch (Exception eee) {
			// don't care. probably tearing down the server anyway
		}
	} // end destruct

}
