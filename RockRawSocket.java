package rawSocket;

import java.io.*;
import java.net.*;
import java.util.*;

import com.savarese.rocksaw.net.RawSocket;

// TODO: Auto-generated Javadoc
/**
 * The Class RockRawSocket.
 */
public class RockRawSocket extends RawSocket
{
	
	/** The server address. */
	private InetAddress serverAddress;
	
	/** The client address. */
	private InetAddress clientAddress;
	
	/** The source port. */
	private int srcPort;
	
	/** The raw socket used to receive data. */
	private RawSocket sock_receive;
	
	/** The IP header received packet. */
	IPHeader ipHeaderRecv;
	
	/** The TCP header of received packet. */
	TCPHeader tcpHeaderRecv;
	
	/** The congestion window size, initial value is 1. */
	private int cwnd = 1; 
	
	/** The maximum segment size: 1460 byte. */
	private int MSS = 1460; 
	
	/** The sender buffer. */
	Queue<Packet> senderBuffer = new LinkedList<Packet>();
	
	/** The unacked buffer, all sent (unacked) packets are placed in this buffer. */
	Queue<Packet> unackedBuffer = new LinkedList<Packet>();
	
	/** The receiver packet buffer. */
	Queue<Packet> receiverBuffer = new LinkedList<Packet>();
	
	/** The output stream, received data (including HTTP response header) were stored in this stream. */
	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	
	/**  The input stream, used by application to read data received. */
	ByteArrayInputStream inputStream;
	
	/**  Sequence of the highest received in order packet, whenever receive a new in order packet, update accumulateACK according to the  	packet's sequence and tcp segment length. */
	long accumulateACK = 0;
	
	/** ACK of the next sent packet, normally this is always accumulateACK + 1. */
	long nextSentACK = 0;
	
	/** Sequence number of the next sent packet, update according to the ACK of received packet. */
	long nextSentSeq = 0;
	
	/** The signal of SYN packet. */
	private int SYN = 0x1;
	
	/** The signal ACK packet. */
	private int ACK = 0x10;
	
	/** The signal FIN packet. */
	private int FIN = 0x100;

	/**
	 * Connect, handle all the three way handshake of a TCP connection.
	 *
	 * @param address the address that you want to connect
	 * @param timeout the timeout
	 * @return true, if successful, otherwise false
	 */
	public boolean connect(InetAddress address, int timeout)
	{
		serverAddress = address;
		clientAddress = getLocalEth0Address();
		if (clientAddress == null)
		{
			System.out.println("Cannot find local IP address!");
			return false;
		}
		srcPort = randomNumber(1025, 65535); // Return a random number between 1025 and 65535
		nextSentSeq = randomNumber(0, 65535); // Initial sequence number
		sendPacket(SYN, null);
		// Create a socket to receive data
		sock_receive = new RawSocket();
		try
		{
			sock_receive.open(RawHttpGet.AF_PACKET, RawHttpGet.SOCK_RAW, RawHttpGet.ETH_P_ALL);
			long begin = System.currentTimeMillis(); 
			while (System.currentTimeMillis() -  begin < timeout)
			{
				byte[] buffer = new byte[65535]; // Buffer to store one packet, should be big enough
				int dataSize = sock_receive.read(buffer, null);
				if (dataSize < 0)
				{
					System.out.println("Recvfrom error , failed to get packets\n");
					return false;
				}
				else
				{
					if (parsePacket(buffer)) // Check to see if this is the packet we want, if it is, get IP header and tcpheader
					{
						byte[] payload = getData(buffer, ipHeaderRecv, tcpHeaderRecv);
						// Handle the received packet
						receiverPacket(ipHeaderRecv, tcpHeaderRecv, payload);
						return true;
					}
				}
			// Now process the packet
			}
			System.out.println("Timeout! Cannot open socket or establish connection");
			return false;
		}
		catch (IllegalStateException | IOException e)
		{
			System.out.println("Cannot open socket or establish connection");
			return false;
		}
	}

