---
title:	Alloy API Overview
---

The Alloy API is a high level API to use Alloy from Java. Its purpose is to establish an abstract API that
allows different Alloy implementations to create a 'hinge' between the different parts of Alloy so that each
part can be developed in its own way and have multiple independent implementations. 

For example, Electrumn has added a new keyword to the language and is doing work with different kinds of logic. They
have forked Alloy and therefore they have to maintain everything themselves. This API could be used to share front
end logic including visualizers. Alloy is a perfect example of an application where everything is coupled to everything
and that makes it difficult to extend. This API should alleviate this.

The API currently provides an abstraction for the the module, compiler, solvers, solutions, instances, atoms, and tuple sets. These are all
defined in a way that allows for many different implementations. It would be desirable to add an abstraction 
for the AST but this is skipped due to its current complexity.

The model of the Alloy API is split in a type part and a solver part. The primary object is the Alloy singleton 
that provides access to the implementations.

```alloy
	one sig Alloy {
		solvers		: AlloySolver,
		files		: String -> Path,
		compilers	: SourceResolver lone -> AlloyCompiler,
		defaultResolver	: one SourceResolver
	}

	fun Alloy.compiler[ sourceResolver : SourceResolver ] : AlloyCompiler { none }
	fun Alloy.compiler : AlloyCompiler  { none }
```

## Compiler

The _Alloy Compiler_ can take either a string as a source or as path. In both cases the compiler turns the source
or file into an _Alloy Module_. The only compilcation is that in many circumstances it is necessary resolve file names
in the Alloy sources to path names in the file system. This is not always the default file system. For example,
the Alloy application stores modules in its user interface to be able to edit them. It would be awkward to always
save each tab before compiling. Some modules also are embedded in Alloy. To handle these cases, a resolver
allows the controller to handle where the actual content comes from. 


```alloy
	abstract sig AlloyCompiler {}

	fun AlloyCompiler.compileSource[ source: Source ] 	: AlloyModule {none}
	fun AlloyCompiler.compile[ path : Path ] 		: AlloyModule {none}

	abstract sig SourceResolver {}
	fun SourceResolver.resolve[ path : Path ] : Source {none}

	sig Path, Source {}
```

## Module
Comping a Source returns an _Alloy Module_. An Alloy Module holds the AST. Although it would be a good
idea to expose the AST (this would enable a lot of extra applications) this was deemed too complex for now.

However, based on the AST the Alloy Module provides the _meta model_. This consists of the signatures, the fields,
and the commands.

```alloy
	sig AlloyModule {
		path	: lone Path,
		sigs	: set TSig,
		runs	: set TRun,
		checks	: set TCheck,
		warnings: set CompilerMessage,
		errors	: set CompilerMessage,
		options	: TCommand -> Id lone -> Option,
		compiler: AlloyCompiler,
		valid	: boolean
 	} {
		valid=true => no errors
	}

	sig CompilerMessage {}
```

## Commands 

Each module specifies zero or more _TCommand_. A command is either a _TRun_ or a _TCheck_. When a command 
does not specify a TRun command then the module must provide a default command.

```alloy
	abstract sig TCommand {
		name	: Id,
		scopes	: set TScope,
		expects	: Resolution,
		module_	: AlloyModule
	}

	sig TRun extends TCommand {}
	sig TCheck extends TCommand {}

	enum Resolution { UNKNOWN, SATISFIED, UNSATISFIED }

	sig CommandId {}
```

Each command defines a _scope_. A scope sets the bounds for the solution, for example, it defines the maximum 
number of Int atoms to use. In the API, all atoms (including the Int atoms) are treated identical. 

```alloy
	sig TScope {
		signature	: TSig,
		size		: Int,
		exact		: boolean
	}
```

A command also specifies an _expect_. This is intended for automatic testing, the test code can see if a command is 
expected to provide a solution or not when run.

## Options

Options can be specified in the source. An option starts with `--option`. Since this indicates a comment older Alloys
will ignore these options. The rest of the line is an assignment like:

	--option                noOverflow=false

This sets the `noOverflow` option to false.

