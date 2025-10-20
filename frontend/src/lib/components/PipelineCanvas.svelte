<script lang="ts">
	import { pipelineEditorStore } from '$lib/stores/pipelineEditor';
	import type { EditorPipelineStep, TransformationTemplate } from '$lib/types';
	import { dndzone } from 'svelte-dnd-action';
	import { flip } from 'svelte/animate';
	import { scale, fade } from 'svelte/transition';

	export let onSelectStep: (stepId: string | null) => void;

	let zoomLevel = 100;
	let isDraggingOver = false;

	$: steps = $pipelineEditorStore.steps;
	$: selectedStepId = $pipelineEditorStore.selectedStepId;

	function handleDragOver(e: DragEvent) {
		e.preventDefault();
		isDraggingOver = true;
	}

	function handleDragLeave() {
		isDraggingOver = false;
	}

	function handleDrop(e: DragEvent) {
		e.preventDefault();
		isDraggingOver = false;

		if (e.dataTransfer) {
			const data = e.dataTransfer.getData('application/json');
			if (data) {
				const template: TransformationTemplate = JSON.parse(data);
				addTransformation(template);
			}
		}
	}

	function addTransformation(template: TransformationTemplate) {
		const newStep: EditorPipelineStep = {
			id: `step-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
			type: template.type,
			name: template.name,
			config: {},
			order: steps.length
		};
		pipelineEditorStore.addStep(newStep);
	}

	function handleDndConsider(e: CustomEvent<any>) {
		const { items } = e.detail;
		pipelineEditorStore.reorderSteps(items);
	}

	function handleDndFinalize(e: CustomEvent<any>) {
		const { items } = e.detail;
		pipelineEditorStore.reorderSteps(items);
	}

	function handleDeleteStep(stepId: string) {
		pipelineEditorStore.removeStep(stepId);
	}

	function getStepIcon(type: string): string {
		const icons: Record<string, string> = {
			filter: '🔍',
			sort: '↕️',
			select: '📋',
			rename: '✏️',
			join: '🔗',
			pivot: '📊',
			'ai-transform': '🤖'
		};
		return icons[type] || '⚙️';
	}

	function getConfigSummary(step: EditorPipelineStep): string {
		const config = step.config;
		if (Object.keys(config).length === 0) {
			return 'Not configured';
		}

		// Generate readable summary based on step type
		switch (step.type) {
			case 'filter':
				return `${config.column} ${config.operator} ${config.value}`;
			case 'sort':
				return `${config.columns?.join(', ')} (${config.direction})`;
			case 'select':
				return `${config.columns?.length || 0} columns`;
			case 'rename':
				return `${Object.keys(config.mappings || {}).length} columns`;
			case 'join':
				return `${config.joinType} join on ${config.leftKey}`;
			case 'pivot':
				return `${config.aggregation} by ${config.indexColumns?.join(', ')}`;
			case 'ai-transform':
				return config.prompt || 'No prompt';
			default:
				return 'Configured';
		}
	}

	function handleZoomIn() {
		zoomLevel = Math.min(150, zoomLevel + 10);
	}

	function handleZoomOut() {
		zoomLevel = Math.max(50, zoomLevel - 10);
	}

	function handleZoomReset() {
		zoomLevel = 100;
	}
</script>

<div class="h-full bg-gray-50 flex flex-col relative">
	<!-- Zoom Controls -->
	<div class="absolute top-4 right-4 z-10 flex gap-2">
		<button
			on:click={handleZoomOut}
			class="w-10 h-10 bg-white border-2 border-gray-300 rounded-lg hover:bg-gray-50 transition-colors flex items-center justify-center"
			title="Zoom out"
		>
			<svg class="w-5 h-5 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
				<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 12H4" />
			</svg>
		</button>
		<button
			on:click={handleZoomReset}
			class="px-3 h-10 bg-white border-2 border-gray-300 rounded-lg hover:bg-gray-50 transition-colors text-sm font-semibold text-gray-700"
		>
			{zoomLevel}%
		</button>
		<button
			on:click={handleZoomIn}
			class="w-10 h-10 bg-white border-2 border-gray-300 rounded-lg hover:bg-gray-50 transition-colors flex items-center justify-center"
			title="Zoom in"
		>
			<svg class="w-5 h-5 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
				<path
					stroke-linecap="round"
					stroke-linejoin="round"
					stroke-width="2"
					d="M12 4v16m8-8H4"
				/>
			</svg>
		</button>
	</div>

	<!-- Canvas -->
	<div
		class="flex-1 overflow-auto p-8"
		on:dragover={handleDragOver}
		on:dragleave={handleDragLeave}
		on:drop={handleDrop}
		role="region"
		aria-label="Pipeline canvas"
	>
		<div
			class="max-w-2xl mx-auto transition-transform duration-200"
			style="transform: scale({zoomLevel / 100}); transform-origin: top center;"
		>
			{#if steps.length === 0}
				<!-- Empty State -->
				<div
					class="border-4 border-dashed rounded-2xl p-12 text-center transition-colors"
					class:border-blue-400={isDraggingOver}
					class:bg-blue-50={isDraggingOver}
					class:border-gray-300={!isDraggingOver}
					class:bg-white={!isDraggingOver}
					transition:fade
				>
					<svg
						class="mx-auto h-24 w-24 mb-4 transition-colors"
						class:text-blue-400={isDraggingOver}
						class:text-gray-300={!isDraggingOver}
						fill="none"
						stroke="currentColor"
						viewBox="0 0 24 24"
					>
						<path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="1.5"
							d="M12 6v6m0 0v6m0-6h6m-6 0H6"
						/>
					</svg>
					<h3 class="text-xl font-bold text-gray-900 mb-2">
						{isDraggingOver ? 'Drop to add transformation' : 'Start Building Your Pipeline'}
					</h3>
					<p class="text-gray-600">
						Drag transformations from the sidebar or click to add them
					</p>
				</div>
			{:else}
				<!-- Pipeline Steps -->
				<div
					use:dndzone={{ items: steps, flipDurationMs: 200, dropTargetStyle: {} }}
					on:consider={handleDndConsider}
					on:finalize={handleDndFinalize}
					class="space-y-4"
				>
					{#each steps as step, index (step.id)}
						<div animate:flip={{ duration: 200 }}>
							<!-- Step Card -->
							<div
								class="bg-white border-2 rounded-xl p-4 cursor-pointer hover:shadow-lg transition-all duration-200"
								class:border-blue-500={selectedStepId === step.id}
								class:shadow-lg={selectedStepId === step.id}
								class:border-gray-300={selectedStepId !== step.id}
								on:click={() => onSelectStep(step.id)}
								transition:scale={{ duration: 200 }}
							>
								<div class="flex items-start gap-4">
									<!-- Step Number -->
									<div
										class="flex-shrink-0 w-10 h-10 rounded-full flex items-center justify-center text-sm font-bold transition-colors"
										class:bg-blue-600={selectedStepId === step.id}
										class:text-white={selectedStepId === step.id}
										class:bg-gray-200={selectedStepId !== step.id}
										class:text-gray-700={selectedStepId !== step.id}
									>
										{index + 1}
									</div>

									<!-- Step Icon & Content -->
									<div class="flex-1 min-w-0">
										<div class="flex items-center gap-2 mb-1">
											<span class="text-2xl">{getStepIcon(step.type)}</span>
											<h4 class="font-bold text-gray-900">{step.name}</h4>
										</div>
										<p class="text-sm text-gray-600">{getConfigSummary(step)}</p>
									</div>

									<!-- Actions -->
									<div class="flex gap-2">
										<button
											on:click|stopPropagation={() => onSelectStep(step.id)}
											class="p-2 hover:bg-gray-100 rounded-lg transition-colors"
											title="Edit"
										>
											<svg
												class="w-5 h-5 text-gray-600"
												fill="none"
												stroke="currentColor"
												viewBox="0 0 24 24"
											>
												<path
													stroke-linecap="round"
													stroke-linejoin="round"
													stroke-width="2"
													d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"
												/>
											</svg>
										</button>
										<button
											on:click|stopPropagation={() => handleDeleteStep(step.id)}
											class="p-2 hover:bg-red-50 rounded-lg transition-colors"
											title="Delete"
										>
											<svg
												class="w-5 h-5 text-red-600"
												fill="none"
												stroke="currentColor"
												viewBox="0 0 24 24"
											>
												<path
													stroke-linecap="round"
													stroke-linejoin="round"
													stroke-width="2"
													d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
												/>
											</svg>
										</button>
									</div>
								</div>
							</div>

							<!-- Connector with animated flow -->
							{#if index < steps.length - 1}
								<div class="flex justify-center py-2">
									<div class="relative">
										<svg width="2" height="24" class="text-gray-300">
											<line
												x1="1"
												y1="0"
												x2="1"
												y2="24"
												stroke="currentColor"
												stroke-width="2"
											/>
										</svg>
										<!-- Animated data flow -->
										<div
											class="absolute top-0 left-1/2 -translate-x-1/2 w-2 h-2 bg-blue-500 rounded-full animate-flow"
										></div>
									</div>
								</div>
							{/if}
						</div>
					{/each}
				</div>
			{/if}
		</div>
	</div>
</div>

<style>
	@keyframes flow {
		0% {
			transform: translate(-50%, 0) scale(0);
			opacity: 0;
		}
		50% {
			opacity: 1;
		}
		100% {
			transform: translate(-50%, 24px) scale(1);
			opacity: 0;
		}
	}

	.animate-flow {
		animation: flow 1.5s ease-in-out infinite;
	}

	/* Custom scrollbar */
	div::-webkit-scrollbar {
		width: 8px;
		height: 8px;
	}

	div::-webkit-scrollbar-track {
		background: #f1f1f1;
	}

	div::-webkit-scrollbar-thumb {
		background: #cbd5e1;
		border-radius: 4px;
	}

	div::-webkit-scrollbar-thumb:hover {
		background: #94a3b8;
	}
</style>
