package com.flowforge.domain;

import com.flowforge.domain.workflow.DagValidator;
import com.flowforge.domain.workflow.InvalidWorkflowException;
import com.flowforge.domain.workflow.WorkflowDefinition;
import com.flowforge.domain.workflow.WorkflowEdge;
import com.flowforge.domain.workflow.WorkflowNode;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DagValidatorTest {
    private final DagValidator validator = new DagValidator();

    @Test
    void acceptsAValidDag() {
        WorkflowDefinition definition = new WorkflowDefinition("demo", "1.0", Map.of(),
                List.of(new WorkflowNode("a", "shell", Map.of()), new WorkflowNode("b", "rest", Map.of())),
                List.of(new WorkflowEdge("a", "b")),
                null);

        assertDoesNotThrow(() -> validator.validate(definition));
    }

    @Test
    void rejectsCycles() {
        WorkflowDefinition definition = new WorkflowDefinition("demo", "1.0", Map.of(),
                List.of(new WorkflowNode("a", "shell", Map.of()), new WorkflowNode("b", "rest", Map.of())),
                List.of(new WorkflowEdge("a", "b"), new WorkflowEdge("b", "a")),
                null);

        assertThrows(InvalidWorkflowException.class, () -> validator.validate(definition));
    }
}
