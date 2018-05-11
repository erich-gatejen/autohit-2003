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
 * AUTOHIT wide constants
 *
 * @author Erich P. Gatejen
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - Rewrite - 9Apr03 
 * 
 */
public class Constants {

	/**
	 * Character tokens
	 */
	public static final char VARIABLE = '$';
	public static final char UNIOBJECT = '|';
	public static final char BUFFER = '{';

	public static final char SEPERATOR = '~';

	public static final String EMPTY_LEFT = "";
	public static final String ZERO = "0";
	public static final String TRUE = "true";
	public static final String FALSE ="false";
	public static final String UNKNOWN ="UNKNOWN";

	public static final String VM_GENERIC_NAME = "anon";

	public static final int NO_OWNER = -1;

	public static final int CLEAN_FREE_PCBS_THRESHOLD = 5;

	public static final String CRUDE_SEPERATOR = "\n";

	public static final char ANAMESPACE_SEPERATOR = '/';

	public static final char MATH_PLUS = '+';
	public static final char MATH_DIVIDE = '/';
	public static final char MATH_MULTIPLY = '*';
	public static final char MATH_MINUS = '-';

	/**
	 * Configuration
	 */
	public static final char CONFIG_OPEN = '{';
	public static final char CONFIG_CLOSE = '}';
	public static final char CONFIG_DIR = 'd';
	public static final char CONFIG_COMMENT = '#';
	public static final char CONFIG_CHKPNT = 'c';
	public static final char CONFIG_CONFIG_YES = 'y';
	public static final char CONFIG_CONFIG_NO = 'n';
	public static final char CONFIG_BLANK = ' ';

	public static final char CHAR_BACKSPACE = '\b';		/* \u0008: backspace BS */
	public static final char CHAR_HTAB		= '\t';		/* \u0009: horizontal tab HT */
	public static final char CHAR_LF		= '\n';		/* \u000a: linefeed LF */
	public static final char CHAR_FF		= '\f';		/* \u000c: form feed FF */
	public static final char CHAR_CR		= '\r';		/* \u000d: carriage return CR */
	public static final char CHAR_DQUOTE	= '\"';		/* \u0022: double quote " */
	public static final char CHAR_SQUOTE    = '\'';		/* \u0027: single quote ' */
	public static final char CHAR_BACKSLASH = '\\';		/* \u005c: backslash \ */


}
