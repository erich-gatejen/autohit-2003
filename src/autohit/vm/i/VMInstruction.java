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

import java.io.Serializable;
//import autohit.common.Constants;

/**
 * A Virtual Machine  instruction base class.  All vm instructions
 * extend this class.  This class also defines the static/final
 * tokens and related constants used by both the vm and the
 * compiler.
 * <p>
 * We will implement Serializable with this base class, so all
 * final instruction classes will inherit it,
 * <p>
 * I thought about making this an interface, but since this IS
 * a vm, it seem like a needless slowdown of a class that is 
 * going to get banged around enough as it is.
 * <p>
 * This class defines the numeric token for all instructions.
 * So, if you create a new instruction, be sure to add a token
 * for it in this class...  and recompile ALL of the packages.
 * <p>
 * All derived-class constructors must set the numeric token.
 *
 * @author Erich P. Gatejen
 * @version 1.1
 * <i>Version History</i>
 * <code>EPG - Rewrite - 9Apr03<br>
 * EPG - Add goto - 16Jul03</code>
 */
public class VMInstruction implements Serializable {

	final static long serialVersionUID = 1;
	
	/**
	 * Numeric token values.
	 *
	 * This is used as an optimization so we can do an OpCode
	 * switch(nToken) in the VM...
	 */
	public static final int NOP = 0;
	public static final int EVAL = 1;
	public static final int STORE = 2;
	public static final int NEW = 3;
	public static final int SCOPE = 4;
	public static final int RSCOPE = 5;
	public static final int REDUCE = 6;
	public static final int MASK = 7; 
	public static final int MERGE = 8;
	public static final int RIGHT = 9;
	public static final int MATH = 10; 
	public static final int LOAD = 11;
	public static final int CLEAR = 12;
	public static final int FAULT = 13;     
	public static final int FETCH = 14;
	public static final int IF = 15;
	public static final int CALL = 16;
	public static final int EXEC = 17;
	public static final int SUBR = 18;
	public static final int JUMP = 19;
	public static final int GOTO = 20;
	public static final int ASSERT = 21;
	public static final int METHOD = 22;
			
	/**
	 * instruction
	 * @serial
	 */
	public int instruction;

	/**
	 * source code line - good for debugging
	 * TODO sourceline doesn't actually work
	 * @serial
	 */
	public int sourceline;


	/**
	 * Default constructor
	 *
	 * Normally you would use the other constructor.
	 */
	public VMInstruction() {
		instruction = NOP;
		sourceline = 0;
	}

	/**
	 * Typical constructor.  Sets the instruction type.  Use this one!
	 */
	public VMInstruction(int I) {
		instruction = I;
	}

	/**
	 *  Dump this Instruction.  Mostly for debugging. 
	 *
	 *  @return a String containing the dump.
	 */
	public String toString() {
		return instruction + ":" + sourceline + ":" + this.dump();
	}
	
	/**
	 *  Dump this Instruction.  Mostly for debugging.  Subclasses should
	 *  override this.
	 *
	 *  @return a String containing the dump.
	 */
	public String dump() {
		return "";
	}

}
