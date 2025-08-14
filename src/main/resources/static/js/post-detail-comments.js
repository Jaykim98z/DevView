'use strict';

function __getCsrf__() {
  const t = document.querySelector('meta[name="_csrf"]')?.content;
  const h = document.querySelector('meta[name="_csrf_header"]')?.content;
  if (t && h) return { header: h, token: t };
  const m = document.cookie.match(/(?:^|;\s*)XSRF-TOKEN=([^;]+)/);
  if (m) return { header: 'X-XSRF-TOKEN', token: decodeURIComponent(m[1]) };
  return { header: null, token: null };
}
function __secureHeaders__(json = true) {
  const { header, token } = __getCsrf__();
  const h = { 'X-Requested-With': 'XMLHttpRequest' };
  if (json) h['Content-Type'] = 'application/json';
  if (header && token) h[header] = token;
  return h;
}

(function () {
  const POST_ID = window.POST_ID;
  const LIST   = document.getElementById('comment-list');
  const MORE   = document.getElementById('comment-load-more');
  const INPUT  = document.getElementById('comment-input');
  const SUBMIT = document.getElementById('comment-submit');
  if (!POST_ID || !LIST) return;

  let page = 0, size = 10, last = false;
  let busy = false;

  function esc(s){
    return (s||'').replace(/[&<>"']/g,ch=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[ch]));
  }
  function handleAuthFailure(res){
    if(res.status===401){ alert('로그인이 필요합니다.'); location.href='/login'; return true; }
    if(res.status===403){ alert('요청이 거부되었습니다. 다시 시도해 주세요.'); return true; }
    return false;
  }
  function resetAndReload() {
    page = 0;
    last = false;
    LIST.innerHTML = '';
    fetchList();
  }

  function isMine(c){
    const uid = window.CURRENT_USER_ID ?? window.currentUserId;
    const uem = window.CURRENT_USER_EMAIL ?? window.currentUserEmail;
    const uname = window.CURRENT_USERNAME ?? window.currentUsername;
    if (uid != null && c.userId != null && String(uid) === String(c.userId)) return true;
    if (uem && c.writerName && String(uem).toLowerCase() === String(c.writerName).toLowerCase()) return true;
    if (uname && c.writerName && String(uname) === String(c.writerName)) return true;
    return !!c.mine;
  }
  const enrichComment = (c)=>({ ...c, mine: isMine(c) });

  async function fetchList(){
    if(last||busy) return; busy=true;
    try{
      const res = await fetch(`/api/community/posts/${POST_ID}/comments?page=${page}&size=${size}`,{
        credentials:'same-origin',
        headers:__secureHeaders__(false)
      });
      if(!res.ok){ if(handleAuthFailure(res))return; return; }
      const data = await res.json();
      (data.content||[]).map(enrichComment).forEach(c => renderItem(c));
      last = !!data.last;
      if(!last){ MORE && (MORE.style.display='block'); page++; }
      else { MORE && (MORE.style.display='none'); }
    } finally { busy=false; }
  }

  function renderItem(c, opts = {}) {
    const id = c.id ?? c.commentId;
    if (id && LIST.querySelector(`.comment-item[data-id="${id}"]`)) return;

    const li=document.createElement('li');
    li.className='comment-item';
    li.dataset.id=id;
    const author=esc(c.writerName || (c.userId!=null?('user#'+c.userId):'익명'));
    const time=c.createdAt? new Date(c.createdAt).toLocaleString() : '';
    li.innerHTML=`
      <img class="comment-item__avatar" src="/img/profile-default.svg" alt="프로필">
      <div class="comment-item__main">
        <div class="comment-item__head">
          <span class="comment-item__author">${author}</span>
          <span class="comment-item__meta">${esc(time)}</span>
        </div>
        <div class="comment-item__body" data-view>${esc(c.content)}</div>
        <div class="comment-item__actions" data-actions>
          ${isMine(c)?`
            <button class="comment-action edit" type="button">수정</button>
            <button class="comment-action comment-action--danger delete" type="button">삭제</button>
          `:''}
        </div>
      </div>`;
    if (opts.prepend) LIST.prepend(li); else LIST.appendChild(li);
  }

  function cancelAnyEditing() {
    const opened = LIST.querySelector('.comment-item[data-editing="true"]');
    if (!opened) return;
    exitEdit(opened, /*restore*/true);
  }

  function enterEdit(item){
    if(item.dataset.editing === 'true') return;
    cancelAnyEditing();

    const body = item.querySelector('[data-view]');
    const actions = item.querySelector('[data-actions]');
    const prev = body.textContent;

    const ta = document.createElement('textarea');
    ta.className = 'comment-edit-textarea';
    ta.value = prev;
    ta.maxLength = 2000;
    ta.rows = Math.min(6, Math.max(2, prev.split('\n').length));
    body.style.display = 'none';
    body.after(ta);

    const saveBtn  = document.createElement('button');
    const cancelBtn= document.createElement('button');
    saveBtn.type='button'; cancelBtn.type='button';
    saveBtn.className='comment-action save';
    cancelBtn.className='comment-action cancel';
    saveBtn.textContent='저장';
    cancelBtn.textContent='취소';
    const editBtn = actions.querySelector('.edit'); if (editBtn) editBtn.style.display='none';
    actions.prepend(cancelBtn);
    actions.prepend(saveBtn);

    const keyHandler = (e)=>{
      if(e.key==='Escape'){ exitEdit(item, true); }
      if(e.key==='Enter' && (e.ctrlKey||e.metaKey)){ saveEdit(item); }
    };
    ta.addEventListener('keydown', keyHandler);
    item._edit = { ta, saveBtn, cancelBtn, keyHandler, prevText: prev };
    item.dataset.editing = 'true';
    ta.focus();
  }

  async function saveEdit(item){
    const ctx = item._edit; if(!ctx) return;
    const id = item.dataset.id;
    const next = (ctx.ta.value||'').trim();
    if(!next){ alert('내용을 입력하세요.'); return; }
    if(next.length>2000){ alert('댓글은 최대 2000자까지 가능합니다.'); return; }

    try{
      const res=await fetch(`/api/community/posts/${POST_ID}/comments/${id}`,{
        method:'PUT', headers:__secureHeaders__(true), credentials:'same-origin',
        body:JSON.stringify({ content: next })
      });
      if(!res.ok){ if(handleAuthFailure(res))return; alert('수정에 실패했습니다.'); return; }
      const body = item.querySelector('[data-view]');
      body.textContent = next;
      exitEdit(item, /*restore*/false);
    }catch{
      alert('수정 중 오류가 발생했습니다.');
    }
  }

  function exitEdit(item, restore){
    const ctx = item._edit; if(!ctx) return;
    const body = item.querySelector('[data-view]');
    body.style.display = '';
    if (restore) body.textContent = ctx.prevText;

    ctx.ta.remove();
    ctx.saveBtn.remove();
    ctx.cancelBtn.remove();
    const actions = item.querySelector('[data-actions]');
    const editBtn = actions.querySelector('.edit'); if (editBtn) editBtn.style.display='';
    item.dataset.editing = 'false';
    item._edit = null;
  }

  async function createComment(){
    if(!window.IS_AUTH){ alert('로그인이 필요합니다.'); return; }
    const content=(INPUT?.value||'').trim();
    if(!content) return;
    if(content.length>2000){ alert('댓글은 최대 2000자까지 가능합니다.'); return; }
    if(busy) return;

    busy=true; SUBMIT && (SUBMIT.disabled=true);
    try{
      const payload = { postId: POST_ID, content, parentId: null, writerName: window.CURRENT_USERNAME || window.CURRENT_USER_EMAIL || null };
      const res=await fetch(`/api/community/posts/${POST_ID}/comments`,{
        method:'POST', headers:__secureHeaders__(true), credentials:'same-origin',
        body:JSON.stringify(payload)
      });
      if(!res.ok){ if(handleAuthFailure(res))return; alert('댓글 등록에 실패했습니다.'); return; }
      INPUT.value='';
      resetAndReload();
    } finally { busy=false; SUBMIT && (SUBMIT.disabled=false); }
  }

  LIST?.addEventListener('click', async (e)=>{
    const btn=e.target.closest('button'); if(!btn) return;
    const item=e.target.closest('.comment-item'); if(!item) return;
    const id=item.dataset.id;

    if(btn.classList.contains('delete')){
      if(!confirm('이 댓글을 삭제할까요?')) return;
      try{
        const res=await fetch(`/api/community/posts/${POST_ID}/comments/${id}`,{
          method:'DELETE', headers:__secureHeaders__(false), credentials:'same-origin'
        });
        if(!res.ok){ if(handleAuthFailure(res))return; alert('삭제에 실패했습니다.'); return; }
        item.remove();
      }catch{ alert('삭제 중 오류가 발생했습니다.'); }
      return;
    }

    if(btn.classList.contains('edit')){
      enterEdit(item);
      return;
    }

    if(btn.classList.contains('save')){
      await saveEdit(item);
      return;
    }

    if(btn.classList.contains('cancel')){
      exitEdit(item, /*restore*/true);
      return;
    }
  });

  MORE?.addEventListener('click', fetchList);
  const keyHandler=(e)=>{ if(e.isComposing) return; if(e.key==='Enter'&&!e.shiftKey){ e.preventDefault(); createComment(); } };
  INPUT?.addEventListener('keydown', keyHandler);
  SUBMIT?.addEventListener('click', createComment);

  fetchList();
})();

document.addEventListener('click', async (e)=>{
  const likeBtn = e.target.closest('.btn-like');
  const scrapBtn = e.target.closest('.btn-scrap');

  const toggle = async (btn)=>{
    const url = btn.dataset.url;
    if(!url) return;
    try{
      const res = await fetch(url,{ method:'POST', headers:__secureHeaders__(false), credentials:'same-origin' });
      if(!res.ok) throw new Error();
      const data = await res.json();
      const active = !!data.active;
      btn.classList.toggle('active', active);
      const icon = btn.querySelector('i');
      if(icon){
        icon.classList.toggle('fa-solid', active);
        icon.classList.toggle('fa-regular', !active);
      }
      btn.querySelector('.count').textContent = data.count;
    }catch{
      const active = btn.classList.toggle('active');
      const span = btn.querySelector('.count');
      const cur = parseInt(span.textContent||'0',10);
      span.textContent = Math.max(0, cur + (active?1:-1));
      const icon = btn.querySelector('i');
      if(icon){
        icon.classList.toggle('fa-solid', active);
        icon.classList.toggle('fa-regular', !active);
      }
    }
  };

  if(likeBtn)  await toggle(likeBtn);
  if(scrapBtn) await toggle(scrapBtn);
});

if(window.POST_ID){
  fetch(`/api/community/posts/${window.POST_ID}/view`,{
    method:'POST', credentials:'same-origin', headers:__secureHeaders__(false)
  })
  .then(r=>r.ok?r.json():null)
  .then(d=>{
    if(d?.viewCount!=null){
      const el=document.getElementById('view-count');
      if(el) el.textContent=d.viewCount;
    }
  })
  .catch(()=>{});
}
