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

/**
 * Manage a command line.  It will return sections with calls to
 * get().  Each section is delimited with whitespace, though a full
 * string of tokens can be batched into a section using quotes.
 * Quotes embedded in tokens are ignored.  Unterminated quotes
 * will just take the rest of the line as the section.
 * 
 * <code>
 * EXAMPLE
 * token1<space>token2<tab>"token 3 token 3"<space><space>token4
 * </code>
 *
 * @author Erich P. Gatejen
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - Rewrite - 3Aug03 
 * 
 */
public class CommandLine {

	// internal state values
	private final static int STATE_START = 0;
	private final static int STATE_KEEP = 1;
	private final static int STATE_QUOTE = 2;
	private final static int STATE_DONE = 3;

	// Current command
	private String current;
	private int rover;
	private int length;

	/**
	 * Start on a command line.
	 * @param command the command line to process
	 */
	public void start(String command) {
		current = command;
		length = command.length();
		rover = 0;
	}

	/**
	 * Get the next section.  It will return null if there is nothing 
	 * left (or it is not start()'ed.)
	 * @return the next section or null
	 */
	public String get() {

		String result = null;
		int state = STATE_START;
		char item;

		try {
			StringBuffer buf = new StringBuffer();
			while ((rover < length) && (state != STATE_DONE)) {
				item = current.charAt(rover);
				switch (state) {

					case STATE_START :
						if (Character.isWhitespace(item)) {
							// keep eating them
						} else if (item == Constants.CHAR_DQUOTE) {
							state = STATE_QUOTE;
						} else {
							state = STATE_KEEP;
							buf.append(item);
						}
						break;

					case STATE_KEEP :
						if (Character.isWhitespace(item)) {
							// DONE!
							state = STATE_DONE;
						} else {
							buf.append(item);
						}
						break;

					case STATE_QUOTE :
						if (item == Constants.CHAR_DQUOTE) {
							state = STATE_DONE;
						} else {
							buf.append(item);
						}
						break;

					case STATE_DONE :
					default :
						// this shouldn't happen
						break;

				} // end switch
				rover++;

			} // end while

			// Take what we got!
			result = buf.toString();
			if (result.length()<=0) result = null;

		} catch (Exception e) {
			// any exception invalidates the whole thing
			current = null;
			result = null;
		}
		return result;
	}

}