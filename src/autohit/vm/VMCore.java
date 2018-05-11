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
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Set;
import java.util.Stack;

import autohit.vm.i.VMIScope;

/**
 * There are four storage mechanisms.<p>
 * 1- A UNIVERSE: A persistent store accessible by everyone and 
 * addressable by name.
 * <p>
 * 2- ENVIRONMENT core:  A shared store accessible by everyone
 * containing name/value pairs.  There is only one per system, so
 * all references point to a single static instance.<br>
 * <pre>
 * set(String name, Object o)
 * read(String name)
 * test(String name)
 * lock(String name)
 * unlock(String name)
 * waitingLock(String name)
 * </pre>
 * <p>
 * 3- PERSISTANT core:  An owned store per VM that contains 
 * name/object pairs.  It is not subject to scope rules.  There can
 * only be one instance of any named object.  It is implemented as a
 * HashMap.  This primarily meant to move object data between services, 
 * executables, and calls.  This is NOT syncronized and threadsafe.<br>
 * <pre>
 * persist(String name, Object o)
 * free(String name)
 * has(String name)
 * get(String name)
 * </pre>
 * <p>
 * 4- STORAGE code: An owned store per VM that contains name/value
 * pairs.  It is subject to scope rules.  Within the reference of
 * a single scope, there is only one valid instance of a new
 * instance is created, it will supercede the previous.  The storage
 * system uses Stacks on Hashmaps to maintain scope rules.  When
 * the scope is discarded, it will take all instances 
 * with it.  Of course, any instances  created in the prior scope(s)
 * will still be there.  This is NOT syncronized and threadsafe.<br>
 * <pre>
 * store(String name, Object o) 
 * remove(String name)
 * exists(String name)
 * fetch(String name)
 * replace(String name, Object o)
 * getStorageNameSet()
 * </pre>
 * <p>
 * You should not access the scope stack directly.
 * <p>
 * @see #set(String name, Object o)
 * @see #read(String name)
 * @see #test(String name)
 * @see #lock(String name)
 * @see #unlock(String name)
 * @see #waitingLock(String name)
 * @see #persist(String name, Object o)
 * @see #free(String name)
 * @see #has(String name)
 * @see #get(String name)
 * @see #store(String name, Object o) 
 * @see #remove(String name)
 * @see #exists(String name)
 * @see #fetch(String name)
 * @see #replace(String name, Object o)
 * 
 * @author Erich P. Gatejen
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - Rewrite - 5Mayt03 
 * 
 */
public class VMCore implements Serializable {

	final static long serialVersionUID = 1;
	
	/**
	 * Storage space.  It is subject to scope rules.  If more than one
	 * item is stored in the same name, it is converted to a stack bucket
	 * and items are stacked.<p>
	 * @see #store(String name, Object o) 
	 * @see #remove(String name)
	 * @see #exists(String name)
	 * @see #fetch(String name)
	 * @see #replace(String name, Object o)
	 */
	protected HashMap storage;

	/**
	 * Persistant storage space.  It is NOT subject to scope rules.  There cannot
	 * be more than one instance of an item.<p>
	 * @see #persist(String name, Object o)
	 * @see #free(String name)
	 * @see #has(String name)
	 * @see #get(String name)
	 */
	protected HashMap persists;

	/**
	 *  Scope stack.
	 *
	 *  Do NOT use scope.pop() or scope.push() yourself!  We must maintain the
	 *  scope cache dirty flag.  However, you can use peek(), empty(), and
	 *  search() at your leasure().
	 */
	protected Stack scope;

	/**
	 *  Scope stack cache dirty flag.  Will be automatically set when any
	 *  scope stack methods are used.
	 */
	protected boolean scDirty;

	/**
	 * Environment.  System wide store.  Set by the factory.<p>
	 * @see #set(String name, Object o)
	 * @see #read(String name)
	 * @see #test(String name)
	 * @see #lock(String name)
	 * @see #unlock(String name)
	 * @see #waitingLock(String name)
	 */
	public Hashtable environment;

	/**
	 *  Call cache.
	 */
	public HashMap callcache;

	/**
	 *  Default constructor.  Use this if you want a private environment.
	 *  Of course, that might be pointless.
	 */
	public VMCore() {
		environment = new Hashtable();
		init();
	}

