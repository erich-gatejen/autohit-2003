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

import autohit.common.AutohitErrorCodes;
import autohit.common.AutohitProperties;
import autohit.common.Constants;
import autohit.common.Utils;
import autohit.common.channels.SimpleInjector;
import autohit.universe.Universe;
import autohit.vm.VM;
import autohit.vm.VMProcess;

/**
 * RUN call.  Runs a new script in a new VM.  It will use the root logger to 
 * report the success or failure of the new VM startup.  It will let the VM create its
 * own logfile for actual execution.  It only supports SimVM for now.
 * <pre>
 * REQURIES: logger, core
 * IGNORES: uni
 * PARAMETERS (INPUT):
 * name= name of the script to run
 * </pre>
 * RETURNS: The PID of the new VM process as a String.  It will be empty if the command failed.
 *
 * @author Erich P. Gatejen
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - Initial - 1APR05</code> 
 * 
 */
public class Call_RUN extends Call {

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
		return "RUN";
	}

	/**
	 * Execute it.
	 * @return the result or null if there is no result
	 */
	public String call() throws CallException {

		String result = Constants.EMPTY_LEFT;
		VM myVM;
		String name = null;

		try {

			// see if the parameter is passed
			name = this.requiredString("name");
				
			// build the process
			VMProcess pcb = (sc.getKernel()).get();
			
			// Get the VM instance
			Class t = Class.forName("autohit.vm.SimVM");
			myVM = (VM) t.newInstance();		

			// hook in the client logger
			SimpleInjector clientInjector = new SimpleInjector();
		    clientInjector.setDefaultSenderID(AutohitProperties.SYSTEM_COMMANDRESPONSE_ID);
			sc.getLogManager().addClient(clientInjector,Utils.norfIt(pcb.getPID()));
			
			// Init it
			myVM.init(clientInjector, name);
			//myVM.init(sc.getRootLogger().sinjector, name);
			
			// Set this as the parent vm Core
			myVM.setParentCore(vmc);
			
			// unleash it
			pcb.execute(myVM);
			result = Integer.toString(pcb.getPID());
						
		} catch (CallException e) {
			throw e;
			
		} catch (ClassNotFoundException e) {
			throw new CallException(
					" failed because it could not load the class for the vm.  VM specified = autohit.vm.SimVM",
					AutohitErrorCodes.CODE_CALL_UNRECOVERABLE_FAULT);

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
