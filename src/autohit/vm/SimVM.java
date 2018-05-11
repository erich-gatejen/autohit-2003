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

import autohit.call.Call;
import autohit.call.CallException;
import autohit.call.Call_METHOD;
import autohit.common.AutohitErrorCodes;
import autohit.common.Constants;
import autohit.creator.SimLanguage;
import autohit.vm.i.VMIAssert;
import autohit.vm.i.VMICall;
import autohit.vm.i.VMIClear;
import autohit.vm.i.VMIEval;
import autohit.vm.i.VMIExec;
import autohit.vm.i.VMIFetch;
import autohit.vm.i.VMIGoto;
import autohit.vm.i.VMIIf;
import autohit.vm.i.VMIJump;
import autohit.vm.i.VMILoad;
import autohit.vm.i.VMIMath;
import autohit.vm.i.VMIMerge;
import autohit.vm.i.VMIMethod;
import autohit.vm.i.VMINew;
import autohit.vm.i.VMINop;
import autohit.vm.i.VMIRScope;
import autohit.vm.i.VMIReduce;
import autohit.vm.i.VMIScope;
import autohit.vm.i.VMIStore;
import autohit.vm.i.VMISubr;
import autohit.vm.i.VMInstruction;
import autohit.vm.process.StringProcessors;

/**
 * A VM for a Sim.
 * <p>
 * A VMNOp marks the bottom of the scope stack.  If we ever pop a nop, there
 * is nothing left to do.
 * <p>
 * Subroutine stack frames consist of the IP pointer (to calling instruction) in Integer form and the calling 
 * VMExecutable.  When a the IP goes past the last instruction, the SimVM wil
 * check the stack for a VMExecutable.  If it sees one, it will assume it is 
 * the return from a subroutine.  The LEFT is left alone, since this is how the 
 * subroutine passes a return value.  If the subroutine specified a return, but it
 * isn't set, the VM will log an error and use a blank return value.  The subroutine depth (subDepth)
 * will be incremented and decremented as we enter and leave routines.  This helps up
 * keep an exception from unravelling the entire VM.
 * <p>
 * Currently only the following MATH operations are implemented
 * <pre>
 * +	= plus
 * - 	= minus
 * /    = division
 * *    = multiply
 * </pre>
 * <p>
 * ERRORS ----------
 * 
 * <p>
 * This VM expects the following environment variables to be set
 * before the execute() method is called.  If they aren't set, the defaults
 * will be used.
 * <p>
 *
 * @see autohit.vm
 *
 * @author Erich P. Gatejen
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - Rewrite - 8May03<br>
 * EPG - Add goto - 16Jul03<br>
 * EPG - Add assert - 5Aug03<br>
 * EPG - Add module shortcut - 9Aug03</code> 
 * 
 */
public class SimVM extends VM {

	/**
	 *  The current executable.
	 *
	 *  @see autohit.vm.VMExecutable
	 */
	private VMExecutable mySim;

	/**
	 *  Subroutine depth.
	 */
	private int subDepth;

	/**
	 *  Last instruction.  Used to detect runawya programs.
	 */
	private int lastIP;

	/**
	 *  Scratch object.  Put it here because I have plans for the future
	 */
	private Object scratch;

	/**
	 *  Current isntruction
	 */
	private VMInstruction ci;

	/**
	 *  Default Constructor.  Don't do anything!
	 */
	public SimVM() {
		super();
	}

	/**
	 *  Complete construction.  This will be (should be) called right after a VM
	 *  object is constructed.  This is where you set the target program name.
	 */
	public void construct() {
		right = Constants.EMPTY_LEFT;
		left = Constants.EMPTY_LEFT;
		subDepth = 0;
	}

	/**
	 * Destroy.  This will be called when the VM is
	 * finalizing.
	 */
	public void destruct() throws VMException {
		// dont do anything
	}

