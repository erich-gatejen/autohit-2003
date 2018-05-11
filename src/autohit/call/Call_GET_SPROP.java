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

import org.apache.commons.collections.ExtendedProperties;

import autohit.common.Constants;
import autohit.universe.Universe;

/**
 * GET_SPROP call.Returns a system property.If there is no system
 * property with the passed name, then return an empty string.
 * <pre> 
 * REQURIES : logger, core,	sc 
 * IGNORES : uni 
 * PARAMETERS(INPUT) : 
 * name = name of entry 
 * </pre>
 *
 * @author Erich P. Gatejen
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - Initial - 5Aug03</code>
 */
public class Call_GET_SPROP extends Call {

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
		return "GET_SPROP";
	}

	/**
	 * Execute it.
	 * @return the result or null if there is no result
	 */
	public String call() throws CallException {

		String name;
		String result = Constants.EMPTY_LEFT;

		try {
			
			name = this.requiredString("name");
			ExtendedProperties ep = sc.getPropertiesSet();
			if (ep.containsKey(name)) {
				result = ep.getString(name);
			}

		} catch (CallException e) {
			throw e;
		} catch (Exception ex) {
			//any other is REAL bad
			throw new CallException(
				this.format("Serious fault.  error=" + ex.getMessage()),
				CallException.CODE_CALL_UNRECOVERABLE_FAULT,
				ex);
		}
		return result;
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
