<script lang="ts">
  import * as AlertDialog from '$lib/components/ui/alert-dialog';
  import { Button, buttonVariants } from '$lib/components/ui/button';
  import * as Card from '$lib/components/ui/card/index.js';
  import { Separator } from '$lib/components/ui/separator';
  import * as m from '$lib/paraglide/messages.js';
  import { toast } from 'svelte-sonner';
  import { superForm } from 'sveltekit-superforms';
  import { zod4Client } from 'sveltekit-superforms/adapters';
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
    validators: zod4Client(revokeDeviceSchema),
    onUpdate({ form }) {
      if (form.message) toast.success(form.message as string);
      if (form.errors._errors) {
        for (const error of form.errors._errors) {
          toast.error(error);
        }
      }
    },
  });
  const { form: revokeDeviceForm, enhance: revokeDeviceEnhance } = revokeDeviceSuperform;

  let dialogOpen = $state(false);
  let selectedDeviceId = $state('');

  function openRevokeDialog(deviceId: string) {
    selectedDeviceId = deviceId;
    $revokeDeviceForm.deviceId = deviceId;
    dialogOpen = true;
  }
</script>

<Card.Root class="w-full">
  <Card.Content class="flex flex-col gap-4">
    {#if data.devices.length === 0}
      <p class="text-center text-sm text-muted-foreground">{m.profile_devicesEmpty()}</p>
    {:else}
      <div class="flex flex-col">
        {#each data.devices as device, i (device.deviceId)}
          {#if i > 0}
            <Separator />
          {/if}
          <div class="flex items-center justify-between gap-4 py-3">
            <div class="flex min-w-0 flex-col gap-0.5">
              <span class="truncate text-sm font-medium">
                {parseUserAgent(device.userAgent)}
                {#if device.current}
                  <span class="text-xs text-muted-foreground">({m.profile_devicesCurrent()})</span>
                {/if}
              </span>
              <span class="text-xs text-muted-foreground">
                {new Date(device.lastActiveAt).toLocaleString()}
              </span>
            </div>
            {#if !device.current}
              <Button
                variant="destructive"
                size="sm"
                onclick={() => openRevokeDialog(device.deviceId)}
              >
                {m.profile_devicesRevoke()}
              </Button>
            {/if}
          </div>
        {/each}
      </div>

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