	/**
	 *  Prepare for execution of the first instruction.  We need to 
	 *  add environment variables.  DO NOT call this method directly.
	 *
	 *  @throws Any exceptions it encounters.
	 */
	public void prepare() throws Exception {

		// Toss a NOP on the stack.
		core.push(new VMINop());

		// Load the program
		mySim = loader.load(rootProgram);

		// last instruction set to soemthing impossible
		lastIP = -99999;
	}

	/**
	 *  Implements the inherited abstract method execute(). Call this to execute
	 *  a single instruction,  The first call will be automatic after the inherited
	 *  start() method is called.  From there, the owning Object/Thread should 
	 *  call this method for each successive instruction to execute.
	 *  <p>
	 *  This method will throw a VMException(VMException.DONE) if there are
	 *  no more instructions that can be executed.  (The ip is past the
	 *  end of the exec Vector). 
	 *  <p>
	 *  @throws VMException
	 *  @see autohit.vm.VMException
	 */
	public void execute() throws VMException {

		ci = null;

		// Have we completed this routine?
		if (ip >= mySim.core.size()) {

			lastIP = -99999; // something impossible

			// Pop until we get to something interesting
			// Intermeadiate crap is prolly old stack-frame junk
			while (true) {
				try {
					scratch = core.pop();
				} catch (Exception e) {
					scratch = null;
				}
				if (scratch == null) {
					throw new VMException(
						"Instruction pointer out of bounds.  Stack drained.  All hell has broken loose.",
						VMException.CODE_VM_PANIC);
				}

				if (scratch instanceof String) {
					// local variable.  Remove it.
					core.remove((String) scratch);
				}

				if (scratch instanceof VMExecutable) {
					// leaving this subroutine.  change context and cycle
					try {
						exit_subr();
						mySim = (VMExecutable) scratch;
						ip++;
					} catch (VMException e) {
						throw e;
					}
					return;
				}

				if (scratch instanceof VMINop) {
					// found that NOP at the bottom of the stack.  Must be done.
					throw new VMException(VMException.CODE_VM_DONE);
				}

			} // end while
		} // end if

		try {

			// check for a runaway program that just keeps executing the
			// same dead command.  This is what happens when a GOTO
			// points at itself.  :^)
			if (ip == lastIP) {
				ip++; // kick it
				myLog.error(
					"VMSim:"
						+ mySim.name
						+ " Detected a runaway program that is looping on the same instruction.   IP kicked by one, but the VM is probibly unstable.",
					AutohitErrorCodes.CODE_VM_INSTRUCTION_ABORT);
				return;
			} else {
				lastIP = ip;
			}

			ci = (VMInstruction) mySim.core.get(ip);
			// NOTES:  Each instruction is responsible for advancing the
			// ip.
			//DEBUG
			//myLog.debug("SIMVM: get token at ip=" + ip + " is=" + ci.instruction);

			switch (ci.instruction) {

				case VMInstruction.CALL :
					// i.call(target)	  : call TARGET, target put result in LEFT, store LEFT in result.
					handleCall((VMICall) ci);
					ip++;
					break;

				case VMInstruction.METHOD :
					// i.method(n,m)	  : call MATHOD, target put result in LEFT, store LEFT in result.
					handleMethod((VMIMethod) ci);
					ip++;
					break;

				case VMInstruction.CLEAR :
					// i.clear(buffer) : clear a buffer
					if (core.exists(((VMIClear) ci).t)) {
						core.replace(((VMIClear) ci).t, new StringBuffer());
					} else {
						// It's a brand new buffer
						core.store(((VMIClear) ci).t, new StringBuffer());
					}
					ip++;
					break;

				case VMInstruction.EVAL :
					// * i.eval(literal)	  : evaluate and store in LEFT(literal)
					handleEval((VMIEval) ci);
					ip++;
					break;

				case VMInstruction.EXEC :
					// i.exec(class)	  : exec TARGET, target put result in LEFT, store LEFT in result.
					handleExec((VMIExec) ci);
					ip++;
					break;

				case VMInstruction.FAULT :
					// i.fault  	  : push the IP out of bounds.  this will bust us
					// out of this routine.
					ip = mySim.core.size() + 1;
					break;

				case VMInstruction.FETCH :
					// i.fetch(variable) : load LEFT from storage specified
					// may not be a buffer
					// TODO SimVM currently only supports String and StringBuffer for passbyreference
					if (core.exists(((VMIFetch) ci).v)) {
						scratch = core.fetch(((VMIFetch) ci).v);
						if ((scratch instanceof String)
							|| (scratch instanceof StringBuffer)) {
							left = scratch;

						} else {
							//	Blech!  Something bad in this object
							throw new VMException(
								"ERROR in FETCH: Fetched object is not a String or StringBuffer.  This may mean bad things later.  name="
									+ ((VMIFetch) ci).v,
								AutohitErrorCodes.CODE_VM_INSTRUCTION_WARNING);
						}

					} else {
						// The variable doesn't exist.  Forgive it if the next command is an assert
						// TODO Terrible hack to look ahead for asserts on fetch fail
						boolean faultme = true;
						try {
							if (mySim.core.get(ip + 1) instanceof VMIAssert) {
								faultme = false;
								left = null;
							}
						} catch (Exception ecccc) {
						}
						// FAULT.
						if (faultme)
							throw new VMException(
								"FETCH failed: Variable does not exist.  name="
									+ ((VMIFetch) ci).v,
								AutohitErrorCodes
									.CODE_VM_VARIABLE_NOT_DEFINED_FAULT);
					}
					ip++;
					break;

				case VMInstruction.IF :
					handleIf((VMIIf) ci);
					break;

				case VMInstruction.ASSERT :
					handleAssert((VMIAssert) ci);
					break;

				case VMInstruction.JUMP :
					// literal jump
					ip = ((VMIJump) ci).t;
					break;

				case VMInstruction.GOTO :
					// literal jump
					handleGoto((VMIGoto) ci);
					break;

				case VMInstruction.LOAD :
					// i.load(literal)   : load literal into LEFT
					left = ((VMILoad) ci).l;
					ip++;
					break;

				case VMInstruction.MATH :
					// i.math(oper)  	  : execute operation from RIGHT(literal) to LEFT(literal)
					handleMath((VMIMath) ci);
					ip++;
					break;

				case VMInstruction.MERGE :
					// i.merge(buffer)   : merge LEFT(literal) with named buffer
					handleMerge((VMIMerge) ci);
					ip++;
					break;

				case VMInstruction.NEW :
					// i.new(variable)   : push LEFT(literal) on stack and mirror to storage
					core.store(((VMINew) ci).v, left);
					ip++;
					break;

				case VMInstruction.NOP :
					// Just burn the cycle
					ip++;
					break;

				case VMInstruction.REDUCE :
					// i.reduce(buffer)  : reduce a buffer and put in LEFT(literal)
					handleReduce((VMIReduce) ci);
					ip++;
					break;

				case VMInstruction.RIGHT :
					// move LEFT(literal) to RIGHT(literal)
					right = left;
					ip++;
					break;

				case VMInstruction.RSCOPE :
					// pop the stack to the next i.scope.  remove all encounted vars
					core.discardScopeFrame();
					ip++;
					break;

				case VMInstruction.SCOPE :
					// push to stack as a marker
					core.markScope();
					ip++;
					break;

				case VMInstruction.STORE :
					// i.store(variable) : update in scope variable.  if does not exist, do a i.new
					if (core.exists(((VMIStore) ci).v)) {
						core.replace(((VMIStore) ci).v, left);
					} else {
						core.store(((VMIStore) ci).v, left);
					}
					ip++;
					break;

				case VMInstruction.SUBR :
					// 	i.subr(target)	  : fork to ROUTINE, store LEFT in result.
					entry_subr((VMISubr) ci);
					break;

				case VMInstruction.MASK :
				default :
					throw new VMException(
						"Unsupported instruction encounted by autohit.SimVM.  nToken=["
							+ ci.instruction
							+ "]",
						AutohitErrorCodes.CODE_VM_INVALID_INSTRUCTION_FAULT);
			}

		} catch (VMException e) {

			// Act based on the level
			if (VMException.isFault(e.numeric)
				|| VMException.isPanic(e.numeric)) {

				faults++;

				// BUST THIS ROUTINE
				if (subDepth > 0) {
					// we are in a subroutine.  Bomb out of it.  Find the stack bottom or 
					// the bottom of the subroutine stackframe
					try {
						scratch = core.peek();
						while ((scratch != null)
							&& (!(scratch instanceof VMINop))
							&& (!(scratch instanceof VMExecutable))) {
							core.pop();
							scratch = core.peek();
						}
					} catch (Exception ex) {
						// don't care - end of line
					}
					ip = mySim.core.size() + 9999;
					// make sure it is out of bounds

					myLog.error(
						"VMSim:Routine "
							+ mySim.name
							+ " FAULT at ip="
							+ ip
							+ ".  Message="
							+ e.getMessage(),
						e.numeric);
					faults++;

				} else {
					// top level - this is the root routine - let the VM unravel
					myLog.error(
						"VMSim:Routine:"
							+ mySim.name
							+ " TERMINAL FAULT at ip="
							+ ip
							+ ".  Message="
							+ e.getMessage(),
						e.numeric);
					dumpei(ci);
					faults++;
					throw e;
				}

			} else if (VMException.isError(e.numeric)) {
				myLog.error(
					"VMSim:Routine:"
						+ mySim.name
						+ " ERROR at ip="
						+ ip
						+ ".  Message="
						+ e.getMessage(),
					e.numeric);
				errors++;
				if (myLog.debugState())
					dumpei(ci);

				// make sure the IP advances!
				ip++;

			} else {
				// Don't consider anything else an error, but log it with that priority
				myLog.error(
					"VMSim:Routine:"
						+ mySim.name
						+ " INFO.  Message="
						+ e.getMessage(),
					e.numeric);

				// make sure the IP advances!
				ip++;
			}

		} catch (Exception e) {
			throw new VMException(
				"Unrecoverable Fault:  exception=[" + e.getMessage(),
				AutohitErrorCodes.CODE_VM_PANIC,
				e);
		}
	}

