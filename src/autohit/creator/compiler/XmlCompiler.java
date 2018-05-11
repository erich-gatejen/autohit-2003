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

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import autohit.common.AutohitErrorCodes;
import autohit.common.AutohitException;
import autohit.common.AutohitLogInjectorWrapper;
import autohit.common.AutohitProperties;
import autohit.server.SystemContext;

/**
 * This is the a base XML compiler.  It must be extended by a specific compiler.  
 * Users of an extended class will call the compile() method in this class, which
 * will first parse the XML then call the abstract method build().  An extended
 * class must override the build() method and use to to compile from the 
 * xml document tree.
 * <p>
 * This will load/cache the DTD by providing a new Resolver that will 
 * return a string reader to the cached DTD.  It assumes that each execution
 * of "build" is for a new compile.
 * <p>
 * WARNING!!!  For the compiler to work, the root property must be set
 * and passed in the prop to the constructor.
 *
 * @author Erich P. Gatejen
 * @version 1.1
 * <i>Version History</i>
 * <code>EPG - Initial - 14Apr03</code> 
 * 
 */
public abstract class XmlCompiler {

	private final static int BUFFER_SIZE = 1024;

	/**
	 *  Runtime logger.  Used for compile-time logging.  Use the two
	 *  helper methods instead of using it directly--runtimeError and
	 *  runtimeWarning.  
	 * @see #runtimeError(String t)
	 * @see #runtimeWarning(String t)
	 */
	public AutohitLogInjectorWrapper runtimeLog;
	public AutohitLogInjectorWrapper myLog;
	private int warnings;
	private int errors;

	/**
	 *  Handles parse/compile errors and warnings.  Also serves as the ErrorHandler
	 *  for the XML parser.
	 *  @see autohit.creator.compiler.XmlParseErrorHandler
	 */
	public XmlParseErrorHandler myErrorHandler;

	/**
	 *  The DTD to use when parsing the source text.
	 */
	private XmlCompilerResolver myResolver;

	/**
	 *  System context
	 */
	private SystemContext sc;

	/**
	 *  XML internals
	 */
	private DocumentBuilder builder;
	private DocumentBuilderFactory factory;

	// --- PUBLIC METHODS ----------------------------------------------------	

	/**
	 *  Constructor.  You must use this and NOT the default.  
	 *  It will make sure that the DTD for the SimLanguage is available.
	 *
	 *  If you use the defaulty constructor, the compiler will not know
	 *  which DTD to use.  That is a BAD THING(tm).
	 *
	 *  @param dtdURI URI of the DTD used in the !DOCTYPE * SYSTEM clause in the
	 *                  compile targets.
	 *  @param sc  A system context containing valid references to a root logger
	 *             and the system properties.
	 *  @throws Exception any exception invalidates the compiler.
	 */
	public XmlCompiler(String dtdURI, SystemContext sc) throws Exception {

		StringBuffer tempDTD;
		String scrubbedURI;

		// See if we have a logger
		// CHEAT!!!  I'm going to seperate the logs later.
		// TODO seperate the logs
		runtimeLog = sc.getRootLogger();
		myLog = runtimeLog;

		// Find the dtd
		String location =
			sc.getPropertiesSet().getString(AutohitProperties.ROOT_PATH);
		if (location == null) {
			myLog.debug(
				"COMPILER ERROR.  Root property not set!",
				AutohitErrorCodes.CODE_COMPILE_CONFIGURATION_FAULT);
			throw new AutohitException(
				"Root property not set.",
				AutohitErrorCodes.CODE_COMPILE_CONFIGURATION_FAULT);
		}

		location = location + AutohitProperties.literal_DTD_PATH + "/";
		scrubbedURI = dtdURI.substring(dtdURI.indexOf(":") + 1);
		String dtdPath = location + scrubbedURI;
		myLog.debug(
			"XMLCompiler:dtdPath=" + dtdPath,
			AutohitErrorCodes.CODE_INFORMATIONAL_OK);

		// load the dtd
		File dtdFile = new File(dtdPath);
		InputStreamReader inFile =
			new InputStreamReader(new FileInputStream(dtdFile));
		tempDTD = new StringBuffer();
		try {
			char[] buf = new char[BUFFER_SIZE];
			int len;

			len = inFile.read(buf, 0, BUFFER_SIZE);
			while (len > 0) {
				tempDTD.append(buf, 0, len);
				len = inFile.read(buf, 0, BUFFER_SIZE);
			}
		} catch (EOFException e) {
			// Dont do anything.  this is A-OK.  For some odd reason, java.io
			// will sometimes throw an EOF instead of just returning a -1.
		} catch (Exception e) {
			throw (e);
		}

		// Set up a resolver from which the XML parser can get our DTD      
		myResolver = new XmlCompilerResolver(myLog);
		myResolver.register(scrubbedURI, tempDTD.toString());

		// Set up our Error Handler
		myErrorHandler = new XmlParseErrorHandler(this);

		// Create my builders
		factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(true);
		//factory.setNamespaceAware(true);

		builder = factory.newDocumentBuilder();
		builder.setErrorHandler(myErrorHandler);
		builder.setEntityResolver(myResolver);

		myLog.debug(
			"XMLCompiler: constructed.",
			AutohitErrorCodes.CODE_INFORMATIONAL_OK);
	}

