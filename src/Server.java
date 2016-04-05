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
     *
     * @return 
     *  - 0 for failure (wrong packet received)
     *  - 1 for success
     */
    public int determineFileName(byte[] bytes) {
        String header = new String(bytes);
        if (header.indexOf("Length:") == -1) return 0;
        this.fileLength = Integer.parseInt(header.substring(header.indexOf("Length:") + 8, header.indexOf("\n", header.indexOf("Length:"))));
        this.fileName = header.substring(header.indexOf("File Name:") + 11, header.indexOf("\n", header.indexOf("File Name:")));
        return 1;
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
            byte[] receiveData = new byte[256]; // Array to store packet in
            byte[] ackData = new byte[2]; // First byte is the sequence number of packet, second is ack (1)
            ackData[0] = 0;
            ackData[1] = 1;
            byte[] nckData = new byte[2]; // First byte is the sequence number of packet, second is nck (0)
            nckData[0] = 0;
            nckData[1] = 0;

            System.out.printf("Listening on UDP:%s:%d%n", InetAddress.getLocalHost().getHostAddress(), portNumber); 
            
            // Create DatagramPackets
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            DatagramPacket ackPacket = new DatagramPacket(ackData, ackData.length);
            DatagramPacket nckPacket = new DatagramPacket(nckData, nckData.length);
            
            // Receive Custom Header
            theServer.receive(receivePacket);
            System.out.println("Received header\nDetermining how many packets will be received...");
            
            // Bind port and ip to ack and nck
            ackPacket.setPort(receivePacket.getPort());
            nckPacket.setPort(receivePacket.getPort());
            ackPacket.setAddress(receivePacket.getAddress());
            nckPacket.setAddress(receivePacket.getAddress());
            
            // Check for failure
            while (determineFileName(receiveData) == 0) {
                theServer.send(nckPacket);
                theServer.receive(receivePacket);
            }
            
            // Send ack
            theServer.send(ackPacket);

            while (true) {
                // Receive Packets
                theServer.receive(receivePacket);
                // now send acknowledgement packet back to sender
                String ackString = "A";
                byte[] ack = ackString.getBytes();
                DatagramPacket sendACK = new DatagramPacket(ack, ack.length, receivePacket.getAddress(), receivePacket.getPort());
                theServer.send(sendACK);
            }
            //createFile();
        } catch (Exception e) {
            System.out.println("Something bad happened... Program exiting.\n" + e);
            System.exit(0);
        }
    }
}