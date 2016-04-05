import java.io.*;
import java.lang.*;
import java.net.*;

/**
 * This class sets up a client that will send a file to a server throught a proxy
 */
public class Client {
    
    private String proxyIP;
    private int portNumber;
    private String filePath;
    private File file;
    private byte[] fileInBytes;
    private byte[] theHeader;
    
    /**
     * Constructor for Client
     *
     * @param proxyIP - the IP address of the proxy
     * @param portNumber - the port number of the proxy
     * @param filePath - the path to the file to transfer to server
     */
    public Client(String proxyIP, int portNumber, String filePath) {
        this.proxyIP = proxyIP;
        this.portNumber = portNumber;
        this.filePath = filePath;
    }
    
    
    /**
     * Determines whether or not the filePath is a file
     *
     * @return
     *  -1 = error
     *   0 = noError
     */
    public int isFilePathValid() {
        File file = new File(filePath);
        if (!file.exists()) return -1;
        this.file = file;
        int temp = (int)file.length();
        this.fileInBytes = new byte[temp];
        constructFileByteArray(file);
        createHeader();
        return 0;
    }
    
    
    /**
     * Puts all of the file data into a byte array
     *
     * @param file - the file to send data
     */
    public void constructFileByteArray(File file) {
        try {
            InputStream istream = new FileInputStream(file);
            int i = 0;
            while (i < fileInBytes.length) {
                fileInBytes[i] = (byte)istream.read();
                i++;
            }
        } catch (IOException e) {
            System.out.println("Something bad happened... Program exiting.\n" + e);
            System.exit(0);
        }
    }
    
    
    /**
     * Creates a custom header to send to the server
     */
    public void createHeader() {
        this.theHeader = new String("0\nLength: " + fileInBytes.length + "\nFile Name: " + file.getName() + "\n").getBytes();
    }
    
    
    /**
     * Creates a custom header to send to the server
     */
    public byte[] createPacketData(int seqNum, int offset, int packetLength) {
        byte[] fileChunk = new byte[packetLength+1];
        fileChunk[0] = (byte)seqNum;
        for (int i = 1; i <= packetLength; i++)
            for (int j = offset; j < offset + packetLength; j++)
                fileChunk[i] = fileInBytes[j];
                
        return fileChunk;
    }
    
    
    /**
     * Starts running the client
     */
    public void run() {
        try {
            DatagramSocket clientSocket = new DatagramSocket(12345);
            clientSocket.setSoTimeout(1000); // Timeout is 1 second
            byte[] ackData = new byte[2]; // First byte is sequence number, second is ack (1) or nck (0)
            ackData[0] = 0;
            ackData[1] = 0;

            System.out.printf("Listening on UDP:%s:%d%n", InetAddress.getLocalHost().getHostAddress(), 12345);
            
            // Set up DatagramPackets
            DatagramPacket header = new DatagramPacket(theHeader, theHeader.length, InetAddress.getByName(proxyIP), portNumber);
            DatagramPacket ackPacket = new DatagramPacket(ackData, ackData.length);
            
            // Set up packet splitting
            int offset = 0;
            int packetLength = 256;
            int seqNum = 0;
            
            // Send custom header
            do {
                clientSocket.send(header);
                System.out.println("Sent header");
                try {
                    clientSocket.receive(ackPacket);
                    System.out.println("Received ack");
                } catch (SocketTimeoutException e) {
                    System.out.println("Packet loss, resending header...");
                    continue;
                }
            } while(ackData[1] == 0);
            
            // Send file data
            while (true) {
                // Determine offset
                offset = seqNum * 256;
                
                if (offset + packetLength > fileInBytes.length) { // reset length of packet for last chunk of data
                    packetLength = fileInBytes.length - offset;
                }
                if (offset > fileInBytes.length) break; // break if offset passes final byte of file
                
                // Create data for packet
                createPacketData(seqNum, offset, packetLength);
                
                // Create DatagramPacket and send it to server
                DatagramPacket sendPacket = new DatagramPacket(fileInBytes, offset, packetLength, InetAddress.getByName(proxyIP), portNumber);
                clientSocket.send(sendPacket);
            }
            System.out.println("File sent");
        } catch (Exception e) {
            System.out.println("Something bad happened... Program exiting.\n" + e);
            System.exit(0);
        }
    }
    
}