	/* 
	 * Handle CALL instruction
	 * i.call(target)	  : call TARGET, target put result in LEFT, store LEFT in result.
	 */
	private void handleCall(VMICall instr) throws Exception {

		try {

			Call c = loader.get(instr.t, core, myLog);
			left = c.call();

		} catch (CallException e) {
			if (myLog.debugState())
				myLog.error(
					"CALL Subsystem error.  CALL implementation threw an exception.  Unable to complete CALL. error="
						+ e.getMessage(),
					e.numeric);
			throw e;

		} catch (VMException e) {
			throw e;
		} catch (Exception e) {
			throw new VMException(
				"CALL Subsystem fault.  Faulted out of a CALL. error="
					+ e.getMessage(),
				VMException.CODE_VM_CALL_FAULT,
				e);
		}
	}

	/* 
	 * Handle METHOD instruction.  Basically, this is a cheater for
	 * using CALL_METHOD.
	 * i.method()	  : call Module/method, target put result in LEFT, store LEFT in result.
	 */
	private void handleMethod(VMIMethod instr) throws Exception {

		try {

			// We know it is a method
			Call_METHOD c = (Call_METHOD) loader.get("METHOD", core, myLog);
			left = c.do_call((String) left, instr.m);

		} catch (CallException e) {
			if (myLog.debugState())
				myLog.error(
					"METHOD Subsystem error.  METHOD implementation threw an exception.  Unable to complete CALL. error="
						+ e.getMessage(),
					e.numeric);
			throw e;

		} catch (VMException e) {
			throw e;
		} catch (Exception e) {
			throw new VMException(
				"METHOD Subsystem fault.  Faulted out of a METHOD. error="
					+ e.getMessage(),
				VMException.CODE_VM_CALL_FAULT,
				e);
		}
	}

