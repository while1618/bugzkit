<script lang="ts">
  import { page } from '$app/state';
  import { Button } from '$lib/components/ui/button';
  import * as Pagination from '$lib/components/ui/pagination';
  import * as m from '$lib/paraglide/messages.js';
  import ChevronLeftIcon from 'lucide-svelte/icons/chevron-left';
  import ChevronRightIcon from 'lucide-svelte/icons/chevron-right';

  interface Props {
    currentPage: number;
    count: number;
    size: number;
    class?: string;
  }

  const { currentPage, count, size, class: className }: Props = $props();
  const totalPages = $derived(Math.ceil(count / size));
</script>

<Pagination.Root {count} perPage={size} page={currentPage} class={className}>
  {#snippet children({ pages, currentPage: cp })}
    <Pagination.Content>
      <Pagination.Item>
        <Button
          variant="ghost"
          disabled={cp <= 1}
          href={cp > 1 ? `${page.route.id}?page=${cp - 1}&size=${size}` : undefined}
        >
          <ChevronLeftIcon class="size-4" />
          {m.general_previous()}
        </Button>
      </Pagination.Item>
      {#each pages as pg (pg.key)}
        {#if pg.type === 'ellipsis'}
          <Pagination.Item class="hidden sm:flex">
            <Pagination.Ellipsis />
          </Pagination.Item>
        {:else}
          <Pagination.Item class="hidden sm:flex">
            <Pagination.Link page={pg} isActive={cp === pg.value}>
              {#snippet child({ props })}
                <a {...props} href="{page.route.id}?page={pg.value}&size={size}">
                  {pg.value}
                </a>
              {/snippet}
            </Pagination.Link>
          </Pagination.Item>
        {/if}
      {/each}
      <Pagination.Item class="sm:hidden">
        <span class="text-sm text-muted-foreground">{cp} / {totalPages}</span>
      </Pagination.Item>
      <Pagination.Item>
        <Button
          variant="ghost"
          disabled={cp >= totalPages}
          href={cp < totalPages ? `${page.route.id}?page=${cp + 1}&size=${size}` : undefined}
        >
          {m.general_next()}
          <ChevronRightIcon class="size-4" />
        </Button>
      </Pagination.Item>
    </Pagination.Content>
  {/snippet}
</Pagination.Root>
