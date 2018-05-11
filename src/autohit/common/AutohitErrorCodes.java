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
 * Standard error codes
 *
 * @author Erich P. Gatejen
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - Initial - 1May03<br>
 * EPG - Change scheme to make more sense of faults - 19jul03</code>
 * 
 */
public interface AutohitErrorCodes {

	/**
	 * Numeric lower bounderies.
	 * The top is is 9999 because the log formatters assume it will be 4 characters.
	 * However, there is no logic reason not to go beyond.
	 */	
	public final static int FLOOR_NUMERIC = 0;
	public final static int INFORMATIONAL = 100;
	public final static int WARNING = 1000;	
	public final static int ERROR = 2000;	
	public final static int FAULT = 4000;	
	public final static int PANIC = 8000;
	public final static int TOP_NUMERIC = 9999;
	
	/**
	 * EVENTS and CODES.    They share number space.
	 */
	public final static int CODE_NONE = FLOOR_NUMERIC;
	
	// 100 =<INFORMATIONAL < 1000
	public final static int CODE_DEFAULT = 100;
	public final static int CODE_DEBUGGING = 101;
	public final static int CODE_DEBUGGING_CORE = 102;	
	public final static int CODE_DEBUGGING_CALLS = 103;
	public final static int CODE_DEBUGGING_MODULES = 104;
	public final static int CODE_DEBUGGING_PUBLIC = 105;
	public final static int CODE_DEBUGGING_SERVICES = 106;
	public final static int CODE_DEBUGGING_SYSTEM = 107;
	public final static int CODE_VM_DONE = 125;
	public final static int CODE_SERVER_DONE = 130;
	public final static int CODE_INFORMATIONAL_OK = 150;
	public final static int CODE_INFORMATIONAL_OK_VERBOSE = 160;
	public final static int CODE_MODULE_REPORTED_INFO_OK = 175;
		
	// EVENTS 200-299 are good.  300-399 bad
	public final static int EVENT_COMMAND_ACCEPTED = 200;
	public final static int EVENT_COMMAND_COMPLELTED = 210;
	public final static int EVENT_COMMAND_PARTIAL_RESULTS = 220;
	public final static int EVENT_COMMAND_FINAL_RESULTS = 220;
	
	public final static int EVENT_COMMAND_REJECTED = 300;
	public final static int EVENT_COMMAND_FAILED = 350;	
	public final static int EVENT_COMMAND_FAULTED = 375;	
	public final static int EVENT_COMMAND_PANICED = 399;
	
	// USER SPACE 500-999	
	public final static int CODE_USER_INFO_FLOOR = 500;
	public final static int CODE_USER_INFO_TOP = 999;

	// 1000 =< WARNING < 2000 
	public final static int CODE_DEFAULT_WARNING = 1000;
	public final static int CODE_COMPILE_WARNING = 1010;
	public final static int CODE_VM_INSTRUCTION_WARNING = 1100;
	public final static int CODE_MODULE_REPORTED_WARNING = 1500;
	
	// 2000 =< ERROR < 4000 
	public final static int CODE_DEFAULT_ERROR = 2000;
	public final static int CODE_COMPILE_ERROR = 2010;
	public final static int CODE_COMPILE_ABORT = 2011;
	public final static int CODE_PROGRAM_ERROR = 2200;
	public final static int CODE_PROGRAM_DIVIDEBYZERO = 2201;

	public final static int CODE_SERVICE_GENERIC_ERROR = 2300;
	public final static int CODE_SYSTEM_GENERIC_ERROR = 2350;
	public final static int CODE_SYSTEM_TELLIO_BROKEN_PROTOCOL = 2360;
	
	public final static int CODE_VM_GENERIC_ERROR = 2400;  // VM
	public final static int CODE_VM_INSTRUCTION_ERROR = 2401;
	public final static int CODE_VM_INSTRUCTION_ABORT = 2402;
	public final static int CODE_VM_VARIABLE_TYPE_MISMATCH_ERROR = 2425;
	public final static int CODE_VM_PROCESSOR_ERROR = 2430;
	public final static int CODE_VM_PROCESSOR_ERROR_UNBOUNDED = 2431;	
	public final static int CODE_VM_PROCESSOR_ERROR_NOTFOUND = 2432;

	public final static int CODE_SERVER_ERROR = 2500;	
	public final static int CODE_SERVER_IO_ERROR = 2510;
	
	public final static int CODE_COMMAND_ERROR = 2700;
	public final static int CODE_COMMAND_UNKNOWN = 2701;
	
	public final static int CODE_CHANNEL_ERROR = 2800;
	public final static int CODE_CHANNEL_BAD_PRIORITY_LEVEL_ERROR = 2810;
	public final static int CODE_CHANNEL_DRAIN_REQUIRES_ID_ERROR = 2812;
	public final static int CODE_CHANNEL_ALREADY_EXISTS_ERROR = 2820;
	public final static int CODE_CHANNEL_DOESNT_EXIST_ERROR = 2822;
	public final static int CODE_CHANNEL_INJECTOR_INVALID_ERROR = 2830;
	public final static int CODE_CHANNEL_DRAIN_INVALID_ERROR = 2832;
	