	/**
	 *  Constructor.  Gets a reference to the environment.
	 */
	public VMCore(Hashtable env) {
		environment = env;
		init();
	}

	/**
	 *  Private initializer
	 */
	private void init() {
		storage = new HashMap();
		persists = new HashMap();
		callcache = new HashMap();
		scope = new Stack();
		scDirty = false;
	}

	/**
	 *  Push an object onto the scope stack.
	 *
	 *  @param o the object
	 */
	public void push(Object o) {
		scDirty = true;
		scope.push(o);
	}

	/**
	 *  Check to see if the stack is dirty.  This is not thread safe.
	 * @return true is the stack is dirty
	 */
	public boolean isDirty() {
		return scDirty;
	}

	/**
	 *  Pop an object off the stack.  USE THIS instead of scope.pop()!!!
	 *  Have to dirty the cache flag...
	 * <p>
	 *  It'll throw any exception it encounters--most likely a EmptyStackException.
	 * @return an object reference
	 * @throws Exception
	 */
	public Object pop() throws Exception {
		scDirty = true;
		return scope.pop();
	}

	/**
	 *  Peek into the scope stack.
	 *
	 *  @return a reference to the object or NULL if the stack is empty
	 */
	public Object peek() {
		Object ob = null;
		try {
			ob = scope.peek();
		} catch (Exception e) {
			// don't care
		}
		return ob;
	}

	/**
	 * Store an item.  It is subject to scope rules.  It will
	 * overload a prior instance with the same name, but not overwrite it. 
	 *
	 * @param name object name as a string
	 * @param o the object
	 * @throws VMException
	 * @see autohit.vm.VMException
	 */
	public void store(String name, Object o) throws VMException {
		Object thang;
		scope.push(name);
		try {
			// Is there something there already?
			if (storage.containsKey(name)) {
				// yes - is there more than one in a stack?
				thang = storage.get(name);
				if (thang instanceof Stack) {
					// no.  Add it to the stack
					 ((Stack) thang).push(o);

				} else {
					// Create a stack and add them both
					Stack thangs = new Stack();
					thangs.push(thang);
					thangs.push(o);
					storage.put(name, thangs);
				}

			} else {
				// no.  just toss it on
				storage.put(name, o);
			}

		} catch (Exception e) {
			throw new VMException(
				"Core failed to store " + name + ".  " + e.getMessage(),
				VMException.CODE_VM_CORE_FAILED_STORE_FAULT,
				e);
		}
	}

	/**
	 * Removes an object from a store.  No real reasion to use this.
	 * It will throw an exception only if something realy bad happened.
	 * This will not remove the item from the scope stack!
	 *
	 * @param name object name as a string
	 * @throws VMException
	 * @see autohit.vm.VMException
	 */
	public void remove(String name) throws VMException {

		Object thang;

		try {
			// Is it even in the store?
			if (storage.containsKey(name)) {

				thang = storage.get(name);
				// is it a stack or a solo item
				if (thang instanceof Stack) {

					// Is there one or two items in the stack?
					if (((Stack) thang).size() > 2) {
						// there is a bunch.  pop the one
						 ((Stack) thang).pop();

					} else {
						// there are only two.  discard the stack and keep the 
						// last item
						 ((Stack) thang).pop();
						thang = ((Stack) thang).pop();
						storage.put(name, thang);
					}

				} else {
					// Solo item - toss it
					storage.remove(name);
				}
			}

		} catch (Exception e) {
			throw new VMException(
				"VM: Core failed on remove for "
					+ name
					+ ".  "
					+ e.getMessage(),
				VMException.CODE_VM_CORE_GENERAL_FAULT,
				e);
		}
	}

	/**
	 * Check for an object in storage. 
	 *
	 * @param name object name as a string
	 * @throws VMException
	 * @see autohit.vm.VMException
	 */
	public boolean exists(String name) throws VMException {

		try {
			if (storage.containsKey(name)) {
				return true;
			}

		} catch (Exception e) {
			throw new VMException(
				"VM: Core failed storage check for "
					+ name
					+ ".  "
					+ e.getMessage(),
				VMException.CODE_VM_CORE_GENERAL_FAULT,
				e);
		}
		return false;
	}

