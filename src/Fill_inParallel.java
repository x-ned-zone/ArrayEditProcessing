// package array_editor;
import java.io.*;
import java.lang.Thread;
import java.lang.InterruptedException;
import java.util.Scanner; 
import java.io.IOException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Arrays;

public class Fill_inParallel<T> implements Runnable {
	private T [] array;
	private T oldValue;
	private T newValue;
	private int startIndex, endIndex;

	Fill_inParallel (T [] array, T newValue, int start, int end) {
		this.array=array;
		this.oldValue=oldValue;
		this.newValue=newValue;
		this.startIndex = start;
		this.endIndex = end;
	}

	@Override
	public void run() {
		try {
			// Bruteforce: O(n) Average/worst-case.   Auxiliary Space: O(1)

			T prevAdj = (T)-1 ;  // Use previous adjacent auxiliary holder

			for (int i = this.startIndex+1; i<this.endIndex; i++) {
				if (prevAdj>=0 && array[i]==prevAdj)
					array[i] = newValue;

				else {
					if ( array[i]==array[i-1] ) {
						prevAdj = array[i];
						array[i-1] = newValue;
					}
					else
						prevAdj = -1 ;
				}
			}
        	System.out.println(Thread.currentThread().getName());
	    }
		catch (Exception ex) { ex.printStackTrace(); System.out.println("Exp. Error"); System.exit(0);}
	}
}