package p2pClient;

import clienteDiretorio.ClienteServidor;
import estruturas.ClienteDetails;
import estruturas.FileDetails;
import estruturas.WordSearchMessage;
import mainClient.Cliente;

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

    void solicitarInscritos(){
        ClienteServidor.getInstance().sendCLT();
    }

    List<ClienteDetails> getCLientesParaDownload(FileDetails fileDetails){
        return mapaDeFicheiros.get(fileDetails);
    }


    public void transferFile(FileDetails fileDetails) {
        List<ClienteDetails> lista = getCLientesParaDownload(fileDetails);
        FileTransferManager fileTransferManager = new FileTransferManager(fileDetails, lista);
        List<Runnable> p2PDownlaods= new ArrayList<>();
        List<Thread> threadsList = new ArrayList<>();  // necessário ? encerra após p2pdownload concluir ?

        System.out.println(lista);

        for (ClienteDetails fonteParaDownload : lista) {
            P2PDownload p2PDownload = new P2PDownload(fonteParaDownload, fileTransferManager);
            Thread t = new Thread(p2PDownload, "P2PDownlaod_" + fonteParaDownload.toString());
            t.start();
            threadsList.add(t);
            p2PDownlaods.add(p2PDownload);
        }

        SaveFile saveFile = new SaveFile(fileTransferManager, threadsList);
        new Thread(saveFile).start();

        saveFile.interruptDownloadThreads(p2PDownlaods);

    }


    public void findFile(String nomeDoFicheiro) throws IOException, ClassNotFoundException {

        WordSearchMessage wordSearchMessage = new WordSearchMessage(nomeDoFicheiro);

        getMapaDeFicheiros().clear();
        solicitarInscritos();
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

    // TODO apagar no final
    public String mapaDeFicheirosToString(){
        StringBuilder stringBuilder = new StringBuilder();
        Iterator iterator = getMapaDeFicheiros().entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry cliente = (Map.Entry)iterator.next();
            stringBuilder.append(cliente.getKey()).append(" = ").append(cliente.getValue()).append('\n');
        }
        return stringBuilder.toString();
    }

    public ArrayList<FileDetails> getmapaDeFicheiros(){
        return new ArrayList<>(getMapaDeFicheiros().keySet());
    }

}

