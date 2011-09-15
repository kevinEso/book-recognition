package org.k53ca;

import java.net.*;
import java.io.*;

/**
 * Server listens for connection from user and assigns jobs to different threads
 * @author hoangtung
 *
 */
public class Server {
	ServerThread st[], reporter;
	
	public static final int MAX_THREAD = 10;
	ServerSocket ss;
	
	/**
	 * Create an instance of server which operates on a specific port
	 * @param port
	 * @throws IOException
	 */
	public Server(int port) throws IOException {
		ss = new ServerSocket (port);
    	ss.setReuseAddress(true);
    	// create a number of thread for handling jobs
    	st = new ServerThread[MAX_THREAD];
    	for(int i = 0; i < MAX_THREAD; ++i) {
    		st[i] = new ServerThread();
    		st[i].start();
    	}
    	// a special thread for reporting error to user
    	reporter = new ServerThread();
    	reporter.start();
	}
	
	/**
	 * Assigns job to a specific thread or return an error message to user if server is too busy 
	 * @param skt
	 * @throws IOException
	 */
	private void assignJob(Socket skt) throws IOException {
		for(int i = 0; i < MAX_THREAD; ++i) {
			// assign job to an idle thread
			if(!st[i].hasJob) {
				st[i].setJob(skt);
				return;
			}
		}
		
		// if server is too busy, return a sorry message
		reporter.responseError("Server is too busy, please try again later");
		skt.close();
	}
	
	/**
	 * Listen for user connections
	 */
	public void listen() {
		try {
			while(true) {
				Socket skt = ss.accept();
				assignJob(skt);
			}
		}
		catch(Exception exc) {
			exc.printStackTrace();
		}
	}
	
	/**
	 * Shutdon standard input and output stream to daemonize the application 
	 * @throws IOException
	 */
	public static void daemonize() throws IOException {
		System.out.close();
		System.in.close();
	}
	
	public static void main(String args[]) {
		if(args != null && args.length > 0) {
			try {
				daemonize();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			Matcher.fetch();
			new Server(Config.getPortNum()).listen();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
