package rawSocket;

// TODO: Auto-generated Javadoc
/**
 * The Class Transform.
 */
public class Transform
{

	/**
	 * Replace some bits in a bit array.
	 *
	 * @param bitBrray the bit array to be replaced
	 * @param startIndex the start index
	 * @param endIndex the end index
	 * @param subBitArray the new sub bit array
	 * @return the byte[]
	 */
	public static byte[] replaceBitArray(byte[] bitBrray, int startIndex, int endIndex, byte[] subBitArray)
	{
		byte[] newBitArray = new byte[bitBrray.length];
		for (int i = 0; i < startIndex; i++)
			newBitArray[i] = bitBrray[i];
		for (int i = startIndex; i <= endIndex; i++)
			newBitArray[i] = subBitArray[i - startIndex];
		for (int i = endIndex + 1; i < bitBrray.length; i++)
			newBitArray[i] = bitBrray[i];
		return newBitArray;
	}
	
	/**
	 * Append a new array behind the old array.
	 *
	 * @param oldArray the array to be appended
	 * @param pad the pad array
	 * @return the new array that has been appended
	 */
	public static byte[] append(byte[] oldArray, byte[] pad)
	{
		byte[] newArray = new byte[oldArray.length + pad.length];
		for (int i = 0; i < oldArray.length; i++)
		{
			newArray[i] = oldArray[i];
		}
		for (int i = oldArray.length; i < newArray.length; i++)
		{
			newArray[i] = pad[i - oldArray.length];
		}
		return newArray;
	}
	
	/**
	 * Transform a 8 bit byte into bit array .
	 *
	 * @param byteNumber the byte number
	 * @return the bit array
	 */
	public static byte[] byteToBitArray(byte byteNumber)
	{
		byte[] bitArray = new byte[8];
		for (int i = 0; i < 8; i++)
		{
			bitArray[i] = (byte) ((byteNumber >> (7 - i)) & 0x1);
		}
		return bitArray;
	}
	
	/**
	 * Transform a bit array into a byte array.
	 *
	 * @param bitArray the bit array
	 * @return the byte array
	 */
	public static byte[] arrayBitToByte(byte[] bitArray)
	{
		byte[] byteArray = new byte[bitArray.length / 8];
		for (int i = 0; i < byteArray.length; i++)
		{
			int startIndex = 8 * i;
			int endIndex = startIndex + 7;
			int intValue = (int) bitArrayToLong(bitArray, startIndex, endIndex);
			byteArray[i] = (byte) (intValue & 0xff);
		}
		return byteArray;
	}
	
	/**
	 * Transform a byte array into a bit array.
	 *
	 * @param byteArray the byte array
	 * @return the bit array
	 */
	public static byte[] arrayByteToBit(byte[] byteArray)
	{
		byte[] bitArray = new byte[byteArray.length * 8];
		for (int i = 0; i < byteArray.length; i++)
		{
			int startIndex = 8 * i;
			byte[] bits = byteToBitArray(byteArray[i]);
			for (int j = 0; j < 8; j++)
			{
				bitArray[startIndex + j] = bits[j];
			}
		}
		return bitArray;
	}
	
	/**
	 * Transform byte to hex string.
	 *
	 * @param byteArray the byte array
	 * @return the string
	 */
	public String[] arrayByteToHexString(byte[] byteArray)
	{
		int length = byteArray.length / 4;
		String[] headerHex = new String[length];
		for (int i = 0; i < length; i++)
		{
			int startIndex = 4 * i;
			int endIndex = startIndex + 3;
			headerHex[i] = Integer.toString((int) Transform.bitArrayToLong(byteArray, startIndex, endIndex), 16);
		}
		return headerHex;
	}
	
	/**
	 * Transform part of bit array to long number.
	 *
	 * @param array the array
	 * @param startIndex the start index
	 * @param endIndex the end index
	 * @return the long number
	 */
	public static long bitArrayToLong(byte[] array, int startIndex, int endIndex)
	{
		int length = endIndex - startIndex + 1;
		byte[] bitArray = new byte[length];
		for (int i = 0; i < length; i++)
		{
			bitArray[i] = array[startIndex + i];
		}
		return bitArrayToLong(bitArray);
	}
	
	/**
	 * Transform the whole bit array to long number.
	 *
	 * @param bitArray the bit array
	 * @return the long
	 */
	public static long bitArrayToLong(byte[] bitArray)
	{
		long result = 0;
		for (int i = 0; i < bitArray.length; i++)
		{
			result += bitArray[i] * Math.pow(2, (bitArray.length - 1 - i));
		}
		return result;
	}
	
	/**
	 * Long number to bit array.
	 *
	 * @param longValue the long value
	 * @param length the length
	 * @return the byte array
	 */
	public static byte[] longToBitArray(long longValue, int length)
	{
		byte[] bitArray = new byte[length];
		String bitString = Long.toBinaryString(longValue);
		int zeroFillLength = length - bitString.length();
		for (int i = 0; i < zeroFillLength; i++) // Add 0 on left (big endian)
		{
			bitArray[i] = 0;
		}
		for (int i = zeroFillLength; i < length; i++)
		{
			int indexOfBit = i - zeroFillLength;
			bitArray[i] = (byte) Character.getNumericValue(bitString.charAt(indexOfBit));
		}
		return bitArray;
	}

	/**
	 * Transform address (in string) to bit array.
	 *
	 * @param address the address
	 * @return the byte[]
	 */
	public static byte[] addressToBitArray(String address)
	{
		byte[] addressByte = new byte[32];
		String delims = "[.]+";
		String[] addressTokens = address.split(delims);
		if (addressTokens.length != 4)
		{
			System.out.println("Invalid address");
			System.exit(0);
		}
		for (int i = 0; i < 4; i++)
		{
			int addressTokenInt = Integer.parseInt(addressTokens[i]);
			if (addressTokenInt < 0 || addressTokenInt > 255)
			{
				System.out.println("Invalid address");
				System.exit(0);
			}
			byte[] addressTokenBit = longToBitArray(addressTokenInt, 8);
			for (int j = 0; j < 8; j++)
				addressByte[i * 8 + j] = addressTokenBit[j];
		}
		return addressByte;
	}

	/**
	 * Transform address (string) to long number.
	 *
	 * @param address the address
	 * @return the long
	 */
	public static long addressToLong(String address)
	{
		byte[] srcAddressBit = addressToBitArray(address);
		return bitArrayToLong(srcAddressBit);
	}
	
}
