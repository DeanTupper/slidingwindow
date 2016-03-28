import client.FileUploader;
import client.SeqFileUploader;
import server.FileReceiver;
import server.SeqFileReceiver;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;

public class Main {

    private static final int fileSize = 1024 * 1024 * 100;
    private static boolean fakeDrop = false;

    public static void main(String[] args) {
        if (args[0].equals("4")) {
            System.setProperty("java.net.preferIPv4Stack", "false");
        }
        if (args[1].equals("drop")) {
            fakeDrop = true;
        }
        if (args[2].equals("client")) {
            initClient(args[3]);
        } else {
            initServer(args[3]);
        }
    }

    private static void initServer(String type) {
        try {
            FileReceiver server = null;
            if (type.equals("seq")) {
                server = new SeqFileReceiver(fakeDrop);
            } else {

            }
            server.beginServer();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        }

    }

    private static void initClient(String type) {
        try {
            FileUploader client = null;
            String fileName = createFileName();
            byte[] file = createFile(fileName);
            if (type.equals("seq")) {
                client = new SeqFileUploader(fakeDrop);
            } else {

            }
            client.uploadFile(file, fileName, fileSize);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

    }

    private static String createFileName() {
        String date = new SimpleDateFormat("dd_HHmm").format(Calendar.getInstance().getTime());
        return date;
    }

    private static byte[] createFile(String fileName) {
        try {
            System.out.println("starting");
            RandomAccessFile file = new RandomAccessFile(fileName + ".txt", "rw");
            file.setLength(fileSize);
            byte[] fileArray = new byte[fileSize];
//            Random rand = new Random();
//            for(int i = 0; i< 100; i++)
//            {
//                file.writeBytes(String.valueOf(rand.nextInt(10)));
//            }
            Random rand = new Random();
            int size = fileSize / 4;
            for (int i = 0; i < size / 100; i++) {
                file.writeInt(rand.nextInt(9));
            }
//            file.writeBytes("THIS_IS_A_TESTTTTTTTT");
            file.seek(0);
            file.readFully(fileArray);
            file.close();
            System.out.println("done creating file");
            return fileArray;
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        return null;
    }
}
