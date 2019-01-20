package org.alloytools.alloy.classic.provider;

import java.util.Set;

import org.alloytools.alloy.module.api.AlloyModule;
import org.alloytools.alloy.module.api.TCheck;
import org.alloytools.alloy.module.api.TCommand;
import org.alloytools.alloy.module.api.TRun;
import org.alloytools.alloy.module.api.TScope;

import edu.mit.csail.sdg.ast.Command;

public class AbstractCommand implements TCommand, TRun, TCheck {
	final Command command;
	final AlloyModule	module;

	AbstractCommand(AlloyModule module, Command command) {
		this.module = module;
		this.command = command;
	}

	@Override
	public String getName() {
		return command.getName();
	}

	@Override
	public Set<TScope> getScopes() {
		return command.getScopes();
	}

	@Override
	public Expects getExpects() {
		return command.getExpects();
	}

	@Override
	public AlloyModule getModule() {
		return module;
	}

	public Command getOriginalCommand() {
		return command;
	}

}
