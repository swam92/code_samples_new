package xtc.random;
import java.util.ArrayList;

import java.util.ArrayList;
import java.lang.StringBuilder;

import xtc.util.SymbolTable.Scope;

public class Symbol {

	public String name;
	public Type type;
	public Scope scope;
	public boolean isGlobal = false;
	
	public Symbol(String n, Type t, Scope s) {
		this.name = n;
		this.type = t;
		this.scope = s;
	}
	
	public String toString() {
		return "Symbol \"" + name + "\" of type \"" + type + "\" from scope \"" + scope.getName() + "\"";
	}
	
	public boolean equals(Symbol other) {
		return name.equals(other.name) && type.equals(other.type) && scope.getName().equals(other.scope.getName());
	}

}
