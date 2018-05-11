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

import java.util.Date;
import java.util.Iterator;

import org.omg.CORBA.Any;

import autohit.common.AutohitLogInjectorWrapper;
import autohit.common.Constants;
import autohit.common.Utils;
import autohit.common.channels.Injector;

/**
 * The abstract base class for virtual machines.
 *
 * A derived class will implement the abstract execute()
 * method to actually run the VM.  Also, it would normally use the
 * pause() and resume() methods to control timing (rather than
 * overloading and re-implementing them).
 * <p> 
 * VM bring up sequence is as follows:  instantiation with default constructor,
 * call init(), call construct() of subclass, give VM to a Process, process
 * will call attach(), process will call start() to run first instruction,
 * start will call prepare() in the base class, start will call execute() for
 * the first time.  
 * <p>
 * The derived class MAY overload the method prepare() if it has 
 * anything it wants to do before the FIRST instruction (and only the
 * first) is executed.  For instance, it could add environment variables.
 * <p>
 * The pause() and resume() methods are not designed to be
 * called by external threads.  If you plan to wrap the 
 * derived vm in a threaded class, you may want to overload or 
 * just not use those methods outside of the vm.  Also, these methods
 * only manage state and timing; it is up the he execute() method
 * in the derived class to actually stop execution.
 * <p>
 * This is not threadsafe, since one one thread should ever own an instance.
 * <p>
 * NO VM is valid until the init() method is called!!!  Every VM needs a loader.
 * <p>
 * USE <b>THESE</b> SERVICES!  Do not make your own ip, for instance!  Methods of
 * this class depend upon it.
 *
 * @author Erich P. Gatejen
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - Rewrite - 8May03</code>
 * 
 */
public abstract class VM {

	/**
	 *  Granulatiry for each tick of the VM's clock.  It
	 *  is used to scale the system time to the vm clock time.
	 *
	 *  Currently, it is set to 1.  Given the current Java
	 *  implementations, this should yield a 1 millisecond
	 *  tick.  That is, each VM clock tick will take one
	 *  millisecond.
	 */
	public static final int TIME_GRAN = 1;

	/**
	 *  State values for the VM.  Any value over STATE_ACTIVE_THRESHOLD
	 * means the VM is active.
	 */
	public static final int STATE_INVALID = 0;
	public static final int STATE_NO_VM = 1;
	public static final int STATE_BUILDING = 50;
	public static final int STATE_DONE = 99;
	public static final int STATE_ACTIVE_THRESHOLD = 100;
	public static final int STATE_NEW = 200;
	public static final int STATE_RUNNING = 201;
	public static final int STATE_PAUSED = 300;

	/**
	 *  VM start time.  System start time for this VM.
	 *  This is a raw value that has not been scaled with the
	 *  TIME_GRAN value.  We don't even want the derived class
	 *  to get direct access to this, in case we have to
	 *  compensate for a pause.
	 */
	private long starttime;

	/**
	 *  Used to compensate the time field after a resume().
	 *  Since the system clock doesn't stop during a pause,
	 *  we will have to change our perceived system time start
	 *  to get the ticks for the VM.
	 */
	private long pauseCompensation;

	/**
	 *  VM state.
	 */
	protected int state;

	/**
	 *  Current instruction address/pointer.  A pointer into the insrtuction Vector.
	 */
	protected int ip;

	/**
	 *  Right value
	 */
	protected Object right;

	/**
	 *  Left value -- accumulator
	 */
	protected Object left;

	/**
	 *  VM Buffer
	 */
	protected StringBuffer buf;

	/**
	 * Controlling environment.  A loader, a process, and our core.
	 */
	public VMLoader loader;
	public VMProcess process;
	public VMCore core;

	/**
	 *  Name of the root program
	 */
	public String rootProgram;

	/**
	 *  Session name.
	 */
	public String sname;

	/**
	 *  Running error total.
	 */
	public int errors;

	/**
	 *  Running faults total.
	 */
	public int faults;

