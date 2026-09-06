package com.flowforge.config;

import com.flowforge.api.dto.FailurePolicyRequest;
import com.flowforge.api.dto.NodeRequest;
import com.flowforge.api.dto.WorkflowDefinitionRequest;
import com.flowforge.api.dto.WorkflowMapper;
import com.flowforge.service.WorkflowService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Component
public class DataSeeder implements CommandLineRunner {
        private final WorkflowService workflowService;
        private final ObjectMapper objectMapper;
        private final Path workflowDirectory;

        public DataSeeder(
                        WorkflowService workflowService,
                        ObjectMapper objectMapper,
                        @Value("${flowforge.workflow-directory:workflows}") String workflowDirectory) {
                this.workflowService = workflowService;
                this.objectMapper = objectMapper;
                this.workflowDirectory = Path.of(workflowDirectory);
        }

        @Override
        public void run(String... args) {
                if (loadWorkflowFiles()) {
                        return;
                }

                register("cleanup_failed_ahv_vm", Map.of("vmName", "ntnx-web-01", "cluster", "prod-ahv-a"), List.of(
                                new NodeRequest("release_ipam_reservation", "rest",
                                                Map.of("method", "POST", "url", "/prism/v4/ipam/release", "delayMs",
                                                                650)),
                                new NodeRequest("detach_flow_categories", "rest",
                                                Map.of("method", "POST", "url", "/prism/v4/flow/categories/detach",
                                                                "delayMs", 700)),
                                new NodeRequest("delete_partial_vm", "shell",
                                                Map.of("command", "acli vm.delete ntnx-web-01", "delayMs", 900)),
                                new NodeRequest("notify_sre_channel", "rest", Map
                                                .of("method", "POST", "url", "/prism-central/alerts", "delayMs", 450))),
                                List.of(
                                                List.of("release_ipam_reservation", "detach_flow_categories"),
                                                List.of("detach_flow_categories", "delete_partial_vm"),
                                                List.of("delete_partial_vm", "notify_sre_channel")),
                                null);

                register("ahv_vm_provisioning",
                                Map.of("vmName", "ntnx-web-01", "cluster", "prod-ahv-a", "cpu", 4, "memoryMb", 8192),
                                List.of(
                                                new NodeRequest("validate_blueprint", "shell",
                                                                Map.of("command", "calm blueprint validate web-tier",
                                                                                "delayMs", 650, "output",
                                                                                Map.of("blueprint", "web-tier"))),
                                                new NodeRequest("reserve_ipam_address", "rest", Map.of("method", "POST",
                                                                "url", "/prism/v4/ipam/reservations", "delayMs", 800,
                                                                "output", Map.of("ip", "10.42.8.51"))),
                                                new NodeRequest("clone_ahv_image", "shell",
                                                                Map.of("command", "acli image.clone ubuntu-golden",
                                                                                "delayMs", 1200)),
                                                new NodeRequest("attach_flow_categories", "rest",
                                                                Map.of("method", "POST", "url",
                                                                                "/prism/v4/flow/categories", "delayMs",
                                                                                700, "output",
                                                                                Map.of("app", "web", "environment",
                                                                                                "prod"))),
                                                new NodeRequest("power_on_vm", "shell",
                                                                Map.of("command", "acli vm.on ntnx-web-01", "delayMs",
                                                                                900)),
                                                new NodeRequest("prism_health_check", "rest", Map.of(
                                                                "method", "GET", "url",
                                                                "/prism/v4/vms/ntnx-web-01/health", "delayMs", 750))),
                                List.of(
                                                List.of("validate_blueprint", "reserve_ipam_address"),
                                                List.of("validate_blueprint", "clone_ahv_image"),
                                                List.of("reserve_ipam_address", "attach_flow_categories"),
                                                List.of("clone_ahv_image", "power_on_vm"),
                                                List.of("attach_flow_categories", "prism_health_check"),
                                                List.of("power_on_vm", "prism_health_check")),
                                new FailurePolicyRequest("cleanup_failed_ahv_vm"));

                register("cluster_upgrade_failure_demo",
                                Map.of("cluster", "edge-lab-a", "node", "node-02", "targetAos", "7.0.1"), List.of(
                                                new NodeRequest("ncc_precheck", "rest", Map.of("method", "GET", "url",
                                                                "/prism/v4/clusters/edge-lab-a/ncc", "delayMs", 600)),
                                                new NodeRequest("snapshot_prism_config", "shell", Map.of("command",
                                                                "ncli cluster get-params > prism-backup.json",
                                                                "delayMs", 750)),
                                                new NodeRequest("evacuate_user_vms", "rest",
                                                                Map.of("method", "POST", "url",
                                                                                "/prism/v4/hosts/node-02/evacuate",
                                                                                "delayMs", 900)),
                                                new NodeRequest("upgrade_cvm", "shell", Map.of("command",
                                                                "lcm upgrade cvm node-02 --target 7.0.1", "delayMs",
                                                                1100, "simulateFailure", true)),
                                                new NodeRequest("restore_host_admission", "rest", Map.of(
                                                                "method", "POST", "url",
                                                                "/prism/v4/hosts/node-02/restore", "delayMs", 650))),
                                List.of(
                                                List.of("ncc_precheck", "snapshot_prism_config"),
                                                List.of("snapshot_prism_config", "evacuate_user_vms"),
                                                List.of("evacuate_user_vms", "upgrade_cvm"),
                                                List.of("upgrade_cvm", "restore_host_admission")),
                                new FailurePolicyRequest("cleanup_failed_ahv_vm"));

                register("aos_lcm_rolling_upgrade",
                                Map.of("cluster", "prod-ahv-a", "targetAos", "7.0.1", "batchSize", 1), List.of(
                                                new NodeRequest("inventory_cluster", "rest",
                                                                Map.of("method", "GET", "url",
                                                                                "/prism/v4/clusters/prod-ahv-a",
                                                                                "delayMs", 2500)),
                                                new NodeRequest("run_ncc_prechecks", "shell",
                                                                Map.of("command", "ncc health_checks run_all",
                                                                                "delayMs", 3500, "output",
                                                                                Map.of("nccStatus", "pass"))),
                                                new NodeRequest("stage_lcm_bundle", "rest",
                                                                Map.of("method", "POST", "url", "/prism/v4/lcm/bundles",
                                                                                "delayMs", 4500)),
                                                new NodeRequest("snapshot_prism_central", "shell",
                                                                Map.of("command",
                                                                                "pc-backup create --cluster prod-ahv-a",
                                                                                "delayMs", 5000)),
                                                new NodeRequest("upgrade_batch_one", "shell",
                                                                Map.of("command", "lcm upgrade --batch 1", "delayMs",
                                                                                6500)),
                                                new NodeRequest("verify_storage_rebalance", "rest",
                                                                Map.of("method", "GET", "url",
                                                                                "/prism/v4/storage/rebalance",
                                                                                "delayMs", 3500)),
                                                new NodeRequest("upgrade_batch_two", "shell",
                                                                Map.of("command", "lcm upgrade --batch 2", "delayMs",
                                                                                6500)),
                                                new NodeRequest("final_ncc_check", "shell", Map.of("command",
                                                                "ncc health_checks run_all --post-upgrade", "delayMs",
                                                                4000))),
                                List.of(
                                                List.of("inventory_cluster", "run_ncc_prechecks"),
                                                List.of("run_ncc_prechecks", "stage_lcm_bundle"),
                                                List.of("run_ncc_prechecks", "snapshot_prism_central"),
                                                List.of("stage_lcm_bundle", "upgrade_batch_one"),
                                                List.of("snapshot_prism_central", "upgrade_batch_one"),
                                                List.of("upgrade_batch_one", "verify_storage_rebalance"),
                                                List.of("verify_storage_rebalance", "upgrade_batch_two"),
                                                List.of("upgrade_batch_two", "final_ncc_check")),
                                null);

                register("flow_microsegmentation_conditional_rollout",
                                Map.of("application", "payments-api", "policyMode", "monitor"), List.of(
                                                new NodeRequest("discover_app_topology", "rest", Map.of("method", "GET",
                                                                "url", "/prism/v4/flow/apps/payments-api/topology",
                                                                "delayMs", 850)),
                                                new NodeRequest("assess_policy_risk", "shell",
                                                                Map.of("command", "flow policy assess payments-api",
                                                                                "delayMs", 700, "output",
                                                                                Map.of("policyMode", "monitor"))),
                                                new NodeRequest("generate_monitor_policy", "rest",
                                                                Map.of("method", "POST", "url",
                                                                                "/prism/v4/flow/policies/monitor",
                                                                                "delayMs", 900)),
                                                new NodeRequest("send_security_review", "rest", Map.of("method", "POST",
                                                                "url", "/servicenow/security-review", "delayMs", 600)),
                                                new NodeRequest("enforce_policy", "rest",
                                                                Map.of("method", "POST", "url",
                                                                                "/prism/v4/flow/policies/enforce",
                                                                                "delayMs", 900)),
                                                new NodeRequest("validate_allowed_flows", "shell", Map.of("command",
                                                                "flow trace payments-api --expected-only", "delayMs",
                                                                750))),
                                List.of(
                                                List.of("discover_app_topology", "assess_policy_risk"),
                                                List.of("assess_policy_risk", "generate_monitor_policy",
                                                                "input.policyMode == monitor"),
                                                List.of("assess_policy_risk", "send_security_review",
                                                                "input.policyMode == enforce"),
                                                List.of("send_security_review", "enforce_policy"),
                                                List.of("enforce_policy", "validate_allowed_flows")),
                                null);
        }

        private boolean loadWorkflowFiles() {
                if (!Files.isDirectory(workflowDirectory)) {
                        return false;
                }

                try (var files = Files.list(workflowDirectory)) {
                        List<Path> workflowFiles = files
                                        .filter(path -> path.getFileName().toString().endsWith(".json"))
                                        .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                                        .toList();
                        for (Path workflowFile : workflowFiles) {
                                WorkflowDefinitionRequest request = objectMapper.readValue(workflowFile.toFile(),
                                                WorkflowDefinitionRequest.class);
                                workflowService.register(WorkflowMapper.toDomain(request));
                        }
                        return !workflowFiles.isEmpty();
                } catch (IOException exception) {
                        throw new IllegalStateException("Unable to load workflow definitions from " + workflowDirectory,
                                        exception);
                }
        }

        private void register(String name, Map<String, Object> input, List<NodeRequest> nodes, List<List<String>> edges,
                        FailurePolicyRequest failurePolicy) {
                workflowService.register(WorkflowMapper.toDomain(new WorkflowDefinitionRequest(
                                name,
                                "1.0",
                                input,
                                nodes,
                                edges,
                                failurePolicy)));
        }
}
