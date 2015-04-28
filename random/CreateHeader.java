package xtc.random;

import xtc.util.Tool;
import java.io.Reader;
import java.io.File;
import xtc.tree.Node;
import java.io.IOException;
import xtc.parser.ParseException;
import xtc.lang.JavaFiveParser;
import xtc.parser.Result;
import xtc.util.SymbolTable;
import xtc.lang.JavaAnalyzer;
import xtc.tree.Visitor;
import xtc.tree.GNode;
import java.util.ArrayList;
import java.io.FileOutputStream;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.lang.StringBuilder;


public class CreateHeader
{	
  //Constructor that is called to create the header.
	public CreateHeader(ArrayList<reverter> cvList, String directoryName)
	{
    
    ArrayList<Header> headerFiles = new ArrayList<Header>(0);
    for(int i = 0; i < cvList.size(); i++)
    {
      headerFiles.add(new Header(cvList.get(i), directoryName));
    }

    for (Header h: headerFiles)
    {
      
      h.toFile();
      for(String s: h.cv.usedClasses)
      {
        System.out.println("uses: "+s);
      }
    }
	}
}

class Header
{
  ArrayList<Method> constructor = new ArrayList<Method>(0);
  ArrayList<Method> publicMethods = new ArrayList<Method>(0);
  ArrayList<Method> privateMethods = new ArrayList<Method>(0);
  ArrayList<Method> otherMethods = new ArrayList<Method>(0);
  ArrayList<String> forwardDeclare = new ArrayList<String>(0); //add with .forwardDeclare.add()
  public reverter cv;
  String indent = "    ";
  StringBuilder sb;
  String directoryName = "";

  JavaToC translator = new JavaToC();

  public Header(reverter cv, String directoryName)
  {
    this.cv = cv;
    this.directoryName = directoryName;
    if(cv.methodObjects != null)
    {
      
      for(Method m: cv.methodObjects)
      {
        add(m);
      }
    }
  }
  //This method will add the methods into the specific arrayList that will be later printed out into the .h file
  public void add(Method method)
  {
    if(method.isAConstructor == false){
      if(method.visibility.equals("public") )
      {
        publicMethods.add(method);
      }
      else if(method.visibility.equals("private"))
      {
        privateMethods.add(method);
      }
      else {
        otherMethods.add(method);
      } 
    }
    else if(method.isAConstructor == true){
      constructor.add(method);
    }
  }

  //not fully translated. but it should work it everything is forward declared
  //will add includes soon
  public void addForwardDeclare(String usedClass)
  {

    if(forwardDeclare.isEmpty())
    {
      forwardDeclare.add(usedClass);
    }
    else {
      boolean isPresent = false;
      for(String s: forwardDeclare)
      {
        //not sure if this is a proper compare
        if(s.equals(usedClass))
        {
          isPresent = true;
        }
      }
      if (!isPresent)
      {
        forwardDeclare.add(usedClass);
      }
    }
  }
  