	/**
	 *  Constructor.  Don't use the default--ever.
	 */
	public XmlCompiler() throws AutohitException {
		throw new AutohitException(
			"BAD PROGRAMMER DETECTED.  PUNISH HIM/HER SEVERELY!   DONT USE THE DEFAULT CONSTRUCTOR FOR autohit.creator.XmlCompiler.",
			AutohitErrorCodes.CODE_CATASTROPHIC_FRAMEWORK_FAULT);
	}

	/**
	 *  Compile a stream into object code.  It will abort on a major error
	 *  and return a null instead of an object.  This base class does not
	 *  specify the format of the object code.   Any compile errors or
	 *  warnings can be found in the errors field.
	 *
	 *  @param is An input stream to the text that is to be compiled.
	 *  @return a reference to the target object.
	 */
	synchronized public Object compile(InputStream is) {

		Document myDocument;
		Object objectCode = null;
		InputSource isource;

		// Any exception aborts the compile
		try {

			// Setup the log for this run		
			this.setRuntimeLog(myLog); // CHEAT

			// Parse.  This needs to be a singleton, because I can't trust the stock parser.
			synchronized (builder) {
				isource = new InputSource(is);
				isource.setSystemId("//");
				myDocument = builder.parse(isource);
			}
			myLog.debug(
				"XMLCompiler: parse successful.",
				AutohitErrorCodes.CODE_INFORMATIONAL_OK);

			objectCode = build(myDocument);

			// Reset the log
			this.resetRuntimeLog();

		} catch (SAXException sxe) {
			// None of these should happen!
			myLog.debug(
				"XMLCompiler: Unrecoverable parsing error.",
				AutohitErrorCodes.CODE_COMPILE_ABORT);
			myLog.debug(":" + sxe.getMessage());
		} catch (IOException ioe) {
			myLog.debug(
				"XMLCompiler: build failed to IOException.",
				AutohitErrorCodes.CODE_CATASTROPHIC_FRAMEWORK_FAULT);
			myLog.debug(":" + ioe.getMessage());
		} catch (Exception e) {
			myLog.debug(
				"XMLCompiler: Software Detected Fault:  Unexpected general Exception.",
				AutohitErrorCodes.CODE_SW_DETECTED_FAULT);
			myLog.debug(":" + e.getMessage());
		}
		// this will return null unless it was set at the end of the try.
		return objectCode;
	}

	/**
	 *  Posts a warning to the runtime log and increments the error count.  
	 * @param t the warning message
	 */
	public void runtimeWarning(String t) {
		runtimeLog.warning(
			"WARNING: " + t,
			AutohitErrorCodes.CODE_COMPILE_WARNING);
		warnings++;
	}

	/**
	 *  Posts a warning to the runtime log and increments the error count.  
	 * @param t the error message
	 */
	public void runtimeError(String t) {
		runtimeLog.error("ERROR: " + t, AutohitErrorCodes.CODE_COMPILE_ERROR);
		errors++;
	}

	/**
	 *  Posts a debug message to the runtime log.  
	 * @param t the debug message
	 */
	public void runtimeDebug(String t) {
		runtimeLog.debug(t, AutohitErrorCodes.CODE_INFORMATIONAL_OK);
	}

	/**
	 * Get error count.  
	 * @return number of errors
	 */
	public int numberErrors() {
		return errors;
	}

	/**
	 * Get warning count.  
	 * @return number of warnings
	 */
	public int numberWarnings() {
		return warnings;
	}

	/**
	 *  This sets the runtime log.  The runtime log will be used during
	 *  compilation.  The system (myLog) is used during overhead functions.
	 *  These can be the same log.  This method lets you point to a 
	 *  different log.   This will clear the error and warning counts.
	 * @param cl a log injector
	 */
	public void setRuntimeLog(AutohitLogInjectorWrapper cl) {
		runtimeLog = cl;
		warnings = 0;
		errors = 0;
	}

	/**
	 *  This sets the runtime log back to be the same as the system log.
	 */
	public void resetRuntimeLog() {
		runtimeLog = myLog;
	}

	/**
	 *  Abstract build method.  Override with a method that builds the object code
	 *  from the XML parse tree.
	 *
	 *  @param xd   A parsed XML document.
	 *  
	 *  @return Object reference to the object code.
	 */
	public abstract Object build(Document xd);

}
