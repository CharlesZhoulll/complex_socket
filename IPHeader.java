package rawSocket;

// TODO: Auto-generated Javadoc
/**
 * The Class IPHeader.
 */
public class IPHeader
{
	
	/** The ip header. */
	private byte[] ipHeader;
	
	/** The ip header in bits. */
	private byte[] ipHeaderInBits;
	
	/** The length in bits. */
	private int lengthInBits;
	
	/** The length in bytes. */
	private int lengthInBytes;
	
	// Header field
	/** The Version. */
	private int Version = 4; 
	
	/** The ihl. */
	private int IHL = 5; // range [5, 15]Word (4 Byte, 32 bit)
	
	/** The dscp. */
	private int DSCP = 0; 
	
	/** The ecn. */
	private int ECN = 0; 
	
	/** The Total length. */
	private int TotalLength;  //include ip header, tcp header and data, range [20, 65535]Byte
	
	/** The Identification. */
	private int Identification = 0; // For fragmentation
	
	/** The Flags. */
	private int Flags = 2; // 010, do not fragment
	
	/** The Fragment offset. */
	private int FragmentOffset = 0;
	
	/** The ttl. */
	private int TTL = 64; // range [1, 127] Seconds
	
	/** The Protocol. */
	private int Protocol = 6; // TCP. full list http://en.wikipedia.org/wiki/List_of_IP_protocol_numbers
	
	/** The Checksum. */
	private int Checksum = 0; 
	
	/** The Src address. */
	private long SrcAddress;
	
	/** The Dest address. */
	private long DestAddress;
	
	/** The Options. */
	private int Options;
	
	/**
	 * Instantiates a new IP header.
	 *
	 * @param length the length
	 */
	public IPHeader(int length) // in Bytes
	{
		if (length % 4 != 0 || length < 20)
		{
			System.out.println("Invalid IP header length");
			System.exit(0);
		}
		IHL = length/4;
		lengthInBytes = length;
		lengthInBits = length*8;
		this.ipHeaderInBits = new byte[lengthInBits]; 
		this.ipHeader = new byte[lengthInBytes];
		setDefaultHeader();
	}

	/**
	 * Instantiates a new IP header.
	 */
	public IPHeader()
	{
		lengthInBits = IHL * 32;
		lengthInBytes = IHL * 4;
		this.ipHeaderInBits = new byte[lengthInBits]; 
		this.ipHeader = new byte[lengthInBytes];
		setDefaultHeader();
	}

	/**
	 * Instantiates a new IP header.
	 *
	 * @param headerContent the header content
	 */
	public IPHeader(byte[] headerContent)
	{
		lengthInBytes = headerContent.length;
		lengthInBits = lengthInBytes * 8;
		this.ipHeader = headerContent;
		this.ipHeaderInBits = Transform.arrayByteToBit(ipHeader);
		setHeaderWithContent(ipHeaderInBits);
	}

	/**
	 * Sets the header with content.
	 *
	 * @param ipHeaderInBits the new header with content
	 */
	private void setHeaderWithContent(byte[] ipHeaderInBits)
	{
		Version = (int) Transform.bitArrayToLong(ipHeaderInBits,0,3);
		IHL = (int) Transform.bitArrayToLong(ipHeaderInBits,4,7);
		DSCP = (int) Transform.bitArrayToLong(ipHeaderInBits,8,13);
		ECN = (int) Transform.bitArrayToLong(ipHeaderInBits,14,15);
		TotalLength = (int) Transform.bitArrayToLong(ipHeaderInBits,16,31);
		Identification = (int) Transform.bitArrayToLong(ipHeaderInBits,32,47);
		Flags = (int) Transform.bitArrayToLong(ipHeaderInBits,48,50);
		FragmentOffset = (int) Transform.bitArrayToLong(ipHeaderInBits,50,63);
		TTL = (int) Transform.bitArrayToLong(ipHeaderInBits,64,71);
		Protocol = (int) Transform.bitArrayToLong(ipHeaderInBits,72,79);
		Checksum = (int) Transform.bitArrayToLong(ipHeaderInBits,80,95);
		SrcAddress = Transform.bitArrayToLong(ipHeaderInBits,96,127);
		DestAddress = Transform.bitArrayToLong(ipHeaderInBits,128,159);
	}

	/**
	 * Sets the default header.
	 */
	private void setDefaultHeader()
	{
		setVersion(Version);
		SetIHL();  // Byte
		SetDSCP(DSCP); 
		SetECN(ECN);
		SetIdentification(Identification);
		SetFlags(Flags);
		SetFragmentOffset(FragmentOffset);
		SetTTL(TTL);
		SetProtocol(Protocol);
		SetChecksum(Checksum);
		SetSrcAddress("0.0.0.2");
		SetDestAddress("0.0.0.1");
	}
	
