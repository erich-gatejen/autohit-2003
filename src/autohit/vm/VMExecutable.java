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

import java.io.Serializable;
import java.util.ArrayList;

import autohit.common.NVPair;

/**
 * A VMExecutable is the holding bin for an executable.
 * <p>
 * When creating a new executable, you must call the init() member after construction.
 * If you do not, you will eventually get an internal exception.  If you are
 * deserializing, don't worry about it.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - Initial - 16apr03</code> 
 * 
 */
public class VMExecutable implements Serializable {

	final static long serialVersionUID = 1;
	
	/**
	 *  An array containing an executable.
	 *  Each member-object will be a vmInstruction derived class object.
	 *
	 *  @see autohit.vm.i.VMInstruction
	 *  @serial
	 */
	public ArrayList core;

	/**
	 * This executable name.
	 *  @serial
	 */
	public String name;

	/**
	 * This executable UID.  OPTIONAL
	 *  @serial
	 */
	public String uid;

	/**
	 * This the type of executable.
	 *  @serial
	 */
	public String type;

	/**
	 * Associated note.
	 *  @serial
	 */
	public String note;

	/**
	 * Version major.
	 *  @serial
	 */
	public int major;

	/**
	 * Version minor.
	 *  @serial
	 */
	public int minor;

	/**
	 * Defines the output variable.  The VM should load this to LEFT 
	 * before returning, if it exists.  The output is an name/value
	 * pair.  The name is the name of the variable and the value is 
	 * a type discriptor.  The latter isn't always useful, but the former is
	 * required.
	 * @see autohit.common.NVPair
	 *  @serial
	 */
	public NVPair output;

	// --- PUBLIC METHODS ----------------------------------------------------	

	/**
	 *  Default Constructor.  It will create an empty VMExecutable.  Remember!  If you are
	 *  creating a new Sim, but sure to call init().
	 *
	 *  @see #init()
	 */
	public VMExecutable() {

	}

	/**
	 *  Initializes a brand-new executable.  Using this in case this has to be
	 *  a singleton in the future.
	 */
	public void init() {

		core = new ArrayList();
	}
}
