package InstanceGet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class MaximumCliqueConflictGraph {
    private List<Pair> conflicts;
    private int numVertices;
    private List<Pair> maxClique;

    public MaximumCliqueConflictGraph(List<Pair> conflicts) {
        this.conflicts = conflicts;
        this.numVertices = calculateNumVertices(conflicts);
        this.maxClique = new ArrayList<>();
    }

    public List<Pair> findMaximumClique() {
        List<Pair> currentClique = new ArrayList<>();
        Set<Pair> candidates = new HashSet<>(conflicts);

        findClique(currentClique, candidates);

        return maxClique;
    }

    private void findClique(List<Pair> currentClique, Set<Pair> candidates) {
        if (candidates.isEmpty() && currentClique.size() > maxClique.size()) {
            maxClique = new ArrayList<>(currentClique);
            return;
        }

        while (!candidates.isEmpty()) {
            Pair pair = candidates.iterator().next();
            List<Pair> newClique = new ArrayList<>(currentClique);
            newClique.add(pair);

            Set<Pair> newCandidates = new HashSet<>();
            for (Pair candidate : candidates) {
                if (pair.isConflict(candidate)) {
                    newCandidates.add(candidate);
                }
            }

            findClique(newClique, newCandidates);

            currentClique.add(pair);
            candidates.remove(pair);
        }
    }

    private int calculateNumVertices(List<Pair> conflicts) {
        Set<Integer> vertices = new HashSet<>();
        for (Pair pair : conflicts) {
            vertices.add(pair.getFirst());
            vertices.add(pair.getSecond());
        }
        return vertices.size();
    }



    public static void main(String[] args) {
        // Example usage
        List<Pair> conflicts = new ArrayList<>();
        conflicts.add(new Pair(0, 1));
        conflicts.add(new Pair(1, 2));
        conflicts.add(new Pair(2, 0));
        conflicts.add(new Pair(3, 4));
        conflicts.add(new Pair(4, 3));

        MaximumCliqueConflictGraph mccc = new MaximumCliqueConflictGraph(conflicts);
        List<Pair> maxClique = mccc.findMaximumClique();

        System.out.println("Maximum Clique Conflicts: " + maxClique);
    }

}
