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

import autohit.common.ProcessMonitor;
import autohit.server.SystemContext;
import autohit.common.Constants;

/**
 * A stand alone VM process context. It will wrap a VM in a thread and do all
 * the thread-safe kinda stuff. A context will handle successive VM runs, so
 * you can use them in a thread pool.
 * <p>
 * BE SURE to .init() and .start() this Thread BEFORE any other threads access
 * its methods (particularly execute()). Failure to heed this warning COULD
 * result in a race condition... :-)
 * <p>
 * 
 * <pre>
 *  There are four "commands" to a VM :
 *  	- PAUSE will hold execution
 *  	- RESUME will restart execution
 *  	- STOP will stop execution of the VM and dump it
 *  	- KILL will stop the execution and let this process die !
 * </pre>
 * 
 * @version 1.0 <i>Version History</i><code>EPG - Rewrite - 15May03<br>
 * EPG - moved context passing to the process, rather than the loader - 23Jul03</code>
 */
public class VMProcessAutomat extends Thread implements VMProcess {

	/**
	 *  A runnable VM. This will be a fully implemented derived-class of VM. */
	protected VM rVM;

	/**
	 *  An object for process management */
	private ProcessMonitor vsBlock;

	/**
	 *  Running. The VMProcess is still valid */
	private boolean alive;

	/**
	 *  Requests */
	private boolean reqPause;
	private boolean reqResume;
	private boolean reqStop;
	private boolean reqKill;
	private boolean reqState;

	/**
	 *  My pid */
	private int pid;

	/**
	 *  System Context */
	private SystemContext sc;

	/**
	 *  Times run. This lets us know if we need to ma */
	private int timesRun;

	/**
	 *  Constructor. */
	public VMProcessAutomat() {

		// be paranoid
		rVM = null;
		alive = true;
		reqKill = false;
		timesRun = 0;

		this.setDaemon(true);

		//Create the process monitor
		vsBlock = new ProcessMonitor();
	}

	/**
	 * Initialize the process controller. It takes an immutable SystemContext.
	 * 
	 * @param sctx
	 *           a ready SystemContext. This will be fed to all controlled
	 *           processes.
	 * @param setpid
	 *           the pid
	 * @throws VMException
	 *            is the process goes bad
	 * @see autohit.vm.VM
	 */
	public void init(SystemContext sctx, int setpid) throws VMException {
		sc = sctx;
		pid = setpid;
	}

	/**
	 * Load and Execute a VM. If a VM is already running, it will return false.
	 * Otherwise, it will return true. Otherwise, it will load it and give the
	 * greenlight to the VM thread.
	 * <p>
	 * This method can be called by any thread. It will clear the request
	 * flags.
	 * 
	 * @param aVM
	 *           A fully implimented derived-class of VM.
	 * @return true if successful and execution begun, false if another VM is
	 *         already running or there is an error.
	 * @see autohit.vm.VM
	 */
	public boolean execute(VM aVM) {

		if (rVM != null)
			return false;
		rVM = aVM;
		alive = true;
		reqKill = false;
		reqPause = false;
		reqResume = false;
		reqStop = false;
		reqState = false;

		// Attach it
		try {
			aVM.attach(this);
		} catch (Exception e) {
			return false;
		}

		// Give the green light. NO NEW CODE AFTER THIS POINT!
		// First make sure the run() is ready for us.
		if (timesRun < 1) {
			vsBlock.rendezous();
		}
		vsBlock.green();
		return true;
	}

	/**
	 * Pause the vm. This may be called by another thread. As with vmResume()
	 * and vmStop(), it posts a request to the VMContext. The context will not
	 * heed the request until the current VM.execute() is complete and the
	 * context has a chance to check for these requests.
	 * <p>
	 * There is no rendezvous; the method will not block. All successive
	 * requests count as the same request until the context services it. There
	 * is no guarentee on the timing of the service. The Context will not check
	 * for requests until AFTER the current instruction is complete. So, if the
	 * vm is executing a long wait instruction, it could indeed be some time
	 * before the request is serviced.
	 * <p>
	 * If you need to make sure that the request worked, then use the
	 * verifyState() method to get the definative state of the VM/Context.
	 * <p>
	 * As for the return value, "success" merely means that the request was
	 * successfully posted and not that the action was completed.
	 * <p>
	 * One last thaught: this is not a robust OS implimentation of a thread
	 * context. You might want to restrict calls to vmPause() and vmResume() to
	 * a single external thread. Multiple threads might get confused if they
	 * don't cooperate...
	 * 
	 * @return true is successful. false if no vm is running or it is already
	 *         paused.
	 * @see autohit.vm.VM
	 */
	public boolean vmPause() {
		if (rVM.getState() == VM.STATE_RUNNING) {
			reqPause = true;
			vsBlock.red();
			return true;
		} else
			return false;
	}

