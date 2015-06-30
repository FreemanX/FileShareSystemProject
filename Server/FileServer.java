import java.net.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.io.*;

public class FileServer {
	int portNum = 8999;
	final private File FileServerDirectory = new File("./ShareFolder");
	final private File passwdFile = new File("./.passwd");
	Scanner scanner = new Scanner(System.in);

	public FileServer() throws IOException {

		if (FileServerDirectory.exists() && FileServerDirectory.isDirectory()) {
			System.out.println("Dirctory exists\n"
					+ passwdFile.getAbsolutePath());
		} else {
			System.out.println("Initializing share folder...");
			FileServerDirectory.mkdirs();
			System.out.println("Done!\n"
					+ FileServerDirectory.getAbsolutePath());
		}

		if (!passwdFile.exists() || passwdFile.isDirectory())
			setPassword();

		ServerSocket serverSocket = new ServerSocket(portNum);

		System.out.printf("Listening to TCP port# %d...\n",
				serverSocket.getLocalPort());

		while (true) {

			Socket clientSocket = serverSocket.accept();

			new WorkThread(clientSocket).start();

		}
	}

	private void setPassword() throws IOException {

		DataOutputStream out = new DataOutputStream(new FileOutputStream(
				passwdFile));
		System.out.print("Set Password:");
		String password = scanner.nextLine();
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
		out.writeBytes(password);
		out.flush();
		out.close();
		Path path = FileSystems.getDefault().getPath("./", ".passwd");
		Files.setAttribute(path, "dos:hidden", true);
		System.out.println("Set Password done!");
	}

	public static void main(String[] args) throws IOException {
		new FileServer();
	}

}

class WorkThread extends Thread {
	private final String rootPath = "./ShareFolder/";
	private String currentPath = rootPath;
	private File f = new File(currentPath);

	Socket clientSocket = null;
	DataInputStream in;
	DataOutputStream out;

	public WorkThread(Socket ClientSocket) throws IOException {
		this.clientSocket = ClientSocket;
		this.currentPath = this.f.getCanonicalPath();
	}

	public void send(byte[] data, int len) throws IOException {

		out.writeInt(len);

		out.write(data, 0, len);
		out.flush();
	}

	public byte[] receive() throws IOException {
		byte[] data;
		int size;
		int len;

		size = in.readInt();

		data = new byte[size];
		do {
			len = in.read(data, data.length - size, size);
			size -= len;
		} while (size > 0);

		return data;
	}

	private boolean varifyPasswd(String passwd) throws IOException {
		String readInPasswd = "";

		File passwdFile = new File("./.passwd");

		try (BufferedReader br = new BufferedReader(new FileReader(passwdFile))) {

			String sCurrentLine;

			while ((sCurrentLine = br.readLine()) != null) {
				readInPasswd = sCurrentLine;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("Password file missing!");
			disconnect();
		}

		return passwd.equals(readInPasswd);
	}

	private void process() throws IOException {
		String msg;

		while (true) {
			if (!varifyPasswd(new String(receive()))) {
				System.out.println("Wrong Passwd!\n");
				msg = "wrong";
			} else {
				msg = "right";
				break;
			}
			send(msg.getBytes(), msg.length());
		}

		System.out.println("Right passwd!");
		msg = "Please Continue...";

		send(msg.getBytes(), msg.length());

		while (true) {
			msg = new String(receive());

			System.out.println("Client(prot " + clientSocket.getPort() + "): "
					+ msg);

			msg = msg.replaceAll("\\s+", "");
			if (msg.startsWith("ls")) {
				File backup = f;
				boolean changePathResult = true;
				if (!msg.equalsIgnoreCase("ls")) {
					String addPath = msg.substring(2);
					changePathResult = changePath(addPath);
				}

				if (changePathResult) {
					msg = showFiles(f.listFiles());
				} else if(f.exists() && f.isFile())
				{
					msg = "\nFile: " + f.getName() + " Size: "
							+ f.length();
				}
				else {
					msg = "No such directory, please typy in correct path!";
				}

				f = backup;
				currentPath = f.getCanonicalPath();
			} else if (msg.equalsIgnoreCase("pwd")) {
				msg = getCurrentPath();
			} else if (msg.startsWith("cd")) {
				File backup = f;

				if (msg.equalsIgnoreCase("cd")) {
					f = new File(rootPath);
					currentPath = f.getCanonicalPath();
					msg = getCurrentPath();
				} else {
					boolean changePathResult = true;
					String addPath = msg.substring(2);

					changePathResult = changePath(addPath);

					if (changePathResult) {
						msg = getCurrentPath();
					} else {
						f = backup;
						currentPath = f.getCanonicalPath();
						msg = "Wrong Path, please typy in correct directory name!";
					}
				}
			} else if (msg.startsWith("get")) {
				String filePath = currentPath;
				filePath += "/" + msg.substring(3);

				File fileToSend = new File(filePath);
				if (fileToSend.exists()) {
					if (!fileToSend.isDirectory()) {
						msg = "file";
						send(msg.getBytes(), msg.length());
						sendFile(fileToSend);
						msg = "File sent";
					} else {
						msg = "dir";
						send(msg.getBytes(), msg.length());
						sendDir(fileToSend);
						msg = "All files sent";
					}
				} else {
					msg = "File or directory does not exist!";
				}

			} else {
				msg = msg + ": command not found, type help for help";
			}

			send(msg.getBytes(), msg.length());
		}

	}

	private String getCurrentPath() throws IOException {
		String path = f.getCanonicalPath();
		String[] splitPath = path.split("ShareFolder");
		path = "";
		for (int i = 1; i < splitPath.length; i++)
			path += splitPath[i];
		return "ShareFolder" + path;
	}

	private void sendFile(File fileToSend) throws IOException {
		byte[] buffer = new byte[(int) fileToSend.length()];
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		try {
			fis = new FileInputStream(fileToSend);
			bis = new BufferedInputStream(fis);
			bis.read(buffer, 0, buffer.length);
			send(buffer, buffer.length);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bis != null)
				bis.close();
		}
	}

