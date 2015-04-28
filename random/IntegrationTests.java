package xtc.random;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;
import xtc.tree.Node;
import java.io.IOException;
import java.util.Scanner;
import java.io.File;
import java.io.FileReader;



//so i think, once we get a working test, we can just make a quick integration test
//for it, so that future things don't break it, this can be done with easy copy and
//pasting
public class IntegrationTests
{
	Translator translate = new Translator();

	//utility method for turning a file to a string
	String readFile(File file) throws IOException {
		return new Scanner(file).useDelimiter("\\A").next();
	}


	//tests. Starting with 001, and proceding down.
	//These should makes sure the output is exactly what the successfulTest copy of the output is
	//useful for making sure old test cases don't break with new stuff
	//if the formating changes, the tests will break, but this is the fastest way
	//to get them going for now
	// @Test 
	// public void test001() throws IOException {
	// 	File file = new File("testFiles/Test001");
	// 	Node test001 = translate.parse(new FileReader(file), file);
	// 	translate.process(test001);
	// 	//file processed. Now test

	// 	//A.cpp
	// 	String expectedValue = readFile(new File("successfulTests/Test001/A.cpp"));
	// 	String actualValue = readFile(new File("output/A.cpp"));
	// 	org.junit.Assert.assertEquals("failure-- A.cpp", actualValue, expectedValue);

	// 	//A.h
	// 	expectedValue = readFile(new File("successfulTests/Test001/A.h"));
	// 	actualValue = readFile(new File("output/A.h"));
	// 	org.junit.Assert.assertEquals("failure-- A.h", actualValue, expectedValue);

	// 	//Main.cpp
	// 	expectedValue = readFile(new File("successfulTests/Test001/main.cpp"));
	// 	actualValue = readFile(new File("output/main.cpp"));
	// 	org.junit.Assert.assertEquals("failure-- main.cpp", actualValue, expectedValue);

	// 	System.out.println("Test001 passing");
	// }


	@Test 
	public void test002() throws IOException {
		File file = new File("testFiles/Test002");
		Node test = translate.parse(new FileReader(file), file);
		translate.process(test);
		//file processed. Now test

		//A.cpp
		String expectedValue = readFile(new File("successfulTests/Test002/A.cpp"));
		String actualValue = readFile(new File("output/A.cpp"));
		org.junit.Assert.assertEquals("failure-- A.cpp", actualValue, expectedValue);

		//A.h
		expectedValue = readFile(new File("successfulTests/Test002/A.h"));
		actualValue = readFile(new File("output/A.h"));
		org.junit.Assert.assertEquals("failure-- A.h", actualValue, expectedValue);

		//Main.cpp
		expectedValue = readFile(new File("successfulTests/Test002/main.cpp"));
		actualValue = readFile(new File("output/main.cpp"));
		org.junit.Assert.assertEquals("failure-- main.cpp", actualValue, expectedValue);

		System.out.println("Test002 passing");
	}

	// @Test
	// public void test022() throws IOException{
	// 	File file = new File("testFiles/Test022");
	// 	Node test = translate.parse(new FileReader(file), file);
	// 	translate.process(test);

	// 	//Main.cpp
	// 	String expectedValue = readFile(new File("successfulTests/Test002/main.cpp"));
	// 	String actualValue = readFile(new File("output/main.cpp"));
	// 	org.junit.Assert.assertEquals("failure-- main.cpp", actualValue, expectedValue);

	// 	System.out.println("Test022 passing");
	// }
	



}
