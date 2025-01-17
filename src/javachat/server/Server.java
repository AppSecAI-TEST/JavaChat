package javachat.server;

import java.util.List;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;
import javachat.UniqueIdentifier;

public class Server implements Runnable {
    
    private List<ServerClient> clients = new ArrayList<ServerClient>();
    
    private Date date = new Date();
    private DatagramSocket socket;
    private int port;
    private boolean running = false;
    private Thread run, manage, send, receive;

    public Server(int port) {
        this.port = port;
        try {
            socket = new DatagramSocket(port);
        } catch (SocketException e) {
            e.printStackTrace();
            return;
        }
        run = new Thread(this, "Server");
        run.start();
    }

    @Override
    public void run() {
        running = true;
        System.out.println("Server started on port " + port + " on " + date.toString());
        manageClients();
        receive();
    }
   
//Managing     
    private void manageClients() {
        manage = new Thread("Manage") {
            @Override
            public void run() {
                while (running) {
                    
                }
            }
        };
        manage.start();
    }
    
//Receiving  
    private void receive() {
        receive = new Thread("Receive") {
            @Override
            public void run() {
                while (running) {
                    byte[] data = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(data, data.length);
                    try {
                        socket.receive(packet); //Waits unitl it receives data
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    process(packet);
                    //clients.add(new ServerClient("Antero", packet.getAddress(), packet.getPort(), 50));
                    System.out.println(clients.get(0).address.toString() + ":" + clients.get(0).port);
                }
            }
        };
        receive.start();
    }
    
    private void sendToAll(String message) {
        for (int i = 0; i < clients.size(); i++) {
            ServerClient client = clients.get(i);
            send(message.getBytes(), client.address, client.port);
        }
    }
    
    private void send(byte[] data, final InetAddress address, final int port) {
        send = new Thread("Send") {
            public void run() {
                DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
                try {
                    socket.send(packet);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        send.start();
    }
    
    private void process(DatagramPacket packet) {
        String string = new String(packet.getData());
        string = string.trim();
        if (string.startsWith("/c/")) {
            //UUID id = UUID.randomUUID();
            int id = UniqueIdentifier.getIdentifier();
            clients.add(new ServerClient(string.substring(3, string.length()), packet.getAddress(), packet.getPort(), id));
            System.out.println("Identifier: " + id);
            System.out.println(string.substring(3, string.length()));
        } else if (string.startsWith("/m/")) {
            sendToAll(string);
        } else {
            System.out.println(string);
        }
    }
    
}
