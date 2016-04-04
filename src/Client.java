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
        this.theHeader = new String("Length: " + fileInBytes.length + "\nFile Name: " + file.getName() + "\n").getBytes();
    }
    
    
    /**
     * Starts running the client
     */
    public void run() {
        try {
            DatagramSocket clientSocket = new DatagramSocket(12345);
            byte[] receiveData = new byte[256];

            System.out.printf("Listening on udp:%s:%d%n", InetAddress.getLocalHost().getHostAddress(), 12345);
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            int offset = 0;
            int packetLength = 256;
            
            // Send custom header
            DatagramPacket header = new DatagramPacket(theHeader, theHeader.length, InetAddress.getByName(proxyIP), portNumber);
            clientSocket.send(header);
            
            while(true) {
                if (offset + packetLength > fileInBytes.length) { // reset length of packet for last chunk of data
                    System.out.println(packetLength);
                    packetLength = fileInBytes.length - offset;
                    System.out.println(packetLength);
                }
                if (offset > fileInBytes.length) break; // break if offset passes final byte of file
                DatagramPacket sendPacket = new DatagramPacket(fileInBytes, offset, packetLength, InetAddress.getByName(proxyIP), portNumber);
                clientSocket.send(sendPacket);
                offset += 256;
                
//                serverSocket.receive(receivePacket);
//                String sentence = new String(receivePacket.getData(), 0, receivePacket.getLength());
//                System.out.println("RECEIVED: " + sentence);
//                // now send acknowledgement packet back to sender
//                InetAddress IPAddress = receivePacket.getAddress();
//                String sendString = "polo";
//                byte[] sendData = sendString.getBytes("UTF-8");
//                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, receivePacket.getPort());
//                serverSocket.send(sendPacket);
            }
        } catch (Exception e) {
            System.out.println("Something bad happened... Program exiting.\n" + e);
            System.exit(0);
        }
    }
    
}