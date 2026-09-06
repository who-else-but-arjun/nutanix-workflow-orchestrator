package com.flowforge.domain.workflow;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DagValidator {
    public void validate(WorkflowDefinition definition) {
        if (definition.workflowName() == null || definition.workflowName().isBlank()) {
            throw new InvalidWorkflowException("workflowName is required");
        }
        if (definition.nodes().isEmpty()) {
            throw new InvalidWorkflowException("at least one node is required");
        }

        Set<String> nodeIds = new HashSet<>();
        for (WorkflowNode node : definition.nodes()) {
            if (node.id() == null || node.id().isBlank()) {
                throw new InvalidWorkflowException("node id is required");
            }
            if (node.type() == null || node.type().isBlank()) {
                throw new InvalidWorkflowException("node type is required for " + node.id());
            }
            if (!nodeIds.add(node.id())) {
                throw new InvalidWorkflowException("duplicate node id: " + node.id());
            }
        }

        Map<String, List<String>> adjacency = new HashMap<>();
        for (String id : nodeIds) {
            adjacency.put(id, List.of());
        }
        for (WorkflowEdge edge : definition.edges()) {
            if (!nodeIds.contains(edge.from()) || !nodeIds.contains(edge.to())) {
                throw new InvalidWorkflowException("edge references unknown node: " + edge);
            }
            adjacency.compute(edge.from(), (key, current) -> {
                var next = new java.util.ArrayList<>(current);
                next.add(edge.to());
                return next;
            });
        }

        detectCycles(adjacency);
        requireReachability(definition.nodes().get(0).id(), adjacency, nodeIds);
    }

    private void detectCycles(Map<String, List<String>> adjacency) {
        Set<String> visiting = new HashSet<>();
        Set<String> visited = new HashSet<>();
        for (String node : adjacency.keySet()) {
            visit(node, adjacency, visiting, visited);
        }
    }

    private void visit(String node, Map<String, List<String>> adjacency, Set<String> visiting, Set<String> visited) {
        if (visited.contains(node)) {
            return;
        }
        if (!visiting.add(node)) {
            throw new InvalidWorkflowException("cycle detected at node: " + node);
        }
        for (String next : adjacency.getOrDefault(node, List.of())) {
            visit(next, adjacency, visiting, visited);
        }
        visiting.remove(node);
        visited.add(node);
    }

    private void requireReachability(String start, Map<String, List<String>> adjacency, Set<String> nodeIds) {
        Set<String> reached = new HashSet<>();
        ArrayDeque<String> queue = new ArrayDeque<>();
        queue.add(start);
        while (!queue.isEmpty()) {
            String node = queue.removeFirst();
            if (reached.add(node)) {
                queue.addAll(adjacency.getOrDefault(node, List.of()));
            }
        }
        if (!reached.containsAll(nodeIds)) {
            Set<String> unreachable = new HashSet<>(nodeIds);
            unreachable.removeAll(reached);
            throw new InvalidWorkflowException("unreachable nodes: " + unreachable);
        }
    }
}
