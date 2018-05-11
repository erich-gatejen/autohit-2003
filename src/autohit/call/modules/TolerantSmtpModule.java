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
package autohit.call.modules;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Writer;

import org.apache.commons.net.io.Util;
import org.apache.commons.net.smtp.RelayPath;
import org.apache.commons.net.smtp.SMTPClient;
import org.apache.commons.net.smtp.SMTPConnectionClosedException;

import autohit.call.CallException;
import autohit.common.Constants;
import autohit.universe.UniverseException;
import autohit.vm.process.StringProcessors;

/**
 * Tolerant SMTP module. There is a client/per module at this time. This one
 * supports streaming and will not throw faults on every error. <code>
 * start(address,optional{port}) start an SMTP session<br>
 * login(optional{hostname}) login to peer<br>
 * sender(address) set the sender with address<br>
 * addsenderrelay(address) add to the sender relay path<br>
 * senderrelay() set the sender with sender relay path.  clear the accumulated relay path.<br>
 * recipient(address) add a recipient with address<br>
 * addrecipientrelay(address) add to the recipient relay path<br>
 * newrecipientrelay(address) start a new recipient relay path<br>
 * recipientrelay() add a recipient with recipient relay path.  clear the accumulated relay path.<br>
 * send(text) send message from text.<br>
 * senduni(uniobj) send message from universe object.<br>
 * senduniscrub(uniobj) send message from universe object.  scrub it first with variable replacements<br>
 * reset() reset the smtp state.<br>
 * done() complete a session.  It will logout and close.<br>
 * mailit(from,to,text,host,optional{port}) convenience method for sending small message.
 * mailituni(from,to,uniobj,host,optional{port}) convenience method for sending small message.
 * </code>
 * 
 * @author Erich P. Gatejen
 * @version 1.0 <i>Version History</i><code>EPG - Initial, branched from SimpleSmtpModule - 25 Dec03</code>
 */
public class TolerantSmtpModule extends Module {

	private final static String myNAME = "TolerantSmtp";
	private final static String TEMPFILE = "temp/tsmtpscrub";

	private final static String ERROR_STRING_FOR_FAILURE = "451";
	private final static int SMTP_ERROR_THRESHOLD = 300;
	// An SMTP error code

	/**
	 * METHODS
	 */
	private final static String method_START = "start";
	private final static String method_START_1_ADDRESS = "address";
	private final static String method_START_2_PORT = "port";
	private final static String method_LOGIN = "login";
	private final static String method_LOGIN_1_HOSTNAME = "hostname";
	private final static String method_SENDER = "sender";
	private final static String method_SENDER_1_ADDRESS = "address";
	private final static String method_ADDSENDERRELAY = "addsenderrelay";
	private final static String method_ADDSENDERRELAY_1_ADDRESS = "address";
	private final static String method_SENDERRELAY = "senderrelay";
	private final static String method_RECIPIENT = "recipient";
	private final static String method_RECIPIENT_1_ADDRESS = "address";
	private final static String method_ADDRECIPIENTRELAY = "addrecipientrelay";
	private final static String method_NEWRECIPIENTRELAY = "newrecipientrelay";
	private final static String method_ADDRECIPIENTRELAY_1_ADDRESS = "address";
	private final static String method_RECIPIENTRELAY = "recipientrelay";
	private final static String method_SEND = "send";
	private final static String method_SEND_1_TEXT = "text";
	private final static String method_SENDUNI = "senduni";
	private final static String method_SENDUNI_1_TEXT = "uniobj";
	private final static String method_SENDUNISCRUB = "senduniscrub";
	private final static String method_SENDUNISCRUB_1_TEXT = "uniobj";
	private final static String method_RESET = "reset";
	private final static String method_DONE = "done";
	private final static String method_MAILIT = "mailit";
	private final static String method_MAILIT_1_TO = "to";
	private final static String method_MAILIT_2_FROM = "from";
	private final static String method_MAILIT_3_TEXT = "text";
	private final static String method_MAILIT_4_HOSTNAME = "host";
	private final static String method_MAILIT_5_PORT = "port";
	private final static String method_MAILITUNI = "mailituni";
	private final static String method_MAILITUNI_1_TO = "to";
	private final static String method_MAILITUNI_2_FROM = "from";
	private final static String method_MAILITUNI_3_UNIOBJ = "uniobj";
	private final static String method_MAILITUNI_4_HOSTNAME = "host";
	private final static String method_MAILITUNI_5_PORT = "port";

