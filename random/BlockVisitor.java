package xtc.random;

import java.lang.StringBuilder;
import java.util.ArrayList;
import java.util.Iterator;

import xtc.lang.JavaFiveParser;

import xtc.parser.ParseException;
import xtc.parser.Result;

import xtc.tree.GNode;
import xtc.tree.Node;
import xtc.tree.Visitor;
import xtc.tree.Printer;

import xtc.util.SymbolTable;

public class BlockVisitor extends Visitor {
    StringBuilder sb = new StringBuilder();
    Method caller;
    JavaToC jtc = new JavaToC();
    int check = 0;
    String thisClass;
    String thisInstance;
    Node parentNode; 
    String javaSystemCallBuilder;
    Boolean isConstructorFirstPass = false;
    ArrayBuilder currArray = new ArrayBuilder();
    ArrayList<Call> calls = new ArrayList<Call>(0);
    Call currentCall = null;
    String rtNullCheck = "";
    String top;
    boolean additiveFlag = false;    
    SymbolTable table = Translator.table;
    
    private void scope(Node curr, Node next) {
        if (null != next) {
            visit(next);
        }
    }
    /**
    *   returns true if sym a global, and therefore not a parameter
    */
    private boolean isGlobal(String sym)
    {
        Symbol callerSymbol = Translator.getSymbol(sym, table.current());
        if (callerSymbol != null && callerSymbol.isGlobal)
        {
            //potentially a global variable, let's just make sure it's not a parameter
            boolean isParameter = false;
            for (Variable parameter : this.caller.parameters) {
              if (parameter.name.equals(sym)) {
                return false;
              }
            }
            return true;
        }
        return false;
    }
    /**
    *  return true if this class has an instance variable sym in it  
    */
    private boolean hasGlobal(String sym)
    {
        for (GlobalVariable g: caller.cv.globalVars)
        {
            if(g.name.equals(sym))
            {
                return true;
            }
            
        }
        return false;
    }

    /**
    *   returns true if sym is a param
    */
    private boolean isParam(String sym)
    {
        for (Variable parameter : this.caller.parameters) {
            if (parameter.name.equals(sym)) {
                return true;
            }
        }
        return false;
    }

    /**
    *   returns true if lowest scope of sym is from an inherited class
    */
    private boolean isInherited(String sym)
    {
        //until we find a better way, putting this here will allow us to encapsulate the work around
        return (Translator.getSymbol(sym, table.current()) == null);
    }

    //returns true if their exists a static variable in the global context with this name
    private boolean isStaticGlobal(String sym)
    {
        for (GlobalVariable g: caller.cv.globalVars)
        {
            if(g.modifier.equals("static") && g.name.equals(sym))
            {
                return true;
            }
            
        }
        return false;
    }

   public BlockVisitor(Method m) {
      this.caller = m;
   }
    
   public void visitFieldDeclaration(GNode n) {
    sb.append(Translator.tab());
    visit(n);
    sb.append(";\n");
    //we save this instance @ declarator

    //wish i didn't have to do this
    Node declarators = n.getNode(2);
    Node declarator = declarators.getNode(0);
    if(declarator.get(2) != null)
    {
      //null checks
	Node checker = declarator.getNode(2);
	String s = checker.getName();
	if(!s.equals("IntegerLiteral") && !s.equals("FloatingPointLiteral")){
      sb.append(Translator.tab()).append("__rt::checkNotNull(").append(thisInstance).append(");\n");
}
    }

    thisInstance = null;//and null it right after 

    Node type = (Node) n.get(1);
    Node theType = (Node) type.get(0);
    String theTypeAsString = theType.get(0).toString();
    Node variable = (Node) ((Node) n.get(2)).get(0);
    String varString = variable.get(0).toString();
  
    TypePool.add(new Type(theTypeAsString));
    Translator.curTable.add(new Symbol(varString, TypePool.get(theTypeAsString), table.current()));

    /*Symbol s = Translator.getSymbol(varString, table.current());
    if (s != null) {
        System.out.println("Caller type is: " + TypePool.get(s.type.getValue()).getParent());

        if (TypePool.get(s.type.getValue()).getParent().equals(TypePool.get("Object")) == false) {
            System.out.println("Caller has a parent and it is: " + s.type.getParent());
        }
    }*/
    //Translator.typeSymbols.add(new Symbol(varString, TypePool.get(theTypeAsString), table.current()));

    }
    