	/**
	 * Sets the dest address.
	 *
	 * @param destAddress the dest address
	 */
	public void SetDestAddress(String destAddress)
	{
		byte[] destAddressBit = Transform.addressToBitArray(destAddress);
		DestAddress = Transform.bitArrayToLong(destAddressBit);
		int startIndex = 128;
		int endIndex = 159;
		ipHeaderInBits = Transform.replaceBitArray(ipHeaderInBits, startIndex, endIndex, destAddressBit);
	}
	

	/**
	 * Sets the src address.
	 *
	 * @param srcAddress the src address
	 */
	public void SetSrcAddress(String srcAddress)
	{
		byte[] srcAddressBit = Transform.addressToBitArray(srcAddress);
		SrcAddress = Transform.bitArrayToLong(srcAddressBit);
		int startIndex = 96;
		int endIndex = 127;
		ipHeaderInBits = Transform.replaceBitArray(ipHeaderInBits, startIndex, endIndex, srcAddressBit);
	}
	
	/**
	 * Sets the checksum.
	 *
	 * @param checksum the checksum
	 */
	public void SetChecksum(int checksum)
	{
		if (checksum < 0 || checksum > 65534)
		{
			System.out.println("Invalid Checksum");
			System.exit(0);
		}
		int startIndex = 80;
		int endIndex = 95;
		int length = endIndex - startIndex + 1;
		Checksum = checksum;
		ipHeaderInBits = Transform.replaceBitArray(ipHeaderInBits, startIndex, endIndex, Transform.longToBitArray(checksum, length));
	}
	
	/**
	 * Sets the protocol.
	 *
	 * @param protocol the protocol
	 */
	public void SetProtocol(int protocol)
	{
		if (protocol < 0 || protocol > 255)
		{
			System.out.println("Invalid protocol");
			System.exit(0);
		}
		Protocol = protocol;
		int startIndex = 72;
		int endIndex = 79;
		int length = endIndex - startIndex + 1;
		ipHeaderInBits = Transform.replaceBitArray(ipHeaderInBits, startIndex, endIndex, Transform.longToBitArray(protocol, length));
	}

	/**
	 * Sets the ttl.
	 *
	 * @param ttl the ttl
	 */
	public void SetTTL(int ttl)
	{
		if (ttl < 0 || ttl > 127)
		{
			System.out.println("Invalid TTL");
			System.exit(0);
		}
		TTL = ttl;
		int startIndex = 64;
		int endIndex = 71;
		int length = endIndex - startIndex + 1;
		ipHeaderInBits = Transform.replaceBitArray(ipHeaderInBits, startIndex, endIndex, Transform.longToBitArray(Math.max(ttl, 1), length));
	}
	
	
	/**
	 * Sets the fragment offset.
	 *
	 * @param fragmentOffset the fragment offset
	 */
	public void SetFragmentOffset(int fragmentOffset)
	{
		if (fragmentOffset < 0 || fragmentOffset > 8191)
		{
			System.out.println("Invalid fragment offset");
			System.exit(0);
		}
		FragmentOffset = fragmentOffset;
		int startIndex = 51;
		int endIndex = 63;
		int length = endIndex - startIndex + 1;
		ipHeaderInBits = Transform.replaceBitArray(ipHeaderInBits, startIndex, endIndex, Transform.longToBitArray(fragmentOffset, length));
	}
	
	/**
	 * Sets the flags.
	 *
	 * @param flags the flags
	 */
	public void SetFlags(int flags)
	{
		if (flags < 0 || flags > 7)
		{
			System.out.println("Invalid flags");
			System.exit(0);
		}
		Flags = flags;
		int startIndex = 48;
		int endIndex = 50;
		int length = endIndex - startIndex + 1;
		ipHeaderInBits = Transform.replaceBitArray(ipHeaderInBits, startIndex, endIndex, Transform.longToBitArray(flags, length));
	}
	
	/**
	 * Sets the identification.
	 *
	 * @param Id the id
	 */
	public void SetIdentification(int Id)
	{
		if (Id < 0 || Id > 65535)
		{
			System.out.println("Invalid Identification");
			System.exit(0);
		}
		Identification = Id;
		int startIndex = 32;
		int endIndex = 47;
		int length = endIndex - startIndex + 1;
		ipHeaderInBits = Transform.replaceBitArray(ipHeaderInBits, startIndex, endIndex, Transform.longToBitArray(Identification, length));
	}
	
	/**
	 * Sets the total length.
	 *
	 * @param datalength the datalength
	 * @param tcpHeaderLength the tcp header length
	 */
	public void SetTotalLength(int datalength, int tcpHeaderLength)
	{
		TotalLength = lengthInBits / 8 + datalength + tcpHeaderLength;
		if (TotalLength < 0 || TotalLength > 65535)
		{
			System.out.println("Invalid total length");
			System.exit(0);
		}
		int startIndex = 16;
		int endIndex = 31;
		int length = endIndex - startIndex + 1;
		ipHeaderInBits = Transform.replaceBitArray(ipHeaderInBits, startIndex, endIndex, Transform.longToBitArray(TotalLength, length));
	}
	
