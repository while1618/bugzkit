<script lang="ts">
  import * as AlertDialog from '$lib/components/ui/alert-dialog';
  import { Button, buttonVariants } from '$lib/components/ui/button';
  import type { AdminUser } from '$lib/models/user/user';
  import * as m from '$lib/paraglide/messages.js';
  import { cn } from '$lib/utils.js';
  import Trash2Icon from 'lucide-svelte/icons/trash-2';
  import { toast } from 'svelte-sonner';
  import { superForm } from 'sveltekit-superforms';
  import { zodClient } from 'sveltekit-superforms/adapters';
  import type { PageData } from '../$types';
  import { actionSchema } from '../schema';

  const { data, user }: { data: PageData; user: AdminUser } = $props();

  const superform = superForm(data.deleteForm, {
    validators: zodClient(actionSchema),
    id: `delete-form-${user.id}`,
    onSubmit({ formData }) {
      formData.set('id', `${user.id}`);
    },
  });
  const { message, errors, enhance } = superform;
  let dialogOpen = $state(false);

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
      <Button {...props} variant="ghost" class="text-red-500 hover:text-red-500/90">
        <Trash2Icon />
      </Button>
    {/snippet}
  </AlertDialog.Trigger>
  <AlertDialog.Content>
    <AlertDialog.Header>
      <AlertDialog.Title>{m.admin_deleteUser()}</AlertDialog.Title>
      <AlertDialog.Description>
        {m.admin_deleteUserConfirmation({ username: user.username })}
      </AlertDialog.Description>
    </AlertDialog.Header>
    <AlertDialog.Footer>
      <AlertDialog.Cancel>{m.general_cancel()}</AlertDialog.Cancel>
      <AlertDialog.Action onclick={() => (dialogOpen = false)}>
        {#snippet child({ props })}
          <form method="POST" action="?/delete" use:enhance>
            <Button
              {...props}
              type="submit"
              class={cn('w-full', buttonVariants({ variant: 'destructive' }))}
            >
              {m.general_delete()}
            </Button>
          </form>
        {/snippet}
      </AlertDialog.Action>
    </AlertDialog.Footer>
  </AlertDialog.Content>
</AlertDialog.Root>
