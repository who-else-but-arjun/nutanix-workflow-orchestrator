package com.flowforge.engine.flow;

import com.flowforge.engine.registry.UnknownTaskTypeException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class WorkflowFlowRegistry {
    private final Map<String, WorkflowFlow> flowsById;

    public WorkflowFlowRegistry() {
        this.flowsById = Stream.of(
                "ahv_vm_provisioning_flow",
                "aos_lcm_rolling_upgrade_flow",
                "cleanup_failed_ahv_vm_flow",
                "cluster_upgrade_failure_flow",
                "flow_microsegmentation_rollout_flow",
                "ahv_vm_provisioning",
                "aos_lcm_rolling_upgrade",
                "cleanup_failed_ahv_vm",
                "cluster_upgrade_failure_demo",
                "flow_microsegmentation_conditional_rollout").map(DummyNutanixFlow::new)
                .collect(Collectors.toUnmodifiableMap(WorkflowFlow::flowId, Function.identity()));
    }

    public WorkflowFlow resolve(String flowId) {
        WorkflowFlow flow = flowsById.get(flowId);
        if (flow == null) {
            throw new UnknownTaskTypeException("Unknown workflow flow: " + flowId);
        }
        return flow;
    }
}
