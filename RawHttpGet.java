package rawSocket;

import static com.savarese.rocksaw.net.RawSocket.PF_INET;

import java.io.*;
import java.net.InetAddress;
import java.util.LinkedList;

// TODO: Auto-generated Javadoc
/**
 * The Class Crawler.
 */
public class RawHttpGet
{
	/** The http status OK. */
	private final String HTTP_OK = "200";

	/** The URL of remote server. */
	private Url serverURL;

	/**  The DEFAULT protocol used. */
	private String PROTOCOL = "HTTP/1.0";

	/** The DEFAULT connection. */
	private String CONNECTION = "close";

	/** The DEFAULT cache control. */
	private String CACHE_CONTROL = "max-age=0";

	/** The DEFAULT accept. */
	private String ACCEPT = "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8";

	/** The DEFAULT content type. */
	private String CONTENT_TYPE = "application/x-www-form-urlencoded";

	/** The DEFAULT user agent. */
	private String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.99 Safari/537.36";

	/** The DEFAULT accept lunguage. */
	private String ACCEPT_LUNGUAGE = "en-us";

	/** The linked list of request header. */
	private LinkedList<String> requestHearder;

	/** The Symbol of new line. */
	private String NEW_LINE = "\n";

	// Something about SOCKET

	/** The socket used to send stuff. */
	private RockRawSocket sendSocket = null;

	/** Time out timer, million second. */
	private static int TIMEOUT = 60000;

	/**  receive everything. */
	static int AF_PACKET = 17;

	/**  receive everything. */
	static int ETH_P_ALL = 0x0300;

	/**  Socket type. */
	static int SOCK_RAW = 3;

	/**  Socket type. */
	static int SOCK_STREAM = 1;

	/**  Protocol type. */
	static int IPPROTO_IP = 0;

	/**  Protocol type. */
	static int IPPROTO_TCP = 0;

	/**  Protocol type. */
	static int IPPROTO_RAW = 255;

	/**
	 * Instantiates a new raw http get.
	 */
	public RawHttpGet()
	{
		requestHearder = new LinkedList<String>();
	}

	/**
	 * Download page.
	 *
	 * @param website the unparsed address
	 * @return true, if successful
	 */
	public boolean downloadPage(String website)
	{
		serverURL = new Url(website);
		setRequest("GET");
		setHeader("Host", serverURL.getHost());
		setDefaultHeader();
		// In processRequest() function, we do actual work of connection and download
		if (processRequest())
		{
			// To separate header and body
			boolean responseChecked = false;
			try
			{
				OutputStream out = new FileOutputStream(new File(serverURL.getFileName()));
				InputStream in = sendSocket.getInputStream();
				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0)
				{
					if (responseChecked == false)
					{
						String str = new String(buf, "UTF-8");
						String respondCode = str.substring(9, 12);
						if (!respondCode.equals(HTTP_OK))
						{
							System.out.println("Cannot find page on this server, error code: "
									+ respondCode);
							out.close();
							return false;
						}
						else
						{
							int splitOfHeader = str.indexOf("\r\n\r\n");
							out.write(buf, splitOfHeader + 4, len - splitOfHeader - 4);
							responseChecked = true;
							continue;
						}
					}
					out.write(buf, 0, len);
				}
				out.close();
				in.close();
			}
			catch (IOException e)
			{
				System.out.println("Fail to write in file");
				return false;
			}

			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 * Sets the request.
	 *
	 * @param request the new request
	 */
	public void setRequest(String request)
	{
		String httpCommand = request + " " + serverURL.getPath() + " " + PROTOCOL + NEW_LINE;
		requestHearder.add(httpCommand);
	}

	/**
	 * Sets the default header.
	 */
	public void setDefaultHeader()
	{
		requestHearder.add("Connection: " + CONNECTION + NEW_LINE);
		requestHearder.add("Cache-Control: " + CACHE_CONTROL + NEW_LINE);
		requestHearder.add("ACCEPT: " + ACCEPT + NEW_LINE);
		requestHearder.add("CONTENT_TYPE: " + CONTENT_TYPE + NEW_LINE);
		requestHearder.add("USER_AGENT: " + USER_AGENT + NEW_LINE);
		requestHearder.add("ACCEPT_LUNGUAGE: " + ACCEPT_LUNGUAGE + NEW_LINE);
	}

	/**
	 * Sets the header.
	 *
	 * @param headerName the header name
	 * @param headerValue the header value
	 */
	public void setHeader(String headerName, String headerValue)
	{
		String header = headerName + ": " + headerValue + NEW_LINE;
		requestHearder.add(header);
	}

	/**
	 * Process request. In this program, the main request is "Get"
	 *
	 * @return true, if successful
	 */
	public boolean processRequest()
	{
		sendSocket = new RockRawSocket();
		try
		{
			// Create a RAW socket to send request
			sendSocket.open(PF_INET, SOCK_RAW, IPPROTO_RAW);
			sendSocket.setSendTimeout(TIMEOUT);
			InetAddress hostAddress = InetAddress.getByName(serverURL.getHost());
			// If connect fail, return
			if (sendSocket.connect(hostAddress, TIMEOUT) == false)
			{
				sendSocket.close();
				return false;
			}
			System.out.println("Connection succeed!");
			String request = "";
			while (!requestHearder.isEmpty())
			{
				request += requestHearder.remove();
			}
			request += "\n";
			// Send request, after this request is sent, all response is saved in outputstream of the socket
			if (sendSocket.Send(request) == false)
			{
				sendSocket.close();
				return false;
			}
			else
			{
				sendSocket.close();
				return true;
			}
		}
		catch (IllegalStateException | IOException e)
		{
			return false;
		}
	}
}