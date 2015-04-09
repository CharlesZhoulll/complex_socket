package rawSocket;

// TODO: Auto-generated Javadoc
/**
 * The Class TCPHeader.
 */
public class TCPHeader
{
	
	/** The tcp header. */
	private byte[] tcpHeader;
	
	/** The tcp header in bits. */
	private byte[] tcpHeaderInBits;
	
	/** The length in bits. */
	private int lengthInBits;
	
	/** The length in bytes. */
	private int lengthInBytes;

	// TCP Header field
	/** The Src port. */
	private int SrcPort = 43545;
	
	/** The Dest port. */
	private int DestPort = 80;
	
	/** The Seq number. */
	private long SeqNumber = 0;
	
	/** The Ack number. */
	private long AckNumber = 0;
	
	/** The Data offset. */
	private int DataOffset = 5; // Byte
	
	/** The Reserved. */
	private int Reserved = 0;
	
	/** The ns. */
	private int NS = 0;
	
	/** The cwr. */
	private int CWR = 0;
	
	/** The ece. */
	private int ECE = 0;
	
	/** The urg. */
	private int URG = 0;
	
	/** The ack. */
	private int ACK = 0;
	
	/** The psh. */
	private int PSH = 0;
	
	/** The rst. */
	private int RST = 0;
	
	/** The syn. */
	private int SYN = 0;
	
	/** The fin. */
	private int FIN = 0;
	
	/** The Window. */
	private int Window = 29213;
	
	/** The Checksum. */
	private int Checksum = 0;
	
	/** The UR gpointer. */
	private int URGpointer = 0;
	
	/** The Options. */
	private int Options;

	/**
	 * Instantiates a new TCP header.
	 *
	 * @param length the length
	 */
	public TCPHeader(int length)
	{
		if (length % 4 != 0 || length < 20)
		{
			System.out.println("Invalid TCP header length");
			System.exit(0);
		}
		DataOffset = length / 4;
		lengthInBits = length * 8;
		lengthInBytes = length;
		this.tcpHeaderInBits = new byte[lengthInBits];
		this.tcpHeader = new byte[lengthInBytes];
		setDefaultHeader();
	}

	/**
	 * Instantiates a new TCP header.
	 */
	public TCPHeader()
	{
		lengthInBits = DataOffset * 32;
		lengthInBytes = DataOffset * 4;
		this.tcpHeaderInBits = new byte[lengthInBits];
		this.tcpHeader = new byte[lengthInBytes];
		setDefaultHeader();
		System.out.println(tcpHeaderInBits);
	}

	/**
	 * Instantiates a new TCP header.
	 *
	 * @param headerContent the header content
	 */
	public TCPHeader(byte[] headerContent)
	{
		tcpHeader = headerContent;
		lengthInBytes = headerContent.length;
		lengthInBits = lengthInBytes * 8;
		this.tcpHeader = headerContent;
		this.tcpHeaderInBits = Transform.arrayByteToBit(tcpHeader);
		setHeaderWithContent(tcpHeaderInBits);
	}

	/**
	 * Sets the header with content.
	 *
	 * @param tcpHeaderInBits the new header with content
	 */
	private void setHeaderWithContent(byte[] tcpHeaderInBits)
	{
		SrcPort = (int) Transform.bitArrayToLong(tcpHeaderInBits, 0, 15);
		DestPort = (int) Transform.bitArrayToLong(tcpHeaderInBits, 16, 31);
		SeqNumber = Transform.bitArrayToLong(tcpHeaderInBits, 32, 63);
		AckNumber = Transform.bitArrayToLong(tcpHeaderInBits, 64, 95);
		DataOffset = (int) Transform.bitArrayToLong(tcpHeaderInBits, 96, 99);
		Reserved = (int) Transform.bitArrayToLong(tcpHeaderInBits, 100, 102);
		NS = (int) Transform.bitArrayToLong(tcpHeaderInBits, 103, 103);
		CWR = (int) Transform.bitArrayToLong(tcpHeaderInBits, 104, 104);
		ECE = (int) Transform.bitArrayToLong(tcpHeaderInBits, 105, 105);
		URG = (int) Transform.bitArrayToLong(tcpHeaderInBits, 106, 106);
		ACK = (int) Transform.bitArrayToLong(tcpHeaderInBits, 107, 107);
		PSH = (int) Transform.bitArrayToLong(tcpHeaderInBits, 108, 108);
		RST = (int) Transform.bitArrayToLong(tcpHeaderInBits, 109, 109);
		SYN = (int) Transform.bitArrayToLong(tcpHeaderInBits, 110, 110);
		FIN = (int) Transform.bitArrayToLong(tcpHeaderInBits, 111, 111);
		Window = (int) Transform.bitArrayToLong(tcpHeaderInBits, 112, 127);
		Checksum = (int) Transform.bitArrayToLong(tcpHeaderInBits, 128, 143);
		URGpointer = (int) Transform.bitArrayToLong(tcpHeaderInBits, 144, 159);
		Options = (int) Transform.bitArrayToLong(tcpHeaderInBits, 160, lengthInBits - 1);
	}

