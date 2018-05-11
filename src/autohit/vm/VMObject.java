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
import autohit.common.Constants;
import autohit.common.ThreadContext;

/**
 * This is a lock-able, set-able object.  Field item holds the actual
 * data.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - Initial - 8may03</code> 
 * 
 */
public class VMObject {

	private Object item;
	private int owner;
	private int lockcount;

	/**
	 * Default Constructor.
	 */
	public VMObject() {
		owner = 0;
		item = null;
	}

	/**
	 * Creator Constructor.
	 * @param o the object
	 */
	public VMObject(Object o) {
		owner = Constants.NO_OWNER;
		item = null;
		item = o;
	}

	/**
	 * The calling thread will try to lock it.  If successful, it will spinlock 
	 * it
	 * @throws VMException if it is already locked.
	 */
	public synchronized void lock() throws VMException {

		int me = ThreadContext.get();
		if ((owner != Constants.NO_OWNER) && (owner != me)) {
			throw new VMException(
				"Object locked by id#" + owner,
				VMException.CODE_VM_OBJECT_LOCKED_FAULT);
		}
		owner = me;
		lockcount++;
	}

	/**
	 *  Test if I can access it
	 * @return true if I can
	 */
	public synchronized boolean test() {

		if ((owner == Constants.NO_OWNER) || (owner == ThreadContext.get())) {
			return true;
		}
		return false;
	}

	/**
	 *  Knock the spinlock down one.  If it hits zero, the lock is
	 *  removed.
	 * 	@throws VMException if it locked by something else.
	 */
	public void unlock() throws VMException {

		if ((owner == Constants.NO_OWNER) || (owner == ThreadContext.get())) {
			throw new VMException(
				"Object locked by id#" + owner,
				VMException.CODE_VM_OBJECT_LOCKED_FAULT);
		}

		lockcount--;
		if (lockcount <= 0) {
			lockcount = 0;
			owner = Constants.NO_OWNER;
			item.notifyAll();
		}
	}

	/**
	 *  Cancel all locks
	 * 	@throws VMException if it locked by something else.
	 */
	public synchronized void free() throws VMException {

		if ((owner == Constants.NO_OWNER) || (owner == ThreadContext.get())) {
			throw new VMException(
				"Object locked by id#" + owner,
				VMException.CODE_VM_OBJECT_LOCKED_FAULT);
		}
		owner = Constants.NO_OWNER;
		lockcount = 0;
	}

	/**
	 *  Set the object
	 * 	@throws VMException if it locked by something else.
	 */
	public synchronized void set(Object o) throws VMException {

		if ((owner == Constants.NO_OWNER) || (owner == ThreadContext.get())) {
			item = o;

		} else {
			throw new VMException(
				"Object locked by id#" + owner,
				VMException.CODE_VM_OBJECT_LOCKED_FAULT);
		}
	}

	/**
	 *  Get the object
	 * 	@throws VMException if it locked by something else.
	 * @return the object
	 */
	public synchronized Object get() throws VMException {

		if ((owner == Constants.NO_OWNER) || (owner == ThreadContext.get())) {
			return item;

		} else {
			throw new VMException(
				"Object locked by id#" + owner,
				VMException.CODE_VM_OBJECT_LOCKED_FAULT);
		}
	}

	/**
	 *  Get the object.  Wait for it to be free if locked.
	 * This is pretty dangerous source of deadlock.  Be careful!
	 * 	@throws VMException if it locked by something else.
	 * 	@return the object
	 */
	public Object waitingRead() {

		Object thang = null;

		while (thang == null) {

			// synchronize to keep the stooges from barging the door
			// the threads waiting for the item will all get unleashed at once.
			synchronized (this) {
				if ((owner == Constants.NO_OWNER)
					|| (owner == ThreadContext.get())) {
					// If it owns it, there is no wait.
					thang = item;
				}
			}
			try {
				item.wait();
			} catch (Exception e) {
				// Don't care
			}

		}
		return thang;
	}

	/**
	 *  Get the object.  Wait for it to be free if locked.
	 * This is pretty dangerous source of deadlock.  Be careful!
	 * 	@throws VMException if it locked by something else.
	 * 	@return the object
	 */
	public void waitingLock() {

		while (true) {

			// synchronize to keep the stooges from barging the door
			// the threads waiting for the item will all get unleashed at once.
			synchronized (this) {
				if ((owner == Constants.NO_OWNER)
					|| (owner == ThreadContext.get())) {
					// If it owns it, there is no wait.
					try {
						this.lock();
						return;
					} catch (Exception e) {
						// someone beat us to it!  Keep waiting.
					}
				}
			}
			try {
				item.wait();
			} catch (Exception e) {
				// Don't care
			}

		}
	}

}