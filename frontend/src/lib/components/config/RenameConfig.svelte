<script lang="ts">
	import { renameConfigSchema, type RenameConfig } from '$lib/schemas/transformations';

	export let config: RenameConfig = { mappings: {} };
	export let availableColumns: string[] = [];
	export let onSave: (config: RenameConfig) => void;
	export let onCancel: () => void;

	let errors: Record<string, string> = {};
	let bulkPrefix = '';
	let bulkSuffix = '';

	$: mappingEntries = Object.entries(config.mappings);

	function addMapping() {
		config.mappings = { ...config.mappings, '': '' };
	}

	function removeMapping(oldName: string) {
		const { [oldName]: _, ...rest } = config.mappings;
		config.mappings = rest;
	}

	function applyBulkRename() {
		const newMappings: Record<string, string> = {};
		availableColumns.forEach(col => {
			newMappings[col] = `${bulkPrefix}${col}${bulkSuffix}`;
		});
		config.mappings = newMappings;
	}

	function validate(): boolean {
		errors = {};
		try {
			renameConfigSchema.parse(config);
			return true;
		} catch (error: any) {
			error.errors.forEach((err: any) => {
				errors[err.path.join('.')] = err.message;
			});
			return false;
		}
	}
</script>

<div class="space-y-4">
	<div>
		<label class="block text-sm font-semibold text-gray-700 mb-2">Column Rename Mappings</label>
		<div class="space-y-2">
			{#each mappingEntries as [oldName, newName]}
				<div class="flex gap-2">
					<select bind:value={oldName} on:change={(e) => {
						const { [oldName]: val, ...rest } = config.mappings;
						config.mappings = { ...rest, [e.currentTarget.value]: val };
					}} class="flex-1 px-3 py-2 border-2 border-gray-300 rounded-lg text-sm">
						<option value="">Select column</option>
						{#each availableColumns as col}
							<option value={col}>{col}</option>
						{/each}
					</select>
					<span class="flex items-center text-gray-400">→</span>
					<input
						type="text"
						value={newName}
						on:input={(e) => {
							config.mappings = { ...config.mappings, [oldName]: e.currentTarget.value };
						}}
						placeholder="New name"
						class="flex-1 px-3 py-2 border-2 border-gray-300 rounded-lg text-sm"
					/>
					<button on:click={() => removeMapping(oldName)} class="p-2 text-red-600 hover:bg-red-50 rounded-lg">
						<svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
							<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
						</svg>
					</button>
				</div>
			{/each}
		</div>
		<button on:click={addMapping} class="mt-2 w-full px-4 py-2 border-2 border-dashed border-gray-300 rounded-lg text-sm text-gray-600 hover:bg-gray-50">
			+ Add Mapping
		</button>
	</div>

	<div class="bg-blue-50 border-2 border-blue-200 rounded-lg p-4">
		<label class="block text-sm font-semibold text-gray-700 mb-2">Bulk Rename Pattern</label>
		<div class="flex gap-2 mb-2">
			<input bind:value={bulkPrefix} placeholder="Prefix" class="flex-1 px-3 py-2 border-2 border-gray-300 rounded-lg text-sm" />
			<input bind:value={bulkSuffix} placeholder="Suffix" class="flex-1 px-3 py-2 border-2 border-gray-300 rounded-lg text-sm" />
		</div>
		<button on:click={applyBulkRename} class="w-full px-4 py-2 bg-blue-600 text-white rounded-lg text-sm">
			Apply to All Columns
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

	<div class="flex gap-3 pt-4 border-t-2">
		<button on:click={onCancel} class="flex-1 px-4 py-2 border-2 border-gray-300 rounded-lg">Cancel</button>
		<button on:click={() => validate() && onSave(config)} class="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg">Save</button>
	</div>
</div>
