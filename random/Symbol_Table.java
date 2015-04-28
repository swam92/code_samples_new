package xtc.random;
import java.util.ArrayList;
import xtc.util.SymbolTable.Scope;


public class Symbol_Table {
	private ArrayList<Symbol> symbols = new ArrayList<Symbol>();
	public Scope scope;

	public Symbol_Table parent = null; // so that we can scope into other symbol tables

	public Symbol_Table(Scope scope) {
		this.scope = scope;
		this.parent = null;
	}
	
	public Symbol_Table(Scope scope, Symbol_Table parent) {
		super();
		this.scope = scope;
		this.parent = parent;
	}
	
	public void add(Symbol symbol) {
		if (this.has(symbol.name) == false) {
			symbols.add(symbol);
		}
	}
	
	public boolean has(String name) {
		for (Symbol s : symbols) {
			if (s.name.equals(name)) {
				return true;
			}
		}
		
		return false;
	}
	
	public Symbol get(String name) {
		for (Symbol s : symbols) {
			if (s.name.equals(name)) {
				return s;
			}
		}
		
		return null;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("\nCurrent symbol table contents:\n");
		for (Symbol s : symbols) {
			sb.append(s + "\n");
		}
		return sb.toString();
	}
}
