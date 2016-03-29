package server;


import client.SlidingPacket;
import org.apache.commons.lang.ArrayUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by dean on 3/21/16.
 */
public class ServerBuffer {
    private static final int DATAGRAM_SIZE = 64000 - (4 + 4);
    private final ConcurrentHashMap<Integer, SlidingPacket> buffer = new ConcurrentHashMap<>();
    private final int bufferSize;
    private final String fileName;
    private final int fileSize;
    private int lastSeq;
    private byte[] fileToWrite;
    private int nextExpectedSeq = 0;
    private long seq = 0;
    private int offset = 0;
    private boolean finished = false;

    public ServerBuffer(String fileName, int fileSize, int bufferSize) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.bufferSize = bufferSize;
        this.fileToWrite = null;
        this.lastSeq = getLastSeq();
    }

    private int getLastSeq() {
        int numberOfPackets = (fileSize / DATAGRAM_SIZE);
        if (numberOfPackets * DATAGRAM_SIZE < fileSize) {
            numberOfPackets++;
        }
        return numberOfPackets;
    }

    public int getFileSize() {
        return fileSize;
    }

    public boolean received(DatagramPacket packet) {
        boolean received = false;
        SlidingPacket slidingPacket = new SlidingPacket(packet.getData());
        System.out.println("received: " + slidingPacket.getSeq());
        System.out.println("next: " + nextExpectedSeq);
        if (slidingPacket.getSeq() < nextExpectedSeq) {
            received = true;
        } else if (buffer.size() < bufferSize) {
            if (slidingPacket.getPosition() + DATAGRAM_SIZE > fileSize) {
                int actualSize = fileSize - slidingPacket.getPosition();
                slidingPacket = new SlidingPacket(ArrayUtils.subarray(packet.getData(), 0, 8 + actualSize));
            }
            if (buffer.get(slidingPacket.getSeq()) == null) {
//                System.out.println("put " + slidingPacket.getSeq() + " into buffer");
                buffer.put(slidingPacket.getSeq(), slidingPacket);
            }
            received = true;
        } else {
            if (slidingPacket.getSeq() == nextExpectedSeq) {
                writeToFileArray(slidingPacket.getInnerData());
                nextExpectedSeq++;
                received = true;
            } else {
                //received = false and an ack will not be sent
            }
        }
        buildFile();
        return received;
    }

    public void buildFile() {
        for (Map.Entry<Integer, SlidingPacket> e : buffer.entrySet()) {
        }
        while (buffer.containsKey(nextExpectedSeq)) {
            writeToFileArray(buffer.get(nextExpectedSeq).getInnerData());
            buffer.remove(nextExpectedSeq);
            nextExpectedSeq++;


        }
        if (nextExpectedSeq == lastSeq) {
            writeFileToDisc();
            finished = true;
        }
    }

    private void writeFileToDisc() {
        try {
            RandomAccessFile file = new RandomAccessFile(fileName.trim() + "Receive.txt", "rw");
            file.write(fileToWrite, 0, fileToWrite.length);
            file.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeToFileArray(byte[] innerData) {
        if (fileToWrite == null) {
            fileToWrite = innerData;
        } else {
            fileToWrite = ArrayUtils.addAll(fileToWrite, innerData);
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