	/**
	 * Sets the default header.
	 */
	private void setDefaultHeader()
	{
		setSrcPort(SrcPort);
		setDestPort(DestPort);
		SetSeqNumber(SeqNumber);
		SetAckNumber(AckNumber);
		SetDataOffset(); // Byte
		SetReserved(Reserved);
		SetNS(NS);
		SetCWR(CWR);
		SetECE(ECE);
		SetURG(URG);
		SetACK(ACK);
		SetPSH(PSH);
		SetRST(RST);
		SetSYN(SYN);
		SetFIN(FIN);
		SetWindow(Window);
		SetChecksum(Checksum);
		SetURGPointer(URGpointer);
	}

	/**
	 * Sets the urg pointer.
	 *
	 * @param urgpointer the urgpointer
	 */
	public void SetURGPointer(int urgpointer)
	{
		if (urgpointer < 0 || urgpointer > 65535 || (URG == 0 && urgpointer != 0))
		{
			System.out.println("Invalid URGPointer");
			System.exit(0);
		}
		URGpointer = urgpointer;
		int startIndex = 144;
		int endIndex = 159;
		int length = endIndex - startIndex + 1;
		tcpHeaderInBits = Transform.replaceBitArray(tcpHeaderInBits, startIndex, endIndex,
				Transform.longToBitArray(urgpointer, length));
	}

	/**
	 * Sets the checksum.
	 *
	 * @param checksum the checksum
	 */
	public void SetChecksum(int checksum)
	{
		if (checksum < 0 || checksum > 65535) // Does not support real big
												// sequence
		{
			System.out.println("Invalid checksum: " + checksum + " ACK number: " + AckNumber);
			System.exit(0);
		}
		Checksum = checksum;
		int startIndex = 128;
		int endIndex = 143;
		int length = endIndex - startIndex + 1;
		tcpHeaderInBits = Transform.replaceBitArray(tcpHeaderInBits, startIndex, endIndex,
				Transform.longToBitArray(checksum, length));
	}

	/**
	 * Sets the window.
	 *
	 * @param window the window
	 */
	public void SetWindow(int window)
	{
		if (window < 0 || window > 65535) // Does not support real big sequence
		{
			System.out.println("Invalid Window number");
			System.exit(0);
		}
		Window = window;
		int startIndex = 112;
		int endIndex = 127;
		int length = endIndex - startIndex + 1;
		tcpHeaderInBits = Transform.replaceBitArray(tcpHeaderInBits, startIndex, endIndex,
				Transform.longToBitArray(window, length));
	}

	/**
	 * Sets the fin.
	 *
	 * @param fin the fin
	 */
	public void SetFIN(int fin)
	{
		if (fin < 0 || fin > 1) // Does not support real big sequence
		{
			System.out.println("Invalid FIN number");
			System.exit(0);
		}
		FIN = fin;
		int startIndex = 111;
		int endIndex = 111;
		int length = endIndex - startIndex + 1;
		tcpHeaderInBits = Transform.replaceBitArray(tcpHeaderInBits, startIndex, endIndex,
				Transform.longToBitArray(fin, length));
	}

	/**
	 * Sets the syn.
	 *
	 * @param syn the syn
	 */
	public void SetSYN(int syn)
	{
		if (syn < 0 || syn > 1) // Does not support real big sequence
		{
			System.out.println("Invalid SYN number");
			System.exit(0);
		}
		SYN = syn;
		int startIndex = 110;
		int endIndex = 110;
		int length = endIndex - startIndex + 1;
		tcpHeaderInBits = Transform.replaceBitArray(tcpHeaderInBits, startIndex, endIndex,
				Transform.longToBitArray(syn, length));
	}

	/**
	 * Sets the rst.
	 *
	 * @param rst the rst
	 */
	public void SetRST(int rst)
	{
		if (rst < 0 || rst > 1) // Does not support real big sequence
		{
			System.out.println("Invalid RST number");
			System.exit(0);
		}
		RST = rst;
		int startIndex = 109;
		int endIndex = 109;
		int length = endIndex - startIndex + 1;
		tcpHeaderInBits = Transform.replaceBitArray(tcpHeaderInBits, startIndex, endIndex,
				Transform.longToBitArray(rst, length));
	}

