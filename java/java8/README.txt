
Luby Transform Code
-------------------

The java version is developed under the ubuntu 14.04 and oracle
jdk. That means you can use the jar file as a library in other
system, but you must use the java version larger then jdk1.8.

If you want to use lower java version, you need to change the 
java 8 stream into the data structure you create.

Default Parameters
------------------

The java verion make everything default other than the transform
file. The list below show some important params:

	1. K
	2. c = 0.1
	3. delta = 0.5

	4. seed = 2067261
	5. PRNG_A = 16807
 	6. PRNG_M = (1 << 31) - 1
 	7. PRNG_MAX_RAND = PRNG_M - 1

	8. package header size:	20
	9. package block size: 80

The transform package contain the header and the block data.
the header contains CRC16, filesize, blocksize, blockseed and 
the block data contains the blocks content.

       |   CRC 16  | file size | block size | seed    | data |
       |   2 bytes |  8 bytes  |   2 bytes  | 8 bytes | 80 bytes |
       |   short   |   long    |   short    |  long   | byte[] |


