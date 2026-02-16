<script lang="ts">
  import * as Card from '$lib/components/ui/card/index.js';
  import * as Form from '$lib/components/ui/form';
  import { Input } from '$lib/components/ui/input/index.js';
  import { Label } from '$lib/components/ui/label/index.js';
  import * as m from '$lib/paraglide/messages.js';
  import LoaderCircleIcon from 'lucide-svelte/icons/loader-circle';
  import { toast } from 'svelte-sonner';
  import { superForm } from 'sveltekit-superforms';
  import { zodClient } from 'sveltekit-superforms/adapters';
  import type { PageData } from '../$types';
  import { changePasswordSchema } from '../schema';

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

  $effect(() => {
    if ($changePasswordMessage) toast.success($changePasswordMessage);
    if ($changePasswordErrors._errors) {
      for (const error of $changePasswordErrors._errors) {
        toast.error(error);
      }
    }
  });
</script>

<Card.Root class="w-[500px]">
  <Card.Content>
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
  </Card.Content>
</Card.Root>