	/* 
	 * Handle EXEC instruction
	 * i.exec(class)	  : exec TARGET, target put result in LEFT, store LEFT in result.
	 */
	private void handleExec(VMIExec instr) throws VMException {

		try {

			// TODO implement exec

		} catch (Exception e) {
			throw new VMException(
				" Execution fork Subsystem fault.  Unable to complete EXEC. error="
					+ e.getMessage(),
				AutohitErrorCodes.CODE_VM_EXEC_FAULT,
				e);
		}
	}

	/* 
	 * Handle GOTO instruction
	 * i.goto(target) : scope sensitive jump.  includes a nasty ass hack to bust any scope frame.
	 */
	private void handleGoto(VMIGoto instr) throws VMException {

		// decide which way we jump
		int delta = instr.t - ip;
		if (delta == 0) {
			// points at itself.  this is BAD
			throw new VMException(
				"Circular GOTO detected.  This subr will deadlock.  Forcing it to bust out.",
				VMException.CODE_VM_INTENTIONAL_FAULT);

		} else if (delta == 1) {
			// points at the next instruction.  silly but valid.
			ip++;

		} else if (delta < 0) {

			// jumping UP.  Look for any SCOPES and bust their frames
			Object tci;
			int drscopes = 0;
			int dscopes = 0;
			int dbalance = 0;
			for (int i = ip; i > instr.t; i--) {
				tci = mySim.core.get(i);
				if (tci instanceof VMIScope) {
					dscopes++;
				} else if (tci instanceof VMIRScope) {
					drscopes++;
				}
			}
			dbalance = dscopes - drscopes;
			while (dbalance > 0) {
				core.discardScopeFrame();
				dbalance--;
			}

			ip = instr.t;

		} else {

			// jumping DOWN.  Look for any RSCOPES and bust their frames	
			Object tci;
			int drscopes = 0;
			int dscopes = 0;
			int dbalance = 0;
			for (int i = ip; i < instr.t; i++) {
				tci = mySim.core.get(i);
				if (tci instanceof VMIScope) {
					dscopes++;
				} else if (tci instanceof VMIRScope) {
					drscopes++;
				}
			}
			dbalance = drscopes - dscopes;
			while (dbalance > 0) {
				core.discardScopeFrame();
				dbalance--;
			}

			ip = instr.t;

		} // end if down
	}

