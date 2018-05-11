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
 * SPECIAL CALL.  INSTANCE  Makes a module instance.  Each instance will
 * be a seperate entity, with it's own local variables.
 * <pre>
 * REQURIES: logger, core, uni
 * IGNORES: 
 * PARAMETERS (INPUT):
 * type= module type name.   It is a valid java classname.  REQUIRED
 * name= to call the instance.  if it already exists, nothing will happen.  REQUIRED
 * </pre>
 * RETURNS: empty string.
 *
 * @author Erich P. Gatejen
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - Initial - 21Jun03</code>
 */
public class Call_INSTANCE extends Call {

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
		return "INSTANCE";
	}

	/**
	 * Execute it.
	 * @return the result or null if there is no result
	 */
	public String call() throws CallException {

		String type;
		String name;
		Module mod;

		try {

			// Get parameters
			type = this.requiredString("type");
			name = this.requiredString("name");

			// Create it
			this.debug(
				"Creating an instance of type=" + type + " and name=" + name);
			if (vmc.has(name)) {
				this.debug("Instance of " + name + " exists and is usable.");
			} else {

				try {
					Class t = Class.forName(type);
					mod = (Module) t.newInstance();

					mod.instance(vmc, u, log, sc);

					// persist it
					vmc.persist(name, mod);

					this.debug("Instantiated a [" + name + "].");

				} catch (ClassNotFoundException ef) {
					throw new CallException(
						this.format(
							"MODULE does not exist.  error=" + ef.getMessage()),
						CallException.CODE_CALL_MODULE_CANT_LOAD_FAULT,
						ef);

				} catch (CallException ex) {
					throw new CallException(
						this.format(
							"Cannot complete instantiation of ["
								+ name
								+ "].  Not aborting, but state of CALL undefined.  Error="
								+ ex.getMessage()),
						CallException.CODE_CALL_ERROR,
						ex);

				} catch (Exception e) {
					throw new CallException(
						this.format(
							"General instantiation fault.  error="
								+ e.getMessage()),
						CallException.CODE_CALL_MODULE_CANT_LOAD_FAULT,
						e);
				}
			}

		} catch (CallException ec) {
			throw ec;
		} catch (Exception e) {
			throw new CallException(
				this.format(
					"Exception while creating instance.  error="
						+ e.getMessage()),
				CallException.CODE_CALL_UNRECOVERABLE_FAULT,
				e);
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
		String result;

		// save the uni and make sure it is put back even if there is an exception
		Universe bucket = u;
		try {
			u = uni;
			result = this.call();
		} catch (CallException e) {
			throw e;
		} finally {
			u = bucket;
		}
		return result;
	}
}
