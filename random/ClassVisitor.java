package xtc.random;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.lang.StringBuilder;
import java.util.ArrayList;

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
A class to represent Classes
array[0] - class name
array[1] - class visibility
array[2:3] - isPolymorphic, and if it is, name of superClass
array[4:5] - does it inherit?  and if it does, name of superClass
*
*/
public class ClassVisitor{
	public String visibility;
	public String name;
	public final ArrayList<String[]> globs = new ArrayList<String[]>();
	public final ArrayList<String[]> globsHelper = new ArrayList<String[]>();
	public final ArrayList<GlobalVariable> globalVariables = new ArrayList<GlobalVariable>();
	public final String[] keeper = new String[10];
	SymbolTable table = Translator.table;
	
	/** Create a new tool. */
	public ClassVisitor(GNode node) {
		//public boolean isPolymorphic, implementer;
		//public String polymorphism, parentClass;
		GNode n = node;
		name = n.get(1).toString();
		this.name = name;
		keeper[0] = name;
		Node vis = (Node)n.get(0);
		if(vis.size() > 0){
			Node temp = (Node)vis.get(0);
			visibility = temp.get(0).toString();
			keeper[1]=visibility;
		}
		else{
			visibility="";
			keeper[1]=visibility;
		}
		process(n);
	}

	public void process(Node node){
		new Visitor() {
			public StringBuilder sb;
			public String isPolymorphic = "default"; 
			public String polymorphism = "default";
			public String implementer = "default"; 
			public String parentClass = "default";
			public int index = 0;

			public void visitClassBody(GNode n) {
				GNode toPass = (GNode)n.get(0);
				if(toPass.getName().equals("FieldDeclaration")){
					visitToGetGlobal red = new visitToGetGlobal(toPass);
					String[] globArray = red.totalGlob.toArray(new String[red.totalGlob.size()]);
					String[] globArrayHelp = red.totalGlobHelper.toArray(new String[red.totalGlobHelper.size()]);
					globs.add(globArray);
					globsHelper.add(globArrayHelp);
					globalVariables.add(red.globalVariable);
					System.out.println("size is " + globsHelper.size());
					// add the global variable to the current symbol table
					/*for (int i = 0; i < globs.size(); i++) {
						if (globs.size() == 3) {
							TypePool.add(new Type(globs.get(i)[1])); // add the type to the pool of types, if it's already in there, this does nothing
							Translator.curTable.add(new Symbol(globs.get(i)[2], TypePool.get(globs.get(i)[1]), Translator.table.current()));
							System.out.println("Global variable added: " + Translator.curTable.get(globs.get(i)[2]));
						} else if (globs.size() == 2) {
							TypePool.add(new Type(globs.get(i)[0])); // add the type to the pool of types, if it's already in there, this does nothing
							Translator.curTable.add(new Symbol(globs.get(i)[1], TypePool.get(globs.get(i)[0]), Translator.table.current()));
							System.out.println("Global variable added: " + Translator.curTable.get(globs.get(i)[1]));
						}
					}*/
				}
			}

			public void visitImplementation(GNode n){
				isPolymorphic = "true";
				Node temp = (Node)n.get(0);
				Node temp2 = (Node)temp.get(0);
				polymorphism = temp2.get(0).toString();
				keeper[2] = isPolymorphic;
				keeper[3] = polymorphism;
			}	
			
			public void visitExtension(GNode n){
				//String implementer,parentClass;
				implementer = "true";
				Node temp = (Node)n.get(0);
				Node temp2 = (Node)temp.get(0);
				parentClass = temp2.get(0).toString();
				keeper[4] = implementer;
				keeper[5] = parentClass;
			}	

			public void visit(Node n) {
				for (Object o : n) {
					// The scope belongs to the for loop!
					if (o instanceof Node){
						 dispatch((Node) o);
						}
				}

			}
		}.dispatch(node);
	}

}
