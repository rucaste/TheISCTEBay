package mainClient;

import gui.InterfaceGrafica;
import p2pClient.P2PClient;
import p2pServer.P2PServer;

import javax.swing.*;
import java.io.IOException;

import static java.lang.Thread.sleep;


public class Cliente {

	private static Cliente instance;

	private int p2pPorto;

	public Cliente(int p2pPorto) {
		instance = this;
		this.p2pPorto = p2pPorto;
		try {
			new P2PServer();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Erro na configuração de ligações, o cliente não é capaz de partilhar ficheiro\n", "Erro", JOptionPane.ERROR_MESSAGE);
		}
		new InterfaceGrafica(p2pPorto);
	}

	public static Cliente getInstance(){
		return instance;
	}

	public int getP2pPorto(){
		return this.p2pPorto;
    }

	public void findFiles(String fileSubString){
		try {
			try {
				P2PClient.getInstance().findFile(fileSubString);
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "Erro na ligação ao fornecedor do ficheiro\n", "Erro", JOptionPane.ERROR_MESSAGE);
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	// TODO apagar no final
	public void printHashMap(){
		System.out.println(P2PClient.getInstance().mapaDeFicheirosToString());
	}

}
