package mainClient;

import clienteDiretorio.ClienteServidor;
import p2pClient.P2PClient;

import javax.swing.*;
import java.io.IOException;


public class Cliente {

	private static Cliente instance;

	private int p2pPorto;

	public Cliente(int p2pPorto) {
		instance = this;
		this.p2pPorto = p2pPorto;
	}

	public static Cliente getInstance(){
		return instance;
	}

	public int getP2pPorto(){
		return this.p2pPorto;
    }

	public void findFiles(String fileSubString){
		int i = 0;
		try {
			try {
				P2PClient.getInstance().findFile(fileSubString);
			} catch (IOException e) {
				i++;
				if(i == 5){
					JOptionPane.showMessageDialog(null, "Problemas na ligação aos restantes cliente\nO programa vai ser encerrado", "Erro", JOptionPane.ERROR_MESSAGE);
					System.exit(1);
				}
				ClienteServidor.getInstance().sendCLT();
				findFiles(fileSubString);
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