	/**
	 * Resume the vm.
	 * <p>
	 * See the notes for the pause() method.
	 * 
	 * @return true is successful. false if no vm is paused or it is already
	 *         running.
	 * @see autohit.vm.VM
	 */
	public boolean vmResume() {
		if (rVM.getState() == VM.STATE_PAUSED) {
			reqResume = true;
			vsBlock.green();
			return true;
		} else
			return false;
	}

	/**
	 * Stop the vm. This will kill it permanently, so be careful.
	 * <p>
	 * See the notes for the pause() method.
	 * 
	 * @return true is successful. false if no vm is running or paused.
	 * @see autohit.vm.VM
	 */
	public boolean vmStop() {
		if (rVM == null)
			return false;
		else {
			reqStop = true;
			vsBlock.green();
			return true;
		}
	}

	/**
	 * Get's the PID for this process
	 * 
	 * @return pid
	 */
	public int getPID() {
		return pid;
	}

	/**
	 * Join the process. ABSOLUTELY DO NOT CALL THIS FROM THE VMProcess thread.
	 * You'll deadlock!
	 */
	public void joinIt() {
		try {
			this.join();
		} catch (Exception ee) {
			// Don't care
		}
	}

	/**
	 *  Kill this context.  It is irrevocable as it will interrupt the Thread.
	 *  If you want to request the program to stop, call vmStop();
	 * <p>
	 * See the notes for the pause() method.
	 * 
	 * @return always returns true.
	 * @see autohit.vm.VM
	 */
	public synchronized boolean kill() {
		alive = false;
		vsBlock.green();
		interrupt();
		return true;
	}

	/**
	 * Verify the state of the VM. It will report a VM state value as defined
	 * in the VM class--VM.State_*. This will be the authorative state, as this
	 * method blocks until the VM has chance to clear requests and unblock it.
	 * <p>
	 * (And, never EVER call this method from within THIS thread. You'll almost
	 * certainly deadlock it.)
	 * <p>
	 * The following describes each state:
	 * 
	 * <pre>
	 *  STATE_NEW =
	 *  	VM is loaded bu not started STATE_RUNNING =
	 *  		VM is actively running.STATE_PAUSED = VM is paused.STATE_DONE = VM finished execution.This is rare,
	 *  	as the VM will be automatically unloaded when finished.STATE_NO_VM = No VM is loaded into this context.
	 *  </pre>
	 * 
	 * @return a VM.State_* value.
	 * @see autohit.vm.VM
	 */
	public int verifyState() {
		if (rVM == null)
			return VM.STATE_NO_VM;
		reqState = true;
		vsBlock.rendezous();
		return this.getState();
	}

	/**
	 * A simple request for state. It may or not be stale be the time the
	 * calling thread gets it. If you msut have THE AUTHORATIVE state, then
	 * call verifyState()
	 * 
	 * @return a VM.State_* value.
	 * @see autohit.vm.VM
	 */
	public int getState() {

		if (rVM == null)
			return VM.STATE_NO_VM;
		else
			return rVM.getState();
	}

	/**
	 * Get my system context.
	 * 
	 * @return a SystemContext
	 * @see autohit.vm.VM
	 */
	public SystemContext getSystemContext() {
		return sc;
	}

	/**
	 * Get a registered process attribute. Not implemented at this time.
	 * 
	 * @return an object that matches the name
	 * @see autohit.vm.VM
	 */
	public Object processAttribute(String name) {
		return null;
	}

