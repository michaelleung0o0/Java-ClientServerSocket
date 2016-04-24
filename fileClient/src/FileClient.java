
/**
 * Created by michaelleung on 9/4/16.
 */

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;

public class FileClient {
	String serverAddress = "", portNum= "", user = "", password = "";
    String dirName = System.getProperty("user.dir") + "/download_file/";
	//"C:/DownloadFile/";
	ArrayList<FileList> fileList = new ArrayList<FileList>();

	Socket clientSocket;
	DataInputStream in;
	DataOutputStream out;
	Scanner scanner = new Scanner(System.in);

	public FileClient() {
		try {
			login();
			connect();
			process();
		} catch (Exception ex) {
			System.out.println("Connection terminated!");
		} finally {
			disconnect();
		}

	}

	public void receiveFile(DataInputStream in, String fileName) throws IOException {
		File theDir = new File(dirName);
		if (!theDir.exists()) {
			theDir.mkdir();
		}

		System.out.println("receiveFile... path = " + dirName + fileName);
		int filesize = 6022386;
		int bytesRead;
		int current = 0;
		byte[] mybytearray = new byte[filesize];
		File downloadFile1 = new File(dirName + fileName.trim());

		FileOutputStream fos = new FileOutputStream(downloadFile1);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		bytesRead = in.read(mybytearray, 0, mybytearray.length);
		current = bytesRead;
		do {
			bytesRead = in.read(mybytearray, current, (mybytearray.length - current));
			if (bytesRead >= 0)
				current += bytesRead;
		} while (bytesRead > -1);
		// bos.write(mybytearray, 0, current);
		bos.flush();
		bos.close();
		System.out.println("receiveFile... finish");
	}

	void login() {
		while (serverAddress.isEmpty()) {
			System.out.print("Please input a server address: ");
			serverAddress = scanner.nextLine();
		}

		while (portNum.isEmpty()) {
			System.out.print("Please input a local port number: ");
			portNum = scanner.nextLine();
		}

		while (user.isEmpty()) {
			System.out.print("Please input the username: ");
			user = scanner.nextLine();
		}

		while (password.isEmpty()) {
			System.out.print("Please input the password: ");
			password = scanner.nextLine();
		}
	}

	void connect() throws IOException {
		clientSocket = new Socket(serverAddress, Integer.parseInt(portNum));
		System.out.printf("Connected to server using local port: %d.\n", clientSocket.getLocalPort());

		in = new DataInputStream(clientSocket.getInputStream());
		out = new DataOutputStream(clientSocket.getOutputStream());
	}

	public void send(byte[] data, int len) throws IOException {
		out.writeInt(len);
		out.write(data, 0, len);
		out.flush();
	}

	public byte[] receive() throws IOException {
		byte[] data = new byte[4];
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

	public void disconnect() {
		System.out.println("disconnected.");
		try {
			in.close();
			out.close();
			clientSocket.close();
		} catch (IOException ex) {
		}
	}

	private void process() throws IOException, InterruptedException {
//        System.out.println("debug process");
		AES aes = new AES(Config.key);
		Gson gson = new Gson();
		String msg, encrypt_msg, decrypt_msg;

		msg = user + "," + password;
		encrypt_msg = aes.encrypt(msg);
		send(encrypt_msg.getBytes(), encrypt_msg.length());

		msg = new String(receive());
		decrypt_msg = aes.decrypt(msg);
        fileList = gson.fromJson(decrypt_msg, new TypeToken<ArrayList<FileList>>(){}.getType());
        for(int i=0; i<fileList.size(); i++) {
			System.out.println(fileList.get(i).getFileName() + " " + fileList.get(i).getFileSize() + "bytes");
		}
		while (true) {
			// get a new message from the console
			System.out.print("Client: ");
			msg = scanner.nextLine();
			encrypt_msg = aes.encrypt(msg);
			// send the message to the server
			send(encrypt_msg.getBytes(), encrypt_msg.length());

			if (msg.startsWith("get")) {
				try {
					String fileName = msg.replaceFirst("get", "");
					receiveFile(in, fileName);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				// receive a message from the server
				msg = new String(receive());
				System.out.println("Server: " + aes.decrypt(msg));

				if (decrypt_msg.equals("QUIT"))
					break;
			}
		}
	}

	public static void main(String[] args) {
		new FileClient();
	}
}
