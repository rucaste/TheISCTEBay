package estruturasDeCoordenacao;

import java.util.LinkedList;


public class ThreadPool {

    private Worker[] workers;
    private LinkedList<Runnable> queue;

    public ThreadPool(int numberOfThreads){
        queue = new LinkedList<Runnable>();
        workers = new Worker[numberOfThreads];

        for(int i = 0; i < workers.length; i++){
            workers[i] = new Worker();
            workers[i].start();
        }
    }

    public void enqueue(Runnable runnable) {
        synchronized (queue) {
            queue.addLast(runnable);
            queue.notify();
        }
    }

    public class Worker extends Thread {
        @Override
        public void run() {
            Runnable runnable;
            while(true){
                synchronized (queue) {
                    while(queue.isEmpty()){
                        try {
                            queue.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    runnable = (Runnable) queue.removeFirst();
                }
                runnable.run();
            }
        }
    }
}