    // private methods won't be visited automatically

    public void visitNewClassExpression(GNode n) {
        if(currentCall != null)
        {
            //System.out.println(n);
            StringBuilder exp = new StringBuilder();
            String newClass = ((GNode) n.get(2)).getString(0);
            // TODO: handle new class expression with arguments
            if(!newClass.equals("Object") && !newClass.equals("String") && !newClass.equals("Class")){
                exp.append("__" + newClass).append("::init( new __" + newClass);
                //rangeVisit(n, 2, 3);
                exp.append(")");
            } else {
                exp.append("java::lang::__" + newClass + "::init( new java::lang::__" + newClass + ")");
                //rangeVisit(n, 2, 2);
            }
            currentCall.arguements.add(exp.toString());
        } else {
            if(!thisClass.equals("Object") || !thisClass.equals("String") || !thisClass.equals("Class")){
                sb.append(jtc.getClass(thisClass)).append("::init( new ");
                visit(n);
                sb.append(")");
            } else {
                sb.append("new ");
                visit(n);
            }
        }
    }
    
    public void visitExpressionStatement(GNode n) {
      sb.append(Translator.tab());
      visit(n);
      sb.append(";\n");
    }
    
    public void visitCallExpression(GNode n) 
    {
    boolean isNestedCaller = false;
    boolean hasChild = false;
    Node oneBeneath = n.getNode(0);
    String currentName = "";
    if(oneBeneath.getName().equals("CallExpression"))
    {
        currentCall.nestedChild = true;
        hasChild = true;
    }
	if(parentNode.getName().equals("CallExpression"))
	{
       isNestedCaller = true;
       currentCall.isNested = true;
       currentCall.instance = thisInstance;
       GNode expCaller1 = (GNode) n.get(0);
       currentCall.instance= expCaller1.get(0).toString();
	}

        calls.add(new Call()); //create a new call
        int index = calls.size()-1; //index of the current call
        if(currentCall != null)
        {
          currentCall.arguements.add(calls.get(index)); //make this an arguement of its proceeder
        }

        currentCall = calls.get(index); //set the current call
        rangeVisit(n, 0, 2); //visit first children

        String methodName = n.getString(2);
        
        // mangle the method name!
        String mangledMethodName = methodName;
        
        if (currentCall != null && Translator.reservedMethodNames.contains(methodName) == false && caller.isAConstructor==false) 
        {
            currentCall.methodName = methodName;
            System.out.println("Call methodName: " + currentCall.methodName);
        
            GNode expCaller = (GNode) n.get(0);        
        
            if (n.get(0) != null && expCaller.get(0) instanceof String) // not a chained method, a simple a.m, s.self, etc. and not System.out.println
            { 
                Symbol_Table tempCurTable = Translator.curTable;
                Symbol callerSymbol = Translator.getSymbol(expCaller.getString(0), table.current());
                System.out.println("Symbol for the method " + methodName + " is Symbol: " + callerSymbol);

              // //   // now we have the symbol for this caller, so we will access the overloader with the scope, paramlist, and name
                ArrayList<Type> argumentList = new ArrayList<Type>();
                argumentList.add(TypePool.get(callerSymbol.type.getValue())); // add the type of __this
                System.out.println("Argument: " + TypePool.get(callerSymbol.type.getValue()));

			 if (callerSymbol != null) {
				    for (Object o : (GNode) n.get(3)) {
				        if (o instanceof Node) {
					       GNode arg = (GNode)o;
					       Type param = null;
				            switch (arg.getName()) {
						        case "NewClassExpression": 
                                {
						          TypePool.add(new Type(((Node) arg.get(2)).get(0).toString()));
						          param = TypePool.get(((Node) arg.get(2)).get(0).toString());
						          break;
						        }
						      case "PrimaryIdentifier": {
						          Symbol paramSymbol = Translator.getSymbol(arg.get(0).toString(), table.current());
						          param = paramSymbol.type;
						          break;
						      }
						      case "IntegerLiteral": {
						          param = TypePool.get("int");
						          break;
						      }
						      case "FloatingPointLiteral": {
						          param = TypePool.get("float");
						          break;
						      }
						      case "StringLiteral": {
						          param = TypePool.get("String");
						          break;
						      }
                  case "AdditiveExpression": {
                    GNode add1 = (GNode)arg.get(0);
                    GNode add2 = (GNode)arg.get(2);
                    String add12 = add1.get(0).toString();
                    String add22 = add2.get(0).toString();
                    Symbol temper = Translator.getSymbol(add22, table.current());
		    String which = temper.type.toString();
		    String whichPool;
		    boolean intFlag = false;
		    for(reverter u: Translator.bookKeeper){
			if(!u.name.equals(caller.cv.name))
			{
			  for(Method oop: u.methodObjects)
				{
				String temp = oop.parameters[1].type;
				if(temp.equals("int")){
				intFlag = true;}
				}
			}
		    }
		   if(intFlag == true){
		    whichPool = "int";}
		   else{
		   whichPool = "double";}
		   param = TypePool.get(whichPool);
                  }break;
						      case "CastExpression": {
						          Node castType = (Node) arg.get(0);
						          Node identifier = (Node) castType.get(0);
						          TypePool.add(new Type(identifier.get(0).toString()));
						          param = TypePool.get(identifier.get(0).toString());
						          break;
						      }
						      // problem is when we hit cases that we haven't defined
                                //->so lets log that case so we know what it is (sdr) <-
                                //this doesn't work with inheritance. if A has printother A, A
                                //B inherits from A, the same method, but its args B, A, then this breaks and rets null
                                //fix this and 15 and 16 compile
                            default:
                            {
                                System.out.println("Warning: MethodMangle failure @ this case:: " + arg.getName());
                            }
					       }
					   System.out.println("Argument: " + param);
					   argumentList.add(param);
				        }
			         }
				  
				    mangledMethodName = Translator.overloads.get(methodName, argumentList, callerSymbol.type);
				    System.out.println(mangledMethodName);
			     }
		      }
            }
        //comment out this line to stop method mangling
        //but actually dont because it doesnt stop mangling in header or method
        if(mangledMethodName != null)
        {
            methodName = mangledMethodName;
        } else {
            System.out.println("MANGLED METHOD NAME FAILURE, failed to mangle " + methodName);
            System.out.println("unmangled name printed instead");
            System.out.println("So let's look at the overloads.\nOverloads contains:\n");
            for (String m : Translator.overloads.getMethods()) {
            	System.out.println("Overload: " + m);
            }
        }

        calls.get(index).name.append(methodName); //append call name, () in call

        if(!methodName.equals("println"))
        {
          //soln now, if not standard lib, append this i.e. the class making the call
          if(currentCall.callClass != null)
          {
            if(isGlobal(currentCall.callClass) || (!isParam(currentCall.callClass) && isInherited(currentCall.callClass)))
            {
                currentCall.callClass = "__this->" + currentCall.callClass;
            }
            currentCall.arguements.add(currentCall.callClass);
          }
        }

        rangeVisit(n, 3, n.size()-1); //visit remaining, might be call nested!!
        if(index == 0)
        { 
          sb.append(calls.get(0).toString());
          currentCall = null;
        } else
        {
          currentCall = calls.get(index-1);//change currentCall
        }
        calls.remove(index); //remove call when finished 
    }
    
