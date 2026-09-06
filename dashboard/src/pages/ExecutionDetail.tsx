import { Braces, ExternalLink, GitBranch, Network, TimerReset } from 'lucide-react';
import { useEffect, useMemo, useState } from 'react';
import { DagVisualizer } from '../components/DagVisualizer';
import { ExecutionTimeline } from '../components/ExecutionTimeline';
import { FailureNarrative } from '../components/FailureNarrative';
import { JsonBlock } from '../components/JsonBlock';
import { StatusBadge } from '../components/StatusBadge';
import type { ExecutionNodeRecord, ExecutionRecord, WorkflowDefinition } from '../types/flowforge';

function formatTime(value?: string) {
  return value ? new Intl.DateTimeFormat(undefined, { hour: '2-digit', minute: '2-digit', second: '2-digit' }).format(new Date(value)) : 'Not started';
}

function formatDateTime(value?: string) {
  return value
    ? new Intl.DateTimeFormat(undefined, { month: 'short', day: '2-digit', hour: '2-digit', minute: '2-digit', second: '2-digit' }).format(new Date(value))
    : 'Not started';
}

function durationText(execution: ExecutionRecord) {
  if (!execution.startedAt) return '0s';
  const duration = typeof execution.durationMillis === 'number'
    ? execution.durationMillis
    : (execution.finishedAt ? new Date(execution.finishedAt).getTime() : Date.now()) - new Date(execution.startedAt).getTime();
  const totalSeconds = Math.max(0, Math.round(duration / 1000));
  const minutes = Math.floor(totalSeconds / 60);
  const seconds = totalSeconds % 60;
  return minutes > 0 ? `${minutes}m ${seconds}s` : `${seconds}s`;
}

function progress(execution: ExecutionRecord) {
  if (execution.nodes.length === 0) return 0;
  const done = execution.nodes.filter((node) => node.state === 'SUCCEEDED' || node.state === 'FAILED' || node.state === 'SKIPPED').length;
  return Math.round((done / execution.nodes.length) * 100);
}

function nodeProgress(execution: ExecutionRecord) {
  const finished = execution.nodes.filter((node) => node.state === 'SUCCEEDED' || node.state === 'FAILED' || node.state === 'SKIPPED').length;
  return `${finished}/${execution.nodes.length}`;
}

function failedNodeName(execution: ExecutionRecord) {
  return execution.nodes.find((node) => node.state === 'FAILED')?.nodeId.replaceAll('_', ' ') ?? 'None';
}

function temporalExecutionUrl(execution: ExecutionRecord) {
  const base = (import.meta.env.VITE_TEMPORAL_UI_URL ?? 'http://127.0.0.1:8088').replace(/\/$/, '');
  const namespace = encodeURIComponent(execution.temporalNamespace ?? 'default');
  const workflowId = encodeURIComponent(execution.temporalWorkflowId ?? execution.id);
  const runId = execution.temporalRunId ? `/${encodeURIComponent(execution.temporalRunId)}` : '';
  return `${base}/namespaces/${namespace}/workflows/${workflowId}${runId}/history`;
}

