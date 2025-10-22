<script lang="ts">
	import { onMount, onDestroy } from 'svelte';
	import { page } from '$app/stores';
	import { goto } from '$app/navigation';
	import { toastStore } from '$lib/stores/toast';
	import JobResultsViewer from '$lib/components/JobResultsViewer.svelte';
	import type { Job } from '$lib/types';

	const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

	let jobId: number;
	let job: Job | null = null;
	let loading = true;
	let pollingInterval: NodeJS.Timeout | null = null;
	let executionTimer: NodeJS.Timeout | null = null;
	let elapsedTime = 0;
	let showConfetti = false;

	$: jobId = parseInt($page.params.id);
	$: statusColor = getStatusColor(job?.status);
	$: statusIcon = getStatusIcon(job?.status);

	onMount(() => {
		loadJob();
		startPolling();
		startExecutionTimer();
	});

	onDestroy(() => {
		stopPolling();
		stopExecutionTimer();
	});

	async function loadJob() {
		try {
			const response = await fetch(`${API_URL}/jobs/${jobId}`);
			if (!response.ok) throw new Error('Failed to load job');

			const previousStatus = job?.status;
			job = await response.json();
			loading = false;

			// Trigger confetti on completion
			if (previousStatus !== 'completed' && job.status === 'completed') {
				triggerConfetti();
			}

			// Stop polling if job is in terminal state
			if (job.status === 'completed' || job.status === 'failed' || job.status === 'cancelled') {
				stopPolling();
				stopExecutionTimer();
			}
		} catch (error) {
			console.error('Failed to load job:', error);
			toastStore.add('error', 'Failed to load job');
			loading = false;
		}
	}

	function startPolling() {
		pollingInterval = setInterval(() => {
			loadJob();
		}, 2000); // Poll every 2 seconds
	}

	function stopPolling() {
		if (pollingInterval) {
			clearInterval(pollingInterval);
			pollingInterval = null;
		}
	}

	function startExecutionTimer() {
		executionTimer = setInterval(() => {
			if (job?.status === 'running') {
				elapsedTime += 1000;
			}
		}, 1000);
	}

	function stopExecutionTimer() {
		if (executionTimer) {
			clearInterval(executionTimer);
			executionTimer = null;
		}
	}

	function getStatusColor(status: string | undefined): string {
		switch (status) {
			case 'pending': return 'text-gray-600 bg-gray-100';
			case 'running': return 'text-blue-600 bg-blue-100';
			case 'completed': return 'text-green-600 bg-green-100';
			case 'failed': return 'text-red-600 bg-red-100';
			case 'cancelled': return 'text-orange-600 bg-orange-100';
			default: return 'text-gray-600 bg-gray-100';
		}
	}

	function getStatusIcon(status: string | undefined): string {
		switch (status) {
			case 'pending': return '⏳';
			case 'running': return '⚙️';
			case 'completed': return '✅';
			case 'failed': return '❌';
			case 'cancelled': return '🚫';
			default: return '❓';
		}
	}

	function formatDuration(ms: number): string {
		const seconds = Math.floor(ms / 1000);
		const minutes = Math.floor(seconds / 60);
		const hours = Math.floor(minutes / 60);

		if (hours > 0) return `${hours}h ${minutes % 60}m ${seconds % 60}s`;
		if (minutes > 0) return `${minutes}m ${seconds % 60}s`;
		return `${seconds}s`;
	}

	async function handleCancelJob() {
		if (!confirm('Are you sure you want to cancel this job?')) return;

		try {
			const response = await fetch(`${API_URL}/jobs/${jobId}/cancel`, {
				method: 'POST'
			});

			if (!response.ok) throw new Error('Failed to cancel job');

			toastStore.add('success', 'Job cancelled');
			loadJob();
		} catch (error) {
			console.error('Failed to cancel job:', error);
			toastStore.add('error', 'Failed to cancel job');
		}
	}

	function triggerConfetti() {
		showConfetti = true;
		setTimeout(() => {
			showConfetti = false;
		}, 5000);
	}
</script>

<svelte:head>
	<title>Job {jobId} - SheetPilot</title>
</svelte:head>

