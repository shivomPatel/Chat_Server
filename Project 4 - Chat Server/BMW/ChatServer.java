import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

final class ChatServer {
    private static int uniqueId = 0;
    private final List<ClientThread> clients = new ArrayList<>();
    private final int port;
    private ChatFilter filter;

    private static Object obj = new Object();


    private ChatServer(int port, String fileName) {
        this.port = port;
        this.filter = new ChatFilter(fileName);
    }

    /*
     * This is what starts the ChatServer.
     * Right now it just creates the socketServer and adds a new ClientThread to a list to be handled
     */
    private void start() {
        System.out.println("Starting server...");
        try {
            ServerSocket serverSocket = new ServerSocket(port);

            while (true) {
                Socket socket = serverSocket.accept();
                Runnable r = new ClientThread(socket, uniqueId++);
                Thread t = new Thread(r);
                clients.add((ClientThread) r);
                t.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     *  > java ChatServer
     *  > java ChatServer portNumber
     *  If the port number is not specified 1500 is used
     */
    public static void main(String[] args) {
        int port_number = 1500;
        String fileName = "badwords.txt";

        if (args.length > 0) {
            port_number = Integer.parseInt(args[0]);
        }

        if (args.length > 1) {
            fileName = args[1];
        }

        ChatServer server = new ChatServer(port_number, fileName);
        // TODO maybe check if port already being used
        server.start();
    }

    private void list(String username) {
        synchronized (obj) {
            int userID = 0;
            String output = "Current online users:\n";
            for (int i = 0; i < clients.size(); i++) {
                if (clients.get(i).username.equals(username)) {
                    userID = i;
                    continue;
                }
                output += clients.get(i).username + "\n";
            }
            SimpleDateFormat time = new SimpleDateFormat("HH:mm:ss");
            Date now = new Date();
            String message = username + ": " + "/list";
            System.out.print(time.format(now) + ": " + message + "\n");
            clients.get(userID).writeMessage(output);
        }
    }

    private void directMessage(String message, String username, String recipient) {
        synchronized (obj) {
            message = filter.filter(message);
            SimpleDateFormat time = new SimpleDateFormat("HH:mm:ss");
            Date now = new Date();

            // Print to server
            String output = time.format(now) + ": " + username + " -> " + recipient + ": " + message + "\n";

            System.out.print(output);
            for (int i = 0; i < clients.size(); i++) {
                if (clients.get(i).username.equals(username) || clients.get(i).username.equals(recipient)) {
                    clients.get(i).writeMessage(output);
                }
            }
        }
    }

    private void broadcast(String message) {

        synchronized (obj) {
            message = filter.filter(message);
            SimpleDateFormat time = new SimpleDateFormat("HH:mm:ss");
            Date now = new Date();

            // Print to server
            String output = time.format(now) + ": " + message + "\n";

            System.out.print(output);
            for (int i = 0; i < clients.size(); i++) {
                clients.get(i).writeMessage(output);
            }
        }

    }


    /*
     * This is a private class inside of the ChatServer
     * A new thread will be created to run this every time a new client connects.
     */
    private final class ClientThread implements Runnable {
        Socket socket;
        ObjectInputStream sInput;
        ObjectOutputStream sOutput;
        int id;
        String username;
        ChatMessage cm;

        private ClientThread(Socket socket, int id) {
            this.id = id;
            this.socket = socket;
            try {
                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sInput = new ObjectInputStream(socket.getInputStream());
                username = (String) sInput.readObject();

                // Check if username is unique
                for (ClientThread thread: clients) {
                    if (thread.username.equals(this.username)) {
                        boolean notFound = true;
                        int i = 1;
                        while (notFound) {
                            boolean done = true;
                            for (ClientThread thread1: clients) {
                                if (thread1.username.equals(this.username + i)) {
                                    i++;
                                    done = false;
                                    break;
                                }
                            }
                            if (done) {
                                username = username + i;
                                notFound = false;
                            }
                        }
                    }
                }

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        private void remove(int id) {
            synchronized (obj) {
                for (int i = 0; i < clients.size(); i++) {
                    if (clients.get(i).id == id) {
                        clients.remove(i);
                        return;
                    }
                }
            }
        }

        /*
         * This is what the client thread actually runs.
         */
        @Override
        public void run() {

            try {
                cm = (ChatMessage) sInput.readObject();
            } catch (IOException | ClassNotFoundException e) {
//                e.printStackTrace();
                close();
                return;
            }

            while (cm != null) {
                if (cm.getType() == 3) {
                    list(username);
                }
                if (cm.getType() == 1) {
                    close();
                    return;
                } else if (cm.getType() == 2) {
                    directMessage(cm.getMessage(), username, cm.getRecipient());
                } else if (cm.getType() == 0) {
                    broadcast(username + ": " + cm.getMessage());
                }


                try {
                    cm = (ChatMessage) sInput.readObject();
                } catch (IOException | ClassNotFoundException e) {
                    close();
                    return;
                }
            }
        }

        private boolean writeMessage(String msg) {
            if (!socket.isConnected()) {
                return false;
            }
            try {
                sOutput.writeObject(msg);
            } catch (IOException e) {
//                System.out.println("Something went wrong with writeMessage.");
                close();
            }

            return true;
        }

        private void close() {
            try {
                System.out.println(username + " has disconnected.");
                remove(id);
                sOutput.close();
                sInput.close();
                socket.close();
            } catch (IOException e) {
                System.out.println("Could not loggout from server.");
            }
        }


    }
}
