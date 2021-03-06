package clienteDiretorio;

import estruturas.ClienteDetails;
import mainClient.Cliente;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.sleep;

public class ClienteServidor{

    private static ClienteServidor instance;

    private static final long KEEP_ALIVE_PERIOD = 60000 * 2 /*(minutes)*/;

    private String diretorioIP;
    private int diretorioPorto;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private List<ClienteDetails> outrosClientes;

    public ClienteServidor(String ip, int porto) {
        instance = this;
        this.diretorioIP = ip;
        this.diretorioPorto = porto;
        this.outrosClientes = new ArrayList<>();
        configurarLigacao();
    }

    public static ClienteServidor getInstance(){
        return instance;
    }

    private void configurarStreams() throws IOException {
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
    }

    public synchronized List<ClienteDetails> getClientsList(){
        return this.outrosClientes;
    }

    private String receberMensagem() throws IOException {
        return in.readLine(); }

    private void enviarMensagemRegistoDir(String mensagem) throws IOException {
        out.println(mensagem);
        out.flush();
        keepAlive();
    }

    private void configurarLigacao() {
        try {
            socket = new Socket(this.diretorioIP, this.diretorioPorto);
            configurarStreams();
            enviarMensagemRegistoDir("INSC " + this.diretorioIP + " " + Cliente.getInstance().getP2pPorto());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Erro na ligação ao diretório, não foi possível adicionar este cliente à rede\nO Sistema vai ser encerrado\n", "Erro", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            System.exit(1);
        }
    }

    public synchronized void removeRegister() {
        out.println("BYE");
        out.flush();
    }

    public synchronized void sendCLT(){
        out.println("CLT");
        out.flush();
        String mensagem = "";
        while(!mensagem.equals("END")) {
            boolean repeated = false;
            try {
                mensagem = receberMensagem();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Não foi possivel obter informações do diretório\nRepita a pesquisa\n", "Aviso", JOptionPane.WARNING_MESSAGE);
                e.printStackTrace();
            }
            if (mensagem.split("\\s+")[0].equals("CLT")) {
                String ip = mensagem.split("\\s+")[1].replace(" ", "");
                int port = Integer.parseInt(mensagem.split("\\s+")[2].replace(" ", ""));
                for (ClienteDetails clienteDetails : this.outrosClientes) {
                    if (clienteDetails.getIP().equals(ip) && clienteDetails.getPorto() == port) {
                        repeated = true;
                        break;
                    }
                }
                if (!repeated) {
                    this.outrosClientes.add(new ClienteDetails(ip, port));
                }
            }
        }
    }

    private void keepAlive(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        sleep(KEEP_ALIVE_PERIOD);
                    } catch (InterruptedException e) {
                        Thread.currentThread().start();
                    }
                    out.println("HI");
                    out.flush();
                }
            }
        });
        thread.start();
    }

}

