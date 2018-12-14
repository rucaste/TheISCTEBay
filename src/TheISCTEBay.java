import clienteDiretorio.ClienteServidor;
import mainClient.Cliente;
import mainClient.Ficheiros;
import mainClient.Progress;
import p2pClient.P2PClient;

public class TheISCTEBay {

	private static String path;

	public static void main(String[] args) {
		String ip = args[0];
		int i = Integer.parseInt(args[3]);
		String pathGglobal = "/home/rui/Downloads/TheISCTEBay/";
		path = pathGglobal + "cliente" + i + "Files/" ;
		int diretorioPort = Integer.parseInt(args[1]);
		int p2pPort = Integer.parseInt(args[2]);

		startFIcheiro(path);
		startClient(i, p2pPort);
		startClienteServidos(ip, diretorioPort);
		startP2PClient();
		startProgress();
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


}