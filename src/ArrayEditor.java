// package array_editor;
import java.io.*;
import java.io.IOException;
import java.lang.Thread;
import java.lang.InterruptedException;
import java.util.Scanner; 

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Arrays;
import java.util.LinkedList; 
import java.util.Queue; 
import java.lang.Math.*;

/**
* <p>
* @Author Masixole M. Ntshinga
* @Version 11/11/2018
*
*  Program: Array Editor
*  Problem: A program that performs operations on a given array.
*  			Operations supported by the program to are: Replace, Crop, Fill, Smooth, Blur, Edge
* </br>
*  Note:
*   • Style: Maximum line length: 120 characters
*   • Program accepts input array and provides output
*   • Methods accessed through OOP/instantiation/invocation
*   • Edge cases?: Methods overloaded for 2-dimensional arrays
*   • Program tested with unit tests
* </br>
* To further enhance performane, plan is to use DP or parallelize for loops Using: 
* - Java multi-threading - for shared memory computers
* - HPC OpenMP(JaMP) - for shared memory computers
* - MPI - for clusters and distributed memory computers (or super-computers)
* </p>
*/

public class ArrayEditor {
	private int n_threads=4; // default threads = 4
	
	public ArrayEditor () { }
	public ArrayEditor (int num_threads) { 
		this.n_threads = num_threads;
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
	public void replace (int [] array, int oldValue, int newValue) 
	{
		// Parallelize among N processors with thread processes.
		// E.g Divided among (n=4) threads : (0, size/4), (size/4, size/2), (size/2, size/2+size/4), 
        //								     (size/2+size/4, size)... ; 
		int size = array.length; 

		// Parallelize if N >= 20
		if (size>=20) {
			ExecutorService executor = Executors.newFixedThreadPool(this.n_threads);
			for (int i = 0; i < size; i+=size/this.n_threads) { // divide among nthreads
				final int ii = i;
				executor.submit(new Runnable() {
					@Override
					public void run() {
						// Bruteforce: O(n) worst-case.   Auxiliary Space:
						for (int col = ii; col<ii+(size/n_threads); col++) {
							if (array[col]== oldValue)
								array[col] = newValue;
						}
						// System.out.println(Thread.currentThread().getName());
					}
				});
	        }
	        // shutdown executor service
	        try {
	        	executor.shutdown();
	        	while (!executor.isTerminated()) {} //System.out.println("All threads Finished!");
	        }
			catch (Exception e) { System.err.println("tasks interrupted"); }
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
	public void replace (int [][] array, int oldValue, int newValue) {
		// Parallelize among N processors with thread processes.
		ExecutorService executor = Executors.newFixedThreadPool( this.n_threads ); // number of threads
		// x dimension
		for (int row=0; row<array.length; row++) { 
		   	// y dimension
		   	final int rowx=row; 
		   	executor.submit(new Runnable() {
				@Override
				public void run() {
					replace( array[rowx], oldValue, newValue );
					// System.out.println(Thread.currentThread().getName());
				}
			});
		}
		// shutdown executor service
	    try {
	    	executor.shutdown();
	        while (!executor.isTerminated()) {} 
	        // System.out.println("All 'replace' threads Finished!");
		}
		catch (Exception e) { System.err.println("tasks interrupted"); }
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
    */
	public int [] crop (int [] c_array, int col_from, int col_to) {
		// cropped outer array size = (col_to-col_from)
		assert(col_to>col_from);
		int [] new_Array = new int [col_to - col_from]; 
        assert( new_Array.getClass().getComponentType() == c_array.getClass().getComponentType() ) ;

		if (c_array.length >= col_to) {
			// copy elements from position 10 to 20
			int i=0;
			for (int col = col_from; col < col_to; col++) {
				new_Array[i] = c_array[col];
				i++;
			}
		}
		// c_array = null;   // collected by garbage collector for deletion.
		// c_array = new_Array;  // copy back to array object.
		return new_Array ;
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
     * @return new_Array. The resulting array from cropping c_array with x,y edge dimensions
    */
	public int [][] crop (int [][] c_array, int row_from, int row_to, int col_from, int col_to) {
		assert(row_to>row_from && col_to>col_from);
		int [][] new_Array = new int[row_to-row_from][col_to-col_from]; 
		// 'cropped x dimension size' = (row_to-row_from) and 'cropped y dimension size' = (col_to-col_from)
        assert(new_Array.getClass().getComponentType()==c_array.getClass().getComponentType());

		ExecutorService executor = Executors.newFixedThreadPool( this.n_threads ); // number of threads
		if (c_array.length > row_to) { // check if row within bounds
			int i=0;
			for (int row = row_from; row < row_to; row++) {  // crop row dimension
				if (c_array[row].length > col_to) {   // check if column within bounds
					final int row_x=row; 
					final int row_i=i; 
					final int [][] new_Array_x = c_array; // copy object (reference)
					// Parallelize among N processors with thread processes. 
				   	executor.submit(new Runnable() {
				   		@Override
				       	public void run() {
				       		new_Array[row_i] = crop (new_Array_x[row_x], col_from, col_to); // crop column dimension
							// System.out.println(Thread.currentThread().getName());
				      	}
				   	});
				}
				else { // y out of bounds
				}
				i++;
			}
		}
		else { // x out of bounds
		}
		// shutdown executor service
	    try {
	    	executor.shutdown();
	        while (!executor.isTerminated()) {}
	        // System.out.println("All 'crop' threads Finished!");

		}
		catch (Exception e) { System.err.println("tasks interrupted"); }

		// c_array = null;   // Will be collected by garbage collector for deletion.
		// c_array = new_Array;  // Copy back to array object. Old reference deleted.
		return new_Array ; // Return reference to this new array.
	}

    /**
     * <p>
 	 *  3. Fill: Similar to replace: Starting at a given index in an array, replace adjacent elements
 	 *  		 if their value is the same (similar to the "paint bucket" fill tool in image editing software)
 	 *  For example, given the array: [0, 2, 0, 1, 1, 1, 2, 2, 1], 
	 *  and filling position 4 with the value "7", the result would be: [0, 2, 0, 7, 7, 7, 2, 2, 1]
	 * </br>
     * </p>
     * 
     * @param array The 1D array with elements to fill
     * @param newValue The new value
     * @param s_index The starting index
     * @return void. Makes changes to the array object passed by reference.
    */
	public int [] fill (int [] array, int newValue, int start_index) {
		int [][] array1Din2D = {array};
		int startYindex = 0; // indicates we only process a 1D array within a 2D array
		try {
			array = fill(array1Din2D, newValue, startYindex, start_index)[0];
		}
		catch (Exception expt)
		{ System.out.println("fill error"); }

		return array;
	}

    /** 
     * <p>
 	 *  Fill  - Overloaded for 2D. Makes changes to the array object passed by reference.
 	 *  Algorithm adapted from Flood fill on wikipedia.
 	 *  - To avoid the overhead of queue management, A loop is used over queue for the west and east directions 
	 *    as an optimization.
     * </p>
     * @param array The 2D array with elements to fill.        
     * @param newValue The new value.        
     * @param s_index The starting index.   
     * @return array. Makes changes to the array object passed by reference.
    */
	public int [][] fill(int [][] array, int replacementValue, int row_start, int col_start) 
						 throws CloneNotSupportedException {
		Queue<int[]> queue = new LinkedList<>();
	  	int [] startNode= {row_start, col_start};
	  	queue.add(startNode);

	  	// check if row_start< and col_start are within array bounds
	  	if ( row_start>= 0 && row_start<array.length && col_start>=0 && col_start<array[0].length ){
	  		int targetValue = array[row_start][col_start];
			// O ( n target occurrences)
		  	while (queue.size()>0) {
		    	int [] left = queue.peek();  //west
		    	int [] right = queue.peek().clone(); //east

		    	// Complexity : O (n target occurrences to the left)
		    	// Move 'left' to the left, while (array[x,y] on the left of 'left') is targetValue
		    	while ((left[0]-1)>=0 && array[left[0]-1][left[1]] == targetValue) {
		      		left[0] = left[0]-1;
		    	}
		    	// Complexity : O (n target occurrences to the right)
		   	 	// Move 'right' to the right, while (array[x,y] on the right of 'right') is targetValue
		    	while ((right[0]+1)<array.length && array[right[0]+1][right[1]] == targetValue){
		      		right[0] = right[0]+1;
		    	}

			    // For each value n between left and right:
			    // Complexity : O ( row (left-right) * col (left-right)  )
			    int col=left[1]; 
			    for (int row=left[0]; row <= right[0]; row++) {
			    	//Set the value of array[x][y] to replacementValue
			    	array[row][col] = replacementValue ;
				    // If the array-value to the top of array[x][y] is target-value
				    if ( (col+1)<array[row].length && array[row][col+1] == targetValue) {
				    	int [] value = {row, col+1};
				        queue.add(value); // add that array-value to Queue
				    }
				    // If the array-value to the bottom of n[x][y] is target-value:
				    if ( (col-1)>=0 && array[row][col-1] == targetValue) {
				    	int [] value = {row, col-1};
				    	queue.add(value); // add that array-value to Queue
				    }
			    }
			    queue.remove();
		  } // Continue looping until Queue is exhausted.
	  	}
	  return array;
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
     * @return void. Makes changes to the array object passed by reference.
     * 
    */
	public void smooth (int [] array, int min, int max) {
		smooth (array, null, 0, 0, min, max);
	}
    /**
     * <p>
 	 *  smooth: Smooth helper method - Overload for 2D arrays
     * </p> 
     * @param array The 1D array with elements to replace.
     * @param array2D The 2D array object with neighbors to aid replacement.   
     * @param top The value indicating if at top edge to check y neighbors. 
     			  value -1 indicates not at edge.
     * @param bottom The value indicating if at bottom edge to check y neighbors. 
     				  value -1 indicates not at edge
     * @return void.
    */ 
	public void smooth (int [] array, int [][] array2D, int top, int bottom, int min, int max) {
		int arraySize = array.length ;    // array size

		// If there is one or more neighbors
		if (arraySize>=2) {
			
			// left edge with right neighbor
			{
				int [] y_neighborsL = get_Y_Neighbors(array2D, 0, top, bottom) ; 
				// position 0, 1 is y neighbors sum, count. 
				array[0] = (array[1]>max || array[1]<min)? (array[1]+y_neighborsL[0])/(2+y_neighborsL[1]) : array[0]; 
			}
			
			// Process body positions [ 1 ... end-1 ]
			for (int i=2; i<arraySize; i++) {
				// if there are y neighbors
		       	// position 0, 1 is y neighbors sum, count. 
		       	int [] y_neighbors = get_Y_Neighbors(array2D, i-1, top, bottom) ; 

				if ( array[arraySize-1] > max || array[arraySize-1] < min ) { 
					array[i-1] = (array[i-2] + array[i] + y_neighbors[0] ) / (2+y_neighbors[1]) ;
				}
			}

			// righ edge with left neighbor
			{	
				int [] y_neighborsR = get_Y_Neighbors(array2D, arraySize-1, top, bottom) ; 
				// position 0, 1 is y neighbors sum, count.
				array[arraySize-1] = (array[arraySize-2]>max || array[arraySize-2]<min)? 
									 (array[arraySize-2]+y_neighborsR[0]) / (2+y_neighborsR[1]) : array[arraySize-1];
			}
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
     * @return void. Makes changes to the array object passed by reference.
    */
	public void smooth (int [][] array, int min, int max) {
		// Parallelize among N processors with thread processes. 
		ExecutorService executor = Executors.newFixedThreadPool( this.n_threads ); // number of threads
		for (int x=0; x < array.length; x++) { // smooth x dimension
			final int xx = x;
		  	executor.submit(new Runnable() {
		  		@Override
		       	public void run() {
		       		// Top-left edge   - x has 1 bottom y neighbor Or
			       	// Top-Middle edge - x has 1 top y neighbor 
		       		if (xx==0) {
						smooth(array[xx], array, -1, xx+1, min, max) ; // smooth y dimension
		       		}
			       	// Bottom-Right edge  - x has 1 top y neighbor Or
			       	// Bottom-Middle edge - x has 1 top y neighbor  
		       		else if (xx==array.length-1) {
						smooth(array[xx], array, xx-1, -1, min, max) ; // smooth y dimension
		       		}
			       	// Middle - x has 2 y neighbors
		       		else {
		       			// array[i][xx-1]+array[i][xx+1]
						smooth(array[xx], array, xx-1, xx+1, min, max) ; // smooth y dimension
		       		}
		       	}
		   	});
		}
		// shutdown executor service
	    try {
	    	executor.shutdown();
	        while (!executor.isTerminated()) {}
		}
		catch (Exception e) { System.err.println("tasks interrupted"); }
	}

	public int [] get_Y_Neighbors(int[][] array2D, int y, int top, int bottom) {
		int [] neighbors = {0, 0};  // sum=0, count=0
		// if there are y neighbors
		if (array2D!=null) {
			// Top-left edge   - x has 1 bottom y neighbor Or
			// Top-Middle edge - x has 1 top y neighbor 	
		    if (top>0 && bottom>0 && y<array2D[top].length ) {
		    	neighbors[0] = array2D[top][y] ;
		       	neighbors[1] = 1;
		    }
			// Bottom-Right edge  - x has 1 top y neighbor Or
			// Bottom-Middle edge - x has 1 top y neighbor 
			else if (top<0 && bottom<0 && y<array2D[top].length) {
				neighbors[0] = array2D[bottom][y] ;
			    neighbors[1] = 1;
			}
			// Middle - x has 2 y neighbors
			else if (y<array2D[top].length) { // array[xx-1][i]+array[xx+1][i]
				neighbors[0] = array2D[top][y]+array2D[bottom][y] ;
			    neighbors[1] = 2;
			}
			else { // no y neighbors
			}
		}
		else { // no y neighbors
		}
		return neighbors;
	}

	// Extra fun (optional):   
    /**
     * <p>
 	 *  1. Blur: Replace every value with the average of itself, and its two neighbours 
     * </p>
     * 
     * @param array The 1D array with elements to replace.
     * @return void. 
     */
    public void blur (int [] array) {
    	blur(array, null, 0, 0);
    }
    /**
     * <p>
 	 *  Blur: Helper method. Replace every value with the average of itself, and its four neighbours 
     * </p> 
     * @param array The 1D array with elements to aid replacement.   
     * @param array2D The 2D array object with neighbors to aid replacement.   
     * @param top The value indicating if at top edge to check y neighbors. 
     			  value -1 indicates not at edge.
     * @param bottom The value indicating if at bottom edge to check y neighbors. 
     				  value -1 indicates not at edge
     * @return void.
     * TODO : re-implement with Gaussian Blur algorithm 
     */
	public void blur (int [] array, int [][] array2D, int top, int bottom) {
		int arraySize = array.length ;    // array size
		int leftAdj = Integer.MIN_VALUE ;  // Temporary auxiliary holder for left adjacent value

		// There is one or more neighbors
		if (arraySize>=2) {
			for (int i=0; i < arraySize; i++) {
				// if there are y neighbors
				int [] y_neighbors = get_Y_Neighbors(array2D, i, top, bottom) ; 
				// position 0, 1 is y neighbors sum, count. 

				// If on the left or right edges of the x array ... x has 1 left/right x neighbor
				if (i==0 || i==arraySize-1 ) {
					leftAdj = array[i] ;
					array[i] = i==0? (array[i]+array[i+1]+y_neighbors[0])/(2+y_neighbors[1]) : 
									 (array[arraySize-2]+array[i]+y_neighbors[0])/(2+y_neighbors[1]) ;
				}
				// else process body positions [ 1 ... end-1 ] ... x has 1 x neighbor
				else {
					leftAdj = array[i] ;
					array[i] = ( array[i-1]+array[i]+array[i+1]+y_neighbors[0] )/(3+y_neighbors[1]) ;
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
     * @param array The 2D array with elements to aid replacement.   
     * @return void. Makes changes to the array object passed by reference.
     */
	public void blur (int [][] array) {
		// Parallelize among N processors with thread processes. 
		ExecutorService executor = Executors.newFixedThreadPool( this.n_threads ); // number of threads

		for (int x=0; x < array.length; x++) { // blur x dimension
			final int xx = x;
		   	executor.submit(new Runnable() {
		   		@Override
		       	public void run() {
		       		// Top-left edge   - x has 1 bottom y neighbor Or
			       	// Top-Middle edge - x has 1 top y neighbor 
		       		if (xx==0) {
		       			blur(array[xx], array, -1, xx+1) ; // blur y dimension
		       		}
			       	// Bottom-Right edge  - x has 1 top y neighbor Or
			       	// Bottom-Middle edge - x has 1 top y neighbor  
		       		else if (xx==array.length-1) {
		       			blur(array[xx], array, xx-1, -1) ; // blur y dimension
		       		}
			       	// Middle - x has 2 y neighbors
		       		else {
		       			// array[i][xx-1]+array[i][xx+1]
		       			blur(array[xx], array, xx-1, xx+1) ; // blur y dimension
		       		}
		      	}
		   	});
		}
		// shutdown executor service
	    try {
	    	executor.shutdown();
	        while (!executor.isTerminated()) {}
		}
		catch (Exception e) { System.err.println("tasks interrupted"); }
	}

	// Function to create Gaussian filter 
	public void blurGaussian (int [][] kArray) {
	}

    /**
     * <p>
 	 *  2. Edge detection:  Detect places where the values in the array change.
 	 *	For example, given:
	 * 		[5, 5, 5, 2, 2, 2, 2, 3, 3] 
	 *  and performing edge detection, the result could be:
	 *	    [0, 0, 1, 1, 0, 0, 1, 1, 0]
     * </p>
     * 
     * @param array The array with elements to perform edge Detection on.   
     * @return void. 
    */

	public void edgeDetection (int [] array) {
	}

    /**
     * <p>
 	 *  2. Edge detection - Overloaded for 2D.
     * </p>
     * @param array The array with elements to perform edge Detection on.   
     * @return void
    */
	public void edgeDetection (int [][] array) {
	}

    /** 
     * <p>
 	 *  GenerateTestArray: Generate a Test Array with N random integers.
     * </p>
     * @param n_Integers The number of integers to generate
     * @return array of Integers
    */
	public int [] GenerateTestArray (int n_Integers) { 
    	int [] test_arr = new int[n_Integers];
    	for (int i = 0; i < n_Integers; i++) {
      		test_arr[i] = (int)(Math.random()*n_Integers);
      		// System.out.println(Array[i] + ", " );
    	}
    	return test_arr;
	}


 	public static void main(String [] args)
 	{
        // Pass number of threads to use for parallelized processes.
        int threads = 8 ;
        ArrayEditor arrayEditor = new ArrayEditor( threads ); 
        
        int [] test_array_s = arrayEditor.GenerateTestArray(10);
        int [] test_array_m = arrayEditor.GenerateTestArray(1000);
        int [] test_array_l = arrayEditor.GenerateTestArray(1000000);
    	
    	int []   test_array1D = {1,2,3,4,5,6,7,7,7,8,8,3,3,2,1,0,9,7,4,5,4,2,3,3,17,2,3,3,7,7};
    	int [][] test_array2D = {{1, 1, 1, 1, 1, 1, 1, 1},
    							{1, 1, 1, 1, 1, 1, 0, 0},
    							{1, 0, 0, 1, 1, 0, 1, 1},
    						    {1, 2, 2, 2, 2, 0, 1, 0},
    						    {1, 1, 1, 2, 2, 0, 1, 0},
    						    {1, 1, 1, 2, 2, 2, 2, 0},
			                    {1, 1, 1, 1, 1, 2, 1, 1},
			                    {1, 1, 1, 1, 1, 2, 2, 1}};

		int [][] array1D = {test_array1D};

        try {
        	int col_from=2, col_to=6;
        	int row_from=2, row_to=6;

        	int min=0, max=2;
        	int oldValue = 2;
        	int newValue = 200;

			System.out.println("\nTesting with:");
			printArray(test_array1D, "Original 1D");
	       	printArray(test_array2D, "Original 2D");
        	{
        		long start_time = System.nanoTime();
	        	arrayEditor.replace(test_array1D, oldValue, newValue/60);	    // 1D  (array, oldValue, newValue) 
	        	arrayEditor.replace(test_array2D, oldValue, newValue/60);  // 2D
	       		printArray(test_array1D, "After 1D replace (v="+oldValue+", replacer="+newValue/60+") "+
	       			"threads="+threads);
	       		printArray(test_array2D, "After 2D replace (v="+oldValue+", replacer="+newValue/60+") "+
	       			"threads="+threads);

	        	// 1D  (array, row_from, row_to)
	        	int[] newArr = arrayEditor.crop(test_array1D, col_from, col_to);    
	        	// 2D  (array, row_from, row_to, col_from, col_to) 
	        	int[][] newArrD = arrayEditor.crop(test_array2D, row_from, row_to, col_from, col_to);  
	       		printArray(newArr, "After 1D crop (col_from="+col_from+", row_to="+col_to+")");
	       		printArray(newArrD, "After 2D crop (row_from="+row_from+", row_to="+row_to+
	       										 ", col_from="+col_from+", row_to="+col_to+")");
	        	arrayEditor.fill(test_array1D, newValue, col_from);    // 1D  (array, newValue, s_index)
	        	arrayEditor.fill(test_array2D, newValue, row_from, col_from);  // 2D  (array, newValue, row_from, col_to)
	       		printArray(test_array1D,"After 1D fill (v="+newValue+", row="+col_from+")");
	       		printArray(test_array2D,"After 2D fill (v="+newValue+", row="+row_from+", col="+col_from+")");

	        	arrayEditor.smooth(test_array1D, min, max);    // 1D  (array, min, max)
	        	arrayEditor.smooth(test_array2D, min, max);  // 2D 
	       		printArray(test_array1D,"After 1D smooth (min="+min+", max="+max+")");
	       		printArray(test_array2D,"After 2D smooth (min="+min+", max="+max+")");

	        	arrayEditor.blur(test_array1D);    // 1D
	        	arrayEditor.blur(test_array2D);  // 2D
	       		printArray(test_array1D,"After 1D blur");
	       		printArray(test_array2D,"After 2D blur");

	        	// arrayEditor.blurGaussian(test_array2D);  // 2D
	       		// printArray(test_array2D,"After 2D Blur-Gaussian");

	        	// arrayEditor.edgeDetection(test_array1D);    // 1D
	        	// arrayEditor.edgeDetection(test_array2D);  // 2D

	            System.out.println("ElapsedTime = " + (System.nanoTime()-start_time)/1e6 );
        	}
        }
        catch (Exception e) { System.err.println("error"); }
	}

	public static void printArray (int[] test_array, String label) {
		System.out.println("\n"+label);
		for (int c=0; c<test_array.length; c++) { 
			System.out.print(test_array[c]+", ");
		}
		System.out.println("");
	}
	public static void printArray (int[][] test_array, String label) {
		System.out.println(""+label);
		for (int r=0; r<test_array.length; r++) { 
			for (int c=0; c<test_array[r].length; c++) {
				System.out.print(test_array[r][c]+", ");
			}
			System.out.println("");
		}
	}

	public void unittest() {
		// 
	}

}


