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
import java.io.Writer;
import autohit.common.AutohitProperties;

/**
 * formatting helpers
 *
 * @author Erich P. Gatejen
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - New - 1 Jul 03<br>
 * EPG - Added change to catch newlines as the end of the text - 30Jul03</code>
 * 
 */
public class Formatter {

	// internal state values
	private final static int STATE_FRESH = 0;
	private final static int STATE_CR = 1;
	private final static int STATE_LF = 2;
	private final static int STATE_WHITESPACE = 3;
	private final static int STATE_BUST = 4;
	private final static int ARBITRARY_ENTRY_LIMIT = 6000;

	// Line separator
	private String lineSeparator;
	
	/*
	 * Setable attributes.  Line length says how big the formatted lines
	 * should be, including headers.
	 */	
	public int lineLength = AutohitProperties.LOGS_LINE_SIZE_DEFAULT;		// default
	
	/*
	 * Setable attributes.  Line limit is the biggest any single entry can be.
	 */	
	public int lineLimit = AutohitProperties.LOGS_ARBITRARY_ENTRY_LIMIT_DEFAULT;

	/* 
	 * Constructor
	 */
	public Formatter() {
		lineSeparator = System.getProperty("line.separator");
	}

	/**
	 *  Formatting writer that accepts an initial and subsiquent prefixes.
	 * @param initialPrefix prefix for the first line
	 * @param prefix prefix for the following lines
	 * @param text the text to write
	 * @param target the target Writer
	 * @throws Exception
	 */
	public void prefixedWriter(
		String initialPrefix,
		String prefix,
		String text,
		Writer target)
		throws Exception {

		int prelength = prefix.length();
		int fieldlength = lineLength - prelength;
		int endspot = text.length();

		// trivial case
		if (endspot <= fieldlength) {
			target.write(initialPrefix + text + lineSeparator);
			target.flush();
			return;
		}

		// Bound it
		if (endspot > lineLimit)
			endspot = lineLimit;

		// Header
		target.write(initialPrefix);

		// set up work through the text
		int state = STATE_FRESH;
		
		int runstart = 0;
		int runlast = 0;
		int rover;
		boolean reset = false;
		char c;
		try {

			for (rover = 0; rover < endspot; rover++) {
				c = text.charAt(rover);
				
				// reset? 
				if (reset) {
					
					if (rover == endspot-1) {
						// this catches trailing newlines
						target.write(lineSeparator);
						reset = false;											
					} else {
						runstart = runlast = rover;
						target.write(lineSeparator);
						target.write(prefix);
						reset = false;
					}
					
				//	Did we bust?
				} else if ((rover - runstart) > fieldlength) {
					
					if (runlast != runstart) {
						
						// it's not a super big line.
						target.write(text.substring(runstart, runlast));
						target.write(lineSeparator);
						target.write(prefix);
						runstart = runlast;	
					}		

				} // end if bust
				
				switch (state) {

					case STATE_FRESH :
						if (c == Constants.CHAR_CR) {
							// New line
							target.write(text.substring(runstart, rover));
							reset = true;
							state = STATE_CR;

						} else if (c == Constants.CHAR_LF) {	
							// New line
							target.write(text.substring(runstart, rover));
							reset = true;
							state = STATE_LF;

						} else if (Character.isWhitespace(c)) {
							// New word
							runlast = rover;
						}
						break;

					case STATE_CR :

						if (c == Constants.CHAR_CR) {
							// Another return
							reset = true;
						} else if (c == Constants.CHAR_LF) {
							//rover++;
							runstart = runlast = rover+1;
						} else {
							state = STATE_FRESH;
						}
						break;

					case STATE_LF :

						if (c == Constants.CHAR_LF) {
							// Another linefeed
							reset = true;
						} else if (c == Constants.CHAR_CR) {
							//rover++;
							runstart = runlast = rover+1;
						} else {
							state = STATE_FRESH;
						}
						break;

					default :
						// SOFTWARE FAULT!
						break;
				} // end case
					
			} // end for

			// catch any dangling
			if (runlast < rover) {
				target.write(text.substring(runstart, rover));
				target.write(lineSeparator);
			}

		} catch (IndexOutOfBoundsException e) {
			reset = false;
			// ignore this one.  :^)
		} catch (Exception ex) {
			throw ex;
		}
		target.flush();
	}

}