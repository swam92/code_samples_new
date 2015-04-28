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

public class GlobalVariable extends Variable{
	public String type, name, value, modifier;
	public boolean isPrimitive;

	public GlobalVariable (String theType, String theName, String theValue, boolean isItPrimitive, String modifier) {
		super(theType,theName,theValue,isItPrimitive);
		this.modifier = modifier;
	}

}
