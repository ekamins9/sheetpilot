<script lang="ts">
	import { joinConfigSchema, type JoinConfig } from '$lib/schemas/transformations';

	export let config: JoinConfig;
	export let availableColumns: string[] = [];
	export let onSave: (config: JoinConfig) => void;
	export let onCancel: () => void;
	export let onTest: (config: JoinConfig) => Promise<any>;

	let errors: Record<string, string> = {};
	let testing = false;
	let previewData: { headers: string[]; rows: string[][] } | null = null;

	const joinTypes = [
		{ value: 'inner', label: 'Inner Join', desc: 'Only matching rows from both tables' },
		{ value: 'left', label: 'Left Join', desc: 'All rows from left, matching from right' },
		{ value: 'right', label: 'Right Join', desc: 'All rows from right, matching from left' },
		{ value: 'outer', label: 'Outer Join', desc: 'All rows from both tables' }
	];

	function validate(): boolean {
		errors = {};
		try {
			joinConfigSchema.parse(config);
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
	<div class="grid grid-cols-2 gap-4">
		<div>
			<label class="block text-sm font-semibold text-gray-700 mb-2">Left Spreadsheet ID</label>
			<input type="number" bind:value={config.leftSpreadsheetId} class="w-full px-3 py-2 border-2 border-gray-300 rounded-lg" />
		</div>
		<div>
			<label class="block text-sm font-semibold text-gray-700 mb-2">Right Spreadsheet ID</label>
			<input type="number" bind:value={config.rightSpreadsheetId} class="w-full px-3 py-2 border-2 border-gray-300 rounded-lg" />
		</div>
	</div>

	<div>
		<label class="block text-sm font-semibold text-gray-700 mb-2">Join Type</label>
		<div class="grid grid-cols-2 gap-2">
			{#each joinTypes as type}
				<button
					on:click={() => config.joinType = type.value}
					class="p-3 rounded-lg border-2 text-left transition-colors"
					class:border-blue-600={config.joinType === type.value}
					class:bg-blue-50={config.joinType === type.value}
					class:border-gray-300={config.joinType !== type.value}
				>
					<div class="font-semibold text-sm">{type.label}</div>
					<div class="text-xs text-gray-600 mt-1">{type.desc}</div>
				</button>
			{/each}
		</div>
	</div>

	<div class="grid grid-cols-2 gap-4">
		<div>
			<label class="block text-sm font-semibold text-gray-700 mb-2">Left Join Key</label>
			<input type="text" bind:value={config.leftKey} placeholder="e.g., customer_id" class="w-full px-3 py-2 border-2 border-gray-300 rounded-lg" />
		</div>
		<div>
			<label class="block text-sm font-semibold text-gray-700 mb-2">Right Join Key</label>
			<input type="text" bind:value={config.rightKey} placeholder="e.g., id" class="w-full px-3 py-2 border-2 border-gray-300 rounded-lg" />
		</div>
	</div>

	<div class="bg-gray-50 p-3 rounded-lg">
		<label class="block text-xs font-semibold text-gray-700 mb-2">Column Conflict Resolution (Optional)</label>
		<div class="grid grid-cols-2 gap-2">
			<input bind:value={config.leftPrefix} placeholder="Left prefix" class="px-3 py-2 border-2 border-gray-300 rounded-lg text-sm" />
			<input bind:value={config.rightPrefix} placeholder="Right prefix" class="px-3 py-2 border-2 border-gray-300 rounded-lg text-sm" />
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
			<h4 class="text-sm font-semibold mb-3">Preview (First 10 Rows)</h4>
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
						{#each previewData.rows.slice(0, 10) as row}
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
