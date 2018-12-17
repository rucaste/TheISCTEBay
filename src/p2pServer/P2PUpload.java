package p2pServer;

import estruturas.ByteArray;
import estruturas.FileBlockRequestMessage;
import estruturasDeCoordenacao.SingleLock;
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
        SingleLock lock = Ficheiros.getInstance().getLock(fileBlockRequestMessage.getFileDetails());

        try {
            lock.acquire();
            file = new RandomAccessFile(path, "r");
        } catch (FileNotFoundException | InterruptedException e) {
            lock.release();
        }

        byte[] fileContents = new byte[fileBlockRequestMessage.getLength()];
            try {
                file.seek(fileBlockRequestMessage.getOffset());
                for (int i = 0; i < fileContents.length; i++) {
                    fileContents[i] = file.readByte();
                }
                lock.release();
                ByteArray byteArray = new ByteArray(fileContents);
                P2PClientHandler.getInstance().getObjectOutput().writeObject(byteArray);
                P2PClientHandler.getInstance().getObjectOutput().flush();
            } catch (IndexOutOfBoundsException | IOException e){
                lock.release();
            }
    }
}
