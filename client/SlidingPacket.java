package client;

import org.apache.commons.lang.ArrayUtils;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/**
 * Created by dean on 3/23/16.
 */
public class SlidingPacket {
    private static final long TIMEOUT_TIME = 5000000000l;

    private final Integer position;

    private final Integer seq;

    private final byte[] innerData;
    private final byte[] packetData;
    private long lastTime;

    public SlidingPacket(Integer position, Integer seq, byte[] innerData) {
        this.position = position;
        this.seq = seq;
        this.innerData = innerData;
        this.packetData = this.buildPacket();
    }

    public SlidingPacket(byte[] data) {
        this.position = bytesToLong(ArrayUtils.subarray(data, 0, 4));
        this.seq = bytesToLong(ArrayUtils.subarray(data, 4, 8));
        this.innerData = ArrayUtils.subarray(data, 8, data.length - 1);
        this.packetData = data;
    }

    private byte[] buildPacket() {
        byte[] posBytes = longToBytes(position);
        byte[] seqBytes = longToBytes(seq);
        byte[] headerBytes = ArrayUtils.addAll(posBytes, seqBytes);
        return ArrayUtils.addAll(headerBytes, innerData);
    }

    public byte[] longToBytes(Integer x) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(x);
        return buffer.array();
    }

    public int bytesToLong(byte[] bytes) {
//        for(int i = 0; i < bytes.length; i++)
//        {
//        }
        try {
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            return buffer.getInt();
        } catch (BufferUnderflowException exception) {
            exception.printStackTrace();
            return -1;
        }
    }

    public DatagramPacket getPacket(InetAddress address, int port) {
        lastTime = System.nanoTime();
        return new DatagramPacket(packetData, 0, packetData.length, address, port);
    }

    public boolean timedOut() {
        long currentTime = System.nanoTime();
        long estimate = currentTime - lastTime;
        return (estimate > TIMEOUT_TIME);
    }

    public Integer getPosition() {
        return position.intValue();
    }

    public Integer getSeq() {
        return seq.intValue();
    }

    public byte[] getInnerData() {
        return innerData;
    }
}
