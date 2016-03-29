package client;

import org.apache.commons.lang.ArrayUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.ByteBuffer;

/**
 * Created by dean on 3/21/16.
 */
public class SeqFileUploader implements FileUploader {
    private final boolean fakeDrop;
    private final DatagramSocket outSocket;
    private final DatagramSocket inSocket;
    private final InetAddress address;
    private final int port = 3244;
    private final int bufferSize;


    public SeqFileUploader(boolean fakeDrop, int bufferSize) throws UnknownHostException, SocketException {
        this.fakeDrop = fakeDrop;
        this.bufferSize = bufferSize;
//        address = InetAddress.getByName("wolf.cs.oswego.edu");
        address = Inet6Address.getByName("fe80::62eb:69ff:fe82:388a");
        inSocket = new DatagramSocket(port);
        outSocket = new DatagramSocket();

    }

    @Override
    public void uploadFile(byte[] fileToUpload, String fileName, int fileSize) {
        ClientBuffer buffer = new ClientBuffer(fileToUpload, bufferSize);
        Thread thread = new Thread(new AckReciever(buffer));
        thread.start();
        // Initial Send
        {
            try {
                fileName = padFileName(fileName);
                byte[] intialBytes = ArrayUtils.addAll(fileName.getBytes("UTF-8"), longToBytes(fileSize));
                DatagramPacket initialPacket = new DatagramPacket(intialBytes, intialBytes.length, address, port);
                outSocket.send(initialPacket);
                outSocket.receive(initialPacket);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        while (!buffer.isFinished()) {
            try {
                DatagramPacket next = buffer.next(address, port);
                if (next != null) {
                    outSocket.send(next);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String padFileName(String fileName) {
        for (int i = fileName.length(); i < 16; i++) {
            fileName = fileName + " ";
        }
        return fileName;
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

    private class AckReciever implements Runnable {
        private final ClientBuffer buffer;
        private DatagramPacket inPacket = new DatagramPacket(new byte[32], 0, 32);

        public AckReciever(ClientBuffer buffer) {
            this.buffer = buffer;
        }

        @Override
        public void run() {
            while (!buffer.isFinished()) {
                try {
                    inSocket.receive(inPacket);
                    buffer.received(bytesToLong(inPacket.getData()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
