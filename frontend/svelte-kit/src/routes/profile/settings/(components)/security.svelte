<script lang="ts">
  import * as AlertDialog from '$lib/components/ui/alert-dialog';
  import { Button, buttonVariants } from '$lib/components/ui/button';
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
  import { changePasswordSchema, deleteSchema } from '../schema';

  const { data }: { data: PageData } = $props();

  const changePasswordSuperform = superForm(data.changePasswordForm, {
    validators: zodClient(changePasswordSchema),
  });
  const {
    form: changePasswordForm,
    message: changePasswordMessage,
    errors: changePasswordErrors,
    enhance: changePasswordEnhance,
    submitting: changePasswordSubmitting,
  } = changePasswordSuperform;

  const deleteSuperform = superForm(data.deleteForm, {
    validators: zodClient(deleteSchema),
  });
  const { errors: deleteErrors, enhance: deleteEnhance } = deleteSuperform;
  let deleteDialogOpen = $state(false);

  $effect(() => {
    if ($changePasswordMessage) toast.success($changePasswordMessage);
    if ($changePasswordErrors._errors) {
      for (const error of $changePasswordErrors._errors) {
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

<Card.Root class="w-[350px]">
  <Card.Content class="flex flex-col gap-5">
    <form
      class="flex flex-col gap-2"
      method="POST"
      action="?/changePassword&username={data.profile?.username}"
      use:changePasswordEnhance
      novalidate
    >
      <Form.Field form={changePasswordSuperform} name="currentPassword">
        <Form.Control>
          {#snippet children({ props })}
            <Label>{m.profile_currentPassword()}</Label>
            <Input type="password" {...props} bind:value={$changePasswordForm.currentPassword} />
          {/snippet}
        </Form.Control>
        <Form.FieldErrors />
      </Form.Field>

      <Form.Field form={changePasswordSuperform} name="newPassword">
        <Form.Control>
          {#snippet children({ props })}
            <Label>{m.profile_newPassword()}</Label>
            <Input type="password" {...props} bind:value={$changePasswordForm.newPassword} />
          {/snippet}
        </Form.Control>
        <Form.FieldErrors />
      </Form.Field>

      <Form.Field form={changePasswordSuperform} name="confirmNewPassword">
        <Form.Control>
          {#snippet children({ props })}
            <Label>{m.profile_confirmNewPassword()}</Label>
            <Input type="password" {...props} bind:value={$changePasswordForm.confirmNewPassword} />
          {/snippet}
        </Form.Control>
        <Form.FieldErrors />
      </Form.Field>

      {#if $changePasswordSubmitting}
        <Form.Button disabled>
          <LoaderCircleIcon class="animate-spin" />
          {m.general_save()}
        </Form.Button>
      {:else}
        <Form.Button>{m.general_save()}</Form.Button>
      {/if}
    </form>

    <Separator />

    <Button href="/auth/sign-out-from-all-devices" class="w-full" variant="destructive">
      {m.profile_signOutFromAllDevices()}
    </Button>

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
