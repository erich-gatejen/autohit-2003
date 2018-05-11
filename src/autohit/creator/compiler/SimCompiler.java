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

//import org.apache.commons.collections.ExtendedProperties;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Stack;

import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import autohit.common.AutohitErrorCodes;
import autohit.common.NOPair;
import autohit.common.NVPair;
import autohit.creator.SimLanguage;
import autohit.server.SystemContext;
import autohit.vm.VMExecutableWrapper;
import autohit.vm.i.VMICall;
import autohit.vm.i.VMIMethod;
import autohit.vm.i.VMIClear;
import autohit.vm.i.VMIEval;
import autohit.vm.i.VMIExec;
import autohit.vm.i.VMIFault;
import autohit.vm.i.VMIFetch;
import autohit.vm.i.VMIGoto;
import autohit.vm.i.VMIIf;
import autohit.vm.i.VMIJump;
import autohit.vm.i.VMILoad;
import autohit.vm.i.VMIMath;
import autohit.vm.i.VMIMerge;
import autohit.vm.i.VMINew;
import autohit.vm.i.VMINop;
import autohit.vm.i.VMIRScope;
import autohit.vm.i.VMIReduce;
import autohit.vm.i.VMIRight;
import autohit.vm.i.VMIScope;
import autohit.vm.i.VMIStore;
import autohit.vm.i.VMISubr;
import autohit.vm.i.VMIAssert;

/**
 * This is the a Sim compiler.  It will compile xml documents that conform
 * to the "%autohit.dtd.location%/sim.dtd" dtd into a Sim object.
 * <p>
 * WARNING!!!  For the compiler to work, the sim.dtd must be at the location
 * specified in the system property autohit.dtd.location.
 * <p>
 * The private methods translate Token() matches the textual tokens, as defined in the
 * dtd to numerics that are used by the rest of the compiler.  Any new tokens
 * need to be added to that method.
 * <p>
 * I can think of several more elegant approaches to making this compiler, but until the
 * XML parsers settle a bit, I'm not going to bother...  
 * <p>
 * IMPORTANT NOTE!!!!  The XML parser WITH DTD ensure instructions are
 * coded in the proper order.  ALL of the code below assumes this!!!
 * If instructions come out of order or in disallowed places 
 * (like a seek outside of a get), I GUARANTEE you'll get a runaway 
 * compiler...
 * <p>
 * Logging will be done to the autohit.creator.sim namespace.  It is set for
 * pretty and 
 *
 * @author Erich P. Gatejen
 * @version 1.1
 * <i>Version History</i>
 * <code>EPG - Initial - 14Apr03<br>
 * EPG - Update to add goto and references - 13Jul03</code> 
 * 
 */
public class SimCompiler extends XmlCompiler implements SimLanguage {

	private final static String DTD_NAME = "sim.dtd";
	private final static String MY_TYPE_OF_EXEC = "sim1";

	// We'll do subtractive compares for IF.  This might change.

	// Lexical elements and attriutes

	// parse tokens

	/**
	 *  The work-in-progress object code
	 */
	protected VMExecutableWrapper ob;

	/**
	 *  Symbol table for lookups
	 */
	protected HashMap symboltable;

	/**
	 *  Stack for fixups
	 */
	protected Stack fixupstack;

	/**
	 *  Local name - UID
	 */
	private String localname = "UNKNOWN";

	
	/**
	 *  Default Constructor.  This is the only and default constructor.
	 *
	 *  @throws Exception any exception invalidates the compiler.
	 */
	public SimCompiler() throws Exception {
		super();

	}

	/**
	 *  Constructor.  This is the only and default constructor.
	 *
	 *  @throws Exception any exception invalidates the compiler.
	 */
	public SimCompiler(SystemContext sc) throws Exception {
		super(DTD_NAME, sc);
	}

