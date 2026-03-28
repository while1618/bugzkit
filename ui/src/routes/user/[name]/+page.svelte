<script lang="ts">
  import * as m from '$lib/paraglide/messages.js';
  import type { PageProps } from './$types';

  const { data }: PageProps = $props();
</script>

<section>
  <div class="container">
    <div class="my-8 flex items-center justify-center px-4 sm:my-20 sm:px-0">
      <div class="flex max-w-lg flex-col items-center gap-5 text-center">
        <h1 class="text-6xl font-extrabold">{data.user.username}</h1>
        <p>{m.home_info()}</p>
      </div>
    </div>
  </div>
</section>
