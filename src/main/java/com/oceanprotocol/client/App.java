package com.oceanprotocol.client;

/**
 * Main class for runnning ocean-java as a standalone application from a .jar file.
 * 
 * May be extended in future to support CLI usage.
 * 
 * @author Mike
 *
 */
public class App {

	public static void main(String[] args) {
		
		System.out.print("Ocean Java Client");
		
		String version = App.class.getPackage().getImplementationVersion();
		if (version==null) {
			System.out.println(" - Development snapshot version");
		} else {
			System.out.println(" - Version "+version);
		}
	}
}
