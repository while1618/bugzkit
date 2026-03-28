<script lang="ts">
  import { page } from '$app/state';
  import { Button } from '$lib/components/ui/button';
  import * as m from '$lib/paraglide/messages.js';
</script>

<section>
  <div class="container">
    <div class="my-8 flex items-center justify-center px-4 sm:my-20 sm:px-0">
      <div class="flex max-w-lg flex-col items-center gap-5 text-center">
        <h1 class="text-6xl font-extrabold">{page.status}</h1>
        <p>{page.error?.message}</p>
        <Button href="/">{m.general_homepage()}</Button>
      </div>
    </div>
  </div>
</section>
