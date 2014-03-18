package nachos.threads;

import nachos.machine.*;

import java.util.*;

/**
 * A scheduler that chooses threads based on their priorities.
 *
 * <p>
 * A priority scheduler associates a priority with each thread. The next thread
 * to be dequeued is always a thread with priority no less than any other
 * waiting thread's priority. Like a round-robin scheduler, the thread that is
 * dequeued is, among all the threads of the same (highest) priority, the
 * thread that has been waiting longest.
 *
 * <p>
 * Essentially, a priority scheduler gives access in a round-robin fassion to
 * all the highest-priority threads, and ignores all other threads. This has
 * the potential to
 * starve a thread if there's always a thread waiting with higher priority.
 *
 * <p>
 * A priority scheduler must partially solve the priority inversion problem; in
 * particular, priority must be donated through locks, and through joins.
 */
public class PriorityScheduler extends Scheduler {
    /**
     * Allocate a new priority scheduler.
     */
    public PriorityScheduler() {
    }
    
    /**
     * Allocate a new priority thread queue.
     *
     * @param	transferPriority	<tt>true</tt> if this queue should
     *					transfer priority from waiting threads
     *					to the owning thread.
     * @return	a new priority thread queue.
     */
    public ThreadQueue newThreadQueue(boolean transferPriority) {
	return new PriorityQueue(transferPriority);
    }

    public int getPriority(KThread thread) {
	Lib.assertTrue(Machine.interrupt().disabled());
		       
	return getThreadState(thread).getPriority();
    }

    public int getEffectivePriority(KThread thread) {
	Lib.assertTrue(Machine.interrupt().disabled());
		       
	return getThreadState(thread).getEffectivePriority();
    }

    public void setPriority(KThread thread, int priority) {
	Lib.assertTrue(Machine.interrupt().disabled());
		       
	Lib.assertTrue(priority >= priorityMinimum &&
		   priority <= priorityMaximum);
	
	getThreadState(thread).setPriority(priority);
    }

    public boolean increasePriority() {
	boolean intStatus = Machine.interrupt().disable();
		       
	KThread thread = KThread.currentThread();

	int priority = getPriority(thread);
	if (priority == priorityMaximum)
	    return false;

	setPriority(thread, priority+1);

	Machine.interrupt().restore(intStatus);
	return true;
    }

    public boolean decreasePriority() {
	boolean intStatus = Machine.interrupt().disable();
		       
	KThread thread = KThread.currentThread();

	int priority = getPriority(thread);
	if (priority == priorityMinimum)
	    return false;

	setPriority(thread, priority-1);

	Machine.interrupt().restore(intStatus);
	return true;
    }

    /**
     * The default priority for a new thread. Do not change this value.
     */
    public static final int priorityDefault = 1;
    /**
     * The minimum priority that a thread can have. Do not change this value.
     */
    public static final int priorityMinimum = 0;
    /**
     * The maximum priority that a thread can have. Do not change this value.
     */
    public static final int priorityMaximum = 7;    

    /**
     * Return the scheduling state of the specified thread.
     *
     * @param	thread	the thread whose scheduling state to return.
     * @return	the scheduling state of the specified thread.
     */
    protected ThreadState getThreadState(KThread thread) {
	if (thread.schedulingState == null)
	    thread.schedulingState = new ThreadState(thread);

	return (ThreadState) thread.schedulingState;
    }

    /**
     * A <tt>ThreadQueue</tt> that sorts threads by priority.
     */
    protected class PriorityQueue extends ThreadQueue {
	
	    PriorityQueue(boolean transferPriority) {
	    	this.transferPriority = transferPriority;
	    	highestPriority = priorityMinimum;
		}
	
		public void waitForAccess(KThread thread) {
		    Lib.assertTrue(Machine.interrupt().disabled());
		    getThreadState(thread).waitForAccess(this);
		}
	
