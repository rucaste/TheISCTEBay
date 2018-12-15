import clienteDiretorio.ClienteServidor;
import gui.InterfaceGrafica;
import mainClient.Cliente;
import mainClient.Ficheiros;
import mainClient.Progress;
import p2pClient.P2PClient;

import java.io.File;
import java.nio.file.Paths;

public class TheISCTEBay {

	private static String path;

	public static void main(String[] args) {

		if(args.length < 4){
			System.out.println("É necessário indicar os seguintes (4() argumentos:\n1 - IP do diretório\n2 - Porto do diretório\n3 - Porto deste cliente para partilha de ficheiros\n4 - diretório com ficheiros partilhados\n");
			System.exit(1);
		}
		else if(args.length > 4){
			System.out.println("Os argumentos a seguir ao quarto foram ignorados");
		}

		String ip = args[0];
		int i = Integer.parseInt(args[3]);
		String path = Paths.get("").toAbsolutePath().toString() + File.separator + "IdeaProjects" + File.separator + "TheISCTEBay" + File.separator + "cliente" + i + "Files" + File.separator;
		System.out.println(path);
		int diretorioPort = Integer.parseInt(args[1]);
		int p2pPort = Integer.parseInt(args[2]);

		startFIcheiro(path);
		startClient(i, p2pPort);
		startClienteServidos(ip, diretorioPort);
		startP2PClient();
		startProgress();
		startGUI();
	}



	private static synchronized void startClient(int i, int port){
		new Cliente(8080 + i);
	}

	private static synchronized void startFIcheiro(String path){
		new Ficheiros(path);
	}

	private static synchronized void startClienteServidos(String ip, int port){
		new ClienteServidor(ip, 8080);
	}

	private static synchronized void startP2PClient(){
		new P2PClient();
	}

	private static synchronized void startProgress(){
		new Progress();
	}

	private static synchronized void startGUI(){
		new InterfaceGrafica();
	}


}