package p2pServer;

import estruturas.ByteArray;
import estruturas.FileBlockRequestMessage;
import estruturasDeCoordenacao.SingleCountSemaphore;
import mainClient.Ficheiros;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;


public class P2PUpload implements Runnable {

    private FileBlockRequestMessage fileBlockRequestMessage;

    P2PUpload(FileBlockRequestMessage fileBlockRequestMessage){
        this.fileBlockRequestMessage = fileBlockRequestMessage;
    }

    @Override
    public void run() {
        String name = fileBlockRequestMessage.getFileDetails().getNome();
        String path = Ficheiros.getInstance().getFilesPath() + "/" + name;
        RandomAccessFile file = null;
        SingleCountSemaphore semaphore = Ficheiros.getInstance().getSemaphore(fileBlockRequestMessage.getFileDetails());

        try {
            semaphore.acquire();
            file = new RandomAccessFile(path, "r");
        } catch (FileNotFoundException | InterruptedException e) {
            semaphore.release();
        }

        byte[] fileContents = new byte[fileBlockRequestMessage.getLength()];
            try {
                file.seek(fileBlockRequestMessage.getOffset());
                for (int i = 0; i < fileContents.length; i++) {
                    fileContents[i] = file.readByte();
                }
                semaphore.release();
                ByteArray byteArray = new ByteArray(fileContents);
                P2PClientHandler.getInstance().getObjectOutput().writeObject(byteArray);
                P2PClientHandler.getInstance().getObjectOutput().flush();
            } catch (IndexOutOfBoundsException | IOException e){
                semaphore.release();
            }
    }
}
