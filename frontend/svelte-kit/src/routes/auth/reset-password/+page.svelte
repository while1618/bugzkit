<script lang="ts">
  import { page } from '$app/state';
  import * as Card from '$lib/components/ui/card';
  import * as Form from '$lib/components/ui/form';
  import { Input } from '$lib/components/ui/input';
  import { Label } from '$lib/components/ui/label';
  import * as m from '$lib/paraglide/messages.js';
  import LoaderCircleIcon from 'lucide-svelte/icons/loader-circle';
  import { toast } from 'svelte-sonner';
  import { superForm } from 'sveltekit-superforms';
  import { zodClient } from 'sveltekit-superforms/adapters';
  import type { PageProps } from './$types';
  import { resetPasswordSchema } from './schema';

  const { data }: PageProps = $props();
  const superform = superForm(data.form, {
    validators: zodClient(resetPasswordSchema),
  });
  const { form, errors, enhance, submitting } = superform;

  $effect(() => {
    if ($errors._errors) {
      for (const error of $errors._errors) {
        toast.error(error);
      }
    }
  });
</script>

<section>
  <div class="container">
    <div class="m-20 flex items-center justify-center">
      <div class="flex max-w-lg flex-col items-center gap-5">
        <Card.Root class="w-[350px]">
          <Card.Header>
            <Card.Title class="text-2xl">{m.auth_resetPassword()}</Card.Title>
          </Card.Header>
          <Card.Content>
            <form
              class="flex flex-col gap-2"
              method="POST"
              action="?/resetPassword&token={page.url.searchParams.get('token')}"
              use:enhance
              novalidate
            >
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

              {#if $submitting}
                <Form.Button disabled>
                  <LoaderCircleIcon class="animate-spin" />
                  {m.auth_resetPassword()}
                </Form.Button>
              {:else}
                <Form.Button>{m.auth_resetPassword()}</Form.Button>
              {/if}
            </form>
          </Card.Content>
        </Card.Root>
      </div>
    </div>
  </div>
</section>