	/**
	 * Compile the xml tree into an VMExecutable object.
	 * 
	 * We will create a new log for each run, so that we can uniquely
	 * identify them.
	 *
	 * @param xd   A parsed XML document.
	 * @return a reference to the target object, in this case it will be a VMExecutableWrapper, or null if it failed.
	 * @see autohit.vm.VMExecutableWrapper
	 */
	public Object build(Document xd) {

		int idx;
		NodeList rootChildren;
		Element itemTree = null;
		Element codeTree = null;
		int numNodes;
		Node scratchNode;
		String scratchString;

		// Any exception or verification check aborts the compile
		try {

			// Ok, build our working Sim object
			ob = new VMExecutableWrapper();
			ob.create();

			// Create our symbol table and fixup stack
			symboltable = new HashMap();
			fixupstack = new Stack();

			// set defaults attributes
			ob.exec.major = 0;

			// set any default attributes
			ob.exec.type = MY_TYPE_OF_EXEC;
			ob.exec.minor = 0;
			ob.exec.output = null; // assume there is nothign to return

			// Get the root element and normalize
			Element root = (Element) xd.getDocumentElement();
			root.normalize();

			// Peal out the <info> and <code> sub-trees
			rootChildren = (NodeList) root.getChildNodes();
			numNodes = rootChildren.getLength();
			while (numNodes > 0) {
				scratchNode = rootChildren.item(numNodes - 1);
				if (scratchNode instanceof Element) {
					scratchString = scratchNode.getNodeName();
					if (scratchString.charAt(0) == 'i') {
						itemTree = (Element) scratchNode;
					} else if (scratchString.charAt(0) == 'c') {
						codeTree = (Element) scratchNode;
					}
				}
				numNodes--;
			}

			if (itemTree == null) {
				runtimeError("Missing infomation <info> block.");
			}

			if (codeTree == null) {
				runtimeError("Missing infomation <code> block.");
				throw new Exception();
			}

			// Deal with the <info> tree
			NodeList itemTreeChildren = itemTree.getChildNodes();
			for (idx = 0; idx < itemTreeChildren.getLength(); idx++) {
				scratchNode = itemTreeChildren.item(idx);

				// pull only Elements
				if (scratchNode instanceof Element) {
					processItem((Element) scratchNode);
				}

			}

			// Deal with the <code> tree
			// Basicall, I'm gonna go wtih recursion.  I don't think it should
			// get very deep.   
			try {
				processCode(codeTree);

				// Put a NOP on the end of the executable
				ob.emit(new VMINop());
				ob.clean();

				// fixup goto symbols
				ListIterator li = fixupstack.listIterator();
				VMIGoto jcandidate;
				NOPair nocandidate;
				Integer currentgoto;
				while (li.hasNext()) {
					nocandidate = (NOPair) li.next();
					if (symboltable.containsKey(nocandidate.n)) {
						jcandidate = (VMIGoto) nocandidate.o;
						currentgoto = (Integer) symboltable.get(nocandidate.n);
						jcandidate.t = currentgoto.intValue();
						runtimeDebug(
							"Fixup GOTO for label="
								+ nocandidate.n
								+ " target="
								+ jcandidate.t);
					} else {
						runtimeError(
							"Broken GOTO.  No label for "
								+ nocandidate.n
								+ ".");
					}
				}

			} catch (Exception e) {
				// an otherwise uncaught exception.  A runaway compiler...
				runtimeError("FATAL ERROR.  Runaway compilation errors.  Stopping compile.");
				ob = null;
			}

		} catch (Exception e) {
			myLog.error(
				"CRITICAL ERROR encountered.  Stopping compile of " + localname + ".  "
					+ e.toString(),
				AutohitErrorCodes.CODE_COMPILE_ERROR);
			myLog.error(e.toString());
			ob = null; // leave the objectCode as null;
		}

		// ditch data as it falls out of scope
		symboltable = null;
		fixupstack = null;

		// clean up logs
		int err = numberErrors();
		runtimeLog.error("Total errors for " + localname + " : " + err);
		runtimeLog.warning("Total errors for " + localname + " : " + numberWarnings());
		if (err > 0) {
			runtimeLog.info("COMPILE FAILED  " + localname + " DUE TO ERRORS.");
			ob = null;
		}
		return ob;
	}

	/**
	 *  Processes info section tags. 
	 */
	private void processItem(Element en) throws Exception {
		String tempText;
		String name;
		//runtimeDebug("ENTER --- " + en.getTagName());
		name = en.getTagName().toLowerCase();
		NodeList itemTreeChildren;
		int idx;
		Node sNode;

		// Parse the token and call a handler
		if (name.charAt(0) == 'n') {
			if (name.charAt(1) == 'a') {
				//NAME
				tempText = getText(en.getFirstChild());
				if (tempText == null) {
					runtimeError("Empty <name> tag.");
				} else {
					ob.exec.name = tempText;
					runtimeDebug("TAG <name> name= " + ob.exec.name);
				}
				// optional uid attribute
				if (en.hasAttribute(ATTR_UID)) {
					ob.exec.uid = en.getAttribute(ATTR_UID);
					localname = ob.exec.uid;
					runtimeDebug("TAG <name> uid= " + ob.exec.uid);
				}
			} else {
				// NOTE - optional
				tempText = getText(en.getFirstChild());
				ob.exec.note = tempText;
			}
		} else if (name.charAt(0) == 'v') {
			// VERSION
			ob.exec.major = 0;
			try {
				if (en.hasAttribute(ATTR_VERSIONNUMBER)) {
					tempText = en.getAttribute(ATTR_VERSIONNUMBER);
					ob.exec.major = Integer.parseInt(tempText);
					runtimeDebug("TAG <version> num= " + ob.exec.major);
				} else {
					runtimeError("ERROR: TAG<version> Attr num not present.");
				}
			} catch (Exception e) {
				runtimeError("ERROR: TAG<version> Could not parse value for Attr num.");
			}

		} else if (name.charAt(0) == 'i') {
			if (name.charAt(1) == 'o') {
				// IO - Recurse with it's children.
				itemTreeChildren = en.getChildNodes();
				for (idx = 0; idx < itemTreeChildren.getLength(); idx++) {
					sNode = itemTreeChildren.item(idx);
					if (sNode instanceof Element) {
						processItem((Element) sNode);
					}
				}
			} else {
				// INPUT - NOT SUPPORTED
			}

		} else if (name.charAt(0) == 'b') {
			// BUFFER - NOT SUPPORTED

		} else if (name.charAt(0) == 'o') {
			ob.exec.output = new NVPair();
			// OUTPUT - Specifies the output variable
			ob.exec.output.name = en.getAttribute(ATTR_NAME);
			if (en.hasAttribute(ATTR_TYPE)) {
				ob.exec.output.value = en.getAttribute(ATTR_TYPE);
			}

		} else {
			runtimeError(
				"Software Detected Fault: creator.compiler.SimCompiler.processItem().  The textual token ["
					+ en.getNodeName()
					+ "] should have NOT reached this code. Check the XML DTD associated with the SimCompiler.");
			throw (
				new Exception("Software Detected Fault in creator.compiler.SimCompiler.processItem()."));
		}

		//DEBUG
		//runtimeDebug("EXIT --- " + en.getTagName());
	}

