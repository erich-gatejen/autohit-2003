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

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Calendar;
import java.util.GregorianCalendar;

import autohit.common.channels.Atom;
import autohit.common.channels.ChannelException;
import autohit.common.channels.Drain;
import autohit.common.channels.Receipt;

/**
 * An abstract logging formatter for Autohit Tools.  It will try to log anything that
 * has a String in the atom.  You can route Atom.senderIDs to different
 * output streams.
 * <p>
 * The subclasses need to implement a setWriter() and initchain().
 * <p>
 * A pretty flag tells the formatter to keep the lines under
 * 80 characters, applying wrapping where possible.  TRUE means 
 * pretty.  The default is FALSE.  It will use the line.separator
 * property to put a return on the wrapped lines.
 * <p>
 * NOT STAMPED
 * <code>
 * 0123456789...............{79}
 * IINNNN:tttttttttt......t[EOL]
 *       :tttttttttt......t[EOL]
 * [  7  ][        69      ]
 * </code>
 * <p>
 * STAMPED (with default formatter) 
 * We will reserve 4 extra spacers that an overloading formatter can use.
 * <code>
 * 012345678901234567890123456789...............{79}
 * IINNNN[DD:HHMMSS]tttttttttt.................t[EOL]
 *                 ]tttttttttt.................t[EOL]
 * [ 6  ][9+3extra ][       54                  ]
 * [18] + [61] = 79
 * </code>
 * 
 * Cs0000:223600: XMLCompiler: Software Detected Fault: SAXException mad
                  e it out of the builder.
 * @author Erich P. Gatejen
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - Rewrite - 27Apr03</code> 
 * 
 */
public abstract class AutohitLogDrain implements Drain {

	private boolean prettyFlag;
	private boolean stampFlag;
	private String lineSep;
	private Calendar cachedCalendar;
	private OutputStream output;
	protected Writer myWriter;
	private Formatter formatter;

	static final String EMPTY_MESSAGE_STRING = "No text in entry.";

	/**
	 * Default constructor
	 * You must init() the drain before it is valid.
	 */
	public AutohitLogDrain() {

	}

	/**
	 * Post an item
	 * Don't care about receipts
	 * @return null -- always
	 */
	public Receipt post(Atom a) throws ChannelException {

		// TODO do I really care if AutohitLogDrain requires a senderID
		if (a.senderID == null) {
			throw new ChannelException(
				"AutohitLogDrain required senderID",
				ChannelException.CODE_CHANNEL_DRAIN_REQUIRES_ID_ERROR);
		}

		try {

			// prequalify the atom.  Print anything that has a string.
			if (!(a.thing instanceof String))
				return null;

			setWriter(a.senderID);

			if (stampFlag == true) {
				if (prettyFlag == true) {
					logStampPretty((Atom) a);
				} else {
					logStamp((Atom) a);
				}
			} else {
				if (prettyFlag == true) {
					logNoStampPretty((Atom) a);
				} else {
					logNoStamp((Atom) a);
				}
			}

		} catch (ClassCastException ce) {
			throw new ChannelException(
				"AutohitLogDrain failed.  Something sent a TYPE.Log that isn't a String.",
				AutohitErrorCodes.CODE_VM_SOFTWARE_DETECTED_FAULT);

		} catch (Exception e) {
			throw new ChannelException(
				"AutohitLogDrain failed drain post.  " + e.getMessage(),
				ChannelException.CODE_CHANNEL_FAULT,
				e);
		}
		// don't care about receipts
		return null;
	}

	/**
	 *  Log with no timestamp
	 * @param a the atom
	 */
	private void logNoStamp(Atom a) {
		String s = (String) a.thing;
		if (s.length() == 0)
			s = EMPTY_MESSAGE_STRING;

		try {

			if (s.charAt(0) == ':') {
				myWriter.write("      :");
			} else {
				myWriter.write(a.senderID);
				myWriter.write(numericFormatter(a.numeric));
				myWriter.write(":");
			}
			myWriter.write(s);
			myWriter.write(lineSep);
			myWriter.flush();

		} catch (Exception e) {
			this.dump(e);
		}
	}

	/**
	 *  Log with no timestamp
	 * @param a the atom
	 */
	private void logNoStampPretty(Atom a) {

		String s = (String) a.thing;
		if (s.length() == 0)
			s = EMPTY_MESSAGE_STRING;

		try {
			formatter.prefixedWriter(
				a.senderID + numericFormatter(a.numeric) + "]",
				"      ]",
				s,
				myWriter);
		} catch (Exception e) {
			this.dump(e);
		}

	}

	/**
	 *  Log with a timestamp with no pretty
	 * @param a the atom
	 * @throws ChannelException
	 */
	private void logStamp(Atom a) throws ChannelException {

		String s = (String) a.thing;
		if (s.length() == 0)
			s = EMPTY_MESSAGE_STRING;

		try {
			if (s.charAt(0) == ':') {
				myWriter.write("                ]");
			} else {
				myWriter.write(a.senderID);
				myWriter.write(numericFormatter(a.numeric));
				myWriter.write(this.timestampFormatter(a.stamp));
			}
			myWriter.write(s);
			myWriter.write(lineSep);
			myWriter.flush();

		} catch (Exception e) {
			this.dump(e);
		}
	}

