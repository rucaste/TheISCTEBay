package p2pClient;

import clienteDiretorio.ClienteServidor;
import estruturas.ClienteDetails;
import estruturas.FileDetails;
import estruturas.WordSearchMessage;
import mainClient.Cliente;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.*;


public class P2PClient {

    private static P2PClient instance;

    private Map<FileDetails, List<ClienteDetails>> mapaDeFicheiros;
    private int p2pPort;

    public P2PClient(){
        instance = this;
        this.mapaDeFicheiros = new HashMap<>();
        this.p2pPort = Cliente.getInstance().getP2pPorto();
    }

    public static P2PClient getInstance(){
        return instance;
    }

    Map<FileDetails, List<ClienteDetails>> getMapaDeFicheiros(){
        return this.mapaDeFicheiros;
    }

    List<ClienteDetails> getCLientesParaDownload(FileDetails fileDetails){
        return mapaDeFicheiros.get(fileDetails);
    }

    public synchronized void transferFile(FileDetails fileDetails) {
        List<ClienteDetails> lista = getCLientesParaDownload(fileDetails);
        FileTransferManager fileTransferManager = new FileTransferManager(fileDetails, lista);
        List<Runnable> p2PDownloads= new ArrayList<>();

        for (ClienteDetails fonteParaDownload : lista) {
            P2PDownload p2PDownload = new P2PDownload(fonteParaDownload, fileTransferManager);
            Thread t = new Thread(p2PDownload, "P2PDownlaod_" + fonteParaDownload.toString());
            t.start();
            p2PDownloads.add(p2PDownload);
        }

        SaveFile saveFile = new SaveFile(fileTransferManager);
        new Thread(saveFile).start();

        try {
            saveFile.interruptDownloadThreads(p2PDownloads);
        } catch (InterruptedException e) {
            JOptionPane.showMessageDialog(null, "Não foi possivel gravar o ficheiro transferido\n", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    public synchronized void findFile(String nomeDoFicheiro) throws IOException, ClassNotFoundException {

        WordSearchMessage wordSearchMessage = new WordSearchMessage(nomeDoFicheiro);

        getMapaDeFicheiros().clear();
        ClienteServidor.getInstance().sendCLT();
        for (ClienteDetails fonte : ClienteServidor.getInstance().getClientsList()) {
            if (p2pPort != fonte.getPorto()) {
                Socket socket = new Socket(fonte.getIP(), fonte.getPorto());
                ObjectOutputStream objectOutput = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream objectInput = new ObjectInputStream(socket.getInputStream());
                objectOutput.writeObject(wordSearchMessage);
                List resposta = (List) objectInput.readObject();

                for (Object ficheiro: resposta){
                    boolean existe = false;
                    for(Map.Entry registo: getMapaDeFicheiros().entrySet()) {
                        if (((FileDetails) ficheiro).sameFile((FileDetails) registo.getKey())) {
                            List<ClienteDetails> lista = (LinkedList)registo.getValue();
                            lista.add(fonte);
                            existe = true;
                            break;
                        }
                    }
                    if(!existe){
                        List<ClienteDetails> lista =  new LinkedList<ClienteDetails>();
                        lista.add(fonte);
                        getMapaDeFicheiros().put((FileDetails)ficheiro, lista);
                    }
                }
            }
        }
    }

    public ArrayList<FileDetails> getmapaDeFicheiros(){
        return new ArrayList<>(getMapaDeFicheiros().keySet());
    }

}

