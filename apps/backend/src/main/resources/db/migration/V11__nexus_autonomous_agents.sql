-- Phase 9: NEXUS Autonomous Agents — Intelligent Workflow Orchestration Schema

-- Agent Runs Table
CREATE TABLE agent_runs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    agent_id VARCHAR(64) NOT NULL,
    workflow_id VARCHAR(64),
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    trigger_type VARCHAR(32) NOT NULL DEFAULT 'MANUAL',
    input_context TEXT,
    input_summary VARCHAR(255),
    output_summary TEXT,
    output_result TEXT,
    error_summary TEXT,
    token_usage INT NOT NULL DEFAULT 0,
    estimated_cost NUMERIC(8,4) NOT NULL DEFAULT 0.0000,
    started_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_agent_runs_user_status ON agent_runs(user_id, status);
CREATE INDEX idx_agent_runs_created ON agent_runs(user_id, created_at DESC);
CREATE INDEX idx_agent_runs_agent ON agent_runs(agent_id);

-- Agent Steps Table
CREATE TABLE agent_steps (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    agent_run_id UUID NOT NULL REFERENCES agent_runs(id) ON DELETE CASCADE,
    sequence_order INT NOT NULL,
    step_type VARCHAR(32) NOT NULL,
    tool_id VARCHAR(64),
    status VARCHAR(32) NOT NULL DEFAULT 'STARTED',
    input_summary TEXT,
    output_summary TEXT,
    started_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_agent_steps_run_order ON agent_steps(agent_run_id, sequence_order ASC);

-- Agent Approvals Table
CREATE TABLE agent_approvals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    agent_run_id UUID NOT NULL REFERENCES agent_runs(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    action_type VARCHAR(64) NOT NULL,
    action_payload TEXT NOT NULL,
    action_summary VARCHAR(512) NOT NULL,
    autonomy_level INT NOT NULL DEFAULT 3,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    requested_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP WITH TIME ZONE,
    decided_at TIMESTAMP WITH TIME ZONE,
    decision_reason TEXT
);

CREATE INDEX idx_agent_approvals_user_status ON agent_approvals(user_id, status);
CREATE INDEX idx_agent_approvals_run ON agent_approvals(agent_run_id);
