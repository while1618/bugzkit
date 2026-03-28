<script lang="ts">
  import { Button } from '$lib/components/ui/button';
  import * as m from '$lib/paraglide/messages.js';
</script>

<section>
  <div class="container">
    <div class="my-8 flex items-center justify-center px-4 sm:my-20 sm:px-0">
      <div class="flex max-w-lg flex-col items-center gap-5 text-center">
        <h1 class="text-6xl font-extrabold">{m.home_greetings()}</h1>
        <p>{m.home_info()}</p>
        <div class="flex gap-4">
          <Button href="/auth/sign-in">{m.auth_signIn()}</Button>
          <Button href="/auth/sign-up" variant="outline">{m.auth_signUp()}</Button>
        </div>
      </div>
    </div>
  </div>
</section>
