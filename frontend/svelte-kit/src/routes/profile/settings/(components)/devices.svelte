<script lang="ts">
  import * as AlertDialog from '$lib/components/ui/alert-dialog';
  import { Button, buttonVariants } from '$lib/components/ui/button';
  import * as Card from '$lib/components/ui/card/index.js';
  import Separator from '$lib/components/ui/separator/separator.svelte';
  import * as Table from '$lib/components/ui/table';
  import * as m from '$lib/paraglide/messages.js';
  import { toast } from 'svelte-sonner';
  import { superForm } from 'sveltekit-superforms';
  import { zodClient } from 'sveltekit-superforms/adapters';
  import { UAParser } from 'ua-parser-js';
  import type { PageData } from '../$types';
  import { revokeDeviceSchema } from '../schema';

  const { data }: { data: PageData } = $props();

  function parseUserAgent(ua: string | null): string {
    if (!ua) return '-';
    const parser = new UAParser(ua);
    const browser = parser.getBrowser();
    const os = parser.getOS();
    const browserStr = browser.name ? `${browser.name}`.trim() : '';
    const osStr = os.name ? `${os.name}`.trim() : '';
    if (browserStr && osStr) return `${browserStr} on ${osStr}`;
    return browserStr || osStr || ua;
  }

  const revokeDeviceSuperform = superForm(data.revokeDeviceForm, {
    validators: zodClient(revokeDeviceSchema),
  });
  const {
    form: revokeDeviceForm,
    message: revokeDeviceMessage,
    errors: revokeDeviceErrors,
    enhance: revokeDeviceEnhance,
  } = revokeDeviceSuperform;

  let dialogOpen = $state(false);
  let selectedDeviceId = $state('');

  $effect(() => {
    if ($revokeDeviceMessage) toast.success($revokeDeviceMessage);
    if ($revokeDeviceErrors._errors) {
      for (const error of $revokeDeviceErrors._errors) {
        toast.error(error);
      }
    }
  });

  function openRevokeDialog(deviceId: string) {
    selectedDeviceId = deviceId;
    $revokeDeviceForm.deviceId = deviceId;
    dialogOpen = true;
  }
</script>

<Card.Root class="w-[500px]">
  <Card.Content class="flex flex-col gap-5">
    {#if data.devices.length === 0}
      <p class="text-center text-sm text-muted-foreground">{m.profile_devicesEmpty()}</p>
    {:else}
      <Table.Root>
        <Table.Header>
          <Table.Row>
            <Table.Head>{m.profile_devicesUserAgent()}</Table.Head>
            <Table.Head>{m.profile_devicesLastActive()}</Table.Head>
            <Table.Head></Table.Head>
          </Table.Row>
        </Table.Header>
        <Table.Body>
          {#each data.devices as device}
            <Table.Row>
              <Table.Cell>
                {parseUserAgent(device.userAgent)}
                {#if device.current}
                  <span class="ml-2 text-xs text-muted-foreground"
                    >({m.profile_devicesCurrent()})</span
                  >
                {/if}
              </Table.Cell>
              <Table.Cell>
                {new Date(device.lastActiveAt).toLocaleString()}
              </Table.Cell>
              <Table.Cell class="text-right">
                {#if !device.current}
                  <Button
                    variant="destructive"
                    size="sm"
                    onclick={() => openRevokeDialog(device.deviceId)}
                  >
                    {m.profile_devicesRevoke()}
                  </Button>
                {/if}
              </Table.Cell>
            </Table.Row>
          {/each}
        </Table.Body>
      </Table.Root>

      <Separator />

      <Button href="/auth/sign-out-from-all-devices" class="w-full" variant="destructive">
        {m.profile_signOutFromAllDevices()}
      </Button>
    {/if}

    <AlertDialog.Root bind:open={dialogOpen}>
      <AlertDialog.Content>
        <AlertDialog.Header>
          <AlertDialog.Title>{m.profile_devicesRevoke()}</AlertDialog.Title>
          <AlertDialog.Description>{m.profile_devicesRevokeConfirmation()}</AlertDialog.Description>
        </AlertDialog.Header>
        <AlertDialog.Footer>
          <AlertDialog.Cancel>{m.general_cancel()}</AlertDialog.Cancel>
          <form method="POST" action="?/revokeDevice" use:revokeDeviceEnhance>
            <input type="hidden" name="deviceId" value={selectedDeviceId} />
            <AlertDialog.Action
              class={buttonVariants({ variant: 'destructive' })}
              onclick={() => (dialogOpen = false)}
            >
              {m.profile_devicesRevoke()}
            </AlertDialog.Action>
          </form>
        </AlertDialog.Footer>
      </AlertDialog.Content>
    </AlertDialog.Root>
  </Card.Content>
</Card.Root>