  //Write this into the .h file that will be created.
  public void toFile() 
  { 
      sb = new StringBuilder();
      beginHeader();
      for(String s: cv.usedClasses)
      {
        addForwardDeclare(s);
      }
     
      
      if(!forwardDeclare.isEmpty())
      {
        addClass();
      }
      startHeader();
      if(!publicMethods.isEmpty())
      {
        sb.append(indent);
        visMethod(publicMethods);
      }
      if(!privateMethods.isEmpty())
      {
        sb.append(indent);
        visMethod(privateMethods);
      }
      if(!otherMethods.isEmpty())
      {
        sb.append(indent);
        visMethod(otherMethods);
      }
      if(!constructor.isEmpty())
      {
        sb.append(indent);
        visMethod(constructor);
      }
      if(publicMethods.isEmpty() && privateMethods.isEmpty() && otherMethods.isEmpty()
        && constructor.isEmpty()){
        sb.append("static " + cv.name + " init("+cv.name+");");
      }
      
      // add the and the v table objects
      sb.append("\n\tstatic Class __class();\n");
      sb.append("\tstatic __" + cv.name + "_VT __vtable;");
      

    sb.append("\n");

    reverter inheritanceLoopedCV = cv;
 
    while(inheritanceLoopedCV != null)
    {
      if(!inheritanceLoopedCV.globalsHelp.isEmpty()){
      sb.append(Translator.tab());
    
      boolean isStaticIntFlag = false;
      for(GlobalVariable g: inheritanceLoopedCV.globalVars){
        if(g.modifier.equals("static")){
          sb.append("static const ");
          if(g.type.equals("int")){
            isStaticIntFlag = true;
          }
        }
        if(g.type.equals("int") || g.type.equals("byte")){
          sb.append(translator.naiveTranslate(g.type) + " ");
        }
        else{
          sb.append(g.type + " ");
        }
        sb.append(g.name);
        if(isStaticIntFlag == true){
          if(g.value != null)
          {
            sb.append(" = ").append(g.value);
          } else {
            sb.append(" = ").append("0");
          }
        }
        sb.append(";");
        sb.append("\n");
      }

      }//end if not
      inheritanceLoopedCV = inheritanceLoopedCV.parent;
    }


      sb.append("\n");
      sb.append("};");
      sb.append("\n");
      
      addVTable();
      
      try
      {      
         createFile();
      } catch (IOException e)
      {
        System.out.println("exception handled: " + e);
      }
      
  }


  //Start writing to the file that was created. Will be called by toFile
  void beginHeader() 
  {
    sb.append("#pragma once\n\n");
    sb.append("#include \"java_lang.h\"\n");

  }

  //Write in the beginning of the header.

  void startHeader() 
  {

    sb.append("struct __" + cv.name + ";");
    sb.append("\n");
	  sb.append("struct __" + cv.name + "_VT;");
	  sb.append("\n");
	  sb.append("typedef __rt::Ptr<__" + cv.name +">"  + " " + cv.name +";");
    sb.append("\n");
	  sb.append("typedef __rt::Ptr<java::lang::__Object> Object;");
	  sb.append("\n");
	  sb.append("typedef __rt::Ptr<java::lang::__String> String;");
	  sb.append("\n");
	  sb.append("typedef __rt::Ptr<java::lang::__Class> Class;");
	  sb.append("\n");
	  sb.append("\n");
    sb.append("struct __" + cv.name);
    sb.append("\n");
    sb.append("{");
    sb.append("\n");
      
      // add the v pointer
      Translator.upTab();
      sb.append(Translator.tab() + "__" + cv.name + "_VT* __vptr;\n\n");
      sb.append(Translator.tab() + "__" + cv.name + "();");
      Translator.downTab();
      sb.append("\n");
      sb.append("\n");
  }
  
