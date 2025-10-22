<script lang="ts">
	export let headers: string[] = [];
	export let rows: string[][] = [];
	export let maxRows: number = 100;
	export let showLineNumbers: boolean = true;

	$: displayRows = rows.slice(0, maxRows);
	$: hasMore = rows.length > maxRows;
</script>

<div class="overflow-x-auto">
	<table class="min-w-full text-sm border-collapse">
		<thead class="bg-gray-100 sticky top-0">
			<tr>
				{#if showLineNumbers}
					<th class="px-3 py-2 text-left font-bold text-gray-600 border-2 border-gray-300">#</th>
				{/if}
				{#each headers as header}
					<th class="px-3 py-2 text-left font-bold text-gray-900 border-2 border-gray-300">
						{header}
					</th>
				{/each}
			</tr>
		</thead>
		<tbody>
			{#each displayRows as row, i}
				<tr class="hover:bg-gray-50 transition-colors">
					{#if showLineNumbers}
						<td class="px-3 py-2 text-gray-500 border-2 border-gray-200 font-mono text-xs">
							{i + 1}
						</td>
					{/if}
					{#each row as cell}
						<td class="px-3 py-2 border-2 border-gray-200">
							{cell || '-'}
						</td>
					{/each}
				</tr>
			{/each}
		</tbody>
	</table>

	{#if hasMore}
		<div class="text-center py-4 text-sm text-gray-600 bg-gray-50 border-2 border-t-0 border-gray-200">
			Showing first {maxRows} of {rows.length.toLocaleString()} rows
		</div>
	{/if}
</div>
