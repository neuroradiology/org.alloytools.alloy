package org.alloytools.alloy.solver.api;

import org.alloytools.alloy.module.api.TField;
import org.alloytools.alloy.module.api.TSig;

/**
 * An Alloy Solution is calculated by an {@link AlloySolver}. A solution,
 * however, can have multiple instances that each hold a unique set of values
 * that match the Alloy specification.
 */
public interface AlloyInstance {

	/**
	 * Get the values for a field
	 * 
	 * @param field
	 *            the field
	 * @return the values
	 */
	ITupleSet getField(TField field);

	/**
	 * Get all atoms in this solution instance for a specific sig in a TupleSet
	 * with arity=1.
	 * 
	 * @param sig
	 *            the sig
	 * @return the atoms with an arity=1
	 */
	ITupleSet getAtoms(TSig sig);

	/**
	 * Get the value of a variable from a function.
	 * 
	 * @param functionName the function name
	 * @param varName the variable name
	 * @return the value, could be empty
	 */
	ITupleSet getVariable(String functionName, String varName);

	/**
	 * Evaluate a command in the context of this instance. TODO what is the
	 * syntax?
	 * 
	 * @param cmd
	 *            the command to execute
	 * @return the return value
	 */
	ITupleSet eval(String cmd);

	/**
	 * The universe for this solution
	 * 
	 * @return the universe
	 */
	ITupleSet universe();

	/**
	 * The ident set for this solution
	 * 
	 * @return the ident set
	 */
	ITupleSet ident();

}