	/**
	 *  Every raw element will enter here.  Bascially, it dispatches it to it's specific
	 *  handler.  It will do so for every child in the passed node. 
	 */
	private void processCode(Element en) throws Exception {

		Element child;
		String name;
		int idx;
		Node scratchNode;
		NodeList itemTreeChildren = en.getChildNodes();

		//DEBUG
		//runtimeDebug("ENTER --- " + en.getTagName());

		for (idx = 0; idx < itemTreeChildren.getLength(); idx++) {

			scratchNode = itemTreeChildren.item(idx);

			// Only process elements
			if (!(scratchNode instanceof Element))
				continue;

			// Clean it
			child = (Element) scratchNode;
			name = child.getTagName().toLowerCase();

			// Parse the token and call a handler
			// Just not enough tokens to justify a state translation
			if (name.charAt(0) == 'a') {
				handleAssert(child);
			} else if (name.charAt(0) == 'b') {
				if (name.charAt(1) == 'l') {
					handleBlock(child);
				} else {
					handleBuffer(child);
				}
			} else if (name.charAt(0) == 'c') {
				handleCall(child);
			} else if (name.charAt(0) == 'e') {
				handleExec(child);
			} else if (name.charAt(0) == 'f') {
				handleFor(child);
			} else if (name.charAt(0) == 'g') {
				handleGoto(child);
			} else if (name.charAt(0) == 'i') {
				if (name.charAt(1) == 'n') {
					handleInput(child);
				} else {
					handleIf(child);
				}
			} else if (name.charAt(0) == 'l') {
				handleLabel(child);
			} else if (name.charAt(0) == 'm') {
				if (name.charAt(1) == 'e') {
					handleMethod(child);
				} else {		
					handleMath(child);
				}
			} else if (name.charAt(0) == 's') {
				if (name.charAt(1) == 'e') {
					handleSet(child);
				} else {
					handleSubroutine(child);
				}
			} else if (name.charAt(0) == 'w') {
				handleWhile(child);
			} else if (name.charAt(0) == 'r') {
				handleReturn(child);
			} else {
				runtimeError(
					"Software Detected Fault: autohit.creator.compiler.SimCompiler().  The textual token ["
						+ en.getNodeName()
						+ "] should have NOT reached this code. Check the XML DTD associated with the SimCompiler.");
				throw (
					new Exception("Software Detected Fault in creator.compiler.SimCompiler.processItem()."));
			}
		}
		//DEBUG
		//runtimeDebug("EXIT --- " + en.getTagName());
	}

	// == ===============================================================================
	// == =                         TOKEN HANDLERS                                      =
	// == ===============================================================================

	/**
	 *  handle a block.  easy enough.  just scope it!.
	 *  MICROCODE
	 *  1- i.scope
	 *	2- ANY
	 *	3- i.rscope
	 */
	private void handleBlock(Element en) {

		//runtimeDebug("handleBlock.");

		// 1- i.scope
		this.emitScope();

		// 2- ANY
		try {
			processCode(en);

		} catch (Exception e) {
			// Stop an error unravelling here....    
		}

		// 3- i.rscope
		this.emitRScope();
	}

	/**
	 *  handle a Buffer.
	 *  MICROCODE
	 *  1- if (clear exists)	i.clear(name), ALREADY = true
	 *	2- if (eval exists)	i.eval(eval), i.merge(name) 
	 *  3-    else if (value exists) i.left(value), i.merge(name)
	 *  4-    else if (buffer exists)i.reduce(buffer), i.merge(name)
	 *  5-    else CLEAR = true			
	 *  6- if (cdata exists)	 		 i.load(cdata), i.merge(name)
	 *  7- 	  else if (CLEAR true, ALREADY false)		i.clear(name)
	 */
	private void handleBuffer(Element en) {

		String name = en.getAttribute(ATTR_NAME);
		String cdata;
		boolean clearFlag = false;
		boolean clearAlready = false;

		//runtimeDebug("handleBuffer.  name=" + name);

		// 1- if (clear exists)	i.clear(name)
		if (en.hasAttribute(ATTR_CLEAR)) {
			this.emitClear(name);
			clearAlready = true;
		}

		// 2- if (eval exists)	i.eval(eval), i.merge(name) 
		if (en.hasAttribute(ATTR_EVALUATOR)) {
			this.emitEval(en.getAttribute(ATTR_EVALUATOR));
			this.emitMerge(en.getAttribute(ATTR_NAME));

			// 3-    else if (value exists) i.load(value), i.merge(name) 	
		} else if (en.hasAttribute(ATTR_VALUE)) {
			this.emitLoad(en.getAttribute(ATTR_VALUE));
			this.emitMerge(en.getAttribute(ATTR_NAME));

			// 4- else if (buffer exists)i.reduce(buffer), i.merge(name) 
		} else if (en.hasAttribute(ATTR_BUFFER)) {
			this.emitReduce(en.getAttribute(ATTR_BUFFER));
			this.emitMerge(en.getAttribute(ATTR_NAME));

			// 5-    else 			i.clear(name), done
		} else {
			clearFlag = true;
		}

		// 6- if (cdata exists)	 i.load(cdata), i.merge(name)
		try {
			cdata = getText(en.getFirstChild());
		} catch (Exception e) {
			cdata = null;
		}

		if (cdata != null) {
			this.emitLoad(cdata);
			this.emitMerge(en.getAttribute(ATTR_NAME));

			// 7- else if (CLEAR true, ALREADY false)		i.clear(name)
		} else if ((clearFlag == true) && (clearAlready == false)) {
			this.emitClear(name);
		}
	}