	SMTPClient client;
	RelayPath senderrelay;
	RelayPath recipientrelay;
	boolean loggedIn;

	/**
	 * Constructor
	 */
	public TolerantSmtpModule() {

	}

	// IMPLEMENTORS

	/**
	 * Execute a named method. You must implement this method. You can call any
	 * of the helpers for data and services. The returned object better be a
	 * string (for now).
	 * 
	 * @param name
	 *            name of the method
	 * @see autohit.common.NOPair
	 * @throws CallException
	 */
	public Object execute_chain(String name) throws CallException {

		Object response = Constants.EMPTY_LEFT;
		Object thingie;

		if (name.equals(method_START)) {
			String param1 = this.required(method_START_1_ADDRESS, name);
			String param2 = this.optional(method_START_2_PORT);
			this.start(param1, param2);

		} else if (name.equals(method_LOGIN)) {
			String param1 = this.optional(method_LOGIN_1_HOSTNAME);
			this.login(param1);

		} else if (name.equals(method_SENDER)) {
			String param1 = this.required(method_SENDER_1_ADDRESS, name);
			this.sender(param1);

		} else if (name.equals(method_ADDSENDERRELAY)) {
			String param1 =
				this.required(method_ADDSENDERRELAY_1_ADDRESS, name);
			this.addsenderrelay(param1);

		} else if (name.equals(method_SENDERRELAY)) {
			this.senderrelay();

		} else if (name.equals(method_RECIPIENT)) {
			String param1 = this.required(method_RECIPIENT_1_ADDRESS, name);
			this.recipient(param1);

		} else if (name.equals(method_ADDRECIPIENTRELAY)) {
			String param1 =
				this.required(method_ADDRECIPIENTRELAY_1_ADDRESS, name);
			this.addrecipientrelay(param1);

		} else if (name.equals(method_NEWRECIPIENTRELAY)) {
			this.newrecipientrelay();

		} else if (name.equals(method_RECIPIENTRELAY)) {
			this.recipientrelay();

		} else if (name.equals(method_SEND)) {
			String param1 = this.required(method_SEND_1_TEXT, name);
			response = this.send(param1);

		} else if (name.equals(method_SENDUNI)) {
			String param1 = this.required(method_SENDUNI_1_TEXT, name);
			response = this.senduni(param1);

		} else if (name.equals(method_SENDUNISCRUB)) {
			String param1 = this.required(method_SENDUNISCRUB_1_TEXT, name);
			response = this.senduniscrub(param1);

		} else if (name.equals(method_RESET)) {
			this.reset();

		} else if (name.equals(method_DONE)) {
			this.done();

		} else if (name.equals(method_MAILIT)) {
			String param1 = this.required(method_MAILIT_1_TO, name);
			String param2 = this.required(method_MAILIT_2_FROM, name);
			String param3 = this.required(method_MAILIT_3_TEXT, name);
			String param4 = this.required(method_MAILIT_4_HOSTNAME, name);
			String param5 = this.optional(method_MAILIT_5_PORT);
			response = this.mailit(param1, param2, param3, param4, param5);

		} else if (name.equals(method_MAILITUNI)) {
			String param1 = this.required(method_MAILITUNI_1_TO, name);
			String param2 = this.required(method_MAILITUNI_2_FROM, name);
			String param3 = this.required(method_MAILITUNI_3_UNIOBJ, name);
			String param4 = this.required(method_MAILITUNI_4_HOSTNAME, name);
			String param5 = this.optional(method_MAILITUNI_5_PORT);
			response = this.mailituni(param1, param2, param3, param4, param5);

		} else {
			error("Not a provided method.  method=" + name);
		}
		return response;
	}

