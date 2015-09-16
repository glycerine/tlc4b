package de.tlc4b.tlc.integration;

import static de.tlc4b.util.TestUtil.testString;
import static org.junit.Assert.assertEquals;
import static de.tlc4b.tlc.TLCResults.TLCResult.*;

import org.junit.Test;

public class ArithmeticTest {

	@Test
	public void testModulo() throws Exception {
		String machine = 
				"MACHINE Test\n" 
				+ "PROPERTIES \n"
				+ "3 mod 3 = 0 & 6 mod 3 = 0 & 4 mod 3 = 1 & 3 mod 6 = 3  \n" 
				+ "END";
		assertEquals(NoError, testString(machine));
	}
	
	@Test
	public void testModuloErrorFirstOperandIsNegative() throws Exception {
		String machine = "MACHINE Test\n" + "PROPERTIES -1 mod 1 = 0 \n" + "END";
		assertEquals(WellDefinednessError, testString(machine));
	}
	
	@Test
	public void testModuloErrorSecondOperandIsNegative() throws Exception {
		String machine = "MACHINE Test\n" + "PROPERTIES 1 mod -1 = 0 \n" + "END";
		assertEquals(WellDefinednessError, testString(machine));
	}
	
	@Test
	public void testModuloErrorSecondOperandIsZero() throws Exception {
		String machine = "MACHINE Test\n" + "PROPERTIES 1 mod 0 = 0 \n" + "END";
		assertEquals(WellDefinednessError, testString(machine));
	}
	
	@Test
	public void testDivision() throws Exception {
		String machine = 
				"MACHINE Test\n" 
				+ "PROPERTIES \n"
				+ "3 / 3 = 1 & 4 / 3 = 1 & 0 / 3 = 0 & \n" 
				+ "-1/3 = 0 & 1/-3 = 0 &  -1/-3 = 0 \n" 
				+ "END";
		assertEquals(NoError, testString(machine));
	}
	
	@Test
	public void testDivisionErrorDivisionByZero() throws Exception {
		String machine = "MACHINE Test\n" + "PROPERTIES 1 / 0 = 0 \n" + "END";
		assertEquals(WellDefinednessError, testString(machine));
	}

}