	/**
	 * receiverPacket, When receive packet, we do following process:
	 * 
	 * (1) Check checksum, packet with wrong checksum will be discarded immediately
	 * (2) Handle the ACK of received packet, decide 
	 *     2.1 Is it a new ACK?
	 *     2.2 Is it a duplicate ACK?
	 *     2.3 Is it a ACK that outdate?
	 * (3) Handle the Seq of received packet, decide 
	 * 	   3.1 Is it a out of order packet?
	 * 	   3.2 Is it a in order packet?
	 * (4) Decide the ACK number and sequence number of next sent packet, put the new packet in send buffer
	 * @param ipHeaderRecv the IP header of received packet
	 * @param tcpHeaderRecv the TCP header received packet
	 * @param payload the data of received packet
	 */
	private void receiverPacket(IPHeader ipHeaderRecv, TCPHeader tcpHeaderRecv, byte[] payload)
	{
		int tcpSegmentLength = payload.length;
		long receivedACK = tcpHeaderRecv.getAckNumber();
		long receivedSeq = tcpHeaderRecv.getSequenceNumber();
		if (!ipHeaderRecv.verifyChecksum() && tcpHeaderRecv.verifyChecksum(ipHeaderRecv, payload))
		{
			System.out.println("Checksum incorrect!");
			return;
		}
		if (!HandleACK(receivedACK))
		{
			System.out.println("Received incorrect ACK! Maybe the connection is resetted");
			return;
		}
		else if (tcpSegmentLength > 0 || tcpHeaderRecv.getSYN() == 1 || tcpHeaderRecv.getFIN() == 1)
		{
			if (isInOrder(receivedSeq))
			{
				try
				{
					outputStream.write(payload);
				}
				catch (IOException e)
				{
					System.out.println("Fail to write data!");
				}
				// Very subtle here
				if (tcpHeaderRecv.getSYN() == 1)
				{
					accumulateACK = tcpHeaderRecv.getSequenceNumber();
				}
				else if (tcpHeaderRecv.getFIN() == 1)
				{
					accumulateACK = tcpHeaderRecv.getSequenceNumber() + tcpSegmentLength;
				}
				else
				{
					accumulateACK = tcpHeaderRecv.getSequenceNumber() + tcpSegmentLength - 1;
				}
				nextSentACK = accumulateACK + 1;
				// If receive buffer is not empty, get all the in order packets' data and store them into outputstream
				while (!receiverBuffer.isEmpty())
				{
					Packet headPacket = receiverBuffer.peek();
					TCPHeader tcp = headPacket.getTCPheader();
					IPHeader ip = headPacket.getIPheader();
					byte[] data = headPacket.getData();
					// Is next packet in order?
					if (tcp.getSequenceNumber() == accumulateACK + 1)
					{
						try
						{
							outputStream.write(data);
						}
						catch (IOException e)
						{
							System.out.println("Fail to write data!");
						}
						accumulateACK = tcp.getSequenceNumber() + getTcpSegmentLength(ip, tcp) - 1;
						nextSentACK = accumulateACK + 1;
						receiverBuffer.remove();
					}
					// Another packet loss in the same window, send back duplicate ACK
					else if (tcp.getSequenceNumber() > accumulateACK + 1)
					{
						break;
					}
					// If the sequence number is even smaller then this, duplicate packet, discard, return directly
					else if (tcp.getSequenceNumber() < accumulateACK + 1)
					{
						return;
					}
				}
			}
			else
			{
				// If this packet's sequence number is even lower then accumulative ACK, duplicate packet, discard it
				if (tcpHeaderRecv.getSequenceNumber() < accumulateACK)
					return;
				Packet outOfOrderPkt = new Packet(payload.length);
				outOfOrderPkt.setIPHeader(ipHeaderRecv);
				outOfOrderPkt.setTcpHeader(tcpHeaderRecv);
				outOfOrderPkt.setData(payload);
				outOfOrderPkt.consturctPacket();
				receiverBuffer.add(outOfOrderPkt);
			}
			sendPacket(ACK, null);
		}
	}

	/**
	 * isInOrder, Checks if the received sequence number in order.
	 *
	 * @param receivedSeq the sequence number of received packet
	 * @return true, if is in order
	 */
	private boolean isInOrder(long receivedSeq)
	{
		if (tcpHeaderRecv.getSYN() == 1)
			return true;
		else
			return receivedSeq == accumulateACK + 1;
	}

