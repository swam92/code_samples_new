package xtc.random;

import java.io.File;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.ArrayList;
import xtc.lang.JavaExternalAnalyzer;
import xtc.lang.JavaFiveParser;
import xtc.lang.JavaEntities;
import xtc.lang.JavaAstSimplifier;

import xtc.parser.ParseException;
import xtc.parser.Result;

import xtc.tree.GNode;
import xtc.tree.Node;
import xtc.tree.Printer;
import xtc.tree.Visitor;

import xtc.util.SymbolTable;

// Random classes


/**
* A tool to process variable information.
*
* @author Chisom Mba
* @version $Revision$
*/
public class Translator extends xtc.util.Tool {

	//new inheritance code
	private ArrayList<String> classMethodOrganizer;
	private ArrayList<Method> methodClassObjectOrganizer; 
	public static ArrayList<reverter> bookKeeper = new ArrayList<reverter>();
	public static final String ANSI_RESET = "\u001B[0m";
	public static final String SEAN_COLOR = "\u001B[36m";
	public static SymbolTable table = new SymbolTable(); // to access in another class use SymbolTable table = Translator.table;
	// then get the current scope like this: SymbolTable.Scope currScope = table.current();
	public static Overloader overloads = new Overloader();
	public static ArrayList<Symbol> typeSymbols = new ArrayList<Symbol>();
	public static Symbol_Table curTable;
	public static ArrayList<Symbol_Table> symTables = new ArrayList<Symbol_Table>();
	public static ArrayList<String> reservedMethodNames = new ArrayList<String>();
	public static SymbolTable symTable;
	//end new inheritance code
	String fileName;
	private static int numTabs = 0; // the number of tabs for the current place in code
	/** Create a new tool. */
	public Translator() {
		// Nothing to do.
		reservedMethodNames.add("toString");
		reservedMethodNames.add("hashCode");
		reservedMethodNames.add("equals");
		reservedMethodNames.add("getClass");
	}

	{
		// A class-level anonymous scope.
	}

	public interface ICommand {
		public void run();
	}

	public String getName() {
		return "Static scope printer for variable information";
	}

	public File locate(String name) throws IOException {
		File file = super.locate(name);
		if (Integer.MAX_VALUE < file.length()) {
			throw new IllegalArgumentException(file + ": file too large");
		}
		return file;
	}



	public Node parse(Reader in, File file) throws IOException, ParseException {
		fileName = file.getName();
		
		JavaFiveParser parser =
			new JavaFiveParser(in, file.toString(), (int)file.length());
		Result result = parser.pCompilationUnit(0);

		new File("output/" + fileName).mkdir();

		return (Node)parser.value(result);
	}

