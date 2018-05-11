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

import java.io.IOException;
import java.io.InputStream;

import javax.activation.DataSource;

/**
* A Universe Data Source useable by the activation framework.  This is a 
* lightweight implementation.  It is not valid until init(...) is called.
*
* @author Erich P. Gatejen
* @version 1.0
* <i>Version History</i>
* <code>EPG - New - 8Aug03</code> 
* 
*/
public class UniverseDataSource implements DataSource {

	/**
	 * Name of this universe object.
	 */
	public String name;

	/**
	 * Owning universe.
	 */
	public Universe uni;

	/**
	 *  Default Constructor.
	 */
	public UniverseDataSource() {

	}

	/**
	 *  Initializer.
	 * @param uniObj name of the universe object to source. 
	 * @param u the universe to source from.
	 */
	public void init(String uniObj, Universe u) {
		name = uniObj;
		uni = u;
	}

	/**
	 * Get the InputStream for this source.
	 * @return The input stream
	 */
	public java.io.InputStream getInputStream() throws java.io.IOException {
		InputStream result = null;
		try {
			result = uni.getStream(name);
		} catch (UniverseException e) {
			IOException ioe = new IOException(e.numeric + ":" + e.getMessage());
			ioe.initCause(e);
			throw ioe;
		}
		return result;
	}

	/**
	 * Get the OutStream for this source.
	 * This implementation doesn't support this.
	 * @return The input stream
	 */
	public java.io.OutputStream getOutputStream() throws java.io.IOException {
		throw new IOException("UniverseDataSource does not support getOutputStream().");
	}

	/**
	 * Get the content type.  LIE!  We'll always say an octet stream.
	 * @return the type
	 */
	public java.lang.String getContentType() {
		return "application/octet-stream";
	}

	/**
	 * Get the underlying object descriptor.
	 * @return the type
	 */
	public java.lang.String getName() {
		return name;
	}

}
