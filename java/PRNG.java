/*
 * @brief
 *      this class will generate the random number for the LT de/en coder
 */
package share;

/**
 *
 * @author smile
 */
public class PRNG {
    
    private final static long PRNG_A = 16807;
    private final static long PRNG_M = (1 << 31) - 1;
    private final static long PRNG_MAX_RAND = PRNG_M - 1;
    
    private long state;
    
    /**
     * @brief make the state a random number
     */
    public PRNG(){
        
        //state = (long) (Math.random() * PRNG_MAX_RAND);
        // make a fix value is good, if the user do not set the initialize value
        state = 2067261;
    }
    
    /**
     * 
     * @param seed state to set
     */
    public final void setSeed(long seed){
        state = seed;
    }
    
    /**
     * 
     * @return current state
     */
    public final long getState(){
        return state;
    }
    
    /**
     * 
     * @return random next integer number
     */
    public final int nextInt(){
        
        state = state * PRNG_A % PRNG_M;
        return (int) state;
    }
    
    /**
     * @brief test the pseudo random number generation
     */
    public static void main(String args[]){
        
        PRNG prng = new PRNG();
        
        for(int i=0; i<10; i++)
            System.out.println(prng.nextInt());
        
    }
}
