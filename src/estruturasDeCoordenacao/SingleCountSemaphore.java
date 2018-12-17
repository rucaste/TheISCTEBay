package estruturasDeCoordenacao;

public class SingleCountSemaphore {

    private int count;

    public SingleCountSemaphore(){
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



