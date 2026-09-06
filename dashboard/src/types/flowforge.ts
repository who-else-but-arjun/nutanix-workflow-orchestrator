export type ExecutionState =
  | 'PENDING'
  | 'RUNNING'
  | 'SUCCEEDED'
  | 'FAILED'
  | 'COMPENSATING'
  | 'COMPENSATED'
  | 'COMPENSATION_FAILED';

export type NodeExecutionState = 'PENDING' | 'RUNNING' | 'SUCCEEDED' | 'FAILED' | 'SKIPPED';

export interface WorkflowNode {
  id: string;
  type: string;
  config: Record<string, unknown>;
}

export interface WorkflowDefinition {
  workflowName: string;
  version: string;
  input: Record<string, unknown>;
  nodes: WorkflowNode[];
  edges: Array<{ from: string; to: string; condition?: string }>;
  onFailure?: { compensationFlow: string };
}

export interface ExecutionNodeRecord {
  nodeId: string;
  type: string;
  state: NodeExecutionState;
  startedAt?: string;
  finishedAt?: string;
  output: Record<string, unknown>;
  error?: string;
}

export interface ExecutionRecord {
  id: string;
  workflowName: string;
  version: string;
  state: ExecutionState;
  startedAt?: string;
  finishedAt?: string;
  input: Record<string, unknown>;
  nodes: ExecutionNodeRecord[];
  failureReason?: string;
  compensationExecutionId?: string;
  temporalWorkflowId?: string;
  temporalRunId?: string;
  temporalNamespace?: string;
  temporalTaskQueue?: string;
  durationMillis: number;
}
