+-src
+-lib
+-doc
+-build.xml

## Project Description: 
In this project, you are required to design a reliable file transfer protocol and write a client application and a server application that communicate with each other using UDP. In particular, the client needs to contact the server, first pass the file name to be uploaded to the server, and send the file itself to the server. The client-server communication must withstand packet loss and must be able to send multiple packets in a pipeline without waiting for an ACK for each packet before being able to send the next. In other words, no stop-and-wait!

To simulate packet loss, we will test your software using a "transparent proxy" [urft-proxy]. An example of how we will run your code is reported below:

- URFTServer <server port>
$ java URFTServer -p 20000 -o server_dir 

- urft-proxy <loss probability> <proxy port> <server ip> <server port>
$ ./urft-proxy 0.10 10000 10.0.0.2 20000

- urft-client <proxy ip> <proxy port> <file name>
$ java URFTClient -s 10.0.0.1 -p 10000 -f client_dir/file1.txt

Once the server is started, it will wait for file upload requests. The proxy will forward any packets received on port 10000 to the server, and then it forwards the server's response packets back to the client. The client will send a request to upload a file (e.g., "file1.txt") to the proxy, and the proxy will forward this request to the server. Notice that from the client's point of view there is no proxy, meaning that the client acts as if it was directly talking to the server (i.e., as if the program listening on port 10000 was the server itself, and not the proxy). Also, from the point of view of the server, the proxy is the client (i.e., the server does not know that the proxy is forwarding packet on behalf of the client).

## NOTE: 
Packets may be lost in both directions
Max Datagram Size = 512 bytes. This is the maxium length of the UDP payload that you should use.
Max Time-out = 1sec.

## TESTING YOUR CODE
To test your code we will create two directories: "client_dir" and "server_dir". The "client_dir" will contain a number of files that need to be uploaded from the client to the server. The "server_dir" will be used to store the file received from the client. The files uploaded by the client need to be stored inside the "server_dir" directory.

Notice that you can run the server, proxy, and client on the same machine using the loopback address. For example, you can run the proxy and server as:

$ java URFTServer -p 20000 -o server_dir 
$ ./urft-proxy 0.10 10000 127.0.0.1 20000

Once the server and proxy have been launched, you can run the client as

$ java URFTClient -s 127.0.0.1 -p 10000 -f client_dir/file1.txt

where 127.0.0.1 and 10000 are the IP and UDP port of the proxy, respectively. In this example, the file "file1.txt" will be split into datagrams and sent to the server, which will store it in the "server_dir" directory (i.e., the same dir from which the "urft-server" is launched). You can then compare the md5sum of "client_dir/file1.txt" and "server_dir/file1.txt" to make sure they match!

