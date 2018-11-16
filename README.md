# ArrayEditProcessing

## An array editor processing program

Programming Langauge: Java

Java Style: 120 characters per line

> To compile and run using makefile:
  ```
  make
  make run

  ```

Six functions implemented below.

## Replace
> For 1D and 2D arrays

Speed up Not possible for unsorted 1D array because, the runtime (and space) complexity 
would be worst than bruteforce as I analysed below:
 1. Create "array of maps" <Value, Original-index>
    - O(1)
 2. Sort array according to 'Value' with Mergesort into "array of maps":
    - O ( n*log(n) ) Average worst-case
 3. Search "array of maps" values with Binary search and use Orig-index to replace value on original array:
    - Average case: O ( log(n) + (number of value-occurences) )
Overal complexity:
- Best/Average/Worst-case : O ( 1 + n*log(n) + log(n) + (number of value-occurences) )
Auxiliary space: O(n)


## Crop
> For 1D and 2D arrays

## Fill
> For 1D and 2D arrays

## Smooth
> For 1D and 2D arrays

## Blur
> For 1D and 2D arrays


