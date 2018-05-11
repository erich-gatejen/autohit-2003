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

import autohit.server.SystemContext;

/**
 * This defines the interface to a process controller.
 * <p>
 * BE SURE to .init() this controller BEFORE any other threads
 * access its methods (particularly execute()).  Some implementations
 * my be bases on Threads.  Be sure to .start() after init()
 * Failure to heed this
 * warning COULD result in a race condition...  :-)
 * <p><pre>
 * There are four "commands" to a process controller:
 * - PAUSE will hold execution
 * - RESUME will restart execution
 * - STOP will stop execution of the VM and dump it
 * - KILL will stop the execution and let this process die!
 * </pre>
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - New - 24Jul03 </code>
 */
public interface VMProcess {

	/**
	 *  Initialize the process controller.  It takes an immutable SystemContext.
	 *  @param sctx a ready SystemContext.  This will be fed to all controlled processes.
	 *  @param setpid the pid
	 *  @throws VMException is the process goes bad
	 *  @see autohit.vm.VM    
	 */
	public void init(SystemContext sctx, int setpid) throws VMException;

	/**
	 *  Load and Execute a VM.  If a VM is already running, it will return 
	 *  false.  Otherwise, it will return true.  Otherwise, it will load it
	 *  and give the greenlight to the VM thread.
	 *  <p>
	 *  This method can be called by any thread.  It will clear the 
	 * request flags.
	 *
	 *  @param aVM A fully implimented derived-class of VM.
	 *  @return true if successful and execution begun, false if
	 *               another VM is already running or there is an error.
	 *
	 *  @see autohit.vm.VM    
	 */
	public boolean execute(VM aVM);

	/**
	 *  Pause the vm.  This may be called by another thread.
	 *
	 *  As with vmResume() and vmStop(), it posts a request to the
	 *  VMContext.  The context will not heed the request until
	 *  the current VM.execute() is complete and the context has
	 *  a chance to check for these requests.
	 *  <p>
	 *  There is no rendezvous; the method will not block.  All successive
	 *  requests count as the same request until the context services
	 *  it.  There is no guarentee on the timing of the service.  The Context
	 *  will not check for requests until AFTER the current instruction is complete.
	 *  So, if the vm is executing a long wait instruction, it could indeed be
	 *  some time before the request is serviced.
	 *  <p>
	 *  If you need to make sure that the request worked, then use
	 *  the verifyState() method to get the definative state of the
	 *  VM/Context.
	 *  <p>
	 *  As for the return value, "success" merely means that the
	 *  request was successfully posted and not that the action
	 *  was completed.
	 *  <p>
	 *  One last thaught: this is not a robust OS implimentation of
	 *  a thread context.  You might want to restrict calls to vmPause()
	 *  and vmResume() to a single external thread.  Multiple threads
	 *  might get confused if they don't cooperate...
	 *
	 *  @return true is successful.  false if no vm is running or
	 *               it is already paused.
	 *  @see autohit.vm.VM    
	 */
	public boolean vmPause();

	/**
	 *  Resume the vm.
	 *  <p>
	 *  See the notes for the pause() method.
	 *
	 *  @return true is successful.  false if no vm is paused or
	 *               it is already running.
	 *  @see autohit.vm.VM    
	 */
	public boolean vmResume();

	/**
	 *  Stop the vm.  This will kill it permanently, so be careful.
	 *  <p>
	 *  See the notes for the pause() method.
	 *
	 *  @return true is successful.  false if no vm is running or
	 *               paused.
	 *  @see autohit.vm.VM    
	 */
	public boolean vmStop();

	/**
	 *  Get's the PID for this process
	 *
	 *  @return pid
	 */
	public int getPID();

	/**
	 *  Get's root program
	 *
	 *  @return string name of the root program
	 */
	public String getRootProgram();

	/**
	 *  Kill this context.  It is irrevocable as it will interrupt the Thread.
	 *  If you want to request the program to stop, call vmStop();
	 *  <p>
	 *  See the notes for the pause() method.
	 *
	 *  @return always returns true.
	 *  @see autohit.vm.VM    
	 */
	public boolean kill();

	/**
	 *  Verify the state of the VM.  It will report a VM state 
	 *  value as defined in the VM class--VM.State_*.  This will
	 *  be the authorative state, as this method blocks until the
	 *  VM has chance to clear requests and unblock it.
	 *  <p>
	 *  (And, never EVER call this method from within THIS thread.
	 *   You'll almost certainly deadlock it.)
	 *  <p>
	 *  The following describes each state:
	 *  <pre>
	 *         STATE_NEW          = VM is loaded bu not started
	 *         STATE_RUNNING      = VM is actively running.
	 *         STATE_PAUSED       = VM is paused.
	 *         STATE_DONE         = VM finished execution.
	 *                              This is rare, as the VM will
	 *                              be automatically unloaded when
	 *                              finished.
	 *         STATE_NO_VM        = No VM is loaded into this context.
	 *  </pre>
	 * 
	 *  @return a VM.State_* value.
	 *  @see autohit.vm.VM    
	 */
	public int verifyState();

	/**
	 *  A simple request for state.  It may or not be stale be the
	 *  time the calling thread gets it.  If you msut have THE
	 *  AUTHORATIVE state, then call verifyState()
	 *  
	 *  @return a VM.State_* value.
	 *  @see autohit.vm.VM    
	 */
	public int getState();

	/**
	 *  Get my system context.
	 *  
	 *  @return a SystemContext
	 *  @see autohit.vm.VM    
	 */
	public SystemContext getSystemContext();

	/**
	 *  Get a registered process attribute.
	 *  
	 *  @return an object that matches the name
	 *  @see autohit.vm.VM    
	 */
	public Object processAttribute(String name);

	/**
	 *  Run the process.
	 */
	public void run();


	/**
	 *  Run the process.
	 */
	public void start();

	/**
	 *  Join the process
	 */
	public void joinIt();

}
