package org.alloytools.alloy.solver.api;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.alloytools.alloy.module.api.AlloyModule;
import org.alloytools.alloy.module.api.TCommand;

/**
 * A solution is the answer of a solver. It can either be satisfied or not. If
 * it is satisfied then you can ask for the _instances_.
 * 
 */
public interface AlloySolution extends Iterable<AlloyInstance> {

	/**
	 * Return the solver that created this solution
	 * 
	 * @return the solver that created this solution
	 */
	AlloySolver getSolver();

	/**
	 * Return the solver that created this solution
	 * 
	 * @return the solver that created this solution
	 */
	AlloyOptions getOptions();

	/**
	 * Return the module that was used for this solution
	 * 
	 * @return the module that was used this solution
	 */
	AlloyModule getModule();

	/**
	 * Answer the command that was used for this solution
	 * 
	 * @return the command that was used for this solution
	 */
	TCommand getCommand();

	/**
	 * Answer if this solution is satisfied.
	 */
	boolean isSatisfied();

	/**
	 * Provides access to a root tupleset which can be used to create new
	 * tuplesets.
	 * 
	 * @return a tuple set representing none.
	 */
	ITupleSet none();

	/**
	 * Turn this solution into a stream of instances.
	 * 
	 * @return a stream of instances
	 */
	default Stream<AlloyInstance> stream() {
		return StreamSupport.stream(this.spliterator(), false);
	}
}