	/**
	 * Fetch an object reference in storage. 
	 *
	 * @param name object name as a string
	 * @return the object or null if it can't be found
	 * @throws VMException
	 * @see autohit.vm.VMException
	 */
	public Object fetch(String name) throws VMException {

		Object thang;

		try {
			// is it even there?
			if (storage.containsKey(name)) {

				thang = storage.get(name);

				// a stack or naked?
				if (thang instanceof Stack) {
					return ((Stack) thang).peek();
				} else {
					return thang;
				}
			}

		} catch (Exception e) {
			throw new VMException(
				"VM: Core failed to fetch " + name + ".  " + e.getMessage(),
				VMException.CODE_VM_CORE_FAILED_RETRIEVAL_FAULT,
				e);
		}
		return null;
	}

	/**
	 * Get a Set of variables in scope in storage. 
	 *
	 * @return A Set of Strings that are the variable names.
	 * @throws VMException
	 * @see autohit.vm.VMException
	 */
	public Set getStorageNameSet() throws VMException {

		Set keySet;

		try {
			keySet = storage.keySet();

		} catch (Exception e) {
			throw new VMException(
				"VM: Core failed to getStorageNameSet() the variables.  " + e.getMessage(),
				VMException.CODE_VM_CORE_FAILED_RETRIEVAL_FAULT,
				e);
		}
		return keySet;
	}
	
	/**
	 * Replace an object in storage.  Obviously it will replace the nearest
	 * in scope.  If the object doesn't exist, it will throw an exception.
	 *
	 * @param name object name as a string
	 * @param o object reference
	 * @throws VMException
	 * @see autohit.vm.VMException
	 */
	public void replace(String name, Object o) throws VMException {

		Object thang;

		// Is there something there already?
		if (storage.containsKey(name)) {

			try {
				// yes - is there more than one in a stack?
				thang = storage.get(name);
				if (thang instanceof Stack) {
					// yes.  pop the top and push the replacement
					 ((Stack) thang).pop();
					((Stack) thang).push(o);

				} else {
					// replace it
					storage.put(name, o);
				}
			} catch (Exception e) {
				throw new VMException(
					"Core failed to replace " + name + ".  " + e.getMessage(),
					VMException.CODE_VM_CORE_FAILED_STORE_FAULT);
			}

		} else {
			// no.  just toss it on
			throw new VMException(
				"Object doesnt exist in core storage: " + name,
				VMException.CODE_VM_CORE_DOESNT_EXIST_FAULT);
		}

	}

	// -- PERSIST

	/**
	 * Persist an item.  It is NOT subject to scope rules.  It will
	 * overwrite a prior instance of it. 
	 *
	 * @param name object name as a string
	 * @param o the object
	 * @throws VMException
	 * @see autohit.vm.VMException
	 */
	public void persist(String name, Object o) throws VMException {

		try {
			persists.put(name, o);
		} catch (Exception e) {
			throw new VMException(
				"VM: Core failed to persist " + name + ".  " + e.getMessage(),
				VMException.CODE_VM_CORE_FAILED_STORE_FAULT,
				e);
		}
	}

	/**
	 * Free an item from persistant storage.  
	 * 
	 * @param name object name as a string
	 * @throws VMException
	 * @see autohit.vm.VMException
	 */
	public void free(String name) throws VMException {

		try {
			if (persists.containsKey(name)) {
				persists.remove(name);
			}
		} catch (Exception e) {
			throw new VMException(
				"VM: Core failed to free " + name + ".  " + e.getMessage(),
				VMException.CODE_VM_CORE_GENERAL_FAULT,
				e);
		}
	}

	/**
	 * Get an object in persistant storage. 
	 *
	 * @param name object name as a string
	 * @return the object or null if it doesn't exist.
	 * @throws VMException
	 * @see autohit.vm.VMException
	 */
	public Object get(String name) throws VMException {
		try {
			if (persists.containsKey(name)) {
				return persists.get(name);
			}
		} catch (Exception e) {
			throw new VMException(
				"VM: Core failed to get " + name + ".  " + e.getMessage(),
				VMException.CODE_VM_CORE_FAILED_RETRIEVAL_FAULT,
				e);
		}
		return null;
	}