	/**
	 * HandleACK, Ideally, whenever sender send a packet with payload, this packet was kept
	 * in unACKed buffer, and was removed when got ACK. Then we reset the sequence number of next sent packet.
	 * But two situations must be taken care of: 
	 * (1) What if the packet has data, but it is lost, hence there will be no ACK for it 
	 * (2) What if several packets has data got lost, and after retransmit client send you a new ack that
	 * 	   are larger than the head sequence seq
	 * So the process to reset sequence number is:
	 * (1) Decide the ideal ACK number, it should be the highest unacked packet's sequence number + highest unacked packet's TCP segment length
	 * (2) Compare the ACK number of received packet and ideal packet, if 
	 * (3) receivedACK == idealACK && tcpHeaderRecv.getSYN() != 1, perfect situation, we ack new packet. Increase cwnd and remove the packet from buffer, set 
	 *     the sequence number to be ACK number of received packet
	 * (4) receivedACK == idealACK + 1 this would happen in FIN stage, we do not have to increase cwnd in this case
	 * (5) receivedACK > idealACK, this may means network just recover from a bursty loss. We moved all the acked packet out of unacked buffer  
	 * @param receivedACK the ACK number of received packet
	 * @return true, if successful
	 */
	private boolean HandleACK(long receivedACK)
	{
		if (unackedBuffer.isEmpty())
		{
			nextSentSeq = tcpHeaderRecv.getAckNumber();
			return true;
		}
		long idealACK = HighestUnAckedPacketSeq() + HighestUnAckedPacketLength();
		if ((receivedACK == idealACK && tcpHeaderRecv.getSYN() != 1))
		{
			unackedBuffer.remove();
			cwnd = Math.min(cwnd + MSS, 1000 * MSS);
			nextSentSeq = tcpHeaderRecv.getAckNumber();
			return true;
		}
		else if ((receivedACK == idealACK + 1 && tcpHeaderRecv.getFIN() == 1))
		{
			unackedBuffer.remove();
			nextSentSeq = tcpHeaderRecv.getAckNumber();
			return true;
		}
		else if (receivedACK > idealACK && tcpHeaderRecv.getSYN() != 1)
		{
			// This happens when just recover from bursty loss. The ACK of new
			// received packet would be able to ack a lot of packets
			while (!unackedBuffer.isEmpty())
			{
				unackedBuffer.remove();
				idealACK = HighestUnAckedPacketSeq() + HighestUnAckedPacketLength();
				if (receivedACK >= idealACK)
					break;
			}
			return true;
		}
		else
			return false;
	}

	/**
	 * HighestUnAckedPacketLength, Get TCP segment length of first unacked packet.
	 *
	 * @return TCP segment length of first unacked packe
	 */
	private int HighestUnAckedPacketLength()
	{
		Packet headPacket = (Packet) unackedBuffer.peek();
		return getTcpSegmentLength(headPacket.getIPheader(), headPacket.getTCPheader());
	}

	/**
	 * randomNumber, Random number.Generate random number between [min, max]
	 *
	 * @param min the min
	 * @param max the max
	 * @return the random number generated
	 */
	private int randomNumber(int min, int max)
	{
		Random rand = new Random();
		return rand.nextInt((max - min) + 1) + min;
	}

	/**
	 * sendPacket, Send a packet.Notice here we do not actually send packet, we just put it into send packet buffer
	 * The real sending work is handled by sendPendingPackets()
	 *
	 * @param type the type of sent packet, SYN, FIN, or ACK
	 * @param data the payload of sent packet
	 */
	private void sendPacket(int type, byte[] data)
	{
		Packet packetSent;
		if (data == null)
			packetSent = new Packet(0);
		else
		{
			packetSent = new Packet(data.length);
			packetSent.setData(data);
		}
		IPHeader ipHeaderSent = new IPHeader(20);
		ipHeaderSent.SetDestAddress(serverAddress.getHostAddress());
		ipHeaderSent.SetSrcAddress(clientAddress.getHostAddress());
		TCPHeader tcpHeaderSent = new TCPHeader(20);
		tcpHeaderSent.setSrcPort(srcPort);
		if (type == SYN)
		{
			tcpHeaderSent.SetSYN(1);
		}
		else if (type == ACK)
		{
			tcpHeaderSent.SetACK(1);
		}
		else if (type == FIN)
		{
			tcpHeaderSent.SetFIN(1);
		}
		tcpHeaderSent.SetSeqNumber(nextSentSeq);
		tcpHeaderSent.SetAckNumber(nextSentACK);
		ipHeaderSent.SetTotalLength(packetSent.getDataLengthInBytes(),
				tcpHeaderSent.getHeaderLengthInBytes());
		// Interesting: NAT can set IP checksum automatically for you. But have to handle TCP checksum yourself
		ipHeaderSent.SetChecksum(ipHeaderSent.computeCheckSum());
		tcpHeaderSent
				.SetChecksum(tcpHeaderSent.computeChecksum(ipHeaderSent, packetSent.getData()));
		packetSent.setIPHeader(ipHeaderSent);
		packetSent.setTcpHeader(tcpHeaderSent);
		packetSent.consturctPacket();
		senderBuffer.add(packetSent);
		sendPendingPackets();
	}