		public void acquire(KThread thread) {
		    Lib.assertTrue(Machine.interrupt().disabled());
		    getThreadState(thread).acquire(this);
		    running = thread;
		}
	
		public KThread nextThread() {
		    Lib.assertTrue(Machine.interrupt().disabled());
		    
		    if(!orderedThreads.isEmpty()){
		    	KThread thread = orderedThreads.poll().thread;
		    	getThreadState(thread).acquire(this);
		    	running = thread;
		    	return thread;
		    }
		    return null;
		}
	
		/**
		 * Return the next thread that <tt>nextThread()</tt> would return,
		 * without modifying the state of this queue.
		 *
		 * @return	the next thread that <tt>nextThread()</tt> would
		 *		return.
		 */
		protected ThreadState pickNextThread() {
		    // implement me
		    return orderedThreads.peek();
		}
		
		public void print() {
		    Lib.assertTrue(Machine.interrupt().disabled());
		    // implement me (if you want)
		    Iterator it = orderedThreads.iterator();
		    while(it.hasNext()){
		    	ThreadState t= ((ThreadState)it.next());
		    	System.out.println(t.thread.getName()+"-"+t.getPriority()+"-"+t.getWaitingTime()+"-"+t.getEffectivePriority());
		    }
		}
	
		public void addState(ThreadState thread) {
            this.orderedThreads.add(thread);
            updateHighestPriorityOnAdd(thread);
	    }
	    
	    public void removeState(ThreadState thread) {
	            this.orderedThreads.remove(thread);
	            updateHighestPriorityOnRemove(thread);
	    }
	    
	    private void updateHighestPriorityOnAdd(ThreadState thread){
	    	if(transferPriority && thread.getPriority() > highestPriority)
	    		highestPriority = thread.getPriority();
	    }
	    
	    private void updateHighestPriorityOnRemove(ThreadState thread){
	    	if(transferPriority){
		    	Iterator it = orderedThreads.iterator();
			    while(it.hasNext()){
			    	int p = ((ThreadState)it.next()).getPriority();
			    	if(p > highestPriority)
			    		highestPriority = p;
			    }
	    	}
	    }
	    
	    public int getHighestPriority(){
	    	return highestPriority;
	    }
	    
	    public KThread getRunning(){
	    	return running;
	    }
	    
		/**
		 * <tt>true</tt> if this queue should transfer priority from waiting
		 * threads to the owning thread.
		 */
		public boolean transferPriority;
		private int highestPriority;
		private KThread running = null;
		/**
		 * Stores the threadStates in the order of priorities
		 */
        private java.util.PriorityQueue<ThreadState> orderedThreads = new java.util.PriorityQueue<ThreadState>(10, new ThreadComparator());
        
        protected class ThreadComparator implements Comparator<ThreadState> {
            @Override
            public int compare(ThreadState t1, ThreadState t2) {
                    if (t1.getEffectivePriority() > t2.getEffectivePriority()) {
                            return -1;
                    } else if (t1.getEffectivePriority() < t2.getEffectivePriority()) {
                            return 1;
                    } else {
                            if (t1.getWaitingTime() < t2.getWaitingTime()) {
                                    return -1;
                            } else if(t1.getWaitingTime() > t2.getWaitingTime()) {
                                    return 1;
                            } else {
                                    return 0;
                            }
                    }
            }
    }
    }

    /**
     * The scheduling state of a thread. This should include the thread's
     * priority, its effective priority, any objects it owns, and the queue
     * it's waiting for, if any.
     *
     * @see	nachos.threads.KThread#schedulingState
     */
    protected class ThreadState {
		/**
		 * Allocate a new <tt>ThreadState</tt> object and associate it with the
		 * specified thread.
		 *
		 * @param	thread	the thread this state belongs to.
		 */
		public ThreadState(KThread thread) {
		    this.thread = thread;
		    setPriority(priorityDefault);
		    //waitingTime = System.currentTimeMillis();
		    waitingTime=i;
		    i++;
		}
	
