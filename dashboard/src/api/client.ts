import type { ExecutionRecord, WorkflowDefinition } from '../types/flowforge';

const jsonHeaders = { 'Content-Type': 'application/json' };

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(path, init);
  if (!response.ok) {
    const body = await response.json().catch(() => ({}));
    throw new Error(body.error ?? `Request failed: ${response.status}`);
  }
  return response.json() as Promise<T>;
}

export const api = {
  workflows: () => request<WorkflowDefinition[]>('/api/workflows'),
  executions: () => request<ExecutionRecord[]>('/api/executions'),
  execution: (id: string) => request<ExecutionRecord>(`/api/executions/${id}`),
  execute: (workflowName: string, input: Record<string, unknown> = {}) =>
    request<ExecutionRecord>(`/api/workflows/${workflowName}/execute`, {
      method: 'POST',
      headers: jsonHeaders,
      body: JSON.stringify({ input })
    })
};
