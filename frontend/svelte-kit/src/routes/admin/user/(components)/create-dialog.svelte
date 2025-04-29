<script lang="ts">
  import { Button } from '$lib/components/ui/button';
  import { Checkbox } from '$lib/components/ui/checkbox';
  import * as Dialog from '$lib/components/ui/dialog';
  import * as Form from '$lib/components/ui/form';
  import { Input } from '$lib/components/ui/input';
  import { Label } from '$lib/components/ui/label';
  import { RoleName } from '$lib/models/user/role';
  import * as m from '$lib/paraglide/messages.js';
  import LoaderCircleIcon from 'lucide-svelte/icons/loader-circle';
  import { toast } from 'svelte-sonner';
  import { superForm } from 'sveltekit-superforms';
  import { zodClient } from 'sveltekit-superforms/adapters';
  import type { PageData } from '../$types';
  import { createSchema } from '../schema';

  const { data }: { data: PageData } = $props();

  const superform = superForm(data.createForm, {
    validators: zodClient(createSchema),
  });
  const { form, message, errors, enhance, submitting } = superform;
  let dialogOpen = $state(false);

  $effect(() => {
    if ($message) {
      toast.success($message);
      dialogOpen = false;
    }
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
      <Button {...props}>{m.admin_userCreate()}</Button>
    {/snippet}
  </Dialog.Trigger>
  <Dialog.Content>
    <Dialog.Header>
      <Dialog.Title>{m.admin_userCreate()}</Dialog.Title>
    </Dialog.Header>

    <form
      id="createForm"
      class="flex flex-col gap-2"
      method="POST"
      action="?/create"
      use:enhance
      novalidate
    >
      <Form.Field form={superform} name="username">
        <Form.Control>
          {#snippet children({ props })}
            <Label>{m.auth_username()}</Label>
            <Input {...props} bind:value={$form.username} />
          {/snippet}
        </Form.Control>
        <Form.FieldErrors />
      </Form.Field>

      <Form.Field form={superform} name="email">
        <Form.Control>
          {#snippet children({ props })}
            <Label>{m.auth_email()}</Label>
            <Input type="email" {...props} bind:value={$form.email} />
          {/snippet}
        </Form.Control>
        <Form.FieldErrors />
      </Form.Field>

      <Form.Field form={superform} name="password">
        <Form.Control>
          {#snippet children({ props })}
            <Label>{m.auth_password()}</Label>
            <Input type="password" {...props} bind:value={$form.password} />
          {/snippet}
        </Form.Control>
        <Form.FieldErrors />
      </Form.Field>

      <Form.Field form={superform} name="confirmPassword">
        <Form.Control>
          {#snippet children({ props })}
            <Label>{m.auth_confirmPassword()}</Label>
            <Input type="password" {...props} bind:value={$form.confirmPassword} />
          {/snippet}
        </Form.Control>
        <Form.FieldErrors />
      </Form.Field>

      <div class="flex flex-col gap-2">
        <Label>{m.general_status()}</Label>

        <Form.Field form={superform} name="active">
          <div class="flex flex-row items-start space-x-2">
            <Form.Control>
              {#snippet children({ props })}
                <Checkbox {...props} bind:checked={$form.active} />
                <Form.Label class="text-sm font-normal">{m.admin_userActive()}</Form.Label>
              {/snippet}
            </Form.Control>
            <Form.FieldErrors />
          </div>
        </Form.Field>

        <Form.Field form={superform} name="lock">
          <div class="flex flex-row items-start space-x-2">
            <Form.Control>
              {#snippet children({ props })}
                <Checkbox {...props} bind:checked={$form.lock} />
                <Form.Label class="text-sm font-normal">{m.admin_userLock()}</Form.Label>
              {/snippet}
            </Form.Control>
            <Form.FieldErrors />
          </div>
        </Form.Field>
      </div>

      <Form.Fieldset form={superform} name="roleNames">
        <Label>{m.admin_userRoles()}</Label>
        {#each data.roles as role}
          <Form.Control>
            {#snippet children({ props })}
              <div class="flex space-x-2">
                <Checkbox
                  {...props}
                  id={role.name}
                  value={role.name}
                  checked={role.name === RoleName.USER}
                />
                <Label class="cursor-pointer" for={role.name}>{role.name}</Label>
              </div>
            {/snippet}
          </Form.Control>
        {/each}
      </Form.Fieldset>
    </form>

    <Dialog.Footer class="gap-2 sm:gap-0">
      <Button variant="outline" onclick={() => (dialogOpen = false)}>{m.general_cancel()}</Button>
      {#if $submitting}
        <Form.Button disabled>
          <LoaderCircleIcon class="animate-spin" />
          {m.general_save()}
        </Form.Button>
      {:else}
        <Form.Button form="createForm">
          {m.general_save()}
        </Form.Button>
      {/if}
    </Dialog.Footer>
  </Dialog.Content>
</Dialog.Root>
