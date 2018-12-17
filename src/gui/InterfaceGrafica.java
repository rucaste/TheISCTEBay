package gui;

import clienteDiretorio.ClienteServidor;
import estruturas.FileDetails;
import estruturasDeCoordenacao.SingleCountSemaphore;
import mainClient.Cliente;
import mainClient.Ficheiros;
import mainClient.Progress;
import p2pClient.P2PClient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;


public class InterfaceGrafica extends JFrame {

    private static InterfaceGrafica instance;

	private JFrame frame = new JFrame("The ISCTE Bay");
	private JTextField textField = new JTextField();
    private JButton buttonProcura = new JButton("Procura");
	private JButton buttonDwonload = new JButton("Download");
    private JLabel labelPalavraChave = new JLabel("Palavra Chave:");

    private DefaultListModel<FileDetails> modeloAux = new DefaultListModel<>();
    private JList<FileDetails> jListAux;
    private JScrollPane jScrollPaneAux = new JScrollPane();

    private DefaultListModel<FileDetails> modelo = new DefaultListModel<>();
	private JList<FileDetails> listaFicheiros;
    private JScrollPane jScrollPane = new JScrollPane();

	private JProgressBar jProgressBar;

	private SingleCountSemaphore singleCountSemaphore = new SingleCountSemaphore();

    public InterfaceGrafica() {
        instance = this;
		configureFrame();
		configureText();
        configureList();
        configureButtons();
        addListeners();
        configureProgressBar();
        startWorkers();
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

        this.jListAux = new JList<FileDetails>(modeloAux);
        this.jScrollPaneAux.setViewportView(jListAux);
        this.jListAux.setLayoutOrientation(JList.VERTICAL);
        frame.add(jScrollPaneAux, BorderLayout.NORTH);

        this.jScrollPaneAux.setBounds(480, 180, 150, 230);
        updateJList2();
    }

    private void updateJList2(){
        for(FileDetails fd: Ficheiros.getInstance().getF()){
            modeloAux.addElement(fd);
        }
    }

    private void configureButtons(){
        frame.add(buttonProcura);
        buttonProcura.setBounds(480, 5, 150, 40);
        frame.add(buttonDwonload);
        buttonDwonload.setBounds(480, 65, 150, 40);
    }

    private void configureProgressBar(){
        jProgressBar = new JProgressBar(0, 100);
        jProgressBar.setValue(0);
        jProgressBar.setStringPainted(true);

        frame.add(jProgressBar);
        jProgressBar.setBounds(480, 125, 150, 40);
    }

    public void startWorkers() {
        ProgressBarManager progressBarManager = new ProgressBarManager(jProgressBar);
        /*progressBarManager.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                if ("progress".equals(propertyChangeEvent.getPropertyName())) {
                    System.out.println("cima " + (Integer) propertyChangeEvent.getNewValue());
                    jProgressBar.setValue((Integer) propertyChangeEvent.getNewValue());
                }
            }
        });*/
        progressBarManager.execute();
    }

    private void downloadAndUpdate(){
        try {
            singleCountSemaphore.acquire();
        } catch (InterruptedException e) {
            singleCountSemaphore.release();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                FileDetails fileDetails = listaFicheiros.getSelectedValue();
                P2PClient.getInstance().transferFile(fileDetails);
                modeloAux.addElement(fileDetails);
                singleCountSemaphore.release();
            }
        }).start();

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
                downloadAndUpdate();
            }
        });

        listaFicheiros.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent ke) {
                if (ke.getKeyCode() == KeyEvent.VK_ENTER) {
                    if(!listaFicheiros.isSelectionEmpty()){
                        downloadAndUpdate();
                    }
                }
            }
        });

        listaFicheiros.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent me) {
                if (me.getClickCount() == 2) {
                    if(!listaFicheiros.isSelectionEmpty()){
                        downloadAndUpdate();
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

        JProgressBar jProgressBar;

        ProgressBarManager(JProgressBar jProgressBar){
            this.jProgressBar = jProgressBar;
        }

        @Override
        protected Integer doInBackground() throws Exception {
            int value = 0;
            while(true) {
                Thread.sleep(100);
                value = (int) (100 * Progress.getInstance().getFractionDone());
                publish(value);
                //setProgress(value);
            }
        }

        @Override
        protected void process(List<Integer> chunks) {
            int i = chunks.get(chunks.size()-1);
            jProgressBar.setValue(i);
        }
    }

}