	/**
	 *  handle call.
	 *  MICROCODE
	 * 1- i.scope
	 * 2- (SET)*
	 * 3- i.call(name)
	 * 4- i.rscope
	 * 5- if (result exist) i.store(result)
	 */
	private void handleCall(Element en) {

		String name = en.getAttribute(ATTR_NAME);

		runtimeDebug("handleCall.  call=" + name);

		// 1- i.scope
		this.emitScope();

		// 2- (SET)*
		try {
			// recurse into the children
			processCode(en);

			// 3- i.call(name)
			this.emitCall(name);

		} catch (Exception e) {
			//	Stop an error unravelling here.  Close the scope and move on
			runtimeError("ERROR handleCall.  Broken call." + name);
			runtimeError(e.toString());
			this.emitRScope();
			return;
		}

		// 4- i.rscope
		this.emitRScope();

		// 5- if (result exist) i.store(result)
		if (en.hasAttribute(ATTR_RESULT)) {
			this.emitStore(en.getAttribute(ATTR_RESULT));
		}
	}
	
	/**
	 *  handle call.
	 *  MICROCODE
	 * 1- i.scope
	 * 2- (SET)*
	 * 3- i.eval(name)
	 * 4- i.method(method)
	 * 5- i.rscope
	 * 6- if (result exist) i.store(result)
	 */
	private void handleMethod(Element en) {

		String name = en.getAttribute(ATTR_NAME);
		String method = en.getAttribute(ATTR_METHOD);

		runtimeDebug("handleMethod.  name=" + name + " method=" + method);

		// 1- i.scope
		this.emitScope();

		// 2- (SET)*
		try {
			// recurse into the children
			processCode(en);

			// 3- i.call(name)
			this.emitEval(name);

			// 4- i.call(name)
			this.emitMethod(method);

		} catch (Exception e) {
			//	Stop an error unravelling here.  Close the scope and move on
			runtimeError("ERROR handleMethod.  Broken call." + name);
			runtimeError(e.toString());
			this.emitRScope();
			return;
		}

		// 5- i.rscope
		this.emitRScope();

		// 5- if (result exist) i.store(result)
		if (en.hasAttribute(ATTR_RESULT)) {
			this.emitStore(en.getAttribute(ATTR_RESULT));
		}
	}

	/**
	 *  handle exec.
	 *  MICROCODE
	 * 1- i.scope
	 * 2- (INPUT)*
	 * 3- i.exec(name)
	 * 4- i.rscope
	 * 5- if (result exist) i.store(result)
	 */
	private void handleExec(Element en) {

		String name = en.getAttribute(ATTR_NAME);

		runtimeDebug("handleExec.  exec=" + name);

		// 1- i.scope
		this.emitScope();

		// 2- (INPUT)*
		try {
			// recurse into the children
			processCode(en);

			// 3- i.call(name)
			this.emitExec(name);

		} catch (Exception e) {
			//	Stop an error unravelling here.  Close the scope and move on
			runtimeError("ERROR handleExec.  Broken exec." + name);
			runtimeError(e.toString());
			this.emitRScope();
			return;
		}

		// 4- i.rscope
		this.emitRScope();

		// 5- if (result exist) i.store(result)
		if (en.hasAttribute(ATTR_RESULT)) {
			this.emitStore(en.getAttribute(ATTR_RESULT));
		}

	}

	/**
	 *  handle a FOR
	 *  MICROCODE
	 * 1-  TOP:	i.scope
	 * 2- 		if (eval exists)	i.eval(eval)
	 * 3- 			else if (value exists) i.load(value)
	 * 4-			else 			!!ERROR	
	 * 5- 		i.new(count)
	 * 6-  LOOP:	i.if(DONE)
	 * 7-  DO:	(ANY)*
	 * 8- 		i.load("1")
	 * 9- 		i.right	
	 * 10- 		i.fetch(count)
	 * 11-		i.math("-")		// LEFT RESULT will stay in LEFT
	 * 12-		i.store(count)		
	 * 13-		i.jump(LOOP)		
	 * 14- DONE:	i.rscope
	 */
	private void handleFor(Element en) {

		int loopstop;

		VMIIf myif;
		VMIJump myjump;

		//runtimeDebug("handleFor");

		// 1- i.scope
		this.emitScope();

		// 2- if (eval exists)	i.eval(eval)
		if (en.hasAttribute(ATTR_EVALUATOR)) {
			this.emitEval(en.getAttribute(ATTR_EVALUATOR));

			// 3-   else if (value exists) i.load(value) 	
		} else if (en.hasAttribute(ATTR_VALUE)) {
			this.emitLoad(en.getAttribute(ATTR_VALUE));

			// 4-	else 			!!ERROR	
		} else {
			runtimeError("ERROR Broken for.  No count value defined.");
			this.emitLoad(en.getAttribute(LITERAL_ZERO));
			// use 0 so we can finish compile
		}

		// 5- 	i.new(count)
		this.emitNew(en.getAttribute("count"));

		// 6-  LOOP:	i.if(DONE)
		loopstop = ob.nextIP(); // loop stop points to #6
		myif = new VMIIf();
		// FIXUP the IF later
		ob.emit(myif);
		runtimeDebug("EMIT(" + loopstop + ") i.if target= FIXUP LATER");

		// 7-  DO:	(ANY)*
		try {
			processCode(en);
		} catch (Exception e) {
			//	Stop an error unravelling here.  Close the scope and move on
			runtimeError("ERROR Broken FOR inner.");
			runtimeError(e.toString());
		}

		// 8-  i.load(LITERAL_ONE)
		this.emitLoad(LITERAL_ONE);

		// 9-  i.right
		this.emitRight();

		// 10-  i.fetch(count)
		this.emitFetch(en.getAttribute(ATTR_COUNT));

		// 11-	i.math("-")
		this.emitMath(MINUS_OPERATION);

		// 12-  i.store(count)
		this.emitStore(en.getAttribute(ATTR_COUNT));

		// 13-	i.jump(LOOP)
		this.emitJump(loopstop);

		// 14- DONE:	i.rscope
		// First fixup the jump
		myif.t = ob.nextIP();
		runtimeDebug("FIXUP IF=" + myif.t);
		this.emitRScope();
	}

