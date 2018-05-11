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

import java.util.Hashtable;
import java.io.Writer;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

/**
 * An routing subclass of AutohitLogDrain.  It assumes the will put all the
 * entries in FILES, based on the sender ID.  You must call setup() before using this.
 * It will create a new log, if it doesn't exist for the sender ID.
 * this!
 * <p>
 * The path needs to be in the form of /path/...path/filename with no extention.
 * @author Erich P. Gatejen
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - Rewrite - 28Jul03</code> 
 */
public class AutohitLogDrainRouting extends AutohitLogDrain {

	public Hashtable	routingtable;
	private String		path;

	/**
	 * The subclass uses this to set the Writer.  the Writer is the
	 * field myWriter.
	 * @param id
	 */
	public void setWriter(String id) throws Exception {
		
		if (routingtable.containsKey(id)) {
			myWriter = (Writer)routingtable.get(id);
		} else {
			FileOutputStream fios = new FileOutputStream(path + id + AutohitProperties.literal_FS_LOG_EXTENSION,true);
			myWriter = new OutputStreamWriter(fios);
			routingtable.put(id,myWriter);
		}
	}

	/**
	 * The subclass uses this to discard the Writer.  It says this id isn't
	 * being used anymore.
	 * @param id
	 */
	public void discardWriter(String id) throws Exception {
		
		if (routingtable.containsKey(id)) {
			OutputStreamWriter fios = (OutputStreamWriter)routingtable.get(id);
			try {
				fios.close();
			} catch (Exception ee) {
				// do nothing.
			}
			routingtable.remove(id);
		}
	}


	/**
	 * The subclass should implement this to do any initialization.
	 */
	public void initchain() {
		routingtable = new Hashtable();
	}
	
	/**
	 * This must be called before it is used.
	 * @param basepath points to the path and base filename of the logs.  The log ID will be appended to this to make the real filename.
	 */
	public void setup(String  basepath) {
		path = basepath;
	}
}