	/**
	 * Allow the subclass a chance to initialize. At a minium, an implementor
	 * should create an empty method.
	 * 
	 * @throws CallException
	 * @return the name
	 */
	protected String instantiation_chain() throws CallException {
		client = null;
		loggedIn = false;
		return myNAME;
	}

	/**
	 * Allow the subclass a chance to cleanup on free. At a minium, an
	 * implementor should create an empty method.
	 * 
	 * @throws CallException
	 */
	protected void free_chain() throws CallException {
		try {
			this.done();
		} catch (Exception e) {
			// don't care
		}
	}

	// PRIVATE IMPLEMENTATIONS

	/**
	 * Start method. It will open a connection to an SMTP server/relay. If a
	 * session is already started, it will report an error. If it cannot make
	 * the connection, it will cause a fault.
	 * 
	 * @param addr
	 *            the domain name address. Do not include protocol or port.
	 * @param post
	 *            this should be a parsable integer. If it is null, the default
	 *            will be used.
	 * @throws CallException
	 */
	private void start(String addr, String port) throws CallException {

		SMTPClient candidate = null;

		// Already started?
		if (client != null) {
			this.error("Session already started.  Ignoring new start().");
			return;
		}
		// Passed a port number?
		int portNum = 0;
		if (port != null) {
			try {
				portNum = Integer.parseInt(port);
			} catch (Exception e) {
				this.fault(
					"Malformed 'port' number.  It must be a parsable integer.  text="
						+ port);
			}
		}
		// Try and construct it
		try {
			candidate = new SMTPClient();
			if (port == null) {
				candidate.connect(addr);
			} else {
				candidate.connect(addr, portNum);
			}

		} catch (Exception ex) {
			this.error(
				"Could not connect to host.  message=" + ex.getMessage());
			return;
		}

		// NO CODE AFTER THIS!
		this.log("Connection started.");
		loggedIn = false;
		client = candidate;
	}

	/**
	 * Done method. Dispose of state and everything.
	 */
	private void done() {

		// Brute force close. Don't care about errors
		if (client != null) {
			try {
				client.logout();
			} catch (Exception exx) {
			}
			try {
				client.disconnect();
			} catch (Exception exx) {
			}
		}
		senderrelay = null;
		client = null;
		loggedIn = false;
		this.log("Connection closed.");
		// NO NEW CODE BEFORE THIS LINE!
	}

	/**
	 * Login method. It will fault if a session has not been started or has
	 * expired. The hostname is optional; pass null if not used.
	 * 
	 * @param hostname
	 *            hostname to use instead of localhost.
	 * @throws CallException
	 */
	private void login(String hostname) throws CallException {

		// Is it started?
		if (client == null) {
			this.error("Session not start()'ed.");
		}

		try {
			if (hostname == null) {
				client.login();
			} else {
				client.login(hostname);
			}

			// what happened?
			int code = client.getReplyCode();
			if (code >= SMTP_ERROR_THRESHOLD) {
				this.error(
					"Login failed with code="
						+ code
						+ " reply="
						+ client.getReplyString());
			} else {
				loggedIn = true;
				if (this.isDebugging()) {
					this.debug("Login complete.  code=" + code);
				}
			}

		} catch (SMTPConnectionClosedException ex) {
			this.done();
			this.error("Cannot login.  Connection expired and closed itself.");
		} catch (Exception ex) {
			this.done();
			this.error(
				"Cannot login due to exception.  message=" + ex.getMessage());
		}
	}