  /*
    Creating the vtable with the hardcoded Object classes
  */
  void addVTable()
  {
  	  sb.append("\n\nstruct __" + cv.name + "_VT");
  	  sb.append("\n");
  	  sb.append("{");
  	  sb.append("\n");
  	  
  	  // get list of methods for cv
  	  ArrayList<Method> cvMethods = new ArrayList<Method>();
  	  if(!publicMethods.isEmpty())
      {
        for (Method m : publicMethods) {
        	cvMethods.add(m);
        }
      }
      if(!privateMethods.isEmpty())
      {
        for (Method m : privateMethods) {
        	cvMethods.add(m);
        }
      }
      if(!otherMethods.isEmpty())
      {
        for (Method m : otherMethods) {
        	cvMethods.add(m);
        }
      }
  	  
  	  // get list of methods for cv if it has a parent
  	  ArrayList<Method> diffs = new ArrayList<Method>();
  	  if (cv.isPolymorphic && cv.parentIndex >= 0) {
  	  	  // has a parent
  	  	  reverter parent = Translator.bookKeeper.get(cv.parentIndex);

	  	  for (Method parentMethod : parent.methodObjects) {
	  	      for (Method childMethod : cv.methodObjects) {
	  	          if (parentMethod.name != childMethod.name) {
	  	          	  if (diffs.contains(parentMethod) == false) {
	  	          	  	  diffs.add(parentMethod);
	  	          	  }
	  	          }
	  	      }
	  	  }
  	  }
  	  
  	  Translator.upTab();
  	   //Begin appending the hard coded Object methods along with the additional methods that will be added in. 
  	  sb.append(Translator.tab() + "Class __isa;");
  	  sb.append("\n");
      sb.append(Translator.tab() + "void (*__delete)(__" + cv.name + "*);");
      sb.append("\n");
  	  sb.append(Translator.tab() + "int32_t (*hashCode)(" + cv.name + ");");
  	  sb.append("\n");
  	  sb.append(Translator.tab() + "bool (*equals)(" + cv.name + ", Object);");
  	  sb.append("\n");
  	  sb.append(Translator.tab() + "Class (*getClass)(" + cv.name + ");");
  	  sb.append("\n");
  	  sb.append(Translator.tab() + "String (*toString)(" + cv.name + ");");
      ArrayList<Method> addedMethods = new ArrayList<Method>(0);
      for(Method extra : cvMethods){
        addedMethods.add(extra);
        if(!extra.name.equals("toString") &&
           !(extra.name.equals("getClass")) &&
           !(extra.name.equals("hashCode")) &&
           !(extra.name.equals("toString")) &&
           !(extra.name.equals("main")) ){
              sb.append("\n").append(Translator.tab());
              sb.append(extra.returnType).append(" (*");
              sb.append(extra.name).append(")(");
              for(int i = 0; i < extra.parameters.length; i++)
              {
		if(extra.parameters[i].type.equals("int") ||extra.parameters[i].type.equals("byte")){
		sb.append(translator.naiveTranslate(extra.parameters[i].type));
		}
		else{
                sb.append(extra.parameters[i].type);}
                if(i != extra.parameters.length-1)
                {
                  sb.append(", ");
                }
              }
              sb.append(");");
            }
      }

      reverter inheritanceCV = cv.parent;

      while(inheritanceCV != null)
      {

       for(Method parentMethods : inheritanceCV.methodObjects){
        if(!parentMethods.name.equals("toString") &&
          !parentMethods.name.equals("getClass") &&
          !parentMethods.name.equals("equals") &&
          !parentMethods.name.equals("hashCode") &&
           parentMethods.isAConstructor == false )
        {

          if(!containsString(addedMethods, parentMethods.name))
          {
            addedMethods.add(parentMethods);
            sb.append("\n");
            sb.append(Translator.tab()+parentMethods.returnType+"(*"+ parentMethods.name + " )(");
             
             int count = 0;
              for(int i = 0; i < parentMethods.parameters.length; i++)
                {
                  if(count==0){
                    sb.append(cv.name);
                    count++;
                  }
                  else{
                  sb.append(parentMethods.parameters[i].type);
                }
                    if(i != parentMethods.parameters.length-1)
                    {
                      sb.append(", ");
                    }
                }
            sb.append(");");
          }
        }
      }
      inheritanceCV = inheritanceCV.parent;
    }

  	  //The Vtable that will be hardcoded
  	  sb.append("\n\n");
  	  
  	  sb.append(Translator.tab() + "__" + cv.name + "_VT()");
  	  sb.append("\n");
      sb.append(Translator.tab() + ": __isa(" + "__" +cv.name + "::__class()),");
      sb.append("\n");

      sb.append(Translator.tab() + "__delete(&__rt::__delete<__" + cv.name + ">),");
      sb.append("\n");
      if (cvMethods.contains("hashCode")) {
      	  sb.append(Translator.tab() + "hashCode(&__" + cv.name + "::hashCode),");
      } else {
	      sb.append(Translator.tab() + "hashCode((int32_t(*)(" + cv.name + "))&java::lang::__Object::hashCode),");
      }
      sb.append("\n");
      if (cvMethods.contains("equals")) {
      	  sb.append(Translator.tab() + "equals(&__" + cv.name + "::equals),");
      } else {
          sb.append(Translator.tab() + "equals((bool(*)(" + cv.name + ", Object))&java::lang::__Object::equals),");
      }
      sb.append("\n");
      if (cvMethods.contains("getClass")) {
          sb.append(Translator.tab() + "getClass(&__" + cv.name + "::getClass),");
      } else {
          sb.append(Translator.tab() + "getClass((Class(*)(" + cv.name + "))&java::lang::__Object::getClass),");
      }
      sb.append("\n");
      if (cv.methods.contains("toString")) {
          sb.append(Translator.tab() + "toString(&__" + cv.name + "::toString)");
      } else {
          sb.append(Translator.tab() + "toString((String(*)(" + cv.name + "))&java::lang::__Object::toString)");
      }

      for (Method listMethod : addedMethods)
      {
        if (!listMethod.name.equals("toString") && !listMethod.name.equals("hashCode")
          && !listMethod.name.equals("equals") && !listMethod.name.equals("getClass")&& !listMethod.name.equals("main") && !listMethod.isAConstructor)
        {

          sb.append(",\n").append(Translator.tab()).append(listMethod.name);
          //its belongs to this class
          if (listMethod.cv.name.equals(cv.name)) 
          {
            sb.append("(__").append(cv.name).append("::").append(listMethod.name).append(")");;
          } else 
          {
             sb.append("((").append(listMethod.returnType).append("(*)(");
              //think we might need a loop here
              sb.append(cv.name).append(",").append(listMethod.parameters[1].type).append("))");
              sb.append("&__").append(listMethod.cv.name).append("::").append(listMethod.name).append(") ");
          }
        }
      }

        sb.append("{};");
  	  
  	  Translator.downTab();
  	  
  	  sb.append("\n");
  	  sb.append("}");
  	  sb.append(";");
  	  sb.append("\n");
  	  sb.append("\n");
  }

