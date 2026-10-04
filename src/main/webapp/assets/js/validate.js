/* Client-side validation for User + Notification Management.
   Server-side validation remains authoritative. */
(function () {
  'use strict';

  const LETTERS = '\\p{L}\\p{M}';
  const numberData = (el, key, fallback) => el.dataset[key] !== undefined ? Number(el.dataset[key]) : fallback;

  const RULES = {
    name: {
      clean: v => v.replace(new RegExp('[^' + LETTERS + " .'\\-]", 'gu'), '').replace(/ {2,}/g, ' ').slice(0, 80),
      error: v => {
        v = v.trim();
        if (v.length < 2) return 'Enter at least 2 letters.';
        return new RegExp('^[' + LETTERS + "][" + LETTERS + " .'\\-]+$", 'u').test(v) ? '' : 'Only letters, spaces and . \' - are allowed.';
      }
    },
    username: {
      clean: v => v.replace(/[^A-Za-z0-9._-]/g, '').slice(0, 30),
      error: v => v.length < 4 ? 'At least 4 characters.' : !/^[A-Za-z]/.test(v) ? 'Must start with a letter.' : ''
    },
    email: {
      clean: v => v.replace(/\s/g, '').slice(0, 120),
      error: v => /^[A-Za-z0-9._%+\-]+@[A-Za-z0-9-]+(\.[A-Za-z0-9-]+)*\.[A-Za-z]{2,}$/.test(v) ? '' : 'Enter a valid email address.'
    },
    phone: {
      clean: v => {
        let d = v.replace(/[^\d+]/g, '');
        if (d.startsWith('+94')) d = '0' + d.slice(3);
        else if (d.startsWith('0094')) d = '0' + d.slice(4);
        else if (/^94\d{9}$/.test(d)) d = '0' + d.slice(2);
        return d.replace(/\D/g, '').slice(0, 10);
      },
      error: v => !/^07[0-8]\d{7}$/.test(v) ? 'Enter a valid 10-digit Sri Lankan mobile number (070-078).' : ''
    },
    newpassword: {
      clean: v => v.replace(/\s/g, '').slice(0, 64),
      error: v => v.length < 8 ? 'At least 8 characters.' :
        !/[a-z]/.test(v) ? 'Add a lowercase letter.' :
        !/[A-Z]/.test(v) ? 'Add an uppercase letter.' :
        !/\d/.test(v) ? 'Add a number.' :
        /^[A-Za-z0-9]+$/.test(v) ? 'Add a special character.' : ''
    },
    text: {
      clean: (v, el) => v.slice(0, numberData(el, 'max', 1000)),
      error: (v, el) => {
        const min = numberData(el, 'min', 1);
        return v.trim().length < min ? 'At least ' + min + ' characters.' : '';
      }
    }
  };

  function messageElement(el) {
    const host = el.closest('label') || el.parentElement;
    let msg = host.querySelector(':scope > .field-msg');
    if (!msg) {
      msg = document.createElement('small');
      msg.className = 'field-msg';
      msg.setAttribute('role', 'alert');
      host.appendChild(msg);
    }
    return msg;
  }

  function validationError(el) {
    if (el.disabled || el.type === 'hidden') return '';
    if (el.type === 'checkbox') return el.required && !el.checked ? 'Please tick this box to continue.' : '';
    const value = el.value || '';
    if (!value.trim()) return el.required ? 'This field is required.' : '';
    const rule = RULES[el.dataset.rule];
    if (rule) {
      const err = rule.error(value, el);
      if (err) return err;
    }
    if (el.dataset.match) {
      const other = document.querySelector(el.dataset.match);
      if (other && other.value !== value) return 'Passwords do not match.';
    }
    return '';
  }

  function paint(el, show) {
    const err = validationError(el);
    const msg = messageElement(el);
    el.classList.toggle('invalid', !!err && show);
    el.classList.toggle('valid', !err && show && !!el.value);
    msg.textContent = show ? err : '';
    el.setAttribute('aria-invalid', err && show ? 'true' : 'false');
    return !err;
  }

  function decoratePassword(el) {
    if (el.dataset.toggle === undefined || el.parentElement.classList.contains('input-wrap')) return;
    const wrapper = document.createElement('span');
    wrapper.className = 'input-wrap';
    el.parentNode.insertBefore(wrapper, el);
    wrapper.appendChild(el);
    const button = document.createElement('button');
    button.type = 'button';
    button.className = 'pw-toggle';
    button.setAttribute('aria-label', 'Show or hide password');
    button.textContent = '👁';
    button.addEventListener('click', () => {
      const show = el.type === 'password';
      el.type = show ? 'text' : 'password';
      button.textContent = show ? '🙈' : '👁';
    });
    wrapper.appendChild(button);
  }

  document.querySelectorAll('form[data-validate]').forEach(form => {
    form.noValidate = true;
    const fields = [...form.querySelectorAll('input:not([type=hidden]):not([type=submit]):not([type=radio]), select, textarea')];

    fields.forEach(el => {
      decoratePassword(el);
      const rule = RULES[el.dataset.rule];
      if (rule && el.value) el.value = rule.clean(el.value, el);

      el.addEventListener('input', () => {
        if (rule) el.value = rule.clean(el.value, el);
        if (el.dataset.touched === '1') paint(el, true);
        if (el.dataset.match || el.dataset.rule === 'newpassword') {
          form.querySelectorAll('[data-match]').forEach(match => {
            if (match.dataset.touched === '1') paint(match, true);
          });
        }
      });

      el.addEventListener('blur', () => {
        el.dataset.touched = '1';
        paint(el, true);
      });

      if (el.dataset.counter !== undefined) {
        const counter = document.createElement('small');
        counter.className = 'char-count';
        el.closest('label').appendChild(counter);
        const update = () => {
          const max = numberData(el, 'max', Number(el.maxLength > 0 ? el.maxLength : 1000));
          counter.textContent = el.value.length + ' / ' + max;
          counter.classList.toggle('warn', el.value.length > max * 0.9);
        };
        el.addEventListener('input', update);
        update();
      }
    });

    form.addEventListener('submit', e => {
      let ok = true;
      fields.forEach(el => {
        el.dataset.touched = '1';
        if (!paint(el, true)) ok = false;
      });
      if (!ok) {
        e.preventDefault();
        e.stopImmediatePropagation();
        form.querySelector('.invalid')?.focus();
      }
    }, true);
  });
})();
