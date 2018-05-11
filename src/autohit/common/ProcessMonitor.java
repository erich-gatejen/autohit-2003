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
package autohit.common;
import autohit.common.Constants;

/**
 * Process monitors.  There are three seperate monitors:<code>
 * 1- Locks: spinlocks on a shared lock monitor.  Includes the redlight,
 * greenlight function.
 * 2- Signals: A cummulative signal, much like a semaphore.
 * 3- Rendezous: Between two threads.
 * Each of the three monitors do not effect each other.
 * </code>
 *
 * @author Erich P. Gatejen
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - Rewrite - 16May03</code> 
 */
public class ProcessMonitor extends Object {

	private int lockcount;
	private int owner;
	private int signals;
	private boolean green;
	private boolean meeting;
	private Object rsync; // for the rendezous synchronization
	private Object ssync; // for the signal synchronization

	/**
	 *  Default constructor.  It will create the monitor that
	 *  is unlocked.
	 */
	public ProcessMonitor() {
		owner = Constants.NO_OWNER;
		green = true;
		meeting = false;
		rsync = new Object();
		ssync = new Object();
	}

	/**
	 *  Lock monitor.  Try to lock it.  If it already locked by
	 *  another thread, it will return false.  If it is locked
	 *  by the current thread, the lockcount will be incremented, and
	 *  it will return true.  If it is not locked, it will be locked and
	 *  owned by the current thread.
	 *
	 *  @return true if it locks, false it is already locked by another thread.  
	 */
	public synchronized boolean lock() {

		int me = ThreadContext.get();
		if ((owner != Constants.NO_OWNER) && (owner != me)) {
			return false;
		}
		owner = me;
		lockcount++;
		return true;
	}

	/**
	 *  Knock the spinlock down one.  If it hits zero, the lock is
	 *  removed.  If the lock is owned by another thread, it will return
	 *  false, otherwise it will return true--even if the spinlock hasn't
	 *  hit zero.
	 * 	@return true for success, false for owned by another thread.
	 */
	public synchronized boolean unlock() {

		if ((owner != Constants.NO_OWNER) && (owner != ThreadContext.get())) {
			return false;
		}

		lockcount--;
		if (lockcount <= 0) {
			lockcount = 0;
			owner = Constants.NO_OWNER;
			this.notifyAll();
		}
		return true;
	}

	/**
	 *  Cancel all locks.  Returns false if the lock is owned by
	 * another thread.  
	 * 	@return true for success, otherwise false
	 */
	public synchronized boolean free() {

		if ((owner != Constants.NO_OWNER) || (owner != ThreadContext.get())) {
			return false;
		}
		owner = Constants.NO_OWNER;
		lockcount = 0;
		this.notifyAll();
		return true;
	}

	/**
	 *  Wait for a lock.
	 * 	@return true for success, otherwise false
	 * TODO Look into the localized synchronization
	 */
	public void waitlock() {

		// The notify's are just a kick

		int me = ThreadContext.get();
		if ((owner != Constants.NO_OWNER) && (owner != me)) {

			// Wait for it.
			while (owner != Constants.NO_OWNER) {
				try {
					// TODO Is sync in waitlock() going to cause deadlock?
					synchronized (this) {
						this.wait();
					}
				} catch (InterruptedException e) {
				}
			}
			owner = me;
			lockcount++;
			synchronized (this) {
				this.notifyAll();
			}

		} else {
			// I own it, so just up the spinlock.
			lockcount++;
			synchronized (this) {
				this.notify();
			}
		}
	}

	/**
	 *  Wait for the green light.  
	 */
	public void stoplight() {

		// Wait for it.
		while (!green) {
			try {
				// TODO Is sync in stoplight() going to cause deadlock?
				synchronized (this) {
					this.wait();
				}
			} catch (InterruptedException e) {
			}
		}
		//synchronized (this) {  // I don't think I need this.
		//	this.notifyAll();
		//}
	}

	/**
	 *  Turn on the green light.
	 */
	public synchronized void green() {
		green = true;
		this.notifyAll();
	}

	/**
	 *  Turn on the green light.
	 */
	public synchronized void red() {
		green = false;
		this.notifyAll();
	}

	/**
	 *  Wait for a signal.  It's pretty mch a semaphore.
	 */
	public void waitSignal() throws InterruptedException {

		// lock on the ssync object.  if there is a singal
		// already, just decrement it and return.
		synchronized (ssync) {

			while (signals < 1) {
				ssync.wait();
			}
			signals--;
		}
	}

	/**
	 * Send a signal.  They are cummulative and independent of the other
	 * monitors.
	 */
	public void signal() {
		
		synchronized (ssync) {
			signals++;
			ssync.notifyAll();
		}
	}

	/**
	 *  Rendevous between two threads-- not signal.  The first in the 
	 *  door will wait for the second.  You should only use this for synchronization
	 *  between two threads--NO MORE THAN TWO!
	 */
	public void rendezous() {

		boolean wait;

		synchronized (rsync) {
			if (meeting == false)
				wait = true;
			else
				wait = false;

			if (wait) {
				meeting = true;
				try {
					rsync.wait();
				} catch (Exception e) {
					// dont care
				}
			} else {
				meeting = false;
				rsync.notify();
			}
		}
	}

}
