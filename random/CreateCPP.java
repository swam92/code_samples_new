package xtc.random; 
import java.util.ArrayList;

import java.io.File;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.lang.StringBuilder;

public class CreateCPP
{
	reverter cv;
	StringBuilder sb = new StringBuilder(); 
	String directoryName = "";
	public CreateCPP(reverter cv, String directoryName)
	{
		this.cv = cv; 
		this.directoryName = directoryName + "/";
		addClassHeader();
		addCCode();
		addInit();
		addMethods();
		addBasicConstructor();
		try {
				toFile();
				}
				catch (Exception e) {
					System.out.println("IO Error creating file " + cv.name + ".cpp :" +e );
				}	
	}

	void addClassHeader()
	{
		sb.append("#include \"" + cv.name + ".h\" \n");
	}
	void addCCode()
	{
		/*
		for(String[] g: cv.globals){
			for(int i =0; i < g.length; i++){
				if(i == (g.length - 1)){
					sb.append("= ");
				}
				sb.append(g[i] + " ");
			}
			sb.append(";\n");
		}*/


		String isaClass= "Class __"+ cv.name + "::__class() {\n \tstatic Class k =\n\tnew java::lang::__Class";
		isaClass = isaClass + "(__rt::literal(\""+ cv.name +"\"), java::lang::__Object::__class());\n\treturn k;\n}\n";
		//change from object to parent later
		String parent = cv.parentString;
		JavaToC j2C = new JavaToC();
		sb.append("Class __"+ cv.name + "::__class() {\n \tstatic Class k =\n\tnew java::lang::__Class");
		sb.append("(__rt::literal(\""+ cv.name +"\"), ");
		sb.append(j2C.getClass(parent));
		sb.append("::__class());\n\treturn k;\n}\n");
		sb.append("\n\n__" + cv.name + "_VT __" + cv.name + "::__vtable;");
	}
	void addMethods()
	{
		for(Method m: cv.methodObjects)
		{
			if(!m.isAConstructor){
			sb.append("\n" + m.toCPPString(cv) + "\n");
			}
		}
	}

	void addInit()
	{
		JavaToC j2C = new JavaToC();
		sb.append("\n\n");
		sb.append(cv.name).append(" __");
		sb.append(cv.name).append("::init(");
			//hey sam, what if theirs 2 constructors? 
			//but i wouldn't worry about it unless their is a relavant test case for it sdr
		if(cv.hasConstructor == true){
			Method test = cv.constructor;
			for(int i=0; i<test.parameters.length; i++){
				if(test.parameters[i].type.equals("byte")){
				String charConvert = j2C.translate(test.parameters[i].type);
				sb.append(charConvert+" ").append(test.parameters[i].name);	
				}
				else{				
				sb.append(test.parameters[i].type+" ").append(test.parameters[i].name);	
				}
				if(i != test.parameters.length -1 )
				{
					sb.append(", ");
				}
			}	
			sb.append(") {");
		}
		else{
			sb.append(cv.name + " __this) {");
		}
		

		if(cv.hasConstructor == true){
			sb.append("\n").append(" java::lang::__Object::init(__this);");
			Method m = cv.constructor;
			//need to know how to use symbol table, but more generic than previous soln sdr
			//notice the tab -_- can be removed with symbol table tho
			
			ArrayList<String> constructorReverser = new ArrayList<String>();
			reverter inheritanceLoopCv = cv;
			while(inheritanceLoopCv != null)
			{
				constructorReverser.add(inheritanceLoopCv.constructor.theBlock);
				inheritanceLoopCv = inheritanceLoopCv.parent;
			}
		
			String[] toArr = new String[constructorReverser.size()];
			toArr = constructorReverser.toArray(toArr);
			for(int i=toArr.length-1; i >= 0; i--)
			{
				sb.append("\n"+toArr[i]);
			}	
		

		}
		else{
			if(cv.parent != null){
			sb.append("__" +cv.parent.name +"::init(__this);");
		}
		}

		for(GlobalVariable g: cv.globalVars){
			if(g.value != "" && g.value != null && !g.modifier.equals("static")){
				sb.append("\n\t__this->").append(g.name + " = new " + j2C.getClass(g.type) + "(" + g.value +");");
			}
		}

		sb.append("\n").append("return __this;");
		sb.append("\n").append("}");
		sb.append("\n");

	}

	void addBasicConstructor()
	{
		sb.append("__"+cv.name + "::__" + cv.name + "()\n: __vptr(&__vtable) {\n }\n");
	}
	void toFile() throws IOException
	{			
		System.out.println("Creating CPP File " + directoryName + cv.name + ".cpp");
		File fout = new File("output/" + directoryName +cv.name + ".cpp");
     	FileOutputStream fos = new FileOutputStream(fout);
     	BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
      	bw.write(sb.toString());
      	bw.close();	
	}
}
