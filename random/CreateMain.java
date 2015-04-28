package xtc.random;

import java.lang.StringBuilder;
import java.io.File;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

public class CreateMain
{
	String directoryName;
	StringBuilder sb = new StringBuilder();

	public CreateMain(String directoryName)
	{
		this.directoryName = directoryName;
		createString(); 
		try{
			toFile();
		} catch (IOException e)
		{
			System.out.println("IOException making main.cpp:" + e);
		}
	}

	void createString()
	{
		sb.append("#include \"java_lang.h\"\n#include \""+ directoryName +".h\"\n");
		sb.append("using namespace java::lang;\n\n");
		sb.append("int main(int argc, char* argv[])\n");
		sb.append("{\n");
		sb.append("\t__rt::Ptr<__rt::Array<String> > args = new __rt::Array<String>(argc-1);\n");
		sb.append("\tfor (int32_t i = 1; i < argc; i++)\n\t{\n");
		sb.append("\t\t(*args)[i-1] = __rt::literal(argv[i]);\n");
		sb.append("\t}\n");
		sb.append("\t__"+directoryName+"::main(args);\n");
		sb.append("\treturn 0; \n");
		sb.append("}");
		
	}

	void toFile() throws IOException
			{
				System.out.println("Creating Main CPP File main.cpp");
				File fout = new File("output/"+ directoryName + "/main.cpp");
     			FileOutputStream fos = new FileOutputStream(fout);
     			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

      			bw.write(sb.toString());
      			bw.close();
      			
			}

	public String toString()
	{
		return sb.toString();
	}
	


}