package p2pClient;

import estruturas.FileDetails;
import mainClient.Ficheiros;

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


    public SaveFile(FileTransferManager fileTransferManager){
        this.fileTransferManager = fileTransferManager;
        this.fileDetails = fileTransferManager.getFileDetails();
        try {
            this.fileOutputStream = new FileOutputStream(Ficheiros.getInstance().getFilesPath() + "/" + this.fileDetails.getNome());
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(null, "Não foi possível gravar o ficheiro transferido\nRepita a operação", "Erro", JOptionPane.ERROR_MESSAGE);
        }
        this.fileArray = new byte[(int)fileDetails.getTamanho()];
        this.count = (int)fileDetails.getTamanho();
    }

    private synchronized void save(byte[] array) throws IOException {
        System.arraycopy(array, 0, this.fileArray, (int) (long) 0, array.length);
        this.count = this.count - array.length;
        if(count <= 0){
            this.writeFile();
        }
    }

    private void writeFile() throws IOException {
        this.fileOutputStream.write(this.fileArray);
        this.fileOutputStream.flush();
        Ficheiros.getInstance().addFile(this.fileDetails);
    }

    void interruptDownloadThreads(List<Runnable> p2PDownloadsList, List<Thread> threadList) throws InterruptedException {
        this.fileTransferManager.getWaitingForSaveBarrier().barrierWait();
        for(Thread thread: threadList){
            thread.interrupt();
        }
        for(Runnable p2PDownloads: p2PDownloadsList){
            ((P2PDownload)p2PDownloads).stopThread();
        }
    }

    @Override
    public void run() {
        long start = this.fileTransferManager.getStartTime();
        try {
            this.fileTransferManager.getP2pDownloadCounterBarrier().barrierWait();
            this.fileTransferManager.getWaitingForSaveBarrier().barrierPost();
            save(this.fileTransferManager.getFile());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Não foi possivel gravar o ficheiro transferido\n", "Erro", JOptionPane.ERROR_MESSAGE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String tempo = "Tempo decorrido:" + (System.currentTimeMillis()- start)/1000 + "s\n";
        JOptionPane.showMessageDialog(null, this.fileTransferManager.getFinalMessage() + tempo, "Download concluído", JOptionPane.INFORMATION_MESSAGE);
    }
}