	/* 
	 * Handle Subroutine instruction
	 * i.subr(target)	  : fork to ROUTINE, store LEFT in result.
	 *
	 * Handle the subroute call, steps 3 through 7
	 * ENTRY
	 * 1- Hit i.scope (emitted)
	 * 2- Instantiate parameters (emitted)
	 * 3- Instantiate return variable as defined in i.subr instruction
	 * 4- Toss i.subr instruction on the stack
	 * 5- Toss Instruction Pointer on the stack
	 * 6- Toss reference to currently runninng executable (VMExec.) on stack
	 * 7- Load new SUBR and let run
	 *
	 * 1, 2 are emitted instructions, as such:
	 * <subroutine>	i.scope				// do 1
	 *		(SET)*						// do 2
	 *		i.subr(name)				// do 3 through 13
	 *		i.rscope					// do 14
	 *		if (result exist) i.store(result)       // do 15
	 */
	private void entry_subr(VMISubr instr) throws VMException {

		myLog.debug("SIMVM: Enter subr.  t=" + instr.t);

		VMExecutable loadedSim = null;

		try {

			//	Make sure we can load it
			loadedSim = loader.load(instr.t);

		} catch (Exception e) {

			// This error is recoverable
			left = Constants.EMPTY_LEFT;
			ip++;
			throw new VMException(
				"Failed to load subroutine= "
					+ instr.t
					+ ".  Aborting call.  Reason="
					+ e.getMessage(),
				AutohitErrorCodes.CODE_VM_INSTRUCTION_ABORT,
				e);
		}

		// Any error after here is catastrophic
		try {
			// 3- Instantiate return variable as defined in i.subr instruction
			if ((loadedSim.output != null)
				&& (loadedSim.output.name != null)) {
				core.store(loadedSim.output.name, Constants.EMPTY_LEFT);
			}

			// 4- Toss i.subr instruction on the stack
			core.push(instr);

			// 5- Toss Instruction Pointer on the stack
			core.push(new Integer(ip));

			// 6- Toss reference to currently runninng executable (VMExec.) on stack
			core.push(mySim);

			// 7- Load new SUBR and let run
			mySim = loadedSim;
			subDepth++;
			ip = 0;

		} catch (Exception e) {
			throw new VMException(
				"SUBROUTINE Subsystem fault.  Unable to complete SUBR. error="
					+ e.getMessage(),
				VMException.CODE_VM_SUBSYSTEM_FAULT,
				e);
		}
	}

