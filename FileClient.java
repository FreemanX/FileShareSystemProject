import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Pattern;
import java.io.*;

public class FileClient {
	static private final String IPV4_REGEX = "(([0-1]?[0-9]{1,2}\\.)|(2[0-4][0-9]\\.)|(25[0-5]\\.)){3}(([0-1]?[0-9]{1,2})|(2[0-4][0-9])|(25[0-5]))";
	static private Pattern IPV4_PATTERN = Pattern.compile(IPV4_REGEX);

	String serverAddress; // = "127.0.0.1";
	int portNum = 8999;
	Socket clientSocket;
	DataInputStream in;
	DataOutputStream out;
	Scanner scanner = new Scanner(System.in);

	public FileClient() {
		try {
			connectToServer();
			process();
		} catch (IOException ex) {
			System.out.println("Connection terminated!");
		} finally {
			disconnect();
		}
	}

	private void getIP() {
		do {
			System.out.print("Please input valid server's IP address: ");
			serverAddress = scanner.nextLine();
		} while (!isValidIPV4(serverAddress));
	}

	public static boolean isValidIPV4(String s) {
		return IPV4_PATTERN.matcher(s).matches();
	}

	public void connectToServer() throws IOException {

		do {
			getIP();
			clientSocket = new Socket(serverAddress, portNum);
		} while (new DataInputStream(clientSocket.getInputStream()) == null);

		System.out.printf("Connected to server using local port: %d.\n",
				clientSocket.getLocalPort());
		in = new DataInputStream(clientSocket.getInputStream());
		out = new DataOutputStream(clientSocket.getOutputStream());
	}

	private void process() throws IOException {
		String msg;

		do { // Password
			System.out.print("Please type int your passwd: ");
			msg = encryptPasswd(scanner.nextLine());
			send(msg.getBytes(), msg.length());
		} while (new String(receive()).equals("wrong"));

		System.out.println("Right passwd!");
		help();

		while (true) {
			System.out.print("Client> ");
			msg = scanner.nextLine();

			if (msg.equalsIgnoreCase("quit")) {
				break;
			} else if (msg.equalsIgnoreCase("help")) {
				help();
			} else if (msg.startsWith("get")) {
				String receiveName = msg.substring(3);
				send(msg.getBytes(), msg.length());
				msg = new String(receive());

				if(receiveName.contains("\\")) ///////////////////////////////////
				{
					String [] subName = receiveName.split("\\");
					receiveName = subName[subName.length - 1];
				}else if(receiveName.contains("/"))
				{
					String [] subName = receiveName.split("/");
					receiveName = subName[subName.length - 1];
				}
				
				System.out.println(receiveName);

				if (msg.equals("file")) {
					receiveFile(receiveName);
					printServerRespond(msg);
				} else if (msg.equals("dir")) {
					receiveDir(receiveName);
					printServerRespond(msg);
				} else {
					System.out.println("Server> " + msg);
				}

			} else {
				if (msg.length() > 1 || !msg.isEmpty()) {
					send(msg.getBytes(), msg.length());
					printServerRespond(msg);
				}
			}

		}
	}

	private void printServerRespond(String msg) throws IOException {
		msg = new String(receive());
		System.out.println("Server> " + msg);
	}

	private void receiveFile(String fileName) throws IOException {
		System.out.println("Receiving files...");
		byte[] buffer = receive();
		FileOutputStream fos = new FileOutputStream(fileName);
		fos.write(buffer);
		fos.flush();
		fos.close();
	}

	private void receiveDir(String dirName) throws IOException {
		receiveFile(dirName + ".zip");
	}

	private void help() {
		System.out
				.println("\n\n\n\n\n\n\nHELP (all commands are case sensitive): ");
		System.out.println("\nls\t----\tlist directory contents");
		System.out.println("cd\t----\tenter a directory");
		System.out.println("get\t----\tdownload a file or a directory");
		System.out
				.println("pwd\t----\tprint name of current/working directory");
		System.out.println("quit\t----\tTerminate the program");
		System.out.println("help\t----\tCall for help.\n\n\n\n\n\n\n\n");
	}

	private String encryptPasswd(String in) {
		String password = in;
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
			md.update(password.getBytes());
			byte[] tmpBytes = md.digest();
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < tmpBytes.length; i++)
				sb.append(Integer.toString((tmpBytes[i] & 0xff) + 0x100, 16)
						.substring(1));
			password = sb.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return password;
	}

	public void disconnect() {
		System.out.println("disconnected.");
		try {
			in.close();
			out.close();
			clientSocket.close();
		} catch (IOException ex) {

		} catch (NullPointerException ex1) {
			System.out.println("Server is down.");
		}
	}

	public void send(byte[] data, int len) throws IOException {
		// send the size of the message
		out.writeInt(len);
		// send the content of the message
		out.write(data, 0, len);

		out.flush();
	}

	public byte[] receive() throws IOException {
		byte[] data;
		int size;
		int len;

		// get the size of the message
		size = in.readInt();

		// receive the message content
		data = new byte[size];
		do {
			len = in.read(data, data.length - size, size);
			size -= len;
		} while (size > 0);

		return data;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new FileClient();

	}
}
