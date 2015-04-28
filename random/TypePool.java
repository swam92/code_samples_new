package xtc.random;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

// this class will be static and will hold all the types used in a program
// when a new type is encountered add it to the type pool and set it's parent if it has one
// if it doesn't have a parent then we set it's parent to object
// once the super class has been added to the TypePool, we add new objects with parents as so:
// TypePool.add(new Type(name, TypePool.get(parentName));
public class TypePool {
	private static HashMap<String, Type> pool = new HashMap<String, Type>();
	//TypePool.add(new Type("Object"));
	private static boolean initiated = false;
	
	public static Type get(String name) {
		if (TypePool.initiated == false) {
			TypePool.init();
		}
		
		return TypePool.pool.get(name);
	}
	
	public static void add(Type type) {
		if (TypePool.initiated == false) {
			TypePool.init();
		}
	
		if (TypePool.pool.containsKey(type.getValue()) == false) {
			TypePool.pool.put(type.getValue(), type);
		} /*else {
			TypePool.pool.remove(type.getValue());
			TypePool.pool.put(type.getValue(), type);
		}*/
	}
	
	public static void set(String name, Type type) {
		TypePool.pool.remove(name);
		TypePool.add(type);
	}
	
	private static void init() {
		// so that it's initiated
		TypePool.initiated = true; // must be called before any other adds, etc
		
		TypePool.add(new Type("Object", null)); // object is the only one without a parent
		TypePool.add(new Type("int", TypePool.get("Object")));
		TypePool.add(new Type("float", TypePool.get("Object")));
		TypePool.add(new Type("double", TypePool.get("Object")));
		TypePool.add(new Type("short", TypePool.get("Object")));
		TypePool.add(new Type("byte", TypePool.get("Object")));
		TypePool.add(new Type("long", TypePool.get("Object")));
		TypePool.add(new Type("char", TypePool.get("Object")));
		TypePool.add(new Type("String", TypePool.get("Object")));
	}
	
	public static String asString() {
		StringBuilder out = new StringBuilder("Printing the current Type Pool");
		Iterator it = pool.entrySet().iterator();
		while (it.hasNext()) {
		    Map.Entry pairs = (Map.Entry)it.next();
		    out.append("\nType " + pairs.getKey() + " has parent " + TypePool.get(pairs.getKey().toString()).getParent());
		    //it.remove(); // avoids a ConcurrentModificationException
		}
		return out.toString();
	}
}
