package rawSocket;

// TODO: Auto-generated Javadoc
/**
 * The Class RawGet.
 */
public class RawGet
{
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args)
	{
		if (args.length != 1)
		{
			System.out.println("Illegal input arguments");
			return;
		}
		String website = args[0];
		RawHttpGet httpGet = new RawHttpGet();
		if (httpGet.downloadPage(website) == false)
		{
			System.out.println("Downloading fail!");
		}
		else
		{
			System.out.println("Download succeed!");
		}
	}
}
