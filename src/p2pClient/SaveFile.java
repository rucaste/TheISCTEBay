package p2pClient;

import estruturas.FileDetails;
import mainClient.Ficheiros;
import mainClient.Progress;

import javax.swing.*;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;


public class SaveFile implements Runnable{

    private FileDetails fileDetails;
    private FileOutputStream fileOutputStream;
    private byte[] fileArray;
    private int count;
    private FileTransferManager fileTransferManager;


    public SaveFile(FileTransferManager fileTransferManager, List<Thread> threadsList){
        this.fileTransferManager = fileTransferManager;
        this.fileDetails = fileTransferManager.getFileDetails();
        try {
            this.fileOutputStream = new FileOutputStream(Ficheiros.getInstance().getFilesPath() + "/" + this.fileDetails.getNome());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        this.fileArray = new byte[(int)fileDetails.getTamanho()];
        this.count = (int)fileDetails.getTamanho();
    }

    private synchronized void save(byte[] array){
        System.arraycopy(array, 0, this.fileArray, (int) (long) 0, array.length);
        this.count = this.count - array.length;
        if(count <= 0){
            this.writeFile();
        }
    }

    private synchronized void writeFile(){
        try {
            this.fileOutputStream.write(this.fileArray);
            Ficheiros.getInstance().addFile(this.fileDetails);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void interruptDownloadThreads(List<Runnable> p2PDownloadsList) {
        this.fileTransferManager.getWaitingForSaveBarrier().barrierWait();
        for(Runnable p2PDownloads: p2PDownloadsList){
            ((P2PDownload)p2PDownloads).stopThread();
        }
    }

    @Override
    public void run() {
        long start = this.fileTransferManager.getStartTime();
        this.fileTransferManager.getP2pDownloadCounterBarrier().barrierWait();
        this.fileTransferManager.getWaitingForSaveBarrier().barrierPost();
        save(this.fileTransferManager.getFile());
        String tempo = "Tempo decorrido:" + (System.currentTimeMillis()- start)/1000 + "s\n";
        JOptionPane.showMessageDialog(null, this.fileTransferManager.getFinalMessage() + tempo, "Download concluído", JOptionPane.INFORMATION_MESSAGE);
    }
}
