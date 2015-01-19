package com.cbarr.mindflex;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.Enumeration;
import java.util.Random;

import purejavacomm.*;

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
//        mindflex.initialize();
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
    
    
    public void initialize() {
    	System.out.println( "initializing" );
        try {
        	
            CommPortIdentifier portid = getPortId();
            setupPort(portid);
            setupSockets();
            setupIO();
            port.addEventListener(this);
            port.notifyOnDataAvailable(true);
            while ( true) {
                try { Thread.sleep(100);} catch (Exception ex) { }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void setupPort(CommPortIdentifier portid) 
    		throws UnsupportedCommOperationException, PortInUseException {
    	System.out.println("opening port: " + portid.getName());
        port = (SerialPort) portid.open("MindFlex", TIME_OUT);
        port.setFlowControlMode(
                SerialPort.FLOWCONTROL_XONXOFF_IN+
                SerialPort.FLOWCONTROL_XONXOFF_OUT);
        System.out.println( "Connected on port: " + portid.getName() );
    }
    
    private void setupIO() throws IOException{
      setupInputIO();
      setupOutputIO();
    }
    

    
    private CommPortIdentifier getPortId(){
    	CommPortIdentifier portid = null;
        @SuppressWarnings("rawtypes")
		Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();
            while(portEnum.hasMoreElements()) {
            	portid = (CommPortIdentifier)portEnum.nextElement();
            	if(portid.getName().contains(PORT_NAME) && portid.getName().contains(PORT_TYPE)){
            		break;
            	}
            }
            return portid;
    }
 
    @Override
    public void serialEvent(SerialPortEvent event) {
        try {
            switch (event.getEventType() ) {
                case SerialPortEvent.DATA_AVAILABLE:
                	String inputLine = input.readLine();
                	while(inputLine != null){
                		System.out.println(inputLine);
                		if(inputLine.length() > 0)
                			if(inputLine.charAt(0) == '0'){
                				output.write(inputLine+"\n");
                				System.out.println(inputLine);
                				output.flush();
                			}
                		inputLine = input.readLine();
                	}
                    break;
                default:
                    break;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}