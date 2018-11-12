// package array_editor;
import java.io.*;
import java.lang.Thread;
import java.lang.InterruptedException;
import java.util.Scanner; 
import java.io.IOException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Arrays;

public class Replace_inParallel<T> implements Runnable {
	private T [] array;
	private T oldValue;
	private T newValue;
	private int startPos, endPos;

	Replace_inParallel (T [] array, T oldValue, T newValue, int start, int end) {
		this.array=array;
		this.oldValue=oldValue;
		this.newValue=newValue;
		this.startPos = start;
		this.endPos = end;
	}

	@Override
	public void run() {
		try {
			// Bruteforce: O(n) worst-case.   Auxiliary Space:
			for (int i = this.startPos; i<this.endPos; i++) {
				if (array[i]== oldValue)
					array[i] = newValue;
			}
        	System.out.println(Thread.currentThread().getName());
	    }
		catch (Exception ex) { ex.printStackTrace(); System.out.println("IO Error"); System.exit(0);}
	}
}