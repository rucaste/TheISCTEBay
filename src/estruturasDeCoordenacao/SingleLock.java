package estruturasDeCoordenacao;

public class SingleLock {

    private int count;

    public SingleLock(){
        this.count = 1;
    }

    public synchronized void acquire() throws InterruptedException {
        while(count == 0){
            wait();
        }
        count = 0;
    }

    public synchronized void release(){
        count = 1;
        notifyAll();
    }

}



