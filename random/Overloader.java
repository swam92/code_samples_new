package xtc.random;

import java.util.ArrayList;
import java.lang.StringBuilder;

public class Overloader {
	ArrayList<Representation> reps;

	public Overloader() {
		reps = new ArrayList<Representation>();
	}
	
	// add a method to the overload list
	// all methods are mangled so method p(arg) becomes p_1(arg), and so on
	public String add(String origName, ArrayList<Type> paramList, Type classType) {
		for (int i = reps.size() - 1; i >= 0; i--) {
			Representation rep = reps.get(i);
			
			if (rep.originalName.equals(origName) && rep.classType.equals(classType)) {
				String[] bits = rep.printName.split("_");
				String lastOne = bits[bits.length-1];
				int num = Integer.parseInt(lastOne);
				int nextIndex = num + 1;
				String newPrintName = origName + "_" + nextIndex;
				
				reps.add(new Representation(origName, newPrintName, paramList, classType));
				
				return newPrintName;
			}
		}
		
		String printName = origName + "_" + 1;
		reps.add(new Representation(origName, printName, paramList, classType));
		return printName;
	}
	
	public String get(String origName, ArrayList<Type> paramList, Type classType) {
		for (Representation rep : reps) {
			//if (rep.originalName.equals(origName) && rep.classType.equals(classType) && rep.paramList.size() == paramList.size()) {
			if (rep.originalName.equals(origName) && classType.isA(rep.classType) && rep.paramList.size() == paramList.size()) {
				boolean the_same = true;
				boolean[] matched = new boolean[paramList.size()];
				for (int i = 0; i < paramList.size(); i++) {
					// if we matched the types for the parameter at this position
					if (rep.paramList.get(i).equals(paramList.get(i)) || paramList.get(i).isA(rep.paramList.get(i))) {
						matched[i] =  true;
					} else {
						matched[i] = false;
					}
				}
				
				for (int i = 0; i < matched.length; i++) {
					if (matched[i] == false) {
						the_same = false;
						break;
					}
				}
				
				if (the_same) {
					return rep.printName;
				}
			}
		}
		
		return null;
	}
	
	public boolean has(String origName, ArrayList<Type> paramList, Type classType) {
		return this.get(origName, paramList, classType) != null;
	}
	
	public ArrayList<String> getMethods() {
		ArrayList<String> methods = new ArrayList<String>();
		
		for (Representation rep : reps) {
			methods.add(rep.toString());
		}
		
		return methods;
	}
	
	// this class represents a method
	// all method's are mangled
	private class Representation {
		String originalName;
		String printName;
		ArrayList<Type> paramList;
		Type classType;
	
		public Representation(String origName, String prtName, ArrayList<Type> prmList, Type classType) {
			this.originalName = origName;
			this.printName = prtName;
			this.paramList = prmList;
			this.classType = classType;
		}
		
		public String toString() {
			StringBuilder str = new StringBuilder(this.classType + "." + this.printName + "(");
			for (int i = 0; i < paramList.size(); i++) {
				str.append(paramList.get(i));
				if (i < paramList.size() - 1) {
					str.append(", ");
				}
			}
			str.append(")");
			return str.toString();
		}
	}
}
