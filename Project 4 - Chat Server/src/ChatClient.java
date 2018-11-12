import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

// added import statements
import java.util.Scanner;

final public class ChatClient {

    private ObjectInputStream sInput;
    private ObjectOutputStream sOutput;
    private Socket socket;

    private final String server;
    private final String username;
    private final int port;

    private ChatClient(String username, int port, String server) {
        this.server = server;
        this.port = port;
        this.username = username;


    }


    /*
     * This starts the Chat Client
     */
    private boolean start() {

        // Create a socket
        try {
            socket = new Socket(server, port);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Connection accepted " + socket.getInetAddress()+ ":" + port);

        // Create your input and output streams
        try {
            sInput = new ObjectInputStream(socket.getInputStream());
            sOutput = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // This thread will listen from the server for incoming messages
        Runnable r = new ListenFromServer();
        Thread t = new Thread(r);
        t.start();

        // After starting, send the clients username to the server.
        try {
            sOutput.writeObject(username);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }


    /*
     * This method is used to send a ChatMessage Objects to the server
     */
    private void sendMessage(ChatMessage msg) {
        try {
            sOutput.writeObject(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /*
     * To start the Client use one of the following command
     * > java ChatClient
     * > java ChatClient username
     * > java ChatClient username portNumber
     * > java ChatClient username portNumber serverAddress
     *
     * If the portNumber is not specified 1500 should be used
     * If the serverAddress is not specified "localHost" should be used
     * If the username is not specified "Anonymous" should be used
     */
    public static void main(String[] args) { // edit: 3
        // Get proper arguments and override defaults
        Scanner in = new Scanner(System.in);



        Socket socket;
        BufferedWriter writer;
        String message;

        String server = "localhost";
        int port = 1500;
        String username = "Anonymous";

        if (args.length == 1) {
            username = args[0];
        }

        if (args.length == 2) {
            username = args[0];
            port = Integer.parseInt(args[1]);
        }

        if (args.length == 3) {
            username = args[0];
            port = Integer.parseInt(args[1]);
            server = args[2];
        }

        // Create your client and start it
        ChatClient client = new ChatClient(username, port, server);



        client.start();

        // Send an empty message to the server

        while (true) {
            System.out.print("> ");
            message = in.nextLine();

            try {

                if (message.equalsIgnoreCase("/logout")) {
                    client.sendMessage(new ChatMessage(message, 1));
                    client.sInput.close();
                    client.sOutput.close();
                    client.socket.close();

                }

                client.sendMessage(new ChatMessage(message, 0));


            } catch (IOException e) {
                System.out.println("Something went wrong in the main of chatClient");
            }
        }


    }


    /*
     * This is a private class inside of the ChatClient
     * It will be responsible for listening for messages from the ChatServer.
     * ie: When other clients send messages, the server will relay it to the client.
     */
    private final class ListenFromServer implements Runnable {
        public void run() {

            try {
                while (true) {
                    String msg = (String) sInput.readObject();
                    System.out.print(msg + "\n");
                }

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }

        }
    }

}
