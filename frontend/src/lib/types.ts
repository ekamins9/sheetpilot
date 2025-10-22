// Type definitions for SheetPilot frontend

export interface UploadFile {
	file: File;
	id: string;
	progress: number;
	status: 'pending' | 'uploading' | 'success' | 'error';
	error?: string;
	spreadsheetId?: number;
}

export interface SpreadsheetResponse {
	id: number;
	name: string;
	fileType: string;
	fileSize: number;
	rowCount: number;
	columnCount: number;
	uploadedBy: string;
	uploadedAt: string;
	updatedAt: string;
}

export interface ValidationError {
	fileName: string;
	errors: string[];
}

export interface ToastMessage {
	id: string;
	type: 'success' | 'error' | 'info' | 'warning';
	message: string;
	duration?: number;
}

export interface PipelineStep {
	id: number;
	stepOrder: number;
	transformationType: string;
	config: string;
	createdAt: string;
}

export interface Pipeline {
	id: number;
	name: string;
	description: string;
	tags: string[];
	status: 'draft' | 'active' | 'archived';
	createdBy: string;
	createdAt: string;
	updatedAt: string;
	lastRunAt: string | null;
	stepCount: number;
	steps?: PipelineStep[];
}

export interface PipelineStats {
	totalPipelines: number;
	runsThisMonth: number;
	successRate: number;
	activePipelines: number;
}

export interface TransformationTemplate {
	id: string;
	type: string;
	name: string;
	description: string;
	category: 'data-cleaning' | 'joining' | 'aggregation' | 'ai-powered';
	icon: string;
	configSchema: Record<string, any>;
}

export interface EditorPipelineStep {
	id: string;
	type: string;
	name: string;
	config: Record<string, any>;
	order: number;
}

export interface PipelineEditorState {
	steps: EditorPipelineStep[];
	selectedStepId: string | null;
	isDirty: boolean;
	isSaving: boolean;
	lastSaved: string | null;
	history: EditorPipelineStep[][];
	historyIndex: number;
}

export interface AIChatMessage {
	id: string;
	role: 'user' | 'assistant';
	content: string;
	timestamp: string;
	transformationSuggestion?: {
		type: string;
		config: Record<string, any>;
		explanation: string;
		confidence: number;
	};
	tokenUsage?: {
		input: number;
		output: number;
		cost: number;
	};
}

export interface AIChatState {
	messages: AIChatMessage[];
	isLoading: boolean;
	error: string | null;
	totalCost: number;
}

export interface JobStepStatus {
	stepOrder: number;
	stepName: string;
	status: 'pending' | 'running' | 'completed' | 'failed';
	startedAt: string | null;
	completedAt: string | null;
	errorMessage: string | null;
	rowsProcessed: number;
	executionTimeMs?: number;
}

export interface JobResult {
	beforeRowCount: number;
	afterRowCount: number;
	beforeColumnCount: number;
	afterColumnCount: number;
	columnsAdded: string[];
	columnsRemoved: string[];
	columnsRenamed: Record<string, string>;
	nullValuesRemoved: number;
	duplicatesRemoved: number;
	recordsFiltered: number;
	dataQualityScore: number;
	warnings: string[];
	resultPreview?: {
		headers: string[];
		rows: string[][];
	};
	downloadUrl?: string;
}

export interface Job {
	id: number;
	pipelineId: number;
	pipelineName: string;
	spreadsheetIds: number[];
	spreadsheetNames: string[];
	status: 'pending' | 'running' | 'completed' | 'failed' | 'cancelled';
	progress: number;
	startedAt: string;
	completedAt: string | null;
	executionTimeMs: number;
	steps: JobStepStatus[];
	logs: string[];
	errorMessage: string | null;
	estimatedCost: number;
	actualCost: number | null;
	result?: JobResult;
}
