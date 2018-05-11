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

import autohit.call.CallException;
import autohit.common.Constants;

/**
 * A stopwatch that can time in seconds or milliseconds.<p>
 *
 * start() starts the stopwatch.  if it is already running, it will reset it<br>
 * time() returns the time that has passed in seconds<br>
 * millis() returns the time that has passed in seconds<br>
 * 
 * @author Erich P. Gatejen
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - Initial - 28Jul03</code>
 */
public class StopwatchModule extends Module {

	private final static String myNAME = "Stopwatch";

	/**
	 * Start time
	 */
	private long starttime;

	/**
	 * METHODS
	 */
	private final static String method_START = "start";
	private final static String method_TIME = "time";
	private final static String method_MILLIS = "millis";

	/**
	 * Constructor
	 */
	public StopwatchModule() {

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

		if (name.equals(method_START)) {
			this.start();

		} else if (name.equals(method_TIME)) {
			response = this.time();

		} else if (name.equals(method_MILLIS)) {
			response = this.millis();

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
		// Make sure we aren't started
		starttime = System.currentTimeMillis();
		return myNAME;
	}

	/**
	 * Allow the subclass a chance to cleanup on free.  At a minium, an 
	 * implementor should create an empty method.
	 * @throws CallException
	 */
	protected void free_chain() throws CallException {
		// NOTHING AT THIS TIME
	}

	// PRIVATE IMPLEMENTATIONS

	/**
	 * Start the stopwatch
	 * @throws CallException
	 */
	private void start() throws CallException {
		// get it from system time
		starttime = System.currentTimeMillis();
	}

	/**
	 * Get the time elapsed in seconds
	 * @throws CallException
	 */
	private String time() throws CallException {

		String result = Constants.ZERO;

		// get it from system time
		long deltatime = System.currentTimeMillis() - starttime;
		deltatime = deltatime / 1000;
		return Long.toString(deltatime);
	}

	/**
	 * Get the time elapsed in seconds
	 * @throws CallException
	 */
	private String millis() throws CallException {

		String result = Constants.ZERO;

		// get it from system time
		long deltatime = System.currentTimeMillis() - starttime;
		return Long.toString(deltatime);
	}

}
