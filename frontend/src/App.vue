<template>
  <div class="app-shell">
    <header class="app-header">
      <div>
        <p class="app-kicker">Tarkov Community</p>
        <h1>逃离塔科夫玩家情报社区</h1>
      </div>
      <nav class="app-nav" aria-label="主导航">
        <RouterLink to="/">社区概览</RouterLink>
      </nav>
    </header>

    <main class="app-main">
      <RouterView />
    </main>
  </div>
</template>
