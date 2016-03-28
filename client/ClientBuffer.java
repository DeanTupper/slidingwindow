package client;

import org.apache.commons.lang.ArrayUtils;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by dean on 3/21/16.
 */
public class ClientBuffer {
    private static final int DATAGRAM_SIZE = 64000 - (4 + 4);
    private final ConcurrentHashMap<Integer, SlidingPacket> buffer = new ConcurrentHashMap<>();
    private final int bufferSize;
    private final byte[] fileToUpload;
    private int seq = 0;
    private int offset = 0;
    private boolean finished = false;

    public ClientBuffer(byte[] fileToUpload, int bufferSize) {
        this.bufferSize = bufferSize;
        this.fileToUpload = fileToUpload;
    }

    public DatagramPacket next(InetAddress address, int port) {
        for (Map.Entry<Integer, SlidingPacket> current : buffer.entrySet()) {
            SlidingPacket currentPacket = current.getValue();
            if (currentPacket.timedOut()) {
                return currentPacket.getPacket(address, port);
            }
        }
        if (buffer.size() < bufferSize) {
            if (offset < fileToUpload.length) {
                if (buffer.keySet().size() < bufferSize) {
                    return addToBuffer().getPacket(address, port);
                }
            } else {
                if (buffer.size() == 0) {
                    finished = true;
                }
            }
        }
        return null;
    }

    private SlidingPacket addToBuffer() {
        byte[] current = ArrayUtils.subarray(fileToUpload, offset, offset + DATAGRAM_SIZE);
        SlidingPacket currentPacket = new SlidingPacket(offset, seq, current);
        buffer.put(offset, currentPacket);
//        System.out.println("putting " + currentPacket.getSeq() + " into buffer");
//        printBuffer();
        offset = offset + DATAGRAM_SIZE;
        seq++;
        return currentPacket;
    }

    public void received(int positionReceived) {
        if (buffer.get(positionReceived) != null) {
            System.out.println("received: " + buffer.get(positionReceived).getSeq());
        }
        if (buffer.remove(positionReceived) != null) {
        }
    }

    public boolean isFinished() {
        return finished;
    }

    public void printBuffer() {
        System.out.println("CURRENT BUFFER");
        for (Map.Entry<Integer, SlidingPacket> current : buffer.entrySet()) {
            System.out.println("key: " + current.getKey() + " val: " + current.getValue().getSeq());
        }
        System.out.println("-----------------");
    }
}
