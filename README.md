# Welcome to Raw socket Get 

[Project website](http://david.choffnes.com/classes/cs4700sp15/project4.php)
[Project Documentation](http://charleszhoulll.github.io/complex_socket/)

Introduction
---------------
This program  have basically the same function as wget: downloading webpage according to 
address specified. What is unique of this program is that it use raw socket to 
establish connection with remote server. The challenge part of this program is to take care of 
all the TCP/IP layer work like add header, maintain correct congestion window size,
handle three-way handshake, connection teardown, etc. 



Install and Usage
-----------------
1. Run `make` to install the program

2. Run `sudo ./rawhttpget [website address]` to download web page. The filename would be exactly 
the same as the file name in server. Default name “index.html” is used if there is no filename in server. 

* Important Note: You must have root permission to run the program!

High level approach
-------------------
There are several program files in the project. We briefly introduce the implementation and usage of each file. For more infomation please refer to the documentation of this project.

### RawGet.java

This is the entry of program. In this file we init a RawHttpGet instance and invoke the 
`downloadPage()` function to download page. If download successful, true will be returned, 
otherwise we return false.

### RawHttpGet.java

This can be seen as the application layer of the program. The main function it 
provided is `downloadPage`. `processRequest()` is invoked by `downloadPage` to sent 
request like `Get` or `Post` to the server. After the request is successfully sent, we can use
`getInputStream()` function to get the data received from server and save the data to local file. 

### RockRawSocket.java

This is a class extends from RawSocket. This is the key part of this project. All TCP and IP 
functions were implemented inside this program. We briefly introduce the important functions implemented here:

1. connect(InetAddress, int timeout)

Firstly, Client will try to send SYN packet to server. Port and sequence number are randomly allocated.
After SYN packet is sent we init another socket with combination of `AF_PACKET`,`SOCK_RAW`, and `ETH_P_ALL`
to receive packets. The problem of this socket is that it will receive every packet received by ethernet
card. We will introduce about how to parse the packet received later. After that Client will enter into a while loop to receive packets until it receive SYN/ACK packet sent by the server. Finally client will send another ACK 
packet and the connection is established. Notice that if timeout happens and the connection is still not
established, false will be returned and program will be terminated. 

2. send(String content)

This function is invoked directly by application layer. Content is the string that application try to send. 
T content will be transfomered into byte array  as the packet’s payload. Then this packet was sent after adding   proper TCP header and IP header. Finally we enter into a while loop to wait until all the data sent from server is 
received.

3. sendPacket(int type, byte[] data)

This function send one packet.Here type is the type of packet, like SYN, ACK or FIN. If the packet contain both FIN and ACK, the FIN|ACK is used as signal. Data is the payload of packet. The two most important parameters used in this function is `nextSentSeq` and `nextSentACK`, which is the sequence and ack number of next sent packet. We will introduce how to decide these two values later. 

4. sendPendingPackets()

We have to point out that `sendPacket` does not really send packet. It simply put packet in a send buffer. The real work of sending data is done in this function. Basically this function will try to send as many packets as
possible as long as the cwnd allows. A tricky part here is that after a packet is sent, the packet is put into an unacked packet buffer. Only when this packet is acked we consider the transmission of this 
packet is completed and we get this packet out of buffer.

5. parsePacket(byte[] buffer)

In this function we get the packet out of buffer and decide: is the packet just received  a IP packet? If it is does it come from the target server destination ? If so we get the TCP header of this packet and check its destination port to make sure it belong to the flow we want. 

6. receiverPacket(IPHeader ipheader, TCPHeader tcpheader, byte[] payload)

This function handle the receive packet we received. Notice that before we call this function, the parsePacket
function has already been invoked so that we make sure the packet is valid. Basically, three things have to be
done in this function:

+ Check checksum, packets with wrong checksum will be discarded immediately
+ Invoke HandleACK() function to check its ACK to decide whether it is a new ACK, duplicate ACK or old ACK. Setting correct sequence number of next sent packet. If it is a new ACK number, move all the acked packets in unacked buffer and increaes correct cwnd accordingly. Also, check the timestamp of the packet that has not been acked to see if there is any timeout. If there is, reset cwnd to be 1 and resent the timeout packet. 
+ Check its sequence number to see if it is an inorder packet or out of order packet. Setting correct ack number according to it. If it is in order packet then store the data inside the buffer. Otherwise store the packet in another outorder packet buffer.  Notice that we will try to extract all the in order packets in the buffer when one in order packet comes. 
+ Send back ACP packet with proper sequence number and ACK number.

### IPHeader.java and TCPHeader.java:

These two files are encapsulation of IP header and TCP header. The basic function is to set and get each field 
of IP and TCP header. Other necessary functions include checksum computation and get header length, etc. 

### Packet.java 

This file provide abstraction of one packet. Its main member are TCP Header, IP header and Payload. Also we include a timestamp inside packet. The value of timestamp is setted when the packet is put into a unacked packet buffer. 
We check the timestamp whenever a new ACK comes to see if there is a timeout event. 

### Transform.java

This class provide all the useful functions to handle the transformation between different formats of data. Like transform string address to bit array or long number, or transform between bit array and byte array. 

### URL. java

This class encapsulate basic functions regarded with the website address, like getting host, path or filename. 


Challenge
---------

There are many challenges in completing this project, but basically the most painful part is that we do not
use python or C which already provide good support to raw socket. As a matter of fact, we have to take care of 
every bit inside the TCP or IP header, which is really super easy to make mistake. The trick we use is to
maintain two separate byte arrays to store the header fields, one in bit and another in byte. The advantage 
of doing so is that it make the field setting much easier. But the disadvantage is that it may consume more 
memory. Other challenges include:

1. Setting up environment

The only library in java that provide the interface to call raw socket function is not that good. It even does 
not provide function to change the socket type, we have to change the library and recompile. Also setting up 
appropriate environment to run the code is also challenging.

2. Set correct sequence number and ack number.

It is also very tricky. The basic idea is that sequence number of sent packet is settled according to the ack 
number of received packet, the ack number of sent packet is settled according to the sequence number of received
packet. It is very easy to make mistake in this, especially that the rule to change sequence/ACK number for SYN,
FIN and data packets have slightly difference. 

3.Checksum

It is also very easy to make mistake about checksum. The result of wrong checksum that your packet is thrown away on the halfway, but you have no idea about this. It is also very hard to see whether your checksum is correct  or not. Fortunatelythen I realized wireshark can handle the checksum automatically, which really saves a lot of time of debugging. 

Test
----
Many tests are did to make sure the program runs well. Here we only generalize some of them:

1. Website downloading test
We use different website to make sure that our program can downloaded them properly.

2. Content consistency test
We compare the file we download and the file wget download to make sure that our program does not miss any byte
of data. Also we print out the sequence of byte that are recorded by the socket, and using a small program to 
make sure that these bytes are in sequence. 

3. TCP behavior test
We using wireshark to see whether our program can correctly send duplicate ACK when receiving out of order 
packet, or correctly close the connection after transmission is finished. 

Teammate collaboration
======================
+ Fan Zhou: system design, code writing, debugging and documentation writing
+ Ajay Kumar Dubey: debugging, testing, setting up environment to make sure program can run on various systems










