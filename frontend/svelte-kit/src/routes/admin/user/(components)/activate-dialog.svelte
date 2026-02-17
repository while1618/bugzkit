<script lang="ts">
  import * as AlertDialog from '$lib/components/ui/alert-dialog';
  import { Button } from '$lib/components/ui/button';
  import type { AdminUser } from '$lib/models/user/user';
  import * as m from '$lib/paraglide/messages.js';
  import { cn } from '$lib/utils';
  import CheckIcon from 'lucide-svelte/icons/check';
  import XIcon from 'lucide-svelte/icons/x';
  import { toast } from 'svelte-sonner';
  import { superForm } from 'sveltekit-superforms';
  import { zodClient } from 'sveltekit-superforms/adapters';
  import type { PageData } from '../$types';
  import { actionSchema } from '../schema';

  const { data, user }: { data: PageData; user: AdminUser } = $props();

  const superform = superForm(data.activateForm, {
    validators: zodClient(actionSchema),
    id: `activate-form-${user.id}`,
    onSubmit({ formData }) {
      formData.set('id', `${user.id}`);
    },
  });
  const { message, errors, enhance } = superform;
  let dialogOpen = $state(false);
  const isSelf = user.id === data.profile?.id;

  $effect(() => {
    if ($message) toast.success($message);
    if ($errors._errors) {
      for (const error of $errors._errors) {
        toast.error(error);
      }
    }
  });
</script>

<AlertDialog.Root bind:open={dialogOpen}>
  <AlertDialog.Trigger>
    {#snippet child({ props })}
      <Button
        {...props}
        variant="ghost"
        onclick={isSelf
          ? (e) => {
              e.preventDefault();
              toast.error(m.admin_cannotModifySelf());
            }
          : props.onclick}
        class={user.active
          ? 'text-green-500 hover:text-green-500/90'
          : 'text-red-500 hover:text-red-500/90'}
      >
        {#if user.active}
          <CheckIcon />
        {:else}
          <XIcon />
        {/if}
      </Button>
    {/snippet}
  </AlertDialog.Trigger>
  <AlertDialog.Content>
    <AlertDialog.Header>
      <AlertDialog.Title>
        {user.active ? m.admin_deactivateUser() : m.admin_activateUser()}
      </AlertDialog.Title>
      <AlertDialog.Description>
        {user.active
          ? m.admin_deactivateUserConfirmation({ username: user.username })
          : m.admin_activateUserConfirmation({ username: user.username })}
      </AlertDialog.Description>
    </AlertDialog.Header>
    <AlertDialog.Footer>
      <AlertDialog.Cancel>{m.general_cancel()}</AlertDialog.Cancel>
      <AlertDialog.Action onclick={() => (dialogOpen = false)}>
        {#snippet child({ props })}
          <form method="POST" action="?/{user.active ? 'deactivate' : 'activate'}" use:enhance>
            <Button
              {...props}
              type="submit"
              class={cn(
                'w-full',
                user.active
                  ? 'bg-red-500 hover:bg-red-500/90'
                  : 'bg-green-500 hover:bg-green-500/90',
              )}
            >
              {user.active ? m.admin_deactivate() : m.admin_activate()}
            </Button>
          </form>
        {/snippet}
      </AlertDialog.Action>
    </AlertDialog.Footer>
  </AlertDialog.Content>
</AlertDialog.Root>