	/**
	 * Sets the psh.
	 *
	 * @param psh the psh
	 */
	public void SetPSH(int psh)
	{
		if (psh < 0 || psh > 1) // Does not support real big sequence
		{
			System.out.println("Invalid PSH number");
			
			System.exit(0);
		}
		PSH = psh;
		int startIndex = 108;
		int endIndex = 108;
		int length = endIndex - startIndex + 1;
		tcpHeaderInBits = Transform.replaceBitArray(tcpHeaderInBits, startIndex, endIndex,
				Transform.longToBitArray(psh, length));
	}

	/**
	 * Sets the ack.
	 *
	 * @param ack the ack
	 */
	public void SetACK(int ack)
	{
		if (ack < 0 || ack > 1) // Does not support real big sequence
		{
			System.out.println("Invalid ACK number");
			System.exit(0);
		}
		ACK = ack;
		int startIndex = 107;
		int endIndex = 107;
		int length = endIndex - startIndex + 1;
		tcpHeaderInBits = Transform.replaceBitArray(tcpHeaderInBits, startIndex, endIndex,
				Transform.longToBitArray(ack, length));
	}

	/**
	 * Sets the urg.
	 *
	 * @param urg the urg
	 */
	public void SetURG(int urg)
	{
		if (urg < 0 || urg > 1) // Does not support real big sequence
		{
			System.out.println("Invalid URG number");
			System.exit(0);
		}
		URG = urg;
		int startIndex = 106;
		int endIndex = 106;
		int length = endIndex - startIndex + 1;
		tcpHeaderInBits = Transform.replaceBitArray(tcpHeaderInBits, startIndex, endIndex,
				Transform.longToBitArray(urg, length));
	}

	/**
	 * Sets the ece.
	 *
	 * @param ece the ece
	 */
	public void SetECE(int ece)
	{
		if (ece < 0 || ece > 1) // Does not support real big sequence
		{
			System.out.println("Invalid ECE number");
			System.exit(0);
		}
		ECE = ece;
		int startIndex = 105;
		int endIndex = 105;
		int length = endIndex - startIndex + 1;
		tcpHeaderInBits = Transform.replaceBitArray(tcpHeaderInBits, startIndex, endIndex,
				Transform.longToBitArray(ece, length));
	}

	/**
	 * Sets the cwr.
	 *
	 * @param cwr the cwr
	 */
	public void SetCWR(int cwr)
	{
		if (cwr < 0 || cwr > 1) // Does not support real big sequence
		{
			System.out.println("Invalid CWR number");
			System.exit(0);
		}
		CWR = cwr;
		int startIndex = 104;
		int endIndex = 104;
		int length = endIndex - startIndex + 1;
		tcpHeaderInBits = Transform.replaceBitArray(tcpHeaderInBits, startIndex, endIndex,
				Transform.longToBitArray(cwr, length));
	}

	/**
	 * Sets the ns.
	 *
	 * @param ns the ns
	 */
	public void SetNS(int ns)
	{
		if (ns < 0 || ns > 1) // Does not support real big sequence
		{
			System.out.println("Invalid NS number");
			System.exit(0);
		}
		NS = ns;
		int startIndex = 103;
		int endIndex = 103;
		int length = endIndex - startIndex + 1;
		tcpHeaderInBits = Transform.replaceBitArray(tcpHeaderInBits, startIndex, endIndex,
				Transform.longToBitArray(ns, length));
	}

	/**
	 * Sets the reserved.
	 *
	 * @param reserved the reserved
	 */
	public void SetReserved(int reserved)
	{
		int startIndex = 100;
		int endIndex = 102;
		int length = endIndex - startIndex + 1;
		tcpHeaderInBits = Transform.replaceBitArray(tcpHeaderInBits, startIndex, endIndex,
				Transform.longToBitArray(reserved, length));
	}

	/**
	 * Sets the data offset.
	 */
	public void SetDataOffset()
	{
		int startIndex = 96;
		int endIndex = 99;
		int length = endIndex - startIndex + 1;
		tcpHeaderInBits = Transform.replaceBitArray(tcpHeaderInBits, startIndex, endIndex,
				Transform.longToBitArray(DataOffset, length));
	}

