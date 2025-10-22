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
