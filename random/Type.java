package xtc.random;

public class Type {
	private final String value;
	private Type parent;

	public Type(String value) {
		this.value = value;
		parent = TypePool.get("Object");
	}
	
	public Type(String value, Type parent) {
		this.value = value;
		this.parent = parent;
	}
	
	public boolean hasParent() {
		return parent != null;
	}
	
	public Type getParent() {
		return parent;
	}
	
	public void setParent(Type type) {
		this.parent = type;
	}
	
	public String getValue() {
		return value;
	}
	
	public String toString() {
		return getValue();
	}
	
	// let's not use instanceOf
	// is a child of another type
	public boolean isA(Type type) {
		Type cur = this;
		do {
			if (cur.equals(type)) {
				return true;
			}
			cur = cur.parent;
		} while (cur != null);
		
		return false;
	}
	
	// is the parent of another type
	public boolean isSuper(Type type) {
		Type cur = type;
		do {
			if (cur.equals(this)) {
				return true;
			}
			cur = cur.parent;
		} while (cur != null);
		
		return false;
	}
	
	// is equal to another type
	public boolean equals(Type type) {
		return value.equals(type.getValue());
	}
	
}
