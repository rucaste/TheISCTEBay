package p2pServer;

import estruturas.FileDetails;
import mainClient.Cliente;
import mainClient.Ficheiros;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;


public class P2PServer{

    private static P2PServer instance;

    private ServerSocket serverSocket;

    public P2PServer() throws IOException {
        instance = this;
        serverSocket = new ServerSocket( Cliente.getInstance().getP2pPorto());
        criarP2PClientHandler();
    }

    static P2PServer getInstance(){
        return instance;
    }

    ServerSocket getServerSocket(){
        return this.serverSocket;
    }

    List<FileDetails> getFileDetailsFromFileName(String nomeDoFicheiro){
        return Ficheiros.getInstance().getFileNames(nomeDoFicheiro);
    }

    private void criarP2PClientHandler() throws IOException {
        P2PClientHandler p2PClientHandler = new P2PClientHandler();
        Thread thread = new Thread(p2PClientHandler, "Thread p2PClientHandler S:" + Cliente.getInstance().getP2pPorto() + " C_");
        thread.start();
    }



}
