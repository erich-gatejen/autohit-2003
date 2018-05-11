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
package autohit.call.modules;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import autohit.call.CallException;
import autohit.common.Constants;

/**
 * Simple scanner module.
 *
 * start(target) start a scan of the target string
 * add(name, pattern) add a pattern to the pattern cache.
 * reset() reset cursor to start
 * find(name) return size of pattern, if found.  zero if not found.  cursor left at beginning.  Cursor does not move if match fails.
 * seek(s) return "true" if found, otherwise "false".  seek an exact string.  cursor left at beginning
 * seekinsensitive(s) return "true" if found, otherwise "false".  seek a string, without regard to case.  cursor left at beginning
 * substring(start, end-1) return string.  exception if error.
 * set(int spot) move cursor to a spot
 * get() get the cursor position
 * move(add) move the position forward by add spots
 * 
 * @author Erich P. Gatejen
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - Initial - 3Jul03 
 * 
 */
public class SimpleScannerModule extends Module {

	private final static String myNAME = "SimpleScanner";

	/**
	 * Pattern cache
	 */
	private HashMap patterncache;

	/**
	 * Current target
	 */
	private String currenttarget;

	/**
	 * Cursor location
	 */
	private int cursor;

	/**
	 * METHODS
	 */
	private final static String method_START = "start";
	private final static String method_START_1_TARGET = "target";
	private final static String method_ADDPATTERN = "add";
	private final static String method_ADDPATTERN_1_NAME = "name";
	private final static String method_ADDPATTERN_2_PATTERN = "pattern";
	private final static String method_RESET = "reset";
	private final static String method_FIND = "find";
	private final static String method_FIND_1_NAME = "name";
	private final static String method_SEEK = "seek";
	private final static String method_SEEK_1_STRING = "string";
	private final static String method_SEEK_CI = "seekinsensitive";
	private final static String method_SEEK_CI_1_STRING = "string";	
	private final static String method_CURSOR = "cursor";
	private final static String method_SUBSTRING = "substring";
	private final static String method_SUBSTRING_1_START = "start";
	private final static String method_SUBSTRING_2_END = "end";
	private final static String method_SET = "set";
	private final static String method_SET_1_SPOT = "spot";
	private final static String method_GET = "get";
	private final static String method_MOVE = "move";
	private final static String method_MOVE_1_ADD = "add";

	/**
	 * Constructor
	 */
	public SimpleScannerModule() {

	}

	// IMPLEMENTORS

	/**
	 * Execute a named method.  You must implement this method.
	 * You can call any of the helpers for data and services.
	 * The returned object better be a string (for now).
	 * @param name name of the method
	 * @see autohit.common.NOPair
	 * @throws CallException
	 */
	public Object execute_chain(String name) throws CallException {

		Object response = Constants.EMPTY_LEFT;
		String param1;
		String param2;
		Object thingie;

		if (name.equals(method_START)) {
			param1 = this.required(method_START_1_TARGET,name);
			this.start(param1);

		} else if (name.equals(method_ADDPATTERN)) {
			param1 = this.desired(method_ADDPATTERN_1_NAME,name);
			param2 = this.desired(method_ADDPATTERN_2_PATTERN,name);
			this.addpattern(param1, param2);

		} else if (name.equals(method_RESET)) {
			this.reset();

		} else if (name.equals(method_FIND)) {
			param1 = this.required(method_FIND_1_NAME,name);
			response = this.find(param1);

		} else if (name.equals(method_SEEK)) {
			param1 = this.required(method_SEEK_1_STRING,name);
			response = this.seek(param1);
			
		} else if (name.equals(method_SEEK_CI)) {
			param1 = this.required(method_SEEK_CI_1_STRING,name);
			response = this.seekinsensitive(param1);

		} else if (name.equals(method_SET)) {
			param1 = this.required(method_SET_1_SPOT,name);
			this.set(param1);

		} else if (name.equals(method_MOVE)) {
			param1 = this.required(method_MOVE_1_ADD,name);
			this.move(param1);

		} else if (name.equals(method_GET)) {
			response = this.get();

		} else if (name.equals(method_SUBSTRING)) {
			param1 = this.desired(method_SUBSTRING_1_START,name);
			param2 = this.desired(method_SUBSTRING_2_END,name);
			response = this.substring(param1, param2);

		} else {
			error("Not a provided method.  method=" + name);
			response = Constants.EMPTY_LEFT;
		}
		return response;
	}

