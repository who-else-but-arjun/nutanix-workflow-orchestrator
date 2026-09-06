import { PanelLeftClose, PanelLeftOpen, Play, RefreshCw } from 'lucide-react';
import type { ExecutionRecord, WorkflowDefinition } from '../types/flowforge';

function countByState(executions: ExecutionRecord[], state: ExecutionRecord['state']) {
  return executions.filter((execution) => execution.state === state).length;
}

export function WorkflowList({
  workflows,
  executions,
  collapsed,
  onToggleCollapsed,
  onExecute,
  onRefresh
}: {
  workflows: WorkflowDefinition[];
  executions: ExecutionRecord[];
  collapsed: boolean;
  onToggleCollapsed: () => void;
  onExecute: (workflowName: string) => void;
  onRefresh: () => void;
}) {
  const metrics = [
    ['Running', countByState(executions, 'RUNNING') + countByState(executions, 'COMPENSATING')],
    ['Completed', countByState(executions, 'SUCCEEDED') + countByState(executions, 'COMPENSATED')],
    ['Failed', countByState(executions, 'FAILED') + countByState(executions, 'COMPENSATION_FAILED')],
    ['Definitions', workflows.length]
  ] as const;

  return (
    <aside className={`left-pane ${collapsed ? 'collapsed' : ''}`}>
      <div className="brand">
        <div className="brand-mark">
          <img src="/flowforge-logo.png" alt="FlowForge" />
        </div>
        <div className="brand-copy">
          <h1>FlowForge</h1>
          <p>Workflow Operations</p>
        </div>
        <button className="icon-button sidebar-toggle" onClick={onToggleCollapsed} aria-label={collapsed ? 'Expand sidebar' : 'Collapse sidebar'}>
          {collapsed ? <PanelLeftOpen size={16} /> : <PanelLeftClose size={16} />}
        </button>
      </div>

      <section className="metric-grid" aria-label="Execution summary">
        {metrics.map(([label, value]) => (
          <div className="metric" key={label}>
            <span>{label}</span>
            <strong>{value}</strong>
          </div>
        ))}
      </section>

      <section className="panel">
        <div className="panel-head">
          <h2>Workflow Definitions</h2>
          <button className="icon-button" onClick={onRefresh} aria-label="Refresh">
            <RefreshCw size={16} />
          </button>
        </div>
        <div className="definition-list">
          {workflows.map((workflow) => (
            <div className="definition-row" key={workflow.workflowName}>
              <div className="definition-copy">
                <strong>{workflow.workflowName.replaceAll('_', ' ')}</strong>
                <small>{workflow.nodes.length} nodes · v{workflow.version}</small>
              </div>
              <button className="icon-button primary" onClick={() => onExecute(workflow.workflowName)} aria-label={`Run ${workflow.workflowName}`}>
                <Play size={15} />
              </button>
            </div>
          ))}
        </div>
      </section>
    </aside>
  );
}