	/**
	 * Sender method. It will fault if a session has not been started or has
	 * expired. It will set the sender. Subsequent calls will overwrite the
	 * value. The address should be a valid email address.
	 * 
	 * @param hostname
	 *            hostname to use instead of localhost.
	 * @throws CallException
	 */
	private void sender(String s) throws CallException {

		// Is it started?
		if (client == null) {
			this.error("Session not start()'ed.");
		}

		try {
			client.setSender(s);

			// what happened?
			int code = client.getReplyCode();
			if (code >= SMTP_ERROR_THRESHOLD) {
				this.error(
					"Sender failed with code="
						+ code
						+ " reply="
						+ client.getReplyString());
			} else if (this.isDebugging()) {
				this.debug("Sender complete.  code=" + code);
			}

		} catch (SMTPConnectionClosedException ex) {
			this.done();
			this.error(
				"Cannot set sender.  Connection expired and closed itself.");
		} catch (Exception ex) {
			this.done();
			this.error(
				"Cannot set sender due to exception.  message="
					+ ex.getMessage());
		}
	}

	/**
	 * Add sender relay leg method. It will fault if a session has not been
	 * started or has expired. It will start accumulating a sender relay path.
	 * You must call this at least once, if you are going to use senderrelay().
	 * The first call should contain a complete email address at the root of
	 * the relay chain. The accumulation will be reset after done() or other
	 * connection ending event.
	 * 
	 * @param leg
	 *            relay leg.
	 * @throws CallException
	 */
	private void addsenderrelay(String leg) throws CallException {

		// Is it started?
		if (client == null) {
			this.fault("Session not start()'ed.");
		}

		try {
			if (senderrelay == null) {
				senderrelay = new RelayPath(leg);
			} else {
				senderrelay.addRelay(leg);
			}

		} catch (Exception ex) {
			this.done();
			this.error(
				"Cannot add sender relay due to exception.  message="
					+ ex.getMessage());
		}
	}

	/**
	 * Sender relay method. It will fault if a session has not been started or
	 * has expired. It will set the sender as the accumulated relay. You must
	 * have called addsenderrelay() at least once or you will get an error.
	 * Subsequent calls will overwrite the value.
	 * 
	 * @param hostname
	 *            hostname to use instead of localhost.
	 * @throws CallException
	 */
	private void senderrelay() throws CallException {

		// Is it started?
		if (client == null) {
			this.error("Session not start()'ed.");
		}

		// Is there a relay?
		if (senderrelay == null) {
			this.error(
				"Sender relay not ready.  You need to call addsenderrelay() at least once before this method.");
			return;
		}

		try {
			client.setSender(senderrelay);

			// what happened?
			int code = client.getReplyCode();
			if (code >= SMTP_ERROR_THRESHOLD) {
				this.error(
					"Set sender relay failed with code="
						+ code
						+ " reply="
						+ client.getReplyString());
			} else if (this.isDebugging()) {
				this.debug("Set sender relay complete.  code=" + code);
			}

		} catch (SMTPConnectionClosedException ex) {
			this.done();
			this.error(
				"Cannot set sender.  Connection expired and closed itself.");
		} catch (Exception ex) {
			this.done();
			this.error(
				"Cannot set sender due to exception.  message="
					+ ex.getMessage());
		}
	}

	/**
	 * Recipient method. It will fault if a session has not been started or has
	 * expired. It will add a recipient. Subsequent calls will add to the list
	 * of recipients. The address should be a valid email address. The only way
	 * to clear the list is to call reset() and start over.
	 * 
	 * @param hostname
	 *            hostname to use instead of localhost.
	 * @throws CallException
	 */
	private void recipient(String s) throws CallException {

		// Is it started?
		if (client == null) {
			this.error("Session not start()'ed.");
		}

		try {
			client.addRecipient(s);

			// what happened?
			int code = client.getReplyCode();
			if (code >= SMTP_ERROR_THRESHOLD) {
				this.error(
					"Add recipient failed with code="
						+ code
						+ " reply="
						+ client.getReplyString());
			} else if (this.isDebugging()) {
				this.debug("Add recipient complete.  code=" + code);
			}

		} catch (SMTPConnectionClosedException ex) {
			this.done();
			this.error(
				"Cannot add recipient.  Connection expired and closed itself.");
		} catch (Exception ex) {
			this.done();
			this.error(
				"Cannot add recipient due to exception.  message="
					+ ex.getMessage());
		}
	}

