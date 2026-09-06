import { CheckCircle2, Circle, Loader2, XCircle } from 'lucide-react';
import type { ExecutionNodeRecord } from '../types/flowforge';
import { StatusBadge } from './StatusBadge';

function iconFor(state: ExecutionNodeRecord['state']) {
  if (state === 'SUCCEEDED') return <CheckCircle2 size={18} />;
  if (state === 'FAILED') return <XCircle size={18} />;
  if (state === 'RUNNING') return <Loader2 className="spin" size={18} />;
  return <Circle size={18} />;
}

export function ExecutionTimeline({
  nodes,
  selectedNodeId,
  onSelect
}: {
  nodes: ExecutionNodeRecord[];
  selectedNodeId?: string;
  onSelect: (node: ExecutionNodeRecord) => void;
}) {
  return (
    <div className="timeline" aria-label="Execution timeline">
      {nodes.map((node, index) => (
        <button
          className={`timeline-row ${selectedNodeId === node.nodeId ? 'active' : ''}`}
          key={node.nodeId}
          onClick={() => onSelect(node)}
        >
          <span className={`timeline-icon state-${node.state.toLowerCase()}`}>{iconFor(node.state)}</span>
          {index < nodes.length - 1 && <span className="timeline-line" />}
          <span className="timeline-main">
            <strong>{node.nodeId.replaceAll('_', ' ')}</strong>
            <small>{node.type.toUpperCase()}</small>
          </span>
          <StatusBadge status={node.state} />
        </button>
      ))}
    </div>
  );
}
