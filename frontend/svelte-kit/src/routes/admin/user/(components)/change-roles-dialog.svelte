<script lang="ts">
  import { Button } from '$lib/components/ui/button';
  import { Checkbox } from '$lib/components/ui/checkbox';
  import * as Dialog from '$lib/components/ui/dialog';
  import * as Form from '$lib/components/ui/form';
  import { Label } from '$lib/components/ui/label';
  import type { AdminUser } from '$lib/models/user/user';
  import * as m from '$lib/paraglide/messages.js';
  import PencilIcon from 'lucide-svelte/icons/pencil';
  import { toast } from 'svelte-sonner';
  import { superForm } from 'sveltekit-superforms';
  import { zodClient } from 'sveltekit-superforms/adapters';
  import type { PageData } from '../$types';
  import { changeRolesSchema } from '../schema';

  const { data, user }: { data: PageData; user: AdminUser } = $props();

  const superform = superForm(data.changeRolesForm, {
    validators: zodClient(changeRolesSchema),
    id: `change-role-form-${user.id}`,
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

<Dialog.Root bind:open={dialogOpen}>
  <Dialog.Trigger>
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
        class="text-blue-500 hover:text-blue-500/90"
      >
        <PencilIcon />
      </Button>
    {/snippet}
  </Dialog.Trigger>
  <Dialog.Content>
    <Dialog.Header>
      <Dialog.Title>
        {m.admin_selectUserRoles({ username: user.username })}
      </Dialog.Title>
    </Dialog.Header>

    <form id="changeRolesForm" method="POST" action="?/changeRoles" use:enhance>
      <div class="flex flex-col gap-3">
        <Form.Fieldset form={superform} name="roleNames">
          {#each data.roles as role}
            {@const checked = user.roles.some((r) => r.name === role.name)}
            <Form.Control>
              {#snippet children({ props })}
                <div class="flex items-center space-x-2">
                  <Checkbox {...props} id={role.name} value={role.name} {checked} />
                  <Label class="cursor-pointer" for={role.name}>{role.name}</Label>
                </div>
              {/snippet}
            </Form.Control>
          {/each}
        </Form.Fieldset>
      </div>
    </form>

    <Dialog.Footer class="gap-2 sm:gap-0">
      <Button variant="outline" onclick={() => (dialogOpen = false)}>{m.general_cancel()}</Button>
      <Form.Button form="changeRolesForm" onclick={() => (dialogOpen = false)}>
        {m.general_save()}
      </Form.Button>
    </Dialog.Footer>
  </Dialog.Content>
</Dialog.Root>