    public void visitSelectionExpression(GNode n)
    {
      //I assume we are in a call, but not certain, ill check anyway
      if(parentNode.getName().equals("CallExpression")){
        if(currentCall != null)
        {
          rangeVisit(n, 0, n.size()-1);
          currentCall.name.append("->");
          if(n.get(1) instanceof String)
          {

            currentCall.name.append(n.getString(1));
            currentCall.name.append("->__vptr->");
          } else
          {
           rangeVisit(n, 1, n.size()-1);
          }
        } 
      } else if (parentNode.getName().equals("Arguments")) 
      {
        //we can use the call class to handle Selection Expresions also, i.e. b->a
        calls.add(new Call()); //create a new call
        int index = calls.size()-1; //index of the current call
        currentCall.arguements.add(calls.get(index)); //make index of current call
        currentCall = calls.get(index); //set the current call
        if(hasGlobal(n.getString(1)) && isStaticGlobal(n.getString(1)))
        {
            currentCall.name.append("__");
        }
        currentCall.isSelect = true;
        rangeVisit(n, 0, 1); //visit first children
        if(hasGlobal(n.getString(1)) && isStaticGlobal(n.getString(1)))
        {
            currentCall.name.append("::");
        } else
        {
            currentCall.name.append("->");
        }
        currentCall.name.append(n.getString(1));
        rangeVisit(n, 2, n.size()-1);
        calls.remove(index);
      } 
      else
      {
        rangeVisit(n, 0, 1);
        sb.append(n.getString(1));
      }
    }
    public void visitArguments(GNode n)
      {
        if (parentNode.getName().equals("NewClassExpression")) 
        {
          for(Object o: n)
          {
              parentNode = n;
              sb.append(", ");
              if(o instanceof Node)
              {
                dispatch((Node)o);
              }
          }    
        } else
        {
         visit(n);
        }
    }
    public void visitExpression(GNode n)
    {
        rangeVisit(n, 0, 1);
        sb.append(n.getString(1));
        rangeVisit(n, 2, 2);
    }
    
