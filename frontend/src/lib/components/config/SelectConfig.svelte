<script lang="ts">
	import { selectConfigSchema, type SelectConfig } from '$lib/schemas/transformations';
	import { dndzone } from 'svelte-dnd-action';

	export let config: SelectConfig = { columns: [] };
	export let availableColumns: string[] = [];
	export let onSave: (config: SelectConfig) => void;
	export let onCancel: () => void;

	let searchQuery = '';
	let selectedColumns: Set<string> = new Set(config.columns.map(c => c.name));

	$: filteredColumns = availableColumns.filter(col =>
		col.toLowerCase().includes(searchQuery.toLowerCase())
	);

	function toggleColumn(col: string) {
		if (selectedColumns.has(col)) {
			selectedColumns.delete(col);
			config.columns = config.columns.filter(c => c.name !== col);
		} else {
			selectedColumns.add(col);
			config.columns = [...config.columns, { name: col }];
		}
		selectedColumns = new Set(selectedColumns);
	}

	function toggleAll() {
		if (selectedColumns.size === availableColumns.length) {
			selectedColumns.clear();
			config.columns = [];
		} else {
			selectedColumns = new Set(availableColumns);
			config.columns = availableColumns.map(name => ({ name }));
		}
	}

	function handleDnd(e: CustomEvent) {
		config.columns = e.detail.items;
	}
</script>

<div class="space-y-4">
	<div>
		<div class="flex justify-between items-center mb-2">
			<label class="text-sm font-semibold text-gray-700">Select Columns</label>
			<button on:click={toggleAll} class="text-xs text-blue-600 hover:text-blue-800">
				{selectedColumns.size === availableColumns.length ? 'Deselect All' : 'Select All'}
			</button>
		</div>
		<input
			type="text"
			bind:value={searchQuery}
			placeholder="Search columns..."
			class="w-full px-3 py-2 border-2 border-gray-300 rounded-lg mb-2"
		/>
		<div class="max-h-64 overflow-y-auto border-2 border-gray-200 rounded-lg">
			{#each filteredColumns as col}
				<label class="flex items-center gap-2 px-3 py-2 hover:bg-gray-50 cursor-pointer">
					<input type="checkbox" checked={selectedColumns.has(col)} on:change={() => toggleColumn(col)} />
					<span>{col}</span>
				</label>
			{/each}
		</div>
	</div>

	{#if config.columns.length > 0}
		<div>
			<label class="block text-sm font-semibold text-gray-700 mb-2">Selected Columns (Drag to Reorder)</label>
			<div use:dndzone={{ items: config.columns, flipDurationMs: 200 }} on:consider={handleDnd} on:finalize={handleDnd} class="space-y-2">
				{#each config.columns as column (column.name)}
					<div class="flex gap-2 items-center bg-blue-50 p-3 rounded-lg">
						<svg class="w-5 h-5 text-gray-400 cursor-move" fill="none" stroke="currentColor" viewBox="0 0 24 24">
							<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 8h16M4 16h16"/>
						</svg>
						<span class="flex-1 font-medium">{column.name}</span>
						<input
							type="text"
							bind:value={column.rename}
							placeholder="Rename (optional)"
							class="px-3 py-1 border-2 border-gray-300 rounded text-sm"
						/>
					</div>
				{/each}
			</div>
		</div>
	{/if}

	<div class="flex gap-3 pt-4 border-t-2">
		<button on:click={onCancel} class="flex-1 px-4 py-2 border-2 border-gray-300 rounded-lg">Cancel</button>
		<button on:click={() => onSave(config)} class="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg">Save</button>
	</div>
</div>