	/**
	 * Check persistant storage for an object. 
	 *
	 * @param name object name as a string
	 * @throws VMException
	 * @see autohit.vm.VMException
	 * @return if the object exists
	 */
	public boolean has(String name) throws VMException {
		try {
			if (persists.containsKey(name)) {
				return true;
			}

		} catch (Exception e) {
			throw new VMException(
				"VM: Core failed persistant storage check for "
					+ name
					+ ".  "
					+ e.getMessage(),
				VMException.CODE_VM_CORE_GENERAL_FAULT,
				e);
		}
		return false;
	}

	/**
	 *  Marks a scope on the scope stack
	 */
	public void markScope() {
		VMIScope vs = new VMIScope();
		scope.push(vs);
	}

	/**
	 *  Discard scope frame.  This will remove all items on the scope to and 
	 *  including the top-most recent VMIScope object.  It will pop the whole damned stack if it doesn't find one...
	 * @throws any VMException
	 */
	public void discardScopeFrame() throws VMException {

		scDirty = true;
		Object item;

		try {

			// Just keep poping until we get to the scope.
			item = scope.pop();
			while (!(item instanceof VMIScope)) {

				// If we are here, then it's going to be a string name
				this.remove((String) item);
				item = scope.pop();
			}

		} catch (VMException e) {
			// This is bad.  Propagate it.
			throw e;
		} catch (Exception e) {
			// looks like we emptied the whole stack.  BAD! 
			throw new VMException(
				"VM: SOFTWARE DETECTED FAULT.  Emptied the scope stack.  Bad thing(tm)!  "
					+ e.getMessage(),
				VMException.CODE_VM_SOFTWARE_DETECTED_FAULT,
				e);
		}
	}

	// -- ENVIRONMENT

	/**
	 * Lock an item in the environment.  It will get a reference to the
	 * object.
	 * @param name object name as a string
	 * @return an vmobject reference or null if it is already locked
	 * @throws VMException for en error or if the item doesn't exist.
	 * @see autohit.vm.VMException
	 */
	public Object lock(String name) throws VMException {

		VMObject to;

		try {
			if (environment.containsKey(name)) {

				// lock it if we can
				to = (VMObject) environment.get(name);
				if (to.test()) {
					to.lock();
					return to.get();
				}

			} else {
				throw new VMException(
					"VM: Core failed to lock " + name + ".  It does not exist.",
					VMException.CODE_VM_CORE_DOESNT_EXIST_FAULT);
			}

		} catch (VMException e) {
			throw e;
		} catch (Exception e) {
			throw new VMException(
				"VM: Core failed to lock due to system fault "
					+ name
					+ ".  "
					+ e.getMessage(),
				VMException.CODE_VM_CORE_FAILED_CONTROL_FAULT,
				e);
		}
		return null;
	}

	/**
	 * Unlock an environment item if we can.  Ignore errors except if the
	 * item doesn't exist or it is owned by someone else.
	 * @param name object name as a string
	 * @throws VMException for en error or if the item doesn't exist.
	 * @see autohit.vm.VMException
	 */
	public void unlock(String name) throws VMException {

		VMObject to;

		try {
			if (environment.containsKey(name)) {

				// lock it if we can
				to = (VMObject) environment.get(name);
				if (to.test()) {
					to.unlock();
				}

			} else {
				throw new VMException(
					"VM: Core failed to lock " + name + ".  It does not exist.",
					VMException.CODE_VM_CORE_DOESNT_EXIST_FAULT);
			}

		} catch (VMException e) {
			throw e;
		} catch (Exception e) {
			throw new VMException(
				"VM: Core failed to lock due to system fault "
					+ name
					+ ".  "
					+ e.getMessage(),
				VMException.CODE_VM_CORE_FAILED_CONTROL_FAULT,
				e);
		}
	}

