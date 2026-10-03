/* Lanka Wings - live form validation.
   These rules MIRROR the server-side rules in Validator.java. The browser check is only for instant
   feedback; the server re-checks everything, so bypassing this file gains an attacker nothing.

   Usage: <form data-validate> ... <input data-rule="phone" required> ... </form>            */
(function () {
  'use strict';

  const L = '\\p{L}\\p{M}';
  const luhn = d => { let s = 0, dbl = false; for (let i = d.length - 1; i >= 0; i--) { let n = +d[i]; if (dbl) { n *= 2; if (n > 9) n -= 9; } s += n; dbl = !dbl; } return s % 10 === 0; };
  const brandOf = d => /^4/.test(d) ? 'VISA' : (/^(5[1-5])/.test(d) || (+d.slice(0, 4) >= 2221 && +d.slice(0, 4) <= 2720)) ? 'MASTERCARD' : 'UNKNOWN';
  const num = (el, k, dflt) => el.dataset[k] !== undefined ? Number(el.dataset[k]) : dflt;

  // Each rule: fmt(value, event) -> cleaned value while typing;  check(value, el) -> error text or ''
  const RULES = {
    name: {
      fmt: v => v.replace(new RegExp('[^' + L + " .'\\-]", 'gu'), '').replace(/ {2,}/g, ' ').slice(0, 80),
      check: v => { v = v.trim(); if (v.length < 2) return 'Enter at least 2 letters.';
        return new RegExp('^[' + L + "][" + L + " .'\\-]+$", 'u').test(v) ? '' : 'Only letters, spaces and . \' - are allowed.'; }
    },
    cardholder: {
      fmt: v => v.replace(new RegExp('[^' + L + " .'\\-]", 'gu'), '').replace(/ {2,}/g, ' ').slice(0, 60),
      check: v => v.trim().length < 2 ? 'Enter the name exactly as printed on the card.' : ''
    },
    username: {
      fmt: v => v.replace(/[^A-Za-z0-9._-]/g, '').slice(0, 30),
      check: v => v.length < 4 ? 'At least 4 characters (' + v.length + ' entered).' : !/^[A-Za-z]/.test(v) ? 'Must start with a letter.' : ''
    },
    email: {
      fmt: v => v.replace(/\s/g, '').slice(0, 120),
      check: v => /^[A-Za-z0-9._%+\-]+@[A-Za-z0-9-]+(\.[A-Za-z0-9-]+)*\.[A-Za-z]{2,}$/.test(v) ? '' : 'Enter a valid email, e.g. name@example.com.'
    },
    // Sri Lankan mobile: exactly 10 digits, 070-078.  +94 / 94 prefixes are converted to a leading 0.
    phone: {
      fmt: v => { let d = v.replace(/[^\d+]/g, ''); if (d.startsWith('+94')) d = '0' + d.slice(3); else if (d.startsWith('0094')) d = '0' + d.slice(4); else if (/^94\d{9}$/.test(d)) d = '0' + d.slice(2); return d.replace(/\D/g, '').slice(0, 10); },
      check: v => v.length < 10 ? 'Too short: exactly 10 digits needed (' + v.length + ' entered), e.g. 0771234567.' : !/^07[0-8]\d{7}$/.test(v) ? 'Enter a valid Sri Lankan mobile number starting with 070 to 078.' : ''
    },
    newpassword: {
      fmt: v => v.replace(/\s/g, '').slice(0, 64),
      check: v => v.length < 8 ? 'At least 8 characters.' : !/[a-z]/.test(v) ? 'Add a lowercase letter.' : !/[A-Z]/.test(v) ? 'Add an uppercase letter.' : !/\d/.test(v) ? 'Add a number.' : /^[A-Za-z0-9]+$/.test(v) ? 'Add a special character (@ # $ % !).' : ''
    },
    passport: {
      fmt: v => v.toUpperCase().replace(/[^A-Z0-9]/g, '').slice(0, 15),
      check: v => v.length < 6 ? 'Passport / ID must be 6-15 letters or digits (' + v.length + ' entered).' : ''
    },
    flightno: {
      fmt: v => v.toUpperCase().replace(/[^A-Z0-9]/g, '').slice(0, 6),
      check: v => /^[A-Z]{2}\d{2,4}$/.test(v) ? '' : '2 letters + 2-4 digits, e.g. LW101.'
    },
    seat: {
      fmt: v => v.toUpperCase().replace(/[^0-9A-F]/g, '').slice(0, 3),
      check: v => /^[1-9]\d?[A-F]$/.test(v) ? '' : 'Seat looks like 12A (row number + letter A-F).'
    },
    place: {
      fmt: v => v.replace(new RegExp("[^" + L + " ().,'\\-]", 'gu'), '').slice(0, 80),
      check: v => v.trim().length < 2 ? 'Enter a city / airport, e.g. Colombo (CMB).' : ''
    },
    aircraft: {
      fmt: v => v.replace(/[^A-Za-z0-9 .\-\/]/g, '').slice(0, 80),
      check: v => v.trim().length < 2 ? 'Enter the aircraft type.' : ''
    },
    cardnumber: {
      fmt: v => v.replace(/\D/g, '').slice(0, 16).replace(/(.{4})/g, '$1 ').trim(),
      check: (v, el) => {
        const d = v.replace(/\s/g, '');
        if (d.length < 16) return 'Too short: 16 digits needed (' + d.length + ' entered).';
        if (!luhn(d)) return 'This card number is not valid. Check for typing mistakes.';
        const b = brandOf(d), m = document.querySelector(el.dataset.method + ':checked');
        if (b === 'UNKNOWN') return 'Only Visa and Mastercard are accepted.';
        if (m && m.value === 'Visa' && b !== 'VISA') return 'This is not a Visa card. Choose the right payment method.';
        if (m && m.value === 'Mastercard' && b !== 'MASTERCARD') return 'This is not a Mastercard. Choose the right payment method.';
        return '';
      }
    },
    expiry: {
      fmt: (v, e) => { let d = v.replace(/\D/g, '').slice(0, 4); if (d.length === 1 && d > '1') d = '0' + d;
        if (d.length > 2) d = d.slice(0, 2) + '/' + d.slice(2); else if (d.length === 2 && !(e && /delete/.test(e.inputType || ''))) d += '/'; return d; },
      check: v => {
        const m = /^(0[1-9]|1[0-2])\/(\d{2})$/.exec(v); if (!m) return 'Use MM/YY, e.g. 08/28.';
        const now = new Date(), cur = now.getFullYear() * 12 + now.getMonth(), exp = (2000 + +m[2]) * 12 + (+m[1] - 1);
        return exp < cur ? 'This card has expired.' : exp > cur + 144 ? 'Expiry date is too far in the future.' : '';
      }
    },
    cvv: {
      fmt: v => v.replace(/\D/g, '').slice(0, 3),
      check: v => v.length !== 3 ? 'CVV is exactly 3 digits (' + v.length + ' entered).' : ''
    },
    text: { fmt: (v, e, el) => v.slice(0, num(el, 'max', 1000)),
      check: (v, el) => { const n = v.trim().length, min = num(el, 'min', 1); return n < min ? 'At least ' + min + ' characters (' + n + ' entered).' : ''; } },
    money: {
      fmt: v => { v = v.replace(/[^\d.]/g, ''); const p = v.split('.'); return p.length > 1 ? p[0] + '.' + p.slice(1).join('').slice(0, 2) : v; },
      check: (v, el) => { const n = Number(v), lo = num(el, 'min', 1), hi = num(el, 'max', 1e6); return !v || isNaN(n) ? 'Enter a valid amount.' : (n < lo || n > hi) ? 'Must be between ' + lo.toLocaleString() + ' and ' + hi.toLocaleString() + '.' : ''; }
    },
    int: {
      fmt: v => v.replace(/\D/g, '').slice(0, 4),
      check: (v, el) => { const n = Number(v), lo = num(el, 'min', 1), hi = num(el, 'max', 500); return !v ? 'Enter a whole number.' : (n < lo || n > hi) ? 'Must be between ' + lo + ' and ' + hi + '.' : ''; }
    },
    datetime: {
      fmt: v => v,
      check: (v, el) => {
        if (!v) return 'Choose a date and time.';
        const t = new Date(v); if (isNaN(t)) return 'Invalid date.';
        if (el.dataset.future !== undefined && t <= new Date()) return 'Must be in the future.';
        if (el.dataset.after) { const o = document.querySelector(el.dataset.after); if (o && o.value) { const d = (t - new Date(o.value)) / 60000;
          if (d <= 0) return 'Arrival must be after departure.'; if (d < 20) return 'A flight lasts at least 20 minutes.'; if (d > 1440) return 'A flight cannot exceed 24 hours.'; } }
        return '';
      }
    },
    date: { fmt: v => v, check: v => (!v || isNaN(new Date(v))) ? 'Choose a valid date.' : '' }
  };

  function msgEl(el) {
    const host = el.closest('label') || el.parentElement;
    let m = host.querySelector(':scope > .field-msg');
    if (!m) { m = document.createElement('small'); m.className = 'field-msg'; m.setAttribute('role', 'alert'); host.appendChild(m); }
    return m;
  }
  function paint(el, err, show) {
    const m = msgEl(el);
    el.classList.toggle('invalid', !!err && show);
    el.classList.toggle('valid', !err && show && el.value !== '');
    m.className = 'field-msg' + (err && show ? '' : (el.dataset.hint && !show ? ' hint' : ''));
    m.textContent = err && show ? err : (!show && el.dataset.hint ? el.dataset.hint : '');
    el.setAttribute('aria-invalid', err && show ? 'true' : 'false');
  }
  function errorFor(el) {
    const v = el.value, rule = RULES[el.dataset.rule];
    if (el.disabled || el.type === 'hidden') return '';
    if (!v.trim()) return el.required ? 'This field is required.' : '';
    if (rule) { const e = rule.check(v, el); if (e) return e; }
    if (el.dataset.match) { const o = document.querySelector(el.dataset.match); if (o && o.value !== v) return 'Passwords do not match.'; }
    if (el.tagName === 'SELECT' || el.type === 'checkbox') return el.checkValidity() ? '' : 'Please choose an option.';
    return '';
  }
  function validate(el, show) { const e = el.type === 'checkbox' ? (el.required && !el.checked ? 'Please tick this box to continue.' : '') : errorFor(el); paint(el, e, show); return !e; }

  // password strength meter + show/hide eye
  function decoratePassword(el) {
    if (el.dataset.toggle !== undefined && !el.parentElement.classList.contains('input-wrap')) {
      const w = document.createElement('span'); w.className = 'input-wrap'; el.parentNode.insertBefore(w, el); w.appendChild(el);
      const b = document.createElement('button'); b.type = 'button'; b.className = 'pw-toggle'; b.setAttribute('aria-label', 'Show or hide password'); b.textContent = '👁';
      b.addEventListener('click', () => { const show = el.type === 'password'; el.type = show ? 'text' : 'password'; b.textContent = show ? '🙈' : '👁'; }); w.appendChild(b);
    }
    if (el.dataset.rule !== 'newpassword') return;
    const host = el.closest('label'), meter = document.createElement('div'), checks = document.createElement('div');
    meter.className = 'pw-meter'; meter.innerHTML = '<i></i><i></i><i></i><i></i>';
    checks.className = 'pw-checks'; checks.innerHTML = '<span data-c="len">8+ characters</span><span data-c="lo">a-z</span><span data-c="up">A-Z</span><span data-c="di">0-9</span><span data-c="sp">@#$!</span>';
    host.appendChild(meter); host.appendChild(checks);
    const update = () => {
      const v = el.value, t = { len: v.length >= 8, lo: /[a-z]/.test(v), up: /[A-Z]/.test(v), di: /\d/.test(v), sp: /[^A-Za-z0-9]/.test(v) };
      let score = Object.values(t).filter(Boolean).length; meter.dataset.level = v ? Math.max(1, Math.min(4, score - (v.length < 8 ? 1 : 0))) : 0;
      Object.keys(t).forEach(k => checks.querySelector('[data-c=' + k + ']').classList.toggle('on', t[k]));
    };
    el.addEventListener('input', update); update();
  }

  function setup(form) {
    form.noValidate = true;   // we show our own messages (without JS the browser's built-in checks still apply)
    const fields = () => [...form.querySelectorAll('input:not([type=hidden]):not([type=radio]):not([type=submit]), select, textarea')];
    fields().forEach(el => {
      decoratePassword(el);
      const rule = RULES[el.dataset.rule];
      if (el.dataset.hint) paint(el, '', false);
      if (rule && el.type !== 'datetime-local' && el.type !== 'date') {
        if (el.value) el.value = rule.fmt(el.value, null, el);
        el.addEventListener('input', e => { const pos = el.selectionStart, before = el.value.length; el.value = rule.fmt(el.value, e, el);
          if (document.activeElement === el && el.value.length === before) try { el.setSelectionRange(pos, pos); } catch (_) { } });
      }
      const live = () => validate(el, el.dataset.touched === '1');
      el.addEventListener('blur', () => { if (el.value || el.required) { el.dataset.touched = '1'; validate(el, true); } });
      el.addEventListener('input', () => { if (el.dataset.touched === '1' || el.classList.contains('invalid')) validate(el, true); if (el.dataset.match || el.dataset.rule === 'newpassword') form.querySelectorAll('[data-match]').forEach(o => { if (o.dataset.touched === '1') validate(o, true); }); });
      el.addEventListener('change', live);
      if (el.dataset.counter !== undefined) {
        const c = document.createElement('small'); c.className = 'char-count'; el.closest('label').appendChild(c);
        const upd = () => { const max = num(el, 'max', 1000); c.textContent = el.value.length + ' / ' + max; c.classList.toggle('warn', el.value.length > max * .9); }; el.addEventListener('input', upd); upd();
      }
    });
    // dependent re-validation (card number depends on chosen method, arrival on departure)
    form.querySelectorAll('input[type=radio][name=method]').forEach(r => r.addEventListener('change', () => { const c = form.querySelector('[data-rule=cardnumber]'); if (c && c.value) validate(c, true); }));
    form.querySelectorAll('[data-rule=datetime]').forEach(el => el.addEventListener('change', () => form.querySelectorAll('[data-after]').forEach(a => { if (a.value) validate(a, true); })));

    form.addEventListener('submit', e => {
      if (form.dataset.busy === '1') { e.preventDefault(); return; }
      let first = null;
      fields().forEach(el => { el.dataset.touched = '1'; if (!validate(el, true) && !first) first = el; });
      form.querySelectorAll('[data-require-radio]').forEach(g => {
        const ok = g.querySelector('input:checked'), m = g.querySelector('.group-msg');
        if (m) m.textContent = ok ? '' : g.dataset.requireRadio;
        if (!ok && !first) first = g.querySelector('input:not(:disabled)') || g;
      });
      if (first) { e.preventDefault(); form.classList.remove('shake'); void form.offsetWidth; form.classList.add('shake'); if (first.focus) first.focus({ preventScroll: false }); if (first.scrollIntoView) first.scrollIntoView({ block: 'center', behavior: 'smooth' }); return; }
      form.dataset.busy = '1';
      const btn = e.submitter || form.querySelector('button:not([type=button]), input[type=submit]');
      if (btn && btn.classList) { btn.classList.add('loading'); setTimeout(() => { btn.disabled = true; }, 0); }
    });
  }

  window.addEventListener('pageshow', () => document.querySelectorAll('form[data-validate]').forEach(f => { f.dataset.busy = '0'; f.querySelectorAll('button.loading').forEach(b => { b.classList.remove('loading'); b.disabled = false; }); }));
  document.addEventListener('DOMContentLoaded', () => document.querySelectorAll('form[data-validate]').forEach(setup));
  window.LWValidate = { RULES, luhn, brandOf };   // exposed for tests
})();
