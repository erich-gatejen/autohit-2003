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
package autohit.universe;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;

/**
 * Utilities for universe interaction. Universe clients are welcome to use
 * them.
 * 
 * @author Erich P. Gatejen
 * @version 1.0 <i>Version History</i><code>EPG - Rewrite - 13Sep03</code>
 */
public class UniverseUtils {

	// CONSTANTS
	private final static int BUFFER_SIZE = 1024;

	/**
	 * Read from an InputStream into a String, using a default encoding.
	 * 
	 * @param is
	 *           the InputStream
	 * @return string if successful
	 * @throws UniverseException
	 */
	public static String load2String(InputStream is) throws UniverseException {
		return load2String(is, null);
	}

	/**
	 * Read from an InputStream into a String, using a specific encoding.
	 * 
	 * @param is
	 *           the InputStream
	 * @param charSetName
	 *           is the charset to use for encoding the stream.
	 * @return string if successful
	 * @throws UniverseException
	 */
	public static String load2String(InputStream is, String charSetName) throws UniverseException {

		StringBuffer buffer = new StringBuffer();
		try {

			char[] buf = new char[BUFFER_SIZE];
			int sbuf;

			BufferedReader bis;
			if (charSetName != null) {
				bis = new BufferedReader(new InputStreamReader(is, charSetName));
			} else {
				bis = new BufferedReader(new InputStreamReader(is));
			}

			sbuf = bis.read(buf, 0, BUFFER_SIZE);
			while (sbuf > 0) {
				buffer.append(buf, 0, sbuf);
				sbuf = bis.read(buf, 0, BUFFER_SIZE);
			}
		} catch (Exception e) {
			throw new UniverseException(
				"Exception loading to String.  message=" + e.getMessage(),
				UniverseException.UE_IO_ERROR);
		} finally {
			try {
				is.close();
			} catch (Exception e) {
				// don't care;
			}
		}
		return buffer.toString();
	}

	/**
	 * Write from a String to an OutputStream, using a default encoding.
	 * 
	 * @param os
	 *           the OutputStream
	 * @param text
	 *           the string to save
	 * @throws UniverseException
	 */
	public static void saveString(OutputStream os, String text) throws UniverseException {
		saveString(os, text, null);
	}

	/**
	 * Write from a String to an OutputStream, using a specific encoding.
	 * 
	 * @param os
	 *           the OutputStream
	 * @param text
	 *           the string to save
	 * @param charSetName
	 *           is the charset to use for encoding the stream.
	 * @throws UniverseException
	 */
	public static void saveString(OutputStream os, String text, String charSetName) throws UniverseException {

		BufferedWriter bw =  null;
		try {

			if (charSetName != null) {
				bw = new BufferedWriter(new OutputStreamWriter(os, charSetName));
			} else {
				bw = new BufferedWriter(new OutputStreamWriter(os));
			}

			bw.write(text);

		} catch (Exception e) {
			throw new UniverseException(
				"Exception saving String.  message=" + e.getMessage(),
				UniverseException.UE_IO_ERROR);
		} finally {
			try {
				bw.close();
			} catch (Exception e) {
				// don't care;
			}
		}
	}

}