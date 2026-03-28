<script lang="ts">
  import { PUBLIC_APP_NAME } from '$env/static/public';
  import { Button } from '$lib/components/ui/button';
  import { Separator } from '$lib/components/ui/separator';
  import * as Sheet from '$lib/components/ui/sheet';
  import MenuIcon from 'lucide-svelte/icons/menu';
  import LanguageSwitcher from './language-switcher.svelte';
  import ThemeSwitcher from './theme-switcher.svelte';
</script>

<header class="sticky top-0 z-50 bg-primary-foreground py-2">
  <div class="container">
    <div class="flex items-center justify-between">
      <div class="flex items-center gap-3 lg:hidden">
        <Sheet.Root>
          <Sheet.Trigger>
            {#snippet child({ props })}
              <Button {...props} variant="ghost" size="icon">
                <MenuIcon />
                <span class="sr-only">Toggle Menu</span>
              </Button>
            {/snippet}
          </Sheet.Trigger>
          <Sheet.Content side="left">
            <Sheet.Header>
              <Sheet.Title class="self-center">{PUBLIC_APP_NAME}</Sheet.Title>
              <Separator />
              <div class="self-center"><LanguageSwitcher /></div>
            </Sheet.Header>
          </Sheet.Content>
        </Sheet.Root>
      </div>

      <Button href="/" class="text-2xl" variant="ghost">{PUBLIC_APP_NAME}</Button>

      <div class="ml-auto flex items-center gap-3">
        <div class="flex gap-3 lg:hidden">
          <ThemeSwitcher />
        </div>

        <div class="hidden gap-3 lg:flex">
          <ThemeSwitcher />
          <LanguageSwitcher />
        </div>
      </div>
    </div>
  </div>
</header>
