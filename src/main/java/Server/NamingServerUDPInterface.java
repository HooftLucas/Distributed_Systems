package Server;

import Messages.Message;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.*;
import java.util.Objects;

public class NamingServerUDPInterface implements Runnable{
    private final NamingServer server;
    private final InetAddress multicastAddress = InetAddress.getByName("255.255.255.255");
    private DatagramSocket socket;
    private final int port = 8000;

    public NamingServerUDPInterface(NamingServer server) throws UnknownHostException, SocketException {
        this.server = server;
        this.socket = new DatagramSocket(this.port);

    }

    /**
     * It takes a message, converts it to a JSON string, converts the string to a byte array, creates a datagram packet
     * with the byte array, the multicast address and the port, and sends the packet
     *
     * @param m The message to be sent
     */
    public void sendMulticast(Message m) throws IOException {
        String json = new Gson().toJson(m);
        byte[] buf = json.getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.multicastAddress, this.port + 1);
        this.socket.send(packet);
        System.out.println("[NS UDP]: Multicast sent type " + m.getType() );

    }

    /**
     * It takes a message, a destination address and a destination port, converts the message to a JSON string, puts the
     * string in a byte array, puts the byte array in a datagram packet, and sends the packet
     *
     * @param m the message to be sent
     * @param destinationAddress The IP address of the destination node
     * @param destinationPort The port to send the message to.
     */
    public void sendUnicast(Message m, InetAddress destinationAddress, int destinationPort) throws IOException {
        String json = new Gson().toJson(m);
        byte[] buf = json.getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, destinationAddress, destinationPort);
        this.socket.send(packet);
        if(!Objects.equals(m.getType(), "PingMessage")) {
            System.out.println("[NS UDP]: Unicast sent to " + destinationAddress.toString() +":" + destinationPort
                    + " type " + m.getType() );
        }

    }


    /**
     * It creates a new thread for each request it receives
     */
    @Override
    public void run() {
        try {
            while (true) {
                byte[] buf = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                this.socket.receive(packet);
                if(packet.getLength() == 0) {
                    System.out.println("[NS UDP]: message is empty");
                } else {
                    Thread rq = new Thread(new NamingServerRequestHandler(server, multicastAddress, packet));
                    rq.start();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
