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
package autohit.creator;

/**
 * SimLanguage interface
 *
 * @author Erich P. Gatejen
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - New - 13Jul03 
 * 
 */
public interface SimLanguage {

	// Lexical attributes
	public final static String ATTR_NAME = "name";
	public final static String ATTR_METHOD = "method";
	public final static String ATTR_VALUE = "value";
	public final static String ATTR_TYPE = "type";
	public final static String ATTR_EVALUATOR = "eval";
	public final static String ATTR_REFERENCE = "ref";
	public final static String ATTR_LEFT = "left";
	public final static String ATTR_OPERATOR = "oper";
	public final static String ATTR_OUTPUT = "output";
	public final static String ATTR_RESULT = "result";
	public final static String ATTR_COUNT = "count";
	public final static String ATTR_BUFFER = "buffer";
	public final static String ATTR_NEW = "new";
	public final static String ATTR_UID = "uid";
	public final static String ATTR_VERSIONNUMBER = "num";
	public final static String ATTR_CLEAR = "clear";
	public final static String ATTR_ITEM = "item";
	public final static String ATTR_LABEL = "label";

	//	Literals
	public final static String LITERAL_ZERO = "0";
	public final static String LITERAL_ONE = "1";

	// Operations
	public final static String IF_OPERATION = "-";
	public final static String MINUS_OPERATION = "-";
	public final static String EQ_OPERATION = "=";
	public final static char  cEQ_OPERATION = '=';

	// Operation flags
	public final static int EQ = 0;
	public final static int LT = 1;
	public final static int GT = 2;
	public final static int NOT = 3;
	
	// Operation strings
	public final static String EQ_STRING = "eq";
	public final static String LT_STRING = "lt";
	public final static String GT_STRING = "gt";
	public final static String NOT_STRING = "not";
	
	public final static int NOT_ZERO = -1;
	public final static int ZERO = 0;
	
}
