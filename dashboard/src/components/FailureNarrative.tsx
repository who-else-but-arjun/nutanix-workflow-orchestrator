import { AlertCircle, CheckCircle2, RotateCcw } from 'lucide-react';
import type { ExecutionRecord } from '../types/flowforge';
import { StatusBadge } from './StatusBadge';

export function FailureNarrative({
  execution,
  compensation,
  onOpenCompensation
}: {
  execution: ExecutionRecord;
  compensation?: ExecutionRecord;
  onOpenCompensation?: (id: string) => void;
}) {
  if (!execution.failureReason) {
    return (
      <section className="incident-strip success">
        <CheckCircle2 size={18} />
        <div>
          <strong>No failure detected</strong>
          <span>All completed nodes returned successful task results.</span>
        </div>
      </section>
    );
  }

  return (
    <section className="incident-strip failed">
      <AlertCircle size={18} />
      <div>
        <strong>Failure handled</strong>
        <span>{execution.failureReason}</span>
      </div>
      {compensation && (
        <button className="recovery-chip" onClick={() => onOpenCompensation?.(compensation.id)}>
          <RotateCcw size={15} />
          <span>{compensation.workflowName.replaceAll('_', ' ')}</span>
          <StatusBadge status={compensation.state} />
        </button>
      )}
    </section>
  );
}
