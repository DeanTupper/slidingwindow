package client;

/**
 * Created by dean on 3/21/16.
 */
public interface FileUploader {
    public void uploadFile(byte[] fileToUpload, String fileName, int fileSize);
}
