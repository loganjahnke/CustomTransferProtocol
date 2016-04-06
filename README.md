# Contributers

__Logan__ *Jahnke*

__Toni-Ann__ *Plowright*


# Description

This is a custom transfer protocol built on top of UDP.
Inside the code, it is known as CTP (Custom Transfer Protocol).
This is not a stop-and-wait protocol. The purpose of this Client/Server
combo is to transfer one file from one location to another without using
TCP. 

### Step-by-Step

The Client sends a header file containing the length of the file to be
sent and the name of the file to be sent. The Server receives the header
file and sends an ack back to the Client. Following, the Client sends all
of the packets to the Server. Afterwards, the Client waits for acks back
from the Server, if it does not receive an ack, it sends back the lost
packets. Once the Server receives all the packets, it constructs the file
and closes the connection.

# Questions and Contact

If you have any questions, please email me at jahnke@uga.edu
