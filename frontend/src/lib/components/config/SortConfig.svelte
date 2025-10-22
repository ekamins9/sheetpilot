<script lang="ts">
	import { sortConfigSchema, type SortConfig } from '$lib/schemas/transformations';
	import { dndzone } from 'svelte-dnd-action';
	import { flip } from 'svelte/animate';

	export let config: SortConfig = { columns: [] };
	export let availableColumns: string[] = [];
	export let onSave: (config: SortConfig) => void;
	export let onCancel: () => void;
	export let onTest: (config: SortConfig) => Promise<any>;

	let errors: Record<string, string> = {};
	let testing = false;
	let previewData: { headers: string[]; rows: string[][] } | null = null;

	$: if (config.columns.length === 0) {
		addColumn();
	}

	function addColumn() {
		config.columns = [
			...config.columns,
			{ id: crypto.randomUUID(), column: '', direction: 'ASC' }
		];
	}

	function removeColumn(id: string) {
		config.columns = config.columns.filter((c) => c.id !== id);
	}

	function handleDnd(e: CustomEvent) {
		config.columns = e.detail.items;
	}

	function validate(): boolean {
		errors = {};
		try {
			sortConfigSchema.parse(config);
			return true;
		} catch (error: any) {
			error.errors.forEach((err: any) => {
				errors[err.path.join('.')] = err.message;
			});
			return false;
		}
	}

	async function handleTest() {
		if (!validate()) return;
		testing = true;
		try {
			previewData = await onTest(config);
		} finally {
			testing = false;
		}
	}
</script>

<div class="space-y-4">
	<div>
		<label class="block text-sm font-semibold text-gray-700 mb-2">Sort Columns</label>
		<div
			use:dndzone={{ items: config.columns, flipDurationMs: 200 }}
			on:consider={handleDnd}
			on:finalize={handleDnd}
			class="space-y-2"
		>
			{#each config.columns as column (column.id)}
				<div animate:flip={{ duration: 200 }} class="flex gap-2 items-center bg-gray-50 p-3 rounded-lg">
					<svg class="w-5 h-5 text-gray-400 cursor-move" fill="none" stroke="currentColor" viewBox="0 0 24 24">
						<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 8h16M4 16h16"/>
					</svg>
					<select bind:value={column.column} class="flex-1 px-3 py-2 border-2 border-gray-300 rounded-lg text-sm">
						<option value="">Select column</option>
						{#each availableColumns as col}
							<option value={col}>{col}</option>
						{/each}
					</select>
					<select bind:value={column.direction} class="px-3 py-2 border-2 border-gray-300 rounded-lg text-sm">
						<option value="ASC">↑ Ascending</option>
						<option value="DESC">↓ Descending</option>
					</select>
					{#if config.columns.length > 1}
						<button on:click={() => removeColumn(column.id)} class="p-2 text-red-600 hover:bg-red-50 rounded-lg">
							<svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
								<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
							</svg>
						</button>
					{/if}
				</div>
			{/each}
		</div>
		<button on:click={addColumn} class="mt-2 w-full px-4 py-2 border-2 border-dashed border-gray-300 rounded-lg text-sm text-gray-600 hover:bg-gray-50">
			+ Add Column
		</button>
	</div>

	{#if Object.keys(errors).length > 0}
		<div class="bg-red-50 border-2 border-red-200 rounded-lg p-3">
			<p class="text-sm font-semibold text-red-800">Errors:</p>
			<ul class="text-sm text-red-700">
				{#each Object.values(errors) as message}
					<li>• {message}</li>
				{/each}
			</ul>
		</div>
	{/if}

	{#if previewData}
		<div class="bg-gray-50 border-2 border-gray-200 rounded-lg p-4">
			<h4 class="text-sm font-semibold mb-3">Preview (First 5 Rows)</h4>
			<div class="overflow-x-auto">
				<table class="min-w-full text-xs">
					<thead class="bg-gray-100">
						<tr>
							{#each previewData.headers as header}
								<th class="px-3 py-2 text-left font-bold">{header}</th>
							{/each}
						</tr>
					</thead>
					<tbody>
						{#each previewData.rows.slice(0, 5) as row}
							<tr>
								{#each row as cell}
									<td class="px-3 py-2">{cell || '-'}</td>
								{/each}
							</tr>
						{/each}
					</tbody>
				</table>
			</div>
		</div>
	{/if}

	<div class="flex gap-3 pt-4 border-t-2">
		<button on:click={onCancel} class="flex-1 px-4 py-2 border-2 border-gray-300 rounded-lg">Cancel</button>
		<button on:click={handleTest} disabled={testing} class="flex-1 px-4 py-2 border-2 border-blue-300 text-blue-700 rounded-lg">
			{testing ? 'Testing...' : 'Test'}
		</button>
		<button on:click={() => validate() && onSave(config)} class="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg">Save</button>
	</div>
</div>
