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
package autohit.server.service;

import autohit.vm.VM;
import autohit.vm.VMException;
import autohit.server.SystemContext;

/**
 * Root service.  Basically, it cheats by being a VM.  You must implement
 * prepare(), execute(), construct_chain(), and destruct().  Look at 
 * autohit.vm.VM for more information.  You must call loadcontext() after instantiating
 * and before calling init()!
 *
 * @author Erich P. Gatejen
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - Initial - 15Sep03</code>
 */
public abstract class Service extends VM {

	/**
	 *   Make it easier to see the SC
	 */
	public SystemContext sc;

	// == IMPLEMENTATIONS ===================

	/**
	 * Complete initialization.  MUST BE CALLED BEFORE
	 * CONSTRUCTION!  This hack lets us use the VM.
	 */
	public void loadcontext(SystemContext ssc) throws VMException {
		sc = ssc;
	}
	
	//	== PROTOTYPES ===================
	/**
	 *  Prepare for execution of the first instruction. 
	 *  @throws Any exceptions it encounters.
	 */
	//public void prepare() throws Exception {

	/**
	 *  Concrete method for VM execution.  The derived class
	 *  @see autohit.vm.VMException
	 */
	//public abstract void execute() throws VMException;

	/**
	 * Complete construction.  This will be called when the VM is
	 * initialized.
	 */
	//public abstract void construct() throws VMException;

	/**
	 * Complete destroy.  This will be called when the VM is
	 * finalizing.
	 */
	//public abstract void destruct() throws VMException;

}
