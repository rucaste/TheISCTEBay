package p2pServer;

import estruturas.FileBlockRequestMessage;
import estruturas.FileDetails;
import estruturas.WordSearchMessage;
import estruturasDeCoordenacao.ThreadPool;

import javax.swing.*;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class P2PClientHandler implements Runnable{

    private static P2PClientHandler instance;

    private static final int NUMBER_OF_THREADS = 5;

    private Socket socket;
    private ObjectOutputStream objectOutput;
    private ThreadPool threadPool;

    P2PClientHandler() {
        instance = this;
        threadPool = new ThreadPool(NUMBER_OF_THREADS);
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
        while (!Thread.currentThread().isInterrupted()) {
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
                    P2PUpload p2PUpload = new P2PUpload((FileBlockRequestMessage)obj);
                    threadPool.enqueue(p2PUpload);
                }

            } catch (ClassNotFoundException | EOFException ignore){} catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Erro na partilha de ficheiross\n", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


}
