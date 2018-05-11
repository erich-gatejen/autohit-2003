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

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import autohit.call.CallException;
import autohit.common.AutohitErrorCodes;
import autohit.common.Constants;
import autohit.universe.UniverseException;

/**
 * Text writer module.  It will write strings and lines to a 
 * universe object or a buffer.<p>
 *
 * startbuffer(buffer) start a write to a buffer.  Be sure to pass the buffer by reference, rather than value.<br>
 * startuni(objname) start a write to a universe object<br>
 * write(string) write a string<br>
 * writeln(string) write a line terminated string<br>
 * done() close the write read (do this for either type, please)<br>
 * 
 * @author Erich P. Gatejen
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - Initial - 7Jul03 
 */
public class TextWriterModule extends Module {

	private final static String myNAME = "TextWriter";

	/**
	 * Current destination, if universe object
	 */
	private BufferedWriter out;

	/**
	 * Current destination, if buffer
	 */
	private StringBuffer buf;

	/**
	 * Flag if this is a buffer, rather than a universe item
	 */
	private boolean isBuffer;

	/**
	 * Meaning it has started
	 */
	private boolean isValid;

	/**
	 * Line seperation sequence of host system
	 */
	private String lineSep;

	/**
	 * METHODS
	 */
	private final static String method_STARTSTR = "startbuffer";
	private final static String method_STARTSTR_1_BUFFERNAME = "buffer";
	private final static String method_STARTUNI = "startuni";
	private final static String method_STARTUNI_1_OBJNAME = "objname";
	private final static String method_WRITE = "write";
	private final static String method_WRITE_1_STRING = "string";
	private final static String method_WRITELINE = "writeln";
	private final static String method_WRITELINE_1_STRING = "string";
	private final static String method_DONE = "done";

	/**
	 * Constructor
	 */
	public TextWriterModule() {

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
			Object paramb =
				this.requiredType(
					method_STARTSTR_1_BUFFERNAME,
					StringBuffer.class, name);
			this.startbuffer((StringBuffer) paramb);

		} else if (name.equals(method_STARTUNI)) {
			param1 = this.required(method_STARTUNI_1_OBJNAME, name);
			this.startuni(param1);

		} else if (name.equals(method_WRITE)) {
			param1 = this.required(method_WRITE_1_STRING, name);
			this.writestring(param1);

		} else if (name.equals(method_WRITELINE)) {
			param1 = this.required(method_WRITELINE_1_STRING, name);
			this.writelnstring(param1);

		} else if (name.equals(method_DONE)) {
			this.done();

		} else {
			error("Not a provided method.  method=" + name);
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

		lineSep = System.getProperty("line.separator");
		out = null;

		// Make sure we aren't started
		isValid = false;
		isBuffer = false;
		return myNAME;
	}

	/**
	 * Allow the subclass a chance to cleanup on free.  At a minium, an 
	 * implementor should create an empty method.
	 * @throws CallException
	 */
	protected void free_chain() throws CallException {
		// NOTHING AT THIS TIME
		if (out != null) {
			try {
				out.close();
			} catch (Exception e) {
				// Ignore
			}
		}
	}

	// PRIVATE IMPLEMENTATIONS

	/**
	 * Start buffer method.  It will set the writer to a buffer in the core
	 * named the target.  If it can't find the buffer (StringBuffer), it will throw a fault.
	 * If a session is already started, it will throw a fault.
	 * @param the buffer we are going to write to
	 * @throws CallException
	 */
	private void startbuffer(StringBuffer target) throws CallException {

		// Invalidate the stream first
		isValid = false;
		if (out != null) {
			throw buildException(
				"Tried to startstring a session over an existing session.  You must call done() first to end the prior session.",
				CallException.CODE_MODULE_FAULT);
		}

		// Validate it
		buf = target;
		isBuffer = true;
		isValid = true;
	}

	/**
	 * Start method.  It will set the write to stream from a universe object.
	 * If a session is already started, it will throw a fault.
	 * @param target the string we are going to read
	 * @throws CallException
	 */
	private void startuni(String name) throws CallException {

		// Invalidate the stream first
		isValid = false;

		if (out != null) {
			throw buildException(
				"Tried to startuni a session over an existing session.  You must call done() first to end the prior session.",
				CallException.CODE_MODULE_FAULT);
		}

		try {

			OutputStream os = visUniverse.putStream(name);
			out = new BufferedWriter(new OutputStreamWriter(os));

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

		// validate it
		isBuffer = false;
		isValid = true;
	}

	/**
	 * Closes the session.
	 * @throws CallException
	 */
	private void done() throws CallException {

		if (isValid) {
			isValid = false;

			if (out != null) {
				try {
					out.close();
				} catch (Exception e) {
					// ignore
				}
			}

		} else {
			error("Called done() when it wasn't started.");
		}
	}

	/**
	 * write(string) Writes a string.
	 * @param item what to write.
	 * @return the next line or an empty string
	 */
	private void writestring(String item) throws CallException {

		if (isValid) {

			if (isBuffer) {
				buf.append(item);

			} else {

				// This is a write to a universe stream.  Any IO exception
				// is a serious problem.  Invalidate the whole thing.
				try {
					out.write(item);
				} catch (Exception e) {
					// Serious problem
					isValid = false;
					try {
						this.free_chain();
					} catch (Exception ee) {
					}
					throw new CallException(
						"module:"
							+ myNAME
							+ ":FAULT.  Encountered an IO problem while writing to a Universe stream.  The session is now invalid.  exception="
							+ e.getMessage(),
						AutohitErrorCodes.CODE_CALL_FAULT);
				}
			} // end if buffer

		} else {
			error("Called write(string) when it isn't started.");
		}
	}

	/**
	 * writeln(string) Writes a string and adds a line terminator.
	 * @param item what to write.
	 * @return the next line or an empty string
	 */
	private void writelnstring(String item) throws CallException {

		if (isValid) {

			if (isBuffer) {
				buf.append(item);
				buf.append(lineSep);

			} else {

				// This is a write to a universe stream.  Any IO exception
				// is a serious problem.  Invalidate the whole thing.
				try {
					out.write(item);
					out.newLine();
				} catch (Exception e) {
					// Serious problem
					isValid = false;
					try {
						this.free_chain();
					} catch (Exception ee) {
					}
					throw new CallException(
						"module:"
							+ myNAME
							+ ":FAULT.  Encountered an IO problem while writing to a Universe stream.  The session is now invalid.  exception="
							+ e.getMessage(),
						AutohitErrorCodes.CODE_CALL_FAULT);
				}
			} // end if buffer

		} else {
			error("Called write(string) when it isn't started.");
		}
	}

}
