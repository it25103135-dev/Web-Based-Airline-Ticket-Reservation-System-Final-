(function(){
'use strict';
const RULES={
 flightno:{fmt:v=>v.toUpperCase().replace(/[^A-Z0-9]/g,'').slice(0,6),check:v=>/^[A-Z]{2}\d{2,4}$/.test(v)?'':'Use 2 letters + 2-4 digits, e.g. LW101.'},
 place:{fmt:v=>v.replace(/[^\p{L}\p{M} ().,'\-]/gu,'').slice(0,80),check:v=>v.trim().length>=2?'':'Enter a city / airport.'},
 aircraft:{fmt:v=>v.replace(/[^A-Za-z0-9 .\-\/]/g,'').slice(0,80),check:v=>v.trim().length>=2?'':'Enter the aircraft type.'},
 money:{fmt:v=>{v=v.replace(/[^\d.]/g,'');const p=v.split('.');return p.length>1?p[0]+'.'+p.slice(1).join('').slice(0,2):v;},check:(v,e)=>{const n=Number(v),lo=Number(e.dataset.min||1),hi=Number(e.dataset.max||1000000);return !v||isNaN(n)?'Enter a valid amount.':n<lo||n>hi?'Value is outside the allowed range.':'';}},
 int:{fmt:v=>v.replace(/\D/g,'').slice(0,4),check:(v,e)=>{const n=Number(v),lo=Number(e.dataset.min||1),hi=Number(e.dataset.max||500);return !v?'Enter a whole number.':n<lo||n>hi?'Value is outside the allowed range.':'';}},
 datetime:{fmt:v=>v,check:(v,e)=>{if(!v)return'Choose a date and time.';const t=new Date(v);if(isNaN(t))return'Invalid date.';if(e.dataset.future!==undefined&&t<=new Date())return'Must be in the future.';if(e.dataset.after){const o=document.querySelector(e.dataset.after);if(o&&o.value){const d=(t-new Date(o.value))/60000;if(d<=0)return'Arrival must be after departure.';if(d<20)return'A flight lasts at least 20 minutes.';if(d>1440)return'A flight cannot exceed 24 hours.';}}return'';}}
};
function setup(form){form.noValidate=true;const fields=[...form.querySelectorAll('input:not([type=hidden]):not([type=submit]),select')];const validate=e=>{if(e.disabled)return true;const v=e.value||'';let msg=!v.trim()&&e.required?'This field is required.':'';const rule=RULES[e.dataset.rule];if(!msg&&v.trim()&&rule)msg=rule.check(v,e);let m=e.closest('label')?.querySelector('.field-msg');if(!m&&e.closest('label')){m=document.createElement('small');m.className='field-msg';e.closest('label').appendChild(m);}if(m)m.textContent=msg;e.classList.toggle('invalid',!!msg);return !msg;};fields.forEach(e=>{const rule=RULES[e.dataset.rule];if(rule&&e.type!=='datetime-local'){e.addEventListener('input',()=>{e.value=rule.fmt(e.value);});}e.addEventListener('blur',()=>validate(e));e.addEventListener('change',()=>validate(e));});form.addEventListener('submit',ev=>{let first=null;fields.forEach(e=>{if(!validate(e)&&!first)first=e;});if(first){ev.preventDefault();first.focus();}});}
document.addEventListener('DOMContentLoaded',()=>document.querySelectorAll('form[data-validate]').forEach(setup));
})();
