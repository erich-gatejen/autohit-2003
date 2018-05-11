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
import autohit.vm.VM;
import autohit.vm.VMProcess;

/**
 * CHECK_PROCESS call.  It will see if a PID is still alive.  It will return TRUE if it is, otherwise FALSE.
 * <pre>
 * REQURIES: logger, core
 * IGNORES: uni
 * PARAMETERS (INPUT):
 * pid= PID for the process checked.
 * </pre>
 * RETURNS: It will return TRUE if it is, otherwise FALSE.
 *
 * @author Erich P. Gatejen
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - Initial - 7APR05</code> 
 * 
 */
public class Call_CHECK_PROCESS extends Call {

	// Trim the size of result logging in debug mode
	final static private int TRIM_SIZE = 500;

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
		return "CHECK_PROCESS";
	}

	/**
	 * Execute it.
	 * @return the result or null if there is no result
	 */
	public String call() throws CallException {

		String result = Constants.FALSE;
		VM myVM;
		String name = null;

		try {

			// see if the parameter is passed
			name = this.requiredString("pid");
			int pid = Integer.parseInt(name);
			
			// Get the process and see if it is there
			VMProcess pcb = sc.getKernel().getProcess(pid);
			if (pcb == null)  result = Constants.FALSE;
			else if (pcb.getState() >= VM.STATE_ACTIVE_THRESHOLD) {
				result = Constants.TRUE;
			}
			
		} catch (NumberFormatException ne) {
			// Ignore this.  This means the pid passed is bad.  It will be reported as not found.
			
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
