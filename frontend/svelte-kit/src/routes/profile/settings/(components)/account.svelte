<script lang="ts">
  import * as AlertDialog from '$lib/components/ui/alert-dialog';
  import { buttonVariants } from '$lib/components/ui/button';
  import Button from '$lib/components/ui/button/button.svelte';
  import * as Card from '$lib/components/ui/card/index.js';
  import * as Form from '$lib/components/ui/form';
  import { Input } from '$lib/components/ui/input/index.js';
  import { Label } from '$lib/components/ui/label/index.js';
  import { Separator } from '$lib/components/ui/separator';
  import * as m from '$lib/paraglide/messages.js';
  import LoaderCircleIcon from 'lucide-svelte/icons/loader-circle';
  import { toast } from 'svelte-sonner';
  import { superForm } from 'sveltekit-superforms';
  import { zodClient } from 'sveltekit-superforms/adapters';
  import type { PageData } from '../$types';
  import { deleteSchema, updateProfileSchema } from '../schema';

  const { data }: { data: PageData } = $props();

  const superform = superForm(data.updateProfileForm, {
    validators: zodClient(updateProfileSchema),
    resetForm: false,
  });
  const { form, message, errors, enhance, submitting } = superform;

  const deleteSuperform = superForm(data.deleteForm, {
    validators: zodClient(deleteSchema),
  });
  const { errors: deleteErrors, enhance: deleteEnhance } = deleteSuperform;
  let deleteDialogOpen = $state(false);

  $effect(() => {
    if ($message) toast.success($message);
    if ($errors._errors) {
      for (const error of $errors._errors) {
        toast.error(error);
      }
    }
    if ($deleteErrors._errors) {
      for (const error of $deleteErrors._errors) {
        toast.error(error);
      }
    }
  });
</script>

<Card.Root class="w-[500px]">
  <Card.Content class="flex flex-col gap-5">
    <form class="flex flex-col gap-3" method="POST" action="?/updateProfile" use:enhance novalidate>
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

      {#if $submitting}
        <Form.Button disabled>
          <LoaderCircleIcon class="animate-spin" />
          {m.general_save()}
        </Form.Button>
      {:else}
        <Form.Button>{m.general_save()}</Form.Button>
      {/if}
    </form>

    <Separator />

    <AlertDialog.Root bind:open={deleteDialogOpen}>
      <AlertDialog.Trigger>
        {#snippet child({ props })}
          <Button {...props} class="w-full" variant="destructive">{m.profile_delete()}</Button>
        {/snippet}
      </AlertDialog.Trigger>
      <AlertDialog.Content>
        <AlertDialog.Header>
          <AlertDialog.Title>{m.profile_delete()}</AlertDialog.Title>
          <AlertDialog.Description>{m.profile_deleteAccountConfirmation()}</AlertDialog.Description>
        </AlertDialog.Header>
        <AlertDialog.Footer>
          <AlertDialog.Cancel>{m.general_cancel()}</AlertDialog.Cancel>
          <form method="POST" action="?/delete" use:deleteEnhance>
            <AlertDialog.Action
              class={buttonVariants({ variant: 'destructive' })}
              onclick={() => (deleteDialogOpen = false)}
            >
              {m.general_delete()}
            </AlertDialog.Action>
          </form>
        </AlertDialog.Footer>
      </AlertDialog.Content>
    </AlertDialog.Root>
  </Card.Content>
</Card.Root>
