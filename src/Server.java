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
    private SequenceConverter convert;
    
    /**
     * Constructor for Server
     *
     * @param portNumber - the port number of the receiver
     * @param directoryName - the directory to save the file in
     */
    public Server(int portNumber, String directoryName) {
        this.portNumber = portNumber;
        this.directoryName = directoryName;
        this.convert = new SequenceConverter();
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
     * Quits program no matter the outcome
     *
     * @param dataKeeper - byte[][] that stores all data for each sequence number
     */
    public void createFile(byte[][] dataKeeper) {
        try {
            File file = new File("" + directoryName + "/" + fileName);
            file.createNewFile();
            FileOutputStream ostream = new FileOutputStream(file, true);
            for (int i = 0; i < dataKeeper.length; i++) {
                ostream.write(dataKeeper[i], 0, dataKeeper[i].length);
            }
            System.out.println("File received from client and copied to machine.\nProgram exiting.");
            System.exit(0);
        } catch (Exception e) {
            System.out.println("Error when creating file.\n Program exiting.");
            System.exit(0);
        }
    }
    
    
    /**
     * Starts running the server
     */
    public void run() {
        try {
            DatagramSocket theServer = new DatagramSocket(portNumber);
            theServer.setSoTimeout(1000); // Timeout = 1 second
            byte[] receiveData = new byte[264]; // Array to store packet in
            byte[] ackData = new byte[2]; // First byte is the sequence number of packet, second is ack (1)
            ackData[0] = 0;
            ackData[1] = 1;
            byte[] nckData = new byte[2]; // First byte is the sequence number of packet, second is nck (0)
            nckData[0] = 0;
            nckData[1] = 0;
            byte[] ackChecker; // For checking acks received: (0) = not received, (1) = received
            byte[][] dataKeeper; // For storing file data

            System.out.printf("Listening on CTP"); 
            
            // Create DatagramPackets
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            DatagramPacket ackPacket = new DatagramPacket(ackData, ackData.length);
            DatagramPacket nckPacket = new DatagramPacket(nckData, nckData.length);
            
            // Receive Custom Header
            while (true) {
                try {
                    theServer.receive(receivePacket);
                    System.out.println("Received header\nDetermining how many packets will be received...");
                    break;
                } catch (SocketTimeoutException e) {
                    continue;
                }
            }
            
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
            
            // Initialize ackChecker and dataKeeper after given header packet
            int numOfChunks = fileLength / 256;
            if (numOfChunks % 256 != 0) numOfChunks++;
            dataKeeper = new byte[numOfChunks][];
            ackChecker = new byte[numOfChunks];
            
            int seqNum = 0;
            int numAcksGood = 0;

            // Loop until all data is collected
            while (true) {
                
                // Receive file data
                while (true) {
                    try {
                        // Receive Packets and store data into dataKeeper
                        theServer.receive(receivePacket);
                        byte[] seqChunk = new byte[4];
                        byte[] lengthChunk = new byte[4];
                        for (int i = 0; i < 4; i++)
                            seqChunk[i] = receiveData[i];
                        for (int i = 4; i < 8; i++)
                            lengthChunk[i-4] = receiveData[i];
                        int index = convert.byteToInt(seqChunk);
                        int plength = convert.byteToInt(lengthChunk);
                        dataKeeper[index] = new byte[plength];
                        for (int i = 8; i <= plength+7; i++)
                            dataKeeper[index][i-8] = receiveData[i];
                        ackChecker[index] = 1;
                    } catch (SocketTimeoutException e) {
                        System.out.println("Moving on... Sending acks");
                        break;
                    }
                }
                
                seqNum = 0;
                numAcksGood = 0;
                
                // Send acks
                while (seqNum < ackChecker.length) {
                    System.out.println("Sending ack: " + seqNum + ":" + ackChecker[seqNum] + " of " + ackChecker.length + " acks.");
                    ackData[0] = (byte)seqNum;
                    ackData[1] = ackChecker[seqNum];
                    seqNum++;
                    ackPacket.setData(ackData, 0, ackData.length);
                    theServer.send(ackPacket);
                }
                
                for (int i = 0; i < numOfChunks; i++) {
                    if (ackChecker[i] == 1) numAcksGood++;
                }
                if (numAcksGood == numOfChunks) createFile(dataKeeper); // If all packets are here, createFile
                
            }
            
        } catch (Exception e) {
            System.out.println("Something bad happened... Program exiting.\n" + e);
            System.exit(0);
        }
    }
}