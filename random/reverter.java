package xtc.random;

import java.lang.StringBuilder;
import java.util.ArrayList;

public class reverter{
	public boolean isPolymorphic, implementer;
	public String polymorphism, parentClass;
	public String name, visibility;
	public int parentIndex;
	public ArrayList<String> usedClasses = new ArrayList<String>(0);
	public ArrayList<String> methods = new ArrayList<String>(0);
	public ArrayList<String[]> globals;
	public ArrayList<String[]> globalsHelp;
	public ArrayList<GlobalVariable> globalVars;
	public String parentString = "Object";
	public Method constructor;
	public reverter parent;
	public StringBuilder sb = new StringBuilder();
	public ArrayList<Method> methodObjects;
	public boolean hasConstructor = false; 
	public reverter(String[] keeper, ArrayList<String[]> globs, ArrayList<String[]> globsHelper){
		this.globals = globs;
		this.globalsHelp = globsHelper;
		this.name = keeper[0];
		this.visibility=keeper[1];
		if(keeper[2] != null){
			if(keeper[2].equals("true")){
				this.isPolymorphic= true;
				polymorphism = keeper[3];
			}
		} else{
			this.isPolymorphic = false;
			this.polymorphism = "null";
		}

		if(keeper[4] != null){
			if(keeper[4].equals("true")){
				this.implementer = true;
				this.parentClass = keeper[5];
			}
		}
		else{
			this.implementer=false;
			this.parentClass = "null";
		}
	}

	public String toString()
	{
		return name;
	}
}

// void appendGlobal(String vis)
// 	{
// 		String indent = "    ";
// 		for(globalVariable var: globals)
// 			{
// 				boolean match = false;
// 				if(var.visibility.equals(vis))
// 				{
// 					if(match == false)
// 					{
// 						sb.append(indent + vis + ": \n");
// 						match = true;
// 					}
// 					sb.append(var.modifier + " " + var.type + " " + var.name);
// 					if(var.value.equals(""))
// 					{
// 						sb.append(";");
// 					} else {
// 						sb.append(" = " + var.value + ";");
// 					}

// 				}
// 			}
// 	}
	