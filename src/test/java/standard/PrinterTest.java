package standard;


import org.junit.Test;

import b2tla.analysis.Ast2String;
import b2tla.analysis.MachineContext;
import b2tla.analysis.PrecedenceCollector;
import b2tla.analysis.TLAPrinter;
import b2tla.analysis.Typechecker;
import b2tla.analysis.UnchangedVariablesFinder;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.exceptions.BException;
import de.be4.classicalb.core.parser.node.Start;

public class PrinterTest {

	
	@Test
	public void test3() throws BException {
		String machine = "MACHINE test\n" 
	+ "VARIABLES x,y\n"
				+ "INVARIANT x = 1 & y = 1\n"
				+ "INITIALISATION x,y := 1,1 \n"
				+ "OPERATIONS foo = x := 1\n" 
				+ " ;bar = IF x = 1 THEN x:= 2 ELSE y := 2 END \n"
				+ "END";
		Env env = new Env(machine);
		System.out.println(env.result);
		//assertEquals(3,env.variablesTable.get("foo").size());
	}
	
	
	@Test
	public void test4() throws BException {
		String machine = "MACHINE test\n" + "VARIABLES x,y\n"
				+ "INVARIANT x = 1 & y = 1\n"
				+ "INITIALISATION x,y := 1,1 \n"
				+ "OPERATIONS foo = CHOICE x := 1 OR x:= 2 END\n" 
				+ " ;bar = y := 1 \n"
				+ "END";
		Env env = new Env(machine);
		System.out.println(env.result);
		//assertEquals(3,env.variablesTable.get("foo").size());
	}

	
	@Test
	public void testIF() throws BException {
		String machine = "MACHINE test\n" 
				+ "PROPERTIES 1 = 1 & 2 = 2 or 3 = 3 \n"
				+ "END";
		Env env = new Env(machine);
		System.out.println(env.result);
	}
	
	
	class Env {
		String result;
		public Env(String machine) throws BException {
			
			BParser parser = new BParser("Test");
			Start start = parser.parse(machine, false);
			final Ast2String ast2String2 = new Ast2String();
			start.apply(ast2String2);
			System.out.println(ast2String2.toString());
			System.out.println();
			MachineContext c = new MachineContext(start);
			Typechecker t = new Typechecker(c);
			UnchangedVariablesFinder u = new UnchangedVariablesFinder(c);
			PrecedenceCollector p = new PrecedenceCollector(start, t.getTypes());
//			TLAPrinter printer = new TLAPrinter(c, t, u, p);
//			result = printer.getStringbuilder().toString(); TODO
		}

	}
}