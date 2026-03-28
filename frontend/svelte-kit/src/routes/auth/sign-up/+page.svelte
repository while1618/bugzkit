<script lang="ts">
  import * as Card from '$lib/components/ui/card';
  import * as Form from '$lib/components/ui/form';
  import { Input } from '$lib/components/ui/input';
  import { Label } from '$lib/components/ui/label';
  import * as m from '$lib/paraglide/messages.js';
  import LoaderCircleIcon from 'lucide-svelte/icons/loader-circle';
  import { toast } from 'svelte-sonner';
  import { superForm } from 'sveltekit-superforms';
  import { zod4Client } from 'sveltekit-superforms/adapters';
  import type { PageProps } from './$types';
  import { signUpSchema } from './schema';

  const { data }: PageProps = $props();
  const superform = superForm(data.form, {
    validators: zod4Client(signUpSchema),
    onUpdate({ form }) {
      if (form.errors._errors) {
        for (const error of form.errors._errors) {
          toast.error(error);
        }
      }
    },
  });
  const { form, enhance, submitting } = superform;
</script>

<section>
  <div class="container">
    <div class="m-20 flex items-center justify-center">
      <div class="flex max-w-lg flex-col items-center gap-5">
        <Card.Root class="w-88">
          <Card.Header>
            <Card.Title class="text-2xl">{m.auth_signUp()}</Card.Title>
          </Card.Header>
          <Card.Content>
            <form
              class="flex flex-col gap-2"
              method="POST"
              action="?/signUp"
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

              {#if $submitting}
                <Form.Button disabled>
                  <LoaderCircleIcon class="animate-spin" />
                  {m.auth_signUp()}
                </Form.Button>
              {:else}
                <Form.Button>{m.auth_signUp()}</Form.Button>
              {/if}

              <div class="flex items-center justify-center gap-1">
                <Label>{m.auth_alreadyHaveAnAccount()}</Label>
                <a href="/auth/sign-in" class="text-sm font-medium underline">
                  {m.auth_signIn()}
                </a>
              </div>
            </form>
          </Card.Content>
        </Card.Root>
      </div>
    </div>
  </div>
</section>
