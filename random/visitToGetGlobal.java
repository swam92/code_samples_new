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

/*
*A class to get global variables
*
*/
public class visitToGetGlobal{
	StringBuilder sb = new StringBuilder(); 
	public ArrayList<String> totalGlob = new ArrayList<String>();
	public ArrayList<String> totalGlobHelper = new ArrayList<String>();
	public GlobalVariable globalVariable = new GlobalVariable("","","",false,"");

	//Constructor
	public visitToGetGlobal(GNode node) {
		GNode n = node;
		process(n);

	}

		public void process(Node node) {
		new Visitor() {

			public String globalType, varName, globalMod1, globalValue;
			public String globalMod2 = "";
			public boolean isPrimitive, isGlobal;
			//public ArrayList<String> modifiers = new ArrayList<String>();
			StringBuilder sb;
			public int offset;

			public void visitModifiers(GNode n){
				for(int i =0; i < n.size(); i++){
					Node visibility = (Node)n.get(i);
					//modifiers.add(visibility.toString());
					totalGlob.add(visibility.get(0).toString());
					globalVariable.modifier = visibility.get(0).toString();
					System.out.println("=============+++++++++++++++++= " + visibility.get(0).toString());
				}	
			}

			public void visitType(GNode n){
				Node type = (Node)n.get(0);
				globalType = type.get(0).toString();
				System.out.println("visitType  " + globalType);
				totalGlob.add(globalType);
				totalGlobHelper.add(globalType);
				globalVariable.type = globalType;
			}

			public void visitIntegerLiteral(GNode n){
				totalGlob.add(n.get(0).toString());
				globalVariable.value = n.get(0).toString();
			}

			public void visitStringLiteral(GNode n){
				totalGlob.add(n.get(0).toString());
				globalVariable.value = n.get(0).toString();
			}

			public void visitDeclarators(GNode n){
				
				Node globalName = (Node)n.get(0);
				for(int i=0; i < globalName.size(); i++){
					if(globalName.get(i) != null){
						if(globalName.get(i) instanceof String){
							varName = globalName.get(i).toString();
							totalGlob.add(varName);
							totalGlobHelper.add(varName);
							globalVariable.name = varName;
						}
						else{
							visit(n);
						}

					}//end if not null

				}//end for
			
			}

			public void visit(Node n) {
				for (Object o : n) {
					// The scope belongs to the for loop!
					if (o instanceof Node) dispatch((Node) o);
				}
			}

		}.dispatch(node);
	}

}
