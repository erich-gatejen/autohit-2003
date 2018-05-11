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
 
package autohit.vm.i;

/**
 * A Virtual Machine instruction.  This is a math instruction
 * It will execute the operation from RIGHT register to LEFT register, and leave
 * the result in the LEFT register.
 *
 * @see autohit.vm.i.VMInstruction
 *
 * @author Erich P. Gatejen
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - Rewrite - 9Apr03 
 * 
 */
public class VMIMath extends VMInstruction {

	final static long serialVersionUID = 1;
	
	/**
	 * String to evaluate.
	 * @serial
	 */
	public	String		o;
	
    /**
     *  Default constructor.
     */ 
    public VMIMath() {
        super(VMInstruction.MATH);
    }

    /**
     *  Dump this Instruction.  Mostly for debugging.
     *
     *  @return a String containing the dump.
     */
    public String toString() {
		return "VMIMath " + super.toString() + ": --- operation=" + o;
    }
    
} 
