import java.io.*;
import java.lang.*;

/**
 * Author: Logan Jahnke
 * 810616891
 *
 * This is the server of my new amazing
 * custom transfer layer protocol. This
 * protocol is going to be at war with
 * the popular TCP. Upon full release,
 * I will name the protocol CTP. Packets
 * to be 256 bytes big.
 * 
 * Custom Transfer Protocol  
 */
public class URFTServer {
    
    public static void main(String args[]) throws IOException {
	
        int portNumber = 0;
        String directoryName = "";

        if (args.length == 4) {
            if (args[0].equals("-p")) {
                portNumber = Integer.parseInt(args[1]);
                directoryName = args[3];
            } else if (args[0].equals("-o")) {
                portNumber = Integer.parseInt(args[3]);
                directoryName = args[1];
            } else {
                System.out.println("java URTFServer -p [Port #] -o [Directory Name]");
                System.exit(0);
            }
        } else {
            System.out.println("java URTFServer -p [Port #] -o [Directory Name]");
            System.exit(0);
        }

        Server urftServer = new Server(portNumber, directoryName);
        
        urftServer.checkDirectory();
        urftServer.run();
        
    }

}