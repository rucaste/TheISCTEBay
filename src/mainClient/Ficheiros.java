package mainClient;

import estruturas.FileDetails;
import estruturasDeCoordenacao.SingleLock;

import java.io.File;
import java.util.*;


public class Ficheiros {

    private static Ficheiros instance;

    private List<FileDetails> filesList = new ArrayList<>();
    private String filesPath;

    private Map<FileDetails, SingleLock> lockMap = new HashMap<>();

    public Ficheiros(String filesPath){
        instance = this;
        this.filesPath = filesPath;
        updateFiles();
    }

    public static Ficheiros getInstance(){
        return instance;
    }

    public String getFilesPath() {
        return filesPath;
    }

    public List<FileDetails> getF(){
        return this.filesList;
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

    synchronized private void updateFiles(){
        File[] files = new File(this.filesPath).listFiles();
        if((files != null ? files.length : 0) > 0){
            for (File file : files) {
                if (file.isFile()) {
                    FileDetails fileDetails = new FileDetails(file.getName(), file.length());
                    this.filesList.add(fileDetails);
                    this.lockMap.put(fileDetails, new SingleLock());
                }
            }
            Collections.sort(this.filesList);
        }
    }

    public SingleLock getLock(FileDetails fileDetails){
        for (Map.Entry<FileDetails, SingleLock> entry : this.lockMap.entrySet()){
            FileDetails fileDetailsMap = entry.getKey();
            if(fileDetailsMap.getNome().equals(fileDetails.getNome())){
                return entry.getValue();
            }
        }
        return null;
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
