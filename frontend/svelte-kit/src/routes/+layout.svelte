<script lang="ts">
  import { navigating } from '$app/state';
  import { PUBLIC_APP_NAME } from '$env/static/public';
  import GuestNavbar from '$lib/components/navbar/guest-navbar.svelte';
  import UserNavbar from '$lib/components/navbar/user-navbar.svelte';
  import Loading from '$lib/components/shared/loading.svelte';
  import { Toaster } from '$lib/components/ui/sonner';
  import { i18n } from '$lib/i18n';
  import { ParaglideJS } from '@inlang/paraglide-sveltekit';
  import { ModeWatcher } from 'mode-watcher';
  import '../app.css';
  import type { LayoutProps } from './$types';

  const { data, children }: LayoutProps = $props();
</script>

<svelte:head>
  <title>{PUBLIC_APP_NAME}</title>
</svelte:head>

<ParaglideJS {i18n}>
  {#if navigating.complete}
    <Loading />
  {:else if data.profile}
    <UserNavbar isAdmin={data.isAdmin} />
    {@render children()}
  {:else}
    <GuestNavbar />
    {@render children()}
  {/if}
</ParaglideJS>

<Toaster />
<ModeWatcher />
