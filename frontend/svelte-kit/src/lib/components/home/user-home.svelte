<script lang="ts">
  import { Button } from '$lib/components/ui/button';
  import type { Profile } from '$lib/models/user/user';
  import * as m from '$lib/paraglide/messages.js';

  interface Props {
    profile: Profile;
  }

  const { profile }: Props = $props();
</script>

<section>
  <div class="container">
    <div class="m-20 flex items-center justify-center">
      <div class="flex max-w-lg flex-col items-center gap-5 text-center">
        <h1 class="text-6xl font-extrabold">{profile.username}</h1>
        <p>{m.home_info()}</p>
        <Button href="/" class="bg-blue-400 hover:bg-blue-300">{m.home_getStarted()}</Button>
      </div>
    </div>
  </div>
</section>
