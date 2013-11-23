package nachos.threads;

import java.util.LinkedList;

import nachos.machine.*;

/**
 * An implementation of condition variables that disables interrupt()s for
 * synchronization.
 *
 * <p>
 * You must implement this.
 *
 * @see	nachos.threads.Condition
 */
public class Condition2 {
    /**
     * Allocate a new condition variable.
     *
     * @param	conditionLock	the lock associated with this condition
     *				variable. The current thread must hold this
     *				lock whenever it uses <tt>sleep()</tt>,
     *				<tt>wake()</tt>, or <tt>wakeAll()</tt>.
     */
    public Condition2(Lock conditionLock) {
	this.conditionLock = conditionLock;
    }

    /**
     * Atomically release the associated lock and go to sleep on this condition
     * variable until another thread wakes it using <tt>wake()</tt>. The
     * current thread must hold the associated lock. The thread will
     * automatically reacquire the lock before <tt>sleep()</tt> returns.
     */
    public void sleep() {
	Lib.assertTrue(conditionLock.isHeldByCurrentThread());

	conditionLock.release();
	// Add the current thread to the waitQueue for this condition
    // and then put it to sleep.
    boolean intStatus = Machine.interrupt().disable();
    waitQueue.add(KThread.currentThread());
    KThread.sleep();
    Machine.interrupt().restore(intStatus);
	conditionLock.acquire();
    }

    /**
     * Wake up at most one thread sleeping on this condition variable. The
     * current thread must hold the associated lock.
     */
    public void wake() {
	Lib.assertTrue(conditionLock.isHeldByCurrentThread());
	// If we have threads waiting on this condition, get the first one
    // off the waitQueue and add it to the readyQueue.
    boolean intStatus = Machine.interrupt().disable();         
    if(!waitQueue.isEmpty() && waitQueue.peek() != null) {
            waitQueue.poll().ready();
    }
    Machine.interrupt().restore(intStatus);
    }

    /**
     * Wake up all threads sleeping on this condition variable. The current
     * thread must hold the associated lock.
     */
    public void wakeAll() {
	Lib.assertTrue(conditionLock.isHeldByCurrentThread());
	while(!waitQueue.isEmpty())
		wake();
    }
    public static void selfTest(){
    	Lock l = new Lock();
    	Condition2 c = new Condition2(l);
    	KThread t1=new KThread(new takethread(c,l)).setName("take");
    	KThread t2=new KThread(new givethread(c,l)).setName("give");
    	t1.fork();
    	t2.fork();
    }

    private static class takethread implements Runnable{
    	Condition2 cond;
    	Lock lock;
    	takethread(Condition2 c,Lock l){
    		cond = c;
    		lock = l;
    	}
		public void run() {
			for(int i=0;i<5;i++){
				lock.acquire();
				if(shared <= 0){
					cond.sleep();
				}
				System.out.println("take "+shared);
				shared--;
				lock.release();
			}
		}
    }

    private static class givethread implements Runnable{
    	Lock lock;
    	Condition2 cond;
    	givethread( Condition2 c,Lock l){
    		cond =c;
    		lock = l;
    	}
		public void run() {
			for(int i=0;i<5;i++){
				lock.acquire();
				shared++;
				System.out.println("give "+shared);
				if(shared >= 1){
					cond.wake();
				}
				lock.release();
				KThread.yield();
			}
		}
    }
    
    private static int shared=0;
    private Lock conditionLock;
    private LinkedList<KThread> waitQueue = new LinkedList<KThread>();
}
