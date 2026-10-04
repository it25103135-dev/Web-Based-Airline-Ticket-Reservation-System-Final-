/* Lanka Wings - User + Notification Management UI behaviour. */
document.addEventListener('DOMContentLoaded', () => {
  const $ = (s, r = document) => r.querySelector(s);
  const $$ = (s, r = document) => [...r.querySelectorAll(s)];

  // Mobile navigation.
  const toggle = $('.nav-toggle'), nav = $('.nav-links');
  if (toggle && nav) toggle.addEventListener('click', () => nav.classList.toggle('open'));

  // Toast messages.
  const dismiss = t => { t.classList.add('leaving'); setTimeout(() => t.remove(), 400); };
  $$('.toast').forEach(t => {
    $('.toast-close', t)?.addEventListener('click', () => dismiss(t));
    setTimeout(() => dismiss(t), t.classList.contains('error') ? 9000 : 6000);
  });

  // Confirmation prompts for sensitive account / notification actions.
  $$('form[data-confirm]').forEach(f => f.addEventListener('submit', e => {
    if (!confirm(f.dataset.confirm)) {
      e.preventDefault();
      e.stopImmediatePropagation();
    }
  }, true));

  // Live unread notification badge. Refresh immediately and every 30 seconds.
  const bell = $('[data-unread-url]'), badge = $('.bell-badge');
  if (bell && badge) {
    const show = n => {
      const prev = Number(String(badge.textContent || '0').replace('+', '')) || 0;
      badge.textContent = n > 99 ? '99+' : String(n);
      badge.hidden = n === 0;
      if (n > prev) {
        bell.classList.remove('ring');
        void bell.offsetWidth;
        bell.classList.add('ring');
      }
    };
    const refresh = () => {
      if (document.hidden) return;
      fetch(bell.dataset.unreadUrl, {
        credentials: 'same-origin',
        headers: { Accept: 'application/json' }
      }).then(r => r.ok ? r.json() : null)
        .then(j => j && Number.isFinite(Number(j.unread)) && show(Number(j.unread)))
        .catch(() => {});
    };
    refresh();
    setInterval(refresh, 30000);
  }

  // Client-side table filtering for User Management.
  $$('[data-filter]').forEach(input => {
    const target = $(input.dataset.filter);
    const itemSelector = input.dataset.filterItem;
    const statusSelect = $(input.dataset.statusSelect || '__none__');
    if (!target || !itemSelector) return;

    const run = () => {
      const q = input.value.trim().toLowerCase();
      const status = statusSelect ? statusSelect.value : '';
      $$(itemSelector, target).forEach(row => {
        const matchesText = !q || row.textContent.toLowerCase().includes(q);
        const matchesStatus = !status || (row.dataset.status || '') === status;
        row.classList.toggle('hidden-by-filter', !(matchesText && matchesStatus));
      });
    };

    input.addEventListener('input', run);
    if (statusSelect) statusSelect.addEventListener('change', run);
  });
});