<div class="min-h-screen bg-gray-50">
	<!-- Header -->
	<div class="bg-white border-b-2 border-gray-200 px-6 py-4">
		<div class="max-w-6xl mx-auto flex items-center justify-between">
			<div class="flex items-center gap-4">
				<button
					on:click={() => goto('/pipelines')}
					class="p-2 hover:bg-gray-100 rounded-lg transition-colors"
				>
					<svg class="w-5 h-5 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
						<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18"/>
					</svg>
				</button>
				<div>
					<h1 class="text-2xl font-bold text-gray-900">Job #{jobId}</h1>
					{#if job}
						<p class="text-sm text-gray-600">{job.pipelineName}</p>
					{/if}
				</div>
			</div>

			{#if job?.status === 'running'}
				<button
					on:click={handleCancelJob}
					class="px-4 py-2 bg-red-600 text-white font-semibold rounded-lg hover:bg-red-700 transition-colors"
				>
					Cancel Job
				</button>
			{/if}
		</div>
	</div>

	<!-- Content -->
	<div class="max-w-6xl mx-auto p-6">
		{#if loading}
			<div class="flex items-center justify-center h-64">
				<div class="w-12 h-12 border-4 border-blue-600 border-t-transparent rounded-full animate-spin"></div>
			</div>
		{:else if !job}
			<div class="text-center text-gray-500 py-12">
				<div class="text-4xl mb-3">❓</div>
				<p>Job not found</p>
			</div>
		{:else}
			<!-- Status Card -->
			<div class="bg-white rounded-xl shadow-lg p-8 mb-6">
				<div class="flex items-center justify-between mb-6">
					<div class="flex items-center gap-4">
						<div class="text-6xl">{statusIcon}</div>
						<div>
							<div class="text-3xl font-bold capitalize {statusColor} px-4 py-2 rounded-lg inline-block">
								{job.status}
							</div>
							<div class="text-sm text-gray-600 mt-2">
								Started {new Date(job.startedAt).toLocaleString()}
							</div>
						</div>
					</div>

					<div class="text-right">
						<div class="text-4xl font-bold text-gray-900">
							{job.status === 'running' ? formatDuration(elapsedTime) : formatDuration(job.executionTimeMs)}
						</div>
						<div class="text-sm text-gray-600">
							{job.status === 'running' ? 'Elapsed Time' : 'Total Time'}
						</div>
					</div>
				</div>

				<!-- Progress Bar -->
				<div class="mb-4">
					<div class="flex items-center justify-between text-sm text-gray-600 mb-2">
						<span>Progress</span>
						<span>{Math.round(job.progress)}%</span>
					</div>
					<div class="w-full h-4 bg-gray-200 rounded-full overflow-hidden">
						<div
							class="h-full bg-gradient-to-r from-purple-600 to-blue-600 transition-all duration-500"
							style="width: {job.progress}%"
						></div>
					</div>
				</div>

				<!-- Stats Grid -->
				<div class="grid grid-cols-4 gap-4 mt-6">
					<div class="text-center p-4 bg-gray-50 rounded-lg">
						<div class="text-2xl font-bold text-gray-900">{job.spreadsheetIds.length}</div>
						<div class="text-xs text-gray-600">Spreadsheets</div>
					</div>
					<div class="text-center p-4 bg-gray-50 rounded-lg">
						<div class="text-2xl font-bold text-gray-900">{job.steps.length}</div>
						<div class="text-xs text-gray-600">Steps</div>
					</div>
					<div class="text-center p-4 bg-gray-50 rounded-lg">
						<div class="text-2xl font-bold text-gray-900">${job.estimatedCost.toFixed(4)}</div>
						<div class="text-xs text-gray-600">Est. Cost</div>
					</div>
					<div class="text-center p-4 bg-gray-50 rounded-lg">
						<div class="text-2xl font-bold text-gray-900">
							{job.actualCost !== null ? `$${job.actualCost.toFixed(4)}` : '-'}
						</div>
						<div class="text-xs text-gray-600">Actual Cost</div>
					</div>
				</div>
			</div>

			<!-- Pipeline Steps -->
			<div class="bg-white rounded-xl shadow-lg p-6 mb-6">
				<h2 class="text-xl font-bold text-gray-900 mb-4">Pipeline Steps</h2>
				<div class="space-y-3">
					{#each job.steps as step}
						<div class="flex items-center gap-4 p-4 border-2 rounded-lg"
							class:border-gray-200={step.status === 'pending'}
							class:border-blue-500={step.status === 'running'}
							class:border-green-500={step.status === 'completed'}
							class:border-red-500={step.status === 'failed'}
							class:bg-blue-50={step.status === 'running'}
							class:bg-green-50={step.status === 'completed'}
							class:bg-red-50={step.status === 'failed'}>
							<div class="text-2xl">
								{#if step.status === 'pending'}⏳
								{:else if step.status === 'running'}
									<div class="w-6 h-6 border-4 border-blue-600 border-t-transparent rounded-full animate-spin"></div>
								{:else if step.status === 'completed'}✅
								{:else if step.status === 'failed'}❌
								{/if}
							</div>
							<div class="flex-1">
								<div class="font-semibold text-gray-900">Step {step.stepOrder + 1}: {step.stepName}</div>
								<div class="text-xs text-gray-600 mt-1">
									{#if step.status === 'completed' && step.completedAt}
										Completed {new Date(step.completedAt).toLocaleTimeString()} · {step.rowsProcessed.toLocaleString()} rows
									{:else if step.status === 'running' && step.startedAt}
										Started {new Date(step.startedAt).toLocaleTimeString()}
									{:else if step.status === 'failed'}
										{step.errorMessage || 'Failed'}
									{:else}
										Waiting...
									{/if}
								</div>
							</div>
						</div>
					{/each}
				</div>
			</div>

			<!-- Spreadsheets -->
			<div class="bg-white rounded-xl shadow-lg p-6 mb-6">
				<h2 class="text-xl font-bold text-gray-900 mb-4">Target Spreadsheets</h2>
				<div class="grid gap-2">
					{#each job.spreadsheetNames as name, i}
						<div class="flex items-center gap-3 p-3 bg-gray-50 rounded-lg">
							<div class="text-xl">📊</div>
							<div class="flex-1 font-medium text-gray-900">{name}</div>
							<div class="text-xs text-gray-600">ID: {job.spreadsheetIds[i]}</div>
						</div>
					{/each}
				</div>
			</div>

			<!-- Logs -->
			{#if job.logs.length > 0}
				<div class="bg-white rounded-xl shadow-lg p-6">
					<h2 class="text-xl font-bold text-gray-900 mb-4">Execution Logs</h2>
					<div class="bg-gray-900 text-green-400 p-4 rounded-lg font-mono text-xs overflow-x-auto max-h-96 overflow-y-auto">
						{#each job.logs as log}
							<div class="mb-1">{log}</div>
						{/each}
					</div>
				</div>
			{/if}

			<!-- Results Viewer (only show when completed) -->
			{#if job.status === 'completed'}
				<div class="mt-6">
					<JobResultsViewer {job} />
				</div>
			{/if}

			<!-- Error Message -->
			{#if job.status === 'failed' && job.errorMessage}
				<div class="bg-red-50 border-2 border-red-200 rounded-xl p-6 mt-6">
					<div class="flex items-start gap-3">
						<div class="text-2xl">❌</div>
						<div class="flex-1">
							<h3 class="font-bold text-red-900 mb-2">Error Details</h3>
							<p class="text-sm text-red-700 mb-3">{job.errorMessage}</p>
							<div class="bg-white border-2 border-red-200 rounded-lg p-4">
								<h4 class="font-semibold text-red-900 text-xs mb-2">Suggested Fixes:</h4>
								<ul class="text-xs text-red-700 space-y-1">
									<li>• Check that all column names in transformation configs exist in the data</li>
									<li>• Verify that join keys contain matching values in both spreadsheets</li>
									<li>• Ensure data types are compatible with the transformations being applied</li>
									<li>• Review the pipeline configuration and test with a smaller dataset</li>
								</ul>
							</div>
						</div>
					</div>
				</div>
			{/if}
		{/if}
	</div>
</div>

<!-- Confetti Animation -->
{#if showConfetti}
	<div class="fixed inset-0 pointer-events-none z-50">
		{#each Array(50) as _, i}
			<div
				class="absolute w-2 h-2 rounded-full animate-confetti"
				style="
					left: {Math.random() * 100}%;
					top: -10px;
					background-color: hsl({Math.random() * 360}, 70%, 60%);
					animation-delay: {Math.random() * 0.5}s;
					animation-duration: {2 + Math.random() * 2}s;
				"
			></div>
		{/each}
	</div>
{/if}

<style>
	@keyframes confetti {
		0% {
			transform: translateY(0) rotate(0deg);
			opacity: 1;
		}
		100% {
			transform: translateY(100vh) rotate(720deg);
			opacity: 0;
		}
	}

	.animate-confetti {
		animation: confetti linear forwards;
	}
</style>
