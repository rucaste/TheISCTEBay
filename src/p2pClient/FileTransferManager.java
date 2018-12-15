package p2pClient;

import estruturas.ByteArray;
import estruturas.ClienteDetails;
import estruturas.FileBlockRequestMessage;
import estruturas.FileDetails;
import estruturasDeCoordenacao.Semaphore;
import estruturasDeCoordenacao.SingleBarrier;
import mainClient.Progress;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Thread.sleep;


public class FileTransferManager {

    private static final int BLOCK_SIZE = 1024;

    private FileDetails fileDetails;
    private FileBlockRequestMessage[] fileBlockRequestMessageArray;
    private ByteArray[] file;
    private Map<ClienteDetails, Integer> clientsMap;
    private int countDone = 0;
    private long startTime;

    private Semaphore semaphore;
    private SingleBarrier p2pDownloadCounterBarrier;
    private SingleBarrier waitingForSaveBarrier;
    private Semaphore countDoneAcessSemaphore;

    FileTransferManager(FileDetails fileDetails, List<ClienteDetails> lista){
        this.startTime = System.currentTimeMillis();
        this.fileDetails = fileDetails;
        this.fileBlockRequestMessageArray = new FileBlockRequestMessage[(int)Math.ceil(fileDetails.getTamanho()/BLOCK_SIZE)];
        for(int i = 0; i< this.fileBlockRequestMessageArray.length; i++){
            int length;
            if(i < this.fileBlockRequestMessageArray.length-1){
                 length = BLOCK_SIZE;
            }
            else {
                if ((int)fileDetails.getTamanho() % BLOCK_SIZE == 0){
                    length = BLOCK_SIZE;
                }
                else {
                    length = (int)fileDetails.getTamanho() % BLOCK_SIZE;
                }
            }
            this.fileBlockRequestMessageArray[i] = new FileBlockRequestMessage(fileDetails, i*BLOCK_SIZE, length);
        }
        this.file = new ByteArray[this.fileBlockRequestMessageArray.length];

        this.clientsMap = new HashMap<>();
        this.addToMap(lista);

        this.semaphore = new Semaphore(1);
        this.countDoneAcessSemaphore = new Semaphore(1);
        this.p2pDownloadCounterBarrier = new SingleBarrier(this.getNumberOfBlocks(), 1);
        this.waitingForSaveBarrier = new SingleBarrier(1, 1);
        this.setFractionDone();
    }

    long getStartTime(){
        return this.startTime;
    }

    private void addToMap(List<ClienteDetails> lista){
        for(ClienteDetails clienteDetails : lista){
            this.clientsMap.put(clienteDetails, 0);
        }
    }

    FileDetails getFileDetails() {
        return fileDetails;
    }

    private int getNumberOfBlocks(){
        return this.fileBlockRequestMessageArray.length;
    }

    synchronized Semaphore getSemaphore(){
        return this.semaphore;
    }

    synchronized SingleBarrier getP2pDownloadCounterBarrier(){
        return this.p2pDownloadCounterBarrier;
    }

    SingleBarrier getWaitingForSaveBarrier() {
        return waitingForSaveBarrier;
    }

    synchronized FileBlockRequestMessage getFileBlockRequestMessage(){
        if(this.countDone < this.fileBlockRequestMessageArray.length) {
            return this.fileBlockRequestMessageArray[countDone];
        }
        else return null;
    }

    synchronized void addByteArray(ByteArray byteArray, ClienteDetails clienteDetails){
        try {
            sleep((int) (Math.random()*100));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        countDoneAcessSemaphore.acquire();
        file[countDone++] = byteArray;
        countDoneAcessSemaphore.release();
        clientsMap.put(clienteDetails, clientsMap.get(clienteDetails) + 1);
    }

    private void setFractionDone(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    countDoneAcessSemaphore.acquire();
                    Progress.getInstance().setFractionDone((float) countDone / (float) fileBlockRequestMessageArray.length);
                    countDoneAcessSemaphore.release();
                }
            }
        });
        thread.start();
    }

    byte[] getFile(){
        byte[] filearray = new byte[(int)fileDetails.getTamanho()];
        for(int i = 0; i < file.length; i++){
            byte[] bytearray = file[i].getArray();
            System.arraycopy(bytearray, 0, filearray, i * BLOCK_SIZE, bytearray.length);
        }
        return filearray;
    }

    String getFinalMessage() {
        StringBuilder stringBuilder = new StringBuilder("Descarga Completa\n");
        for(Map.Entry<ClienteDetails, Integer> client: clientsMap.entrySet()){
            stringBuilder.append("Fornecedor [endereço=").append(client.getKey().getIP()).append(", porto=").append(client.getKey().getPorto()).append(":").append(client.getValue()).append("\n");
        }
        return stringBuilder.toString();
    }

    @Override
    public String toString() {
        return this.fileDetails.toString();
    }
}