	private void sendDir(File DirToSend) throws IOException {
		AppZip zipDir = new AppZip(DirToSend);
		zipDir.generateFileList(new File(zipDir.get_sourceFolder()));
		zipDir.zipIt(zipDir.get_outputZipFile());
		File zipFile = new File(zipDir.get_outputZipFile());
		sendFile(zipFile);
		zipFile.delete();
	}

	private boolean changePath(String addPath) throws IOException {
		if (!currentPath.substring(currentPath.length()).equals("/")
				&& !currentPath.substring(currentPath.length()).equals("\\")) {
			currentPath += "/" + addPath;
		} else {
			currentPath += addPath;
		}
		f = new File(currentPath);
		File rootDir = new File(rootPath);
		if (!f.getCanonicalPath().contains(rootDir.getCanonicalPath())) {
			f = rootDir;
			currentPath = f.getCanonicalPath();
		}
		if (f.isDirectory()
				&& !currentPath.substring(currentPath.length()).equals("/")) {
			currentPath += "/";
			f = new File(currentPath);
		}

		if (f.exists() && f.isDirectory()) { // Debug
			currentPath = f.getCanonicalPath();
			System.out.println("\ncurrentPath: " + currentPath + "\nfile f: "
					+ f.getPath());
		}
		return f.exists() && f.isDirectory();
	}

	private String showFiles(File[] files) throws IOException {
		String result = "";
		for (File file : files) {
			if (file.isDirectory()) {
				long size = getDirectorySize(file);
				result += "\nDirectory: " + file.getName() + " Size: " + size;
			} else {
				result += "\nFile: " + file.getName() + " Size: "
						+ file.length();
			}
		}
		return result;
	}

	private long getDirectorySize(File directory) {
		long length = 0;
		for (File file : directory.listFiles()) {
			if (file.isFile())
				length += file.length();
			else
				length += getDirectorySize(file);
		}
		return length;
	}

	private void disconnect() {
		System.out.println("disconnected.");
		try {
			in.close();
			out.close();
			clientSocket.close();
		} catch (IOException ex) {
		}
	}

	public void run() {
		System.out.printf("Established connection to client %s:%d.\n",
				clientSocket.getInetAddress().getHostAddress(),
				clientSocket.getPort());

		try {
			in = new DataInputStream(clientSocket.getInputStream());
			out = new DataOutputStream(clientSocket.getOutputStream());
			process();

		} catch (IOException ex) {
			System.out.println("Connection terminated!");

		} finally {
			disconnect();
		}
	}
}

/*
 * Reference: http://www.mkyong.com/java/how-to-compress-files-in-zip-format/
 */
class AppZip {
	List<String> fileList;
	private String outputZipFile;
	private String sourceFolder;

	AppZip(String OutputZipFile, String SourceFolder) {
		this.outputZipFile = OutputZipFile;
		this.sourceFolder = SourceFolder;
		fileList = new ArrayList<String>();
	}

	AppZip(File Dir) {

		this.outputZipFile = Dir.getAbsolutePath() + ".zip";
		this.sourceFolder = Dir.getAbsolutePath();
		fileList = new ArrayList<String>();
	}

	public String get_outputZipFile() {
		return outputZipFile;
	}

	public String get_sourceFolder() {
		return sourceFolder;
	}

	/**
	 * Zip it
	 * 
	 * @param zipFile
	 *            output ZIP file location
	 */
	public void zipIt(String zipFile) {

		byte[] buffer = new byte[1024];

		try {

			FileOutputStream fos = new FileOutputStream(zipFile);
			ZipOutputStream zos = new ZipOutputStream(fos);

			System.out.println("Output to Zip : " + zipFile);

			for (String file : this.fileList) {

				System.out.println("File Added : " + file);
				ZipEntry ze = new ZipEntry(file);
				zos.putNextEntry(ze);

				FileInputStream in = new FileInputStream(sourceFolder
						+ File.separator + file);

				int len;
				while ((len = in.read(buffer)) > 0) {
					zos.write(buffer, 0, len);
				}

				in.close();
			}

			zos.closeEntry();
			// remember close it
			zos.close();

			System.out.println("Done");
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Traverse a directory and get all files, and add the file into fileList
	 * 
	 * @param node
	 *            file or directory
	 */
	public void generateFileList(File node) {

		// add file only
		if (node.isFile()) {
			fileList.add(generateZipEntry(node.getAbsoluteFile().toString()));
		}

		if (node.isDirectory()) {
			String[] subNote = node.list();
			for (String filename : subNote) {
				generateFileList(new File(node, filename));
			}
		}

	}

	/**
	 * Format the file path for zip
	 * 
	 * @param file
	 *            file path
	 * @return Formatted file path
	 */
	private String generateZipEntry(String file) {
		return file.substring(sourceFolder.length() + 1, file.length());
	}
}
