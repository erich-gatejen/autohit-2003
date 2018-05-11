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

//import java.io.BufferedInputStream;
//import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.lang.InterruptedException;

import autohit.common.AutohitErrorCodes;
import autohit.common.AutohitLogInjectorWrapper;

/**
 * Socket Relay Helper. Basically, this a bound relay.
 * 
 * @author Erich P. Gatejen
 * @version 1.0 <i>Version History</i><code>EPG - Initial - 15Sep03</code>
 */
public class SocketRelayHelper extends Thread {

	final private static int BUFFER_SIZE = 512;

	/**
	 *  Server side */
	public Socket up;

	/**
	 *  Client side */
	public Socket down;

	// Props
	private String outAddress;
	private int outPort;

	// Links
	private SocketRelayServiceHelper uplink;
	private SocketRelayServiceHelper downlink;

	/**
	 *  Logging mechinism */
	public AutohitLogInjectorWrapper myLog;
	public boolean wireFlag;

	/**
	 *  Default constructor */
	public SocketRelayHelper() {
		super();
	}

	/**
	 * Complete construction. This will be called when the VM is initialized. */
	public void init(Socket ins, String addr, int port, AutohitLogInjectorWrapper logger, boolean wire) {
		down = ins;
		outAddress = addr;
		outPort = port;
		myLog = logger;
		wireFlag = wire;
	}

	/**
	 *  Run the context */
	public void run() {

		try {

			// Try to construct
			up = new Socket(outAddress, outPort);

			// links
			if (wireFlag) {
				SocketRelayServiceHelperLink tuplink = new SocketRelayServiceHelperLink();
				tuplink.init(down.getInputStream(), up.getOutputStream());
				uplink = (SocketRelayServiceHelper) tuplink;
				SocketRelayServiceHelperLink tdownlink = new SocketRelayServiceHelperLink();
				tdownlink.init(up.getInputStream(), down.getOutputStream());
				downlink = (SocketRelayServiceHelper) tdownlink;
			} else {
				SocketRelayServiceHelperLinkWIRE tuplink = new SocketRelayServiceHelperLinkWIRE();
				tuplink.init(down.getInputStream(), up.getOutputStream(), "uplink");
				uplink = (SocketRelayServiceHelper) tuplink;
				SocketRelayServiceHelperLinkWIRE tdownlink = new SocketRelayServiceHelperLinkWIRE();
				tdownlink.init(up.getInputStream(), down.getOutputStream(), "downlink");
				downlink = (SocketRelayServiceHelper) tdownlink;
			}
				
			// go dog go
			uplink.start();
			downlink.start();
			myLog.info(
				"SocketRelayServiceHelper: Uplink and downlink established.",
				AutohitErrorCodes.CODE_INFORMATIONAL_OK);

			// the end... This assumes that the uplink will come down if the
			// downlink socket has a problem, since the socket IO should cause
			// an exception. If this is a bad assumption, this code may ghost
			// or
			// deadlock.
			uplink.join();
			downlink.interrupt();
			myLog.info("SocketRelayServiceHelper:Relay to " + outAddress + " interrupted.  Killing link.");

		} catch (InterruptedException e) {

			try {

				uplink.interrupt();
			} catch (Exception eee) { // dont care
			}
			try {
				downlink.interrupt();
			} catch (Exception eee) { // dont care
			}

		} catch (Exception e) {

			// Generally, don't care why.
			myLog.info(
				"SocketRelayServiceHelper:Connection to " + outAddress + " died.  reason=" + e.getMessage(),
				AutohitErrorCodes.CODE_INFORMATIONAL_OK);

		} finally {

			// Brute force close these guys
			try {
				up.close();
			} catch (Exception eee) {
				// Don't care
			}
			try {
				down.close();
			} catch (Exception eee) {
				// Don't care
			}
		}
	}

	// IMBEDDED CLASS - LINK DRAIN
	class SocketRelayServiceHelper extends Thread {
		
	}
	
	// IMBEDDED CLASS - LINK DRAIN
	class SocketRelayServiceHelperLink extends SocketRelayServiceHelper {

		//BufferedInputStream in;
		//BufferedOutputStream out;
		InputStream in;
		OutputStream out;

		public void init(InputStream i, OutputStream o) {
			//in = new BufferedInputStream(i);
			//out = new BufferedOutputStream(o);
			in = i;
			out = o;
		}

		public void run() {
			int thing;

			try {

				// turn and burn
				while ((thing = in.read()) != -1) {
					out.write(thing);
				}

			} catch (Exception e) {
				// don't care. may be an interrupt.
			} finally {
				// 
				try {
					out.close();
					in.close();
				} catch (Exception e) {
					// dont care
				}
			}
		} // end run()

	} // end IMBEDDED CLASS - LINK DRAIN
	
	// IMBEDDED CLASS - LINK DRAIN
	class SocketRelayServiceHelperLinkWIRE extends SocketRelayServiceHelper {

		InputStream in;
		OutputStream out;
		String wname;

		public void init(InputStream i, OutputStream o, String  name) {

			in = i;
			out = o;
			wname = "SocketRelayService WIRE:" + name + ":";
		}

		public void run() {
			int thing;

			try {

				// turn and burn
				while ((thing = in.read()) != -1) {
					out.write(thing);
					myLog.debug(wname + thing);
				}

			} catch (Exception e) {
				// don't care. may be an interrupt.
			} finally {
				// 
				try {
					out.close();
					in.close();
				} catch (Exception e) {
					// dont care
				}
			}
		} // end run()

	} // end IMBEDDED CLASS - LINK DRAIN
}
