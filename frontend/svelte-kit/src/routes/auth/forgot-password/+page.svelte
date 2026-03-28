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
  import { forgotPasswordSchema } from './schema';

  const { data }: PageProps = $props();
  // svelte-ignore state_referenced_locally
  const superform = superForm(data.form, {
    validators: zod4Client(forgotPasswordSchema),
    onUpdate({ form }) {
      if (form.message) toast.success(form.message as string);
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
    <div class="my-8 flex items-center justify-center px-4 sm:my-20 sm:px-0">
      <div class="flex max-w-lg flex-col items-center gap-5">
        <Card.Root class="w-88">
          <Card.Header>
            <Card.Title class="text-2xl">{m.auth_forgotPassword()}</Card.Title>
          </Card.Header>
          <Card.Content>
            <form
              class="flex flex-col gap-3"
              method="POST"
              action="?/forgotPassword"
              use:enhance
              novalidate
            >
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
                  {m.general_send()}
                </Form.Button>
              {:else}
                <Form.Button>{m.general_send()}</Form.Button>
              {/if}
            </form>
          </Card.Content>
        </Card.Root>
      </div>
    </div>
  </div>
</section>