	/**
	 * Allow the subclass a chance to initialize.  At a minium, an 
	 * implementor should create an empty method.
	 * @throws CallException
	 * @return the name
	 */
	protected String instantiation_chain() throws CallException {
		// At least reset the pattern cache
		patterncache = new HashMap();
		currenttarget = null;
		return myNAME;
	}

	/**
	 * Allow the subclass a chance to cleanup on free.  At a minium, an 
	 * implementor should create an empty method.
	 * @throws CallException
	 */
	protected void free_chain() throws CallException {
		// NOTHING AT THIS TIME
	}

	// PRIVATE IMPLEMENTATIONS

	/**
	 * Start method.  It will set the target for the scan.
	 * @param target the string we are going to scan
	 * @throws CallException
	 */
	private void start(String target) throws CallException {
		currenttarget = target;
		cursor = 0;
	}

	/**
	 * Adds a pattern to the scanner.  It uses the java regex package
	 * for compiling a pattern.
	 * @param name of the pattern
	 * @param pattern the regular expression pattern
	 * @throws CallException
	 * @see java.util.regex.Pattern
	 */
	private void addpattern(String name, String pattern) throws CallException {
		try {
			Pattern p = Pattern.compile(pattern);
			patterncache.put(name, p);
		} catch (Exception e) {
			error("Could not compile a pattern.  pattern=" + pattern);
		}
	}

	/**
	 * Resets the cursor
	 * @throws CallException
	 */
	private void reset() throws CallException {
		cursor = 0;
	}

	/**
	 * Find a pattern.   return size of pattern, if found.  zero if not found.  cursor left at beginning.  Cursor does not move if match fails.
	 * @param name name of pattern already added to the scanner.
	 * @return   size of pattern, if found.  zero if not found.
	 * @throws CallException
	 */
	private String find(String name) throws CallException {
		String result = Constants.ZERO;
		Pattern p;
		int local;

		// check cursor
		if (cursor >= currenttarget.length()) {
			log("Cursor at end.  find() ignored.");
			return result;
		}

		// check pattern cache
		if (patterncache.containsKey(name)) {
			p = (Pattern) patterncache.get(name);
		} else {
			// This is a bad one
			throw buildException(
				"Pattern for find() not added.  pattern name =" + name,
				CallException.CODE_MODULE_FAULT);
		}

		try {
			Matcher m = p.matcher(currenttarget.substring(cursor));
			if (m.find()) {
				local = m.start();
				result = Integer.toString(m.end() - local);
				cursor = cursor + local;
				debug("matched.  Cursor=" + cursor + "  result=" + result);
			}

		} catch (Exception e) {
			error("Non-fatal Exception in find().  message=" + e.getMessage());
		}
		return result;
	}

	/**
	 * seek(string) return "true" if found, otherwise "false".  seek an exact string.  cursor left at beginning
	 * if it isn't found, cursor is left at end
	 * @param s string to seek
	 * @throws CallException
	 * @return return "true" if found, otherwise "false"
	 */
	private String seek(String s) throws CallException {

		String result = Constants.FALSE;

		// check cursor
		if (cursor >= currenttarget.length()) {
			log("Cursor at end.  seek() ignored.");
			return result;
		}

		try {

			String t = currenttarget.substring(cursor);
			int idx = t.indexOf(s);
			if (idx >= 0) {
				cursor = idx + cursor; // Add to the original cursor
				result = Constants.TRUE;
				debug("Seek found.  Cursor=" + cursor);
			}

		} catch (Exception e) {
			// just fall out.  FALSE should be returned
		}
		return result;
	}

