<script lang="ts">
	import type { SpreadsheetResponse } from '$lib/types';

	export let show = false;
	export let onConfirm: (spreadsheetIds: number[]) => void;
	export let onCancel: () => void;

	const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

	let spreadsheets: SpreadsheetResponse[] = [];
	let selectedIds: number[] = [];
	let loading = true;
	let searchQuery = '';

	$: filteredSpreadsheets = spreadsheets.filter(s =>
		s.name.toLowerCase().includes(searchQuery.toLowerCase())
	);

	$: estimatedTime = calculateEstimatedTime();
	$: estimatedCost = calculateEstimatedCost();

	async function loadSpreadsheets() {
		loading = true;
		try {
			const response = await fetch(`${API_URL}/spreadsheets?page=0&size=100`);
			if (!response.ok) throw new Error('Failed to load spreadsheets');
			const data = await response.json();
			spreadsheets = data.content || [];
		} catch (error) {
			console.error('Failed to load spreadsheets:', error);
		} finally {
			loading = false;
		}
	}

	function toggleSelection(id: number) {
		if (selectedIds.includes(id)) {
			selectedIds = selectedIds.filter(sid => sid !== id);
		} else {
			selectedIds = [...selectedIds, id];
		}
	}

	function selectAll() {
		selectedIds = filteredSpreadsheets.map(s => s.id);
	}

	function deselectAll() {
		selectedIds = [];
	}

	function calculateEstimatedTime(): string {
		const selected = spreadsheets.filter(s => selectedIds.includes(s.id));
		const totalRows = selected.reduce((sum, s) => sum + s.rowCount, 0);

		// Estimate: 1000 rows per second
		const seconds = Math.ceil(totalRows / 1000);

		if (seconds < 60) return `~${seconds}s`;
		if (seconds < 3600) return `~${Math.ceil(seconds / 60)}m`;
		return `~${Math.ceil(seconds / 3600)}h`;
	}

	function calculateEstimatedCost(): number {
		const selected = spreadsheets.filter(s => selectedIds.includes(s.id));
		const totalRows = selected.reduce((sum, s) => sum + s.rowCount, 0);

		// Estimate: $0.001 per 1000 rows
		return totalRows / 1000 * 0.001;
	}

	function handleConfirm() {
		if (selectedIds.length === 0) return;
		onConfirm(selectedIds);
	}

	$: if (show) {
		loadSpreadsheets();
	}
</script>

{#if show}
	<div class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" on:click={onCancel}>
		<div class="bg-white rounded-xl shadow-2xl w-full max-w-3xl max-h-[80vh] flex flex-col" on:click|stopPropagation>
			<!-- Header -->
			<div class="px-6 py-4 border-b-2 border-gray-200">
				<div class="flex items-center justify-between">
					<h2 class="text-2xl font-bold text-gray-900">Select Target Spreadsheets</h2>
					<button on:click={onCancel} class="p-2 hover:bg-gray-100 rounded-lg transition-colors">
						<svg class="w-6 h-6 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
							<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
						</svg>
					</button>
				</div>
				<p class="text-sm text-gray-600 mt-1">Choose one or more spreadsheets to process</p>
			</div>

			<!-- Search and Actions -->
			<div class="px-6 py-3 border-b-2 border-gray-200 bg-gray-50">
				<div class="flex gap-3">
					<input
						type="text"
						bind:value={searchQuery}
						placeholder="Search spreadsheets..."
						class="flex-1 px-3 py-2 border-2 border-gray-300 rounded-lg text-sm focus:outline-none focus:border-blue-500"
					/>
					<button on:click={selectAll} class="px-3 py-2 text-sm border-2 border-gray-300 rounded-lg hover:bg-gray-100 transition-colors">
						Select All
					</button>
					<button on:click={deselectAll} class="px-3 py-2 text-sm border-2 border-gray-300 rounded-lg hover:bg-gray-100 transition-colors">
						Clear
					</button>
				</div>
				<div class="text-xs text-gray-600 mt-2">
					{selectedIds.length} selected · Est. time: {estimatedTime} · Est. cost: ${estimatedCost.toFixed(4)}
				</div>
			</div>

			<!-- Spreadsheet List -->
			<div class="flex-1 overflow-y-auto p-6">
				{#if loading}
					<div class="flex items-center justify-center h-40">
						<div class="w-8 h-8 border-4 border-blue-600 border-t-transparent rounded-full animate-spin"></div>
					</div>
				{:else if filteredSpreadsheets.length === 0}
					<div class="text-center text-gray-500 py-12">
						<div class="text-4xl mb-3">📊</div>
						<p>{searchQuery ? 'No spreadsheets match your search' : 'No spreadsheets available'}</p>
					</div>
				{:else}
					<div class="grid gap-3">
						{#each filteredSpreadsheets as spreadsheet}
							<label
								class="flex items-center gap-4 p-4 border-2 rounded-lg cursor-pointer transition-all"
								class:border-blue-600={selectedIds.includes(spreadsheet.id)}
								class:bg-blue-50={selectedIds.includes(spreadsheet.id)}
								class:border-gray-200={!selectedIds.includes(spreadsheet.id)}
								class:hover:border-gray-300={!selectedIds.includes(spreadsheet.id)}
							>
								<input
									type="checkbox"
									checked={selectedIds.includes(spreadsheet.id)}
									on:change={() => toggleSelection(spreadsheet.id)}
									class="w-5 h-5"
								/>
								<div class="flex-1">
									<div class="font-semibold text-gray-900">{spreadsheet.name}</div>
									<div class="text-xs text-gray-600 mt-1 flex gap-4">
										<span>📋 {spreadsheet.rowCount.toLocaleString()} rows</span>
										<span>📊 {spreadsheet.columnCount} columns</span>
										<span>📦 {(spreadsheet.fileSize / 1024).toFixed(1)} KB</span>
										<span>📅 {new Date(spreadsheet.uploadedAt).toLocaleDateString()}</span>
									</div>
								</div>
								{#if selectedIds.includes(spreadsheet.id)}
									<div class="w-6 h-6 bg-blue-600 text-white rounded-full flex items-center justify-center">
										<svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
											<path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M5 13l4 4L19 7"/>
										</svg>
									</div>
								{/if}
							</label>
						{/each}
					</div>
				{/if}
			</div>

			<!-- Footer -->
			<div class="px-6 py-4 border-t-2 border-gray-200 bg-gray-50">
				<div class="flex gap-3">
					<button
						on:click={onCancel}
						class="flex-1 px-4 py-3 border-2 border-gray-300 rounded-lg font-semibold text-gray-700 hover:bg-gray-100 transition-colors"
					>
						Cancel
					</button>
					<button
						on:click={handleConfirm}
						disabled={selectedIds.length === 0}
						class="flex-1 px-4 py-3 bg-blue-600 text-white rounded-lg font-semibold hover:bg-blue-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
					>
						Run Pipeline ({selectedIds.length})
					</button>
				</div>
			</div>
		</div>
	</div>
{/if}
