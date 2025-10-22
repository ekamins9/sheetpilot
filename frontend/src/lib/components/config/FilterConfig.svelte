<script lang="ts">
	import { filterConfigSchema, type FilterCondition, type FilterConfig } from '$lib/schemas/transformations';
	import { scale } from 'svelte/transition';

	export let config: FilterConfig = { conditions: [], logic: 'AND' };
	export let availableColumns: string[] = [];
	export let onSave: (config: FilterConfig) => void;
	export let onCancel: () => void;
	export let onTest: (config: FilterConfig) => Promise<any>;

	let errors: Record<string, string> = {};
	let testing = false;
	let previewData: { headers: string[]; rows: string[][] } | null = null;

	$: if (config.conditions.length === 0) {
		addCondition();
	}

	function addCondition() {
		config.conditions = [
			...config.conditions,
			{ id: crypto.randomUUID(), column: '', operator: '=', value: '' }
		];
	}

	function removeCondition(id: string) {
		config.conditions = config.conditions.filter((c) => c.id !== id);
	}

	function validate(): boolean {
		errors = {};
		try {
			filterConfigSchema.parse(config);
			return true;
		} catch (error: any) {
			error.errors.forEach((err: any) => {
				errors[err.path.join('.')] = err.message;
			});
			return false;
		}
	}

	function handleSave() {
		if (validate()) {
			onSave(config);
		}
	}

	async function handleTest() {
		if (!validate()) return;

		testing = true;
		try {
			previewData = await onTest(config);
		} catch (error) {
			console.error('Test failed:', error);
		} finally {
			testing = false;
		}
	}

	const operators = [
		{ value: '=', label: 'Equals (=)' },
		{ value: '!=', label: 'Not Equals (!=)' },
		{ value: '>', label: 'Greater Than (>)' },
		{ value: '<', label: 'Less Than (<)' },
		{ value: '>=', label: 'Greater or Equal (>=)' },
		{ value: '<=', label: 'Less or Equal (<=)' },
		{ value: 'contains', label: 'Contains' },
		{ value: 'startsWith', label: 'Starts With' },
		{ value: 'endsWith', label: 'Ends With' },
		{ value: 'regex', label: 'Regex Match' }
	];
</script>

