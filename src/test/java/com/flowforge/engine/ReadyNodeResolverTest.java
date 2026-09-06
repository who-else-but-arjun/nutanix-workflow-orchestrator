package com.flowforge.engine;

import com.flowforge.domain.execution.NodeExecutionState;
import com.flowforge.domain.workflow.WorkflowDefinition;
import com.flowforge.domain.workflow.WorkflowEdge;
import com.flowforge.domain.workflow.WorkflowNode;
import com.flowforge.engine.scheduler.ReadyNodeResolver;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ReadyNodeResolverTest {
        @Test
        void returnsNodesWhoseDependenciesSucceeded() {
                WorkflowDefinition definition = new WorkflowDefinition("demo", "1.0", Map.of(),
                                List.of(
                                                new WorkflowNode("a", "shell", Map.of()),
                                                new WorkflowNode("b", "rest", Map.of()),
                                                new WorkflowNode("c", "rest", Map.of())),
                                List.of(new WorkflowEdge("a", "b"), new WorkflowEdge("a", "c")),
                                null);
                Map<String, NodeExecutionState> states = new LinkedHashMap<>();
                states.put("a", NodeExecutionState.SUCCEEDED);
                states.put("b", NodeExecutionState.PENDING);
                states.put("c", NodeExecutionState.PENDING);

                assertThat(new ReadyNodeResolver().resolve(definition, states))
                                .extracting(WorkflowNode::id)
                                .containsExactly("b", "c");
        }

        @Test
        void resolvesOnlyBranchesWhoseConditionsMatch() {
                WorkflowDefinition definition = new WorkflowDefinition("demo", "1.0", Map.of("policyMode", "monitor"),
                                List.of(
                                                new WorkflowNode("assess", "shell", Map.of()),
                                                new WorkflowNode("monitor", "rest", Map.of()),
                                                new WorkflowNode("enforce", "rest", Map.of())),
                                List.of(
                                                new WorkflowEdge("assess", "monitor", "input.policyMode == monitor"),
                                                new WorkflowEdge("assess", "enforce", "input.policyMode == enforce")),
                                null);
                Map<String, NodeExecutionState> states = new LinkedHashMap<>();
                states.put("assess", NodeExecutionState.SUCCEEDED);
                states.put("monitor", NodeExecutionState.PENDING);
                states.put("enforce", NodeExecutionState.PENDING);

                assertThat(new ReadyNodeResolver().resolve(definition, states, definition.input(), Map.of()))
                                .extracting(WorkflowNode::id)
                                .containsExactly("monitor");
                assertThat(new ReadyNodeResolver().resolveSkipped(definition, states, definition.input(), Map.of()))
                                .extracting(WorkflowNode::id)
                                .containsExactly("enforce");
        }

        @Test
        void skipsNodesAfterFailedDependencies() {
                WorkflowDefinition definition = new WorkflowDefinition("demo", "1.0", Map.of(),
                                List.of(
                                                new WorkflowNode("failed", "shell", Map.of()),
                                                new WorkflowNode("downstream", "rest", Map.of())),
                                List.of(new WorkflowEdge("failed", "downstream")),
                                null);
                Map<String, NodeExecutionState> states = new LinkedHashMap<>();
                states.put("failed", NodeExecutionState.FAILED);
                states.put("downstream", NodeExecutionState.PENDING);

                assertThat(new ReadyNodeResolver().resolveSkipped(definition, states, Map.of(), Map.of()))
                                .extracting(WorkflowNode::id)
                                .containsExactly("downstream");
        }
}
