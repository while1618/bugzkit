<script lang="ts">
  import type { Profile } from '$lib/models/user/user';
  import * as m from '$lib/paraglide/messages.js';

  const { profile }: { profile: Profile } = $props();
</script>

<section>
  <div class="container">
    <div class="my-8 flex items-center justify-center px-4 sm:my-20 sm:px-0">
      <div class="flex max-w-lg flex-col items-center gap-5 text-center">
        <h1 class="text-6xl font-extrabold">{m.home_greetings()}, {profile.username}</h1>
        <p>{m.home_info()}</p>
      </div>
    </div>
  </div>
</section>
