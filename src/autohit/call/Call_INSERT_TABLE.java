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
package autohit.call;

import java.util.Hashtable;

import autohit.common.Constants;
import autohit.universe.Universe;

/**
 * CALL.  INSERT_TABLE  Inserts a name(n)/value(v) into a TABLE in persist.  It will
 * replace the name(n)/value(v) is the name alredy exists.
 * <pre>
 * REQURIES: logger, core
 * IGNORES: 
 * PARAMETERS (INPUT):
 * name= name of the table in which to insert.  REQUIRED
 * n= name of the table entry  REQUIRED
 * v= value of the table entry  REQUIRED
 * </pre>
 * RETURNS: empty string.
 *
 * @author Erich P. Gatejen
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - Initial - 28Jun03 
 * 
 */
public class Call_INSERT_TABLE extends Call {

	/**
	 * Implement this to handle load time initialization.  The 
	 * four main fields will already be set--vmc, sc, log, and u.
	 * You must implement this, but you don't have to do anything.
	 * Remember that calls are cached per VM and reused as often
	 * as possible.  There will be no thread-safety issues with the
	 * VMCore or log, but the SystemContecxt and Universe may be shared.
	 * @throws CallException
	 */
	public void load_chain() throws CallException {
		// Nothing to do.
	}

	/**
	 * Implement this to return the name of the CALL
	 * @return name of the CALL
	 */
	public String name() {
		return "INSERT_TABLE";
	}

	/**
	 * Execute it.
	 * @return the result or null if there is no result
	 */
	public String call() throws CallException {

		String name = Constants.UNKNOWN;
		Hashtable table;

		try {

			// Get the params and the table.  Exceptions will handle problems
			name = this.requiredString("name");
			String n = this.requiredString("n");
			String v = this.requiredString("v");
			table = (Hashtable) this.requiredPersist(name, Hashtable.class);

			// Insert it
			table.put(n, v);

		} catch (CallException e) {
			// Trap the table errors.
			if (e.numeric == CallException.CODE_CALL_PERSISTMISMATCH_FAULT) {
				throw new CallException(
					this.format(
						"Persist object named "
							+ name
							+ " found, but it is not a TABLE."),
					CallException.CODE_CALL_PERSISTMISMATCH_FAULT,
					e);
			} else if (
				e.numeric == CallException.CODE_CALL_PERSISTNOTFOUND_FAULT) {
				throw new CallException(
					this.format("Table named " + name + " not found."),
					CallException.CODE_CALL_PERSISTNOTFOUND_FAULT,
					e);
			}
			throw e;
		}
		return Constants.EMPTY_LEFT;
	}

	/**
	 * Execute using the passed universe, rather than the loaded.
	 * @param uni a universe
	 * @return the result or null if there is no result
	 * @see autohit.universe.Universe
	 */
	public String call(Universe uni) throws CallException {
		return this.call();
	}
}
