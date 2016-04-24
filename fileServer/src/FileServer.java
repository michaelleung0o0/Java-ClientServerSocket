/**
 * Created by michaelleung on 9/4/16.
 */

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.prefs.Preferences;


public class FileServer {
    int portNum = 8999;
    Scanner scanner = new Scanner(System.in);

    public FileServer() throws IOException {
        int action = -1;
        while(action != 0 || action !=1) {
            System.out.print("0) Verify account: \n");
            System.out.print("1) Start server: \n");
            System.out.print("please input the number: ");
            action = Integer.parseInt(scanner.nextLine());
            if (action == 0) {
                createAccount();
            } else if (action == 1) {
                startServer();
            } else {
                System.out.print("Input error,try again \n");
            }
        }
    }

    public void createAccount() throws IOException {
        Preferences prefs = Preferences.userNodeForPackage(FileServer.class);
        String user = "";
        String password = "";

        while(user.isEmpty()){
            System.out.print("Please iput the new username : \n");
            user = scanner.nextLine();
        }
        prefs.put(Config.PREF_USER, user);

        while(password.isEmpty()) {
            System.out.print("Please iput the new password : \n");
            password = scanner.nextLine();
        }
        prefs.put(Config.PREF_PASSWORD, password);

        System.out.println("account updated.\n");
        new FileServer();

    }

    public void startServer() throws IOException {
        ServerSocket serverSocket = new ServerSocket(portNum);

        System.out.println("Listening to TCP port# " + serverSocket.getLocalPort());

        Socket clientSocket = null;


        while (true) {
            clientSocket = serverSocket.accept();
            new MyThread(clientSocket).start();
        }
    }

    public static void main(String[] args) throws IOException {
        new FileServer();
    }
}
