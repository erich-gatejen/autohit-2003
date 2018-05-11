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
package autohit.common.channels;

import java.util.LinkedList;
import autohit.common.ProcessMonitor;

/**
 * Basically, a queued drain. It is absolutely threadsafe. It can be used for
 * interprocess communication.
 * 
 * @author Erich P. Gatejen
 * @version 1.0 <i>Version History</i><code>EPG - Rewrite - 17Sep03</code>
 */
public class QueuedDrain implements Drain {

	/**
	 * INTERNAL DATA */
	private LinkedList fifo;
	private ProcessMonitor monitor;
	private String drainname;
	private int sequence; // Let overflow wrap it

	/**
	 * Constructor */
	public QueuedDrain() {
		super();
		drainname = "unnamed QueueDrain";
		monitor = new ProcessMonitor();
		fifo = new LinkedList();
	}

	/**
	 * Initializer
	 * 
	 * @param name
	 *           of the drain
	 */
	public void init(String name) throws ChannelException {
		drainname = name;
	}

	/**
	 * Post an item
	 * 
	 * @param a
	 *           An atom containing the posted data
	 * @return a receipt
	 * @throws ChannelException
	 */
	public Receipt post(Atom a) throws ChannelException {

		Receipt rr = null;

		try {
			monitor.waitlock();
			fifo.add(a);

			// Build the receipt
			sequence++;
			rr = new QueueReceipt(a.senderID, drainname, sequence);

			// Let someone waiting on the fifo go
			monitor.signal();

		} catch (Exception ee) {
			throw new ChannelException(
				"Post to QueuedDrain failed due to exception.  message=" + ee.getMessage(),
				ChannelException.CODE_CHANNEL_DRAIN_GENERAL_FAULT);
		} finally {
			try {
				monitor.unlock();
			} catch (Exception e) {
				// Don't care
			}
		}
		return rr;
	}

	/**
	 * Is there something available.   There is no guarantee that between a
	 * poll() and a get() that there will still be something there.
	 * 
	 * @return true if there is something in the queue, otherwise false
	 */
	public boolean poll() {
		if (fifo.size() > 0)
			return true;
		return false;
	}

	/**
	 * Get something from the queue. There is no guarantee that anything is
	 * there. If there isn't, it will return a null.
	 * 
	 * @return An Atom or null
	 * @throws ChannelException
	 * @see Atom
	 */
	public Atom get() throws ChannelException {

		Atom candidate = null;

		try {
			monitor.waitlock();
			if (fifo.size() > 0) {
				candidate = (Atom) fifo.removeFirst();
			}

		} catch (Exception ee) {
			throw new ChannelException(
				"FAULT in " + drainname + ".get().  message=" + ee.getMessage(),
				ChannelException.CODE_CHANNEL_DRAIN_GENERAL_FAULT);

		} finally {
			try {
				monitor.unlock();
			} catch (Exception eee) {
				// Don't care
			}
		}
		return candidate;
	}

	/**
	 * Block until there is something in the queue. It will always return
	 * something
	 * 
	 * @return An Atom or null
	 * @throws ChannelException
	 * @see Atom
	 */
	public Atom block() throws ChannelException {
		Atom candidate = this.get();

		try {
			while (candidate == null) {
				monitor.waitSignal();
				candidate = this.get();
			}
		} catch (InterruptedException ie) {
			throw new ChannelException("Channel.QueueDrain.block() interrupted.", ChannelException.CODE_CHANNEL_INTERRUPTED);
		}
		return candidate;
	}

}
