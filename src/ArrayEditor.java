// package array_editor;
import java.io.*;
import java.io.IOException;
import java.lang.Thread;
import java.lang.InterruptedException;
import java.util.Scanner; 

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Arrays;

/**
* <p>
*  Program: Array Editor
*  Problem: A program that performs operations on a given array,
*  			The operations supported by the program to are: Replace, Crop, Fill, Smooth, Blur, Edge
* </br>
*  Features:
*   • How program accepts input array and provides output.
*   • For larger project, methods accessed through OOP.
*   • Edge cases? Input changes (if the arrays were 2-dimensional).
*   • How is the program tested
* </br>
* To further enhance performane, Plan is to parallelize for loops Using: 
* - Java multi-threading - for shared memory computers
* - HPC OpenMP(JaMP) - for shared memory computers
* - MPI - for clusters and distributed memory computers (or super-computers)
* </p>
* 
* @param array The array with elements to replace
* @param oldValue
* @param newValue
* @return void
*/

public class ArrayEditor<T> {
	public ArrayEditor () { }
    /**
     * <p>
 	 * 1. Replace: Replace all instances of one value with another.
	 * </br>
	 * Assumptions:
	 * - Array contains integers or floats
	 * - The order of array elements matters
	 * </br>
	 * Dynamic programming Not possible. The runtime complexity (and space) would be worst than bruteforce as analysed below:
	 * - Create array of maps <Element, Original-index> -> O(1).
	 * - Sort array according to 'Element' with Mergesort into new array -> O(n*log(n)) Average/worst-case.
	 * - Find elements with Binary search & use index to replace element on original array -> O(log(n)) Average/worst-case.
	 * - Base-case complexity. Best case is O [ n*log(n)+(n * m-occurences) ] and Average/Worst case is O [ n*log(n) + (n) ].
	 * - Auxiliary Space: O(n)
	 * </br>
	 * Multi-threading used. Runtime -> O (N / number of processors). Proportional to # of processors.
	 * No Synchronization. There's no concurrent access to same memory location.
     * </p>
     * 
     * @param array The 1D array with elements to replace
     * @param oldValue The old value to be searched for occurances in the array
     * @param newValue The new value to replace occurances of the old value in the array
     * @return void.
    */ 
	public void replace (T [] array, T oldValue, T newValue) throws CloneNotSupportedException 
	{
		// Use multi-threading. Runtime -> O (N / number of processors). Proportional to # of processors.
		// E.g Divided among 4 threads = (0, size/4), (size/4, size/2), (size/2, size/2+size/4), (size/2+size/4, size)... ;
		// Synchronization? No, there's no concurrent access to same memory location.
		
		int size = array.length; 

		// Parallelize if N >= 40
		if (size>=40) { 
	        int n_threads = 4;
        	ExecutorService executor = Executors.newFixedThreadPool(n_threads);

	        for (int i = 0; i < size; i+=size/n_threads) {
				Runnable worker = new Replace_inParallel(array, oldValue, newValue, i, i+size/n_threads);
	            executor.execute(worker);
	        }
	        // shutdown executor service
	        try {
	        	executor.shutdown();
	        	while (!executor.isTerminated()) {} 
	        	System.out.println("All threads Finished!");
	        }
			catch (Exception e) { System.err.println("tasks interrupted"); }
			finally {
			    if (!executor.isTerminated()) { System.err.println("cancel incomplete tasks"); }
			    executor.shutdownNow();  
			}
		}
		// run serial
		else {
			// Bruteforce: O(n) worst-case.   Auxiliary Space:
			for (int i=0; i<size; i++) {
				if (array[i]==oldValue)
					array[i] = newValue;
			}
		}
	}
	/**     
	 * Replace method Overloaded.
     * @param array The 2D array with elements to replace
     * @param oldValue The old value to be searched for occurances in the array
     * @param newValue The new value to replace occurances of the old value in the array
     * @return void.
    */ 
	public void replace (T [][] array, T oldValue, T newValue) throws CloneNotSupportedException {
	}

    /**
     * <p>
 	 *  2. Crop: Change the size of the array. 
 	 *  for example, if the input array is 100 elements, crop it so that the output is only elements 10 to 20:
	 *  			 the size of the output array is 10)
	 * </br>
     * Bruteforce: O(newSize) worst-case.   Auxiliary Space: O(newSize)
     * </p>
     * 
     * @param c_array The current array to crop.        
     * @param newSize The cropped array resulting size.        
     * @param startPos The starting index.   
     * @return new_Array The cropped array
     * Run
    */
	public T [] crop (T [] c_array, int newSize, int startPos) {
		T [] new_Array = (T []) new Object[newSize]; 
        assert( new_Array.getClass().getComponentType() == c_array.getClass().getComponentType() ) ;

		if (c_array.length >=20) {
			// copy elements from position 10 to 20
			for (int i=10; i<20; i++)
				new_Array[i] = c_array[i];
		}
		else {
			for (int i=0; i<newSize; i++)
				new_Array[i] = c_array[i];
		}
		// copy back to array object.
		c_array = new_Array;
		return new_Array ;
	} 
	/**
     * Crop method overloaded.
     * @param c_array The current 2D array to crop.        
     * @param newSize The cropped array resulting size.        
     * @param startPos The starting index.   
     * @return new_Array The cropped array
     * Run
    */
	public T [] crop (T [][] c_array, int newSize, int startPos) {

	}


