package nachos.threads;

import java.util.TreeMap;

import nachos.machine.*;

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
    /**
     * Allocate a new Alarm. Set the machine's timer interrupt handler to this
     * alarm's callback.
     *
     * <p><b>Note</b>: Nachos will not function correctly with more than one
     * alarm.
     */
    public Alarm() {
	Machine.timer().setInterruptHandler(new Runnable() {
		public void run() { timerInterrupt(); }
	    });
    }

    /**
     * The timer interrupt handler. This is called by the machine's timer
     * periodically (approximately every 500 clock ticks). Causes the current
     * thread to yield, forcing a context switch if there is another thread
     * that should be run.
     */
    public void timerInterrupt() {
	//KThread.currentThread().yield();
    	boolean intStatus = Machine.interrupt().disable();
    	if(!list.isEmpty()){
    		if(list.firstKey() <= Machine.timer().getTime()){
    			KThread t = list.pollFirstEntry().getValue();
    			t.ready();
    			System.out.println("Thread "+t.getName()+" is ready at "+Machine.timer().getTime());
    		}
    	} 
    }

    /**
     * Put the current thread to sleep for at least <i>x</i> ticks,
     * waking it up in the timer interrupt handler. The thread must be
     * woken up (placed in the scheduler ready set) during the first timer
     * interrupt where
     *
     * <p><blockquote>
     * (current time) >= (WaitUntil called time)+(x)
     * </blockquote>
     *
     * @param	x	the minimum number of clock ticks to wait.
     *
     * @see	nachos.machine.Timer#getTime()
     */
    public void waitUntil(long x) {
	// for now, cheat just to get something working (busy waiting is bad)
	long wakeTime = Machine.timer().getTime() + x;
	boolean intStatus = Machine.interrupt().disable();
	wakeTime = Machine.timer().getTime() + x;
	System.out.println("Thread "+KThread.currentThread().getName()+" is going to sleep until "+wakeTime+" ticks");
	list.put(wakeTime,KThread.currentThread());
	KThread.sleep();
	Machine.interrupt().restore(intStatus);
	//while (wakeTime > Machine.timer().getTime())
	  //  KThread.yield();
    }

    public static void selfTest(){
    	KThread t1 = new KThread(new Runnable(){
			public void run() {
				long ticks = 40;
				System.out.println(KThread.currentThread().getName() + " before sleep "+Machine.timer().getTime());
				ThreadedKernel.alarm.waitUntil(ticks);
				System.out.println(KThread.currentThread().getName() + " after sleep "+Machine.timer().getTime());
			}
    	}).setName("t1"); 
    	KThread t2 = new KThread(new Runnable(){
			public void run() {
				long ticks = 800;
				System.out.println(KThread.currentThread().getName() + " before sleep "+Machine.timer().getTime());
				ThreadedKernel.alarm.waitUntil(ticks);
				System.out.println(KThread.currentThread().getName() + " after sleep "+Machine.timer().getTime());
			}
    	}).setName("t2"); 
    	t1.fork();
    	t2.fork();
    }
    
    private long wakeTime;
    private TreeMap<Long,KThread> list = new TreeMap<Long,KThread>();
}
