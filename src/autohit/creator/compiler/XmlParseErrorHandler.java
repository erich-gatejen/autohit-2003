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
package autohit.creator.compiler;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Parse Error Handler.  It will just collect the errors in a list for use later.
 *
 * WARNING!!!  An instance of this must be registered with the parser
 * before EACH compile.
 *
 * @author Erich P. Gatejen
 * @version 1.1
 * <i>Version History</i>
 * <code>EPG - Initial - 10Apr03</code> 
 * 
 */
public class XmlParseErrorHandler implements ErrorHandler {

	/**
	 *  Keep a reference to the Xml compiler, so we can get
	 * to the logging funktions.
	 */
	public XmlCompiler xc;

	/**
	 *  Constructor.  Takes a reference to the XmlCompiler so
	 * we can get to the logging functions.  You can use the default
	 * constructor, but you better set xc or you will get undefined
	 * results.
	 * @param xcp A valid XmlCompiler
	 * @see autohit.creator.compiler.XmlCompiler
	 */
	public XmlParseErrorHandler(XmlCompiler xcp) throws Exception {
		super(); // Call constructor for HandlerBase()
		xc = xcp;
	}

	/**
	 *  Default constructor.
	 */
	public XmlParseErrorHandler() throws Exception {
		super(); // Call constructor for HandlerBase()
	}

	/**
	 *  Add an error to the error log.
	 */
	public void emitError(String text) {
		xc.runtimeError(text);
	}

	/**
	 *  Add an debug notice to the error log.
	 */
	public void emitWarning(String text) {
		xc.runtimeWarning(text);
	}

	/**
	 *  Add an warning to the error log.
	 */
	public void emitDebug(String text) {
		xc.runtimeDebug(text);
	}

	/**
	 *  Receive an error from the compiler.  Do not call this method directly.
	 *  
	 *  @throws SAXException Send back to document builder
	 */
	public void error(SAXParseException e) throws SAXException {

		String entry =
			"ParseError @line #" + e.getLineNumber() + ":" + e.getMessage();
		emitError(entry);

		//throw e;  // Do I want to can these?
	}

	/**
	 *  Receive a fatal error from the compiler.  Do not call this method directly.
	 *  
	 *  @throws SAXException Send back to document builder
	 */
	public void fatalError(SAXParseException e) throws SAXException {

		String entry =
			"FATAL ParseError @line #"
				+ e.getLineNumber()
				+ ":"
				+ e.getMessage();
		emitError(entry);

		//throw e;  // Do I want to can these?
	}

	/**
	 *  Receive a warning from the compiler.  Do not call this method directly.
	 *  
	 *  @throws SAXException Send back to document builder
	 */
	public void warning(SAXParseException e) throws SAXException {

		// trap a "!DOCTYPE" error and let the user know
		//		if (e.getMessage().indexOf("<!DOCTYPE", 0) > 0) {
		// BIG No
		//			errors.add(
		//				"Does not have a proper <!DOCTYPE>.  Add or fix it.  It should look something like <!DOCTYPE sim SYSTEM \"file:sim.dtd\">");
		//			throw e;

		//		} else {

		String entry =
			"ParseWarning @line #" + e.getLineNumber() + ":" + e.getMessage();
		emitWarning(entry);
		//		}
		// throw e;  // Keep chugging on a warning.
	}

}