	public void process(Node node) {

		SymbolTable tempTable = new SymbolTable();
		new JavaAstSimplifier().dispatch(node);
		new JavaExternalAnalyzer(runtime, tempTable).dispatch(node);
		symTable = tempTable;

		new Visitor() {
			ArrayList<String> fileNames = new ArrayList<String>(0);
			reverter currentCV;
			StringBuilder sb;
			String className = "";

			private int count = 0;
			boolean hasMethod = false;

			
			private void scope(Node curr, Node next) {
				if (null != next) visit(next);
			}

			public void visitCompilationUnit(GNode n) {
				table.enter(JavaEntities.fileNameToScopeName(n.getLocation().file));
				

			    table.mark(n);
			
				visit(n);
				Object class2 = new Object();
				for(reverter r: bookKeeper){
					if(!r.parentString.equals("Object"))
					{
						for(reverter possParent: bookKeeper)
						{
							
							if(possParent.name.equals(r.parentString))
							{
								r.parent = possParent;
							}
						}
					}
					if(r.hasConstructor || r.methodObjects.size() > 0 || r.globals.size() > 0)
					{
						CreateCPP printer = new CreateCPP(r, fileName);
						fileNames.add(r.name + ".cpp");
					}
				}
				try{
					createMake();
				} catch (Exception e)
				{
					System.out.println("IOException when creating make " + e);
				}
				
				
				CreateHeader header = new CreateHeader(bookKeeper, fileName);
				

				runtime.console().flush();
				
				table.setScope(table.root());

				curTable = new Symbol_Table(table.current());
				addSymbolTable(curTable);
			}
			public void visitPackageDeclaration(GNode n)
			{
				String canonicalName = null == n ? "" : (String) dispatch(n.getNode(1));	
			}
			public void visitClassDeclaration(GNode n) {
				
				// get the name of the class we just hit
				className = n.getString(1);
			    
			    // enter the scope of this class
			    table.enter(className);
			    table.mark(n);
			    
			    // get the methods for this class
				classMethodOrganizer = new ArrayList<String>();
				methodClassObjectOrganizer = new ArrayList<Method>();
				ClassVisitor temp = new ClassVisitor(n);
				reverter cv = new reverter(temp.keeper, temp.globs, temp.globsHelper);
				currentCV = cv;
				cv.globals = temp.globs;
				cv.globalsHelp = temp.globsHelper;
				cv.globalVars = temp.globalVariables;
				bookKeeper.add(cv); // add the class to our translator
				
				// determine class info
				if(cv.isPolymorphic == true || cv.implementer == true){
					int parentIndexTracker = 0;
					for(reverter g: bookKeeper){
						if(g.name.equals(cv.polymorphism)){
							cv.parentIndex = parentIndexTracker;
						}
						parentIndexTracker++;
					}
				}
				cv.methods = classMethodOrganizer;
				cv.methodObjects = methodClassObjectOrganizer;
				
				// determine the parent from the node
				String parentString = "Object";
				if (n.get(3) != null) {
					GNode extension = (GNode) n.get(3);
					GNode parentType = (GNode) extension.get(0);
					GNode qI = (GNode) parentType.get(0);
					parentString = qI.getString(0);
				}
							
				// Create the type of this class to add to our translator	
				Type theType = new Type(cv.name);
				if (parentString.equals("Object") == false) { // if this class has a parent other than Object
					// check to see if the type is in our type pool
					if (TypePool.get(parentString) != null) {
						// set the parent and then add the type into the pool
						theType.setParent(TypePool.get(parentString));
						TypePool.add(theType);
					} else {
						// just add the type
						TypePool.add(theType);
					}
				} else {
					// just add the type
					TypePool.add(theType);
				}

				hasMethod = false;
				
				// create a symbol table for the scope of this class
				Symbol_Table st = new Symbol_Table(table.current(), curTable);
				// add the symbol table to the translator's collection of tables
				addSymbolTable(st);
				// set the current symbol table to the table of the current scope by using xtc's scope
				curTable = getSymbolTable(table.current());
				
				// add the global variables for this class to the symbol table for this scope
				for (GlobalVariable gb : temp.globalVariables) {
					TypePool.add(new Type(gb.type));
					Symbol globalSymbol = new Symbol(gb.name, TypePool.get(gb.type), table.current());
					globalSymbol.isGlobal = true;
					curTable.add(globalSymbol);
				}
				
				visit(n);
					
				// exit this scope
				table.exit();
				// set the current table back to the upper scope's table (the table above in the implicit symbol tables)
				curTable = curTable.parent;
			}

		
			public void visitExtension(GNode n)
			{
				GNode type = (GNode)n.get(0);
				GNode qual = (GNode)type.get(0);
				String parent = qual.get(0).toString();
				currentCV.parentString = parent;
				if(!currentCV.usedClasses.contains(parent))
				{
					currentCV.usedClasses.add(parent);
				}

					//no need to visit this methods children

			}

			
			public void visitMethodDeclaration(GNode n) {
				count++;
				// create a method instance for this method
				Method m = new Method(n, currentCV);
				if (m.name.equals(className)) // if the method name is equal to a class name we just hit a constructor
				{
					currentCV.hasConstructor = true;
					currentCV.constructor = m;
					m.isAConstructor = true;
				}
				
				// if it's a main method
				if(m.name.equals("main"))
				{
					CreateMain main = new CreateMain(fileName);
					fileNames.add("main.cpp");
				} 
				else {
				
					// Overload control
					// As long as the method name is not in the list of our translator's reserved methods, add it to the list of overloads
					if (reservedMethodNames.contains(m.name) == false) {
						ArrayList<Type> paramList = new ArrayList<Type>();
						boolean encounteredSelf = false; // if we have encountered the __this parameter that we added in
						for (Variable p : m.parameters) {
							// add the type, if the type already exists it does nothing
							TypePool.add(new Type(p.type));
							paramList.add(TypePool.get(p.type));
						}
						
						// Just in case we don't have this method's class's type, let's add it to the Type pool
						TypePool.add(new Type(m.cv.name));
						m.name = overloads.add(m.name, paramList, TypePool.get(m.cv.name));
					}
				}
				
				String methodName = JavaEntities.methodSymbolFromAst(n);
			    table.enter(methodName);
			    table.mark(n);
				classMethodOrganizer.add(m.name);
				methodClassObjectOrganizer.add(m);
				
				// create a symbol table for this method
				Symbol_Table st = new Symbol_Table(table.current(), curTable);
				addSymbolTable(st);
				curTable = getSymbolTable(table.current());
				
				visit(n);
				
				// exit this method's scope
				table.exit();
				curTable = curTable.parent;
			}

			public void visit(Node n) {
				for (Object o : n) {
					if (o instanceof Node) dispatch((Node) o);
				}
			}
			
			void createMake() throws IOException
			{
				File fout = new File("output/"+ fileName + "/Makefile");
     			FileOutputStream fos = new FileOutputStream(fout);
     			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
     			String list = "";
     			for(String s: fileNames)
     			{
     				list = list + s + " ";
     			}
     			list = list + "java_lang.cc";;
      			bw.write("all: " + list + "\n");
      			bw.write("\t g++ -o " + fileName + " " + list);

      			bw.close();
			}
	
		}.dispatch(node);
	}
	
	// tab functionality
	public static String tab() {
		StringBuilder tabs = new StringBuilder();
		for (int i = 0; i < numTabs; i++) {
			tabs.append("\t");
		}
		return tabs.toString();
	}
	
	public static void upTab() {
		numTabs++;
	}
	
	public static void downTab() {
		numTabs--;
	}
	
	public static int getTabCount() {
		return numTabs;
	}
	
	// returns the closest matching Symbol (scope wise) to the symbol name passed
	public static Symbol getSymbol(String name, SymbolTable.Scope scope) {
		Symbol_Table tempCurTable = getSymbolTable(scope);
    	Symbol symbol = tempCurTable.get(name);

    	while (symbol == null) {
    		tempCurTable = tempCurTable.parent;
    		if (tempCurTable == null) { // we are at the highest scope in our program
    			break;
    		} else {
    			symbol = tempCurTable.get(name);
    		}
    	}
    	return symbol;
	}
	
	// returns the closest matching symbol table for the given scope
	public static Symbol_Table getSymbolTable(SymbolTable.Scope scope) {
		for (Symbol_Table st : symTables) {
			if (st.scope == scope) {
				return st;
			}
		}
		return null;
	}
	
	public static void addSymbolTable(Symbol_Table st) {
		symTables.add(st);
	}

	/**
	* Run the tool with the specified command line arguments.
	*
	* @param args The command line arguments.
	*/
	public static void main(String[] args) {
		new Translator().run(args);
	}
}
