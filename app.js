/* Lanka Wings Booking Management - UI helpers only. */
(function(){
  'use strict';
  const $=(q,r=document)=>r.querySelector(q), $$=(q,r=document)=>[...r.querySelectorAll(q)];
  document.addEventListener('DOMContentLoaded',()=>{
    const toggle=$('.nav-toggle'), links=$('.nav-links'); if(toggle&&links) toggle.addEventListener('click',()=>links.classList.toggle('open'));
    $$('[data-confirm]').forEach(f=>f.addEventListener('submit',e=>{if(!window.confirm(f.dataset.confirm||'Are you sure?'))e.preventDefault();}));
    $$('.toast-close').forEach(b=>b.addEventListener('click',()=>b.closest('.toast')?.remove()));
    setTimeout(()=>$$('.toast').forEach(t=>t.remove()),5000);

    $$('[data-filter]').forEach(input=>{
      const list=$(input.dataset.filter), itemSel=input.dataset.filterItem||'*', statusSel=input.dataset.statusSelect?$(input.dataset.statusSelect):null;
      if(!list)return;
      const run=()=>{const q=input.value.trim().toLowerCase(), st=statusSel?statusSel.value:''; let shown=0;
        $$(itemSel,list).forEach(item=>{const okText=!q||item.textContent.toLowerCase().includes(q), okStatus=!st||item.dataset.status===st; const ok=okText&&okStatus;item.hidden=!ok;if(ok)shown++;});
        const none=$('.no-results'); if(none)none.style.display=shown?'none':'block';};
      input.addEventListener('input',run); if(statusSel)statusSel.addEventListener('change',run); run();
    });

    $$('input[name=seatNumber]').forEach(r=>r.addEventListener('change',()=>{$$('[data-seat-picked]').forEach(x=>x.textContent=r.checked?r.value:'-');}));
  });
})();