In many cases, especially with automated testing, it is useful to be able to specify options per _command_. Options
can therefore be specified with a _selector_. This is a _globbing_ expression on the command name. For example:

	--option.noOvflw_*      noOverflow=true

This will override the earlier option and set the `noOverflow` to `true` for the any command (check or run) that has
a name that starts with `oOvflw_*`.

Clearly, options are then selective to the command. 

```alloy
	sig Id {}
	abstract sig Option {}
```


## Signatures

A signature is a set of TField, which represent the _relation_. Each field has a name and a parent TSig. 

In Alloy, each column of the relation/field is _typed_ with the types of the atoms it can hold. The type
is not a single TSig since a column can handle different sigs if appropriately defined. This is held in the
_TColumnType_. (##note it is probably more complex that is why it has its own sig.)

A field/relation can hold multiple columns. The first column is the TSig _parent_ of the field.

```alloy
	sig TSig {
		name	: Id,
		fields	: set TField,
	}

	sig TField {
		name	: Id,
		type	: seq TColumnType,
		parent	: TSig
	}

	sig TColumnType {
		sigs	: some TSig
	}
```

## Solving

The _Alloy_ object provides access to the solvers that are available. These solvers can take a command from a module
and find a _solution_. Alloy Solvers can be enumerated and requested by name. Each solver can describe itself to a user
interface that then is not required to hard code the used solvers.

A solver is completely abstracted, there is no implied semantics that it is a KodKod solver, uses SAT or whatever. 
It should only be able to provide an _Alloy Solution_ based on a command in a module. 

In the API it is possible to pass an instance that should be used as a lower bound of the solution.

```alloy
	sig AlloySolver {
		id		: Id,
		name		: String,
		description	: String,
		available	: boolean,
		solverType	: SolverType,
	}

	fun AlloySolver.newOptions : AlloyOptions {none}
	fun AlloySolver.solve[ command : TCommand, options : lone AlloyOptions, instance : lone AlloyInstance ] : AlloySolution {none}

	enum SolverType { SAT, UNSAT, SMT, OTHER }

	abstract sig AlloyOptions {}
```

## Solver Options

Solvers can have options. One of the ambitions of this API is to not create any coupling between the UI or command
line code and the actual installed solvers. For this reason, each solver can provide an _option DTO_. A DTO is
an object that only has fields, no methods. In Java, such an object is easy to reflect upon and it is not hard
to create a simple user interface that edits such an object since there is plenty of type information.

For this reason, a solver can provide a default options object. The User interface can edit this and
then provide the edited object when it needs a solution.

## Solution

An Alloy Solver provides an _Alloy Solution_. A solution is _not_ an instance. If a solution
is satisfied it can provide one or more instances. 

```alloy

	sig AlloySolution {
		solver		: AlloySolver,
		options		: AlloyOptions,
		module_		: AlloyModule,
		command		: TCommand,
		resolution	: Resolution,
		none_		: set univ,
		stream		: lone Stream	
	} {
		no none_ 
	}

	sig Stream {
		next : lone Stream,
		instance : AlloyInstance
	}

	
```

## Instance

And instance is an actual set of _atoms_ stored in a _ITuple Set_ for each field/relation. 

```alloy
	sig AlloyInstance {
		tuples		: TField one -> ITupleSet,
		atoms		: TSig -> IAtom,
		variables	: Id -> Id -> ITupleSet,
		univ_		: ITupleSet,
		iden_		: ITupleSet
	}
```

## Tuple Sets

In the end, the instance is encoded in _tuple sets_. A tuple set is a, as it states, a set of _ITuple_.  An ITuple
is an array (seq) of _IAtom_. An atom is an opaque object only used for its identity. 

```alloy
	sig ITupleSet {
		solution	: AlloySolution,
		arity		: Int,
		size		: Int,
		tuples		: set ITuple
	}
	sig ITuple {
		atoms		: seq IAtom
	}
	sig IAtom {
		name		: Id,
		index		: Int,
		solution	: AlloySolution,
		sig_		: TSig
	}
```
		
## Housekeeping	

```alloy
	enum boolean { false, true }
```