	/**
	 * Sets the ecn.
	 *
	 * @param ecn the ecn
	 */
	public void SetECN(int ecn)
	{
		if (ecn < 0 || ecn > 3)
		{
			System.out.println("Invalid ECN");
			System.exit(0);
		}
		ECN = ecn;
		int startIndex = 14;
		int endIndex = 15;
		int length = endIndex - startIndex + 1;
		ipHeaderInBits = Transform.replaceBitArray(ipHeaderInBits, startIndex, endIndex, Transform.longToBitArray(ecn, length));
	}
	
	
	/**
	 * Sets the dscp.
	 *
	 * @param dscp the dscp
	 */
	public void SetDSCP(int dscp)
	{
		if (dscp < 0 || dscp > 65)
		{
			System.out.println("Invalid DSCP");
			System.exit(0);
		}
		DSCP = dscp;
		int startIndex = 8;
		int endIndex = 13;
		int length = endIndex - startIndex + 1;
		ipHeaderInBits = Transform.replaceBitArray(ipHeaderInBits, startIndex, endIndex, Transform.longToBitArray(dscp, length));
	}
	
	/**
	 * Sets the ihl.
	 */
	public void SetIHL()
	{
		int startIndex = 4;
		int endIndex = 7;
		int length = endIndex - startIndex + 1;
		ipHeaderInBits = Transform.replaceBitArray(ipHeaderInBits, startIndex, endIndex, Transform.longToBitArray(IHL, length));
	}
	
	/**
	 * Sets the version.
	 *
	 * @param version the new version
	 */
	public void setVersion(int version)
	{
		if (Version != 4 && Version != 6)
		{
			System.out.println("Invalid IP Vesion");
			System.exit(0);
		}
		Version = version;
		int startIndex = 0;
		int endIndex = 3;
		int length = endIndex - startIndex + 1;
		ipHeaderInBits = Transform.replaceBitArray(ipHeaderInBits, startIndex, endIndex, Transform.longToBitArray(version, length));
	}


	/**
	 * Compute check sum.
	 *
	 * @return the int
	 */
	public int computeCheckSum()
	{
		int checksum = 0;
		// Before compute checksum, set original checksum to be 0
		int startIndex = 80;
		int endIndex = 95;
		int length = 16;
		ipHeaderInBits = Transform.replaceBitArray(ipHeaderInBits, startIndex, endIndex, Transform.longToBitArray(0, length));
		for (int i = 0; i < ipHeaderInBits.length / 16; i++)
		{
			startIndex = i * 16;
			endIndex = startIndex + 15;
			checksum += Transform.bitArrayToLong(ipHeaderInBits, startIndex, endIndex);
		}
		while (checksum > Math.pow(2, 16) - 1)
		{
			int carry = checksum >> 16;
			checksum = (int) (checksum % Math.pow(2, 16) + carry);
		}
		return checksum^0xffff;
	}
	
	/**
	 * Gets the src address.
	 *
	 * @return the src address
	 */
	public long getSrcAddress()
	{
		return SrcAddress;
	}
	
	/**
	 * Gets the dest address.
	 *
	 * @return the dest address
	 */
	public long getDestAddress()
	{
		return DestAddress;
	}

	/**
	 * Gets the protocol.
	 *
	 * @return the protocol
	 */
	public int getProtocol()
	{
		return Protocol;
	}
	
	/**
	 * Gets the header length in bytes.
	 *
	 * @return the header length in bytes
	 */
	public int getHeaderLengthInBytes()
	{
		return lengthInBytes;
	}
	
	/**
	 * Gets the header length in bits.
	 *
	 * @return the header length in bits
	 */
	public int getHeaderLengthInBits()
	{
		return lengthInBits;
	}
	
	/**
	 * Gets the header.
	 *
	 * @return the header
	 */
	public byte[] getHeader()
	{
		ipHeader = Transform.arrayBitToByte(ipHeaderInBits);
		return ipHeader;
	}

	/**
	 * Verify checksum.
	 *
	 * @return the boolean
	 */
	public Boolean verifyChecksum()
	{
		long total=0;
		for (int i = 0; i < ipHeaderInBits.length / 16; i++)
		{
			int startIndex = i * 16;
			int endIndex = startIndex + 15;
			total += Transform.bitArrayToLong(ipHeaderInBits, startIndex, endIndex);
		}
		while (total > Math.pow(2, 16) - 1)
		{
			int carry = (int) (total >> 16);
			total = (int) (total % Math.pow(2, 16) + carry);
		}
		return (total == 0xffff);
	}

	/**
	 * Gets the total length.
	 *
	 * @return the total length
	 */
	public int getTotalLength()
	{
		return TotalLength;
	}

	
}

