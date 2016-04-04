import java.io.*;
import java.lang.*;

/**
 * Author: Logan Jahnke
 * 810616891
 *
 * This is the client of my new amazing
 * custom transfer layer protocol. This
 * protocol is going to be at war with
 * the popular TCP. Upon full release,
 * I will name the protocol CTP. Packets
 * to be 256 bytes big.
 * 
 * Custom Transfer Protocol  
 */
public class URFTClient {
    
    public static void main(String args[]) throws IOException {
	
        String proxyIP = "";
        int portNumber = 0;
        String filePath = "";

        if (args.length == 6) {
            if (args[0].equals("-s")) {
                proxyIP = args[1];
                portNumber = Integer.parseInt(args[3]);
                filePath = args[5];
            } else {
                System.out.println("java URTFClient -s [Proxy IP] -p [Proxy Port #] -f [File Path]");
                System.exit(0);
            } 
        } else {
            System.out.println("java URTFClient -s [Proxy IP] -p [Proxy Port #] -f [File Path]");
            System.exit(0);
        }
        
        Client urftClient = new Client(proxyIP, portNumber, filePath);
        
        if (urftClient.isFilePathValid() == -1) {
            System.out.println("This file does not exist.\nProgram exiting.");
            System.exit(0);
        } else {
            urftClient.run();
        }

    }

}