	/**
	 * seekinsensitive(string) return "true" if found, otherwise "false".  seek a string without regard to case.  cursor left at beginning
	 * if it isn't found, cursor is left at end
	 * @param s string to seek
	 * @throws CallException
	 * @return return "true" if found, otherwise "false"
	 */
	private String seekinsensitive(String s) throws CallException {

		String result = Constants.FALSE;
		
		try {
		
		    int sourceLength = currenttarget.length();
		    int compareLength = s.length();
		    int runLength = 0;
		    int proposedCursor = cursor;
		    int rovingCursor = cursor;
		    char sourceCandidate;
		    char compareCandidate;
		    while (rovingCursor < sourceLength) { 
		        
		        // Get the candidates
		        sourceCandidate = currenttarget.charAt(rovingCursor);
		        if (Character.isUpperCase(sourceCandidate))  sourceCandidate=Character.toLowerCase(sourceCandidate);
		        compareCandidate = s.charAt(runLength);
		        if (Character.isUpperCase(compareCandidate))  compareCandidate=Character.toLowerCase(compareCandidate);
		      
		        // Pop ahead one
		        rovingCursor++;

		        // Are they the same?
		        if (sourceCandidate == compareCandidate) {
		            
		            // Start a run
			        runLength++;		            
		            if (runLength==compareLength) {
		                // YAHOO!
		                cursor = proposedCursor;
		                result = Constants.TRUE;
		                break;
		            }
		            
		        } else {
		            // Nope.  Kill the run.
		            runLength = 0;
			        proposedCursor = rovingCursor;
		        }		        
		    }		    
		    
		} catch (Exception e) {
			// just fall out.  FALSE should be returned
		}
		return result; 
	}
	
	/**
	 * set(spot) move cursor to a spot
	 * if it is out of bounds, it will throw an exception
	 * @param spot spot to set the cursor as a parsable Integer
	 * @throws CallException
	 */
	private void set(String spot) throws CallException {

		try {
			int s = Integer.parseInt(spot);
			if (s > currenttarget.length()) {
				throw new Exception("Out of bounds.");
			}
			cursor = s;

		} catch (Exception e) {
			throw buildException(
				"Set failed due to " + e.getMessage(),
				CallException.CODE_MODULE_REPORTED_ERROR,
				e);
		}
	}

	/**
	 * move(add) move cursor by adding to it
	 * if it is out of bounds, it will throw an exception
	 * @param add positions to move it forward
	 * @throws CallException
	 */
	private void move(String add) throws CallException {

		try {
			int s = Integer.parseInt(add) + cursor;
			if (s > currenttarget.length()) {
				throw new Exception("Out of bounds.");
			}
			cursor = s;

		} catch (Exception e) {
			throw buildException(
				"Move failed due to " + e.getMessage(),
				CallException.CODE_MODULE_REPORTED_ERROR,
				e);
		}
	}

	/**
	 * get the cursor position
	 * @throws CallException
	 */
	private String get() throws CallException {

		return Integer.toString(cursor);
	}

	/**
	 * substring(start, end-1) return string in range specified.  exception if error.
	 * @param start start spot as a parsable integer
	 * @param end end spot as a parsable integer minus one
	 * @return the string
	 * @throws CallException
	 * @see java.util.regex.Pattern
	 */
	private String substring(String start, String end) throws CallException {

		String result = Constants.EMPTY_LEFT;

		try {
			int s = Integer.parseInt(start);
			int e = Integer.parseInt(end);
			result = currenttarget.substring(s, e);

		} catch (Exception e) {
			throw buildException(
				"Substring failed due to " + e.getMessage(),
				CallException.CODE_MODULE_REPORTED_ERROR,
				e);
		}
		return result;
	}
}
