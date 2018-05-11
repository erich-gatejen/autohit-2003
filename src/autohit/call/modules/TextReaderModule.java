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
import java.io.StringReader;
import java.util.StringTokenizer;

import autohit.call.CallException;
import autohit.common.Constants;
import autohit.universe.UniverseException;

/**
 * Text reader module.  It'll supply lines or tokens out of a 
 * text source.  The source can be a String or a Universe object.
 * If you start a new session over an old one, it will throw a
 * fault.  You must call done() first.<p>
 *
 * startstring(string) start a read on a target string<br>
 * startuni(objname) start a read on a universe object<br>
 * line() get the next full line.  if reading tokens in a line, it will
 *        give the whole contents of the current line.<br>
 * token() get the next whitespace delimited token.  if there are no more
 *         tokens in the current line, it will eat lines until it finds one.<br>
 * hasmore() return "true" if there are more tokens and/or lines, else false.<br>
 * done() close read (do this for either type, please)<br>
 * 
 * @author Erich P. Gatejen
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - Initial - 7Jul03 
 */
public class TextReaderModule extends Module {

	private final static String myNAME = "TextReader";

	/**
	 * Current source
	 */
	private BufferedReader in;

	/**
	 * Flag is the reader is valid, meaning it has more to read
	 */
	private boolean valid;

	/**
	 * Line we are currently reading
	 */
	private String currentLine;

	/**
	 * Line we are currently tokenizing
	 */
	private StringTokenizer currentTokens;

	/**
	 * METHODS
	 */
	private final static String method_STARTSTR = "startstring";
	private final static String method_STARTSTR_1_STRING = "string";
	private final static String method_STARTUNI = "startuni";
	private final static String method_STARTUNI_1_NAME = "objname";
	private final static String method_LINE = "line";
	private final static String method_TOKEN = "token";
	private final static String method_HASMORE = "hasmore";
	private final static String method_DONE = "done";

	/**
	 * Constructor
	 */
	public TextReaderModule() {

	}

	// IMPLEMENTORS

	/**
	 * Execute a named method.  You must implement this method.
	 * You can call any of the helpers for data and services.
	 * The returned object better be a string (for now).
	 * @param name name of the method
	 * @see autohit.common.NOPair
	 * @throws CallException
	 */
	public Object execute_chain(String name) throws CallException {

		Object response = Constants.EMPTY_LEFT;
		String param1;

		if (name.equals(method_STARTSTR)) {
			param1 = this.required(method_STARTSTR_1_STRING,name);
			this.startstring(param1);

		} else if (name.equals(method_STARTUNI)) {
			param1 = this.required(method_STARTUNI_1_NAME,name);
			this.startuni(param1);

		} else if (name.equals(method_LINE)) {
			response = this.line();

		} else if (name.equals(method_TOKEN)) {
			response = this.token();

		} else if (name.equals(method_HASMORE)) {
			response = this.hasmore();

		} else if (name.equals(method_DONE)) {
			this.done();

		} else {
			error(
				"Not a provided method.  method=" + name);
			response = Constants.EMPTY_LEFT;
		}
		return response;
	}

	/**
	 * Allow the subclass a chance to initialize.  At a minium, an 
	 * implementor should create an empty method.
	 * @throws CallException
	 * @return the name
	 */
	protected String instantiation_chain() throws CallException {
		// Make sure we aren't started
		in = null;
		valid = false;
		return myNAME;
	}

	/**
	 * Allow the subclass a chance to cleanup on free.  At a minium, an 
	 * implementor should create an empty method.
	 * @throws CallException
	 */
	protected void free_chain() throws CallException {
		// NOTHING AT THIS TIME
		if (in != null) {
			try {
				in.close();
			} catch (Exception e) {
				// Ignore
			}
		}
	}

	// PRIVATE IMPLEMENTATIONS

