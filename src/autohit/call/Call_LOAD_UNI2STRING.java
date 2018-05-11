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

import autohit.common.Constants;
import autohit.universe.Universe;
import autohit.universe.UniverseException;
import autohit.universe.UniverseUtils;

/**
 * LOAD_UNI2STRING.  Load a universe object to a store string.
 * <pre>
 * REQURIES: logger, core
 * IGNORES: uni
 * PARAMETERS (INPUT):
 *	uniobj= universe object to load
 * </pre>
 * RETURNS: The string
 *
 * @author Erich P. Gatejen
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - Initial - 12Sep03</code>
 * 
 */
public class Call_LOAD_UNI2STRING extends Call {

	// total number of times to try and find a unique file.
	public final static int MAX_TRIES = 5;

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
		return "LOAD_UNI2STRING";
	}

	/**
	 * Execute it.
	 * @return the result or null if there is no result
	 */
	public String call() throws CallException {
		return this.call(u);
	}

	/**
	 * Execute using the passed universe, rather than the loaded.
	 * @param uni a universe
	 * @return the result or null if there is no result
	 * @see autohit.universe.Universe
	 */
	public String call(Universe uni) throws CallException {
		String result = Constants.EMPTY_LEFT;

		try {

			// Get the uniobj name
			String name = (String) this.requiredString("uniobj");

			// Load it
			result = UniverseUtils.load2String(uni.getStream(name));

		} catch (UniverseException ue) {
			throw new CallException(
				this.format(
					"Failed with Universe exception.  message="
						+ ue.getMessage()),
				CallException.CODE_MODULE_FAULT,
				ue);
		} catch (CallException cce) {
			throw cce;
		} catch (Exception e) {
			throw new CallException(
				this.format(
					"Exception while trying to load.  error=" + e.getMessage()),
				CallException.CODE_CALL_ERROR,
				e);
		}
		return result;
	}
}