	/*
	 * Handle the subroutine exit, steps 10 through 11
	 * EXIT
	 * 10- Pop the IP (in the Integer)
	 * 11- Pop the i.subr.  Read the i.subr.return, put in left, remove from core.
	 */
	private void exit_subr() throws VMException {
		try {

			myLog.debug("SIMVM: Exit subr.");

			// 10- Pop the IP (in the Integer)
			Integer ipObj = (Integer) core.pop();
			ip = ipObj.intValue();

			// 11- Pop the i.subr.  Read the vmexec.output.name, put in left.
			core.pop();
			if ((mySim.output != null)&&(mySim.output.name != null)) {
				left = (String) core.fetch(mySim.output.name);
				if (left == null) {
					left = Constants.EMPTY_LEFT;
					myLog.warning(
						"SUBR EXIT: Expected a return value for "
							+ mySim.output.name
							+ " but it was null.");
				}
			} else {
				left = Constants.EMPTY_LEFT;
			}

			// Unwrap
			subDepth--;

		} catch (Exception e) {
			throw new VMException(
				"Software Detected Fault: Failed subroutine context switch. error="
					+ e.getMessage(),
				VMException.CODE_VM_SOFTWARE_DETECTED_FAULT,
				e);
		}
	}

	/* 
	 * Handle eval Instruction
	 * i.eval(literal)	  : evaluate and store in LEFT(literal)
	 */
	private void handleEval(VMIEval instr) throws VMException {

		try {

			left = StringProcessors.evalString2Core(instr.e, core);

		} catch (Exception e) {
			left = Constants.EMPTY_LEFT;

			throw new VMException(
				"Evaluation error.  Unable to complete EVAL on ["
					+ instr.e
					+ "].  error="
					+ e.getMessage(),
				AutohitErrorCodes.CODE_PROGRAM_ERROR,
				e);

		}
	}

	/**
	 * Handle Math Instruction<code>
	 * If either left or right fail a parse, the values will be compared
	 * as strings.
	 * i.math(oper)  	  : execute operation from RIGHT(literal)</code>
	 */
	private void handleMath(VMIMath instr) throws VMException {

		int res = 0;
		int l = 0;
		int r = 0;
		boolean donumbers = true;

		myLog.debug(
			"MATH start: left="
				+ left.toString()
				+ "  right="
				+ right.toString());

		try {

			try {
				l = Integer.parseInt(left.toString());
			} catch (NumberFormatException e) {
				donumbers = false;
			}
			try {
				r = Integer.parseInt(right.toString());
			} catch (NumberFormatException e) {
				donumbers = false;
			}

			if (donumbers) {

				// treat like numeric arthimatic
				if (instr.o.charAt(0) == Constants.MATH_PLUS) {
					res = l + r;

				} else if (instr.o.charAt(0) == Constants.MATH_MINUS) {
					res = l - r;

				} else if (instr.o.charAt(0) == Constants.MATH_DIVIDE) {

					try {
						res = l / r;
					} catch (ArithmeticException e) {
						dumpei(instr);
						res = 0;
						throw new VMException(
							"DIVIDE BY ZERO.  Math operation aborted; result = 0.  Operation="
								+ instr.o,
							AutohitErrorCodes.CODE_PROGRAM_DIVIDEBYZERO,
							e);
					}

				} else if (instr.o.charAt(0) == Constants.MATH_MULTIPLY) {
					res = r * l;

				} else if (instr.o.charAt(0) == SimLanguage.cEQ_OPERATION) {
					res = r - l;

				} else {
					dumpei(instr);
					throw new VMException(
						"Unrecognized math operations.  Math operation aborted; result = 0.  Operation="
							+ instr.o,
						AutohitErrorCodes.CODE_PROGRAM_ERROR);

				}

			} else if (right instanceof String) {
				// treat like a string compare.  Compares two strings lexicographically.
				// "0" means the strings are identical
				res = ((String) right).compareTo(left.toString());

			} else {
				throw new VMException(
					"Math error.  Left and/or Right expressions inappropriate objects for a math operation.",
					VMException.CODE_VM_INSTRUCTION_ERROR);
			}

		} catch (VMException ve) {
			throw ve;
		} catch (Exception e) {
			throw new VMException(
				"Math fault.  Unable to complete MATH.  error="
					+ e.getMessage(),
				VMException.CODE_VM_INSTRUCTION_FAULT,
				e);
		}

		// store result
		left = Integer.toString(res);

		myLog.debug("MATH done: left=" + left);
	}

