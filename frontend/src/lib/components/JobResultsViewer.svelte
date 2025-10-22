<script lang="ts">
	import { goto } from '$app/navigation';
	import { toastStore } from '$lib/stores/toast';
	import type { Job } from '$lib/types';
	import DataTable from './DataTable.svelte';

	export let job: Job;

	const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

	let activeTab: 'overview' | 'preview' | 'logs' | 'errors' = 'overview';

	$: result = job.result;
	$: warnings = result?.warnings || [];
	$: errors = job.logs.filter(log => log.toLowerCase().includes('error'));

	function formatDuration(ms: number): string {
		const seconds = Math.floor(ms / 1000);
		const minutes = Math.floor(seconds / 60);
		const hours = Math.floor(minutes / 60);

		if (hours > 0) return `${hours}h ${minutes % 60}m ${seconds % 60}s`;
		if (minutes > 0) return `${minutes}m ${seconds % 60}s`;
		return `${seconds}s`;
	}

	async function downloadCSV() {
		try {
			const response = await fetch(`${API_URL}/jobs/${job.id}/download?format=csv`);
			if (!response.ok) throw new Error('Download failed');

			const blob = await response.blob();
			const url = window.URL.createObjectURL(blob);
			const a = document.createElement('a');
			a.href = url;
			a.download = `job-${job.id}-result.csv`;
			document.body.appendChild(a);
			a.click();
			window.URL.revokeObjectURL(url);
			document.body.removeChild(a);

			toastStore.add('success', 'CSV downloaded successfully');
		} catch (error) {
			console.error('Download failed:', error);
			toastStore.add('error', 'Failed to download CSV');
		}
	}

	async function downloadExcel() {
		try {
			const response = await fetch(`${API_URL}/jobs/${job.id}/download?format=xlsx`);
			if (!response.ok) throw new Error('Download failed');

			const blob = await response.blob();
			const url = window.URL.createObjectURL(blob);
			const a = document.createElement('a');
			a.href = url;
			a.download = `job-${job.id}-result.xlsx`;
			document.body.appendChild(a);
			a.click();
			window.URL.revokeObjectURL(url);
			document.body.removeChild(a);

			toastStore.add('success', 'Excel file downloaded successfully');
		} catch (error) {
			console.error('Download failed:', error);
			toastStore.add('error', 'Failed to download Excel file');
		}
	}

	async function saveAsSpreadsheet() {
		try {
			const response = await fetch(`${API_URL}/jobs/${job.id}/save-result`, {
				method: 'POST'
			});

			if (!response.ok) throw new Error('Save failed');

			const data = await response.json();
			toastStore.add('success', 'Saved as new spreadsheet');
			goto(`/spreadsheets/${data.spreadsheetId}`);
		} catch (error) {
			console.error('Save failed:', error);
			toastStore.add('error', 'Failed to save as spreadsheet');
		}
	}

	async function runAgain() {
		goto(`/pipelines/${job.pipelineId}/edit`);
	}

	function editPipeline() {
		goto(`/pipelines/${job.pipelineId}/edit`);
	}

	async function shareResults() {
		const shareUrl = `${window.location.origin}/jobs/${job.id}`;
		try {
			await navigator.clipboard.writeText(shareUrl);
			toastStore.add('success', 'Share link copied to clipboard');
		} catch (error) {
			toastStore.add('info', `Share URL: ${shareUrl}`);
		}
	}

	function printResults() {
		window.print();
	}
</script>

