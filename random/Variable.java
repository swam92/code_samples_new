package xtc.random;

public class Variable {
	public String type, name, value;
	public boolean isPrimitive;

	public Variable (String theType, String theName, String theValue, boolean isItPrimitive) {
		this.type = theType;
		this.name = theName;
		this.value = theValue;
		this.isPrimitive = isItPrimitive;
	}
}