	/**
	 *  handle a Set
	 *  MICROCODE
	 * 1- if (eval exists)			i.eval(eval)
	 * 2- 	 else if (ref exists)	i.fetch(ref)
	 * 3-    else if (value exists) i.load(value)
	 * 4-    else (buffer exists)	i.reduce(buffer)
	 * 5-    else 					!!ERROR	
	 * 6- if (new exists)			i.new(name)
	 * 7-    else					i.store(name)
	 */
	private void handleSet(Element en) {

		String name = en.getAttribute(ATTR_NAME);

		//runtimeDebug("handleSet  name=" + name);

		// 1- if (eval exists)			i.eval(eval)
		if (en.hasAttribute(ATTR_EVALUATOR)) {
			this.emitEval(en.getAttribute(ATTR_EVALUATOR));

			// 2- else if (ref exists)	i.fetch(ref)	
		} else if (en.hasAttribute(ATTR_REFERENCE)) {
			this.emitFetch(en.getAttribute(ATTR_REFERENCE));

			// 3- else if (value exists) i.load(value) 	
		} else if (en.hasAttribute(ATTR_VALUE)) {
			this.emitLoad(en.getAttribute(ATTR_VALUE));

			// 4- else (buffer exists)	i.reduce(buffer) 
		} else if (en.hasAttribute(ATTR_BUFFER)) {
			this.emitReduce(en.getAttribute(ATTR_BUFFER));

			// 5- else 					!!ERROR	
		} else {
			runtimeError("ERROR.  No source given for SET.");
			return;
		}

		// 6- if (new exists)			i.new(name)
		if (en.hasAttribute(ATTR_NEW)) {
			this.emitNew(name);

			// 7-    else		i.store(name)
		} else {
			this.emitStore(name);
		}
	}

	/**
	 *  handle a Input
	 *  MICROCODE
	 * 1- if (eval exists)			i.eval(eval)
	 * 2-    else if (value exists) i.load(value)
	 * 3-    else (buffer exists)	i.reduce(buffer)
	 * 4-    else 					!!ERROR	
	 * 5- i.new(name)
	 */
	private void handleInput(Element en) {

		String name = en.getAttribute(ATTR_NAME);

		//runtimeDebug("handleInput  name=" + name);

		// 1- if (eval exists)			i.eval(eval)
		if (en.hasAttribute(ATTR_EVALUATOR)) {
			this.emitEval(en.getAttribute(ATTR_EVALUATOR));

			// 2- else if (value exists) i.load(value) 	
		} else if (en.hasAttribute(ATTR_VALUE)) {
			this.emitLoad(en.getAttribute(ATTR_VALUE));

			// 3- else (buffer exists)	i.reduce(buffer) 
		} else if (en.hasAttribute(ATTR_BUFFER)) {
			this.emitReduce(en.getAttribute(ATTR_BUFFER));

			// 4- else 					!!ERROR	
		} else {
			runtimeError("ERROR.  No source given for INPUT.");
			return;
		}

		// 5- i.new(name)
		this.emitNew(name);
	}

	/**
	 *  handle goto
	 *  Emit the goto.  Put it in the fixup stack.
	 *  MICROCODE
	 *  1- i.goto(label)
	 */
	private void handleGoto(Element en) {

		String label = en.getAttribute(ATTR_LABEL);

		//runtimeDebug("handleGoto  label=" + label);

		// 1- i.goto(label)
		fixupstack.push(new NOPair(label, (Object) this.emitGoto(0)));
	}

