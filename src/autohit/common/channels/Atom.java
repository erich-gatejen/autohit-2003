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
package autohit.common.channels;

import java.io.Serializable;

/**
 * An atom
 * @author Erich P. Gatejen
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - Rewrite - 25Apr03</code> 
 * 
 */
public class Atom implements Serializable {

	final static long serialVersionUID = 1;
	
	/**
	 * Type of atom
	 */
	public int type;

	public static final int TYPE_INVALID = 0;
	public static final int TYPE_GENERIC = 1;
	public static final int TYPE_EVENT = 2;
	public static final int TYPE_LOG = 3;
	public static final int TYPE_CONTROL = 4;

	/**
	 * time stamp
	 */
	public long stamp;

	/**
	 * sender ID - optional
	 */
	public String senderID;

	/**
	 * thing
	 */
	public Object thing;

	/**
	 * priority - roughly the same as from java.util.logging.Level.  Some aliases are 
	 * provided for convenience.
	 */
	public int priority;

	public final static int P_NONE = 0;
	public final static int FLASH = 100;
	public final static int P1 = 100;
	public final static int IMMEDIATE = 200;
	public final static int P2 = 200;
	public final static int PRIORITY = 300;
	public final static int P3 = 300;
	public final static int ROUTINE = 400;
	public final static int P4 = 400;
	public final static int DEBUG = 500;
	public final static int P5 = 500;
	public final static int FLOOD = 600;
	public final static int P_ALL = 600;
	public final static int P_TOP = 1000;

	/**
	 * Numeric
	 */
	public int numeric;

	/**
	 *  Default constructor.  Default priority of ROUTINE.  Null object.
	 *  Generic type.  Timestamped.
	 */
	public Atom() {
		stamp = System.currentTimeMillis();
		type = TYPE_GENERIC;
		priority = ROUTINE;
		thing = null;
		senderID = null;
	}

	/**
	 *  Default constructor.  Sets type only.
	 * @param t the type
	 */
	public Atom(int t) {
		type = t;
		senderID = null;
	}

	/**
	 * Constructor.  Sets everything but ID and timestamps it.
	 * @param t the type
	 * @param p the priority
	 * @param n the numeric
	 * @param o the object
	 */
	public Atom(int t, int p, int n, Object o) {
		stamp = System.currentTimeMillis();
		type = t;
		priority = p;
		thing = o;
		numeric = n;
		senderID = null;
	}

	/**
	 * Slap a timestamp on it.
	 */
	public void stampit() {
		stamp = System.currentTimeMillis();
	}

}