	/**
	 * Sets the ack number.
	 *
	 * @param ackNumber the ack number
	 */
	public void SetAckNumber(long ackNumber)
	{
		if (ackNumber < 0 || ackNumber > Math.pow(2, 32) - 1) // Does not
																// support real
																// big sequence
		{
			System.out.println("Invalid ack number: " + ackNumber);
			System.exit(0);
		}
		AckNumber = ackNumber;
		int startIndex = 64;
		int endIndex = 95;
		int length = endIndex - startIndex + 1;
		tcpHeaderInBits = Transform.replaceBitArray(tcpHeaderInBits, startIndex, endIndex,
				Transform.longToBitArray(ackNumber, length));
	}

	/**
	 * Sets the seq number.
	 *
	 * @param seqNumber the seq number
	 */
	public void SetSeqNumber(long seqNumber)
	{
		if (seqNumber < 0 || seqNumber > Math.pow(2, 32) - 1) // Does not
																// support real
																// big sequence
		{
			System.out.println("Invalid sequence number");
			System.exit(0);
		}
		SeqNumber = seqNumber;
		int startIndex = 32;
		int endIndex = 63;
		int length = endIndex - startIndex + 1;
		tcpHeaderInBits = Transform.replaceBitArray(tcpHeaderInBits, startIndex, endIndex,
				Transform.longToBitArray(seqNumber, length));
	}

	/**
	 * Sets the dest port.
	 *
	 * @param destPort the new dest port
	 */
	public void setDestPort(int destPort)
	{
		if (destPort < 0 || destPort > 65535)
		{
			System.out.println("Invalid dest port");
			System.exit(0);
		}
		DestPort = destPort;
		int startIndex = 16;
		int endIndex = 31;
		int length = endIndex - startIndex + 1;
		tcpHeaderInBits = Transform.replaceBitArray(tcpHeaderInBits, startIndex, endIndex,
				Transform.longToBitArray(destPort, length));
	}

	/**
	 * Sets the src port.
	 *
	 * @param srcPort the new src port
	 */
	public void setSrcPort(int srcPort)
	{
		if (srcPort < 0 || srcPort > 65535)
		{
			System.out.println("Invalid source port");
			System.exit(0);
		}
		SrcPort = srcPort;
		int startIndex = 0;
		int endIndex = 15;
		int length = endIndex - startIndex + 1;
		tcpHeaderInBits = Transform.replaceBitArray(tcpHeaderInBits, startIndex, endIndex,
				Transform.longToBitArray(srcPort, length));
	}




	// IP change format of address
	// Change function of bit array
	/**
	 * Compute checksum.
	 *
	 * @param ipHeader the ip header
	 * @param data the data
	 * @return the int
	 */
	public int computeChecksum(IPHeader ipHeader, byte[] data)
	{
		int checksum = 0;
		long sourceAddress = ipHeader.getSrcAddress();
		long destAddress = ipHeader.getDestAddress();
		int protocol = ipHeader.getProtocol();
		int tcpLength = lengthInBytes + data.length;
		int psudoLength = tcpHeaderInBits.length + 96 + data.length * 8;
		byte[] psudoHeader = new byte[psudoLength];
		psudoHeader = Transform.replaceBitArray(psudoHeader, 0, 31, Transform.longToBitArray(sourceAddress, 32));
		psudoHeader = Transform.replaceBitArray(psudoHeader, 32, 63, Transform.longToBitArray(destAddress, 32));
		psudoHeader = Transform.replaceBitArray(psudoHeader, 64, 71, Transform.longToBitArray(0, 8));
		psudoHeader = Transform.replaceBitArray(psudoHeader, 72, 79, Transform.longToBitArray(protocol, 8));
		psudoHeader = Transform.replaceBitArray(psudoHeader, 80, 95, Transform.longToBitArray(tcpLength, 16));
		psudoHeader = Transform.replaceBitArray(psudoHeader, 96, 95 + lengthInBits, tcpHeaderInBits);
		// Handle payload, be careful about two things:
		// (1): if payload == 0, do not try to replace bit array, otherwise overflow
		// (2): if after adding payload the total length is odd number, pad eigth "0" to make it divisible by 16
		// (3): if payload == 0. no need to pad as option is always divisible by 32
		if (data.length > 0)
		{
			psudoHeader = Transform.replaceBitArray(psudoHeader, 96 + lengthInBits, psudoLength - 1,
					Transform.arrayByteToBit(data));
			if (psudoLength % 16 != 0)
			{
				byte[] pad = new byte[8];
				for (int i = 0; i < 8; i++)
				{
					pad[i] = 0;
				}
				psudoHeader = Transform.append(psudoHeader, pad);
			}
		}
		
		// Before compute checksum, set original checksum to be 0
		int startIndex = 224;
		int endIndex = 239;
		int length = 16;
		psudoHeader = Transform.replaceBitArray(psudoHeader, startIndex, endIndex, Transform.longToBitArray(0, length));
		StringBuilder sb = new StringBuilder();
		for (byte b : Transform.arrayBitToByte(psudoHeader))
		{
			sb.append(String.format("%02X ", b));
		}
		//System.out.println(sb.toString());
		checksum = computeChecksum(psudoHeader);
		return checksum;
	}

