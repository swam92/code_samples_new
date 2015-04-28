package xtc.random;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.lang.StringBuilder;
import java.util.ArrayList;
import java.util.Iterator;

import xtc.lang.JavaFiveParser;

import xtc.parser.ParseException;
import xtc.parser.Result;

import xtc.tree.GNode;
import xtc.tree.Node;
import xtc.tree.Printer;
import xtc.tree.Visitor;

import xtc.util.SymbolTable;

/*
*
A class to represent method declarations
*
*/
public class Method {
	public String name;
	public String returnType;
	public boolean isPrimitiveReturnType;
	public String visibility = "private";
	public Variable[] parameters;
	public boolean isStatic = false;
	public ArrayList<String> usedClasses;
	public boolean isAConstructor = false; // is this method a constructor
	public String constructorLHS;
	public String constructorRHS;
	public int flag = 0;
	public reverter cv; 
	
	// save all the ast nodes incase we need them later
	private Node Block; // the block node where the code happens
	public String theBlock; // the string of the block

	SymbolTable table = Translator.table;

	/*
	Constructor
	*/
	public Method(GNode node, reverter cv) {
		//runtime.console().pln(node.toString());
		this.cv = cv;
		GNode n = node; // method declaration
		usedClasses = new ArrayList<String>();
		
		
			this.name = n.get(3).toString(); // normal method
	
		
		for (Object o : n) {
			if (o instanceof Node && o != null) {
				Node curr = (Node) o;
				switch (curr.getName()) {
					// modifiers
					case "Modifiers": {
						Node modifiers = curr;
						if (modifiers.size() > 0) {
							for (int i = 0; i < modifiers.size(); i++) {
								String modifier = ((Node) modifiers.get(i)).get(0).toString();
								switch(modifier) {
									case "public":
									case "private":
									case "protected": {
										this.visibility = modifier;
										break;
									}
									case "static": {
										this.isStatic = true;
										break;
									}
								}
							}
						}
						break;
					}
					// return type
					case "VoidType": { // method returns void
						this.returnType = "void";
						this.isPrimitiveReturnType = false;
						break;
					}
					case "Type": {
						Node type = curr;
						Node typeValue = (Node) type.get(0);
						this.returnType = typeValue.get(0).toString();
						this.isPrimitiveReturnType = (typeValue.getName() == "PrimitiveType") ? true : false;
						JavaToC translate = new JavaToC();
						break;
					}
					// parameters
					case "FormalParameters": {
						Node formalParameters = curr;
						// create the array of parameter variables
						int offset = 0; //check if main, if not, offset to add __this
						if(name.equals("main"))
						{
							this.parameters = new Variable[formalParameters.size()];
						} else
						{
							this.parameters = new Variable[formalParameters.size()+1];
							this.parameters[0] = new Variable(cv.name, "__this", null, false);
							offset = 1;
						}
						
						// get the variables from the AST
						if (formalParameters.size() > 0) {
							for (int i = 0; i < formalParameters.size(); i++) {
								Node parameter = (Node) formalParameters.get(i);
								Node type = (Node) parameter.get(1);
								Node typeValue = (Node) type.get(0);
								boolean isPrimitive = (typeValue.getName() == "PrimitiveType") ? true : false;
								String theType = typeValue.get(0).toString();
								String name = parameter.get(3).toString();
								System.out.println("THIS IS THE PARAM: " + name);
								Node dim = (Node) type.get(1);
								if (dim == null)
								{
									dim = (Node) parameter.get(4);
								}
								if (dim != null)
								{
									if(dim.getName().equals("Dimensions"))
									{
										theType = "__rt::Ptr<__rt::Array<" + theType + "> >" ;
									}	
								}						
								String value = null; // for now since we don't handle default parameter values
								// add it to the parameters array
								this.parameters[i+offset] = new Variable(theType, name, value, isPrimitive);
							}
						}
						for(Variable p : this.parameters)
						{
							System.out.println("\n\n" + p.name);
						}
						break;
					}
					// block
					case "Block": {
						System.out.println("\t\t BLOCK NAME" + name);
						
						this.Block = curr;
						
						// parse the code block
						BlockVisitor bv = new BlockVisitor(this);
						Translator.upTab(); // up tab because we're entering the block
						
						table.enter(table.freshName("block"));
					    table.mark(n);
						
						Symbol_Table st = new Symbol_Table(table.current(), Translator.curTable);
						Translator.addSymbolTable(st);
						Translator.curTable = Translator.getSymbolTable(table.current());
						
						this.theBlock = bv.stringDispatch(this.Block);
						
						table.exit();
						Translator.curTable = Translator.curTable.parent;
						
						System.out.println(this.theBlock);
						Translator.downTab(); // down tab because we're leaving the block
						
						break;
					}
				}
			}
		}
		//push usedClasses method to usedClasses cv
		for (String s: usedClasses)
		{
			System.out.println("used classed " + s);
			cv.usedClasses.add(s);
		}
	}
	
