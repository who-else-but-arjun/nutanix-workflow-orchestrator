import { useEffect, useMemo, useState } from 'react';
import { api } from './api/client';
import { ExecutionDetail } from './pages/ExecutionDetail';
import { WorkflowList } from './pages/WorkflowList';
import type { ExecutionRecord, WorkflowDefinition } from './types/flowforge';

export default function App() {
  const [workflows, setWorkflows] = useState<WorkflowDefinition[]>([]);
  const [executions, setExecutions] = useState<ExecutionRecord[]>([]);
  const [selectedExecutionId, setSelectedExecutionId] = useState<string>();
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
  const [error, setError] = useState<string>();

  const selectedExecution = useMemo(
    () => executions.find((execution) => execution.id === selectedExecutionId) ?? executions[0],
    [executions, selectedExecutionId]
  );
  const selectedWorkflow = useMemo(
    () => workflows.find((workflow) => workflow.workflowName === selectedExecution?.workflowName),
    [workflows, selectedExecution?.workflowName]
  );

  async function refresh() {
    try {
      const [workflowList, executionList] = await Promise.all([api.workflows(), api.executions()]);
      setWorkflows(workflowList);
      setExecutions(executionList);
      setError(undefined);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unable to load FlowForge state');
    }
  }

  async function execute(workflowName: string) {
    try {
      const execution = await api.execute(workflowName);
      setSelectedExecutionId(execution.id);
      setExecutions((current) => [execution, ...current.filter((item) => item.id !== execution.id)]);
      await refresh();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unable to start workflow');
    }
  }

  useEffect(() => {
    refresh();
    const timer = window.setInterval(refresh, 3000);
    return () => window.clearInterval(timer);
  }, []);

  return (
    <div className={`app-shell ${sidebarCollapsed ? 'sidebar-collapsed' : ''}`}>
      <WorkflowList
        workflows={workflows}
        executions={executions}
        collapsed={sidebarCollapsed}
        onToggleCollapsed={() => setSidebarCollapsed((collapsed) => !collapsed)}
        onExecute={execute}
        onRefresh={refresh}
      />
      <ExecutionDetail execution={selectedExecution} workflow={selectedWorkflow} executions={executions} onSelectExecution={setSelectedExecutionId} />
      {error && <div className="toast">{error}</div>}
    </div>
  );
}