    public void visitType(GNode n)
    {
      if(parentNode.getName().equals("CastExpression") )
      {
        rangeVisit(n, 0, 1);
        sb.append(">(");
        rangeVisit(n, 1, n.size()-1);
      } else 
      {
        visit(n);
      }
    
    }
    public void visitNested(Node n){
        Translator.upTab();
        //BlockVisitor bv= new BlockVisitor(null);
        //String nestedBlock = bv.stringDispatch(n);
    
        table.enter(table.freshName("nestedStatement"));
        table.mark(n);
      
        visit(n);
        
        for (Symbol sym : Translator.typeSymbols) {
          if (sym.scope.equals(table.current())) {
            Translator.typeSymbols.remove(sym);
          }
        }
        table.exit();
        //sb.append(nestedBlock);
        Translator.downTab();
        sb.append("\n" + Translator.tab() +"}\n");

    }


    public void visitWhileStatement(GNode n) {
        sb.append(Translator.tab());
        sb.append("while (" + n.getNode(0).getNode(0).getString(0)+ n.getNode(0).getString(1) + n.getNode(0).getNode(2).getString(0)+") {\n");
        visitNested(n.getNode(1));
    }

    //Stuff for For Statemnet
    public void visitForStatement(GNode n) {
      table.enter(table.freshName("forStatement"));
      table.mark(n);
      
        forStatement forLoop = new forStatement();
        //forcontrol statement
        sb.append(forLoop.visitForControl((GNode)n.getNode(0)));
        visitNested(n.getNode(1));
        
        for (Symbol sym : Translator.typeSymbols) {
          if (sym.scope.equals(table.current())) {
            Translator.typeSymbols.remove(sym);
          }
        }
        table.exit();
    }

    //might need more work for expressions and the like. 
    //FAILS nullpointer expeption for return;
    public void visitReturnStatement(GNode n) {
      sb.append(Translator.tab());
      GNode returnValue = (GNode) n.get(0);
        sb.append("return ");
        visit(n);
        sb.append(";\n");
        // if (returnValue.getName().equals("StringLiteral"))
        // {
        //     sb.append("new " + jtc.getClassType("String") + "(" + returnValue.get(0).toString() + ");\n");
        // }
        // else {
        //     sb.append("__this->" + returnValue.get(0).toString() + ";\n");
        // }
      
    }
    
