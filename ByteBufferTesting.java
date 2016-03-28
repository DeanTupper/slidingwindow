import client.SlidingPacket;
import org.apache.commons.lang.ArrayUtils;

import java.net.DatagramPacket;
import java.nio.ByteBuffer;

/**
 * Created by dean on 3/23/16.
 */
public class ByteBufferTesting {
    public static void main(String[] args) {
        SlidingPacket packet = new SlidingPacket(345, 3, new byte[3453]);
        DatagramPacket data = packet.getPacket(null, 3444);
        System.out.println(bytesToLong(ArrayUtils.subarray(data.getData(), 0, 32)));
    }


    public static byte[] longToBytes(Integer x) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(x);
        return buffer.array();
    }

    public static int bytesToLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        return buffer.getInt();
    }
}
