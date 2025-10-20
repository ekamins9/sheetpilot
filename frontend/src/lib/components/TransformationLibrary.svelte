<script lang="ts">
	import { transformationTemplates } from '$lib/stores/pipelineEditor';
	import type { TransformationTemplate } from '$lib/types';
	import { scale, slide } from 'svelte/transition';

	export let onAddTransformation: (template: TransformationTemplate) => void;

	let searchQuery = '';
	let selectedCategory: string | null = null;

	const categories = [
		{ id: 'data-cleaning', name: 'Data Cleaning', icon: '🧹' },
		{ id: 'joining', name: 'Joining', icon: '🔗' },
		{ id: 'aggregation', name: 'Aggregation', icon: '📊' },
		{ id: 'ai-powered', name: 'AI-Powered', icon: '🤖' }
	];

	$: filteredTemplates = transformationTemplates.filter((template) => {
		const matchesSearch =
			!searchQuery ||
			template.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
			template.description.toLowerCase().includes(searchQuery.toLowerCase());

		const matchesCategory = !selectedCategory || template.category === selectedCategory;

		return matchesSearch && matchesCategory;
	});

	$: groupedTemplates = filteredTemplates.reduce(
		(acc, template) => {
			if (!acc[template.category]) {
				acc[template.category] = [];
			}
			acc[template.category].push(template);
			return acc;
		},
		{} as Record<string, TransformationTemplate[]>
	);

	function handleDragStart(e: DragEvent, template: TransformationTemplate) {
		if (e.dataTransfer) {
			e.dataTransfer.effectAllowed = 'copy';
			e.dataTransfer.setData('application/json', JSON.stringify(template));
		}
	}
</script>

<div class="h-full bg-white border-r-2 border-gray-200 flex flex-col">
	<!-- Header -->
	<div class="p-4 border-b-2 border-gray-200">
		<h2 class="text-lg font-bold text-gray-900 mb-3">Transformations</h2>

		<!-- Search -->
		<div class="relative mb-3">
			<svg
				class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400"
				fill="none"
				stroke="currentColor"
				viewBox="0 0 24 24"
			>
				<path
					stroke-linecap="round"
					stroke-linejoin="round"
					stroke-width="2"
					d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
				/>
			</svg>
			<input
				type="text"
				bind:value={searchQuery}
				placeholder="Search transformations..."
				class="w-full pl-9 pr-3 py-2 text-sm border-2 border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
			/>
		</div>

		<!-- Category Filter -->
		<div class="flex flex-wrap gap-2">
			<button
				on:click={() => (selectedCategory = null)}
				class="px-3 py-1 text-xs font-semibold rounded-full transition-colors"
				class:bg-blue-600={selectedCategory === null}
				class:text-white={selectedCategory === null}
				class:bg-gray-100={selectedCategory !== null}
				class:text-gray-700={selectedCategory !== null}
			>
				All
			</button>
			{#each categories as category}
				<button
					on:click={() => (selectedCategory = category.id)}
					class="px-3 py-1 text-xs font-semibold rounded-full transition-colors"
					class:bg-blue-600={selectedCategory === category.id}
					class:text-white={selectedCategory === category.id}
					class:bg-gray-100={selectedCategory !== category.id}
					class:text-gray-700={selectedCategory !== category.id}
				>
					{category.icon} {category.name}
				</button>
			{/each}
		</div>
	</div>

	<!-- Transformations List -->
	<div class="flex-1 overflow-y-auto p-4">
		{#if filteredTemplates.length === 0}
			<div class="text-center py-8">
				<p class="text-gray-500 text-sm">No transformations found</p>
			</div>
		{:else}
			{#each Object.entries(groupedTemplates) as [category, templates]}
				<div class="mb-6" transition:slide={{ duration: 200 }}>
					<h3 class="text-xs font-bold text-gray-500 uppercase tracking-wider mb-3">
						{categories.find((c) => c.id === category)?.icon}
						{categories.find((c) => c.id === category)?.name}
					</h3>
					<div class="space-y-2">
						{#each templates as template (template.id)}
							<div
								draggable="true"
								on:dragstart={(e) => handleDragStart(e, template)}
								on:click={() => onAddTransformation(template)}
								class="bg-gray-50 border-2 border-gray-200 rounded-lg p-3 cursor-move hover:border-blue-400 hover:bg-blue-50 transition-all duration-150 group"
								transition:scale={{ duration: 150 }}
							>
								<div class="flex items-start gap-3">
									<div
										class="flex-shrink-0 w-10 h-10 bg-white border-2 border-gray-300 rounded-lg flex items-center justify-center text-xl group-hover:border-blue-400 transition-colors"
									>
										{template.icon}
									</div>
									<div class="flex-1 min-w-0">
										<h4 class="font-semibold text-gray-900 text-sm mb-1">
											{template.name}
										</h4>
										<p class="text-xs text-gray-600 line-clamp-2">
											{template.description}
										</p>
									</div>
								</div>
							</div>
						{/each}
					</div>
				</div>
			{/each}
		{/if}
	</div>

	<!-- Help Text -->
	<div class="p-4 bg-blue-50 border-t-2 border-blue-200">
		<p class="text-xs text-blue-800">
			💡 <strong>Tip:</strong> Drag transformations to the canvas or click to add them to your pipeline
		</p>
	</div>
</div>

<style>
	/* Custom scrollbar */
	div::-webkit-scrollbar {
		width: 8px;
	}

	div::-webkit-scrollbar-track {
		background: #f1f1f1;
	}

	div::-webkit-scrollbar-thumb {
		background: #cbd5e1;
		border-radius: 4px;
	}

	div::-webkit-scrollbar-thumb:hover {
		background: #94a3b8;
	}
</style>