	/**
	 *  Log with a timestamp and pretty format
	 * @param a the atom
	 */
	private void logStampPretty(Atom a) throws ChannelException {

		String s = (String) a.thing;
		if (s.length() == 0)
			s = EMPTY_MESSAGE_STRING;

		try {
			formatter.prefixedWriter(
				a.senderID
					+ numericFormatter(a.numeric)
					+ this.timestampFormatter(a.stamp),
				"                ]",
				s,
				myWriter);
		} catch (Exception e) {
			this.dump(e);
		}
	}

	/**
	 * This formats the the numeric into a four digit string.
	 * @param n numeric value
	 * @return a string representation of the numeric
	 */
	public String numericFormatter(int n) {

		StringBuffer buf = new StringBuffer();

		if (n < 0) {
			return "UNDR";
		} else if (n < 10) {
			return "000" + n;
		} else if (n < 100) {
			return "00" + n;
		} else if (n < 1000) {
			return "0" + n;
		} else if (n < 10000) {
			return Integer.toString(n);
		}
		return "OVER";
	}

	/**
	 * This formats the timestamp for stamped entries.  You can overload 
	 * this if you want a different format.
	 * This one will do [DD:HHMMSS]
	 * @param stamp timestamp in milliseconds.  This formatter assumes it is the unmodified system time.
	 * @return a string representation of the timestamp
	 */
	public String timestampFormatter(long stamp) {

		int t;
		cachedCalendar.setTimeInMillis(stamp);
		StringBuffer buf = new StringBuffer(cachedCalendar.get(Calendar.DATE));
		buf.append('[');

		t = cachedCalendar.get(Calendar.DAY_OF_MONTH);
		if (t < 10) {
			buf.append("0" + t);
		} else {
			buf.append(t);
		}
		buf.append(':');

		t = cachedCalendar.get(Calendar.HOUR_OF_DAY);
		if (t < 10) {
			buf.append("0" + t);
		} else {
			buf.append(t);
		}
		t = cachedCalendar.get(Calendar.MINUTE);
		if (t < 10) {
			buf.append("0" + t);
		} else {
			buf.append(t);
		}
		t = cachedCalendar.get(Calendar.SECOND);
		if (t < 10) {
			buf.append("0" + t);
		} else {
			buf.append(t);
		}
		buf.append(']');
		return buf.toString();
	}

	/**
	 *  Form this string into an entry.
	 * @param s string
	 * @return formed string
	 */
	protected String form(String s) {

		StringBuffer t = new StringBuffer();
		int runlen = formatter.lineLength - 10;

		if (s.charAt(0) == ':') {
			t.append("        ");
			t.append(s);
		} else if (prettyFlag) {

			int rover = 0;
			int endspot = s.length();

			try {

				while (rover < formatter.lineLimit) {
					if ((rover + runlen) >= endspot) {
						t.append(s.substring(rover, endspot));
						break;
					} else {
						t.append(s.substring(rover, rover + runlen));
						t.append(lineSep);
						rover = rover + runlen;
					}
				}

			} catch (IndexOutOfBoundsException e) {
				// ignore this one.  :^)
			}

		} else {
			t.append("--");
			t.append(" : ");
			t.append(s);
		}
		return t.toString();
	}

	/**
	 * This sets the pretty flag.  This will try and keep log lines
	 * at under 80 characters, applying wrapping where possible.
	 * TRUE means pretty.  The default is FALSE.   Not syncronized.  It 
	 * will take effect on the next log write.
	 * @param b
	 */
	public void setPrettyFlag(boolean b) {
		prettyFlag = b;
	}

	/**
	 * This sets the timestamp flag.  This will tell it to timestamp the
	 * entries.  TRUE means do it.  The default is FALSE.  Not syncronized.  It 
	 * will take effect on the next log write.
	 * @param b
	 */
	public void setTimestampFlag(boolean b) {
		stampFlag = b;
	}

	/**
	 * Set the max number characters printed per line
	 * @param limit the number of characters printed in a line
	 */
	public void setLineLimit(int limit) {
		formatter.lineLimit = limit;
	}

	
	
	/**
	 *  Dump
	 *  @param e Exception object, not an thrown exception
	 */
	private void dump(Exception e) {
		System.out.println("!!!!!!!!!LOGGING SYSTEM FAILED!!!!!!!!!!!");
		e.printStackTrace();
	}

	/**
	 * The subclass uses this to set the Writer.  the Writer is the
	 * field myWriter.
	 * @param id
	 */
	public abstract void setWriter(String id) throws Exception;

	/**
	 * The subclass uses this to discard the Writer.  It says this id isn't
	 * being used anymore.
	 * @param id
	 */
	public abstract void discardWriter(String id) throws Exception;

	/**
	 * The subclass should implement this to do any initialization.
	 */
	public abstract void initchain();

	/**
	 *  Call this to initialize.  Need to provide at least a default Stream.
	 */
	public void init(OutputStream os) throws Exception {

		output = os;
		myWriter = new OutputStreamWriter(output);

		prettyFlag = false;
		stampFlag = false;
		lineSep = System.getProperty("line.separator");

		cachedCalendar = new GregorianCalendar();

		formatter = new Formatter();
		formatter.lineLimit = AutohitProperties.LOGS_ARBITRARY_ENTRY_LIMIT_DEFAULT;
		formatter.lineLength = AutohitProperties.LOGS_LINE_SIZE_DEFAULT;
		
		// chain the initialization - don't change this!
		this.initchain();
	}

	/**
	 *  Call this to initialize.  Need to provide at least a default Stream.
	 */
	public void init(OutputStream os, int linesize) throws Exception {
		this.init(os);
		formatter.lineLength = linesize;
	}
	
}