	/**
	 *  The root logging mechinism.  Used for controller logging.
	 *
	 *  @see autohit.common.AutohitLogInjectorWrapper
	 */
	public AutohitLogInjectorWrapper myLog;
	
	/**
	 *  Injector for response channel.
	 *
	 *  @see autohit.common.channels.Injector
	 */
	protected Injector rinjector;

	/**
	 *  Might be used if attached
	 */
	private VMCore parentCore;

	
	/**
	 * Default Constructor. 
	 * <p>
	 */
	public VM() {
		sname = Constants.VM_GENERIC_NAME;
		state = STATE_BUILDING;
		parentCore = null;
		rootProgram =
			"Software Detected Fault.  VM implementation did not set root program name during construction.";
	}

	/**
	 * This will set a parent core.  The storage variables from the parent will be copied to 
	 * this VM while attaching.
	 * <p>
	 */
	public void setParentCore(VMCore pc) {	
		parentCore = pc;
	}
	
	/**
	 *  Attach VM to it's owning process.  Typically this is called by 
	 *  the VMProcess.  This will hook up the SystemContext attributes.
	 *  This will effectively initialize the VM.
	 *  @throws Any exceptions it encounters.
	 * TODO fix the id KLUDGE
	 */
	public void attach(VMProcess vmp) throws Exception {

		// Attach it
		process = vmp;
		loader = process.getSystemContext().getLoader();
		int tpid = vmp.getPID();

		// KLUDGE to hash the id
		sname = Utils.norfIt(tpid);
		
		// Put loggin on the response channel
		// Debug state is frozen here for the injector
		myLog = new AutohitLogInjectorWrapper();
		myLog.init(sname,rinjector);
		myLog.debugFlag(process.getSystemContext().debuggingState());

		// Now initialize it.
		core = loader.create();
		
		// Parent?  If so, copy the storage
		if (parentCore!=null) {
			
			try {
				
				// We need to copy this core into the new core to catch all
				// properties
				Iterator varList = parentCore.getStorageNameSet().iterator();
				String ikey;
				Object item;
				while (varList.hasNext()) {
					ikey = (String)varList.next();
					item = parentCore.fetch(ikey);
					core.store(ikey,item);
				}
				
			} catch (Exception cce) {
				throw new VMException(
						"VM["
							+ sname
							+ "] FATAL EXCEPTION during startup.  Exception will be thrown to the Kernel.  exception="
							+ cce.getMessage(),
						VMException.CODE_VM_PREPARE_FAULT,
						cce);
			}
		}

		// TODO Maybe I shouldn't log to the root logger from VM attach
		(loader.sc.getLogManager()).getRootLogger().info(
			"VM attached to Process.  sname="
				+ sname
				+ ". pid="
				+ tpid
				+ ".  To run program="
				+ rootProgram,
			VMException.CODE_INFORMATIONAL_OK_VERBOSE);
		
		// State to NEW
		state = STATE_NEW;
	}

	/**
	 *  Start the VM.  It will set state and timing info, then
	 *  call the abstract method execute() to execute the
	 *  code.
	 *  <p>
	 *  Calling this method consecutively will effectively 
	 *  reset the state and timing info.  It is probibly a 
	 *  REAL BAD IDEA to call this from the execute method.
	 *  <p>
	 *  It throws any exceptions that are thrown out of execute().
	 *
	 *  @throws autohit.vm.VMException
	 */
	public void start() throws VMException {

		state = STATE_RUNNING;
		ip = 0; // Always start at home.  :-)
		Date d = new Date();
		starttime = d.getTime();
		errors = 0;
		faults = 0;
		try {
			prepare();
		} catch (Exception e) {
			throw new VMException(
				"VM["
					+ sname
					+ "] FATAL EXCEPTION during startup.  Exception will be thrown to the Kernel.  exception="
					+ e.getMessage(),
				VMException.CODE_VM_PREPARE_FAULT,
				e);
		}
		execute();
	}

	/**
	 *  Get VM state.  Reports the state of the vm using the
	 *  STATE_* values.
	 *  <p>
	 *  You may call this from another thread, but it isn't
	 *  very reliable.
	 *
	 *  @return a STATE_* value
	 */
	public int getState() {
		return state;
	}

