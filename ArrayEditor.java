// package array_editor;

// Program: Array Editor
// Problem:
// 	You're creating a program that is going to perform operations on a given array,'
// 	'Your program is going to be used as part of a larger project, but should still be able to be run on its own.'
// The operations that your program needs to be able to support are: Replace, Crop, Fill, Smooth, Blur, Edge

// Things to keep in mind:
// •	Think about how your program is going to accept input and provide output. 
// •	Remember that it will be used in a larger project, but must be able to be run by itself
// •	Remember to keep OO design principles in mind
// •	What about edge cases? What happens if the input changes (for example, if the arrays were 2-dimensional)?
// •	How would you test your program?

import java.io.*;
import java.lang.Thread;
import java.lang.InterruptedException;
import java.util.Scanner; 
import java.io.IOException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Arrays;

public class ArrayEditor<T>
{
	public ArrayEditor () { }


	/* 1. Replace: Replace all instances of one value with another.
		  Assumptions:
		  	- Array contains integers or floats.
		  	- The order of array elements matters.
		// Further enhance performane to parallelize for loops Use: 
		//	- HPC OpenMP(JaMP)  - for shared memory computers
		// 	- MPI - for clusters and distributed memory computers (or super-computers)

		// Dynamic programming?? Not possible, the runtime complexity (and space) would be worst than bruteforce:
		//	Create array of maps <Element, Original-index> -> O(1)
		//	Sort array according to 'Element' with Mergesort into new array -> O(n*log(n)).
		//	Find elements & use index to replace element on original array -> Binary search O(log(n)) worst-case.
		//  Base Runtime - Best case : O(n*log(n)) + O (n x m occurences) -> Worst case: O(n*log(n)) + O (n).
		//  Auxiliary Space: O(n)
	*/
	public void replace (T [] array, T oldValue, T newValue) throws CloneNotSupportedException 
	{
		// Use multi-threading. Runtime -> O (N / number of processors). Proportional to # of processors.
		// Synchronization? No, there's no concurrent access to same memory location.
		
		int size = array.length; 
		if (size>=40) { // Parallelize if N >= 40
	        int n_threads = 4;
        	ExecutorService executor = Executors.newFixedThreadPool(n_threads);

			// E.g Divided among 4 threads = (0, size/4), (size/4, size/2), (size/2, size/2+size/4), (size/2+size/4, size)... ;
	        for (int i = 0; i < size; i+=size/n_threads) {
				Runnable worker = new Replace_inParallel(array, oldValue, newValue, i, i+size/n_threads);
	            executor.execute(worker);
	        }
	        // shutdown executor service
	        try {
	        	executor.shutdown();
	        	while (!executor.isTerminated()) {}
	        	// System.out.println("All threads Finished!");
	        }
			catch (Exception e) { System.err.println("tasks interrupted"); }
			finally {
			    if (!executor.isTerminated())
			        System.err.println("cancel incomplete tasks");
			    executor.shutdownNow();
			    // System.out.println("shutdown finished");
			}
		}

		else {
			// Bruteforce: O(n) worst-case.   Auxiliary Space:
			for (int i=0; i<size; i++) {
				if (array[i]==oldValue)
					array[i] = newValue;
			}
		}

	}

	/* 2. Crop: Change the size of the array.
		  (for example, if the input array is 100 elements, crop it so that the output is only elements 10 to 20:
		  				the size of the output array is 10) */
	public void crop (T [] curr_array, int newSize, int startPos) {
		T [] new_Array = (T []) new Object[newSize]; 
        assert( new_Array.getClass().getComponentType() == curr_array.getClass().getComponentType() ) ;

		// Use multi-threading. Runtime -> O (10 / number of processors). Proportional to # of processors.
		if (curr_array.length >=20) {
			// copy elements from position 10 to 20
			for (int i=10; i<20; i++)
				new_Array[i] = curr_array[i];
		}
		else {
			// Bruteforce: O(newSize) worst-case.   Auxiliary Space: O(newSize)
			for (int i=0; i<newSize; i++)
				new_Array[i] = curr_array[i];
		}
		// copy back to array object.
		curr_array = new_Array;
	}

	/* 3. Fill: Similar to replace: Starting at a given index in an array, 
			replace adjacent elements if their value is the same (similar to the "paint bucket" fill tool in image editing software)*/
	 	// For example, given the array: [0, 2, 0, 1, 1, 1, 2, 2, 1], 
	 	// and filling position 4 with the value "7", the result would be: [0, 2, 0, 7, 7, 7, 2, 2, 1] 
	public void fill (T [] array, T oldValue, T newValue, int s_position) {

	}

	/* 4. Smooth: replace values smaller than a minimum and larger with a maximum with the average of their neighbours. */
	   // For example, given:
	   // 	 [40, 5, 200, 15], 
	   // and smoothing with min 0 and max 100, the result would be:  (5+15)/2 = 10
	   //   [40, 5, 10, 15] 
	public void smooth (T [] array, T oldValue, T newValue) {

	}

	// For some extra fun (these are completely optional):
	// ---------------------------------------------------
	/* 1. Blur:  Replace every value with the average of itself, and its two neighbours*/

	/* 2. Edge detection:  Detect places where the values in the array change.*/
		// For example, given 
	 // 		[5, 5, 5, 2, 2, 2, 2, 3, 3] 
	 // 	and performing edge detection, the result could be
	 // 		[ 0, 0, 1, 1, 0, 0, 1, 1, 0]

	// Things to keep in mind:
	// •	Think about how your program is going to accept input and provide output. Remember that it will be used in a larger project, 
	// 	but must be able to be run by itself
	// •	Remember to keep OO design principles in mind
	// •	What about edge cases? What happens if the input changes (for example, if the arrays were 2-dimensional)?
	// •	How would you test your program?

	public static void main(String [] args) {
        // Integer [] large_array = {1,2,3,4,5,6,7,7,7,8,8,3,3,2,1,0,9,76,2,3,4,56,44,267,72,274,47,8,88,6,2,34,67,55,7,7};
        ArrayEditor <Integer> arrayEditor = new ArrayEditor <Integer>();
        Integer [] large_array = arrayEditor.GenerateTestArray();
        try {
        	long start_time = System.nanoTime();
        	arrayEditor.replace(large_array, 7, 777);
            System.out.println("Multi-threaded: ElapsedTime = " + (System.nanoTime()-start_time)/1e6 );
            // System.out.println("Array [ replaced_Pos ] = " + array[5]);   
        }
        catch (Exception e) { System.err.println("error"); }
	}

	public Integer [] GenerateTestArray (){
    	Integer n_Integers = 10000;
    	Integer[] test_arr = new int[n_Integers];
    	for (int i = 0; i < n_Integers; i++) {
      		test_arr[i] = (Integer)(Math.random()*n_Integers);
    	}
    	return test_arr;
	}
}