	/**
	 * Handle merge instruction<p><code>
	 * i.merge(buffer)   : merge LEFT(literal) with named buffer</code>
	 */
	private void handleMerge(VMIMerge instr) throws VMException {

		try {

			Object tb = core.fetch(instr.b);

			if (tb != null) {

				if (tb instanceof StringBuffer) {

					((StringBuffer) tb).append(left);

				} else {
					throw new VMException(
						"VMSim: Merge not possible.  ["
							+ instr.b
							+ "] is not a buffer.  Aborting instruction, but not execution.",
						AutohitErrorCodes.CODE_PROGRAM_ERROR);
				}

			} else {
				throw new VMException(
					"VMSim: Merge not possible.  ["
						+ instr.b
						+ "] does not exist.  Aborting instruction, but not execution.",
					AutohitErrorCodes.CODE_PROGRAM_ERROR);
			}

		} catch (Exception e) {
			throw new VMException(
				"VMSim: Merge fault.  Unable to complete MERGE.  error="
					+ e.getMessage(),
				AutohitErrorCodes.CODE_VM_SOFTWARE_DETECTED_FAULT,
				e);
		}
	}

	/**
	 * Handle If instruction<p><code>
	 * i.if(literal,oper): Evaluate LEFT(literal).  0 means the evaluation was a match.  
	 *      You can modify with an operationl, oper = (gt|lt|eq|not):
	 *		eq  : default.   if LEFT(literal) is not 0, jump to literal
	 *		gt  : if LEFT(literal) is =< 0, jump to literal
	 *		lt  : if LEFT(literal) is >= 0, jump to literal
	 *		not : if LEFT(literal) is 0, jump to literal</code>
	 * TODO This using a non-zero as a default in IF resolution may be scary
	 */
	private void handleIf(VMIIf instr) throws VMException {

		// Prepare Left.  Default value is not zero	
		int leftval = SimLanguage.NOT_ZERO;
		String scrubleft;

		if (left instanceof String) {
			scrubleft = (String) left;

		} else if (left instanceof StringBuffer) {
			scrubleft = ((StringBuffer) left).toString();
		} else {
			ip = ((VMIIf) ci).t;
			throw new VMException(
				"Left expression inappropriate type for IF operation.  Assume FALSE.",
				VMException.CODE_VM_INSTRUCTION_ERROR);
		}

		try {
			leftval = Integer.parseInt(scrubleft);
		} catch (Exception e) {
		} // Dont care

		switch (instr.operFlag) {

			case SimLanguage.EQ :
				if (leftval == SimLanguage.ZERO) {
					ip++; // success.  branch.
				} else {
					ip = ((VMIIf) ci).t; // Failed if
				}
				break;

			case SimLanguage.LT :
				if (leftval <= SimLanguage.ZERO) {
					ip++; // success.  branch.
				} else {
					ip = ((VMIIf) ci).t; // Failed if
				}
				break;

			case SimLanguage.GT :
				if (leftval >= SimLanguage.ZERO) {
					ip++; // success.  branch.
				} else {
					ip = ((VMIIf) ci).t; // Failed if
				}
				break;

			case SimLanguage.NOT :
				if (leftval != SimLanguage.ZERO) {
					ip++; // success.  branch.
				} else {
					ip = ((VMIIf) ci).t; // Failed if
				}
				break;

			default :
				throw new VMException(
					"Software Detected Fault.  IF with an unknown oper.  This should never happen.",
					VMException.CODE_VM_SOFTWARE_DETECTED_FAULT);
		}

	}

