package p2pClient;

import estruturas.ByteArray;
import estruturas.ClienteDetails;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class P2PDownload implements Runnable {

    private ClienteDetails clienteDetails;
    private FileTransferManager fileTransferManager;
    private boolean stopped;
    private Socket socket;

    public P2PDownload(ClienteDetails clienteDetails, FileTransferManager fileTransferManager) {
        this.stopped = false;
        this.clienteDetails = clienteDetails;
        this.fileTransferManager = fileTransferManager;
    }

    void stopThread(){
        this.stopped = true;
    }

    @Override
    public void run() {

        Object obj = null;
        ObjectOutputStream objectOutput = null;
        ObjectInputStream objectInput = null;

        while (!stopped){
            try {
                this.socket = new Socket(clienteDetails.getIP(), clienteDetails.getPorto());
                this.socket.setSoTimeout(10000);
                objectOutput = new ObjectOutputStream(socket.getOutputStream());
                objectInput = new ObjectInputStream(socket.getInputStream());
                this.fileTransferManager.getSingleCountSemaphore().acquire();
                objectOutput.writeObject(fileTransferManager.getFileBlockRequestMessage());

                obj = objectInput.readObject();
                fileTransferManager.addByteArray((ByteArray)obj, clienteDetails);
                this.fileTransferManager.getP2pDownloadCounterBarrier().barrierPost();
                this.fileTransferManager.getSingleCountSemaphore().release();

            } catch (IOException | ClassNotFoundException | InterruptedException e){
                this.fileTransferManager.getSingleCountSemaphore().release();
                this.stopped = true;
                break;
            }
        }
        System.out.println(Thread.currentThread().getName()  + " foi parada");
    }

}
