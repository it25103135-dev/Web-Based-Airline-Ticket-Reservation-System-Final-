/* Client-side convenience validation. Server-side validation remains authoritative. */
(function(){
  'use strict';
  const rules={
    name:v=>{v=v.trim();return v.length<2?'Enter at least 2 characters.':!/^[\p{L}\p{M}][\p{L}\p{M} .'\-]+$/u.test(v)?"Use letters, spaces and . ' - only.":'';},
    passport:v=>{v=v.trim().toUpperCase();return !/^[A-Z0-9][A-Z0-9-]{3,19}$/.test(v)?'Use 4-20 letters, numbers or dashes.':'';},
    seat:v=>{v=v.trim().toUpperCase();return !/^(?:[1-9]|[1-9][0-9]|1[0-9]{2})[A-F]$/.test(v)?'Seat looks like 1A, 12C or 105F.':'';},
    date:v=>(!v||isNaN(new Date(v)))?'Choose a valid date.':''
  };
  function msg(el){let host=el.closest('label')||el.parentElement,m=host.querySelector(':scope > .field-msg');if(!m){m=document.createElement('small');m.className='field-msg';m.setAttribute('role','alert');host.appendChild(m);}return m;}
  function check(el,show){if(el.disabled||el.type==='hidden')return true;let v=el.value||'',e='';if(el.required&&!v.trim())e='This field is required.';else if(el.dataset.rule&&rules[el.dataset.rule])e=rules[el.dataset.rule](v);if(show){el.classList.toggle('invalid',!!e);el.classList.toggle('valid',!e&&!!v);msg(el).textContent=e;}return !e;}
  function decoratePassword(el){if(el.type!=='password'||el.dataset.toggle===undefined)return;const wrap=document.createElement('span');wrap.className='input-wrap';el.parentNode.insertBefore(wrap,el);wrap.appendChild(el);const b=document.createElement('button');b.type='button';b.className='pw-toggle';b.textContent='👁';b.setAttribute('aria-label','Show or hide password');b.addEventListener('click',()=>{const show=el.type==='password';el.type=show?'text':'password';b.textContent=show?'🙈':'👁';});wrap.appendChild(b);}
  document.addEventListener('DOMContentLoaded',()=>document.querySelectorAll('form[data-validate]').forEach(form=>{
    form.noValidate=true;const fields=[...form.querySelectorAll('input:not([type=hidden]):not([type=radio]):not([type=submit]),select,textarea')];
    fields.forEach(el=>{decoratePassword(el);el.addEventListener('blur',()=>check(el,true));el.addEventListener('input',()=>{if(el.classList.contains('invalid'))check(el,true);});});
    form.addEventListener('submit',e=>{let first=null;fields.forEach(el=>{if(!check(el,true)&&!first)first=el;});form.querySelectorAll('[data-require-radio]').forEach(g=>{const ok=g.querySelector('input:checked'),m=g.querySelector('.group-msg');if(m)m.textContent=ok?'':g.dataset.requireRadio;if(!ok&&!first)first=g.querySelector('input:not(:disabled)')||g;});if(first){e.preventDefault();if(first.focus)first.focus();if(first.scrollIntoView)first.scrollIntoView({block:'center',behavior:'smooth'});}});
  }));
})();