	/**
	 * Handle Assert instruction<p><code>
	 * i.if(literal,oper): Evaluate LEFT(literal).  If it is empty, null, or void, the check passes
	 * 	    and the IP should move to the next instruction.  If the check fails,
	 *      then the IP should be set to the target.
	 *      You can modify with an operationl, oper = (eq|not):
	 *		eq  : default.   if LEFT(literal) is empty, null, or void, then jump to literal
	 *		not : if LEFT(literal) is NOT empty, null, or void, jump to literal</code>
	 */
	private void handleAssert(VMIAssert instr) throws VMException {

		// See if it is empty
		boolean isempty = false;
		if (left == null) {
			isempty = true;
		} else if (left instanceof String) {
			if (((String) left).equals(Constants.EMPTY_LEFT)
				|| ((String) left).length() < 1) {
				isempty = true;
			}
		}

		switch (instr.operFlag) {

			// ASSERT THERE IS SOMETHING
			case SimLanguage.EQ :
				if (isempty) {
					// It is empty.  Assert failed
					ip = ((VMIAssert) ci).t;
				} else {
					// It is not empty.  Assert succeeded.
					ip++;
				}
				break;

				// ASSERT THERE IS NOT SOMETHING
			case SimLanguage.NOT :
				if (isempty) {
					// It is empty.  Assert succeeded.
					ip++;
				} else {
					// It is not empty.  Assert failed
					ip = ((VMIAssert) ci).t;
				}
				break;

			default :
				throw new VMException(
					"Software Detected Fault.  ASSERT with an unknown or unsupported operation.  This should never happen.  oper="
						+ instr.operFlag,
					VMException.CODE_VM_SOFTWARE_DETECTED_FAULT);
		} // end case
	}

	/* 
	 * Handle merge instruction
	 * i.reduce(buffer)  : reduce a buffer and put in LEFT(literal)
	 */
	private void handleReduce(VMIReduce instr) throws VMException {

		try {

			Object tb = core.fetch(instr.b);

			if (tb != null) {

				if (tb instanceof StringBuffer) {

					left = tb.toString();

				} else {
					throw new VMException(
						"VMSim: Reduce not possible.  ["
							+ instr.b
							+ "] is not a buffer.  Aborting instruction, but not execution.",
						AutohitErrorCodes.CODE_PROGRAM_ERROR);
				}

			} else {
				throw new VMException(
					"Reduce not possible.  ["
						+ instr.b
						+ "] does not exist.  Aborting instruction, but not execution.",
					AutohitErrorCodes.CODE_PROGRAM_ERROR);
			}

		} catch (Exception e) {
			throw new VMException(
				"Reduce fault.  Unable to complete REDUCE.  error="
					+ e.getMessage(),
				AutohitErrorCodes.CODE_VM_SOFTWARE_DETECTED_FAULT,
				e);
		}
	}

	/*
	 * LOG/Message Helper. -- DUMP
	 */
	private void dumpei(VMInstruction i) {
		if ((myLog.debugState() == false) || (ci == null))
			return;
		myLog.debug("VMSim: DUMP instruction pointer.  ip=" + ip);
		myLog.debug("VMSim: DUMP errored instruction.   i=" + i.toString());
		myLog.debug("VMSim: DUMP left.  l=[" + left + "]");
		myLog.debug("VMSim: DUMP right. l=[" + right + "]");
		myLog.debug("VMSim: DUMP state. state=" + state);
		myLog.debug(
			"VMSim: DUMP running program="
				+ mySim.name
				+ "v"
				+ mySim.major
				+ "."
				+ mySim.minor);
	}

}
