package gui;

import clienteDiretorio.ClienteServidor;
import estruturas.FileDetails;
import mainClient.Cliente;
import mainClient.Ficheiros;
import mainClient.Progress;
import p2pClient.P2PClient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

public class InterfaceGrafica extends JFrame {

	private JFrame frame = new JFrame("The ISCTE Bay");
	private JTextField textField = new JTextField();
    private JButton buttonProcura = new JButton("Procura");
	private JButton buttonDwonload = new JButton("Download");
    private JLabel labelPalavraChave = new JLabel("Palavra Chave:");
    private JTextArea textArea = new JTextArea();

    private DefaultListModel<FileDetails> modelo = new DefaultListModel<>();
	private JList<FileDetails> listaFicheiros;
    private JScrollPane jScrollPane = new JScrollPane();

	private JProgressBar jProgressBar = new JProgressBar(0, 100);

    public InterfaceGrafica() {
		configureFrame();
		configureText();
        configureList();
        configureButtons();
        addListeners();
        configureProgressBar();
        startWorkers();
	}

    private void configureFrame(){
        this.frame.setSize(635, 450);
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.getContentPane().setLayout(null);
        this.frame.setVisible(true);
    }

    private void configureText(){
        labelPalavraChave.setBounds(5, 5, 150, 25);
        frame.add(labelPalavraChave, BorderLayout.NORTH);

        frame.add(textField, BorderLayout.NORTH);
        textField.setBounds(150, 5, 325, 25);
    }

	private void configureList(){
        this.listaFicheiros = new JList<FileDetails>(modelo);
        this.jScrollPane.setViewportView(listaFicheiros);
        this.listaFicheiros.setLayoutOrientation(JList.VERTICAL);
        frame.add(jScrollPane, BorderLayout.NORTH);

        this.jScrollPane.setBounds(5, 35, 470, 380);
    }

    private void configureButtons(){
        this.textArea = new JTextArea();
        frame.add(buttonProcura);
        buttonProcura.setBounds(480, 5, 150, 40);
        frame.add(buttonDwonload);
        buttonDwonload.setBounds(480, 65, 150, 40);
        frame.add(textArea);
        textArea.setBounds(480, 180, 150, 230);
        textArea.setText(Ficheiros.getInstance().getF());
    }

    private void configureProgressBar(){
        jProgressBar = new JProgressBar(0, 100);
        jProgressBar.setValue(0);
        jProgressBar.setStringPainted(true);

        frame.add(jProgressBar);
        jProgressBar.setBounds(480, 125, 150, 40);
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
            Cliente.getInstance().findFiles(textField.getText());
            for(FileDetails fd: P2PClient.getInstance().getmapaDeFicheiros()){
                modelo.addElement(fd);
            }
        });

        buttonDwonload.addActionListener(e -> {
            if(!listaFicheiros.isSelectionEmpty()){
                P2PClient.getInstance().transferFile(listaFicheiros.getSelectedValue());
            }
        });

        listaFicheiros.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent ke) {
                if (ke.getKeyCode() == KeyEvent.VK_ENTER) {
                    if(!listaFicheiros.isSelectionEmpty()){
                        P2PClient.getInstance().transferFile(listaFicheiros.getSelectedValue());
                    }
                }
            }
        });

        listaFicheiros.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent me) {
                if (me.getClickCount() == 2) {
                    if(!listaFicheiros.isSelectionEmpty()){
                        P2PClient.getInstance().transferFile(listaFicheiros.getSelectedValue());
                    }
                }
            }
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
                //System.out.println(d);
                jProgressBar.setValue((int)(100*d));
            }
        }

        @Override
        public void done() {
            System.out.println("Done");
        }
    }

}
