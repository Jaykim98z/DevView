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
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init, { once: true });
  } else {
    init();
  }

  function init() {
    const POST_ID = window.POST_ID;
    const LIST = document.getElementById('comment-list');
    const FORM = document.getElementById('comment-form');
    const INPUT = document.getElementById('comment-input');
    const SUBMIT = document.getElementById('comment-submit');
    const COUNT = document.getElementById('comment-count');
    const MORE = document.getElementById('comment-load-more');
    if (!POST_ID || !LIST) return;

    let page = 0, size = 10, last = false, busy = false, composing = false;
    let initialCountSet = false;

    function esc(s){ return (s||'').replace(/[&<>"']/g, ch => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[ch])); }
    function handleAuthFailure(res){
      if(res.status===401){ alert('로그인이 필요합니다.'); location.href='/login'; return true; }
      if(res.status===403){ alert('요청이 거부되었습니다. 다시 시도해 주세요.'); return true; }
      return false;
    }

    function getDisplayName(c){
      const meName = window.CURRENT_USERNAME ?? null;
      if (c.username && String(c.username).trim()) return String(c.username);
      if (c.writerName && String(c.writerName).trim()) return String(c.writerName);
      if (c.user && c.user.username) return String(c.user.username);
      if (c.mine && meName) return String(meName);
      if (c.userId != null) return `user#${c.userId}`;
      return '익명';
    }

    function isMine(c){
      const uid   = window.CURRENT_USER_ID ?? window.currentUserId;
      const uname = window.CURRENT_USERNAME ?? window.currentUsername;
      const uem   = window.CURRENT_USER_EMAIL ?? window.currentUserEmail;

      if (uid != null && c.userId != null && String(uid) === String(c.userId)) return true;
      if (uname && (c.username || c.writerName) && String(uname) === String(c.username || c.writerName)) return true;
      if (uem && c.writerEmail && String(uem).toLowerCase() === String(c.writerEmail).toLowerCase()) return true;
      return false;
    }

    function enrichComment(c){
      const mine = isMine(c);
      const meName = window.CURRENT_USERNAME ?? window.currentUsername ?? null;
      const username = c.username || (mine ? meName : null);
      return { ...c, mine, username, writerName: c.writerName || username || c.writerName };
    }

    async function fetchList(){
      if(last||busy) return; busy=true;
      try{
        const res = await fetch(`/api/community/posts/${POST_ID}/comments?page=${page}&size=${size}`,{
          credentials:'same-origin',
          headers:__secureHeaders__(false)
        });
        if(!res.ok){ if(handleAuthFailure(res))return; return; }
        const data = await res.json();

        if (!initialCountSet && COUNT && typeof data.totalElements === 'number') {
          COUNT.textContent = `댓글 ${data.totalElements}`;
          initialCountSet = true;
        }

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
      const author = esc(getDisplayName(c));
      const time = c.createdAt ? new Date(c.createdAt).toLocaleString() : '';
      li.innerHTML=`
        <img class="comment-item__avatar" src="${esc(c.profileImageUrl || '/img/profile-default.svg')}" alt="프로필">
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

    function exitEdit(item, restore){
      const ctx=item._edit; if(!ctx) return;
      const body=item.querySelector('[data-view]');
      if(restore) body.textContent = ctx.prevText;
      body.style.display='';
      ctx.ta.remove();
      ctx.saveBtn.remove();
      ctx.cancelBtn.remove();
      const editBtn = item.querySelector('.comment-action.edit'); if (editBtn) editBtn.style.display='';
      item.removeAttribute('data-editing');
      item._edit = null;
    }

    function enterEdit(item){
      if(item.dataset.editing === 'true') return;
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

      saveBtn.addEventListener('click', ()=>saveEdit(item));
      cancelBtn.addEventListener('click', ()=>exitEdit(item, true));
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
        exitEdit(item, false);
      }catch{
        alert('수정 중 오류가 발생했습니다.');
      }
    }

    async function deleteComment(item){
      const id = item.dataset.id;
      if(!id) return;
      if(!confirm('이 댓글을 삭제할까요?')) return;
      try{
        const res = await fetch(`/api/community/posts/${POST_ID}/comments/${id}`,{
          method:'DELETE', headers:__secureHeaders__(false), credentials:'same-origin'
        });
        if(!res.ok){ if(handleAuthFailure(res))return; alert('삭제에 실패했습니다.'); return; }
        item.remove();
        bumpCount(-1);
      }catch{
        alert('삭제 중 오류가 발생했습니다.');
      }
    }

    function bumpCount(delta){
      if(!COUNT) return;
      const n = parseInt((COUNT.textContent||'').replace(/\D/g,''),10);
      const cur = isNaN(n)?0:n;
      COUNT.textContent = `댓글 ${cur + delta}`;
    }

    function hydrateSavedForUI(saved, submittedContent){
      const nowIso = new Date().toISOString();
      const currentUid   = window.CURRENT_USER_ID ?? window.currentUserId ?? null;
      const currentName  = window.CURRENT_USERNAME ?? null;
      const currentEmail = window.CURRENT_USER_EMAIL ?? null;

      const username = (saved && (saved.username || saved.writerName || saved.writerEmail))
                        || currentName || currentEmail || (currentUid!=null?`user#${currentUid}`:null);

      return {
        id: saved?.id ?? saved?.commentId ?? null,
        userId: saved?.userId ?? currentUid,
        username: username,
        writerName: saved?.writerName ?? username,
        writerEmail: saved?.writerEmail ?? currentEmail ?? null,
        content: saved?.content ?? submittedContent,
        createdAt: saved?.createdAt ?? nowIso,
        profileImageUrl: saved?.profileImageUrl ?? null,
        mine: true
      };
    }

    async function postComment(content){
      const res = await fetch(`/api/community/posts/${POST_ID}/comments`, {
        method: 'POST',
        headers: __secureHeaders__(true),
        credentials: 'same-origin',
        body: JSON.stringify({ content })
      });
      if (!res.ok) {
        if (handleAuthFailure(res)) return null;
        alert('댓글 등록에 실패했습니다.');
        return null;
      }
      try { return await res.json(); } catch { return {}; }
    }

    async function handleSubmit(){
      if (!INPUT) return;
      const val = (INPUT.value||'').trim();
      if (!val) { INPUT.focus(); return; }
      if (val.length > 2000) { alert('댓글은 최대 2000자까지 가능합니다.'); return; }

      if (SUBMIT) { SUBMIT.disabled = true; SUBMIT.classList.add('is-loading'); }
      try{
        const saved = await postComment(val);
        if (saved === null) return;
        const uiObj = hydrateSavedForUI(saved, val);
        const enriched = enrichComment(uiObj);
        renderItem(enriched, { prepend: true });
        INPUT.value = '';
        INPUT.blur();
        bumpCount(+1);
      } finally {
        if (SUBMIT) { SUBMIT.disabled = false; SUBMIT.classList.remove('is-loading'); }
      }
    }

    MORE && MORE.addEventListener('click', fetchList);

    if (FORM) FORM.addEventListener('submit', (e)=>{ e.preventDefault(); handleSubmit(); });
    if (SUBMIT) {
      SUBMIT.addEventListener('click', (e)=>{ e.preventDefault(); handleSubmit(); });
      if (!SUBMIT.getAttribute('type')) SUBMIT.setAttribute('type','submit');
    }
    if (INPUT) {
      INPUT.addEventListener('compositionstart', ()=>{ composing = true; });
      INPUT.addEventListener('compositionend',   ()=>{ composing = false; });
      INPUT.addEventListener('keydown', (e)=>{
        if ((e.ctrlKey||e.metaKey) && e.key === 'Enter') { e.preventDefault(); handleSubmit(); }
        if (!e.ctrlKey && !e.metaKey && e.key === 'Enter' && !composing && FORM) { e.preventDefault(); handleSubmit(); }
      });
    }

    LIST.addEventListener('click',(e)=>{
      const target = e.target;
      if (target.closest('.comment-action.delete')) {
        const item = target.closest('.comment-item'); if(item) deleteComment(item);
      } else if (target.closest('.comment-action.edit')) {
        const item = target.closest('.comment-item'); if(item) enterEdit(item);
      }
    });

    fetchList();
  }
})();
