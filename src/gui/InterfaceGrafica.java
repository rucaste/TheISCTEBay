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
import java.io.IOException;
import java.util.List;

import static java.lang.Thread.sleep;

public class InterfaceGrafica extends JFrame {

    private static InterfaceGrafica instance;

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
        instance = this;
		configureFrame();
		configureText();
        configureList();
        configureButtons();
        addListeners();
        configureProgressBar();
	}

	public static InterfaceGrafica getInstance(){
        return instance;
    }

    private void configureFrame(){
        this.frame.setSize(650, 460);
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
                    System.out.println("cima " + (Integer) propertyChangeEvent.getNewValue());
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
                startWorkers();
                P2PClient.getInstance().transferFile(listaFicheiros.getSelectedValue());
            }
        });

        listaFicheiros.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent ke) {
                if (ke.getKeyCode() == KeyEvent.VK_ENTER) {
                    if(!listaFicheiros.isSelectionEmpty()){
                        startWorkers();
                        P2PClient.getInstance().transferFile(listaFicheiros.getSelectedValue());
                    }
                }
            }
        });

        listaFicheiros.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent me) {
                if (me.getClickCount() == 2) {
                    if(!listaFicheiros.isSelectionEmpty()){
                        startWorkers();
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

    class ProgressBarManager extends SwingWorker<Integer, Integer> {

        @Override
        protected Integer doInBackground() throws Exception {
            int value = 0;
            while(!isCancelled() && value < 100) {
                System.out.println("t: " + value);
                Thread.sleep(100);
                value = (int) (100 * Progress.getInstance().getFractionDone());
                setProgress(value);
            }
            return value;
        }
    }

}
