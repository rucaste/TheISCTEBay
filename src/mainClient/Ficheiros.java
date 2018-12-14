package mainClient;

import estruturas.FileDetails;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Ficheiros {

    private static Ficheiros instance;

    private List<FileDetails> filesList;
    private String filesPath;

    public Ficheiros(String filesPath){
        instance = this;
        this.filesPath = filesPath;
        this.filesList = new ArrayList<>();
        updateFiles();
    }

    public static Ficheiros getInstance(){
        return instance;
    }

    public String getFilesPath() {
        return filesPath;
    }

    public String getF(){
        StringBuilder stringBuilder = new StringBuilder();
        for(FileDetails fileDetails: this.filesList){
            stringBuilder.append(fileDetails.toString()).append("\n");
        }
        return stringBuilder.toString();
    }

    public void addFile(FileDetails fileDetails){
        this.filesList.add(fileDetails);
    }

    public List<FileDetails> getFileNames(String pesquisa){
        List<FileDetails> listaDeFicheiros= new ArrayList<FileDetails>();
        for(FileDetails ficheiro: filesList){
            String nome = ficheiro.getNome();
            if (nome.toLowerCase().contains(pesquisa.toLowerCase()))
                listaDeFicheiros.add(ficheiro);
        }
        return listaDeFicheiros;
    }

    private synchronized void updateFiles(){
        File[] files = new File(this.filesPath).listFiles();
        if((files != null ? files.length : 0) > 0){
            for (File file : files) {
                if (file.isFile()) {
                    this.filesList.add(new FileDetails(file.getName(), file.length()));
                }
            }
            Collections.sort(this.filesList);
        }
        System.out.println(filesList);
        System.out.println(filesPath);
    }

    @Override
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder().append(filesPath).append('\n')
                .append("---------------------------------------------------------------------").append('\n');
        for(FileDetails fileDetails:this.filesList){
            stringBuilder.append(fileDetails.toString()).append('\n');
        }
        return stringBuilder.toString();
    }

}