    public void visitThisExpression(GNode n)
      {
        String parentName = parentNode.getName();
        
        if(parentName.equals("SelectionExpression"))
        {
          //if(caller.isAConstructor == true && isConstructorFirstPass ==false) {
            //change later
            //isConstructorFirstPass = true;

          //} else {
          sb.append("__this->");
        //}
        } else {
          sb.append("__this");
        }
      }

    // if we encounter string literals
    public void visitStringLiteral(GNode n) {
      if(currentCall != null)
      {
	if(currentCall.toString().equals("std::cout <<  << std::endl")){
	currentCall.arguements.add(n.getString(0));
	}
	else{
        currentCall.arguements.add("new java::lang::__String("+n.getString(0) +")");}
	}
      else {
        sb.append(" new java::lang::__String(").append(n.getString(0)).append(")");
      }
    }

  public void visitAdditiveExpression(GNode n){
    visit(n);
  }

    public void visitIntegerLiteral(GNode n) {
        if(parentNode.getName() == "ConcreteDimensions"){
            currArray.setSize(n.getString(0));
        } else{
         if(currentCall != null)
          {
            //currentCall.arguements.add(n.getString(0));
          } else {
            sb.append(n.getString(0));
          }
        }
    }

    public void visitPrimitiveType(GNode n) {
        sb.append(jtc.translate(n.get(0).toString()) + " ");
    }
    /**
    *   branch based on parent, if Declarator, handle vmd
    */
    public void visitPrimaryIdentifier(GNode n) 
    {
    
      String parentName = parentNode.getName();
        String primID = n.getString(0);
         if(currArray.arrayClass != null && parentName.equals("Declarator")){
          currArray.setDeclare(primID);
          sb.append(currArray.toString());
        }
        else if(parentName.equals("Declarator"))
        {
            sb.append("new ").append(jtc.getClass(thisClass)).append(";\n");
            sb.append(Translator.tab());
            sb.append(parentNode.getString(0)).append("->__vptr = (").append(jtc.getVT(thisClass)).append(") ");
            sb.append(primID).append("->__vptr");
        } else if (parentName.equals("SelectionExpression"))
        {
            if(currentCall != null)
            {
              currentCall.name.append(primID);
              currentCall.callClass = primID;

            } else
            {
              //this is if the grandfather is a declarator
               sb.append(primID).append("->");
            }
        } 

        else if (parentName.equals("Arguments") || parentName.equals("AdditiveExpression"))
        {
          if(currentCall != null)
            {

                if(Translator.getSymbol(n.get(0).toString(), table.current()) == null || isGlobal(primID))
                {
                    currentCall.arguements.add("__this-> "+ primID);
                } 
                else {
                if(additiveFlag == false && parentName.equals("AdditiveExpression")){  
                  currentCall.arguements.add(primID+"+");
                  additiveFlag=true;
              }
              else{
                currentCall.arguements.add(primID);
              }
            }
            } else 
            {
               sb.append("currentCall was null at primID: " + primID + " parent: " + parentNode);
            }
        }else if (parentName.equals("CallExpression"))
        {
          if(currentCall != null)
          { 
            if(isGlobal(primID) || (!isParam(primID) && isInherited(primID)) )
            {
              currentCall.name.append("__this->");
            }  
            //here currentCall is an arguement of another call
            currentCall.callClass = primID;
            currentCall.name.append(primID).append("->__vptr->");
          }
        }
        else if (parentName.equals("ReturnStatement")){
          //need to branch based on wether or not you should add this
	  boolean flag = false;
	  for(Variable params: caller.parameters){
	    if(params.name.equals(primID)){
		flag = true;}
	  }
	  if(flag == false){
          sb.append("__this->").append(primID);}
	   else{
	sb.append(primID);
        } 
	}else 
        {
          if(!primID.equals("System") && isGlobal(primID))
          {
            sb.append("__this->");
          } 
         else if(Translator.getSymbol(primID, table.current()) == null && !isParam(primID)) //inherited. assumes this is inherited. workaround -_-
           {
                sb.append("__this->");
           }
              sb.append(primID);
           
          } 
      visit(n);
    }
        
