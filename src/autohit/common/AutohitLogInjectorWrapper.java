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
package autohit.common;
import autohit.common.channels.Atom;
import autohit.common.channels.Injector;

/**
 * This is a helper for using channels for logging.
 *
 * @author Erich P. Gatejen
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - Rewrite - 9Apr03</code> 
 * 
 */
public class AutohitLogInjectorWrapper {

	public Injector sinjector;
	private String sender;
	private boolean debugging;
	private boolean ivePanicked;

	/**
	 *  Default constructor.
	 */
	public AutohitLogInjectorWrapper() {

	}

	/**
	 *  Default constructor.  It will put the logger autohit namespace.
	 *  It lets the instantiator handle any exceptions. 
	 * @param senderID the sender id (you can leave this blank).
	 * @param target the target injector for logging
	 */
	public void init(String senderID, Injector target) {
		sinjector = target;
		sender = senderID;
		ivePanicked = false;
		debugging = false;
	}

	/**
	 *  Log helper - debug
	 * @param msg Log message
	 * @param num numeric value
	 */
	public void debug(String msg, int num) {
		if (debugging) {
			this.log(msg, num, Atom.DEBUG);
		}
	}

	/**
	 *  Log helper - debug
	 * @param msg Log message
	 */
	public void debug(String msg) {
		if (debugging) {
			this.log(msg, AutohitErrorCodes.INFORMATIONAL, Atom.DEBUG);
		}
	}

	/**
	 *  Log helper - debug
	 * @param msg Log message
	 * @param num numeric value
	 */
	public void info(String msg, int num) {
		this.log(msg, num, Atom.ROUTINE);
	}

	/**
	 *  Log helper - debug
	 * @param msg Log message
	 */
	public void info(String msg) {
		this.log(msg, AutohitErrorCodes.INFORMATIONAL, Atom.ROUTINE);
	}

	/**
	 *  Log helper - error
	 * @param msg Log message
	 * @param num numeric value
	 */
	public void error(String msg, int num) {
		this.log(msg, num, Atom.FLASH);
	}

	/**
	 *  Log helper - error
	 * @param msg Log message
	 */
	public void error(String msg) {
		this.log(msg, AutohitErrorCodes.ERROR, Atom.FLASH);
	}

	/**
	 *  Log helper - warning
	 * @param msg Log message
	 * @param num numeric value
	 */
	public void warning(String msg, int num) {
		this.log(msg, num, Atom.PRIORITY);
	}

	/**
	 *  Log helper - warning
	 * @param msg Log message
	 */
	public void warning(String msg) {
		this.log(msg, AutohitErrorCodes.WARNING, Atom.PRIORITY);
	}

	/**
	 *  Log helper - warning
	 * @param msg Log message
	 */
	public void log(String msg, int numeric, int priority) {
		try {
			Atom a = new Atom(Atom.TYPE_LOG, priority, numeric, (Object) msg);
			a.senderID = sender;
			sinjector.post(a);
		} catch (Exception e) {
			// this is a very bad thing.
			if (!ivePanicked) {
				ivePanicked = true;
				System.out.println(
					"PANIC!  PANIC!  PANIC!  PANIC!  PANIC!  PANIC!  PANIC!  PANIC!  The logging system failed in AutohitLogInjectorWrapper.  No exception propagated.  System state undefined.  message="
						+ e.getMessage());
			}
		}
	}

	/**
	 *  Set debugging flag.  Will early filter debug statements
	 * @param f flag is true or false
	 */
	public void debugFlag(boolean f) {
		debugging = f;
	}

	/**
	 * Report debugging flag 
	 * @return debugging state
	 */
	public boolean debugState() {
		return debugging;
	}

}