	/**
	 * Start method.  It will set the reader to the string.
	 * If a session is already started, it will throw a fault.
	 * @param target the string we are going to read
	 * @throws CallException
	 */
	private void startstring(String target) throws CallException {

		// Invalidate the stream first
		valid = false;

		if (in != null) {
			throw buildException(
				"Tried to startstring a session over an existing session.  You must call done() first to end the prior session.",
				CallException.CODE_MODULE_FAULT);
		}
		try {
			in = new BufferedReader(new StringReader(target));
			if (this.eat()) {
				// It's a valid stream
				valid = true;
			}
		} catch (Exception e) {
			throw buildException(
				"Startstring failed to exception.  message="
					+ e.getMessage(),
			CallException.CODE_MODULE_FAULT,
				e);
		}
	}

	/**
	 * Start method.  It will set the reader to stream from a universe object.
	 * If a session is already started, it will throw a fault.
	 * @param target the string we are going to read
	 * @throws CallException
	 */
	private void startuni(String name) throws CallException {

		// Invalidate the stream first
		valid = false;

		if (in != null) {
			throw buildException(
				"Tried to startuni a session over an existing session.  You must call done() first to end the prior session.",
			CallException.CODE_MODULE_FAULT);
		}

		try {

			InputStream is = visUniverse.getStream(name);
			in = new BufferedReader(new InputStreamReader(is));
			if (this.eat()) {
				// It's a valid stream
				valid = true;
			}

		} catch (UniverseException ue) {
			throw new CallException(
				"Startuni failed with Universe exception.  message="
					+ ue.getMessage(),
			CallException.CODE_MODULE_FAULT,
				ue);
		} catch (Exception e) {
			throw new CallException(
				"Startuni failed to general exception.  message="
					+ e.getMessage(),
			CallException.CODE_MODULE_FAULT,
				e);
		}
	}

	/**
	 * Closes the session.
	 * @throws CallException
	 */
	private void done() throws CallException {
		if (in == null) {
			error(
				"module:" + myName + ":called done() when it wasn't started.");
			return;
		}
		try {
			valid = false;
			in.close();
		} catch (Exception e) {
			// ignore
		}
		in = null;
	}

	/**
	 * line() return the next line.  If there is no next line, it will
	 * return an empty string.
	 * @return the next line or an empty string
	 */
	private String line() {

		String result = Constants.EMPTY_LEFT;

		if (valid) {
			result = currentLine;
			valid = this.eat();

		} else {
			debug(
				"Line() from an empty source.  Returning empty string.");
		}
		return result;
	}

	/**
	 * token() return the token.  If there is no next token, it will
	 * return an empty string.
	 * @return the next line or an empty string
	 */
	private String token() {

		String result = Constants.EMPTY_LEFT;

		if (valid) {

			if (currentTokens.hasMoreTokens()) {
				result = currentTokens.nextToken();
			} else {
				valid = this.eat();
				if (valid == true) {
					result = currentTokens.nextToken();
				}
			}

		} else {
			debug(
				"Token() from an empty source.  Returning empty string.");
		}
		return result;
	}

	/**
	 * hasmore()  return "true" if there are more tokens and/or lines, else "false."
	 * @return "true" if there is more, otherwise "false"
	 */
	private String hasmore() {

		// Assume it will fail
		if (valid) {
			if (currentTokens.hasMoreTokens()) {
				// current line has the goods
				return Constants.TRUE;
			} else {
				// see if the next line has the goods
				valid = this.eat();
				if (valid) {
					return Constants.TRUE;
				}
			}		
		}
		return Constants.FALSE;
	}

	// HELPERS

	/**
	 * chew lines until one is found with a token or there is nothing
	 * left to chew
	 * @return true if successful, false if we ran out
	 */
	private boolean eat() {

		try {
			currentLine = in.readLine();
			while (currentLine != null) {
				currentTokens = new StringTokenizer(currentLine);
				if (currentTokens.hasMoreTokens())
					return true;
				currentLine = in.readLine();
			}
		} catch (Exception e) {
			// Don't care.  false will bubble out
		}
		return false;
	}

}
