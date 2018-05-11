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

import autohit.call.modules.Module;
import autohit.common.Constants;
import autohit.universe.Universe;

/**
 * FREE.  Frees a module instance.
 * <pre>
 * REQURIES: logger, core
 * IGNORES: uni
 * PARAMETERS (INPUT):
 	name= name of the module to free
 * </pre>
 * RETURNS: empty string.
 *
 * @author Erich P. Gatejen
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - Initial - 22Jun03</code>
 */
public class Call_FREE extends Call {

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
		return "FREE";
	}

	/**
	 * Execute it.
	 * @return the result or null if there is no result
	 */
	public String call() throws CallException {
		String name;

		try {

			name = this.requiredString("name");
			log.debug("call:FREE.: Going to free= " + name);

			// Even if the Module.free() causes an exception, at least remove it
			// from the persist
			// DO NOT USE THE Call.class requiredPersist!!!	
			if (vmc.has(name)) {
				Module mod = (Module) vmc.get(name);
				try {
					mod.free();
				} catch (Exception e) {
					throw e;
				} finally {
					vmc.free(name);
				}

			} else {
				// if it doesn't exist, that's a problem
				throw new CallException(
					this.format("Instance of " + name + " doesn't exist."),
					CallException.CODE_CALL_ERROR);
			}

		} catch (CallException ce) {
			throw ce;
		} catch (Exception ex) {
			//any other is REAL bad
			throw new CallException(
				this.format("Serious fault.  error=" + ex.getMessage()),
				CallException.CODE_CALL_UNRECOVERABLE_FAULT,
				ex);
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
