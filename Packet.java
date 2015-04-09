package rawSocket;


// TODO: Auto-generated Javadoc
// Packet format: IP Header || TCP Header || DATA
/**
 * The Class Packet.
 */
public class Packet
{
	
	/** The packet content. */
	private byte[] packetContent;
	
	/** The Data. */
	private byte[] Data;
	
	/** The total length in bits. */
	private int totalLengthInBits;
	
	/** The total length in bytes. */
	private int totalLengthInBytes;
	
	/** The I pheader. */
	protected IPHeader IPheader;
	
	/** The TC pheader. */
	private TCPHeader TCPheader;
	
	/** The Timestamp. */
	private long Timestamp;
	
	/**
	 * Instantiates a new packet.
	 *
	 * @param dataLength the data length
	 */
	public Packet(int dataLength) // In byte
	{
		totalLengthInBytes = dataLength;
		totalLengthInBits = dataLength * 8;
		Data = new byte[dataLength];
	}

	/**
	 * Sets the tcp header.
	 *
	 * @param tcpHeader the new tcp header
	 */
	public void setTcpHeader(TCPHeader tcpHeader)
	{
		this.TCPheader = tcpHeader;
	}
	
	/**
	 * Sets the IP header.
	 *
	 * @param iPHeader the new IP header
	 */
	public void setIPHeader(IPHeader iPHeader)
	{
		this.IPheader = iPHeader;
	}
	
	/**
	 * Gets the data length in bytes.
	 *
	 * @return the data length in bytes
	 */
	public int getDataLengthInBytes()
	{
		// TODO Auto-generated method stub
		return Data.length;
	}
	
	/**
	 * Sets the data.
	 *
	 * @param data the new data
	 */
	public void setData(byte[] data)
	{
		Data = data;
	}
	
	/**
	 * Sets the timestamp.
	 *
	 * @param timestamp the new timestamp
	 */
	public void setTimestamp(long timestamp)
	{
		this.Timestamp = timestamp;
	}
	
	/**
	 * Gets the timestamp.
	 *
	 * @return the timestamp
	 */
	public long getTimestamp()
	{
		return Timestamp;
	}
	
	/**
	 * Gets the packet length.
	 *
	 * @return the packet length
	 */
	public int getPacketLength()
	{
		return totalLengthInBits/8;
	}
	
	/**
	 * Consturct packet.
	 */
	public void consturctPacket()
	{
		int ipHeaderInBytes = IPheader.getHeaderLengthInBytes();
		int tcpHeaderInBytes = TCPheader.getHeaderLengthInBytes();
		totalLengthInBytes =  ipHeaderInBytes +  tcpHeaderInBytes + Data.length;
		packetContent = new byte[totalLengthInBytes];
		byte [] ipHeader = IPheader.getHeader();
		byte [] tcpHeader = TCPheader.getHeader();
		for (int i = 0; i < ipHeaderInBytes; i++)
		{
			packetContent[i] = ipHeader[i];
		}
		for (int i = ipHeaderInBytes; i < ipHeaderInBytes + tcpHeaderInBytes; i++)
		{
			packetContent[i] = tcpHeader[i - ipHeaderInBytes];
		}
		for (int i = ipHeaderInBytes + tcpHeaderInBytes; i < totalLengthInBytes; i++)
		{
			packetContent[i] = Data[i - ipHeaderInBytes - tcpHeaderInBytes];
		}
	}

	/**
	 * Gets the content.
	 *
	 * @return the content
	 */
	public byte[] getContent()
	{
		return packetContent;
	}

	/**
	 * Gets the TC pheader.
	 *
	 * @return the TC pheader
	 */
	public TCPHeader getTCPheader()
	{
		return TCPheader;
	}
	
	/**
	 * Gets the data.
	 *
	 * @return the data
	 */
	public byte[] getData()
	{
		return Data;
	}

	/**
	 * Gets the i pheader.
	 *
	 * @return the i pheader
	 */
	public IPHeader getIPheader()
	{
		return IPheader;
	}
}
