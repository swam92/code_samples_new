package xtc.random;

import java.lang.StringBuilder;
import java.util.ArrayList;
import xtc.lang.JavaFiveParser;

import xtc.parser.ParseException;
import xtc.parser.Result;

import xtc.tree.GNode;
import xtc.tree.Node;
import xtc.tree.Visitor;

public class forStatement extends Visitor {
	  public forStatement() {

	  }
	  String forString = "";
      public void visitForStatement(GNode n) {
        visitForControl((GNode)n.getNode(0));
      }
      public String visitForControl(GNode n) {
        forString += ("for(");
        if(n.getNode(0) != null) {
          forString += (n.getNode(1).getNode(0).getString(0) + " ");
          visitDeclarator((GNode)n.getNode(2).getNode(0));
          forString += ("; ");
          visitRelationalExpression((GNode)n.getNode(3));
          forString += ("; ");
          visitExpression((GNode)n.getNode(4).getNode(0));
        } else {
          visitDeclarator((GNode)n.getNode(2).getNode(0));
          forString += ("; ");
          visitRelationalExpression((GNode)n.getNode(3));
          forString += ("; ");
          visitExpression((GNode)n.getNode(4).getNode(0));
        }
        forString += (") {\n");
        return forString;
      }

      public void visitRelationalExpression(GNode n) {
        for(Object o: n) {
          if(o instanceof GNode) {
            GNode curr = (GNode) o;
            switch(curr.getName()) {
              case "PrimaryIdentifier": {
                forString += (curr.getString(0) + " ");
              }
              break;
              case "SelectionExpression": {
                  forString += ("");
                  visitExpression((GNode)curr.getNode(0));
                  forString += ("->length");
                
              }
              break;

            }
          } else {
            forString += (o.toString() + " ");
          }
        }
      }

      public void visitExpression(GNode n) {
        for(Object o: n)
        {
          if(o instanceof GNode)
          {
            GNode curr = (GNode) o;
            switch(curr.getName()) {

              case "PrimaryIdentifier": {

                forString += (curr.get(0).toString());
              } 
              break;
              case "AdditiveExpression": {
                visitAdditiveExpression(curr);
              }
              break;

              case "MultiplicativeExpression": {
                visitMultiplicativeExpression(curr);
              }
              break;
            default: {
              
            }
          }
        }
        else {
          forString += (o.toString());
        }
      }
    }
    public void visitDeclarator(GNode n) {
      forString += (n.getString(0) + " = ");
      forString += (n.getNode(2).getString(0));
    }

    public void visitAdditiveExpression(GNode n)
    {
        for(Object o : n)
        {
          if (o instanceof GNode)
          {
            GNode curr = (GNode) o; 
              switch(curr.getName()) {
                 case "PrimaryIdentifier": {
                     forString += (curr.get(0).toString() + " ");
                 }
                 break;
                 case "IntegerLiteral": { //other types
                     forString += (curr.get(0).toString() + " ");
                    
                 }
                 break;
              
               case "AdditiveExpression": {
                
                visitAdditiveExpression(curr); //regular visit is not working out for me
              
                 }
                 break;

                 case "MultiplicativeExpression": {
                
                visitMultiplicativeExpression(curr); //regular visit is not working out for me
              
                 }
                 break;
              }
            }
            else {
              forString += (o.toString() + " ");
              
            }
        }
        
    }

    public void visitMultiplicativeExpression(GNode n)
    {

        for(Object o : n)
        {
          if (o instanceof GNode)
          {
            GNode curr = (GNode) o; 
              switch(curr.getName()) {
                 case "PrimaryIdentifier": {
                     forString += (curr.get(0).toString() + " ");
                 }
                 break;
                 case "IntegerLiteral": { //other types
                     forString += (curr.get(0).toString() + " ");
                 }
                 break;
              
               case "AdditiveExpression": {
                
                visitAdditiveExpression(curr); //regular visit is not working out for me
              
                 }
                 break;
                 case "MultiplicativeExpression": {
                
                visitMultiplicativeExpression(curr); //regular visit is not working out for me
              
                 }
                 break;
              }
            }
            else {
              forString += (o.toString() + " ");
            }
        }
    }
}