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
package autohit.vm;
import java.util.Hashtable;

/**
 * A VM memory core factory
 *
 * @author Erich P. Gatejen
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - Rewrite - 8May03 
 * 
 */
public class VMCoreFactory {

	/**
	 * Registry of cores.  There is only ONE per factory
	 */
	private Hashtable environment;

	/**
	 * Default constructor
	 */
	public VMCoreFactory() {
		environment = new Hashtable();
	}

	/**
	 * Allocate a new core
	 */
	public VMCore allocate() {

		VMCore thang = new VMCore(environment);
		return thang;
	}

}
