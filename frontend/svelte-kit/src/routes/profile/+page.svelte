<script lang="ts">
  import * as Card from '$lib/components/ui/card/index.js';
  import * as Form from '$lib/components/ui/form';
  import { Input } from '$lib/components/ui/input';
  import { Label } from '$lib/components/ui/label/index.js';
  import * as m from '$lib/paraglide/messages.js';
  import LoaderCircleIcon from 'lucide-svelte/icons/loader-circle';
  import { toast } from 'svelte-sonner';
  import { superForm } from 'sveltekit-superforms';
  import { zodClient } from 'sveltekit-superforms/adapters';
  import type { PageProps } from './$types';
  import { setUsernameSchema } from './schema';

  const { data }: PageProps = $props();

  const superform = superForm(data.setUsernameForm, {
    validators: zodClient(setUsernameSchema),
  });
  const { form, errors, enhance, delayed } = superform;

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
    <div class="m-20 flex flex-col items-center justify-center gap-4">
      {#if !data.profile?.username}
        <h1 class="text-2xl font-extrabold">{m.profile_setUsername()}</h1>
        <Card.Root class="w-[350px]">
          <Card.Content>
            <form
              class="flex flex-col gap-2"
              method="POST"
              action="?/setUsername"
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

              {#if $delayed}
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
      {:else}
        <div class="flex max-w-lg flex-col items-center gap-5 text-center">
          <h1 class="text-6xl font-extrabold">{data.profile?.username}</h1>
          <p>{m.home_info()}</p>
        </div>
      {/if}
    </div>
  </div>
</section>