	/**
	 *  Run the context */
	public void run() {

		// Make this so that it that is is owned by this thread.
		// and turn on the redlight. We won't do anything until someone
		// else turns on the green.

		// Outer loop controls the VMProcess
		do {

			// ---- DO NOT CHANGE ANYTHING BETWEEN HERE...
			// Don't do anything until given the greenlight. However,
			// if this is the first run, make sure the execute() does not
			// complete before this thread makes it to here.
			vsBlock.red();
			if (timesRun < 1) {
				vsBlock.rendezous();
			}
			vsBlock.stoplight();
			timesRun++;
			// --- ...AND HERE. ^^^^ NO CHANGE!!!!! ^^^^
			// --- IF YOU DO, YOU'RE ASKING FOR a RACE CONDITION.

			// catch any KILL request while there is no VM to run.
			if (alive == false)
				break;

			// Inner loop controls a specific VM execution
			// It's wrapped in a catch since any exception out of the VM
			// is fatal.
			try {

				// Always run the first instruction with start
				rVM.start();

				do {

					// DIE!
					if (reqKill == true) {
						alive = false;
						break;
					}

					if (reqStop == true) {
						reqStop = false;
						rVM.die();
						break; // bust out of the loop.
					}

					if (reqPause == true) {
						rVM.pause();
						reqPause = false;
					}

					if (reqResume == true) {
						rVM.resume();
						reqResume = false;
					}

					if (reqState == true) {
						reqState = false;
						vsBlock.rendezous();
						// This might be dangerous, instead of a
						// semiphore/signal. not sure
						yield();
						// We need to make sure the other thread has a chance
						// to get the status.
					}

					// see if anyone is stopping us.
					vsBlock.stoplight();

					// Execute an instruciton. An exception
					// kills this VM)
					if (rVM.getState() == VM.STATE_RUNNING)
						rVM.execute();

				} while (alive);

			} catch (VMException e) {

				// PROCESS various codes.
				if (e.numeric == VMException.CODE_SERVICE_INTENTIONAL_HALT) {
					// ORDERED HALT
					sc.getRootLogger().info("VMProcessAutomat: Process pid=" + pid + " ordered to stop.");
					alive = false;

				} else if (e.numeric == VMException.CODE_VM_DONE) {					
			
					sc.getRootLogger().info("VM: Program " + rVM.rootProgram + " done in pid=" + pid);
					
				} else if (e.numeric > VMException.FAULT) {
					// FAULTED
					try {
						sc.getRootLogger().error(
							"VMProcessAutomat: Process pid=" + pid + " died to FAULT.  message=" + e.getMessage(),
							e.numeric);
						rVM.myLog.error("VM: Process pid=" + pid + ".  I'm dying to a fatal fault.  message=" + e.getMessage(), e.numeric);
					} catch (Exception epas) {
					} // no chances
					alive = false;
				} else {
					// ERROR. Don't die.
					rVM.myLog.error(
						"VM: Process pid=" + pid + " reported an exception.  Current program died.message=" + e.getMessage(),
						e.numeric);
				}

			} catch (Exception e) {
				sc.getRootLogger().error(
					"VMProcessAutomat: Process pid="
						+ pid
						+ " died to unexpected exception.  The system may be unstable.  message="
						+ e.getMessage(),
					VMException.CODE_VM_PANIC);
				rVM.myLog.error(
					"VM: Process pid=" + pid + ".  I'm dying to a serious and unexpected exception.  message=" + e.getMessage(),
					VMException.CODE_VM_PANIC);
				alive = false;
			}

			// If we get here, the VM instance is dead
			// force the finalization here.
			try {
				rVM.finalize();
			} catch (Throwable ee) {
				//dont care
			}
			rVM = null;

		}
		while (alive);

		// If we get here, this process is dead.
		// Last out the door turn off the lights... Make sure we haven't
		// stopped anyone.
		// DO NOT put ANY other code beyond the following statements
		// or you are liable to deadlock other Threads.
		vsBlock.green();
		yield();
	}

	/**
	 * Get's root program
	 * 
	 * @return string name of the root program
	 */
	public String getRootProgram() {
		String result = Constants.UNKNOWN;
		if (rVM != null) {
			result = rVM.rootProgram;
		}
		return result;
	}

}
