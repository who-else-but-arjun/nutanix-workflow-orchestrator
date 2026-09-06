import { CheckCircle2, Circle, Loader2, MinusCircle, XCircle } from 'lucide-react';
import type { ExecutionNodeRecord, ExecutionRecord, WorkflowDefinition } from '../types/flowforge';
import { StatusBadge } from './StatusBadge';

const NODE_WIDTH = 184;
const NODE_HEIGHT = 92;
const COLUMN_GAP = 92;
const ROW_GAP = 30;
const PADDING = 28;

type Edge = { from: string; to: string };
type PositionedNode = { node: ExecutionNodeRecord; x: number; y: number };

function stateIcon(state: ExecutionNodeRecord['state']) {
  if (state === 'SUCCEEDED') return <CheckCircle2 size={17} />;
  if (state === 'FAILED') return <XCircle size={17} />;
  if (state === 'RUNNING') return <Loader2 className="spin" size={17} />;
  if (state === 'SKIPPED') return <MinusCircle size={17} />;
  return <Circle size={17} />;
}

function elapsed(node: ExecutionNodeRecord) {
  if (node.state === 'SKIPPED') return 'Skipped';
  if (!node.startedAt) return 'Waiting';
  const end = node.finishedAt ? new Date(node.finishedAt).getTime() : Date.now();
  return `${Math.round(Math.max(0, end - new Date(node.startedAt).getTime()) / 1000)}s`;
}

function buildEdges(nodes: ExecutionNodeRecord[], workflow?: WorkflowDefinition): Edge[] {
  const known = new Set(nodes.map((node) => node.nodeId));
  const edges = (workflow?.edges ?? []).filter((edge) => known.has(edge.from) && known.has(edge.to));
  if (edges.length > 0 || nodes.length < 2) return edges;
  return nodes.slice(1).map((node, index) => ({ from: nodes[index].nodeId, to: node.nodeId }));
}

function layoutGraph(nodes: ExecutionNodeRecord[], edges: Edge[]) {
  const order = new Map(nodes.map((node, index) => [node.nodeId, index]));
  const parents = new Map(nodes.map((node) => [node.nodeId, [] as string[]]));
  edges.forEach((edge) => parents.get(edge.to)?.push(edge.from));

  const levels = new Map<string, number>();
  const unresolved = new Set(nodes.map((node) => node.nodeId));
  while (unresolved.size > 0) {
    const ready = [...unresolved]
      .filter((id) => (parents.get(id) ?? []).every((parent) => levels.has(parent)))
      .sort((left, right) => (order.get(left) ?? 0) - (order.get(right) ?? 0));
    const current = ready.length > 0 ? ready : [[...unresolved].sort((left, right) => (order.get(left) ?? 0) - (order.get(right) ?? 0))[0]];
    current.forEach((id) => {
      const parentLevels = (parents.get(id) ?? []).map((parent) => levels.get(parent) ?? -1);
      levels.set(id, parentLevels.length ? Math.max(...parentLevels) + 1 : 0);
      unresolved.delete(id);
    });
  }

  const columns = new Map<number, ExecutionNodeRecord[]>();
  nodes.forEach((node) => {
    const level = levels.get(node.nodeId) ?? 0;
    columns.set(level, [...(columns.get(level) ?? []), node]);
  });

  const largestColumn = Math.max(1, ...[...columns.values()].map((column) => column.length));
  const positioned: PositionedNode[] = [];
  [...columns.entries()].sort(([left], [right]) => left - right).forEach(([level, column]) => {
    const columnHeight = column.length * NODE_HEIGHT + Math.max(0, column.length - 1) * ROW_GAP;
    const offset = PADDING + ((largestColumn * NODE_HEIGHT + Math.max(0, largestColumn - 1) * ROW_GAP) - columnHeight) / 2;
    column.forEach((node, index) => positioned.push({
      node,
      x: PADDING + level * (NODE_WIDTH + COLUMN_GAP),
      y: offset + index * (NODE_HEIGHT + ROW_GAP)
    }));
  });

  return {
    nodes: positioned,
    width: PADDING * 2 + Math.max(1, columns.size) * NODE_WIDTH + Math.max(0, columns.size - 1) * COLUMN_GAP,
    height: PADDING * 2 + largestColumn * NODE_HEIGHT + Math.max(0, largestColumn - 1) * ROW_GAP
  };
}

function edgeClass(edge: Edge, nodes: PositionedNode[]) {
  const source = nodes.find((item) => item.node.nodeId === edge.from)?.node;
  const target = nodes.find((item) => item.node.nodeId === edge.to)?.node;
  if (source?.state === 'FAILED' || target?.state === 'FAILED') return 'failed';
  if (source?.state === 'SKIPPED' || target?.state === 'SKIPPED') return 'skipped';
  if (source?.state === 'RUNNING' || target?.state === 'RUNNING') return 'running';
  if (source?.state === 'SUCCEEDED' && target?.state === 'SUCCEEDED') return 'succeeded';
  return 'pending';
}

export function DagVisualizer({ execution, workflow, selectedNodeId, onSelect }: {
  execution: ExecutionRecord;
  workflow?: WorkflowDefinition;
  selectedNodeId?: string;
  onSelect: (node: ExecutionNodeRecord) => void;
}) {
  const edges = buildEdges(execution.nodes, workflow);
  const graph = layoutGraph(execution.nodes, edges);
  const nodesById = new Map(graph.nodes.map((item) => [item.node.nodeId, item]));

  return (
    <section className="dag-board">
      <div className="panel-head dag-head">
        <div>
          <h2>Workflow map</h2>
          <p>Dependencies and live execution state</p>
        </div>
        <StatusBadge status={execution.state} />
      </div>
      <div className="dag-scroll">
        <div className="dag-viewport" style={{ width: graph.width, height: graph.height }}>
          <svg className="dag-edges" viewBox={`0 0 ${graph.width} ${graph.height}`} aria-hidden="true">
            {edges.map((edge) => {
              const source = nodesById.get(edge.from);
              const target = nodesById.get(edge.to);
              if (!source || !target) return null;
              const startX = source.x + NODE_WIDTH;
              const startY = source.y + NODE_HEIGHT / 2;
              const endX = target.x;
              const endY = target.y + NODE_HEIGHT / 2;
              return <path key={`${edge.from}-${edge.to}`} className={`dag-edge ${edgeClass(edge, graph.nodes)}`} d={`M ${startX} ${startY} C ${startX + 44} ${startY}, ${endX - 44} ${endY}, ${endX} ${endY}`} />;
            })}
          </svg>
          {graph.nodes.map(({ node, x, y }) => (
            <button className={`dag-node dag-node-${node.state.toLowerCase()} ${selectedNodeId === node.nodeId ? 'selected' : ''}`} key={node.nodeId} onClick={() => onSelect(node)} style={{ left: x, top: y }} type="button">
              <span className={`dag-node-icon state-${node.state.toLowerCase()}`}>{stateIcon(node.state)}</span>
              <span className="dag-node-content">
                <strong>{node.nodeId.replaceAll('_', ' ')}</strong>
                <small>{node.type} task</small>
              </span>
              <span className="dag-node-time">{elapsed(node)}</span>
            </button>
          ))}
        </div>
      </div>
    </section>
  );
}
