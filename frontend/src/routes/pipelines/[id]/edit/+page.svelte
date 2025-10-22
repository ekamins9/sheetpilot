<script lang="ts">
	import { onMount, onDestroy } from 'svelte';
	import { page } from '$app/stores';
	import { goto } from '$app/navigation';
	import { pipelineEditorStore, canUndo, canRedo, transformationTemplates } from '$lib/stores/pipelineEditor';
	import { toastStore } from '$lib/stores/toast';
	import TransformationLibrary from '$lib/components/TransformationLibrary.svelte';
	import PipelineCanvas from '$lib/components/PipelineCanvas.svelte';
	import StepConfigPanel from '$lib/components/StepConfigPanel.svelte';
	import AIAssistant from '$lib/components/AIAssistant.svelte';
	import type { TransformationTemplate, EditorPipelineStep } from '$lib/types';

	const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

	let pipelineId: number;
	let pipelineName = '';
	let isEditingName = false;
	let saveTimeout: NodeJS.Timeout;
	let keyboardListener: ((e: KeyboardEvent) => void) | null = null;

	$: pipelineId = parseInt($page.params.id);
	$: isDirty = $pipelineEditorStore.isDirty;
	$: isSaving = $pipelineEditorStore.isSaving;
	$: lastSaved = $pipelineEditorStore.lastSaved;
	$: selectedStepId = $pipelineEditorStore.selectedStepId;

	onMount(() => {
		loadPipeline();
		setupKeyboardShortcuts();
	});

	onDestroy(() => {
		if (keyboardListener) {
			window.removeEventListener('keydown', keyboardListener);
		}
		if (saveTimeout) {
			clearTimeout(saveTimeout);
		}
	});

	async function loadPipeline() {
		try {
			const response = await fetch(`${API_URL}/pipelines/${pipelineId}`);
			if (!response.ok) throw new Error('Failed to load pipeline');

			const pipeline = await response.json();
			pipelineName = pipeline.name;

			// Convert backend steps to editor format
			const editorSteps: EditorPipelineStep[] = (pipeline.steps || []).map((step: any, index: number) => ({
				id: `step-${step.id}`,
				type: step.transformationType,
				name: transformationTemplates.find(t => t.type === step.transformationType)?.name || step.transformationType,
				config: JSON.parse(step.config || '{}'),
				order: index
			}));

			pipelineEditorStore.init(editorSteps);
		} catch (error) {
			console.error('Failed to load pipeline:', error);
			toastStore.add('error', 'Failed to load pipeline');
			goto('/pipelines');
		}
	}

	function setupKeyboardShortcuts() {
		keyboardListener = (e: KeyboardEvent) => {
			// Undo: Ctrl/Cmd + Z
			if ((e.ctrlKey || e.metaKey) && e.key === 'z' && !e.shiftKey) {
				e.preventDefault();
				if ($canUndo) {
					pipelineEditorStore.undo();
					toastStore.add('info', 'Undo');
				}
			}
			// Redo: Ctrl/Cmd + Shift + Z or Ctrl/Cmd + Y
			else if ((e.ctrlKey || e.metaKey) && (e.shiftKey && e.key === 'z' || e.key === 'y')) {
				e.preventDefault();
				if ($canRedo) {
					pipelineEditorStore.redo();
					toastStore.add('info', 'Redo');
				}
			}
			// Save: Ctrl/Cmd + S
			else if ((e.ctrlKey || e.metaKey) && e.key === 's') {
				e.preventDefault();
				savePipeline();
			}
		};

		window.addEventListener('keydown', keyboardListener);
	}

	// Auto-save with 2 second debounce
	$: if ($pipelineEditorStore.steps && isDirty) {
		if (saveTimeout) clearTimeout(saveTimeout);
		saveTimeout = setTimeout(() => {
			savePipeline();
		}, 2000);
	}

	async function savePipeline() {
		if (isSaving) return;

		pipelineEditorStore.setSaving(true);

		try {
			// Convert editor steps to backend format
			const backendSteps = $pipelineEditorStore.steps.map((step, index) => ({
				stepOrder: index,
				transformationType: step.type,
				config: JSON.stringify(step.config)
			}));

			const response = await fetch(`${API_URL}/pipelines/${pipelineId}`, {
				method: 'PUT',
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify({
					name: pipelineName,
					steps: backendSteps
				})
			});

			if (!response.ok) throw new Error('Failed to save pipeline');

			pipelineEditorStore.markSaved();
			toastStore.add('success', 'Pipeline saved');
		} catch (error) {
			console.error('Failed to save pipeline:', error);
			toastStore.add('error', 'Failed to save pipeline');
		} finally {
			pipelineEditorStore.setSaving(false);
		}
	}

	function handleAddTransformation(template: TransformationTemplate) {
		const newStep: EditorPipelineStep = {
			id: `step-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
			type: template.type,
			name: template.name,
			config: {},
			order: $pipelineEditorStore.steps.length
		};
		pipelineEditorStore.addStep(newStep);
	}

	function handleSelectStep(stepId: string | null) {
		pipelineEditorStore.selectStep(stepId);
	}

	async function handleTestRun() {
		toastStore.add('info', 'Starting test run...');
		try {
			const response = await fetch(`${API_URL}/pipelines/${pipelineId}/test`, {
				method: 'POST'
			});
			if (!response.ok) throw new Error('Test run failed');
			toastStore.add('success', 'Test run completed successfully');
		} catch (error) {
			toastStore.add('error', 'Test run failed');
		}
	}

	function formatLastSaved(timestamp: string | null): string {
		if (!timestamp) return '';
		const date = new Date(timestamp);
		const now = new Date();
		const diffSeconds = Math.floor((now.getTime() - date.getTime()) / 1000);

		if (diffSeconds < 60) return 'just now';
		if (diffSeconds < 3600) return `${Math.floor(diffSeconds / 60)}m ago`;
		if (diffSeconds < 86400) return `${Math.floor(diffSeconds / 3600)}h ago`;
		return date.toLocaleString();
	}
</script>

<svelte:head>
	<title>Edit Pipeline - {pipelineName}</title>
</svelte:head>

<div class="h-screen flex flex-col bg-gray-50">
	<!-- Top Bar -->
	<div class="bg-white border-b-2 border-gray-200 px-6 py-3 flex items-center justify-between">
		<div class="flex items-center gap-4">
			<!-- Back Button -->
			<button
				on:click={() => goto('/pipelines')}
				class="p-2 hover:bg-gray-100 rounded-lg transition-colors"
				aria-label="Back to pipelines"
			>
				<svg class="w-5 h-5 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
					<path
						stroke-linecap="round"
						stroke-linejoin="round"
						stroke-width="2"
						d="M10 19l-7-7m0 0l7-7m-7 7h18"
					/>
				</svg>
			</button>

			<!-- Pipeline Name -->
			{#if isEditingName}
				<input
					type="text"
					bind:value={pipelineName}
					on:blur={() => (isEditingName = false)}
					on:keydown={(e) => {
						if (e.key === 'Enter') isEditingName = false;
					}}
					class="text-xl font-bold text-gray-900 border-2 border-blue-500 rounded px-2 py-1 focus:outline-none"
					autofocus
				/>
			{:else}
				<button
					on:click={() => (isEditingName = true)}
					class="text-xl font-bold text-gray-900 hover:text-blue-600 transition-colors"
				>
					{pipelineName}
				</button>
			{/if}

			<!-- Status Indicators -->
			<div class="flex items-center gap-2 text-sm">
				{#if isSaving}
					<span class="text-blue-600 flex items-center gap-1">
						<div class="w-3 h-3 border-2 border-blue-600 border-t-transparent rounded-full animate-spin"></div>
						Saving...
					</span>
				{:else if isDirty}
					<span class="text-orange-600">● Unsaved changes</span>
				{:else if lastSaved}
					<span class="text-gray-500">Saved {formatLastSaved(lastSaved)}</span>
				{/if}
			</div>
		</div>

		<!-- Action Buttons -->
		<div class="flex items-center gap-3">
			<!-- Undo/Redo -->
			<div class="flex gap-1 border-2 border-gray-300 rounded-lg overflow-hidden">
				<button
					on:click={() => pipelineEditorStore.undo()}
					disabled={!$canUndo}
					class="px-3 py-2 hover:bg-gray-100 transition-colors disabled:opacity-30 disabled:cursor-not-allowed"
					title="Undo (Ctrl+Z)"
				>
					<svg class="w-5 h-5 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
						<path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="2"
							d="M3 10h10a8 8 0 018 8v2M3 10l6 6m-6-6l6-6"
						/>
					</svg>
				</button>
				<div class="w-px bg-gray-300"></div>
				<button
					on:click={() => pipelineEditorStore.redo()}
					disabled={!$canRedo}
					class="px-3 py-2 hover:bg-gray-100 transition-colors disabled:opacity-30 disabled:cursor-not-allowed"
					title="Redo (Ctrl+Y)"
				>
					<svg class="w-5 h-5 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
						<path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="2"
							d="M21 10h-10a8 8 0 00-8 8v2M21 10l-6 6m6-6l-6-6"
						/>
					</svg>
				</button>
			</div>

			<!-- Test Run -->
			<button
				on:click={handleTestRun}
				class="px-4 py-2 border-2 border-gray-300 text-gray-700 font-semibold rounded-lg hover:bg-gray-50 transition-colors flex items-center gap-2"
			>
				<svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
					<path
						stroke-linecap="round"
						stroke-linejoin="round"
						stroke-width="2"
						d="M14.752 11.168l-3.197-2.132A1 1 0 0010 9.87v4.263a1 1 0 001.555.832l3.197-2.132a1 1 0 000-1.664z"
					/>
					<path
						stroke-linecap="round"
						stroke-linejoin="round"
						stroke-width="2"
						d="M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
					/>
				</svg>
				Test Run
			</button>

			<!-- Save Button -->
			<button
				on:click={savePipeline}
				disabled={!isDirty || isSaving}
				class="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white font-semibold rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
			>
				<svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
					<path
						stroke-linecap="round"
						stroke-linejoin="round"
						stroke-width="2"
						d="M8 7H5a2 2 0 00-2 2v9a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-3m-1 4l-3 3m0 0l-3-3m3 3V4"
					/>
				</svg>
				Save
			</button>
		</div>
	</div>

	<!-- Main Content - Three Panel Layout -->
	<div class="flex-1 flex overflow-hidden">
		<!-- Left Sidebar - Transformation Library -->
		<div class="w-80 flex-shrink-0">
			<TransformationLibrary onAddTransformation={handleAddTransformation} />
		</div>

		<!-- Center Canvas -->
		<div class="flex-1">
			<PipelineCanvas onSelectStep={handleSelectStep} />
		</div>

		<!-- Right Panel - Step Configuration -->
		{#if selectedStepId}
			<div class="w-96 flex-shrink-0">
				<StepConfigPanel />
			</div>
		{/if}
	</div>

	<!-- AI Assistant -->
	<AIAssistant />
</div>