    public void visitSubscriptExpression(GNode n) {
      if(parentNode.getName().equals("Arguments"))
      {
        StringBuilder tempsb = new StringBuilder();
        if(n.getNode(0).getName() == "SubscriptExpression"){
            tempsb.append(n.getNode(0).getNode(0).getString(0));
            tempsb.append("[");
            tempsb.append(n.getNode(0).getNode(1).getString(0));
            tempsb.append("][");
            tempsb.append(n.getNode(1).getString(0));
            tempsb.append("]");
        } else {
            tempsb.append("(*" + n.getNode(0).getString(0) + ")");
            tempsb.append("[");
            tempsb.append(n.getNode(1).getString(0));
            tempsb.append("]");
        }

        currentCall.arguements.add(tempsb.toString());

      } else 
      {
        if(n.getNode(0).getName() == "SubscriptExpression"){
            sb.append(n.getNode(0).getNode(0).getString(0));
            sb.append("[");
            sb.append(n.getNode(0).getNode(1).getString(0));
            sb.append("][");
            sb.append(n.getNode(1).getString(0));
            sb.append("]");
        } else {
            sb.append("(*" + n.getNode(0).getString(0) + ")");
            sb.append("[");
            sb.append(n.getNode(1).getString(0));
            sb.append("]");
        }
      }
    }
    public void visitNewArrayExpression(GNode n){
        visit(n);
        String arrayString = ("new __rt::Array<"+currArray.postarrayClass+">"+"("+currArray.arraySize+");");
        currArray.setDeclare(arrayString);
        sb.append(currArray.toString());
    }
    /**
    *  branch for type and newClass Expression
    */
    public void visitQualifiedIdentifier(GNode n)
    {
        thisClass = n.getString(0);
        if(this.caller != null && this.caller.usedClasses.contains(n.getString(0)) == false) 
        {
            this.caller.usedClasses.add(n.getString(0));
        }

        String parentName = parentNode.getName();

        if(parentName != "NewArrayExpression"){
            currArray = new ArrayBuilder();
        }
        if(parentNode.getNode(1) != null && parentName != "NewArrayExpression"){
            currArray.setClass(n.getString(0));
            currArray.setDimension(parentNode.getNode(1).size());
        }
        else if(parentName == "NewArrayExpression"){
            currArray.setPostClass(n.getString(0));
        } else if(parentName.equals("Type"))
        {
            sb.append(jtc.getPointer(n.getString(0))).append(" ");
        } else if(parentName.equals("NewClassExpression"))
        {
            sb.append(jtc.getClass(n.getString(0)));
        } else 
        {
            sb.append("error: case " + parentName + "not handled by BlockVisitor.visitQualified Identifier");
        }
        visit(n);
    }

    public void visitCastExpression(GNode n)
    {
        if (currentCall != null) {
            GNode type = (GNode) n.get(0);
            GNode qI = (GNode) type.get(0);
            GNode var = (GNode) n.get(1);
            if (var.getName().equals("PrimaryIdentifier")) {
                currentCall.arguements.add(" __rt::java_cast<" + qI.getString(0) + " >(" + var.getString(0) + ")");    
            } else if (var.getName().equals("SubscriptExpression")) {
                dispatch(var);
            }
        } else {
            sb.append(" __rt::java_cast<");
            visit(n);
            sb.append(")");
        }
    }
    public void visitDeclarator(GNode n)
    {
        this.thisInstance = n.getString(0); //need to save to rt check
        if(currArray.arrayClass != null){
            currArray.setName(n.getString(0));
        }else {
       
          // Symbol variable = Translator.getSymbol(n.getString(0), table.current());
          // if(variable != null)
          // {
          //   System.out.println("VARIABLE:: " +variable + " " + variable.isGlobal);
          // }
          // if(variable!= null &&  variable.isGlobal ==true )
          // {
          //   sb.append("__this->");
          // }    
          sb.append(n.getString(0));
          if(n.get(2) != null)
          {
            sb.append(" = ");
          }
        }
        visit(n);
    }