	/**
	 * Read an environment item if we can.  Ignore errors except if the
	 * item doesn't exist or it is owned by someone else.
	 * @param name object name as a string
	 * @throws VMException for en error or if the item doesn't exist.
	 * @return the item or null if it is empty
	 * @see autohit.vm.VMException
	 */
	public Object read(String name) throws VMException {

		VMObject to;

		try {
			if (environment.containsKey(name)) {

				// lock it if we can
				to = (VMObject) environment.get(name);
				if (to.test()) {
					return to.get();
				}

			} else {
				throw new VMException(
					"VM: Core failed to read() "
						+ name
						+ ".  It does not exist.",
					VMException.CODE_VM_CORE_DOESNT_EXIST_FAULT);
			}

		} catch (VMException e) {
			throw e;
		} catch (Exception e) {
			throw new VMException(
				"VM: Core failed to read() due to system fault "
					+ name
					+ ".  "
					+ e.getMessage(),
				VMException.CODE_VM_CORE_FAILED_RETRIEVAL_FAULT,
				e);
		}
		return null;
	}

	/**
	 * Set an environment item if we can.  This will throw an exception if 
	 * the item exists and is locked.  If it doesn't exist, this will create it.
	 * @param name object name as a string
	 * @throws VMException for en error or if the item doesn't exist.
	 * @return the item or null if it is empty
	 * @see autohit.vm.VMException
	 */
	public Object set(String name, Object o) throws VMException {

		VMObject to;

		try {
			// does it already exist?
			if (environment.containsKey(name)) {

				// lock it if we can
				to = (VMObject) environment.get(name);
				if (to.test()) {
					to.set(o);
				} else {
					throw new VMException(
						"VM: Core failed to set() "
							+ name
							+ ".  It exists and is locked.",
						VMException.CODE_VM_OBJECT_LOCKED_FAULT);
				}

			} else {

				// no - create it
				to = new VMObject(o);
				environment.put(name, o);
			}

		} catch (VMException e) {
			throw e;
		} catch (Exception e) {
			throw new VMException(
				"VM: Core failed to set() due to system fault "
					+ name
					+ ".  "
					+ e.getMessage(),
				VMException.CODE_VM_CORE_FAILED_STORE_FAULT,
				e);
		}
		return null;
	}

	/**
	 * Wait for a lock on an environment.  It will get a reference to the
	 * object.
	 * @param name object name as a string
	 * @return an vmobject reference or null if it is already locked
	 * @throws VMException for en error or if the item doesn't exist.
	 * @see autohit.vm.VMException
	 */
	public Object waitingLock(String name) throws VMException {

		VMObject to;
		Object thang;

		try {
			if (environment.containsKey(name)) {

				// lock it if we can
				to = (VMObject) environment.get(name);
				to.waitingLock();
				return to.get();

			} else {
				throw new VMException(
					"VM: Core failed to waitlock "
						+ name
						+ ".  It does not exist.",
					VMException.CODE_VM_CORE_DOESNT_EXIST_FAULT);
			}

		} catch (VMException e) {
			throw e;
		} catch (Exception e) {
			throw new VMException(
				"VM: Core failed to waitlock due to system fault "
					+ name
					+ ".  "
					+ e.getMessage(),
				VMException.CODE_VM_CORE_FAILED_CONTROL_FAULT,
				e);
		}
		//return null;
	}

	/**
	 * Test if environment item is accessable.  
	 * @param name object name as a string
	 * @return true if it is available, otherwise false.
	 * @throws VMException for en error or if the item doesn't exist.
	 * @see autohit.vm.VMException
	 */
	public boolean test(String name) throws VMException {

		VMObject to;

		try {
			if (environment.containsKey(name)) {

				// lock it if we can
				to = (VMObject) environment.get(name);
				return to.test();

			} else {
				throw new VMException(
					"VM: Core failed to waitlock "
						+ name
						+ ".  It does not exist.",
					VMException.CODE_VM_CORE_DOESNT_EXIST_FAULT);
			}

		} catch (VMException e) {
			throw e;
		} catch (Exception e) {
			throw new VMException(
				"VM: Core failed to waitlock due to system fault "
					+ name
					+ ".  "
					+ e.getMessage(),
				VMException.CODE_VM_CORE_FAILED_CONTROL_FAULT,
				e);
		}
		//return null;
	}

	/**
	 *  Dump the core to a string.
	 *
	 *  @return an empty string for now.  this will be dangerous.
	 * TODO implement toString for VMCore
	 */
	public String toString() {
		return "not implemented yet";
	}

}
