import clienteDiretorio.ClienteServidor;
import gui.InterfaceGrafica;
import mainClient.Cliente;
import mainClient.Ficheiros;
import mainClient.Progress;
import p2pClient.P2PClient;
import p2pServer.P2PServer;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class TheISCTEBay {

	public static void main(String[] args) {

		if(args.length < 4){
			System.out.println("É necessário indicar os seguintes (4() argumentos:\n1 - IP do diretório\n2 - Porto do diretório\n3 - Porto deste cliente para partilha de ficheiros\n4 - diretório com ficheiros partilhados\n");
			System.exit(1);
		}
		else if(args.length > 4){
			System.out.println("Os argumentos a seguir ao quarto foram ignorados");
		}

		String ip = args[0];

		String path = Paths.get("").toAbsolutePath().toString() + File.separator + args[3] + File.separator;
		File directory = new File(String.valueOf(path));
		if(!directory.exists()){
			directory.mkdir();
		}

		System.out.println(path);
		int diretorioPort = Integer.parseInt(args[1]);
		int p2pPort = Integer.parseInt(args[2]);

		startFicheiro(path);
		startClient(p2pPort);
		startClienteServidor(ip, diretorioPort);
		startP2PClient();
		startProgress();
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				startGUI();
			}
		});
		startP2PServer();
	}

	private static void startClient(int port){
		new Cliente(port);
	}

	private static void startFicheiro(String path){
		new Ficheiros(path);
	}

	private static void startClienteServidor(String ip, int port){
		new ClienteServidor(ip, port);
	}

	private static void startP2PClient(){
		new P2PClient();
	}

	private static synchronized void startProgress(){
		new Progress();
	}

	private static synchronized void startGUI(){
		new InterfaceGrafica();
	}

	private static void startP2PServer() {
		try {
			new P2PServer();
		} catch (
				IOException e) {
			JOptionPane.showMessageDialog(null, "Erro na configuração de ligações, o cliente não é capaz de partilhar ficheiros\n", "Erro", JOptionPane.ERROR_MESSAGE);
		}
	}




}