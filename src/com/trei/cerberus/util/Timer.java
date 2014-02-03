/**
 * 
 */
package com.trei.cerberus.util;

/**
 * @date 06 Dec 2013
 * @author Kovalskiy Andrey
 */
public class Timer extends Thread {
	private boolean restart = false;
	private boolean state = false;
	long time = Long.parseLong(PropertiesManager.acqureProperty("timerValue", "10000"));

	/**
	 * Allocates a new {@code Timer} object.
	 */
	public Timer() {
		super();
	}

	/**
	 * Allocates a new {@code Timer} object. Initializes time to sleep.
	 */
	public Timer(long time) {
		super();
		this.time = time;
	}

	/**
	 * @return the state
	 */
	public synchronized  boolean isActive() {
		return state;
	}
	
	public synchronized void  setRestart(boolean restart){
		this.restart = restart;
	}

	public void run() {
		while(true){
			if (restart){
				try {
					state = true;
					restart = false;
					Thread.sleep(time);
					state = false;
				} catch (InterruptedException e) {
					System.out.println(e);
				}
			}
		}
	}

}
