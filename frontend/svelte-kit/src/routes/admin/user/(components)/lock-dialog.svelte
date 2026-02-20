<script lang="ts">
  import * as AlertDialog from '$lib/components/ui/alert-dialog';
  import { Button } from '$lib/components/ui/button';
  import type { AdminUser } from '$lib/models/user/user';
  import * as m from '$lib/paraglide/messages.js';
  import { cn } from '$lib/utils.js';
  import LockKeyholeIcon from 'lucide-svelte/icons/lock-keyhole';
  import LockKeyholeOpenIcon from 'lucide-svelte/icons/lock-keyhole-open';
  import { toast } from 'svelte-sonner';
  import { superForm } from 'sveltekit-superforms';
  import { zodClient } from 'sveltekit-superforms/adapters';
  import type { PageData } from '../$types';
  import { actionSchema } from '../schema';

  const { data, user }: { data: PageData; user: AdminUser } = $props();

  const superform = superForm(data.lockForm, {
    validators: zodClient(actionSchema),
    id: `lock-form-${user.id}`,
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
        onclick={() => {
          if (isSelf) {
            toast.error(m.admin_cannotModifySelf());
          } else {
            dialogOpen = true;
          }
        }}
        class={user.lock
          ? 'text-red-500 hover:text-red-500/90'
          : 'text-blue-500 hover:text-blue-500/90'}
      >
        {#if user.lock}
          <LockKeyholeIcon />
        {:else}
          <LockKeyholeOpenIcon />
        {/if}
      </Button>
    {/snippet}
  </AlertDialog.Trigger>
  <AlertDialog.Content>
    <AlertDialog.Header>
      <AlertDialog.Title>
        {user.lock ? m.admin_unlockUser() : m.admin_lockUser()}
      </AlertDialog.Title>
      <AlertDialog.Description>
        {user.lock
          ? m.admin_unlockUserConfirmation({ username: user.username })
          : m.admin_lockUserConfirmation({ username: user.username })}
      </AlertDialog.Description>
    </AlertDialog.Header>
    <AlertDialog.Footer>
      <AlertDialog.Cancel>{m.general_cancel()}</AlertDialog.Cancel>
      <AlertDialog.Action onclick={() => (dialogOpen = false)}>
        {#snippet child({ props })}
          <form method="POST" action="?/{user.lock ? 'unlock' : 'lock'}" use:enhance>
            <Button
              {...props}
              type="submit"
              class={cn(
                'w-full',
                user.lock ? 'bg-blue-500 hover:bg-blue-500/90' : 'bg-red-500 hover:bg-red-500/90',
              )}
            >
              {user.lock ? m.admin_unlock() : m.admin_lock()}
            </Button>
          </form>
        {/snippet}
      </AlertDialog.Action>
    </AlertDialog.Footer>
  </AlertDialog.Content>
</AlertDialog.Root>
