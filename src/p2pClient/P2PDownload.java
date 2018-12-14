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

        while (!stopped){
            try {
                this.socket = new Socket(clienteDetails.getIP(), clienteDetails.getPorto());
                this.socket.setSoTimeout(5000);
                ObjectOutputStream objectOutput = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream objectInput = new ObjectInputStream(socket.getInputStream());

                this.fileTransferManager.getSemaphore().acquire();
                objectOutput.writeObject(fileTransferManager.getFileBlockRequestMessage());
                obj = objectInput.readObject();
                fileTransferManager.addByteArray((ByteArray)obj, clienteDetails);
                this.fileTransferManager.getP2pDownloadCounterBarrier().barrierPost();
                this.fileTransferManager.getSemaphore().release();

            } catch (IOException | ClassNotFoundException e){
                this.fileTransferManager.getSemaphore().release();
                this.stopped = true;
                break;
            } finally {
                try {
                    this.socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println(Thread.currentThread().getName()  + " foi parada");
    }

}