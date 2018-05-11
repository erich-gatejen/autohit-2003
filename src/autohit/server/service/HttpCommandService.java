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

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashSet;
import java.util.Iterator;

import autohit.common.AutohitErrorCodes;
import autohit.common.AutohitProperties;
import autohit.common.Utils;
import autohit.common.channels.Channel;
import autohit.common.channels.Controller;
import autohit.common.channels.Injector;
import autohit.common.channels.SimpleInjector;
import autohit.server.invoker.SimTextCommand;

/**
 * This is a socket relay service. It's a dumb relay for sockets. Don't ask why
 * I did this. It will accept connections on port
 * AutohitProperties.value_SOCKETRELAY_SERVER_PORT. It'll relay them to the
 * current property settings for
 * AutohitProperties.SERVICE_SOCKETRELAY_DESTINATION_ADDR and
 * AutohitProperties.SERVICE_SOCKETRELAY_DESTINATION_PORT for address and port,
 * respectively. If either is not set, a default will be used, though there is
 * little utility in the default.
 * <p>
 * <p>
 * The relay will stay active until either side drops the connection. There is
 * absolutely no filtering or logging.
 * 
 * @author Erich P. Gatejen
 * @version 1.0 <i>Version History</i><code>EPG - Initial - 12SEP03</code>
 */
public class HttpCommandService extends Service {

	final private static int ACCEPT_TIMEOUT = 2000;
	final private static int CLEANUP_THRESHOLD = 200;
	
	public final static String INJECTOR_NAME = AutohitProperties.COMMAND_SERVER_INJECTOR + ".HttpCommandService";

	/**
	 *  accept port */
	ServerSocket listen;

	/**
	 *  active ports */
	HashSet active;
	
	/**
	 *  Channel stuff 
	 *  */
	private Channel commandChannel;
	private Injector commandInjector;
	private SimTextCommand commander;


	/**
	 *  Default constructor */
	public HttpCommandService() {
		super();
	}

	/**
	 * Complete construction. This will be called when the VM is initialized. */
	public void construct() throws ServiceException {

		try {
			
			// How are we confiugred
			int port = AutohitProperties.default_HTTPCOMMAND_SERVER_PORT;
			String portString =
				(String) Utils.testGetProperty(
					AutohitProperties.SERVICE_HTTPCOMMAND_SERVER_PORT,
					sc.getPropertiesSet());
			if (portString != null) {
				port = Integer.parseInt(portString);
			} 
			// Set up communication
			listen = new ServerSocket(port);
			active = new HashSet();
			
			// command interface
			commandInjector = new SimpleInjector();
			commandChannel = Controller.tune(AutohitProperties.COMMAND_SERVER_STATION);
			commandChannel.register(INJECTOR_NAME, commandInjector);
			
			commander = new SimTextCommand();
			commander.init(sc);
	
		} catch (NumberFormatException nee) {
			throw new ServiceException(
				"Failed constructing HttpCommandService.  Bad value for property " +
				AutohitProperties.SERVICE_HTTPCOMMAND_SERVER_PORT + ". message=" + nee.getMessage(),
				ServiceException.CODE_SERVICE_STARTUP_FAULT);
				
		} catch (Exception ee) {
			throw new ServiceException(
				"Failed constructing HttpCommandService.  message=" + ee.getMessage(),
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

		Socket accepted;

		// Try to accept
		try {

			// Do we need to clean the active set?
			if (active.size() > CLEANUP_THRESHOLD) {
				this.cleanActive();
			}

			// ACCEPT
			listen.setSoTimeout(ACCEPT_TIMEOUT);
			accepted = listen.accept();
			myLog.debug("HttpCommandService:Accepted a connection.", AutohitErrorCodes.CODE_DEBUGGING_SERVICES);

			// Build helper
			HttpCommandHelper srh = new HttpCommandHelper();
			srh.init(accepted, myLog, commandInjector, commander, this);
			myLog.debug(
				"HttpCommandService:Relay helper initialized.",
				AutohitErrorCodes.CODE_DEBUGGING_SERVICES);

			// dispatch
			srh.start();
			active.add(srh);
			myLog.debug("HttpCommandService:Relay helper dispatched.", AutohitErrorCodes.CODE_DEBUGGING_SERVICES);

		} catch (SocketTimeoutException ste) {
			// This is ok. See if we are dead.
			if (this.getState() < HttpCommandService.STATE_ACTIVE_THRESHOLD) 
				throw new ServiceException(
						"HttpCommandService ordered to stop.",
						ServiceException.CODE_SERVICE_INTENTIONAL_HALT);

		} catch (Exception ee) {
			throw new ServiceException(
				"Fault in HttpCommandService.  message=" + ee.getMessage(),
				ServiceException.CODE_SERVICE_GENERAL_FAULT);
		}
	}

	/**
	 * Helper */
	private void cleanActive() throws Exception {

		Iterator i = active.iterator();

		while (i.hasNext()) {
			HttpCommandHelper srh = (HttpCommandHelper) i.next();
			if (!srh.isAlive())
				active.remove(srh);
		}
	}

	/**
	 * Complete destroy. This will be called when the VM is finalizing. */
	public void destruct() throws ServiceException {

		Iterator i = active.iterator();

		while (i.hasNext()) {
			HttpCommandHelper srh = (HttpCommandHelper) i.next();
			active.remove(srh);
			if (srh.isAlive()) {
				try {
					srh.interrupt();
				} catch (Exception eee) { // dont care
				}
			}
		} // end while

	} // end destruct

}
