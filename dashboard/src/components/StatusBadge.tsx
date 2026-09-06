import type { ExecutionState, NodeExecutionState } from '../types/flowforge';

type Status = ExecutionState | NodeExecutionState;

const labels: Record<string, string> = {
  SUCCEEDED: 'Succeeded',
  COMPENSATED: 'Compensated',
  COMPENSATING: 'Compensating',
  RUNNING: 'Running',
  PENDING: 'Pending',
  FAILED: 'Failed',
  COMPENSATION_FAILED: 'Recovery failed',
  SKIPPED: 'Skipped'
};

export function StatusBadge({ status }: { status: Status }) {
  return <span className={`status status-${status.toLowerCase()}`}>{labels[status] ?? status}</span>;
}
