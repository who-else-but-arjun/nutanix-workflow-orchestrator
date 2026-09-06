package com.flowforge.temporal;

import com.flowforge.domain.execution.ExecutionContext;
import com.flowforge.domain.execution.ExecutionState;
import com.flowforge.domain.execution.NodeExecutionState;
import com.flowforge.domain.workflow.WorkflowDefinition;
import com.flowforge.engine.port.WorkflowExecutionPort;
import com.flowforge.persistence.ExecutionNodeRecord;
import com.flowforge.persistence.ExecutionRecord;
import com.flowforge.persistence.InMemoryWorkflowStore;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
public class TemporalExecutionAdapter implements WorkflowExecutionPort {
        private final WorkflowClient client;
        private final InMemoryWorkflowStore store;
        private final String taskQueue;
        private final String namespace;

        public TemporalExecutionAdapter(
                        WorkflowClient client,
                        InMemoryWorkflowStore store,
                        @Value("${flowforge.temporal.task-queue}") String taskQueue,
                        @Value("${flowforge.temporal.namespace}") String namespace) {
                this.client = client;
                this.store = store;
                this.taskQueue = taskQueue;
                this.namespace = namespace;
        }

        @Override
        public ExecutionRecord submit(WorkflowDefinition definition, ExecutionContext context) {
                String executionId = "exec-" + UUID.randomUUID();
                ExecutionRecord created = new ExecutionRecord(
                                executionId,
                                definition.workflowName(),
                                definition.version(),
                                ExecutionState.PENDING,
                                Instant.now(),
                                null,
                                context.workflowInput(),
                                definition.nodes().stream()
                                                .map(node -> new ExecutionNodeRecord(node.id(), node.type(),
                                                                NodeExecutionState.PENDING, null, null, Map.of(), null))
                                                .toList(),
                                null,
                                null,
                                executionId,
                                null,
                                namespace,
                                taskQueue);
                store.saveExecution(created);

                FlowForgeWorkflow workflow = client.newWorkflowStub(FlowForgeWorkflow.class,
                                WorkflowOptions.newBuilder()
                                                .setTaskQueue(taskQueue)
                                                .setWorkflowId(executionId)
                                                .build());
                WorkflowExecution temporalExecution = WorkflowClient.start(workflow::run,
                                new TemporalWorkflowRequest(executionId, definition, context.workflowInput()));
                ExecutionRecord current = store.findExecution(executionId).orElse(created);
                ExecutionRecord started = new ExecutionRecord(
                                current.id(),
                                current.workflowName(),
                                current.version(),
                                current.state(),
                                current.startedAt(),
                                current.finishedAt(),
                                current.input(),
                                current.nodes(),
                                current.failureReason(),
                                current.compensationExecutionId(),
                                temporalExecution.getWorkflowId(),
                                temporalExecution.getRunId(),
                                namespace,
                                taskQueue);
                return store.saveExecution(started);
        }

        @Override
        public Optional<ExecutionRecord> getStatus(String executionId) {
                return store.findExecution(executionId);
        }
}
