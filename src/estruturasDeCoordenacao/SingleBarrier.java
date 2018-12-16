package estruturasDeCoordenacao;

public class SingleBarrier {

    private int currentPosters = 0;
    private int totalPosters;

    private int passedWaiters = 0;
    private int totalWaiters;

    public SingleBarrier(int i, int j) {
        totalPosters = i;
        totalWaiters = j;
    }

    public synchronized void barrierWait() throws InterruptedException {
        while(currentPosters < totalPosters){
            wait();
        }
        passedWaiters++;
        if(passedWaiters == totalWaiters) {
            currentPosters = 0;
            passedWaiters = 0;
            notifyAll();
        }
    }

    public synchronized void barrierPost() throws InterruptedException {
        while (currentPosters == totalPosters){
            wait();
        }
        currentPosters++;
        if(currentPosters == totalPosters)
            notifyAll();
    }
}