<div class="bg-white rounded-xl shadow-lg overflow-hidden print:shadow-none">
	<!-- Header -->
	<div class="bg-gradient-to-r from-green-600 to-blue-600 text-white p-6 print:bg-green-600">
		<div class="flex items-center justify-between">
			<div>
				<div class="flex items-center gap-3 mb-2">
					<div class="text-4xl">✅</div>
					<h2 class="text-2xl font-bold">Job Completed Successfully</h2>
				</div>
				<p class="text-sm opacity-90">
					Pipeline: {job.pipelineName} · Execution time: {formatDuration(job.executionTimeMs)}
				</p>
			</div>
			<button on:click={printResults} class="p-2 hover:bg-white/20 rounded-lg transition-colors print:hidden">
				<svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
					<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 17h2a2 2 0 002-2v-4a2 2 0 00-2-2H5a2 2 0 00-2 2v4a2 2 0 002 2h2m2 4h6a2 2 0 002-2v-4a2 2 0 00-2-2H9a2 2 0 00-2 2v4a2 2 0 002 2zm8-12V5a2 2 0 00-2-2H9a2 2 0 00-2 2v4h10z"/>
				</svg>
			</button>
		</div>
	</div>

	<!-- Action Buttons -->
	<div class="p-6 border-b-2 border-gray-200 bg-gray-50 print:hidden">
		<div class="flex flex-wrap gap-3">
			<button on:click={downloadCSV} class="px-4 py-2 bg-blue-600 text-white rounded-lg font-semibold hover:bg-blue-700 transition-colors flex items-center gap-2">
				<svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
					<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4"/>
				</svg>
				Download CSV
			</button>
			<button on:click={downloadExcel} class="px-4 py-2 bg-green-600 text-white rounded-lg font-semibold hover:bg-green-700 transition-colors flex items-center gap-2">
				<svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
					<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4"/>
				</svg>
				Download Excel
			</button>
			<button on:click={saveAsSpreadsheet} class="px-4 py-2 border-2 border-purple-600 text-purple-600 rounded-lg font-semibold hover:bg-purple-50 transition-colors flex items-center gap-2">
				<svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
					<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7H5a2 2 0 00-2 2v9a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-3m-1 4l-3 3m0 0l-3-3m3 3V4"/>
				</svg>
				Save as Spreadsheet
			</button>
			<button on:click={runAgain} class="px-4 py-2 border-2 border-gray-300 rounded-lg font-semibold hover:bg-gray-50 transition-colors flex items-center gap-2">
				<svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
					<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"/>
				</svg>
				Run Again
			</button>
			<button on:click={editPipeline} class="px-4 py-2 border-2 border-gray-300 rounded-lg font-semibold hover:bg-gray-50 transition-colors flex items-center gap-2">
				<svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
					<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/>
				</svg>
				Edit Pipeline
			</button>
			<button on:click={shareResults} class="px-4 py-2 border-2 border-gray-300 rounded-lg font-semibold hover:bg-gray-50 transition-colors flex items-center gap-2">
				<svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
					<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8.684 13.342C8.886 12.938 9 12.482 9 12c0-.482-.114-.938-.316-1.342m0 2.684a3 3 0 110-2.684m0 2.684l6.632 3.316m-6.632-6l6.632-3.316m0 0a3 3 0 105.367-2.684 3 3 0 00-5.367 2.684zm0 9.316a3 3 0 105.368 2.684 3 3 0 00-5.368-2.684z"/>
				</svg>
				Share
			</button>
		</div>
	</div>

	<!-- Tabs -->
	<div class="border-b-2 border-gray-200 print:hidden">
		<div class="flex">
			<button
				on:click={() => activeTab = 'overview'}
				class="px-6 py-3 font-semibold transition-colors border-b-4"
				class:border-blue-600={activeTab === 'overview'}
				class:text-blue-600={activeTab === 'overview'}
				class:border-transparent={activeTab !== 'overview'}
				class:text-gray-600={activeTab !== 'overview'}
			>
				Overview
			</button>
			<button
				on:click={() => activeTab = 'preview'}
				class="px-6 py-3 font-semibold transition-colors border-b-4"
				class:border-blue-600={activeTab === 'preview'}
				class:text-blue-600={activeTab === 'preview'}
				class:border-transparent={activeTab !== 'preview'}
				class:text-gray-600={activeTab !== 'preview'}
			>
				Preview
			</button>
			<button
				on:click={() => activeTab = 'logs'}
				class="px-6 py-3 font-semibold transition-colors border-b-4"
				class:border-blue-600={activeTab === 'logs'}
				class:text-blue-600={activeTab === 'logs'}
				class:border-transparent={activeTab !== 'logs'}
				class:text-gray-600={activeTab !== 'logs'}
			>
				Logs
			</button>
			{#if errors.length > 0 || warnings.length > 0}
				<button
					on:click={() => activeTab = 'errors'}
					class="px-6 py-3 font-semibold transition-colors border-b-4 flex items-center gap-2"
					class:border-red-600={activeTab === 'errors'}
					class:text-red-600={activeTab === 'errors'}
					class:border-transparent={activeTab !== 'errors'}
					class:text-gray-600={activeTab !== 'errors'}
				>
					Warnings
					<span class="px-2 py-0.5 bg-red-100 text-red-700 rounded-full text-xs font-bold">
						{errors.length + warnings.length}
					</span>
				</button>
			{/if}
		</div>
	</div>

	<!-- Tab Content -->
	<div class="p-6">
		{#if activeTab === 'overview'}
			<div class="space-y-6 print:space-y-4">
				<!-- Summary Statistics -->
				{#if result}
					<div class="grid grid-cols-2 gap-6">
						<!-- Before/After Comparison -->
						<div class="bg-gray-50 rounded-lg p-6 print:break-inside-avoid">
							<h3 class="font-bold text-lg text-gray-900 mb-4">Before vs After</h3>
							<div class="space-y-3">
								<div class="flex items-center justify-between">
									<span class="text-gray-700">Rows</span>
									<div class="flex items-center gap-3">
										<span class="font-mono text-gray-600">{result.beforeRowCount.toLocaleString()}</span>
										<svg class="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
											<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/>
										</svg>
										<span class="font-mono font-bold text-blue-600">{result.afterRowCount.toLocaleString()}</span>
										<span class="text-xs px-2 py-1 rounded-full"
											class:bg-green-100={result.afterRowCount >= result.beforeRowCount}
											class:text-green-700={result.afterRowCount >= result.beforeRowCount}
											class:bg-red-100={result.afterRowCount < result.beforeRowCount}
											class:text-red-700={result.afterRowCount < result.beforeRowCount}>
											{result.afterRowCount >= result.beforeRowCount ? '+' : ''}{result.afterRowCount - result.beforeRowCount}
										</span>
									</div>
								</div>
								<div class="flex items-center justify-between">
									<span class="text-gray-700">Columns</span>
									<div class="flex items-center gap-3">
										<span class="font-mono text-gray-600">{result.beforeColumnCount}</span>
										<svg class="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
											<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/>
										</svg>
										<span class="font-mono font-bold text-blue-600">{result.afterColumnCount}</span>
										<span class="text-xs px-2 py-1 rounded-full"
											class:bg-green-100={result.afterColumnCount >= result.beforeColumnCount}
											class:text-green-700={result.afterColumnCount >= result.beforeColumnCount}
											class:bg-red-100={result.afterColumnCount < result.beforeColumnCount}
											class:text-red-700={result.afterColumnCount < result.beforeColumnCount}>
											{result.afterColumnCount >= result.beforeColumnCount ? '+' : ''}{result.afterColumnCount - result.beforeColumnCount}
										</span>
									</div>
								</div>
							</div>
						</div>

						<!-- Data Quality Metrics -->
						<div class="bg-gray-50 rounded-lg p-6 print:break-inside-avoid">
							<h3 class="font-bold text-lg text-gray-900 mb-4">Data Quality</h3>
							<div class="space-y-3">
								<div class="flex items-center justify-between">
									<span class="text-gray-700">Quality Score</span>
									<div class="flex items-center gap-2">
										<div class="w-24 h-2 bg-gray-200 rounded-full overflow-hidden">
											<div class="h-full bg-gradient-to-r from-green-500 to-blue-500" style="width: {result.dataQualityScore}%"></div>
										</div>
										<span class="font-bold text-blue-600">{result.dataQualityScore}%</span>
									</div>
								</div>
								{#if result.nullValuesRemoved > 0}
									<div class="flex items-center justify-between">
										<span class="text-gray-700">Null Values Removed</span>
										<span class="font-mono font-bold text-gray-900">{result.nullValuesRemoved.toLocaleString()}</span>
									</div>
								{/if}
								{#if result.duplicatesRemoved > 0}
									<div class="flex items-center justify-between">
										<span class="text-gray-700">Duplicates Removed</span>
										<span class="font-mono font-bold text-gray-900">{result.duplicatesRemoved.toLocaleString()}</span>
									</div>
								{/if}
								{#if result.recordsFiltered > 0}
									<div class="flex items-center justify-between">
										<span class="text-gray-700">Records Filtered</span>
										<span class="font-mono font-bold text-gray-900">{result.recordsFiltered.toLocaleString()}</span>
									</div>
								{/if}
							</div>
						</div>
					</div>

					<!-- Column Changes -->
					{#if result.columnsAdded.length > 0 || result.columnsRemoved.length > 0 || Object.keys(result.columnsRenamed).length > 0}
						<div class="bg-gray-50 rounded-lg p-6 print:break-inside-avoid">
							<h3 class="font-bold text-lg text-gray-900 mb-4">Column Changes</h3>
							<div class="grid grid-cols-3 gap-4">
								{#if result.columnsAdded.length > 0}
									<div>
										<div class="text-sm font-semibold text-green-700 mb-2">Added ({result.columnsAdded.length})</div>
										<div class="space-y-1">
											{#each result.columnsAdded as col}
												<div class="text-xs px-2 py-1 bg-green-100 text-green-800 rounded">+ {col}</div>
											{/each}
										</div>
									</div>
								{/if}
								{#if result.columnsRemoved.length > 0}
									<div>
										<div class="text-sm font-semibold text-red-700 mb-2">Removed ({result.columnsRemoved.length})</div>
										<div class="space-y-1">
											{#each result.columnsRemoved as col}
												<div class="text-xs px-2 py-1 bg-red-100 text-red-800 rounded">- {col}</div>
											{/each}
										</div>
									</div>
								{/if}
								{#if Object.keys(result.columnsRenamed).length > 0}
									<div>
										<div class="text-sm font-semibold text-blue-700 mb-2">Renamed ({Object.keys(result.columnsRenamed).length})</div>
										<div class="space-y-1">
											{#each Object.entries(result.columnsRenamed) as [oldName, newName]}
												<div class="text-xs px-2 py-1 bg-blue-100 text-blue-800 rounded">
													{oldName} → {newName}
												</div>
											{/each}
										</div>
									</div>
								{/if}
							</div>
						</div>
					{/if}
				{/if}

				<!-- Step Breakdown -->
				<div class="bg-gray-50 rounded-lg p-6 print:break-inside-avoid">
					<h3 class="font-bold text-lg text-gray-900 mb-4">Transformation Steps</h3>
					<div class="space-y-2">
						{#each job.steps as step}
							<div class="flex items-center justify-between p-3 bg-white rounded-lg">
								<div class="flex items-center gap-3">
									<div class="text-xl">
										{#if step.status === 'completed'}✅{:else if step.status === 'failed'}❌{:else}⏳{/if}
									</div>
									<div>
										<div class="font-semibold text-gray-900">Step {step.stepOrder + 1}: {step.stepName}</div>
										<div class="text-xs text-gray-600">{step.rowsProcessed.toLocaleString()} rows processed</div>
									</div>
								</div>
								{#if step.executionTimeMs}
									<div class="text-sm text-gray-600">{formatDuration(step.executionTimeMs)}</div>
								{/if}
							</div>
						{/each}
					</div>
				</div>

				<!-- Performance Stats -->
				<div class="grid grid-cols-3 gap-4 print:break-inside-avoid">
					<div class="bg-gradient-to-br from-blue-50 to-purple-50 rounded-lg p-4 text-center">
						<div class="text-2xl font-bold text-blue-600">{formatDuration(job.executionTimeMs)}</div>
						<div class="text-xs text-gray-600 mt-1">Total Execution Time</div>
					</div>
					<div class="bg-gradient-to-br from-green-50 to-blue-50 rounded-lg p-4 text-center">
						<div class="text-2xl font-bold text-green-600">
							{result ? Math.round(result.afterRowCount / (job.executionTimeMs / 1000)).toLocaleString() : 0}
						</div>
						<div class="text-xs text-gray-600 mt-1">Rows/Second</div>
					</div>
					<div class="bg-gradient-to-br from-purple-50 to-pink-50 rounded-lg p-4 text-center">
						<div class="text-2xl font-bold text-purple-600">${job.actualCost?.toFixed(4) || '0.0000'}</div>
						<div class="text-xs text-gray-600 mt-1">Actual Cost</div>
					</div>
				</div>
			</div>

		{:else if activeTab === 'preview'}
			<div>
				<h3 class="font-bold text-lg text-gray-900 mb-4">Result Preview (First 100 Rows)</h3>
				{#if result?.resultPreview}
					<DataTable
						headers={result.resultPreview.headers}
						rows={result.resultPreview.rows}
						maxRows={100}
					/>
				{:else}
					<div class="text-center py-12 text-gray-500">
						<div class="text-4xl mb-3">📊</div>
						<p>No preview data available</p>
					</div>
				{/if}
			</div>

		{:else if activeTab === 'logs'}
			<div>
				<h3 class="font-bold text-lg text-gray-900 mb-4">Execution Logs</h3>
				<div class="bg-gray-900 text-green-400 p-4 rounded-lg font-mono text-xs overflow-x-auto max-h-96 overflow-y-auto">
					{#each job.logs as log}
						<div class="mb-1">{log}</div>
					{/each}
				</div>
			</div>

		{:else if activeTab === 'errors'}
			<div class="space-y-4">
				{#if warnings.length > 0}
					<div>
						<h3 class="font-bold text-lg text-orange-900 mb-3">Warnings ({warnings.length})</h3>
						<div class="space-y-2">
							{#each warnings as warning}
								<div class="flex items-start gap-3 p-4 bg-orange-50 border-2 border-orange-200 rounded-lg">
									<div class="text-xl">⚠️</div>
									<div class="flex-1 text-sm text-orange-800">{warning}</div>
								</div>
							{/each}
						</div>
					</div>
				{/if}

				{#if errors.length > 0}
					<div>
						<h3 class="font-bold text-lg text-red-900 mb-3">Errors ({errors.length})</h3>
						<div class="space-y-2">
							{#each errors as error}
								<div class="flex items-start gap-3 p-4 bg-red-50 border-2 border-red-200 rounded-lg">
									<div class="text-xl">❌</div>
									<div class="flex-1 text-sm text-red-800 font-mono">{error}</div>
								</div>
							{/each}
						</div>
					</div>
				{/if}

				{#if errors.length === 0 && warnings.length === 0}
					<div class="text-center py-12 text-gray-500">
						<div class="text-4xl mb-3">✅</div>
						<p class="font-semibold">No errors or warnings!</p>
						<p class="text-sm mt-2">Job completed without any issues.</p>
					</div>
				{/if}
			</div>
		{/if}
	</div>
</div>

<style>
	@media print {
		:global(body) {
			background: white;
		}

		.print\:hidden {
			display: none !important;
		}

		.print\:shadow-none {
			box-shadow: none !important;
		}

		.print\:bg-green-600 {
			background: #059669 !important;
			-webkit-print-color-adjust: exact;
			print-color-adjust: exact;
		}

		.print\:space-y-4 > * + * {
			margin-top: 1rem;
		}

		.print\:break-inside-avoid {
			break-inside: avoid;
			page-break-inside: avoid;
		}

		@page {
			margin: 1cm;
			size: A4;
		}
	}
</style>
