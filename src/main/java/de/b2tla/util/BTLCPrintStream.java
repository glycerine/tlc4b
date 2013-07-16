package de.b2tla.util;

import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import de.b2tla.B2TLAGlobals;


public class BTLCPrintStream extends PrintStream {
	private final PrintStream console;
	private final ArrayList<String> array;
	public BTLCPrintStream() {
		super(new PipedOutputStream());
		this.console = System.out;
		this.array = new ArrayList<String>();
	}
	
	public void resetSystemOut(){
		System.setOut(console);
	}
	
	public String[] getArray(){
		return array.toArray(new String[array.size()]);
	}
	
	public ArrayList<String> getArrayList(){
		return array;
	}
	
	@Override
	public void println(String str){
		synchronized (BTLCPrintStream.class){
			if(!B2TLAGlobals.isTool()){
				console.println(str);
			}
			array.add(str);
		}
	}
	
	@Override
	public void print(String str){
		synchronized (BTLCPrintStream.class){
			if(!B2TLAGlobals.isTool()){
				console.println(str);
			}
			console.println(str);
			array.add(str);
		}
	}
	
	@Override
	public void print(Object obj){
		synchronized (BTLCPrintStream.class){
			console.println(obj.toString());
			array.add(obj.toString());
		}
	}
	
	
}