	/**
	 * Add recipeint relay leg method. It will fault if a session has not been
	 * started or has expired. It will start accumulating a recipeint relay
	 * path. You must call this at least once, if you are going to use
	 * recipient relay(). The first call should contain a complete email
	 * address at the root of the relay chain. The accumulation will be reset
	 * after done(), a call to new recipientrelay(), or some other connection
	 * ending event.
	 * 
	 * @param leg
	 *            relay leg.
	 * @throws CallException
	 */
	private void addrecipientrelay(String leg) throws CallException {

		// Is it started?
		if (client == null) {
			this.error("Session not start()'ed.");
		}

		try {
			if (recipientrelay == null) {
				recipientrelay = new RelayPath(leg);
			} else {
				recipientrelay.addRelay(leg);
			}
		} catch (Exception ex) {
			this.done();
			this.error(
				"Cannot add recipient relay due to exception.  message="
					+ ex.getMessage());
		}
	}

	/**
	 * Recipient relay method. It will fault if a session has not been started
	 * or has expired. It will set the sender as the accumulated relay. You
	 * must have called addrecipientrelay() at least once or you will get an
	 * error. Subsequent calls will overwrite the value.
	 * 
	 * @param hostname
	 *            hostname to use instead of localhost.
	 * @throws CallException
	 */
	private void recipientrelay() throws CallException {

		// Is it started?
		if (client == null) {
			this.error("Session not start()'ed.");
		}

		// Is there a relay?
		if (recipientrelay == null) {
			this.error(
				"Recipient relay not ready.  You need to call addrecipientrelay() at least once before this method.");
			return;
		}

		try {
			client.addRecipient(recipientrelay);

			// what happened?
			int code = client.getReplyCode();
			if (code >= SMTP_ERROR_THRESHOLD) {
				this.error(
					"Add recipient relay failed with code="
						+ code
						+ " reply="
						+ client.getReplyString());
			} else if (this.isDebugging()) {
				this.debug("Add recipient relay complete.  code=" + code);
			}

		} catch (SMTPConnectionClosedException ex) {
			this.done();
			this.error(
				"Cannot add recipient.  Connection expired and closed itself.");
		} catch (Exception ex) {
			this.done();
			this.error(
				"Cannot add recipient due to exception.  message="
					+ ex.getMessage());
		}	
	}
	
	/**
	 * Clear the recipeint relay accumulation. Do this if you want to start on
	 * a new recipeint relay. This will never report any kind of error.
	 * 
	 * @param leg
	 *            relay leg.
	 * @throws CallException
	 */
	private void newrecipientrelay() {
		recipientrelay = null;
	}

	/**
	 * Send the message in the text. Returns the SMTP reply code.
	 * 
	 * @param hostname
	 *            hostname to use instead of localhost.
	 * @throws CallException
	 */
	private String send(String text) throws CallException {

		String result = ERROR_STRING_FOR_FAILURE;

		// Is it started?
		if (client == null) {
			this.error("Session not start()'ed.");
		}

		// Is there something to send? PARANOID
		if (text == null) {
			this.error("Nothing to send.");
			return result;
		}

		try {

			client.sendShortMessageData(text);
			//client.completePendingCommand(); // don't care if it was ok.
			result = Integer.toString(client.getReplyCode());

			// what happened?
			int code = client.getReplyCode();
			if (code >= SMTP_ERROR_THRESHOLD) {
				this.error(
					"Message send FAILED.  code="
						+ code
						+ " reply="
						+ client.getReplyString());
			} else if (this.isDebugging()) {
				this.debug(
					"Message send complete.  code="
						+ code
						+ " reply="
						+ client.getReplyString());
			}

		} catch (SMTPConnectionClosedException ex) {
			this.done();
			this.error("Send failed.  Connection expired and closed itself." + " last reply="
					+ client.getReplyString());
		} catch (Exception ex) {
			this.done();
			this.error(
				"Send failed due to exception.  message=" + ex.getMessage());
		} 
		return result;
	}