function ExecutionsTable({
  executions,
  selectedId,
  onSelectExecution
}: {
  executions: ExecutionRecord[];
  selectedId?: string;
  onSelectExecution: (id: string) => void;
}) {
  return (
    <section className="executions-table-panel">
      <div className="panel-head compact-head">
        <div>
          <h2>Executions</h2>
          <p>{executions.length} runs tracked</p>
        </div>
      </div>
      <div className="table-scroll">
        <table className="executions-table">
          <thead>
            <tr>
              <th>Workflow</th>
              <th>Status</th>
              <th>Execution ID</th>
              <th>Version</th>
              <th>Started</th>
              <th>Finished</th>
              <th>Duration</th>
              <th>Nodes</th>
              <th>Failed Node</th>
              <th>Compensation</th>
              <th>Temporal</th>
            </tr>
          </thead>
          <tbody>
            {executions.length === 0 && (
              <tr>
                <td colSpan={11} className="table-empty">No executions yet.</td>
              </tr>
            )}
            {executions.map((item) => (
              <tr className={selectedId === item.id ? 'selected' : ''} key={item.id}>
                <td>
                  <button className="table-link" onClick={() => onSelectExecution(item.id)} type="button">
                    {item.workflowName.replaceAll('_', ' ')}
                  </button>
                </td>
                <td><StatusBadge status={item.state} /></td>
                <td className="mono-cell">{item.id}</td>
                <td>v{item.version}</td>
                <td>{formatDateTime(item.startedAt)}</td>
                <td>{formatDateTime(item.finishedAt)}</td>
                <td>{durationText(item)}</td>
                <td>{nodeProgress(item)}</td>
                <td>{failedNodeName(item)}</td>
                <td className="mono-cell">{item.compensationExecutionId ?? 'None'}</td>
                <td>
                  <a className="temporal-link" href={temporalExecutionUrl(item)} target="_blank" rel="noreferrer" title="Open in Temporal UI">
                    <ExternalLink size={14} />
                    <span>Open</span>
                  </a>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}

export function ExecutionDetail({
  execution,
  workflow,
  executions,
  onSelectExecution
}: {
  execution?: ExecutionRecord;
  workflow?: WorkflowDefinition;
  executions: ExecutionRecord[];
  onSelectExecution: (id: string) => void;
}) {
  const [selectedNode, setSelectedNode] = useState<ExecutionNodeRecord | undefined>();
  useEffect(() => setSelectedNode(undefined), [execution?.id]);
  const compensation = useMemo(
    () => executions.find((item) => item.id === execution?.compensationExecutionId),
    [execution?.compensationExecutionId, executions]
  );
  const activeNode = useMemo(() => selectedNode ?? execution?.nodes.find((node) => node.state === 'RUNNING') ?? execution?.nodes[0], [execution, selectedNode]);
  const failedNode = execution?.nodes.find((node) => node.state === 'FAILED');

  if (!execution) {
    return (
      <main className="detail">
        <ExecutionsTable executions={executions} selectedId={undefined} onSelectExecution={onSelectExecution} />
        <section className="empty-state">
          <GitBranch size={42} />
          <h2>No execution selected</h2>
          <p>Run a workflow to watch the DAG move through node states.</p>
        </section>
      </main>
    );
  }

  return (
    <main className="detail">
      <ExecutionsTable executions={executions} selectedId={execution.id} onSelectExecution={onSelectExecution} />

      <section className="detail-hero">
        <div>
          <span className="eyebrow">Execution</span>
          <h2>{execution.workflowName.replaceAll('_', ' ')}</h2>
          <p>{execution.id}</p>
        </div>
        <StatusBadge status={execution.state} />
      </section>

      <section className="summary-strip">
        <div>
          <TimerReset size={17} />
          <span>Started</span>
          <strong>{formatTime(execution.startedAt)}</strong>
        </div>
        <div>
          <GitBranch size={17} />
          <span>Progress</span>
          <strong>{progress(execution)}%</strong>
        </div>
        <div>
          <Braces size={17} />
          <span>Recovery</span>
          <strong>{compensation?.state ?? execution.compensationExecutionId ?? 'None'}</strong>
        </div>
        <div>
          <Network size={17} />
          <span>Failed Node</span>
          <strong>{failedNode?.nodeId.replaceAll('_', ' ') ?? 'None'}</strong>
        </div>
      </section>

      <div className="progress-track">
        <span style={{ width: `${progress(execution)}%` }} />
      </div>

      <FailureNarrative execution={execution} compensation={compensation} onOpenCompensation={onSelectExecution} />

      <DagVisualizer execution={execution} workflow={workflow} selectedNodeId={activeNode?.nodeId} onSelect={setSelectedNode} />

      <section className="workspace">
        <div className="timeline-panel">
          <div className="panel-head">
            <h2>Activity</h2>
          </div>
          <ExecutionTimeline nodes={execution.nodes} selectedNodeId={activeNode?.nodeId} onSelect={setSelectedNode} />
          {execution.failureReason && <p className="failure-note">{execution.failureReason}</p>}
        </div>

        <div className="inspector">
          <div className="panel-head">
            <h2>Node details</h2>
          </div>
          {activeNode && (
            <>
              <div className="inspector-title">
                <div>
                  <strong>{activeNode.nodeId.replaceAll('_', ' ')}</strong>
                  <small>{activeNode.type.toUpperCase()} executor</small>
                </div>
                <StatusBadge status={activeNode.state} />
              </div>
              <dl className="facts">
                <div><dt>Started</dt><dd>{formatTime(activeNode.startedAt)}</dd></div>
                <div><dt>Finished</dt><dd>{formatTime(activeNode.finishedAt)}</dd></div>
              </dl>
              <JsonBlock title="Workflow Input" value={execution.input} />
              <JsonBlock title="Node Output" value={activeNode.output} />
              {activeNode.error && <p className="failure-note">{activeNode.error}</p>}
            </>
          )}
        </div>
      </section>
    </main>
  );
}
