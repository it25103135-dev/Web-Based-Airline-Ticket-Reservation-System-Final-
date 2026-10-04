document.addEventListener('DOMContentLoaded', () => {
  const $=(s,r=document)=>r.querySelector(s), $$=(s,r=document)=>[...r.querySelectorAll(s)];
  const io='IntersectionObserver' in window?new IntersectionObserver(es=>es.forEach(e=>{if(e.isIntersecting){e.target.classList.add('visible');io.unobserve(e.target);}}),{threshold:.08}):null;
  $$('.reveal').forEach(el=>{if(io)io.observe(el);else el.classList.add('visible');});
  const toggle=$('.nav-toggle'),nav=$('.nav-links'); if(toggle&&nav)toggle.addEventListener('click',()=>nav.classList.toggle('open'));
  $$('[data-count]').forEach(el=>{const end=Number(el.dataset.count||0);let n=0;const step=Math.max(1,Math.ceil(end/25));const t=setInterval(()=>{n=Math.min(end,n+step);el.textContent=n;if(n>=end)clearInterval(t);},25);});
  const dismiss=t=>{t.classList.add('leaving');setTimeout(()=>t.remove(),400)}; $$('.toast').forEach(t=>{$('.toast-close',t)?.addEventListener('click',()=>dismiss(t));setTimeout(()=>dismiss(t),6500)});
  $$('form[data-confirm]').forEach(f=>f.addEventListener('submit',e=>{if(!confirm(f.dataset.confirm)){e.preventDefault();e.stopImmediatePropagation();}},true));
  $$('[data-filter]').forEach(input=>{const target=$(input.dataset.filter),sel=input.dataset.filterItem,statusSel=$(input.dataset.statusSelect||'__none__');if(!target)return;const empty=$('.no-results',target.parentElement);const run=()=>{const q=input.value.trim().toLowerCase(),st=statusSel?statusSel.value:'';let shown=0;$$(sel,target).forEach(it=>{const ok=(!q||it.textContent.toLowerCase().includes(q))&&(!st||(it.dataset.status||'').includes(st));it.classList.toggle('hidden-by-filter',!ok);if(ok)shown++;});if(empty)empty.classList.toggle('show',shown===0);};input.addEventListener('input',run);statusSel&&statusSel.addEventListener('change',run);});
});
