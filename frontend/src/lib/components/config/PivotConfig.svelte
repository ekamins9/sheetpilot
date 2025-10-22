<script lang="ts">
	import { pivotConfigSchema, type PivotConfig } from '$lib/schemas/transformations';

	export let config: PivotConfig = { indexColumns: [], valueColumns: [], aggregation: 'sum' };
	export let availableColumns: string[] = [];
	export let onSave: (config: PivotConfig) => void;
	export let onCancel: () => void;
	export let onTest: (config: PivotConfig) => Promise<any>;

	let errors: Record<string, string> = {};
	let testing = false;
	let previewData: { headers: string[]; rows: string[][] } | null = null;

	const aggregations = [
		{ value: 'sum', label: 'Sum', icon: 'Σ' },
		{ value: 'avg', label: 'Average', icon: '≈' },
		{ value: 'count', label: 'Count', icon: '#' },
		{ value: 'min', label: 'Minimum', icon: '↓' },
		{ value: 'max', label: 'Maximum', icon: '↑' }
	];

	function toggleColumn(list: string[], col: string) {
		if (list.includes(col)) {
			return list.filter(c => c !== col);
		}
		return [...list, col];
	}

	function validate(): boolean {
		errors = {};
		try {
			pivotConfigSchema.parse(config);
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
		<label class="block text-sm font-semibold text-gray-700 mb-2">Index Columns (Group By)</label>
		<div class="grid grid-cols-2 gap-2">
			{#each availableColumns as col}
				<label class="flex items-center gap-2 px-3 py-2 border-2 rounded-lg cursor-pointer"
					class:border-blue-600={config.indexColumns.includes(col)}
					class:bg-blue-50={config.indexColumns.includes(col)}>
					<input type="checkbox" checked={config.indexColumns.includes(col)} on:change={() => config.indexColumns = toggleColumn(config.indexColumns, col)} />
					<span class="text-sm">{col}</span>
				</label>
			{/each}
		</div>
	</div>

	<div>
		<label class="block text-sm font-semibold text-gray-700 mb-2">Value Columns (To Aggregate)</label>
		<div class="grid grid-cols-2 gap-2">
			{#each availableColumns as col}
				<label class="flex items-center gap-2 px-3 py-2 border-2 rounded-lg cursor-pointer"
					class:border-purple-600={config.valueColumns.includes(col)}
					class:bg-purple-50={config.valueColumns.includes(col)}>
					<input type="checkbox" checked={config.valueColumns.includes(col)} on:change={() => config.valueColumns = toggleColumn(config.valueColumns, col)} />
					<span class="text-sm">{col}</span>
				</label>
			{/each}
		</div>
	</div>

	<div>
		<label class="block text-sm font-semibold text-gray-700 mb-2">Aggregation Function</label>
		<div class="grid grid-cols-5 gap-2">
			{#each aggregations as agg}
				<button
					on:click={() => config.aggregation = agg.value}
					class="p-3 rounded-lg border-2 transition-colors"
					class:border-blue-600={config.aggregation === agg.value}
					class:bg-blue-50={config.aggregation === agg.value}
					class:border-gray-300={config.aggregation !== agg.value}
				>
					<div class="text-2xl mb-1">{agg.icon}</div>
					<div class="text-xs font-semibold">{agg.label}</div>
				</button>
			{/each}
		</div>
	</div>

	{#if Object.keys(errors).length > 0}
		<div class="bg-red-50 border-2 border-red-200 rounded-lg p-3">
			<ul class="text-sm text-red-700">
				{#each Object.values(errors) as message}
					<li>• {message}</li>
				{/each}
			</ul>
		</div>
	{/if}

	{#if previewData}
		<div class="bg-gray-50 border-2 border-gray-200 rounded-lg p-4">
			<h4 class="text-sm font-semibold mb-3">Preview Pivoted Table</h4>
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
						{#each previewData.rows as row}
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
