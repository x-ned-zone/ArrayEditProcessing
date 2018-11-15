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
*  Problem: A program that performs operations on a given array.
*  			Operations supported by the program to are: Replace, Crop, Fill, Smooth, Blur, Edge
* @Author Masixole M. Ntshinga
* @Version 11/11/2018
* </br>
*  Note:
*   • Style: Maximum line length: 120 characters
*   • How program accepts input array and provides output
*   • Methods accessed through OOP/instantiation/invocation
*   • Edge cases?: Methods overlaoded for 2-dimensional arrays
*   • Program tested with unit tests
* </br>
* To further enhance performane, plan is to use DP or parallelize for loops Using: 
* - Java multi-threading - for shared memory computers
* - HPC OpenMP(JaMP) - for shared memory computers
* - MPI - for clusters and distributed memory computers (or super-computers)
* </p>
*/

public class ArrayEditor<T> {
	private int n_threads;
	
	public ArrayEditor () { }
	public ArrayEditor (int num_threads) { 
		this.n_threads = m_threads;
	}
    /**
     * <p>
 	 * 1. Replace: Replace all instances of one value with another.
	 * </br>
	 * Assumptions:
	 * - Array contains integers or floats
	 * - The order of array elements matters
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
		int size = array.length; 

		// Parallelize if N >= 40
		if (size>=40) {
        	ExecutorService executor = Executors.newFixedThreadPool(n_threads);

        	// E.g Divided among (n=4) threads : (0, size/4), (size/4, size/2), (size/2, size/2+size/4), 
        	//								     (size/2+size/4, size)... ; 
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
			    if (!executor.isTerminated()) { 
			    	System.err.println("cancel incomplete tasks"); 
			    }
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
	 * Replace - Overloaded for 2D.
     * @param array The 2D array with elements to replace
     * @param oldValue The old value to be searched for occurances in the array
     * @param newValue The new value to replace occurances of the old value in the array
     * @return void.
    */ 
	public void replace (T [][] array, T oldValue, T newValue) throws CloneNotSupportedException {
		// ... Possibly parallelize among N threads
		for (int x=0; x<array.length; x++) { // x dimension
			  replace( array[x], oldValue, newValue ); // y dimension
		}
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
     * @param c_array The 1D array to crop.
     * @param x_from The crop start-index for x dimension
     * @param x_to The crop end-index for x dimension
     * @return new_Array The cropped array
     * Run
    */
	public T [] crop (T [] c_array, int x_from, int x_to) {
		
		int cropped_Size = x_to - x_from ;  // cropped outer array size

		T [] new_Array = (T []) new Object[cropped_Size]; 
       
        assert( new_Array.getClass().getComponentType() == c_array.getClass().getComponentType() ) ;

		if (c_array.length >= x_to) {
			// copy elements from position 10 to 20
			int i=0;
			for (int x = x_from; x < x_to; x++) {
				new_Array[i] = c_array[x];
				i++;
			}	
		}
		// c_array = null;   // collected by garbage collector for deletion.
		c_array = new_Array;  // copy back to array object.
		return c_array ;
	}	
	/**
     * <p> 
     *  Crop - overloaded for 2D.
	 * </br>
     * Bruteforce: O(N*M) average/worst-case.   Auxiliary Space: O(x-dimension * y-dimension + 2)
     * </p>
     * @param c_array The current 2D array to crop 
     * @param x_from The crop start-index for x dimension
     * @param x_to The crop end-index for x dimension
     * @param y_from The crop start-index y dimension
     * @param y_to The crop end-index y dimension
     * @return new_Array The resulting array from cropping c_array with x,y edge dimensions
    */
	public T [][] crop (T [][] c_array, int x_from, int x_to, int y_from, int y_to) {
		int c_XSize = x_to - x_from ; 	// cropped x dimension size
		int c_YSize = y_to - y_from ;	// cropped y dimension size
		T [][] new_Array = (T []) new Object[c_XSize][c_YSize]; 
        assert( new_Array.getClass().getComponentType() == c_array.getClass().getComponentType() ) ;

		if (c_array.length >= x_to) { // check if x within bounds
			int i=0;
			for (int x = x_from; x < x_to; x++) {  // crop x dimension
				if (c_array[x].length >= y_to) { //  check if y within bounds
					new_Array[i] = crop (c_array[x], y_from, y_to); // crop y dimension
				}
				i++;
			}
		}

		// c_array = null;   // will be collected by garbage collector for deletion.
		c_array = new_Array;  // copy back to array object.
		return new_Array ;
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
     * @param array The 1D array with elements to fill
     * @param newValue The new value
     * @param s_index The starting index
     * @return void
     * Run
    */
	public void fill (T [] array, T newValue, int s_index) {
		int size = array.length;

		// assuming no possible value in the array can match or exceed 'Integer.MIN_VALUE'.
		Object prevAdj = Integer.MIN_VALUE ;  // Use previous adjacent auxiliary holder

		// Serial / bruteforce. Cannot parallelize because i depends on i-1.
		for (int i = this.startIndex+1; i<this.endIndex; i++) {
			if (prevAdj > Integer.MIN_VALUE && array[i]==prevAdj) {
				array[i] = (T) newValue;
			}
			else {
				if ( array[i]==array[i-1] ) {
					prevAdj = array[i];
					array[i-1] = newValue;
				}
				else {
					prevAdj = Integer.MIN_VALUE ; 
				}
			}
		}

		// ExecutorService executor = Executors.newFixedThreadPool(n_threads);
		// Parallelize if N >= 40
		// if (size>=40) { 
	    	// int n_threads = 4;

		// 	// E.g Divide among (N=4) threads = (0, size/4), (size/4, size/2), (size/2, size/2+size/4), 
		// 									 	(size/2+size/4, size)... ;
	 //        for (int i = s_index; i < size; i+=(size/n_threads) ) {
	 //        	if (i == s_index) {
	 //        		Runnable worker = new Fill_inParallel(array, oldValue, newValue, i, i+(size/n_threads) );
	 //            	executor.execute(worker);
	 //        	}
	 //        	// For all other remaining threads start at end of the array processed by previous thread,
	 //        	//  to cater for the gaps between splited arrays. for eg. thread-1 [1,2,4], thread-2 [4,4,7]
	 //        	else {
	 //        		Runnable worker = new Fill_inParallel(array, oldValue, newValue, i-1, i+(size/n_threads) );
	 //            	executor.execute(worker);
	 //        	}
	 //        }
	 //        // shutdown executor service
	 //        try {
	 //        	executor.shutdown();
	 //        	while (!executor.isTerminated()) {} // System.out.println("All threads Finished!");
	 //        }
		// 	catch (Exception e) { System.err.println("tasks interrupted"); }
		// 	finally {
		// 	    if (!executor.isTerminated()) { System.err.println("cancel incomplete tasks"); }
		// 	    executor.shutdownNow(); // System.out.println("shutdown finished");
		// 	}
		// }
		// serial 
		// else {
		// }
	}
    /** 
     * <p>
 	 *  Fill  - Ooverloaded for 2D
     * </p>
     * @param array The 2D array with elements to fill.        
     * @param newValue The new value .        
     * @param s_index The starting index.   
     * @return void.
     * Run
    */
	public void fill (T [][] array, T newValue, int s_index) {
	}

    /** 
     * <p>
 	 *  4. Smooth: replace values smaller than a minimum and larger than a maximum with the average of their neighbours
	 *  For example, smoothing given a min 0 and a max 100 and array:  [40, 5, 200, 15]
	 *  the result would be:  [40, 5, 10, 15]   ... (5+15)/2 = 10
     * </p>
     * 
     * @param array The 1D array with elements to replace.        
     * @param min The minimum value.        
     * @param max The maximum value.   
     * @return void.
     * 
    */    
	public void smooth (T [] array, T min, T max) {
		int arraySize = array.length ;    // array size

		// If there is one or more neighbors
		if (arraySize>=2) {
			// left edge with right neighbor 
			array[0] = (array[1]>max || array[1]<min)? : (array[1])/2 : array[0]; 
			// process body positions [ 1 ... end-1 ]
			for (int i=2; i<arraySize; i++) {
				if ( array[arraySize-1] > max || array[arraySize-1] < min ) {
					array[i-1] = (array[i-2] + array[i] ) / 2 ;
				}
			}
			// righ edge with left neighbor 
			array[arraySize-1] = (array[arraySize-2]>max || array[arraySize-2]<min)? 
									(array[arraySize-2]) / 2 : array[arraySize-1];
		}
		// No neighbors
		else {
		}
	}

    /** 
     * <p>
 	 *  4. Smooth - Overloaded for 2D.
     * </p>
     * @param array The 2D array with elements to replace.        
     * @param min The minimum value.        
     * @param max The maximum value.   
     * @return void.
     * Run
    */
	public void smooth (T [][] array, T min, T max) {

	}

	// Extra fun (optional):   
    /**
     * <p>
 	 *  1. Blur: Replace every value with the average of itself, and its two neighbours 
     * </p>
     * 
     * @param array The 1D array with elements to replace.   
     * @return void
     */
	public void blur (T [] array) {
		int arraySize = array.length ;    // array size
		T leftAdj = Integer.MIN_VALUE ;  // Temporary auxiliary holder for left adjacent value

		// There is one or more neighbors
		if (arraySize>=2) {
			for (i=0; i < arraySize; i++) {
				// If on the left or right edges of the array  
				if (i==0 || i==arraySize-1 ) {
					leftAdj = array[i] ;
					array[i] = i==0? (array[i]+array[i+1])/2 : (array[arraySize-2]+array[i])/2 ;
				}
				// else process body positions [ 1 ... end-1 ]
				else {
					leftAdj = array[i] ;
					array[i] = ( array[i-1]+array[i]+array[i+1] )/2 ;
				}
			}
		}
		// No neighbors
		else {
		}
	}
    /**
     * <p>
 	 *  Blur - Overloaded for 2D.
     * </p>
     * @param array The 2D array with elements to replace.   
     * @return void
     */
	public void blur (T [][] array) {
	}
    /**
     * <p>
 	 *  2. Edge detection:  Detect places where the values in the array change.
 	 *	For example, given:
	 * 		[5, 5, 5, 2, 2, 2, 2, 3, 3] 
	 *  and performing edge detection, the result could be:
	 *	      [0, 0, 1, 1, 0, 0, 1, 1, 0]
     * </p>
     * 
     * @param array The array with elements to perform edge Detection on.   
     * @return void
    */

	public void edgeDetection (T [] array) {
		
	}

    /**
     * <p>
 	 *  2. Edge detection - Overloaded for 2D.
     * </p>
     * @param array The array with elements to perform edge Detection on.   
     * @return void
    */
	public void edgeDetection (T [][] array) {

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
        // pass number of threads to use for parallelized processes.
        ArrayEditor <Integer> arrayEditor = new ArrayEditor <Integer>( 4 ); 
        Integer [] large_array = arrayEditor.GenerateTestArray();
        try {
        	long start_time = System.nanoTime();
        	
        	arrayEditor.replace(large_array, 7, 111);	    // 1D  (array, oldValue, newValue) 
        	// arrayEditor.replace(large_array2D, 7, 111);  // 2D


        	// arrayEditor.crop(large_array, 10, 20);    // 1D  (array, x_from, x_to)
        	// arrayEditor.crop(large_array2D, 10, 20);  // 2D  (array, x_from, x_to, y_from, y_to) 

        	// arrayEditor.fill(large_array, 7, 4);    // 1D  (array, newValue, s_index)
        	// arrayEditor.fill(large_array2D, 7, 4);  // 2D

        	// arrayEditor.smooth(large_array, 0, 100);    // 1D  (array, min, max)
        	// arrayEditor.smooth(large_array2D, 0, 100);  // 2D

        	// arrayEditor.blur(large_array);    // 1D
        	// arrayEditor.blur(large_array2D);  // 2D

        	// arrayEditor.edgeDetection(large_array);    // 1D
        	// arrayEditor.edgeDetection(large_array2D);  // 2D
            
            System.out.println("Multi-threaded: ElapsedTime = " + (System.nanoTime()-start_time)/1e6 );
            // System.out.println("Array [ replaced_Pos ] = " + array[5]);   
        }
        catch (Exception e) { System.err.println("error"); }
	}

	
	public void unittest(){
		//
	}

}


