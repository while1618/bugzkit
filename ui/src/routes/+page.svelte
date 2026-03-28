<script lang="ts">
  import GuestHome from '$lib/components/home/guest-home.svelte';
  import UserHome from '$lib/components/home/user-home.svelte';
  import type { PageProps } from './$types';

  const { data }: PageProps = $props();
</script>

{#if data.profile}
  <UserHome profile={data.profile} />
{:else}
  <GuestHome />
{/if}
