import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MaxUtilization {
    static List<List<Process>> getOptimalProcessPairs(List<Process> p1, List<Process> p2, int memoryCapacity) {
        int maxMemory = 0;
        final List<List<Process>> result = new ArrayList<>();
        for (final Process p : p1) {
            int memory = memoryCapacity - p.memory;
            final List<Process> potentialPairs = getProcessesLessThan(p2, memory);
            if (potentialPairs == null || potentialPairs.isEmpty() || p.memory + potentialPairs.get(0).memory < maxMemory) {
                continue;
            }
            if (potentialPairs.get(0).memory + p.memory > maxMemory) {
                result.clear();
                maxMemory = potentialPairs.get(0).memory + p.memory;
            }

            for (final Process p2Pair : potentialPairs) {
                final List<Process> optimalPair = new ArrayList<>();
                optimalPair.add(p);
                optimalPair.add(p2Pair);
                result.add(optimalPair);
            }
        }
        return result;
    }

    final static List<Process> getProcessesLessThan(List<Process> p, int memory) {
        final TreeMap<Integer, List<Process>> map = new TreeMap();

        for (final Process process : p) {
            if (!map.containsKey(process.memory)) {
                map.put(process.memory, new ArrayList<>());
            }
            map.get(process.memory).add(process);
        }
        final Map.Entry<Integer, List<Process>> floor = map.floorEntry(memory);
        return floor != null ? floor.getValue() : new ArrayList<>();
    }

    public static void main(String[] args) {
        final List<Process> p1 = new ArrayList<>();
        p1.add(new Process("p1_1", 2));
        p1.add(new Process("p1_2", 7));
        p1.add(new Process("p1_3", 3));
        p1.add(new Process("p1_4", 2));

        final List<Process> p2 = new ArrayList<>();
        p2.add(new Process("p2_1", 1));
        p2.add(new Process("p2_2", 2));
        p2.add(new Process("p2_3", 3));
        p2.add(new Process("p2_4", 3));
        p2.add(new Process("p2_5", 4));

        System.out.println(getOptimalProcessPairs(p1, p2, 5));

    }


    static class Process {
        public String id;
        public int memory;

        public Process(String id, int memory) {
            this.id = id;
            this.memory = memory;
        }

        public String getId() {
            return id;
        }

        public int getMemory() {
            return memory;
        }

        @Override
        public String toString() {
            return id + ":" + memory;
        }
    }
}