	/**
	 *  handle a IF
	 * <code>MICROCODE
	 * 1-	  if (oper exists)	OP = oper
	 * 2-	     else			OP = '=' (default)
	 * 3-  	  if (eval exists)			i.eval(eval)
	 * 4-  	     else if (value exists) i.load(value)
	 * 5- 		 else 					!!ERROR	
	 * 6-  	  i.right
	 * 7-  	  i.fetch(item)
	 * 8-  	  i.math(operation)
	 * 9- 	  i.if(OUTER,OP)
	 * 10- INNER:	i.scope
	 * 11-     (ANY)*
	 * 12-    i.rscope
	 * 13- OUTER:	i.nop </code>
	 */
	private void handleIf(Element en) {

		VMIIf myif;
		int opthang = EQ;  // default is =

		//runtimeDebug("handleIf");

		// 1- if (oper exists)	OP = oper
		if (en.hasAttribute(ATTR_OPERATOR)) {
			String ops = en.getAttribute(ATTR_OPERATOR);
			if (ops.equals(EQ_STRING)) {
				opthang = EQ;
			} else if (ops.equals(LT_STRING)) {
				opthang = LT;
			} else if (ops.equals(GT_STRING)) {
				opthang = GT;
			} else if (ops.equals(NOT_STRING)) {
				opthang = NOT;
			} else {
				runtimeWarning("WARNING If has unrecognized operation.  Using default \"eq\"");
				opthang = EQ;
			}	
		} 
			//	2-   else			OP = '=' (default)
			// Implied since it is the default.

		// 3- if (eval exists)			 i.eval(eval)
		if (en.hasAttribute(ATTR_EVALUATOR)) {
			this.emitEval(en.getAttribute(ATTR_EVALUATOR));

			// 4- else if (value exists) i.load(value) 	
		} else if (en.hasAttribute(ATTR_VALUE)) {
			this.emitLoad(en.getAttribute(ATTR_VALUE));

			// 5- else !!ERROR	
		} else {
			runtimeError("ERROR Broken IF.  No right value defined defined.");
			this.emitLoad(en.getAttribute(LITERAL_ZERO));
			// use 0 so we can finish compile
		}

		// 6- i.right
		this.emitRight();

		// 7- i.fetch(item)
		this.emitFetch(en.getAttribute(ATTR_ITEM));

		// 8- i.math(operation)
		this.emitMath(IF_OPERATION);

		// 9- i.if(OUTER, OP)
		myif = new VMIIf();
		myif.operFlag = opthang;
		ob.emit(myif);
		//runtimeDebug(
		//	"EMIT(" + (ob.nextIP() - 1) + ") i.if target= FIXUP LATER");

		// 10- INNER:	i.scope
		this.emitScope();

		// 11- (ANY)*
		try {
			processCode(en);
		} catch (Exception e) {
			//	Stop an error unravelling here.  Close the scope and move on
			runtimeError("ERROR Broken IF inner.");
			runtimeError(e.toString());
		}

		// 12- i.rscope
		this.emitRScope();

		// 13- OUTER:	i.nop
		// First fixup the if.  Put a NOP for safety, so there will always be a target
		myif.t = ob.nextIP();
		runtimeDebug("FIXUP if target=" + myif.t);
		this.emitNOP();
	}

	/**
	 *  handle a ASSERT
	 * <code>MICROCODE
	 * 
	 * 1-    if (oper NOT exists)	OP = NOT
     * 2-    else			        OP = EQ
     * 3-    i.fetch(item)
	 * 4-    i.assert(OUTER,OP)
	 * 5- INNER:  i.scope
	 * 6-    (ANY)*
	 * 7-    i.rscope
	 * 8- OUTER:   i.nop
	 * </code>
	 */
	private void handleAssert(Element en) {

		VMIAssert myassert;
		int opthang = EQ;  // default is =

		//runtimeDebug("handleAssert");

		// 1- if (oper exists)	OP = oper
		if (en.hasAttribute(ATTR_OPERATOR)) {
			String ops = en.getAttribute(ATTR_OPERATOR);
			if (ops.equals(EQ_STRING)) {
				opthang = EQ;
			} else if (ops.equals(NOT_STRING)) {
				opthang = NOT;
			} else {
				runtimeWarning("WARNING If has unrecognized operation.  Using default \"eq\"");
				opthang = EQ;
			}	
		} 
		//	2-   else			OP = '=' (default)
		// Implied since it is the default.

		//  3-   i.fetch(item)
		this.emitFetch(en.getAttribute(ATTR_ITEM));

		//  4-	 i.assert(OUTER,OP)
		myassert = new VMIAssert();
		myassert.operFlag = opthang;
		ob.emit(myassert);
		//runtimeDebug(
		//	"EMIT(" + (ob.nextIP() - 1) + ") i.assert target= FIXUP LATER");

		// 5- INNER:  i.scope
		this.emitScope();

		// 6- (ANY)*
		try {
			processCode(en);
		} catch (Exception e) {
			//	Stop an error unravelling here.  Close the scope and move on
			runtimeError("ERROR Broken ASSERT inner.");
			runtimeError(e.toString());
		}

		// 7- i.rscope
		this.emitRScope();

		// 8- OUTER:   i.nop
		// First fixup the assert.  Put a NOP for safety, so there will always be a target
		myassert.t = ob.nextIP();
		runtimeDebug("FIXUP assert target=" + myassert.t);
		this.emitNOP();
	}

	/**
	 *  handle subroutine.
	 *  MICROCODE
	 * 1- i.scope
	 * 2- (SET)*
	 * 3- i.subr(name)
	 * 4- i.rscope
	 * 5- if (result exist) i.store(result)
	 */
	private void handleSubroutine(Element en) {

		String name = en.getAttribute(ATTR_NAME);

		runtimeDebug("handleSubroutine.  name=" + name);

		// 1- i.scope
		this.emitScope();

		// 2- (SET)*
		try {
			// recurse into the children
			processCode(en);

			// 3- i.call(name)
			this.emitSubr(name);

		} catch (Exception e) {
			//	Stop an error unravelling here.  Close the scope and move on
			runtimeError("ERROR handleCall.  Broken call." + name);
			runtimeError(e.toString());
			ob.emit(new VMIRScope());
			return;
		}

		// 4- i.rscope
		this.emitRScope();

		// 5- if (result exist) i.store(result)
		if (en.hasAttribute(ATTR_RESULT)) {
			this.emitStore(en.getAttribute(ATTR_RESULT));
		}
	}

	/**
	 *  handle a label
	 *  MICROCODE
	 * 1- {mark label}
	 */
	private void handleLabel(Element en) {

		String name = en.getAttribute(ATTR_NAME);

		//runtimeDebug("handleLabel.  name=" + name);

		// 1- {mark label}.  Put it in the symboltable with current IP
		// See if there is a duplicate.
		if (symboltable.containsKey(name)) {
			runtimeError("Duplicate label declared.  label=" + name);
		} else {
			symboltable.put(name, new Integer(ob.nextIP()));
		}
	}

