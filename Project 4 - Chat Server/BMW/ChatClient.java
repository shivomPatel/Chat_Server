import java.awt.event.HierarchyBoundsAdapter;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

final class ChatClient {
    private ObjectInputStream sInput;
    private ObjectOutputStream sOutput;
    private Socket socket;

    private final String server;
    private final String username;
    private final int port;

    private ChatClient(String server, int port, String username) {
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
//            e.printStackTrace();
            System.out.println("No server was found at " + server + " on port " + port);
            return false;
        }

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
    public static void main(String[] args) {


        // Get proper arguments and override defaults
        String username;
        int port_number;
        String server_address;

        if (args.length > 0) {
            username = args[0];
        } else {
            username = "Anonymous";
        }


        if (args.length > 1) {
            port_number = Integer.parseInt(args[1]);
        } else {
            port_number = 1500;
        }

        if (args.length > 2) {
            server_address = args[2];
        } else {
            server_address = "localhost";
        }


        // Create your client and start it
        ChatClient client = new ChatClient(server_address, port_number, username);
        if (!client.start()) {
            return;
        }

        // Print out server connection information
        System.out.println("Connection accepted " + client.socket.getInetAddress());


        // Send message to the server
        Scanner scanner = new Scanner(System.in);
        String message;


        while (scanner.hasNextLine()) {
            message = scanner.nextLine();

            String[] input = message.split(" ");
            if (input[0].equals("/list")) {
                client.sendMessage(new ChatMessage(3));
            } else if (message.toLowerCase().equals("/logout")) {
                client.sendMessage(new ChatMessage(1, "Server has closed the connection."));

//                System.out.println("Server has closed the connection.");
                try {
                    client.sInput.close();
                    client.sOutput.close();
                    client.socket.close();
                    scanner.close();
                    return;
                } catch (IOException e) {
                    System.out.println("Something went wrong with logging out.");
                }
            } else if (input[0].equals("/msg")) {
                String to = input[1];
                client.sendMessage(new ChatMessage(2, input[2], to));
            } else {
                client.sendMessage(new ChatMessage(0, message));
            }

            // TODO if server disconnects client program doesn't quit

        } //end while
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
                    System.out.print(msg);
                }
            } catch (IOException | ClassNotFoundException e) {
//                e.printStackTrace();
                System.out.println("Server has closed the connection.");
            }
        }
    }
}
