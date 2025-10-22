<script lang="ts">
	import { pipelineEditorStore, selectedStep } from '$lib/stores/pipelineEditor';
	import type { EditorPipelineStep } from '$lib/types';
	import { slide } from 'svelte/transition';

	$: step = $selectedStep;
	$: config = step?.config || {};

	function updateConfig(key: string, value: any) {
		if (!step) return;
		pipelineEditorStore.updateStep(step.id, {
			config: { ...config, [key]: value }
		});
	}

	function handleClose() {
		pipelineEditorStore.selectStep(null);
	}

	// Helper to parse comma-separated values
	function parseArray(value: string): string[] {
		return value
			.split(',')
			.map((v) => v.trim())
			.filter((v) => v);
	}
</script>

{#if step}
	<div
		class="h-full bg-white border-l-2 border-gray-200 flex flex-col"
		transition:slide={{ duration: 200, axis: 'x' }}
	>
		<!-- Header -->
		<div class="p-4 border-b-2 border-gray-200 flex items-center justify-between">
			<h2 class="text-lg font-bold text-gray-900">Configure Step</h2>
			<button
				on:click={handleClose}
				class="p-2 hover:bg-gray-100 rounded-lg transition-colors"
				aria-label="Close configuration panel"
			>
				<svg class="w-5 h-5 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
					<path
						stroke-linecap="round"
						stroke-linejoin="round"
						stroke-width="2"
						d="M6 18L18 6M6 6l12 12"
					/>
				</svg>
			</button>
		</div>

		<!-- Step Info -->
		<div class="p-4 bg-gray-50 border-b border-gray-200">
			<div class="flex items-center gap-3 mb-2">
				<span class="text-2xl">
					{step.type === 'filter'
						? '🔍'
						: step.type === 'sort'
							? '↕️'
							: step.type === 'select'
								? '📋'
								: step.type === 'rename'
									? '✏️'
									: step.type === 'join'
										? '🔗'
										: step.type === 'pivot'
											? '📊'
											: step.type === 'ai-transform'
												? '🤖'
												: '⚙️'}
				</span>
				<div>
					<h3 class="font-bold text-gray-900">{step.name}</h3>
					<p class="text-xs text-gray-600">Step {step.order + 1}</p>
				</div>
			</div>
		</div>

		<!-- Configuration Form -->
		<div class="flex-1 overflow-y-auto p-4">
			{#if step.type === 'filter'}
				<!-- Filter Configuration -->
				<div class="space-y-4">
					<div>
						<label class="block text-sm font-semibold text-gray-700 mb-2">Column Name</label>
						<input
							type="text"
							value={config.column || ''}
							on:input={(e) => updateConfig('column', e.currentTarget.value)}
							placeholder="e.g., revenue"
							class="w-full px-3 py-2 border-2 border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
						/>
					</div>

					<div>
						<label class="block text-sm font-semibold text-gray-700 mb-2">Operator</label>
						<select
							value={config.operator || '='}
							on:change={(e) => updateConfig('operator', e.currentTarget.value)}
							class="w-full px-3 py-2 border-2 border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
						>
							<option value="=">Equals (=)</option>
							<option value="!=">Not Equals (!=)</option>
							<option value=">">Greater Than (&gt;)</option>
							<option value="<">Less Than (&lt;)</option>
							<option value=">=">Greater or Equal (&gt;=)</option>
							<option value="<=">Less or Equal (&lt;=)</option>
							<option value="contains">Contains</option>
							<option value="startsWith">Starts With</option>
							<option value="endsWith">Ends With</option>
						</select>
					</div>

					<div>
						<label class="block text-sm font-semibold text-gray-700 mb-2">Value</label>
						<input
							type="text"
							value={config.value || ''}
							on:input={(e) => updateConfig('value', e.currentTarget.value)}
							placeholder="e.g., 1000"
							class="w-full px-3 py-2 border-2 border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
						/>
					</div>
				</div>
			{:else if step.type === 'sort'}
				<!-- Sort Configuration -->
				<div class="space-y-4">
					<div>
						<label class="block text-sm font-semibold text-gray-700 mb-2">
							Columns (comma-separated)
						</label>
						<input
							type="text"
							value={config.columns?.join(', ') || ''}
							on:input={(e) => updateConfig('columns', parseArray(e.currentTarget.value))}
							placeholder="e.g., date, revenue"
							class="w-full px-3 py-2 border-2 border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
						/>
						<p class="text-xs text-gray-500 mt-1">Separate multiple columns with commas</p>
					</div>

					<div>
						<label class="block text-sm font-semibold text-gray-700 mb-2">Direction</label>
						<select
							value={config.direction || 'ASC'}
							on:change={(e) => updateConfig('direction', e.currentTarget.value)}
							class="w-full px-3 py-2 border-2 border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
						>
							<option value="ASC">Ascending</option>
							<option value="DESC">Descending</option>
						</select>
					</div>
				</div>
			{:else if step.type === 'select'}
				<!-- Select Configuration -->
				<div class="space-y-4">
					<div>
						<label class="block text-sm font-semibold text-gray-700 mb-2">
							Columns to Keep (comma-separated)
						</label>
						<textarea
							value={config.columns?.join(', ') || ''}
							on:input={(e) => updateConfig('columns', parseArray(e.currentTarget.value))}
							placeholder="e.g., id, name, email, revenue"
							rows="4"
							class="w-full px-3 py-2 border-2 border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none"
						></textarea>
						<p class="text-xs text-gray-500 mt-1">All other columns will be removed</p>
					</div>
				</div>
			{:else if step.type === 'rename'}
				<!-- Rename Configuration -->
				<div class="space-y-4">
					<div>
						<label class="block text-sm font-semibold text-gray-700 mb-2">Column Mappings</label>
						<p class="text-xs text-gray-500 mb-2">
							Enter old name and new name for each column:
						</p>
						<div class="space-y-2">
							{#each Object.entries(config.mappings || {}) as [oldName, newName], i}
								<div class="flex gap-2">
									<input
										type="text"
										value={oldName}
										on:input={(e) => {
											const newMappings = { ...config.mappings };
											delete newMappings[oldName];
											newMappings[e.currentTarget.value] = newName;
											updateConfig('mappings', newMappings);
										}}
										placeholder="Old name"
										class="flex-1 px-3 py-2 border-2 border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
									/>
									<span class="flex items-center">→</span>
									<input
										type="text"
										value={newName}
										on:input={(e) => {
											const newMappings = { ...config.mappings };
											newMappings[oldName] = e.currentTarget.value;
											updateConfig('mappings', newMappings);
										}}
										placeholder="New name"
										class="flex-1 px-3 py-2 border-2 border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
									/>
									<button
										on:click={() => {
											const newMappings = { ...config.mappings };
											delete newMappings[oldName];
											updateConfig('mappings', newMappings);
										}}
										class="p-2 text-red-600 hover:bg-red-50 rounded-lg transition-colors"
									>
										<svg
											class="w-5 h-5"
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
							{/each}
							<button
								on:click={() => {
									const newMappings = { ...config.mappings, '': '' };
									updateConfig('mappings', newMappings);
								}}
								class="w-full px-3 py-2 border-2 border-dashed border-gray-300 rounded-lg text-sm text-gray-600 hover:bg-gray-50 transition-colors"
							>
								+ Add Mapping
							</button>
						</div>
					</div>
				</div>
			{:else if step.type === 'join'}
				<!-- Join Configuration -->
				<div class="space-y-4">
					<div>
						<label class="block text-sm font-semibold text-gray-700 mb-2">Join Type</label>
						<select
							value={config.joinType || 'inner'}
							on:change={(e) => updateConfig('joinType', e.currentTarget.value)}
							class="w-full px-3 py-2 border-2 border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
						>
							<option value="inner">Inner Join</option>
							<option value="left">Left Join</option>
							<option value="right">Right Join</option>
							<option value="outer">Outer Join</option>
						</select>
					</div>

					<div>
						<label class="block text-sm font-semibold text-gray-700 mb-2">
							Right Spreadsheet ID
						</label>
						<input
							type="number"
							value={config.rightSpreadsheetId || ''}
							on:input={(e) => updateConfig('rightSpreadsheetId', parseInt(e.currentTarget.value))}
							placeholder="e.g., 123"
							class="w-full px-3 py-2 border-2 border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
						/>
					</div>

					<div>
						<label class="block text-sm font-semibold text-gray-700 mb-2">Left Join Key</label>
						<input
							type="text"
							value={config.leftKey || ''}
							on:input={(e) => updateConfig('leftKey', e.currentTarget.value)}
							placeholder="e.g., customer_id"
							class="w-full px-3 py-2 border-2 border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
						/>
					</div>

					<div>
						<label class="block text-sm font-semibold text-gray-700 mb-2">Right Join Key</label>
						<input
							type="text"
							value={config.rightKey || ''}
							on:input={(e) => updateConfig('rightKey', e.currentTarget.value)}
							placeholder="e.g., id"
							class="w-full px-3 py-2 border-2 border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
						/>
					</div>
				</div>
			{:else if step.type === 'pivot'}
				<!-- Pivot Configuration -->
				<div class="space-y-4">
					<div>
						<label class="block text-sm font-semibold text-gray-700 mb-2">
							Index Columns (comma-separated)
						</label>
						<input
							type="text"
							value={config.indexColumns?.join(', ') || ''}
							on:input={(e) => updateConfig('indexColumns', parseArray(e.currentTarget.value))}
							placeholder="e.g., category, region"
							class="w-full px-3 py-2 border-2 border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
						/>
						<p class="text-xs text-gray-500 mt-1">Columns to group by</p>
					</div>

					<div>
						<label class="block text-sm font-semibold text-gray-700 mb-2">
							Value Columns (comma-separated)
						</label>
						<input
							type="text"
							value={config.valueColumns?.join(', ') || ''}
							on:input={(e) => updateConfig('valueColumns', parseArray(e.currentTarget.value))}
							placeholder="e.g., revenue, profit"
							class="w-full px-3 py-2 border-2 border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
						/>
						<p class="text-xs text-gray-500 mt-1">Columns to aggregate</p>
					</div>

					<div>
						<label class="block text-sm font-semibold text-gray-700 mb-2">Aggregation</label>
						<select
							value={config.aggregation || 'sum'}
							on:change={(e) => updateConfig('aggregation', e.currentTarget.value)}
							class="w-full px-3 py-2 border-2 border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
						>
							<option value="sum">Sum</option>
							<option value="avg">Average</option>
							<option value="count">Count</option>
							<option value="min">Minimum</option>
							<option value="max">Maximum</option>
						</select>
					</div>
				</div>
			{:else if step.type === 'ai-transform'}
				<!-- AI Transform Configuration -->
				<div class="space-y-4">
					<div>
						<label class="block text-sm font-semibold text-gray-700 mb-2">
							Natural Language Prompt
						</label>
						<textarea
							value={config.prompt || ''}
							on:input={(e) => updateConfig('prompt', e.currentTarget.value)}
							placeholder="Describe the transformation in plain English, e.g., 'Remove all rows where revenue is less than 1000 and sort by date descending'"
							rows="6"
							class="w-full px-3 py-2 border-2 border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none"
						></textarea>
						<p class="text-xs text-gray-500 mt-1">
							AI will interpret your instructions and configure the transformation
						</p>
					</div>
				</div>
			{:else}
				<!-- Generic Configuration -->
				<div class="text-center py-8 text-gray-500">
					<p>No configuration available for this transformation type</p>
				</div>
			{/if}
		</div>

		<!-- Footer -->
		<div class="p-4 bg-gray-50 border-t-2 border-gray-200">
			<button
				on:click={handleClose}
				class="w-full px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white font-semibold rounded-lg transition-colors"
			>
				Done
			</button>
		</div>
	</div>
{/if}

<style>
	/* Custom scrollbar */
	div::-webkit-scrollbar {
		width: 8px;
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
