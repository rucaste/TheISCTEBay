package gui;

import clienteDiretorio.ClienteServidor;
import estruturas.FileDetails;
import mainClient.Cliente;
import mainClient.Ficheiros;
import mainClient.Progress;
import p2pClient.P2PClient;

import javax.swing.*;
import javax.xml.bind.Unmarshaller;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

public class InterfaceGrafica extends JFrame {

	private JFrame frame;
	private JTextField textField;
    private JButton buttonProcura;
	private JButton buttonPedidoClt;
    private DefaultListModel<FileDetails> modelo = new DefaultListModel<>();
	private JList<FileDetails> listaFicheiros;
	private JTextArea textArea;

	private JProgressBar jProgressBar = new JProgressBar(0, 100);

    public InterfaceGrafica(int porto) {
		configureFrame(porto);
		configureText();
        configureList();
        configureButtons();
        addListeners();
        configureProgressBar();
        startWorkers();
	}

    private void configureFrame(int porto){
        this.frame = new JFrame(Integer.toString(porto));
        this.frame.setSize(635, 450);
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.getContentPane().setLayout(null);
        this.frame.setVisible(true);
    }

    private void configureText(){
        JLabel labelPalavraChave = new JLabel("Palavra Chave:");
        this.textField = new JTextField();

        labelPalavraChave.setBounds(5, 5, 150, 20);
        frame.add(labelPalavraChave, BorderLayout.NORTH);

        frame.add(textField, BorderLayout.NORTH);
        textField.setBounds(150, 5, 325, 20);
    }

	private void configureList(){
        this.listaFicheiros = new JList<FileDetails>(modelo);
        this.listaFicheiros.setBounds(5, 30, 470, 385);
        frame.add(listaFicheiros, BorderLayout.NORTH);
    }

    private void configureButtons(){
        this.buttonProcura = new JButton("Procurar");
        this.buttonPedidoClt = new JButton("Pedido CLT");
        JButton buttonDownload = new JButton("Descarregar");
        this.textArea = new JTextArea();

        frame.add(buttonProcura);
        buttonProcura.setBounds(480, 5, 150, 20);
        frame.add(buttonPedidoClt);
        buttonPedidoClt.setBounds(480, 45, 150, 20);
        frame.add(buttonDownload);
        buttonDownload.setBounds(480, 85, 150, 20);

        frame.add(textArea);
        textArea.setBounds(480, 165, 150, 235);
        textArea.setText(Ficheiros.getInstance().getF());

    }

    private void configureProgressBar(){
        jProgressBar = new JProgressBar(0, 100);
        jProgressBar.setValue(0);
        jProgressBar.setStringPainted(true);

        frame.add(jProgressBar);
        jProgressBar.setBounds(480, 125, 150, 20);
    }

    public void startWorkers() {
        ProgressBarManager progressBarManager = new ProgressBarManager();
        progressBarManager.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                if ("progress".equals(propertyChangeEvent.getPropertyName())) {
                    jProgressBar.setIndeterminate(false);
                    jProgressBar.setValue((Integer) propertyChangeEvent.getNewValue());
                }
            }
        });
        progressBarManager.execute();
    }

    private void addListeners(){
        buttonProcura.addActionListener(e -> {
            modelo.removeAllElements();
            String nome = textField.getText();
            Cliente.getInstance().findFiles(nome);
            for(FileDetails fd: P2PClient.getInstance().getmapaDeFicheiros()){
                modelo.addElement(fd);
            }
        });

        listaFicheiros.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent ke) {
                if (ke.getKeyCode() == KeyEvent.VK_ENTER) {
                    System.out.println(listaFicheiros.getSelectedValue());
                    P2PClient.getInstance().transferFile(listaFicheiros.getSelectedValue());
                }
            }
        });

        listaFicheiros.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent me) {
                if (me.getClickCount() == 2) {
                    System.out.println(listaFicheiros.getSelectedValue());
                    P2PClient.getInstance().transferFile(listaFicheiros.getSelectedValue());
                }
            }
        });

        buttonPedidoClt.addActionListener(e -> { ;
            ClienteServidor.getInstance().sendCLT();
        });

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ClienteServidor.getInstance().removeRegister();
                e.getWindow().dispose();
            }
        });
    }

    class ProgressBarManager extends SwingWorker<Float, Float> {

        @Override
        protected Float doInBackground() throws Exception {
            Float value = null;
            while(!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                value = Progress.getInstance().getFractionDone();
                publish(value);
            }
            return value;
        }

        @Override
        protected void process(List<Float> chunks) {
            for (Float d : chunks) {
                System.out.println(d);
                jProgressBar.setValue((int)(100*d));
            }
        }

        @Override
        public void done() {
            System.out.println("Done");
        }
    }

}