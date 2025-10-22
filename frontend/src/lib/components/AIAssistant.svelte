<script lang="ts">
	import { aiChatStore } from '$lib/stores/aiChat';
	import { pipelineEditorStore } from '$lib/stores/pipelineEditor';
	import type { AIChatMessage } from '$lib/types';

	const API_URL = 'http://localhost:8080/api';

	let expanded = false;
	let messageInput = '';
	let chatContainer: HTMLDivElement;

	$: messages = $aiChatStore.messages;
	$: isLoading = $aiChatStore.isLoading;
	$: totalCost = $aiChatStore.totalCost;

	const examplePrompts = [
		'Remove rows with missing values',
		'Calculate average revenue by category',
		'Sort by date descending',
		'Join with customer data on customer_id',
		'Filter rows where amount > 1000'
	];

	function formatTimestamp(timestamp: string): string {
		const date = new Date(timestamp);
		const now = new Date();
		const diff = now.getTime() - date.getTime();
		const minutes = Math.floor(diff / 60000);

		if (minutes < 1) return 'Just now';
		if (minutes < 60) return `${minutes}m ago`;
		if (minutes < 1440) return `${Math.floor(minutes / 60)}h ago`;
		return date.toLocaleDateString();
	}

	async function sendMessage(content: string) {
		if (!content.trim()) return;

		messageInput = '';
		aiChatStore.addMessage({ role: 'user', content });
		aiChatStore.setLoading(true);

		try {
			const pipelineContext = {
				steps: $pipelineEditorStore.steps.map(s => ({
					type: s.type,
					config: s.config
				}))
			};

			const response = await fetch(`${API_URL}/ai/parse`, {
				method: 'POST',
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify({
					prompt: content,
					context: pipelineContext
				})
			});

			if (!response.ok) throw new Error('AI request failed');

			const data = await response.json();

			aiChatStore.addMessage({
				role: 'assistant',
				content: data.explanation,
				transformationSuggestion: {
					type: data.transformationType,
					config: data.config,
					explanation: data.explanation,
					confidence: data.confidence
				},
				tokenUsage: {
					input: data.tokenUsage?.input || 0,
					output: data.tokenUsage?.output || 0,
					cost: data.tokenUsage?.cost || 0
				}
			});

			setTimeout(() => {
				if (chatContainer) {
					chatContainer.scrollTop = chatContainer.scrollHeight;
				}
			}, 100);
		} catch (error) {
			aiChatStore.setError('Failed to get AI response. Please try again.');
			console.error(error);
		} finally {
			aiChatStore.setLoading(false);
		}
	}

	function addToPipeline(suggestion: AIChatMessage['transformationSuggestion']) {
		if (!suggestion) return;

		const newStep = {
			id: `step-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
			type: suggestion.type,
			name: suggestion.type.charAt(0).toUpperCase() + suggestion.type.slice(1),
			config: suggestion.config,
			order: $pipelineEditorStore.steps.length
		};

		pipelineEditorStore.addStep(newStep);
	}

	function regenerate() {
		aiChatStore.regenerateLastResponse();
		const lastUserMessage = messages.filter(m => m.role === 'user').pop();
		if (lastUserMessage) {
			sendMessage(lastUserMessage.content);
		}
	}

	function handleKeydown(e: KeyboardEvent) {
		if (e.key === 'Enter' && !e.shiftKey) {
			e.preventDefault();
			sendMessage(messageInput);
		}
	}
</script>

{#if !expanded}
	<button
		on:click={() => expanded = true}
		class="fixed bottom-6 right-6 w-14 h-14 bg-gradient-to-br from-purple-600 to-blue-600 text-white rounded-full shadow-lg hover:shadow-xl transition-all duration-200 flex items-center justify-center text-2xl z-50"
	>
		🤖
	</button>
{:else}
	<div class="fixed right-0 top-0 h-full w-96 bg-white border-l-2 border-gray-200 shadow-2xl z-50 flex flex-col">
		<!-- Header -->
		<div class="bg-gradient-to-r from-purple-600 to-blue-600 text-white p-4 flex items-center justify-between">
			<div>
				<h3 class="font-bold text-lg">AI Assistant</h3>
				<p class="text-xs opacity-90">Ask me to help with transformations</p>
			</div>
			<button
				on:click={() => expanded = false}
				class="p-2 hover:bg-white/20 rounded-lg transition-colors"
			>
				<svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
					<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
				</svg>
			</button>
		</div>

		<!-- Cost Tracker -->
		<div class="px-4 py-2 bg-gray-50 border-b-2 border-gray-200 text-xs text-gray-600">
			Total cost: <span class="font-semibold text-purple-600">${totalCost.toFixed(4)}</span>
		</div>

		<!-- Messages -->
		<div bind:this={chatContainer} class="flex-1 overflow-y-auto p-4 space-y-4">
			{#if messages.length === 0}
				<div class="text-center text-gray-500 mt-8">
					<div class="text-4xl mb-3">💬</div>
					<p class="text-sm">Start a conversation!</p>
					<p class="text-xs mt-2">Try one of the examples below</p>
				</div>
			{/if}

			{#each messages as message (message.id)}
				<div class="flex gap-3" class:flex-row-reverse={message.role === 'user'}>
					<div class="flex-shrink-0 w-8 h-8 rounded-full flex items-center justify-center text-sm"
						class:bg-purple-100={message.role === 'user'}
						class:bg-blue-100={message.role === 'assistant'}>
						{message.role === 'user' ? '👤' : '🤖'}
					</div>
					<div class="flex-1 max-w-[80%]">
						<div class="rounded-2xl px-4 py-2"
							class:bg-purple-600={message.role === 'user'}
							class:text-white={message.role === 'user'}
							class:bg-gray-100={message.role === 'assistant'}>
							<p class="text-sm whitespace-pre-wrap">{message.content}</p>
						</div>
						<div class="text-xs text-gray-500 mt-1 px-2">
							{formatTimestamp(message.timestamp)}
							{#if message.tokenUsage}
								· {message.tokenUsage.input + message.tokenUsage.output} tokens · ${message.tokenUsage.cost.toFixed(4)}
							{/if}
						</div>

						{#if message.transformationSuggestion}
							<div class="mt-2 bg-white border-2 border-gray-200 rounded-lg p-3">
								<div class="flex items-center justify-between mb-2">
									<span class="text-xs font-semibold text-gray-700">Suggested Transformation</span>
									<span class="text-xs px-2 py-1 rounded-full"
										class:bg-green-100={message.transformationSuggestion.confidence >= 0.8}
										class:text-green-700={message.transformationSuggestion.confidence >= 0.8}
										class:bg-yellow-100={message.transformationSuggestion.confidence < 0.8 && message.transformationSuggestion.confidence >= 0.6}
										class:text-yellow-700={message.transformationSuggestion.confidence < 0.8 && message.transformationSuggestion.confidence >= 0.6}
										class:bg-orange-100={message.transformationSuggestion.confidence < 0.6}
										class:text-orange-700={message.transformationSuggestion.confidence < 0.6}>
										{Math.round(message.transformationSuggestion.confidence * 100)}% confident
									</span>
								</div>

								<div class="text-xs mb-2">
									<span class="font-semibold">Type:</span>
									<code class="ml-1 px-2 py-1 bg-gray-100 rounded">{message.transformationSuggestion.type}</code>
								</div>

								<div class="text-xs mb-3">
									<span class="font-semibold">Config:</span>
									<pre class="mt-1 p-2 bg-gray-50 rounded overflow-x-auto text-xs">{JSON.stringify(message.transformationSuggestion.config, null, 2)}</pre>
								</div>

								<div class="flex gap-2">
									<button
										on:click={() => addToPipeline(message.transformationSuggestion)}
										class="flex-1 px-3 py-2 bg-purple-600 text-white rounded-lg text-xs font-semibold hover:bg-purple-700 transition-colors"
									>
										Add to Pipeline
									</button>
									<button
										on:click={regenerate}
										class="px-3 py-2 border-2 border-gray-300 rounded-lg text-xs font-semibold hover:bg-gray-50 transition-colors"
									>
										🔄
									</button>
								</div>
							</div>
						{/if}
					</div>
				</div>
			{/each}

			{#if isLoading}
				<div class="flex gap-3">
					<div class="flex-shrink-0 w-8 h-8 rounded-full bg-blue-100 flex items-center justify-center text-sm">
						🤖
					</div>
					<div class="bg-gray-100 rounded-2xl px-4 py-3">
						<div class="flex gap-1">
							<div class="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style="animation-delay: 0ms"></div>
							<div class="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style="animation-delay: 150ms"></div>
							<div class="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style="animation-delay: 300ms"></div>
						</div>
					</div>
				</div>
			{/if}
		</div>

		<!-- Example Prompts -->
		{#if messages.length === 0 && !isLoading}
			<div class="px-4 pb-3 border-t-2 border-gray-200 pt-3">
				<p class="text-xs font-semibold text-gray-700 mb-2">Try these:</p>
				<div class="flex flex-wrap gap-2">
					{#each examplePrompts as prompt}
						<button
							on:click={() => sendMessage(prompt)}
							class="text-xs px-3 py-1.5 bg-gray-100 hover:bg-purple-100 border-2 border-gray-200 hover:border-purple-300 rounded-full transition-colors"
						>
							{prompt}
						</button>
					{/each}
				</div>
			</div>
		{/if}

		<!-- Input -->
		<div class="p-4 border-t-2 border-gray-200">
			<div class="flex gap-2">
				<textarea
					bind:value={messageInput}
					on:keydown={handleKeydown}
					placeholder="Describe what you want to do..."
					rows="2"
					class="flex-1 px-3 py-2 border-2 border-gray-300 rounded-lg text-sm resize-none focus:outline-none focus:border-purple-500"
				></textarea>
				<button
					on:click={() => sendMessage(messageInput)}
					disabled={isLoading || !messageInput.trim()}
					class="px-4 bg-purple-600 text-white rounded-lg text-sm font-semibold hover:bg-purple-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
				>
					Send
				</button>
			</div>

			{#if messages.length > 0}
				<button
					on:click={() => aiChatStore.clearHistory()}
					class="w-full mt-2 px-3 py-1.5 text-xs text-red-600 hover:bg-red-50 border-2 border-red-200 rounded-lg transition-colors"
				>
					Clear History
				</button>
			{/if}
		</div>
	</div>
{/if}
