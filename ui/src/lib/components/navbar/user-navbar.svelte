<script lang="ts">
  import { PUBLIC_APP_NAME } from '$env/static/public';
  import { Button } from '$lib/components/ui/button';
  import * as DropdownMenu from '$lib/components/ui/dropdown-menu';
  import { Separator } from '$lib/components/ui/separator';
  import * as Sheet from '$lib/components/ui/sheet';
  import * as m from '$lib/paraglide/messages.js';
  import ChartBarIncreasingIcon from 'lucide-svelte/icons/chart-bar-increasing';
  import HouseIcon from 'lucide-svelte/icons/house';
  import LogOutIcon from 'lucide-svelte/icons/log-out';
  import MenuIcon from 'lucide-svelte/icons/menu';
  import SettingsIcon from 'lucide-svelte/icons/settings';
  import UserIcon from 'lucide-svelte/icons/user';
  import LanguageSwitcher from './language-switcher.svelte';
  import ThemeSwitcher from './theme-switcher.svelte';

  const { isAdmin }: { isAdmin: boolean } = $props();
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
                <span class="sr-only">Open Menu</span>
              </Button>
            {/snippet}
          </Sheet.Trigger>
          <Sheet.Content side="left">
            <Sheet.Header>
              <Sheet.Title class="self-center">{PUBLIC_APP_NAME}</Sheet.Title>
              <Separator />
              <Sheet.Description>
                <div class="flex w-full flex-col gap-2">
                  <Button href="/" variant="ghost">
                    <HouseIcon />
                    {m.navbar_home()}
                  </Button>
                  <Button href="/profile" variant="ghost">
                    <UserIcon />
                    {m.navbar_profile()}
                  </Button>
                  <Button href="/profile/settings" variant="ghost">
                    <SettingsIcon />
                    {m.navbar_settings()}
                  </Button>
                  {#if isAdmin}
                    <Button href="/admin/user" variant="ghost">
                      <ChartBarIncreasingIcon />
                      {m.navbar_admin()}
                    </Button>
                  {/if}
                  <Separator />
                  <Button href="/auth/sign-out" variant="ghost">
                    <LogOutIcon />
                    {m.navbar_signOut()}
                  </Button>
                </div>
              </Sheet.Description>
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
          <DropdownMenu.Root>
            <DropdownMenu.Trigger>
              {#snippet child({ props })}
                <Button {...props} variant="ghost" size="icon"><UserIcon /></Button>
              {/snippet}
            </DropdownMenu.Trigger>
            <DropdownMenu.Content class="w-56">
              <DropdownMenu.Group>
                <DropdownMenu.GroupHeading>My Account</DropdownMenu.GroupHeading>
                <DropdownMenu.Separator />
                <DropdownMenu.Group>
                  <DropdownMenu.Item class="cursor-pointer">
                    {#snippet child({ props })}
                      <a {...props} href="/"><HouseIcon />{m.navbar_home()}</a>
                    {/snippet}
                  </DropdownMenu.Item>
                  <DropdownMenu.Item class="cursor-pointer">
                    {#snippet child({ props })}
                      <a {...props} href="/profile"><UserIcon />{m.navbar_profile()}</a>
                    {/snippet}
                  </DropdownMenu.Item>
                  <DropdownMenu.Item class="cursor-pointer">
                    {#snippet child({ props })}
                      <a {...props} href="/profile/settings">
                        <SettingsIcon />{m.navbar_settings()}
                      </a>
                    {/snippet}
                  </DropdownMenu.Item>
                  {#if isAdmin}
                    <DropdownMenu.Item class="cursor-pointer">
                      {#snippet child({ props })}
                        <a {...props} href="/admin/user">
                          <ChartBarIncreasingIcon />
                          {m.navbar_admin()}
                        </a>
                      {/snippet}
                    </DropdownMenu.Item>
                  {/if}
                </DropdownMenu.Group>
                <DropdownMenu.Separator />
                <DropdownMenu.Group>
                  <DropdownMenu.Item class="cursor-pointer">
                    {#snippet child({ props })}
                      <a {...props} href="/auth/sign-out"><LogOutIcon />{m.navbar_signOut()}</a>
                    {/snippet}
                  </DropdownMenu.Item>
                </DropdownMenu.Group>
              </DropdownMenu.Group>
            </DropdownMenu.Content>
          </DropdownMenu.Root>
        </div>
      </div>
    </div>
  </div>
</header>
