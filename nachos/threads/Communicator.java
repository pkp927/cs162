package nachos.threads;

import nachos.machine.*;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>,
 * and multiple threads can be waiting to <i>listen</i>. But there should never
 * be a time when both a speaker and a listener are waiting, because the two
 * threads can be paired off at this point.
 */
public class Communicator {
    /**
     * Allocate a new communicator.
     */
    public Communicator() {
    	conditionLock = new Lock();
    	conds = new Condition(conditionLock);
    	condl = new Condition(conditionLock);
    }

    /**
     * Wait for a thread to listen through this communicator, and then transfer
     * <i>word</i> to the listener.
     *
     * <p>
     * Does not return until this thread is paired up with a listening thread.
     * Exactly one listener should receive <i>word</i>.
     *
     * @param	word	the integer to transfer.
     */
    public void speak(int word) {
    	conditionLock.acquire();
    	while(datas != -1) conds.sleep();
    	datas = word;
    	System.out.println(KThread.currentThread().getName()+" spoke "+datas);
    	condl.wake();
    	conditionLock.release();
    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return	the integer transferred.
     */    
    public int listen() {
    	conditionLock.acquire();
    	conds.wake();
    	while(datas == -1) condl.sleep();
    	datal = datas;
    	System.out.println(KThread.currentThread().getName()+" listened "+datal);
    	datas = -1;
    	conditionLock.release();
    	return datal;
    }
    
    public static void selfTest(){
    	final Communicator com = new Communicator();
    	
    	KThread t1 = new KThread(new Runnable(){
    		public void run(){
    			com.speak(1);
    		}
    	}).setName("t1");
    	KThread t2 = new KThread(new Runnable(){
    		public void run(){
    			com.listen();
    		}
    	}).setName("t2");
    	KThread t3 = new KThread(new Runnable(){
    		public void run(){
    			com.speak(3);
    		}
    	}).setName("t3");
    	KThread t4 = new KThread(new Runnable(){
    		public void run(){
    			com.listen();
    		}
    	}).setName("t4");
    	KThread t5 = new KThread(new Runnable(){
    		public void run(){
    			com.speak(5);
    		}
    	}).setName("t5");
    	/*
    	Machine.interrupt().disable();
    	ThreadedKernel.scheduler.setPriority(t5, 2);
    	ThreadedKernel.scheduler.setPriority(t4, 2);
    	ThreadedKernel.scheduler.setPriority(t3, 5);
    	ThreadedKernel.scheduler.setPriority(t2, 2);
    	Machine.interrupt().enable();
    	*/
    	t1.fork();
    	t2.fork();
    	t3.fork();
    	t4.fork();
    	t5.fork();
    }

    private Lock conditionLock;
    private Condition condl;
    private Condition conds;
    private int datas = -1;
    private int datal = -1;
}
