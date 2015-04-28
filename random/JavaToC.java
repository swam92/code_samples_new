package xtc.random;
import xtc.tree.GNode;
//changed cout to std::cout
//changed Object from __Object to "new java::lang::Object"
public class JavaToC 
{
	String translated = "";
	String namespace = "";
	String include = "";
	String returnType = "";
	String classType = "";
	String suppliment = "";
	String translated2 = "";
	String virtualTable = "";
	String simpleTranslate = "";
	Boolean isDefault = false;


	private String map(String java)
	{
		translated = "";
		namespace = "";
		include = "";
		returnType = "";
		classType = "";
		suppliment = "";
		virtualTable = "";
		isDefault = false;

		switch (java)
		{
			case "String":
				translated = "__String";
				include = "\"java_lang.h\"";
				namespace = "java::lang::";
				returnType = "String";
				classType = "java::lang::__String";
				virtualTable = "java::lang::__String_VT *";
				suppliment = "";
				simpleTranslate = "String";
				break;
			case "cout":
			case "System->out->__vptr->println":
			case "System->out->println":
				translated = "cout";
				namespace = "std::";
				include = "<iostream>";
				suppliment = " << std::endl";
				classType = "std::cout";
				break;
			case "Object":
				translated = "__Object";
				include = "\"java_lang.h\"";
				namespace = "java::lang::";
				returnType = "Object";
				virtualTable = "java::lang::__Object_VT *";
				classType = "java::lang::__Object";
				suppliment = "";
				simpleTranslate = "Object";
				break;
			case "Class":
				translated = "__Class";
				include = "\"java_lang.h\"";
				namespace = "java::lang::";
				returnType = "Class";
				virtualTable = "java::lang::__Class_VT *";
				classType = "java::lang::__Class";
				suppliment = "";
				break;
			case "int":
				translated = "int32_t";
				classType = "int32_t";
				returnType = "int32_t";
				simpleTranslate = "int32_t";
				break;
			case "byte":
				translated = "unsigned char";
				classType = "unsigned char";
				returnType = "unsigned char";
				simpleTranslate = "unsigned char";
				break;
			default:
				translated = java;
				returnType = "";
				suppliment = "";
				classType = java;
				include = "";
				namespace = "";
				isDefault = true;
		}
		return translated;
	}

	//use to include a class in a header file
	public String getInclude(String java)
	{
		map(java);
		//if its a regular class
		if(include.equals(""))
		{
			return ("#include \""+java+".h\"\n");
		}
		//if its a translated class
		else return ("#include "+include+"\n");
	}
	//use to get the translated name
	public String translate(String java)
	{
		map(java);
		return translated;
	}

	public String naiveTranslate(String java)
	{
		map(java);
		return simpleTranslate;
	}

	//this gets a return type, can return a pointer if that's the designated return type
	//not sure if i should make it return a pointer if its not a primitive type
	//for example, if object A is called, object A is returned not a pointer to it right now
	//but String, Object, and Class will return pointers. 
	//also int probably needs remapped*
	public String getMethodReturnType(String java)
	{
		map(java);
		if (returnType.equals(""))
		{
			return java;
		}
			return returnType;
	}
	//gitVT
	public String getVT(String java)
	{
		map(java);
		if(virtualTable.equals(""))
		{
			return ("__" + java + "_VT *");
		}
		return virtualTable;
	}
	public String getNamespace(String java)
	{
		map(java);
		return namespace;
	}


	//returns full class type i.e java::lang::__Class
	public String getClassType(String java)
	{
		map(java);
		return classType;
	}

	//gets a suppliment i.e. endl in println
	public String getSuppliment(String java, boolean isArray)
	{
		map(java);
		if(isArray) {
			suppliment = "<< std::endl";
		}
		return suppliment;
	}

	public String getClass(String java)
	{
		map(java);
		if (isDefault == true)
		{
			return ("__" + classType);
		}
		return classType;
	}

	public String getPointer(String java)
	{
		map(java);
		if (returnType.equals(""))
		{
			return (java);
		}
		return returnType;
	}

	public String getCall(GNode n)
	{
		if(n.get(0).getClass().toString().equals("class java.lang.String"))
		{
			map(n.get(0).toString());
			return "->__vptr->";
		}
		else if (n.get(1) instanceof String)
		{
			if (n.getString(1).equals("out"))
			{
		 		return ".";
			}		
		}
		
		return "->";
		
	}

	// public String getVirtualMethodDispatch(reverter cv)
	// {
	// 	//initialize a object, then set its vtable to the right vtable with a cast
		
	// 	Object o = new java::lang::__Object();
	// 	o->__vptr = (java::lang::__Object_VT *) a.__vptr;
	// 	__Object o = a;
	// }
}
