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
package autohit.server;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import autohit.common.AutohitErrorCodes;
import autohit.common.AutohitLogInjectorWrapper;
import autohit.vm.VM;
import autohit.vm.VMProcess;
import autohit.common.ProcessMonitor;

/**
 * Process kernel.  This is a basic kernel.  It does not run
 * in a monitor process.  It does not route commands or events.
 *
 * @author Erich P. Gatejen
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - Initial - 17May03<br>
 * EPG - Support new VMProcess scheme - 25Jul03<br>
 * EPG - Switch PCB table to a Vector - 28Jul03
 * </code>
 * 
 */
public class Kernel {

	/**
	 *  Root system context
	 */
	private SystemContext sc;

	/**
	 *  Process table.  Maps processes to Integer(pid).
	 */
	private Hashtable ptable;

	/**
	 *  Logger
	 */
	private AutohitLogInjectorWrapper logger;

	/**
	 *  Write monitor
	 */
	private ProcessMonitor wmonitor;

	/**
	 *  The next pid to get.  One instance per jvm.
	 *  Leaving it public in care there needs to be some
	 *  segmentation voodoo in the server.
	 */
	static public int nextpid = 1;

	// Number of processes before we bother sweaping the process table
	public final static int PROCESS_CLEAN_THRESHOLD = 10;

	/**
	 *  Default constructor
	 */
	public Kernel() {
		sc = null;
		wmonitor = new ProcessMonitor();
	}

	/**
	 *  Initialize.  It might be a bad idea to call this more than once,
	 * since it could ghost a number of processes.
	 * @param c is an instance of SystemContext
	 * @see autohit.server.SystemContext
	 */
	public void init(SystemContext c) {
		sc = c;
		ptable = new Hashtable();
		logger = c.getRootLogger();
		logger.debug("Kernel: Initialized.", AutohitErrorCodes.CODE_DEBUGGING);
	}

	/**
	 *  Get a usable process.  Create it if neccesssary.  It will
	 * instantiate a default VMProcess implementation.
	 * @return a VMProcess or null if failed
	 */
	public VMProcess get() {
		return this.get("autohit.vm.VMProcessAutomat");
	}

	/**
	 *  Get a usable process.  Create it if neccesssary.
	 * @param processImpl class name for process implementation
	 * @return a VMProcess or null if failed
	 */
	public VMProcess get(String processImpl) {

		VMProcess pcb;
		VMProcess result = null;
		Object key;

		try {

			// LOCK everything
			wmonitor.waitlock();

			// Scrub the PCB table
			if (ptable.size() >= PROCESS_CLEAN_THRESHOLD) {
				this.scrubTable();
			} // end scrub table

			// Create the new process
			int newpid = this.nextPid();
			Class t = Class.forName(processImpl);
			pcb = (VMProcess) t.newInstance();

			pcb.init(sc, newpid);
			pcb.start();
			ptable.put(new Integer(newpid), pcb);
			result = pcb;

			// Unlock it
			wmonitor.unlock();

			logger.info("Kernel: Process requested by get().  PID=" + newpid, AutohitErrorCodes.CODE_INFORMATIONAL_OK);

		} catch (Exception epc) {
			logger.info(
				"Kernel: Fundimental problem trying to start a process.  The whole system may be degraded.  Message="
					+ epc.getMessage(),
				AutohitErrorCodes.CODE_CATASTROPHIC_FRAMEWORK_FAULT);
		}
		return result;
	}

	/**
	 * Get an list of active processes.  This will take a snapshot of the
	 * process list as a List of VMProcesses.
	 * BEWARE!  Processes are very volatile.  Their state can 
	 * change at any time.  It is quite possible that the VMProcess can go bad
	 * after you get the list.  Also, be extra sure not to keep any references to a
	 * VMProcess for very long.  The Kernel won't like this.
	 * @return A List of VMProcesses (it may be empty)
	 */
	public List getProcessList() {

		VMProcess pcb;

		// LOCK everything
		wmonitor.waitlock();

		// Is there any list?
		Vector theList = new Vector();
		for (Enumeration e = ptable.elements(); e.hasMoreElements();) {
			pcb = (VMProcess) e.nextElement();

			// Add only active processes to the list
			if ((pcb.getState() >= VM.STATE_ACTIVE_THRESHOLD)) {
				theList.add(pcb);
			}
		}

		wmonitor.unlock();

		return theList;
	}

	/**
	 * Get a process by pid.  it will return null if it isn't found.
		 * @param pid the pid to get.
	 * @return A VMProcess or null if not found.
	 */
	public VMProcess getProcess(int pid) {

		VMProcess pcb = null;

		// Get the process
		Integer ipid = new Integer(pid);
		if (ptable.containsKey(ipid)) {
			pcb = (VMProcess) ptable.get(ipid);
		}
		return pcb;
	}

	/**
	 * Force the PCB table to clean
	 */
	public void scrubTable() {

		// LOCK everything
		wmonitor.waitlock();

		Integer ipid;
		VMProcess pcb;
		for (Enumeration e = ptable.keys(); e.hasMoreElements();) {
			ipid = (Integer) e.nextElement();
			pcb = (VMProcess) ptable.get(ipid);
			if (pcb.getState() < VM.STATE_ACTIVE_THRESHOLD) {
				logger.debug(
					"Kernel:Defunct process removed from memory.  PID=" + ipid.toString(),
					AutohitErrorCodes.CODE_INFORMATIONAL_OK);
				pcb.kill();
				ptable.remove(ipid);
			} // end if

		} //end for

		wmonitor.unlock();
	}

	/**
	 * finalizer
	 * We will kill all the processors to make sure nothing ghosts.
	 * Why?  It is possible that other objects will hold references
	 * to the VMProcess, so will kill them here.
	 */
	protected void finalize() throws Throwable {
		VMProcess pcb;
		Object key;

		super.finalize();

		logger.info("Kernel: Exiting.  Killing managed processes.", AutohitErrorCodes.CODE_INFORMATIONAL_OK_VERBOSE);

		Set setOfPcbs = ptable.keySet();
		if (!setOfPcbs.isEmpty()) {
			Iterator i = setOfPcbs.iterator();
			while (i.hasNext()) {
				key = i.next();
				pcb = (VMProcess) ptable.get(key);
				logger.debug("Kernel: Killing process.  PID=" + pcb.getPID(), AutohitErrorCodes.CODE_DEBUGGING);
				pcb.kill();
				ptable.remove(key);
			}
		}
	}

	/**
	 * Next pid helper.
	 */
	private int nextPid() {
		int candidate = nextpid;
		nextpid++;
		return candidate;
	}

}