<div class="space-y-4">
	<!-- Logic Selector -->
	<div>
		<label class="block text-sm font-semibold text-gray-700 mb-2">
			Condition Logic
			<span class="text-xs font-normal text-gray-500 ml-1">
				(How to combine multiple conditions)
			</span>
		</label>
		<div class="flex gap-2">
			<button
				on:click={() => (config.logic = 'AND')}
				class="flex-1 px-4 py-2 rounded-lg font-semibold transition-colors"
				class:bg-blue-600={config.logic === 'AND'}
				class:text-white={config.logic === 'AND'}
				class:bg-gray-100={config.logic !== 'AND'}
				class:text-gray-700={config.logic !== 'AND'}
			>
				AND (All must match)
			</button>
			<button
				on:click={() => (config.logic = 'OR')}
				class="flex-1 px-4 py-2 rounded-lg font-semibold transition-colors"
				class:bg-blue-600={config.logic === 'OR'}
				class:text-white={config.logic === 'OR'}
				class:bg-gray-100={config.logic !== 'OR'}
				class:text-gray-700={config.logic !== 'OR'}
			>
				OR (Any can match)
			</button>
		</div>
	</div>

	<!-- Conditions -->
	<div>
		<label class="block text-sm font-semibold text-gray-700 mb-2">Filter Conditions</label>
		<div class="space-y-3">
			{#each config.conditions as condition, i (condition.id)}
				<div
					class="bg-gray-50 border-2 border-gray-200 rounded-lg p-4"
					transition:scale={{ duration: 200 }}
				>
					<div class="flex items-start gap-3">
						<div class="flex-1 grid grid-cols-3 gap-3">
							<!-- Column Selector -->
							<div>
								<label class="block text-xs font-medium text-gray-600 mb-1">Column</label>
								<select
									bind:value={condition.column}
									class="w-full px-3 py-2 border-2 border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
								>
									<option value="">Select column</option>
									{#each availableColumns as col}
										<option value={col}>{col}</option>
									{/each}
								</select>
							</div>

							<!-- Operator Selector -->
							<div>
								<label class="block text-xs font-medium text-gray-600 mb-1">Operator</label>
								<select
									bind:value={condition.operator}
									class="w-full px-3 py-2 border-2 border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
								>
									{#each operators as op}
										<option value={op.value}>{op.label}</option>
									{/each}
								</select>
							</div>

							<!-- Value Input -->
							<div>
								<label class="block text-xs font-medium text-gray-600 mb-1">Value</label>
								<input
									type="text"
									bind:value={condition.value}
									placeholder={condition.operator === 'regex' ? 'Pattern' : 'Value'}
									class="w-full px-3 py-2 border-2 border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
								/>
							</div>
						</div>

						<!-- Remove Button -->
						{#if config.conditions.length > 1}
							<button
								on:click={() => removeCondition(condition.id)}
								class="mt-6 p-2 text-red-600 hover:bg-red-50 rounded-lg transition-colors"
								title="Remove condition"
							>
								<svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
									<path
										stroke-linecap="round"
										stroke-linejoin="round"
										stroke-width="2"
										d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
									/>
								</svg>
							</button>
						{/if}
					</div>

					{#if i < config.conditions.length - 1}
						<div class="mt-3 text-center">
							<span
								class="inline-block px-3 py-1 text-xs font-bold rounded-full"
								class:bg-blue-100={config.logic === 'AND'}
								class:text-blue-700={config.logic === 'AND'}
								class:bg-purple-100={config.logic === 'OR'}
								class:text-purple-700={config.logic === 'OR'}
							>
								{config.logic}
							</span>
						</div>
					{/if}
				</div>
			{/each}
		</div>

		<button
			on:click={addCondition}
			class="mt-3 w-full px-4 py-2 border-2 border-dashed border-gray-300 rounded-lg text-sm text-gray-600 hover:bg-gray-50 transition-colors"
		>
			+ Add Condition
		</button>
	</div>

	<!-- Errors -->
	{#if Object.keys(errors).length > 0}
		<div class="bg-red-50 border-2 border-red-200 rounded-lg p-3">
			<p class="text-sm font-semibold text-red-800 mb-1">Validation Errors:</p>
			<ul class="text-sm text-red-700 space-y-1">
				{#each Object.entries(errors) as [key, message]}
					<li>• {message}</li>
				{/each}
			</ul>
		</div>
	{/if}

	<!-- Preview -->
	{#if previewData}
		<div class="bg-gray-50 border-2 border-gray-200 rounded-lg p-4">
			<h4 class="text-sm font-semibold text-gray-900 mb-3">Preview (First 10 Rows)</h4>
			<div class="overflow-x-auto">
				<table class="min-w-full divide-y divide-gray-200 text-xs">
					<thead class="bg-gray-100">
						<tr>
							{#each previewData.headers as header}
								<th class="px-3 py-2 text-left font-bold text-gray-700">{header}</th>
							{/each}
						</tr>
					</thead>
					<tbody class="bg-white divide-y divide-gray-200">
						{#each previewData.rows.slice(0, 10) as row}
							<tr>
								{#each row as cell}
									<td class="px-3 py-2 text-gray-900">{cell || '-'}</td>
								{/each}
							</tr>
						{/each}
					</tbody>
				</table>
			</div>
		</div>
	{/if}

	<!-- Actions -->
	<div class="flex gap-3 pt-4 border-t-2 border-gray-200">
		<button
			on:click={onCancel}
			class="flex-1 px-4 py-2 border-2 border-gray-300 text-gray-700 font-semibold rounded-lg hover:bg-gray-50 transition-colors"
		>
			Cancel
		</button>
		<button
			on:click={handleTest}
			disabled={testing}
			class="flex-1 px-4 py-2 border-2 border-blue-300 text-blue-700 font-semibold rounded-lg hover:bg-blue-50 transition-colors disabled:opacity-50"
		>
			{testing ? 'Testing...' : 'Test Transformation'}
		</button>
		<button
			on:click={handleSave}
			class="flex-1 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white font-semibold rounded-lg transition-colors"
		>
			Save
		</button>
	</div>
</div>
