<script lang="ts">
  let mobileMenuOpen = false;

  function toggleMobileMenu() {
    mobileMenuOpen = !mobileMenuOpen;
  }
</script>

<header class="bg-white shadow-sm">
  <nav class="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
    <div class="flex h-16 justify-between items-center">
      <!-- Logo -->
      <div class="flex items-center">
        <a href="/" class="flex items-center space-x-2">
          <svg
            class="h-8 w-8 text-primary-600"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
            />
          </svg>
          <span class="text-xl font-bold text-gray-900">SheetPilot</span>
        </a>
      </div>

      <!-- Desktop Navigation -->
      <div class="hidden md:flex md:items-center md:space-x-8">
        <a href="/" class="text-gray-700 hover:text-primary-600 transition-colors">Home</a>
        <a href="/pipelines" class="text-gray-700 hover:text-primary-600 transition-colors">Pipelines</a>
        <a href="/spreadsheets" class="text-gray-700 hover:text-primary-600 transition-colors">Spreadsheets</a>
        <a href="/jobs" class="text-gray-700 hover:text-primary-600 transition-colors">Jobs</a>
      </div>

      <!-- CTA Button -->
      <div class="hidden md:flex md:items-center">
        <a
          href="/dashboard"
          class="rounded-md bg-primary-600 px-4 py-2 text-sm font-medium text-white hover:bg-primary-700 transition-colors"
        >
          Dashboard
        </a>
      </div>

      <!-- Mobile menu button -->
      <div class="flex md:hidden">
        <button
          type="button"
          class="inline-flex items-center justify-center rounded-md p-2 text-gray-700 hover:bg-gray-100 hover:text-gray-900"
          on:click={toggleMobileMenu}
        >
          <span class="sr-only">Open main menu</span>
          {#if !mobileMenuOpen}
            <svg class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
            </svg>
          {:else}
            <svg class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          {/if}
        </button>
      </div>
    </div>
  </nav>

  <!-- Mobile menu -->
  {#if mobileMenuOpen}
    <div class="md:hidden">
      <div class="space-y-1 px-4 pb-3 pt-2">
        <a href="/" class="block rounded-md px-3 py-2 text-base font-medium text-gray-700 hover:bg-gray-50 hover:text-gray-900">Home</a>
        <a href="/pipelines" class="block rounded-md px-3 py-2 text-base font-medium text-gray-700 hover:bg-gray-50 hover:text-gray-900">Pipelines</a>
        <a href="/spreadsheets" class="block rounded-md px-3 py-2 text-base font-medium text-gray-700 hover:bg-gray-50 hover:text-gray-900">Spreadsheets</a>
        <a href="/jobs" class="block rounded-md px-3 py-2 text-base font-medium text-gray-700 hover:bg-gray-50 hover:text-gray-900">Jobs</a>
        <a href="/dashboard" class="block rounded-md bg-primary-600 px-3 py-2 text-base font-medium text-white hover:bg-primary-700">Dashboard</a>
      </div>
    </div>
  {/if}
</header>
