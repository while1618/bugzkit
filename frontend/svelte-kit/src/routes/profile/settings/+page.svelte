<script lang="ts">
  import * as Tabs from '$lib/components/ui/tabs/index.js';
  import * as m from '$lib/paraglide/messages.js';
  import type { PageProps } from './$types';
  import Account from './(components)/account.svelte';
  import Devices from './(components)/devices.svelte';
  import Password from './(components)/password.svelte';

  const { data }: PageProps = $props();
</script>

<section>
  <div class="container">
    <div class="m-20 flex justify-center">
      <Tabs.Root value="account">
        <Tabs.List class="grid grid-cols-3">
          <Tabs.Trigger value="account">
            {m.profile_account()}
          </Tabs.Trigger>
          <Tabs.Trigger value="password">
            {m.profile_password()}
          </Tabs.Trigger>
          <Tabs.Trigger value="devices">
            {m.profile_devices()}
          </Tabs.Trigger>
        </Tabs.List>
        <Tabs.Content value="account">
          <Account {data} />
        </Tabs.Content>
        <Tabs.Content value="password">
          <Password {data} />
        </Tabs.Content>
        <Tabs.Content value="devices">
          <Devices {data} />
        </Tabs.Content>
      </Tabs.Root>
    </div>
  </div>
</section>