	/**
	 *  handle a WHILE
	 *  MICROCODE
	 * 1- 	i.scope
	 * 2- DO:	i.load(value)
	 * 3- 	i.right
	 * 4-   i.fetch(name)
		 * 5- 	i.math("=")
	 * 6- 	i.if(DONE)
	 * 7-   (ANY)*
	 * 8- 	i.jump(DO)
	 * 9- DONE:	i.rscope
	 */
	private void handleWhile(Element en) {

		int loopdo;
		VMIIf myif;
		VMIJump myjump;

		//runtimeDebug("handleWhile");

		// 1- i.scope
		this.emitScope();

		// 2- DO:	i.load(value)
		loopdo = ob.nextIP(); // DO points at #2
		this.emitLoad(en.getAttribute(ATTR_VALUE));

		// 3- 	i.right
		this.emitRight();

		// 4-   i.fetch(name)
		this.emitFetch(en.getAttribute(ATTR_NAME));

		// 5- 	i.math("=")
		this.emitMath(EQ_OPERATION);

		// 6- 	i.if(DONE)
		myif = new VMIIf();
		ob.emit(myif);
		//runtimeDebug(
		//	"EMIT(" + (ob.nextIP() - 1) + ") i.if target= FIXUP LATER");

		// 7- (ANY)*
		try {
			processCode(en);
		} catch (Exception e) {
			//	Stop an error unravelling here.  Close the scope and move on
			runtimeError("ERROR Broken FOR inner.");
			runtimeError(e.toString());
		}

		// 8- 	i.jump(DO)
		myjump = new VMIJump();
		myjump.t = loopdo;
		ob.emit(myjump);
		runtimeDebug("EMIT(" + (ob.nextIP() - 1) + ") i.jump target=" + loopdo);

		// 9- DONE:	i.rscope
		// First fixup the if
		myif.t = ob.nextIP();
		this.emitRScope();
	}

	/**
	 *  handle a MATH
	 *  MICROCODE
	 * 1- if (eval exists)			i.eval(eval)
	 * 2-    else if (value exists) i.load(value)
	 * 3-    else 					!!ERROR	
	 * 4- i.right()
	 * 5- i.fetch(left)
	 * 6- i.math(oper)
	 * 7- if (output exists)		i.store(output)
	 * 8-    else 					i.store(left)
	 */
	private void handleMath(Element en) {

		//runtimeDebug("handleMath");

		// 1- if (eval exists)		i.eval(eval)
		if (en.hasAttribute(ATTR_EVALUATOR)) {
			this.emitEval(en.getAttribute(ATTR_EVALUATOR));

			// 2- else if (value exists) i.load(value) 	
		} else if (en.hasAttribute(ATTR_VALUE)) {
			this.emitLoad(en.getAttribute(ATTR_VALUE));

			// 3- else 			!!ERROR	
		} else {
			runtimeError("ERROR Broken Math.  No right value defined.");
			this.emitLoad(LITERAL_ZERO);
			// use 0 so we can finish compile
		}

		// 4- i.right
		this.emitRight();

		// 5- i.fetch(item)
		this.emitFetch(en.getAttribute(ATTR_LEFT));

		// 6- i.math(operation)
		this.emitMath(en.getAttribute(ATTR_OPERATOR));

		// 7- if (output exists)		i.store(output)
		if (en.hasAttribute(ATTR_OUTPUT)) {
			this.emitStore(en.getAttribute(ATTR_OUTPUT));

			// else 					i.store(left)
		} else {
			this.emitStore(en.getAttribute(ATTR_LEFT));
		}

	}

	/**
	 *  handle a return.  It just emits a fault
	 *  MICROCODE
	 * 1- i.fault
	 */
	private void handleReturn(Element en) {

		//runtimeDebug("handleReturn");

		// 1- i.fault
		this.emitFault();
	}

	// == ===============================================================================
	// == =                             HELPERS                                         =
	// == ===============================================================================

	/**
	 *  Valid string.
	 * 
	 *  Checks if the reference is not null and the string contains characters.
	 *
	 *  @param s the string to check.
	 *  @return true if valid, otherwise false
	 */
	private boolean isValid(String s) {
		if ((s == null) || (s.length() < 1))
			return false;
		else
			return true;
	}

	/**
	 *  Get the text out of an XML node.
	 *
	 *  @param cdn XML node.
	 *  @return the text.
	 */
	private String getText(Node cdn) {

		try {
			if ((cdn.getNodeType() == Node.TEXT_NODE)
				|| (cdn.getNodeType() != Node.CDATA_SECTION_NODE)) {
				CharacterData cdnc = (CharacterData) cdn;
				return cdnc.getData();
			}
		} catch (Exception e) {
		} // ignore.  re are returning empty anyway
		return null;
	}

	/**
	 *  Get the text out of an XML node.
	 *
	 *  @param where where it was bad.
	 *  @param which which attribute was bad.
	 *  @param tag  which tag.
	 *  @return the text.
	 */
	private void errBadAttribute(String where, String which, String tag) {
		runtimeError(
			"ERROR: Invalid '"
				+ which
				+ "' attribute for <"
				+ tag
				+ ">.  @"
				+ where);
	}

	/**
	 *  emitClear
	 */
	private void emitClear(String name) {
		VMIClear ic;
		ic = new VMIClear();
		ic.t = name;
		ob.emit(ic);
		//runtimeDebug("EMIT(" + (ob.nextIP() - 1) + ") i.clear name= " + name);
	}

