package p2pServer;

import estruturas.FileBlockRequestMessage;
import estruturas.FileDetails;
import estruturas.WordSearchMessage;
import estruturasDeCoordenacao.ThreadPool;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class P2PClientHandler implements Runnable{

    private static P2PClientHandler instance;

    private Socket socket;
    private ObjectOutputStream objectOutput;
    private ThreadPool threadPool;

    public P2PClientHandler() {
        instance = this;
        threadPool = new ThreadPool(5);
    }

    static P2PClientHandler getInstance(){
        return instance;
    }

    ObjectOutputStream getObjectOutput() {
        return objectOutput;
    }

    private void esperarPorConexao() throws IOException {
        socket = P2PServer.getInstance().getServerSocket().accept();
    }

    @Override
    public void run() {
        while (true) {
            try {
                esperarPorConexao();
                ObjectInputStream objectInput = new ObjectInputStream(socket.getInputStream());
                objectOutput = new ObjectOutputStream(socket.getOutputStream());
                Object obj = objectInput.readObject();

                if(obj instanceof WordSearchMessage) {
                    String nomeDoFicheiro = ((WordSearchMessage)obj).getNome();
                    List<FileDetails> resposta = P2PServer.getInstance().getFileDetailsFromFileName(nomeDoFicheiro);
                    objectOutput.writeObject(resposta);
                }

                else if(obj instanceof FileBlockRequestMessage){
                    /*Thread thread = new Thread(new P2PUpload(this, (FileBlockRequestMessage)obj));
                    thread.start();*/
                    P2PUpload p2PUpload = new P2PUpload((FileBlockRequestMessage)obj);
                    threadPool.enqueue(p2PUpload);
                }
            } catch (ClassNotFoundException | IOException e){
                e.printStackTrace();
            }
        }
    }


}
