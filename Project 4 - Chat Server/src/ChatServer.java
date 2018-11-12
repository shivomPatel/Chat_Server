import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

// added import statements
import java.text.SimpleDateFormat;
import java.util.Date;

final public class ChatServer {
    private static Object o = new Object();
    private static int uniqueId = 0;
    private final List<ClientThread> clients = new ArrayList<>();
    private final int port;

    String pattern = "HH:mm:ss";
    SimpleDateFormat sdf = new SimpleDateFormat(pattern);
    Date date = new Date();


    private ChatServer(int port) {
        this.port = port;
        System.out.println("Starting Server...");
    }

    /*
     * This is what starts the ChatServer.
     * Right now it just creates the socketServer and adds a new ClientThread to a list to be handled
     */
    private void start() { // Does this need to be surrounded by a while loop

            try {

                ServerSocket serverSocket = new ServerSocket(port);

                while(true){
                    System.out.println("Server waiting for Clients on port " + serverSocket.getLocalPort() + ".");
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

        int serverNum = 1500;

        if (args.length == 1) {
            serverNum = Integer.parseInt(args[0]);
        }

        ChatServer server = new ChatServer(serverNum);
        server.start();
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
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        /*
         * This is what the client thread actually runs.
         */
        @Override
        public void run() {
            // Read the username sent to you by client


            while(true) {

                synchronized (o) {
                    try {
                        cm = (ChatMessage) sInput.readObject();
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }

                    if (cm.getType() == 1) {
                       close();
                    }
                    broadcast(sdf.format(date) + " " + username + ": " + cm.getMsg());
                }
            }

            // Send message back to the client

        }


        public boolean writeMessage(String msg) {
            try {
                sOutput.writeObject(msg);
            } catch (IOException e) {

            }

            if (this.socket.isConnected()) {
                return true;
            } else {
                return false;
            }
        }

        public void close(){

            try {
                sOutput.close();
                sInput.close();
                socket.close();
            } catch(IOException e){

            }
        }
    }

    public void broadcast(String message) {

        synchronized (o) {
            for (int i = 0; i < clients.size(); i++) {
                clients.get(i).writeMessage(message);
            }

            System.out.println(sdf.format(date) + ": " + message);
        }

    }

    public void remove(int id){
        synchronized (o){

            for (int i = 0; i <clients.size() ; i++) {
                if(clients.get(i) == clients.get(id)){
                    clients.remove(id);
                    return;
                }

            }
        }
    }


}