	/**
	 *  emitEval
	 */
	private void emitEval(String eval) {
		VMIEval ic;
		ic = new VMIEval();
		ic.e = eval;
		ob.emit(ic);
		//runtimeDebug("EMIT(" + (ob.nextIP() - 1) + ") i.eval eval= " + eval);
	}

	/**
	 *  emitReduce
	 */
	private void emitReduce(String buffer) {
		VMIReduce ic;
		ic = new VMIReduce();
		ic.b = buffer;
		ob.emit(ic);
		//runtimeDebug(
		//	"EMIT(" + (ob.nextIP() - 1) + ") i.reduce name= " + buffer);
	}

	/**
	 *  emitReduce
	 */
	private void emitLoad(String value) {
		VMILoad ic;
		ic = new VMILoad();
		ic.l = value;
		ob.emit(ic);
		//runtimeDebug("EMIT(" + (ob.nextIP() - 1) + ") i.load value= " + value);
	}

	/**
	 *  emitMerge
	 */
	private void emitMerge(String buffer) {
		VMIMerge ic;
		ic = new VMIMerge();
		ic.b = buffer;
		ob.emit(ic);
		//runtimeDebug(
		//	"EMIT(" + (ob.nextIP() - 1) + ") i.merge buffer= " + buffer);
	}

	/**
	 *  emitCall
	 */
	private void emitCall(String target) {
		VMICall ic;
		ic = new VMICall();
		ic.t = target;
		ob.emit(ic);
		//runtimeDebug(
		//	"EMIT(" + (ob.nextIP() - 1) + ") i.call target= " + target);
	}

	/**
	 *  emitMethod
	 */
	private void emitMethod(String m) {
		VMIMethod ic;
		ic = new VMIMethod();
		ic.m = m;
		ob.emit(ic);
		//runtimeDebug(
		//	"EMIT(" + (ob.nextIP() - 1) + ") i.method method=" + m);
	}

	/**
	 *  emitSubr
	 */
	private void emitSubr(String target) {
		VMISubr ic;
		ic = new VMISubr();
		ic.t = target;
		ob.emit(ic);
		//runtimeDebug(
		//	"EMIT(" + (ob.nextIP() - 1) + ") i.subr target= " + target);
	}

	/**
	 *  emitExec
	 */
	private void emitExec(String target) {
		VMIExec ic;
		ic = new VMIExec();
		ic.c = target;
		ob.emit(ic);
		//runtimeDebug(
		//	"EMIT(" + (ob.nextIP() - 1) + ") i.exec target= " + target);
	}

	/**
	 *  emitNew
	 */
	private void emitNew(String var) {
		VMINew ic;
		ic = new VMINew();
		ic.v = var;
		ob.emit(ic);
		//runtimeDebug("EMIT(" + (ob.nextIP() - 1) + ") i.new variable= " + var);
	}

	/**
	 *  emitRight
	 */
	private void emitRight() {
		ob.emit(new VMIRight());
		//runtimeDebug("EMIT(" + (ob.nextIP() - 1) + ") i.right");
	}

	/**
	 *  emitFetch
	 */
	private void emitFetch(String var) {
		VMIFetch ic;
		ic = new VMIFetch();
		ic.v = var;
		ob.emit(ic);
		//runtimeDebug(
		//	"EMIT(" + (ob.nextIP() - 1) + ") i.fetch variable= " + var);
	}

	/**
	 *  emitMath
	 */
	private void emitMath(String oper) {
		VMIMath ic;
		ic = new VMIMath();
		ic.o = oper;
		ob.emit(ic);
		//runtimeDebug(
		//	"EMIT(" + (ob.nextIP() - 1) + ") i.math operation= " + oper);
	}

	/**
	 *  emitStore
	 */
	private void emitStore(String var) {
		VMIStore ic;
		ic = new VMIStore();
		ic.v = var;
		ob.emit(ic);
		//runtimeDebug(
		//	"EMIT(" + (ob.nextIP() - 1) + ") i.store variable= " + var);
	}

	/**
	 *  emitJump
	 */
	private VMIJump emitJump(int target) {
		VMIJump ic;
		ic = new VMIJump();
		ic.t = target;
		ob.emit(ic);
		//runtimeDebug(
		//	"EMIT(" + (ob.nextIP() - 1) + ") i.Jump target= " + target);
		return ic;
	}

	/**
	 *  emitGoto
	 */
	private VMIGoto emitGoto(int target) {
		VMIGoto ic;
		ic = new VMIGoto();
		ic.t = target;
		ob.emit(ic);
		//runtimeDebug(
		//	"EMIT(" + (ob.nextIP() - 1) + ") i.goto target= " + target);
		return ic;
	}

	/**
	 *  emitScope
	 */
	private void emitScope() {
		ob.emit(new VMIScope());
		//runtimeDebug("EMIT(" + (ob.nextIP() - 1) + ") i.scope");
	}

	/**
	 *  emitRScope
	 */
	private void emitRScope() {
		ob.emit(new VMIRScope());
		//runtimeDebug("EMIT(" + (ob.nextIP() - 1) + ") i.rscope");
	}

	/**
	 *  emitNOP
	 */
	private void emitNOP() {
		ob.emit(new VMINop());
		//runtimeDebug("EMIT(" + (ob.nextIP() - 1) + ") i.nop");
	}

	/**
	 *  emitNOP
	 */
	private void emitFault() {
		ob.emit(new VMIFault());
		//runtimeDebug("EMIT(" + (ob.nextIP() - 1) + ") i.fault");
	}

}