	public final static int CODE_CONFIGURATION_ERROR = 3000;
	
	public final static int CODE_CALL_ERROR = 3100;
	public final static int CODE_CALL_REPORTED_ERROR = 3110;
	public final static int CODE_CALL_PROGRAM_ERROR = 3300;	
	public final static int CODE_CALL_PUBLIC_ERROR = 3300;	
	public final static int TOP_CODE_CALL_ERROR = 3499;		
	
	public final static int CODE_UNIVERSE_ERROR = 3500;
	public final static int CODE_OBJECT_DOES_NOT_EXIST = 3550;
	public final static int TOP_CODE_UNIVERSE_ERROR = 3599;	

	public final static int CODE_MODULE_REPORTED_ERROR = 3800;	
	
	// 4000 =< FAULT < 8000 
	public final static int CODE_DEFAULT_FAULT = 4000;
	public final static int CODE_SW_DETECTED_FAULT = 4100;
	public final static int CODE_COMMAND_FAULT = 4200;
	public final static int CODE_COMMAND_REGISTRY_FAULT = 4210;
	public final static int CODE_COMMAND_METHOD_NOT_SUPPORTED = 4220;
	public final static int CODE_COMPILE_CONFIGURATION_FAULT = 4500;
	
	public final static int CODE_SERVICE_GENERAL_FAULT = 4800;
	public final static int CODE_SERVICE_STARTUP_FAULT = 4810;
	public final static int CODE_SERVICE_INTENTIONAL_HALT = 4820;	

	public final static int CODE_VM_GENERAL_FAULT = 5000;
	public final static int CODE_VM_INSTRUCTION_FAULT = 5001;
	public final static int CODE_VM_ROUTINE_BREAKING_FAULT = 5002;
	public static final int CODE_VM_INVALID_INSTRUCTION_FAULT = 5010;
	public static final int CODE_VM_VARIABLE_NOT_DEFINED_FAULT = 5020;
	public static final int CODE_VM_PREPARE_FAULT = 5100;
	public static final int CODE_VM_EXEC_DOES_NOT_EXIST_FAULT = 5200;
	public static final int CODE_VM_SUBSYSTEM_FAULT = 5500;
	public static final int CODE_VM_CORE_GENERAL_FAULT = 5510;
	public static final int CODE_VM_CORE_DOESNT_EXIST_FAULT = 5512;
	public static final int CODE_VM_CORE_FAILED_STORE_FAULT = 5514;
	public static final int CODE_VM_CORE_FAILED_RETRIEVAL_FAULT = 5516;
	public static final int CODE_VM_CORE_FAILED_CONTROL_FAULT = 5518;
	public static final int CODE_VM_OBJECT_LOCKED_FAULT = 5520;
	public static final int CODE_VM_PROCESSOR_FAULT = 5600;
	public static final int CODE_VM_CALL_FAULT = 5800;
	public static final int CODE_VM_EXEC_FAULT = 5900;
	public static final int CODE_VM_INTENTIONAL_FAULT = 5998;	
	public static final int CODE_VM_SOFTWARE_DETECTED_FAULT = 5999;	
	
	public final static int CODE_STARTUP_FAULT = 6000;
	public final static int CODE_STARTUP_CONFIGURATION_FAULT = 6200;

	public final static int CODE_SERVER_FAULT = 6300;
	public final static int CODE_SERVER_BAD_CONTEXT_FAULT = 6310;
	
	public final static int CODE_CHANNEL_FAULT = 6900;
	public final static int CODE_CHANNEL_BAD_CONTROLLER_FAULT = 6902;
	public final static int CODE_CHANNEL_DRAIN_GENERAL_FAULT = 6904;

	public final static int CODE_CALL_FAULT = 7000;
	public final static int CODE_CALL_REQUIRED_PARAM_MISSING_FAULT = 7005;
	public final static int CODE_CALL_REQUIRED_PARAM_CLASSMISMATCH_FAULT = 7006;
	public final static int CODE_CALL_PERSISTNOTFOUND_FAULT = 7007;
	public final static int CODE_CALL_PERSISTMISMATCH_FAULT = 7008;
	public final static int CODE_CALL_MODULE_CANT_LOAD_FAULT = 7010;
	public final static int CODE_CALL_UNRECOVERABLE_FAULT = 7020;
	public final static int CODE_CALL_INTENTIONAL_FAULT = 7499;
	
	public final static int CODE_MODULE_FAULT = 7500;
	
	// 8000 =< PANIC < 9999 
	public final static int CODE_DEFAULT_PANIC = 8000;
	public final static int CODE_VM_PANIC = 8200;
	public final static int CODE_SERVER_PANIC = 8300;
	public final static int CODE_SERVICE_PANIC = 8400;
	public final static int CODE_STARTUP_ABORT = 8500;
	public final static int CODE_CHANNEL_PANIC = 8900;
	public final static int CODE_CHANNEL_INTERRUPTED = 8901;	
	public final static int CODE_CATASTROPHIC_FRAMEWORK_FAULT= 9000;


}