	/**
	 * Compute checksum.
	 *
	 * @param psudoHeader the psudo header
	 * @return the int
	 */
	private int computeChecksum(byte[] psudoHeader)
	{
		int checksum = 0;
		for (int i = 0; i < psudoHeader.length / 16; i++)
		{
			int startIndex = i * 16;
			int endIndex = startIndex + 15;
			checksum += Transform.bitArrayToLong(psudoHeader, startIndex, endIndex);
		}
		while (checksum > Math.pow(2, 16) - 1)
		{
			int carry = checksum >> 16;
			checksum = (int) (checksum % Math.pow(2, 16) + carry);
		}
		return checksum ^ 0xffff;
	}


	/**
	 * Gets the header.
	 *
	 * @return the header
	 */
	public byte[] getHeader()
	{
		tcpHeader = Transform.arrayBitToByte(tcpHeaderInBits);
		return tcpHeader;
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
	 * Gets the sequence number.
	 *
	 * @return the sequence number
	 */
	public long getSequenceNumber()
	{
		return SeqNumber;
	}

	/**
	 * Gets the ack number.
	 *
	 * @return the ack number
	 */
	public long getAckNumber()
	{
		return AckNumber;
	}

	/**
	 * Gets the fin.
	 *
	 * @return the fin
	 */
	public int getFIN()
	{
		return FIN;
	}

	/**
	 * Gets the syn.
	 *
	 * @return the syn
	 */
	public int getSYN()
	{
		return SYN;
	}

	/**
	 * Gets the dest port.
	 *
	 * @return the dest port
	 */
	public int getDestPort()
	{
		return DestPort;
	}

	/**
	 * Verify checksum.
	 *
	 * @param ipHeader the ip header
	 * @param data the data
	 * @return true, if successful
	 */
	public boolean verifyChecksum(IPHeader ipHeader, byte[] data)
	{
		int total = 0;
		long sourceAddress = ipHeader.getSrcAddress();
		long destAddress = ipHeader.getDestAddress();
		int protocol = ipHeader.getProtocol();
		int tcpLength = lengthInBytes + data.length;
		int psudoLength = tcpHeaderInBits.length + 96 + data.length * 8;
		byte[] psudoHeader = new byte[psudoLength];
		psudoHeader = Transform.replaceBitArray(psudoHeader, 0, 31, Transform.longToBitArray(sourceAddress, 32));
		psudoHeader = Transform.replaceBitArray(psudoHeader, 32, 63, Transform.longToBitArray(destAddress, 32));
		psudoHeader = Transform.replaceBitArray(psudoHeader, 64, 71, Transform.longToBitArray(0, 8));
		psudoHeader = Transform.replaceBitArray(psudoHeader, 72, 79, Transform.longToBitArray(protocol, 8));
		psudoHeader = Transform.replaceBitArray(psudoHeader, 80, 95, Transform.longToBitArray(tcpLength, 16));
		psudoHeader = Transform.replaceBitArray(psudoHeader, 96, 95 + lengthInBits, tcpHeaderInBits);
		if (data.length > 0)
		{
			psudoHeader = Transform.replaceBitArray(psudoHeader, 96 + lengthInBits, psudoLength - 1,
					Transform.arrayByteToBit(data));
			if (psudoLength % 16 != 0)
			{
				byte[] pad = new byte[8];
				for (int i = 0; i < 8; i++)
				{
					pad[i] = 0;
				}
				psudoHeader = Transform.append(psudoHeader, pad);
			}
		}
		for (int i = 0; i < psudoHeader.length / 16; i++)
		{
			int startIndex = i * 16;
			int endIndex = startIndex + 15;
			total += Transform.bitArrayToLong(psudoHeader, startIndex, endIndex);
		}
		while (total > Math.pow(2, 16) - 1)
		{
			int carry = total >> 16;
				total = (int) (total % Math.pow(2, 16) + carry);
		}
		return (total == 0xffff);
	}
}