		public long getWaitingTime() {
			return waitingTime;
		}
		
		/**
		 * Return the priority of the associated thread.
		 *
		 * @return	the priority of the associated thread.
		 */
		public int getPriority() {
		    return priority;
		}
	
		/**
		 * Return the effective priority of the associated thread.
		 *
		 * @return	the effective priority of the associated thread.
		 */
		public int getEffectivePriority() {
		    // implement me
			//int effectivePriority = priority;
			//if(waitingOn != null)
				//effectivePriority = waitingOn.getHighestPriority();
			//if(priority > effectivePriority)
				return effectivePriority;
			//else
				//return effectivePriority;
		}
		
		public void updateEffectivePriority(int p){
			 
			if(waitingOn != null && waitingOn.transferPriority && p > effectivePriority){
			    waitingOn.removeState(this);
				effectivePriority = p;
			    waitingOn.addState(this);
			}
			
		}
		
		public void donatePriority(){
			if(waitingOn != null && getThreadState(waitingOn.getRunning()).getPriority() < this.getPriority()){
				if(getThreadState(waitingOn.getRunning()).waitingOn != null)
					getThreadState(waitingOn.getRunning()).updateEffectivePriority(this.getEffectivePriority());
			}
		}
	
		/**
		 * Set the priority of the associated thread to the specified value.
		 *
		 * @param	priority	the new priority.
		 */
		public void setPriority(int priority) {
		    if (this.priority == priority)
			return;
		    // implement me
		    if(waitingOn != null) waitingOn.removeState(this);
		    this.priority = priority;
		    this.effectivePriority = priority;
		    if(waitingOn != null) waitingOn.addState(this);
		}
	
		/**
		 * Called when <tt>waitForAccess(thread)</tt> (where <tt>thread</tt> is
		 * the associated thread) is invoked on the specified priority queue.
		 * The associated thread is therefore waiting for access to the
		 * resource guarded by <tt>waitQueue</tt>. This method is only called
		 * if the associated thread cannot immediately obtain access.
		 *
		 * @param	waitQueue	the queue that the associated thread is
		 *				now waiting on.
		 *
		 * @see	nachos.threads.ThreadQueue#waitForAccess
		 */
		public void waitForAccess(PriorityQueue waitQueue) {
		    // implement me
			waitQueue.addState(this);
			waitingOn = waitQueue;
			if (myResources.indexOf(waitQueue) != -1) {
	            myResources.remove(waitQueue);
	        }
			if(waitingOn.transferPriority) donatePriority();
		}
	
		/**
		 * Called when the associated thread has acquired access to whatever is
		 * guarded by <tt>waitQueue</tt>. This can occur either as a result of
		 * <tt>acquire(thread)</tt> being invoked on <tt>waitQueue</tt> (where
		 * <tt>thread</tt> is the associated thread), or as a result of
		 * <tt>nextThread()</tt> being invoked on <tt>waitQueue</tt>.
		 *
		 * @see	nachos.threads.ThreadQueue#acquire
		 * @see	nachos.threads.ThreadQueue#nextThread
		 */
		public void acquire(PriorityQueue waitQueue) {
		    // implement me
		    Lib.assertTrue(Machine.interrupt().disabled());
		    if (myResources.indexOf(waitQueue) == -1) {
	            myResources.add(waitQueue);
	        }
		    waitQueue.removeState(this);
		    if(waitingOn == waitQueue){
		    	waitingOn = null;
		    }
		}	
	
		/** The thread with which this object is associated. */	   
		protected KThread thread;
		/** The priority of the associated thread. */
		protected int priority;
		protected int effectivePriority;
		
		protected PriorityQueue waitingOn = new PriorityQueue(true);
		protected long waitingTime;
	    protected LinkedList<PriorityQueue> myResources = new LinkedList<PriorityQueue>(); 
    }
	int i=0;
    
}
