/*
 * this class is used in decode process to handle the data block in complex
 * graph.
 *
 */
package share;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author smile
 */
public class GraphResolver implements Iterable<byte[]> {

    private final long number_of_block;
    private final Map<Integer, Set<GraphNode>> graph;
    private final Map<Integer, byte[]> resolved_block;

    public GraphResolver(long k) {

        number_of_block = k;
        graph = new HashMap<>();
        resolved_block = new HashMap<>();
    }

    /**
     *
     * @param blocks related blocks
     * @param data block data
     * @return true if the graph resolve done
     */
    public boolean addBlock(Set<Integer> blocks, byte[] data) {

        // just contain only one source block
        if (1 == blocks.size()) {

            Set<Tuple<Integer, byte[]>> to_eliminate
                    = resolve(blocks.iterator().next(), data);

            Tuple<Integer, byte[]> current_eliminate;
            // recursively eliminate until empty
            while (!to_eliminate.isEmpty()) {
                // get
                current_eliminate = to_eliminate.iterator().next();
                // operate
                to_eliminate.addAll(resolve(
                        current_eliminate.getFirst(),
                        current_eliminate.getSecond()));
                // remove
                to_eliminate.remove(current_eliminate);
            }

        } else {

            List<Integer> to_remove = blocks.parallelStream()
                    .filter(key -> resolved_block.containsKey(key))
                    .collect(Collectors.toList());

            // remove relation and fresh data
            int size = data.length;
            for (int key : to_remove) {
                blocks.remove(key);
                data = xorOperation(resolved_block.get(key), data, size);
            }

            if (1 == blocks.size()) {

                return addBlock(blocks, data);
            } else {

                GraphNode node = new GraphNode(blocks, data);

                // add to each seperate block
                blocks.parallelStream().forEach((key) -> {
                    graph.getOrDefault(key, new HashSet<>()).add(node);
                });
            }
        }

        return resolved_block.size() >= number_of_block;
    }

    /**
     *
     * @param block source block index
     * @param data block data
     * @return new single block data
     */
    private Set<Tuple<Integer, byte[]>> resolve(int block, byte[] data) {

        // save information firstly
        resolved_block.put(block, data.clone());
        Set<GraphNode> nodes = graph.getOrDefault(block, new HashSet<>());
        graph.remove(block);

        // pass message to all associated nodes
        Iterator<GraphNode> node_iterator = nodes.iterator();
        Set<Tuple<Integer, byte[]>> eliminate_set = new HashSet<>();

        while (node_iterator.hasNext()) {

            GraphNode current_node = node_iterator.next();
            current_node.data
                    = xorOperation(current_node.data, data, data.length);
            current_node.blocks
                    .remove(block);

            if (1 == current_node.blocks.size()) {
                // create new tuple
                Tuple<Integer, byte[]> tuple = new Tuple<>(
                        current_node.blocks.iterator().next(),
                        current_node.data);
                // add to the set
                eliminate_set.add(tuple);
            }
        }

        return eliminate_set;
    }

    /**
     *
     * @return the data of source file use iterator, the order is very important
     */
    @Override
    public Iterator<byte[]> iterator() {

        return resolved_block.entrySet().parallelStream()
                .sorted((a, b) -> {
                    return a.getKey().compareTo(b.getKey());
                })
                .map(entry -> entry.getValue())
                .collect(Collectors.toCollection(ArrayList::new)) // ordered it
                .iterator();
    }

    /**
     *
     * @param a byte[] operand, this parameter can be changed.
     * @param b byte[] operand, this parameter can be changed.
     * @param size the length of the byte [], this parameter can be changed.
     * 
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

    // graph node here 
    private class GraphNode {

        public Set<Integer> blocks;
        public byte[] data;

        public GraphNode(Set<Integer> blocks, byte[] data) {

            this.data = data.clone();
            this.blocks = blocks;
        }
    }

    // tuple class
    private class Tuple<E1, E2> {

        private final E1 e1;
        private final E2 e2;

        public Tuple(E1 e1, E2 e2) {
            this.e1 = e1;
            this.e2 = e2;
        }

        public E1 getFirst() {
            return e1;
        }

        public E2 getSecond() {
            return e2;
        }
    }

}