	/**
	 *  Pause execution in the VM.  This should NOT be called
	 *  by another thread.
	 *  <p>
	 *  It will only pause if the VM is running.
	 */
	public void pause() {

		if (state == STATE_RUNNING) {
			state = STATE_PAUSED;
			Date d = new Date();
			pauseCompensation = - (d.getTime() - starttime);
		}
	}

	/**
	 *  Mark the VM for death.
	 */
	public void die() {

		state = STATE_DONE;
	}

	/**
	 *  Resume execution in the VM.  This should NOT be called
	 *  by another thread.
	 *  <p>
	 *  It will only resume if the VM is paused.
	 */
	public void resume() {

		if (state == STATE_PAUSED) {
			state = STATE_RUNNING;
			Date d = new Date();
			starttime = d.getTime() - pauseCompensation;
			pauseCompensation = d.getTime() - starttime;
		}
	}

	/**
	 *  Number of ticks the VM has been running.  It will
	 *  be scaled according to the TIME_GRAN field.
	 *  <p>
	 *  Note that it returns an int rather than a long like 
	 *  system time usually is.  This means that the VM timing.
	 *  This technically could cause some overflow problems, but
	 *  I doubt a VM would ever run that long.
	 *
	 *  @return number of ticks the VM has run.
	 */
	public int ticks() {

		Date d = new Date();

		long sticks = d.getTime() - starttime;

		return (int) (sticks / TIME_GRAN);

		// If the compiler has half of a brain, this division
		// should be optimised out given the current
		// granularity.
	}

	/**
	 *  Absract method for VM execution.  The derived class
	 *  must implement the actual execution.  This method will
	 *  be automatically called by start().  Therefore, you
	 *  probibly should not call start() from within this 
	 *  method.
	 *  <p>
	 *  The implimentation of this method should only execute
	 *  ONE INSTRUCTION.  Successive calls would then execute
	 *  the entire program.  If you do not impliment it this way,
	 *  you are likely to ghost the vm's.
	 *  <p>
	 *  NOTE!  An implementing method MUST throw a 
	 *  VMException(VMException.DONE) when it reaches the
	 *  end of execution.
	 *  <p>
	 *  If the derived-class VM encounters an instruction that
	 *  it does now support, it should throw a 
	 *  VMException.INVALID_INSTRUCTION.
	 *
	 *  @see autohit.vm.VMException
	 */
	public abstract void execute() throws VMException;

	/**
	 * Complete construction.  This will be called when the VM is
	 * initialized.
	 */
	public abstract void construct() throws VMException;

	/**
	 * Destroy.  This will be called when the VM is
	 * finalizing.
	 */
	public abstract void destruct() throws VMException;

	/**
	 * Complete initialization.  This must be called after the VM
	 * is constructed, but before VM is attached to a process.
	 * @param responseChannel the response channel
	 * @param target target program in universe namespace
	 */
	final public void init(Injector responseChannel, String target) throws VMException {

		rootProgram = target;
		rinjector = responseChannel;
		this.construct();
	}

	/**
	 *  Prepare for execution of the first instruction.  The derived
	 *  class may overload this if it has any stuff it wants to do
	 *  before execute() is called the first time.
	 *
	 *  @throws Any exceptions it encounters.
	 */
	public void prepare() throws Exception {

		// The base class doesn't wanna do anything...   
	}

	/**
	 * Complete initialization.  This must be called after the VM
	 * is constructed, but before VM is attached to a process.
	 * @param responseChannel the response channel
	 * @param target target program in universe namespace
	 */
	
	/**
	 * finalizer
	 * It will kill the logs last.
	 */
	protected void finalize() throws Throwable {
		if (finalizedvm == true) return;
		super.finalize();
		this.destruct();
		
		// kill the drain.  This is a horrible hack.
		process.getSystemContext().getLogManager().discardDrainWriter(sname);
		finalizedvm = true;
	}
	private boolean finalizedvm = false;

}