	// if parent class is null then just get the declaration else return as parent::function()...
	public String getDeclarationString(String parentClass) {
		// return type
		StringBuilder sb = new StringBuilder(this.returnType);
		// if this method belongs to a class
		if (parentClass != null) {
			sb.append(" ").append(parentClass).append("::") ;
		} else {
			sb.append(" ");
		}
		
		// name
		sb.append(this.name);
		// parameters
		sb.append("(");
		for (int i = 0; i < this.parameters.length; i++) {
			Variable parameter = this.parameters[i];
			// first parameter doesn't get space and comma before it, but the rest do
			if (i > 0) {
				sb.append(",").append(" ");
			}
			sb.append(parameter.type).append(" ").append(parameter.name);
		}
		sb.append(")");
		// main block
		sb.append(" ").append("{");
		sb.append("\n");
		// add the tab
		sb.append("\t");
		//sb.append("Block Content Goes Here...");
		sb.append(this.theBlock);
		sb.append("\n");
		sb.append("}");
		
		return sb.toString();
	}
	
	public String createMain()
	{	
		StringBuilder sb = new StringBuilder();
		sb.append("void __" + cv.name +"::main(__rt::Ptr<__rt::Array<String> > args)\n{\n");
		sb.append(theBlock + "\n}");
		return sb.toString();
	}
	
	public Node getBlock() {
		return this.Block;
	}

	// returns the CPP equivalent of this method as a string
	public String toCPPString(reverter cv) {
		this.theBlock = new String();
		// visit the block
		BlockVisitor bv = new BlockVisitor(this);
		Translator.upTab(); // up tab because we're entering the block
		
		table.enter(table.freshName("block"));
	    table.mark(getBlock());
		
		Symbol_Table st = new Symbol_Table(table.current(), Translator.curTable);
		Translator.addSymbolTable(st);
		Translator.curTable = Translator.getSymbolTable(table.current());
		
		this.theBlock = bv.stringDispatch(this.Block);
		
		table.exit();
		Translator.curTable = Translator.curTable.parent;
		
		System.out.println("BLOCK\n\n" + this.theBlock + "\n\nBLOCK");
		Translator.downTab();
	
		// visibility modifier
		if(this.name.equals("main"))
		{
			for (int i = 0; i < this.parameters.length; i++) {
				System.out.println("\tparam list: " + this.parameters[i].name);
			}
			System.out.println(createMain());
			return createMain();
		}else 
		{
			JavaToC translate = new JavaToC();
		
			StringBuilder sb = new StringBuilder();
			// return type
			if (this.isAConstructor == false) { // if not a constructor because constructors don't have return types
				sb.append(" ").append(translate.getMethodReturnType(this.returnType));
			}
			// name
			sb.append(" __").append(cv.name + "::" + this.name);
			// parameters
			sb.append("(");
				switch (this.name)
        		{
         	  	case "toString":
         	   	//sb.append(cv.name + " " + cv.name.toLowerCase());
          	 	break;
           	 	case "hashCode":
            	sb.append(cv.name + " " + cv.name.toLowerCase());
            	break;
           		case "equals":
           	 	sb.append(cv.name + " " + cv.name.toLowerCase()+ ", ");
           	 	break;
          	  	case "getClass":
          	  	sb.append(cv.name + " " + cv.name.toLowerCase() + ", ");
          	  	break;
          	  	default:
          	  	break;
      	  		}
			//need to add what happens if real static, when we come that far

			for (int i = 0; i < this.parameters.length; i++) {
				Variable parameter = this.parameters[i];
				sb.append(translate.getPointer(parameter.type)).append(" ").append(parameter.name);
				if(i != this.parameters.length-1)
				{
					sb.append(",").append(" ");
				}
				

			}
			sb.append(")");
			//sb.append(" :__vptr(&__vtable)").append("\n");
			// main block
			sb.append(" ").append("{");
			sb.append("\n");
			// tabs already added in the block
			sb.append(this.theBlock);
			sb.append("\n");
			sb.append("}");
		
			return sb.toString();
		}
	}
}