	/**
	 * Send the message in a Universe Object. Returns the SMTP reply code.
	 * 
	 * @param hostname
	 *            hostname to use instead of localhost.
	 * @throws CallException
	 */
	private String senduni(String uniobject) throws CallException {

		String result = ERROR_STRING_FOR_FAILURE;

		// Is it started?
		if (client == null) {
			this.error("Session not start()'ed.");
		}

		try {
			// get the hoses
			InputStream unio = visUniverse.getStream(uniobject);
			BufferedReader bin =
				new BufferedReader(new InputStreamReader(unio));
			Writer mwriter = client.sendMessageData();

			// and pipe them together
			if (mwriter != null) {
				Util.copyReader(bin, mwriter);
				mwriter.close();
				unio.close();
				client.completePendingCommand(); // don't care if it was ok.

				// what happened?
				int code = client.getReplyCode();
				if (code >= SMTP_ERROR_THRESHOLD) {
					this.error(
						"Message send FAILED (from Universe).  code="
							+ code
							+ " reply="
							+ client.getReplyString());
				} else if (this.isDebugging()) {
					this.debug("Message send complete (from Universe).  code=");
				}
				result = Integer.toString(code);

			} else {
				this.log(
					"Message send FAILED (from Universe) because SMTP connection was completely ready.  reply="
						+ client.getReplyString());
			}

		} catch (UniverseException uex) {
			this.fault(
				"Could not send universe object due to Universe problem.  code="
					+ uex.numeric
					+ "  message="
					+ uex.getMessage(),
				uex);
		} catch (SMTPConnectionClosedException ex) {
			this.done();
			this.error(
				"Send failed (from Universe).  Connection expired and closed itself.  Last reply="
					+ client.getReplyString());
		} catch (Exception ex) {
			this.done();
			this.error(
				"Send failed (from Universe) due to exception.  message="
					+ ex.getMessage());
		}
		return result;
	}

	/**
	 * Send the message in a Universe Object. Returns the SMTP reply code. It
	 * will run a variable replace on it before sending it.
	 * 
	 * @param hostname
	 *            hostname to use instead of localhost.
	 * @throws CallException
	 */
	private String senduniscrub(String uniobject) throws CallException {

		String result = ERROR_STRING_FOR_FAILURE;
		String tempObj = null;

		// Is it started?
		if (client == null) {
			this.error("Session not start()'ed.");
		}

		try {

			// Process it
			try {
				tempObj = visUniverse.reserveUnique(TEMPFILE);
				OutputStream os = visUniverse.putStream(tempObj);
				StringProcessors.evalStreams2Core(visUniverse.getStream(uniobject), os, visCore);
				os.close();

			} catch (UniverseException ue) {
				throw ue;
			} catch (Exception e) {
				this.fault(
					"Send failed (from Universe).  Could not create scrubbed intermediary tempfile.  message="
						+ e.getMessage());
			}

			// get the hoses
			InputStream unio = visUniverse.getStream(tempObj);
			BufferedReader bin =
				new BufferedReader(new InputStreamReader(unio));
			Writer mwriter = client.sendMessageData();

			// and pipe them together
			if (mwriter != null) {
				Util.copyReader(bin, mwriter);
				mwriter.close();
				unio.close();
				client.completePendingCommand(); // don't care if it was ok.

				// what happened?
				int code = client.getReplyCode();
				if (code >= SMTP_ERROR_THRESHOLD) {
					this.error(
						"Message send FAILED (from Universe).  code="
							+ code
							+ " reply="
							+ client.getReplyString());
				} else if (this.isDebugging()) {
					this.debug("Message send complete (from Universe).  code=");
				}
				result = Integer.toString(code);
			
			} else {
				this.log(
					"Message send FAILED (from Universe) because SMTP connection was completely ready.  reply="
						+ client.getReplyString());
			}

		} catch (CallException cex) {
			throw cex;
		} catch (UniverseException uex) {
			this.fault(
				"Could not send universe object due to Universe problem.  code="
					+ uex.numeric
					+ "  message="
					+ uex.getMessage(),
				uex);
		} catch (SMTPConnectionClosedException ex) {
			this.done();
			this.error(
				"Send failed (from Universe).  Connection expired and closed itself.  Last reply="
					+ client.getReplyString());
		} catch (Exception ex) {
			this.done();
			this.error(
				"Send failed (from Universe) due to exception.  message="
					+ ex.getMessage());
		} finally  {
			if ((!this.isDebugging()) && (tempObj != null)) {
				try {
					visUniverse.remove(tempObj);
				} catch (Exception ee) {
					// Don't care
				}
			}
		}
		return result;
	}