  void addClass()
  {
    JavaToC translate = new JavaToC();
    for(String s: forwardDeclare)
    {

        sb.append(translate.getInclude(s));

    }
    sb.append("\n\n");
  }

  /*
      This method goes through all of the methods inside the ArrayList of methods that was parsed and taken from the file.
      It will then go onto appending all of them into the .h file that is being created in this class.
  */
  void visMethod(ArrayList<Method> methods)
  {
    JavaToC translate = new JavaToC();
    

    if(cv.hasConstructor == false){
        sb.append(Translator.tab() + "static ");
        sb.append(cv.name).append(" init("+cv.name);
        sb.append(");\n");

    }


    for (Method m: methods) {
      Translator.upTab();
        sb.append(Translator.tab() + "static ");
        if(m.isAConstructor == false ){
          sb.append(translate.getMethodReturnType(m.returnType) + " ");

          sb.append(m.name);
          sb.append("(");

          for(int i = 0; i < m.parameters.length; i++)
          {
            
	   if(m.parameters[i].type.equals("int")){
	sb.append("int32_t");
	}
	else if(m.parameters[i].type.equals("byte")){
	sb.append("unsigned char");
	}
	else{ 
          sb.append(m.parameters[i].type);
	}
          if(i != (m.parameters.length-1))
            {
              sb.append(", ");
            }
          }
          sb.append(")");
        }

        else{

          sb.append(cv.name).append(" init(");
          for(int i=0; i<m.parameters.length;i++){
		if(m.parameters[i].type.equals("byte")){
		sb.append("unsigned char");}
	     else{
            sb.append(m.parameters[i].type);}
            if(i != m.parameters.length-1)
            {
              sb.append(", ");
            }
          }
          sb.append(");");
        }

        sb.append(";");
        sb.append("\n");
      }
      Translator.downTab();
  }
  /**
  * if the string is the name of a method in the arraylist return true
  */
  private boolean containsString(ArrayList<Method> methods, String name)
  {
    for(Method m: methods)
    {
      if(name.equals(m.name))
      {
        return true;
      }
    }
    return false;
  }
  void createFile() throws IOException
  {
      File fout = new File("output/" + directoryName + "/"+ cv.name + ".h");
      FileOutputStream fos = new FileOutputStream(fout);
      BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

      bw.write(sb.toString());
      bw.close();
  }

  public String getHeader()
  {
    return sb.toString();
  }
}
