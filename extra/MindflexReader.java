package com.cbarr.mindflex;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Random;

import java.net.*;
 
public class MindflexReader implements SerialPortEventListener {
    SerialPort port = null;
    Socket clientSocket = null;
    ServerSocket serverSocket = null;
 
    private BufferedReader input = null;
    private PrintWriter output = null;
     
    private static final int TIME_OUT = 1000; // Port open timeout
 
    private static final String PORT_TYPE = "tty";
    private static final String PORT_NAME = "Mindflex";
     
    public static void main(String[] args) {
        MindflexReader mindflex = new MindflexReader();
        mindflex.test();
    }
    
    public void test(){
    	try {
    		setupSockets();
    		setupOutputIO();
        	Random r = new java.util.Random();
        	while(true) {
                try { 
                	Thread.sleep(1000);
                	StringBuilder sb = new StringBuilder();
                	sb.append(0+",");
                	sb.append(r.nextInt(100)+",");
                	sb.append(r.nextInt(100)+",");
                	for(int i = 0; i < 7; i++){
                		sb.append(r.nextInt(100000)+",");
                	}
                	sb.append(r.nextInt(100000));
                	String line = sb.toString()+"\n";
                	output.write(line);
    				output.flush();
//    				System.out.println(line);
                } catch (Exception ex) { }
            }
    	} catch(Exception ex){
    		ex.printStackTrace();
    	}
    }
    
    private void setupInputIO() throws IOException{
        input = new BufferedReader(
      		  new InputStreamReader( port.getInputStream() ));
    }
    
    private void setupOutputIO() throws IOException{
    	output = new PrintWriter(clientSocket.getOutputStream(),true);
    }
    
    private void setupSockets() throws IOException {
        System.out.println("setting up server socket on 9999");
        serverSocket = new ServerSocket(9999);
        System.out.println("setting up client socket");
        clientSocket = serverSocket.accept();
        System.out.println("connection with client socket established");
    }
    
    private void setupIO() throws IOException{
      setupInputIO();
      setupOutputIO();
    }
}