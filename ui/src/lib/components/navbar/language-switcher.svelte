<script lang="ts">
  import * as Select from '$lib/components/ui/select';
  import * as m from '$lib/paraglide/messages.js';
  import { getLocale, locales, setLocale } from '$lib/paraglide/runtime.js';

  type Locale = (typeof locales)[number];

  const labels: Record<Locale, string> = {
    en: `🇬🇧 ${m.language_english()}`,
    sr: `🇷🇸 ${m.language_serbian()}`,
  };

  let selectedLanguage = $state(getLocale());

  const changeLanguage = () => {
    setLocale(selectedLanguage);
  };
</script>

<Select.Root type="single" bind:value={selectedLanguage} onValueChange={changeLanguage}>
  <Select.Trigger class="w-45">{labels[selectedLanguage]}</Select.Trigger>
  <Select.Content>
    <Select.Group>
      <Select.GroupHeading>{m.languages()}</Select.GroupHeading>
      {#each locales as lang (lang)}
        <Select.Item value={lang} label={labels[lang]} />
      {/each}
    </Select.Group>
  </Select.Content>
</Select.Root>
