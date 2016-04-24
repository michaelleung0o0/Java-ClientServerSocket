/**
 * Created by michaelleung on 9/4/16.
 */
import com.google.gson.Gson;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.prefs.Preferences;

public class MyThread extends Thread {
    final int login = 0;
    final int command = 1;
    int state = login;

    String user;
    String password;
    String dirName = System.getProperty("user.dir"); //"C:/";

    ArrayList<FileList> fileList = new ArrayList<FileList>();
    Socket clientSocket = null;
    DataInputStream in;
    DataOutputStream out;

    AES aes = new AES(Config.key);

    public MyThread(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        System.out.println("Established connection to client " +
                clientSocket.getInetAddress().getHostAddress() + ":" +
                clientSocket.getPort());

        try {
            out = new DataOutputStream(clientSocket.getOutputStream());
            in = new DataInputStream(clientSocket.getInputStream());
            process();
        } catch (Exception e) {
            System.out.println("Connection terminated!");
        } finally {
            disconnect();
        }
    }

    public void sendMsg(byte[] data, int len) throws IOException {
        out.writeInt(len);
        out.write(data, 0, len);
        out.flush();
    }

    public void send(DataOutputStream out, String fileName ) throws Exception {
            System.out.println("path = " + dirName + "/" + fileName);
            File myFile = new File(dirName + "/" + fileName);
            byte[] mybytearray = new byte[(int) myFile.length() + 1];
            FileInputStream fis = new FileInputStream(myFile);
            BufferedInputStream bis = new BufferedInputStream(fis);
            bis.read(mybytearray, 0, mybytearray.length);
            System.out.println("Sending...");
            out.write(mybytearray, 0, mybytearray.length);
            out.flush();
    }

    public byte[] receive() throws IOException {
        byte[] data;
        int size;
        int len;

        // TODO: TASK 4 - Receive data and store them in the byte array

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

    private void process() throws IOException {
        String send_msg;
        while (true) {
            String line = aes.decrypt(new String(receive()));
            if (line.equals("QUIT")) {
                break;
            }

            switch (state) {
                case login: {
                    String[] data = line.split(",");
                    user = data[0];
                    password = data[1];

                    if (user == null) {
                        user = line;
                    }
                    else if (password == null) {
                        password = line;
                    }
                    else {
                        if (validUser(user, password)) {
                            state = command;
//                            System.out.println("state = " + state + " login success" );
                            File f = new File( dirName );
                            fileList = Utils.folder(f);

                            Gson gson = new Gson();
                            send_msg = aes.encrypt(gson.toJson(fileList));
                            sendMsg(send_msg.getBytes(), send_msg.length());
                        }
                        else {
                            disconnect();
                        }
                    }
                    break;
                }
                case command: {
                    System.out.println("FileClient: " + line);

                    if (line.startsWith("get")) {
                            try {
                                String fileName = line.replaceFirst("get", Config.get);
                                send(out, fileName.trim());
                                disconnect();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
//                        }
                    } else {
                        send_msg = aes.encrypt(line);
                        sendMsg(send_msg.getBytes(), send_msg.length());
                    }
                    break;
                }
            }
        }

        send_msg = aes.encrypt("QUIT");
        sendMsg(send_msg.getBytes(), send_msg.length());
    }

    Boolean validUser(String user,String pw){
        Preferences prefs = Preferences.userNodeForPackage(FileServer.class);
        String valid_user = prefs.get(Config.PREF_USER, Config.default_admin);
        String valid_password = prefs.get(Config.PREF_PASSWORD, Config.default_admin);

        if(user.equals(valid_user) && pw.equals(valid_password)){
            return true;
        }
        else{
            return false;
        }
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
}
