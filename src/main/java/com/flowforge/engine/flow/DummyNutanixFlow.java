package com.flowforge.engine.flow;

import com.flowforge.domain.task.TaskRequest;
import com.flowforge.domain.task.TaskResult;

import java.util.LinkedHashMap;
import java.util.Map;

public class DummyNutanixFlow implements WorkflowFlow {
    private final String flowId;

    public DummyNutanixFlow(String flowId) {
        this.flowId = flowId;
    }

    @Override
    public String flowId() {
        return flowId;
    }

    @Override
    public TaskResult execute(TaskRequest request) {
        if (Boolean.TRUE.equals(request.config().get("simulateFailure"))) {
            return TaskResult.failure("simulated failure in " + flowId + "." + request.nodeId());
        }

        long startedAt = System.currentTimeMillis();
        performOperation(request.nodeId());
        Map<String, Object> output = new LinkedHashMap<>();
        output.put("flowId", flowId);
        output.put("operation", request.nodeId());
        output.put("status", "completed");
        output.put("durationMs", System.currentTimeMillis() - startedAt);
        return TaskResult.success(output);
    }

    private void performOperation(String nodeId) {
        int durationMs = switch (flowId) {
            case "ahv_vm_provisioning_flow" -> switch (nodeId) {
                case "validate_blueprint" -> 650;
                case "reserve_ipam_address" -> 800;
                case "clone_ahv_image" -> 1200;
                case "attach_flow_categories" -> 700;
                case "power_on_vm" -> 900;
                case "prism_health_check" -> 750;
                default -> 500;
            };
            case "aos_lcm_rolling_upgrade_flow" -> switch (nodeId) {
                case "inventory_cluster" -> 800;
                case "run_ncc_prechecks" -> 1100;
                case "stage_lcm_bundle" -> 1400;
                case "snapshot_prism_central" -> 1200;
                case "upgrade_batch_one", "upgrade_batch_two" -> 1800;
                case "verify_storage_rebalance" -> 1000;
                case "final_ncc_check" -> 1200;
                default -> 500;
            };
            case "cleanup_failed_ahv_vm_flow" -> switch (nodeId) {
                case "release_ipam_reservation" -> 650;
                case "detach_flow_categories" -> 700;
                case "delete_partial_vm" -> 900;
                case "notify_sre_channel" -> 450;
                default -> 500;
            };
            case "cluster_upgrade_failure_flow" -> switch (nodeId) {
                case "ncc_precheck" -> 600;
                case "snapshot_prism_config" -> 750;
                case "evacuate_user_vms" -> 900;
                case "upgrade_cvm" -> 1100;
                case "restore_host_admission" -> 650;
                default -> 500;
            };
            case "flow_microsegmentation_rollout_flow" -> switch (nodeId) {
                case "discover_app_topology" -> 850;
                case "assess_policy_risk" -> 700;
                case "generate_monitor_policy" -> 900;
                case "send_security_review" -> 600;
                case "enforce_policy" -> 900;
                case "validate_allowed_flows" -> 750;
                default -> 500;
            };
            default -> 500;
        };
        sleep(durationMs);
    }

    private void sleep(int durationMs) {
        try {
            Thread.sleep(durationMs);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }
}
