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

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ListIterator;

import autohit.common.Constants;
import autohit.vm.i.VMInstruction;

/**
 * This is a wrapper for an executable.  It provides helpers and stuff.
 * You shouldn't log from these helps.  Also, most exceptions should be 
 * propogated, rather than handled, unless it's part of the normal logic.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - Initial - 15apr03</code> 
 * 
 */
public class VMExecutableWrapper {

	private ListIterator dump;

	/**
	 *  This is a reference to the executable.
	 *  @see autohit.vm.VMExecutable
	 */
	public VMExecutable exec;

	/**
	 * Default Constructor.  The wrappers are always empty.  You need
	 * to load or create the executable.
	 * @see #load(InputStream is)
	 * @see #create()
	 */
	public VMExecutableWrapper() {
		exec = null;
	}

	/**
	 *  Emit an instruction helper
	 *  @see autohit.vm.i.VMInstruction
	 */
	public void emit(VMInstruction i) {
		synchronized (exec) {
			exec.core.add(i);
		}
	}

	/**
	 *  Next IP location, if instruction were to be added
	 *  @see autohit.vm.i.VMInstruction
	 */
	public int nextIP() {
		synchronized (exec) {
			return exec.core.size();
		}
	}

	/**
	 *  Clean the core helper
	 *  @see autohit.vm.i.VMInstruction
	 */
	public void clean() {
		synchronized (exec) {
			exec.core.trimToSize();
		}
	}

	/**
	 *  Create a fresh and new executable
	 *  @see autohit.vm.VMExecutable
	 */
	public void create() {
		exec = new VMExecutable();
		exec.init();
	}

	/**
	 *  Load a VMExecutable from a stream.  This will deserialize it.
	 *  @see autohit.vm.VMExecutable
	 *  @throws any exception
	 */
	public void load(InputStream is) throws Exception {
		ObjectInputStream p = new ObjectInputStream(is);
		exec = (VMExecutable) p.readObject();
		is.close();
	}

	/**
	 *  Save a VMExecutable from a stream.  This will serialize it.
	 *  @see autohit.vm.VMExecutable
	 *  @throws any exception
	 */
	public void save(OutputStream os) throws Exception {
		ObjectOutputStream sobj = new ObjectOutputStream(os);
		sobj.writeObject(exec);
		sobj.flush();
		os.close();
	}

	/**
	 *  This starts a dump of the executable.  It will yield the first
	 *  line.  When there is no more to dump, it will return a null.
	 *
	 *  @return a String containing the first line of the dump or null.
	 */
	public String startDump() {

		VMInstruction i;
		String r;

		// if something bad happens, just return an empty dump
		try {
			dump = exec.core.listIterator();
			i = (VMInstruction) dump.next();
			r = i.toString();
		} catch (Exception e) {
			r = null;
		}
		return r;
	}

	/**
	 *  This returns the next line of the dump.  It will return null if the 
	 *  dump is done or not valid.
	 *
	 *  @return a String containing the next line of the dump or null.
	 */
	public String nextDump() {

		VMInstruction i;
		String r;

		// if something bad happens, just return an empty dump
		try {
			i = (VMInstruction) dump.next();
			r = i.toString();
		} catch (Exception e) {
			// This also traps the no next element exception
			r = null;
		}
		return r;
	}

	/**
	 *  Creates a text dump of the executable.  It will use line.seperator
	 *  system property as the line terminator.
	 *
	 *  @return a String containing the text dump.
	 */
	public String toString() {
		StringBuffer d = new StringBuffer();

		int ip = 0;
		String n = this.startDump();
		while (n != null) {
			d.append(ip + ":");
			d.append(n);
			d.append(Constants.CRUDE_SEPERATOR);
			n = this.nextDump();
			ip++;
		}
		return d.toString();
	}
}
