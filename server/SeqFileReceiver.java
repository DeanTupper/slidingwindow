package server;

import org.apache.commons.lang.ArrayUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Random;

/**
 * Created by dean on 3/23/16.
 */
public class SeqFileReceiver implements FileReceiver {
    private final boolean fakeDrop;
    private final DatagramSocket outSocket;
    private final DatagramSocket inSocket;
    private final int port = 3244;
    private final int bufferSize;
    private InetAddress address;
    private ServerBuffer buffer;
    private Random rand = new Random();
    private boolean sentinel = true;

    public SeqFileReceiver(boolean fakeDrop, int bufferSize) throws SocketException, UnknownHostException {
        this.fakeDrop = fakeDrop;
        this.bufferSize = bufferSize;
//        address = InetAddress.getByName("wolf.cs.oswego.edu");
        address = Inet6Address.getByName("fe80::62eb:69ff:fe82:388a");
        inSocket = new DatagramSocket(port);
        outSocket = new DatagramSocket();
        System.out.println("server");
    }

    @Override
    public void beginServer() {
        while (sentinel) {
            System.out.println("start loop");
            sentinel = false;
            try {
                byte[] inArray = new byte[20];
                DatagramPacket inPacket = new DatagramPacket(inArray, 0, 20);
                inSocket.receive(inPacket);
                long time = System.nanoTime();
                buffer = createServerBuffer(inPacket);
                inSocket.send(inPacket);
                System.out.println("START");
                while (!buffer.isFinished()) {
                    inArray = new byte[64000];
                    inPacket = new DatagramPacket(inArray, 0, 64000);
                    inSocket.receive(inPacket);
                    if (buffer.received(inPacket)) {
                        byte[] ackArray = ArrayUtils.subarray(inPacket.getData(), 0, 4);
                        DatagramPacket ackPacket = new DatagramPacket(ackArray, 0, 4, inPacket.getAddress(), 3244);
                        inSocket.send(ackPacket);
                    }
                }
                System.out.println("afterrrrwhile");
                time = System.nanoTime() - time;
                time = time / 1000000000l;
                System.out.println(time);
                System.out.println(buffer.getFileSize());
                double size = buffer.getFileSize() / 1000000;
                System.out.println("total time = " + ((double) (size / time)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private ServerBuffer createServerBuffer(DatagramPacket initialPacket) {
        try {
            byte[] data = initialPacket.getData();
            String fileName = new String(ArrayUtils.subarray(data, 0, 16), "UTF-8");
            int fileSize = bytesToLong(ArrayUtils.subarray(data, 16, 20));
            return new ServerBuffer(fileName, fileSize, bufferSize);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte[] longToBytes(Integer x) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(x);
        return buffer.array();
    }

    public int bytesToLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        return buffer.getInt();
    }
}

