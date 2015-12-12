/*
 * this class encode the source file block and continue send the package
 * out until the decoder decode the source file out.
 *
 * this file use the java 8 stream api, so you need to use the java version 8
 * if you want to use the lower version of java, just implement the same
 * logical.
 *
 */
package LTCode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.Optional;

import share.CodeHelper;
import share.LTPackage;

/**
 *
 * @author smile
 */
public class LTEncoder {

    private final short BLOCK_SIZE = 80;

    private final Map<Integer, byte[]> source_blocks;

    private long file_size;
    private long block_seed;

    private final CodeHelper helper;

    // default
    public LTEncoder(String filename)
            throws IOException {

        source_blocks = spliteSourceFile(filename);

        long k = source_blocks.size();

        helper = new CodeHelper(k);
    }

    // with delta, c
    public LTEncoder(String filename, double delta, double c)
            throws IOException {

        source_blocks = spliteSourceFile(filename);

        long k = source_blocks.size();

        helper = new CodeHelper(k, delta, c);
    }

    // with seed
    public LTEncoder(String filename, double delta, double c, long seed)
            throws IOException {

        source_blocks = spliteSourceFile(filename);

        long k = source_blocks.size();

        helper = new CodeHelper(k, delta, c);

        helper.setSeed(seed);
    }

    /**
     *
     * @return the package bytes, this bytes array will send in network
     */
    public byte[] getNextPackage() {

        block_seed = helper.getSeed();

        // get source block first
        Set<Integer> blocks = helper.getSrcBlocks();

        Optional<byte[]> block_data;
        block_data = blocks.parallelStream()
                .map(index -> source_blocks.get(index))
                .reduce((a, b) -> {
                    return xorOperation(a, b, BLOCK_SIZE);
                });

        // build the package
        LTPackage.PackageBuilder builder = new LTPackage.PackageBuilder();

        builder.fileSize(file_size);
        builder.blockSize(BLOCK_SIZE);
        builder.blockSeed(block_seed);
        builder.blockData(block_data.get());

        return LTPackage.toBytes(builder);
    }

    /**
     * 
     * @param a byte[] operand, this parameter can be changed.
     * @param b byte[] operand, this parameter can be changed.
     * @param size the length of the byte [], this parameter can be changed.
     * @return the byte[] of length size
     */
    private byte[] xorOperation(
            final byte[] a, 
            final byte[] b, 
            final int size) {

        byte[] return_value = new byte[size];
        
        for (int index = 0; index < size; index++) {
            return_value[index] = (byte) (a[index] ^ b[index]);
        }

        return return_value;
    }

    /**
     *
     * @param file source file to send
     * @return block map contain the source file data
     */
    private Map<Integer, byte[]> spliteSourceFile(String filename)
            throws FileNotFoundException, IOException {

        Map<Integer, byte[]> blocks_map = new HashMap<>();

        // set the file size
        File file = new File(filename);

        file_size = file.length();

        // get the data block map
        int index = 0;
        int remain = 0;
        FileInputStream fin = new FileInputStream(file);
        
        while ((remain = fin.available()) > 0) {
            byte[] block = new byte[BLOCK_SIZE];
            if (remain >= BLOCK_SIZE) {
                fin.read(block, 0, BLOCK_SIZE);
                blocks_map.put(index, block);
            } else {
                fin.read(block, 0, remain);
                blocks_map.put(index, block);
            }

            index++;
        }

        return blocks_map;
    }

    /**
     * @throws java.io.IOException
     * @brief test this module
     *
     * @param args
     */
    public static void main(String[] args) throws IOException {

        if (args.length > 0) {

            LTEncoder encoder = new LTEncoder(args[0]);

            OutputStream out = System.out;

            int index = 0;
            while (index < 10000) {

                byte[] pack;
                pack = encoder.getNextPackage();
                out.write(pack);
                index++;
            }
        }
    }
}