	/**
	 * Reset method. It will never give an error, even if not start()'ed.
	 * 
	 * @throws CallException
	 */
	private void reset() throws CallException {
		try {
			if (client != null)
				client.reset();

			// what happened?
			int code = client.getReplyCode();
			if (code >= SMTP_ERROR_THRESHOLD) {
				this.error(
					"Reset FAILED with code="
						+ code
						+ " reply="
						+ client.getReplyString());
				this.done();
			} else if (this.isDebugging()) {
				this.debug("Reset complete.  code=" + code);
			}

		} catch (Exception ex) {
		}
	}

	/**
	 * Mailit method. A complete transaction wrapped into a convenient method.
	 * It will start a session using host/port parameters. If a session is
	 * already started, host/port will be ignored and the current session will
	 * be used.
	 * 
	 * @param to
	 *            TO address.
	 * @param from
	 *            FROM address.
	 * @param text
	 *            to send as the message
	 * @param host
	 *            address of smpt server or relay
	 * @param port
	 *            optional port
	 * @throws CallException
	 */
	private String mailit(
		String to,
		String from,
		String text,
		String host,
		String port)
		throws CallException {

		String response = null;

		// Is it started?
		if (client == null) {
			this.start(host, port);
		}
		
		if (client == null) return "XXX";
		
		try {

			if (loggedIn == false)
				this.login(null);
			this.reset();
			this.sender(from);
			this.recipient(to);
			response = this.send(text);

		} catch (CallException ce) {
			this.done();
			throw ce;
		} catch (Exception ee) {
			this.done();
			this.fault("Serious exception caused FAULT while sending.  message=" + ee.getMessage());
		}
		return response;
	}

	/**
	 * Mailituni method. A complete transaction wrapped into a convenient
	 * method. It will start a session using host/port parameters. If a session
	 * is already started, host/port will be ignored and the current session
	 * will be used.
	 * 
	 * @param to
	 *            TO address.
	 * @param from
	 *            FROM address.
	 * @param uniobj
	 *            to send as the message from universe
	 * @param host
	 *            address of smpt server or relay
	 * @param port
	 *            optional port
	 * @throws CallException
	 */
	private String mailituni(
		String to,
		String from,
		String uniobj,
		String host,
		String port)
		throws CallException {

		String response = null;

		// Is it started?
		if (client == null) {
			this.start(host, port);
		}

		if (client == null) return "XXX";
		
		try {
			if (loggedIn == false)
				this.login(null);
			this.reset();
			this.sender(from);
			this.recipient(to);
			response = this.senduniscrub(uniobj);
			
		} catch (CallException ce) {
			this.done();
			throw ce;
		} catch (Exception ee) {
			this.done();
			this.fault("Serious exception caused FAULT while sending.  message=" + ee.getMessage());
		}
		return response;
	}
}
