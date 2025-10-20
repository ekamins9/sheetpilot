import { writable, derived, get } from 'svelte/store';
import type { PipelineEditorState, EditorPipelineStep, TransformationTemplate } from '$lib/types';

const MAX_HISTORY = 50;

const initialState: PipelineEditorState = {
	steps: [],
	selectedStepId: null,
	isDirty: false,
	isSaving: false,
	lastSaved: null,
	history: [[]],
	historyIndex: 0
};

function createPipelineEditorStore() {
	const { subscribe, set, update } = writable<PipelineEditorState>(initialState);

	function addToHistory(steps: EditorPipelineStep[]) {
		update((state) => {
			const newHistory = state.history.slice(0, state.historyIndex + 1);
			newHistory.push(JSON.parse(JSON.stringify(steps)));

			// Limit history size
			if (newHistory.length > MAX_HISTORY) {
				newHistory.shift();
			}

			return {
				...state,
				history: newHistory,
				historyIndex: newHistory.length - 1
			};
		});
	}

	return {
		subscribe,

		// Initialize editor with pipeline data
		init: (steps: EditorPipelineStep[]) => {
			set({
				steps: JSON.parse(JSON.stringify(steps)),
				selectedStepId: null,
				isDirty: false,
				isSaving: false,
				lastSaved: new Date().toISOString(),
				history: [JSON.parse(JSON.stringify(steps))],
				historyIndex: 0
			});
		},

		// Add step
		addStep: (step: EditorPipelineStep) => {
			update((state) => {
				const newSteps = [...state.steps, { ...step, order: state.steps.length }];
				addToHistory(newSteps);
				return {
					...state,
					steps: newSteps,
					isDirty: true
				};
			});
		},

		// Update step
		updateStep: (stepId: string, updates: Partial<EditorPipelineStep>) => {
			update((state) => {
				const newSteps = state.steps.map((s) =>
					s.id === stepId ? { ...s, ...updates } : s
				);
				addToHistory(newSteps);
				return {
					...state,
					steps: newSteps,
					isDirty: true
				};
			});
		},

		// Remove step
		removeStep: (stepId: string) => {
			update((state) => {
				const newSteps = state.steps
					.filter((s) => s.id !== stepId)
					.map((s, i) => ({ ...s, order: i }));
				addToHistory(newSteps);
				return {
					...state,
					steps: newSteps,
					selectedStepId: state.selectedStepId === stepId ? null : state.selectedStepId,
					isDirty: true
				};
			});
		},

		// Reorder steps
		reorderSteps: (newSteps: EditorPipelineStep[]) => {
			update((state) => {
				const reordered = newSteps.map((s, i) => ({ ...s, order: i }));
				addToHistory(reordered);
				return {
					...state,
					steps: reordered,
					isDirty: true
				};
			});
		},

		// Select step
		selectStep: (stepId: string | null) => {
			update((state) => ({ ...state, selectedStepId: stepId }));
		},

		// Undo
		undo: () => {
			update((state) => {
				if (state.historyIndex > 0) {
					const newIndex = state.historyIndex - 1;
					return {
						...state,
						steps: JSON.parse(JSON.stringify(state.history[newIndex])),
						historyIndex: newIndex,
						isDirty: true
					};
				}
				return state;
			});
		},

		// Redo
		redo: () => {
			update((state) => {
				if (state.historyIndex < state.history.length - 1) {
					const newIndex = state.historyIndex + 1;
					return {
						...state,
						steps: JSON.parse(JSON.stringify(state.history[newIndex])),
						historyIndex: newIndex,
						isDirty: true
					};
				}
				return state;
			});
		},

		// Save state
		setSaving: (isSaving: boolean) => {
			update((state) => ({ ...state, isSaving }));
		},

		markSaved: () => {
			update((state) => ({
				...state,
				isDirty: false,
				lastSaved: new Date().toISOString()
			}));
		},

		// Reset
		reset: () => set(initialState)
	};
}

export const pipelineEditorStore = createPipelineEditorStore();

// Derived stores
export const canUndo = derived(
	pipelineEditorStore,
	($store) => $store.historyIndex > 0
);

export const canRedo = derived(
	pipelineEditorStore,
	($store) => $store.historyIndex < $store.history.length - 1
);

export const selectedStep = derived(
	pipelineEditorStore,
	($store) => $store.steps.find((s) => s.id === $store.selectedStepId) || null
);

// Transformation templates library
export const transformationTemplates: TransformationTemplate[] = [
	// Data Cleaning
	{
		id: 'filter',
		type: 'filter',
		name: 'Filter Rows',
		description: 'Filter rows based on column conditions',
		category: 'data-cleaning',
		icon: '🔍',
		configSchema: {
			column: { type: 'string', required: true },
			operator: { type: 'string', required: true },
			value: { type: 'string', required: true }
		}
	},
	{
		id: 'sort',
		type: 'sort',
		name: 'Sort Data',
		description: 'Sort rows by one or more columns',
		category: 'data-cleaning',
		icon: '↕️',
		configSchema: {
			columns: { type: 'array', required: true },
			direction: { type: 'string', required: true }
		}
	},
	{
		id: 'select',
		type: 'select',
		name: 'Select Columns',
		description: 'Choose specific columns to keep',
		category: 'data-cleaning',
		icon: '📋',
		configSchema: {
			columns: { type: 'array', required: true }
		}
	},
	{
		id: 'rename',
		type: 'rename',
		name: 'Rename Columns',
		description: 'Rename one or more columns',
		category: 'data-cleaning',
		icon: '✏️',
		configSchema: {
			mappings: { type: 'object', required: true }
		}
	},

	// Joining
	{
		id: 'join',
		type: 'join',
		name: 'Join Tables',
		description: 'Combine two tables using a join operation',
		category: 'joining',
		icon: '🔗',
		configSchema: {
			joinType: { type: 'string', required: true },
			leftKey: { type: 'string', required: true },
			rightKey: { type: 'string', required: true },
			rightSpreadsheetId: { type: 'number', required: true }
		}
	},

	// Aggregation
	{
		id: 'pivot',
		type: 'pivot',
		name: 'Pivot Table',
		description: 'Create pivot table with aggregations',
		category: 'aggregation',
		icon: '📊',
		configSchema: {
			indexColumns: { type: 'array', required: true },
			valueColumns: { type: 'array', required: true },
			aggregation: { type: 'string', required: true }
		}
	},

	// AI-Powered
	{
		id: 'ai-transform',
		type: 'ai-transform',
		name: 'AI Transform',
		description: 'Use natural language to describe transformation',
		category: 'ai-powered',
		icon: '🤖',
		configSchema: {
			prompt: { type: 'string', required: true }
		}
	}
];
