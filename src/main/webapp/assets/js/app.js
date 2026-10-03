/* Lanka Wings - UI behaviour (no inline scripts, so the strict Content-Security-Policy stays intact). */
document.addEventListener('DOMContentLoaded', () => {
  const $ = (s, r = document) => r.querySelector(s), $$ = (s, r = document) => [...r.querySelectorAll(s)];

  // scroll reveal
  const io = 'IntersectionObserver' in window ? new IntersectionObserver(es => es.forEach(e => { if (e.isIntersecting) { e.target.classList.add('visible'); io.unobserve(e.target); } }), { threshold: .08 }) : null;
  $$('.reveal').forEach((el, i) => { if (io) { el.style.transitionDelay = Math.min(i * 45, 260) + 'ms'; io.observe(el); } else el.classList.add('visible'); });

  // mobile nav
  const toggle = $('.nav-toggle'), nav = $('.nav-links');
  if (toggle && nav) toggle.addEventListener('click', () => nav.classList.toggle('open'));

  // count-up numbers
  $$('[data-count]').forEach(el => {
    const end = Number(el.dataset.count || 0), fmt = el.dataset.format === 'money';
    let n = 0; const step = Math.max(1, Math.ceil(end / 30));
    const t = setInterval(() => { n = Math.min(end, n + step); el.textContent = fmt ? n.toLocaleString() : n; if (n >= end) clearInterval(t); }, 30);
  });

  // toasts: auto dismiss + close button
  const dismiss = t => { t.classList.add('leaving'); setTimeout(() => t.remove(), 400); };
  $$('.toast').forEach(t => { $('.toast-close', t)?.addEventListener('click', () => dismiss(t)); setTimeout(() => dismiss(t), t.classList.contains('error') ? 9000 : 6000); });

  // confirm dialogs (replaces inline onsubmit)
  $$('form[data-confirm]').forEach(f => f.addEventListener('submit', e => { if (!confirm(f.dataset.confirm)) { e.preventDefault(); e.stopImmediatePropagation(); } }, true));
  $$('[data-print]').forEach(b => b.addEventListener('click', () => window.print()));

  // live notification bell (polls every 30s)
// client-side filter for lists/tables:  <input data-filter="#target" data-filter-item=".row">
  $$('[data-filter]').forEach(input => {
    const target = $(input.dataset.filter), sel = input.dataset.filterItem, statusSel = $(input.dataset.statusSelect || '__none__');
    if (!target) return;
    const empty = $('.no-results', target.parentElement);
    const run = () => {
      const q = input.value.trim().toLowerCase(), st = statusSel ? statusSel.value : ''; let shown = 0;
      $$(sel, target).forEach(it => { const ok = (!q || it.textContent.toLowerCase().includes(q)) && (!st || (it.dataset.status || '').includes(st)); it.classList.toggle('hidden-by-filter', !ok); if (ok) shown++; });
      if (empty) empty.classList.toggle('show', shown === 0);
    };
    input.addEventListener('input', run); statusSel && statusSel.addEventListener('change', run);
  });

  // seat map: show chosen seat in the summary
  const picked = $('[data-seat-picked]');
  $$('.seat input').forEach(r => r.addEventListener('change', () => { if (picked) picked.textContent = r.value; const m = $('.group-msg'); if (m) m.textContent = ''; }));
  const pre = $('.seat input:checked'); if (pre && picked) picked.textContent = pre.value;

  // live credit-card preview
  const card = $('.cc-card');
  if (card) {
    const num = $('#cardNumber'), name = $('#cardHolder'), exp = $('#expiry'), cvv = $('#cvv');
    const setText = (sel, v) => { const e = $(sel, card); if (e) e.textContent = v; };
    const brand = () => { const d = (num?.value || '').replace(/\s/g, ''); const b = /^4/.test(d) ? 'visa' : (/^5[1-5]/.test(d) || (+d.slice(0, 4) >= 2221 && +d.slice(0, 4) <= 2720)) ? 'mastercard' : ''; card.classList.remove('visa', 'mastercard'); if (b) card.classList.add(b); setText('.cc-brand', b === 'visa' ? 'VISA' : b === 'mastercard' ? 'MASTERCARD' : 'CARD'); };
    const upd = () => {
      const d = (num?.value || '').replace(/\s/g, '').padEnd(16, '•'); setText('.cc-number', d.replace(/(.{4})/g, '$1 ').trim());
      setText('.cc-name', (name?.value || '').trim() || 'FULL NAME'); setText('.cc-exp', exp?.value || 'MM/YY'); setText('.cc-cvv', (cvv?.value || '').padEnd(3, '•')); brand();
    };
    [num, name, exp, cvv].forEach(e => e && e.addEventListener('input', upd));
    cvv && cvv.addEventListener('focus', () => card.classList.add('flipped')); cvv && cvv.addEventListener('blur', () => card.classList.remove('flipped'));
    upd();
  }

  // barcode drawn from the ticket number
  $$('.barcode-bars[data-code]').forEach(el => {
    const code = el.dataset.code; let h = 7;
    for (let i = 0; i < 46; i++) { h = (h * 31 + code.charCodeAt(i % code.length) + i) >>> 0; const b = document.createElement('i'); b.style.width = (1 + (h % 4)) + 'px'; b.style.animationDelay = (i * 12) + 'ms'; el.appendChild(b); }
  });

  // live clock on the home departure board
  const clock = $('[data-clock]');
  if (clock) { const tick = () => clock.textContent = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' }); tick(); setInterval(tick, 1000); }
});
