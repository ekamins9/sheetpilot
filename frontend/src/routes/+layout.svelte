<script lang="ts">
	import '../app.css';
	import Header from '$lib/components/Header.svelte';
	import favicon from '$lib/assets/favicon.svg';

	let { children } = $props();
</script>

<svelte:head>
	<link rel="icon" href={favicon} />
</svelte:head>

<div class="min-h-screen flex flex-col">
	<Header />
	<main class="flex-1">
		{@render children?.()}
	</main>
</div>