	/**
	 * Send pending packets. Here is where we try to send as much as the packet under the limitation of cwnd
	 */
	private void sendPendingPackets()
	{
		try
		{
			while (!senderBuffer.isEmpty())
			{
				// Have not received any ACK, which means it is a SYN
				if (accumulateACK == 0)
				{
					Packet packetSent = (Packet) senderBuffer.remove();
					this.write(serverAddress, packetSent.getContent());
					return; 
				}
				else if (HighestUnSentPacketSeq() - accumulateACK <= cwnd * MSS)
				{
					Packet packetSent = (Packet) senderBuffer.remove();
					if (packetSent.getData().length != 0)
					{
						packetSent.setTimestamp(System.currentTimeMillis());
						unackedBuffer.add(packetSent);
					}
					this.write(serverAddress, packetSent.getContent());
				}
			}
		}
		catch (IOException e)
		{
			System.out.println("Send packet error!");
			System.exit(-1);
		}
	}

	/**
	 * Packets must be acked in order. All unackedd packets will be stored in the buffer.
	 * This function will check which is the oldest un acked packet
	 *
	 * @return the sequence number of highest unacked packet
	 */
	private long HighestUnAckedPacketSeq()
	{
		Packet headPacket = (Packet) unackedBuffer.peek();
		return headPacket.getTCPheader().getSequenceNumber();
	}

	/**
	 * Packets must also be sent in order. All unsent packets will be stored in unsent packet buffer
	 * first. So this function check which is the next to be sent packet
	 *
	 * @return the sequence number of next to be sent packet.
	 */
	private long HighestUnSentPacketSeq()
	{
		Packet headPacket = (Packet) senderBuffer.peek();
		return headPacket.getTCPheader().getSequenceNumber();
	}

