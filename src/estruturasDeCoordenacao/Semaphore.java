package estruturasDeCoordenacao;

public class Semaphore {

    private int count;

    public Semaphore(int i){
        this.count = i;
    }

    public synchronized void acquire() throws InterruptedException {
        while(count == 0){
            wait();
        }
        count--;
    }

    public synchronized void release(){
        count++;
        notifyAll();
    }

}