   public void visitFloatingPointLiteral(GNode n){
   if(parentNode.getName() == "ConcreteDimensions"){
            currArray.setSize(n.getString(0));
        } else{
         if(currentCall != null)
          {
            currentCall.arguements.add(" __rt::java_cast<Object>((object) " + n.getString(0) + ") ");
          } else {
            sb.append(n.getString(0));
          }
        }
		visit(n);
	/*sb.append(n.getString(0));*/
	}

    public void visitBlock(GNode n)
    {
        Translator.upTab();
        sb.append("{");
        visit(n);
        sb.append("}");
        Translator.downTab();
    }


    public void visit(Node n) {
        for (Object o : n) {
            // The scope belongs to the for loop!
            if (o instanceof Node)
            {
              parentNode = n;
                // dispath as usual
                /*sb.append("\n");
                sb.append(((Node) o).toString());
                sb.append("\n");*/
                dispatch((Node) o);
            }
        }
    }

    /**
    *  visit nodes in a specified range 
    *  
    */
    public int rangeVisit(Node n, int start, int end)
    {
        
        if (end >= n.size())
        {
            end = n.size()-1;
        }
        for(int i = start; i <= end; i++)
        {
            parentNode = n;
            //not sure if cast is nessesary
            if(n.get(i) instanceof Node)
            {
                dispatch((Node) n.getNode(i));
            }
        }
        return (n.size() - 1 - end);
    }
    
    public String stringDispatch(Node n) {
        this.dispatch(n);
        return sb.toString();
    }
}

class ArrayBuilder {
    String arrayClass;
    String postarrayClass;
    int dimension;
    String declare;
    String name;
    int arraySize;

    public void setClass(String c){
        arrayClass = c;
    }
    public void setPostClass(String p){
        postarrayClass = p;
    }
    public void setDimension(int d){
        dimension = d;
    }
    public void setDeclare(String d){
        declare = d;
    }
    public void setName(String n){
        name = n;
    }
    public void setSize(String s){
        arraySize = Integer.parseInt(s);
    }


    public String toString(){
        String arrayString;
        arrayString = ("__rt::Ptr<__rt::Array<"+arrayClass+"> > " + name + "= " + declare);
        return arrayString;
    }
}
class Call 
{
  StringBuilder name = new StringBuilder();
  String methodName;
  ArrayList<Object> arguements = new ArrayList<Object>();
  ArrayList<String> usedClasses = new ArrayList<String>();
  String callClass;
  boolean isNested;
  boolean nestedChild;
  static String instance;
  boolean isSelect = false;

  public String toString()
  {
    JavaToC jtc = new JavaToC();
    StringBuilder sb = new StringBuilder();
    String name = jtc.translate(this.name.toString());
    sb.append(jtc.getNamespace(name)).append(name);
    if(name.equals("cout"))
    {
      usedClasses.add("cout");
      sb.append(" << ");
    } else {
      if(isSelect == false)
      {
        sb.append("(");
      }
    }
    for(int i = 0; i < arguements.size(); i++) 
    {
      if (arguements.get(i) instanceof String)
      {
        sb.append(arguements.get(i));
      } else if (arguements.get(i) instanceof Call)
      {
        if(isNested){
            sb.append(arguements.get(i).toString());
        }
        else if (nestedChild){
        sb.append(this.instance + "->__vptr->" +arguements.get(i).toString());
        }
        
        else{
            sb.append(arguements.get(i).toString());
        }
        

      } else 
      {
        sb.append("Illegal arguement type:: ").append(arguements.get(i)).append(" ");
      }

      if(i != arguements.size()-1)
      {
        boolean toAppendComma = true;
        String temp = arguements.get(i).toString();
        toAppendComma = temp.contains("+");
        if(toAppendComma == false){
          sb.append(", ");
        }
      }

    }
    if(name.equals("cout"))
    {
      sb.append(jtc.getSuppliment(name, false));
    } else {
      if(isSelect == false)
      {
        sb.append(")");
      }
    }
    return sb.toString();
  }
}
