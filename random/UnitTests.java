package xtc.random;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

public class UnitTests
{
	// ~~~~~~~~~~~~~~~~JAVATOC TESTS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		JavaToC j2c = new JavaToC();
	
		@Test
		public void testGetInclude()
		{
			String query = j2c.getInclude("String");
			org.junit.Assert.assertEquals("failure- should be \'#include \"java_lang.h\"\n \'", "#include \"java_lang.h\"\n", query);
			query = j2c.getInclude("A");
			org.junit.Assert.assertEquals("failure- should be \"#include \"A.h\"\n \'", "#include \"A.h\"\n", query);
		}

		@Test
		public void testTranslate()
		{
			String query = j2c.translate("Object");
			org.junit.Assert.assertEquals("failure", "__Object", query);
			query = j2c.translate("A");
			org.junit.Assert.assertEquals("failure", "A", query);
		}

		@Test
		public void testGetMethodReturnType()
		{
			String query = j2c.getMethodReturnType("Object");
			org.junit.Assert.assertEquals("failure", "Object" , query);
			query = j2c.getMethodReturnType("A");
			org.junit.Assert.assertEquals("failure", "A" , query);
		}

		@Test
		public void testGetNamespace()
		{
			String query = j2c.getNamespace("Object");
			org.junit.Assert.assertEquals("failure", "\"java::lang\"", query);
			query = j2c.getNamespace("A");
			org.junit.Assert.assertEquals("failure", "" , query);
		}

	@Test
	public void testObjTranslate()
	{
		/*String query = j2c.objTranslate("Object");
		org.junit.Assert.assertEquals("failure", "new java::lang::__Object();", query);
		query = j2c.objTranslate("A");
		org.junit.Assert.assertEquals("failure", "" , query);*/
	}

		@Test
		public void testGetClassType()
		{
			String query = j2c.getClassType("Object");
			org.junit.Assert.assertEquals("failure", "java::lang::__Object", query);
			query = j2c.getClassType("A");
			org.junit.Assert.assertEquals("failure", "A" , query);
		}
	// @Test
	// public void testGetCall()
	// {
	// 	String query = j2c.getCall("String");
	// 	org.junit.Assert.assertEquals("failure", "->__vptr->", query);
	// 	query = j2c.getCall("A");
	// 	org.junit.Assert.assertEquals("failure", "." , query);
	// }
	/*
		@Test
		public void testGetSuppliment()
		{
			String query = j2c.getSuppliment("System.out.println");
			org.junit.Assert.assertEquals("failure", "->data << endl", query);
			query = j2c.getSuppliment("A");
			org.junit.Assert.assertEquals("failure", "" , query);
		}*/

}	
	