	/**
	 * Gets the local eth0 address.
	 *
	 * @return the local eth0 address
	 */
	private static InetAddress getLocalEth0Address()
	{
		try
		{
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements())
			{
				NetworkInterface i = (NetworkInterface) interfaces.nextElement();
				for (Enumeration<InetAddress> addresses = i.getInetAddresses(); addresses
						.hasMoreElements();)
				{
					InetAddress addr = (InetAddress) addresses.nextElement();
					if (!addr.isLoopbackAddress())
					{
						if (addr instanceof Inet4Address)
						{
							return addr;
						}
					}
				}
			}
		}
		catch (SocketException e)
		{
			return null;
		}
		return null;
	}

	/**
	 * Parses the packet.Identify if the packet is the packet we are interested.
	 * The process is: 
	 * (1) Check if it is a IP packet,if is, check source address
	 * (2) If it is packet come from target server, get TCP header
	 * (3) Check destination port, to see if it is the flow we want
	 *
	 * @param buffer the buffer
	 * @return true, if successful
	 */
	private boolean parsePacket(byte[] buffer)
	{
		int ipProtocolVersion = buffer[14] >> 4;
		int tcpProtocolVersion = buffer[23];
		if (ipProtocolVersion == 4 && tcpProtocolVersion == 6)
		{
			ipHeaderRecv = getIpHeader(buffer);
			if (ipHeaderRecv.getSrcAddress() == Transform.addressToLong(serverAddress
					.getHostAddress()))
			{
				tcpHeaderRecv = getTCPHeader(buffer, ipHeaderRecv);
				if (tcpHeaderRecv.getDestPort() == srcPort)
					return true;
			}
		}
		return false;
	}

	/**
	 * Gets the TCP header.
	 *
	 * @param buffer the buffer
	 * @param ipHeaderRecv the ip header recv
	 * @return the TCP header
	 */
	private TCPHeader getTCPHeader(byte[] buffer, IPHeader ipHeaderRecv)
	{
		int startIndex = 14 + ipHeaderRecv.getHeaderLengthInBytes();
		int tcpHeaderLength = 4 * ((buffer[startIndex + 12] >> 4) & 0xF);
		byte[] headerContent = new byte[tcpHeaderLength];
		for (int i = 0; i < tcpHeaderLength; i++)
		{
			headerContent[i] = buffer[startIndex + i];
		}
		TCPHeader header = new TCPHeader(headerContent);
		return header;
	}

	/**
	 * Gets the ip header.
	 *
	 * @param buffer the buffer that stores data
	 * @return the IP header of received packet
	 */
	private IPHeader getIpHeader(byte[] buffer)
	{
		byte versionIHL = buffer[14];
		int ipHeaderLength = 4 * (versionIHL & 0xF);
		byte[] headerContent = new byte[ipHeaderLength];
		for (int i = 0; i < ipHeaderLength; i++)
		{
			headerContent[i] = buffer[i + 14];
		}

		IPHeader header = new IPHeader(headerContent);
		return header;
	}

	/**
	 * THis function used to send data and also store the received data (response)
	 * in receive buffer.
	 *
	 * @param content the content
	 * @return true, if successful
	 */
	public boolean Send(String content)
	{
		byte[] data = content.getBytes();
		sendPacket(ACK, data);
		while (true)
		{
			byte[] buffer = new byte[65535];
			try
			{
				int dataSize = sock_receive.read(buffer, null);
				CheckTimeOut();
				if (dataSize < 0)
				{
					System.out.println("Recvfrom error , failed to get packets\n");
					return false;
				}
				else
				{
					if (parsePacket(buffer))
					{
						byte[] payload = getData(buffer, ipHeaderRecv, tcpHeaderRecv);
						receiverPacket(ipHeaderRecv, tcpHeaderRecv, payload);
						// If receive FIN, return
						if (tcpHeaderRecv.getFIN() == 1)
						{
							return true;
						}
					}
				}
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				return false;
			}
		}
	}

	/**
	 * Check time out.
	 */
	private void CheckTimeOut()
	{
		if (!unackedBuffer.isEmpty())
		{
			long nowTime = System.currentTimeMillis();
			Packet unackedPkt = unackedBuffer.peek();
			if (nowTime - unackedPkt.getTimestamp() > 60000)
			{
				nextSentSeq = unackedPkt.getTCPheader().getSequenceNumber();
				nextSentACK = unackedPkt.getTCPheader().getAckNumber();
				sendPacket(ACK, unackedPkt.getData());
			}
		}
		else
			return;
	}

	/**
	 * Gets the data from received data in buffer.
	 *
	 * @param buffer the buffer which stores the packet
	 * @param ipHeader the IP header of received packet
	 * @param tcpHeader the TCP header of received packet
	 * @return the payload of received packet
	 */
	private byte[] getData(byte[] buffer, IPHeader ipHeader, TCPHeader tcpHeader)
	{
		int startIndex = 14 + ipHeader.getHeaderLengthInBytes()
				+ tcpHeader.getHeaderLengthInBytes();
		// int endIndex = 14 + ipHeader.getTotalLength();
		int dataLength = ipHeader.getTotalLength() - ipHeader.getHeaderLengthInBytes()
				- tcpHeader.getHeaderLengthInBytes();
		byte[] payload = new byte[dataLength];
		for (int i = 0; i < dataLength; i++)
		{
			payload[i] = buffer[i + startIndex];
		}
		return payload;
	}

	/**
	 * Gets the tcp segment length.
	 *
	 * @param ipHeader the IP header of received packet
	 * @param tcpHeader the TCP header of received packet
	 * @return the tcp segment length of received packet
	 */
	private int getTcpSegmentLength(IPHeader ipHeader, TCPHeader tcpHeader)
	{
		int totalLength = ipHeader.getTotalLength();
		int tcpSegmentLength = totalLength - ipHeader.getHeaderLengthInBytes()
				- tcpHeader.getHeaderLengthInBytes();
		return tcpSegmentLength;
	}

	/**
	 * Gets the input stream.
	 *
	 * @return the input stream
	 */
	public InputStream getInputStream()
	{
		byte[] byteArray = outputStream.toByteArray();
		inputStream = new ByteArrayInputStream(byteArray);
		return inputStream;
	}
}
