import java.io.*;
import java.lang.*;
import java.net.*;

/**
 * This class sets up a server that will receive a file from a client throught a proxy
 */
public class Server {
    
    private int portNumber;
    private String directoryName;
    private int fileLength;
    private String fileName;
    
    /**
     * Constructor for Server
     *
     * @param portNumber - the port number of the receiver
     * @param directoryName - the directory to save the file in
     */
    public Server(int portNumber, String directoryName) {
        this.portNumber = portNumber;
        this.directoryName = directoryName;
    }
    
    
    /**
     * Creates directory if not already there
     */
    public void checkDirectory() {
        File theDir = new File(directoryName);

        // if the directory does not exist, create it
        if (!theDir.exists()) {
            System.out.println("Creating directory: " + directoryName);
            boolean result = false;

            try{
                theDir.mkdir();
                result = true;
            } 
            catch (SecurityException e) {
                System.out.println("Error in creating directory... Program exiting.\n" + e);
                System.exit(0);
            }        
            if (result) {    
                System.out.println("Directory created!");  
            }
        }
    }
    
    
    /**
     * Gets the length and name of file
     */
    public void determineFileName(byte[] bytes) {
        String header = new String(bytes);
        this.fileLength = Integer.parseInt(header.substring(header.indexOf("Length:") + 8, header.indexOf("\n", header.indexOf("Length:"))));
        this.fileName = header.substring(header.indexOf("File Name:") + 11, header.indexOf("\n", header.indexOf("File Name:")));
    }
    
    
    /**
     * Connects data together and creates the file
     */
    public void createFile() {
        
    }
    
    
    /**
     * Starts running the server
     */
    public void run() {
        try {
            DatagramSocket theServer = new DatagramSocket(portNumber);
            byte[] receiveData = new byte[256];

            System.out.printf("Listening on udp:%s:%d%n", InetAddress.getLocalHost().getHostAddress(), portNumber);     
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            theServer.receive(receivePacket); // Receive custom header
            determineFileName(receiveData);

            while(true) {
                  theServer.receive(receivePacket);
                  String sentence = new String(receivePacket.getData(), 0, receivePacket.getLength());
                  System.out.println("RECEIVED: " + sentence);
                  // now send acknowledgement packet back to sender
//                  InetAddress IPAddress = receivePacket.getAddress();
//                  String sendString = "polo";
//                  byte[] sendData = sendString.getBytes("UTF-8");
//                  DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, receivePacket.getPort());
//                  serverSocket.send(sendPacket);
            }
            createFile();
        } catch (Exception e) {
            System.out.println("Something bad happened... Program exiting.\n" + e);
            System.exit(0);
        }
    }
}