    /** 
     * <p>
 	 *  3. Fill: Similar to replace: Starting at a given index in an array, replace adjacent elements
 	 *  		 if their value is the same (similar to the "paint bucket" fill tool in image editing software)
 	 *  For example, given the array: [0, 2, 0, 1, 1, 1, 2, 2, 1], 
	 *  and filling position 4 with the value "7", the result would be: [0, 2, 0, 7, 7, 7, 2, 2, 1]
	 * </br>
	 * Multi-threading used. Runtime -> O (N / number of processors). Proportional to # of processors.
	 * No Synchronization. There's no concurrent access to same memory location.
     * </p>
     * 
     * @param array The array with elements to fill.        
     * @param newValue The new value .        
     * @param s_index The starting index.   
     * @return void.
     * Run
    */
	public void fill (T [] array, T newValue, int s_index) {
		int size = array.length;
		ExecutorService executor = Executors.newFixedThreadPool(n_threads);
		if (size>=40) { // Parallelize if N >= 40
	        int n_threads = 4;

			// E.g Divide among (N=4) threads = (0, size/4), (size/4, size/2), (size/2, size/2+size/4), (size/2+size/4, size)... ;
	        for (int i = s_index; i < size; i+=(size/n_threads) ) {
	        	if (i == s_index) {
	        		Runnable worker = new Fill_inParallel(array, oldValue, newValue, i, i+(size/n_threads) );
	            	executor.execute(worker);
	        	}
	        	// For all other remaining threads start at end of the array processed by previous thread,
	        	//  to cater for the gaps between splited arrays. for eg. thread-1 [1,2,4], thread-2 [4,4,7]
	        	else {
	        		Runnable worker = new Fill_inParallel(array, oldValue, newValue, i-1, i+(size/n_threads) );
	            	executor.execute(worker);
	        	}
	        }
	        // shutdown executor service
	        try {
	        	executor.shutdown();
	        	while (!executor.isTerminated()) {} // System.out.println("All threads Finished!");
	        }
			catch (Exception e) { System.err.println("tasks interrupted"); }
			finally {
			    if (!executor.isTerminated()) { System.err.println("cancel incomplete tasks"); }
			    executor.shutdownNow(); // System.out.println("shutdown finished");
			}
		}
		// serial 
		else { 
			Runnable worker = new Fill_inParallel(array, oldValue, newValue, 0, size);
	        executor.execute(worker);
		}
	}

    /** 
     * <p>
 	 *  4. Smooth: replace values smaller than a minimum and larger than a maximum with the average of their neighbours. 
	 *  For example, smoothing given min 0 and max 100 and array: 
	 * 		[40, 5, 200, 15]
	 *  the result would be: 
	 *  	[40, 5, 10, 15]   ... (5+15)/2 = 10
     * </p>
     * 
     * @param array The array with elements to replace.        
     * @param min The minimum value.        
     * @param max The maximum value.   
     * @return void.
     * Run
    */
	public void smooth (T [] array, T min, T max) {
		int arr_size = array.length ;
		if (arr_size>=2) {
			array[0] = (array[1]>max || array[1]<min)? : (array[1]) / 2 : array[0] ;
		}
		for (int i=2; i<arr_size; i++) { // 0 1 2 
			if ( array [arr_size-1] > max || array[arr_size-1] < min ) {
				array[i-1] = (array[i-2] + array[i] ) / 2 ;
			}
		}
		if (arr_size>=2) {
			array[arr_size-1] = (array[arr_size-2]>max || array[arr_size-2]<min)? 
								(array[arr_size-2]) / 2 : array[arr_size-1];
		}
	}

	// Extra fun (optional):
	// =================================================== 
    /**
     * <p>
 	 *  1. Blur: Replace every value with the average of itself, and its two neighbours
     * </p>
     * 
     * @param array The array with elements to replace.   
     * @return void
     * Run
     */
	public void blur (T [] array) {

	}

    /**
     * <p>
 	 *  2. Edge detection:  Detect places where the values in the array change.
 	 *	For example, given:
	 * 		[5, 5, 5, 2, 2, 2, 2, 3, 3] 
	 *  and performing edge detection, the result could be:
	 *		[0, 0, 1, 1, 0, 0, 1, 1, 0]
     * </p>
     * 
     * @param array The array with elements to perform edge Detection on.   
     * @return void
     * Run 
    */
	public void edgeDetection (T [] array) {

	}

    /** 
     * <p>
 	 *  GenerateTestArray: Generate a Test Array with N random integers.
     * </p>
     * @param n_Integers The number of integers to generate
     * @return array of Integers
     * Run
     */
	public Integer [] GenerateTestArray (int n_Integers) { 
    	Integer[] test_arr = new int[n_Integers];
    	for (int i = 0; i < n_Integers; i++) {
      		test_arr[i] = (Integer)(Math.random()*n_Integers);
      		// System.out.println(Array[i] + ", " );
    	}
    	return test_arr;
	}






	public static void main(String [] args) {
        //Integer [] large_array = {1,2,3,4,5,6,7,7,7,8,8,3,3,2,1,0,9,76,2,3,4,56,44,267,72,274,47,8,88,6,2,34,67,55,7,7};
        ArrayEditor <Integer> arrayEditor = new ArrayEditor <Integer>();
        Integer [] large_array = arrayEditor.GenerateTestArray();
        try {
        	long start_time = System.nanoTime();
        	
        	arrayEditor.replace(large_array, 7, 777);
        	// arrayEditor.crop(large_array, 7, 777);
        	// arrayEditor.fill(large_array, 7, 777);
        	// arrayEditor.smooth(large_array, 7, 777);
            
            System.out.println("Multi-threaded: ElapsedTime = " + (System.nanoTime()-start_time)/1e6 );
            // System.out.println("Array [ replaced_Pos ] = " + array[5]);   
        }
        catch (Exception e) { System.err.println("error"); }
	}

	
	public void unittest(){
		//
	}

}


