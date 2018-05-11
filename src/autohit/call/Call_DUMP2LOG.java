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
package autohit.call;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import autohit.common.AutohitProperties;
import autohit.common.Constants;
import autohit.universe.Universe;
import autohit.vm.VMLoader;

/**
 * DUMP2LOG call.  Dumps a string to new, unique file in the /log directory.
 * <pre>
 * REQURIES: logger, core
 * IGNORES: uni
 * PARAMETERS (INPUT):
 	text= text entry
 * </pre>
 * RETURNS: name of file in /log.
 *
 * @author Erich P. Gatejen
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - Initial - 08Jul03</code>
 * 
 */
public class Call_DUMP2LOG extends Call {

	// total number of times to try and find a unique file.
	public final static int MAX_TRIES = 5;

	/**
	 * Implement this to handle load time initialization.  The 
	 * four main fields will already be set--vmc, sc, log, and u.
	 * You must implement this, but you don't have to do anything.
	 * Remember that calls are cached per VM and reused as often
	 * as possible.  There will be no thread-safety issues with the
	 * VMCore or log, but the SystemContecxt and Universe may be shared.
	 * @throws CallException
	 */
	public void load_chain() throws CallException {
		// Nothing to do.
	}

	/**
	 * Implement this to return the name of the CALL
	 * @return name of the CALL
	 */
	public String name() {
		return "DUMP2LOG";
	}

	/**
	 * Execute it.
	 * @return the result or null if there is no result
	 */
	public String call() throws CallException {

		String result = Constants.EMPTY_LEFT;

		try {

			// Find the table object.  Make sure it is a Hashtable
			String text = (String) this.desiredString("text");
			if (text == null) {
				return result;
			}

			// Find a unique file name
			VMLoader vl = sc.getLoader();
			String p = vl.property(AutohitProperties.ROOT_PATH);
			String path = p + AutohitProperties.vLOG_ROOT;
			String proposed = Long.toString(System.currentTimeMillis()) + "0";
			File f = null;
			int i;
			for (i = 0; i < MAX_TRIES; i++) {
				f =
					new File(
						path
							+ proposed
							+ AutohitProperties.literal_FS_DUMP_EXTENSION);
				if (!f.exists())
					break;
				proposed =
					proposed.substring(0, proposed.length() - 1)
						+ Integer.toString(i);
			}

			if (i < MAX_TRIES) {
				result = proposed;
				BufferedWriter out = new BufferedWriter(new FileWriter(f));
				out.write(text);
				out.close();
				this.info(
					"Succeeded with the dump.  Put in the file="
						+ path
						+ proposed
						+ AutohitProperties.literal_FS_DUMP_EXTENSION);

			} else {
				throw new CallException(
					this.format(
						"Failed trying to create unique filename.  Last try="
							+ path
							+ proposed
							+ AutohitProperties.literal_FS_DUMP_EXTENSION),
					CallException.CODE_CALL_UNRECOVERABLE_FAULT);
			}

		} catch (Exception e) {
			throw new CallException(
				this.format(
					"Exception while trying to dump.  error=" + e.getMessage()),
				CallException.CODE_CALL_ERROR,
				e);
		}
		return result;
	}

	/**
	 * Execute using the passed universe, rather than the loaded.
	 * @param uni a universe
	 * @return the result or null if there is no result
	 * @see autohit.universe.Universe
	 */
	public String call(Universe uni) throws CallException {
		return this.call();
	}
}
