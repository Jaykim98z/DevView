document.addEventListener('DOMContentLoaded', () => {
    void initSummary();
    void initScoreChart();
    void initLists();
});

async function initSummary(){
    try{
        const res = await fetch('/api/mypage',{credentials:'include'});
        const {data:main} = await res.json();
        setText('#interviewCount', main?.totalInterviews ?? 0);
        setText('#avgScore',       main?.avgScore ?? 0);
        setText('#bestGrade',      main?.grade ?? '-');
    }catch(e){ console.error('요약 로드 실패', e); }
}

/** @type {import('chart.js').Plugin} */
const valueLabelPlugin = {
    id: 'valueLabel',
    ['afterDatasetsDraw'](chart, _args, opts) {
        const { ctx } = chart;
        const meta = chart.getDatasetMeta(0);
        const data = chart.data.datasets[0]?.data ?? [];

        ctx.save();
        ctx.font = (opts && opts.font) || '12px "Noto Sans KR", system-ui, Arial';
        ctx.fillStyle = (opts && opts.color) || '#3a4a5a';
        ctx.textAlign = 'center';
        ctx.textBaseline = 'bottom';

        data.forEach((v, i) => {
            if (v == null || Number.isNaN(v)) return;
            const pt = meta.data[i];
            if (!pt) return;
            ctx.fillText(String(v), pt.x, pt.y - 8);
        });

        ctx.restore();
    }
};

async function initScoreChart(){
    const canvas = document.getElementById('scoreChart');
    if(!canvas) return;

    let labels = parseJSONSafe(canvas?.dataset?.labels) || [];
    let scores = parseJSONSafe(canvas?.dataset?.scores) || [];

    if(!labels.length || !scores.length){
        try{
            const res = await fetch('/api/mypage/score-graph',{credentials:'include'});
            const {data:graph} = await res.json();
            labels = graph?.labels || [];
            scores = (graph?.scores || []).map(n => typeof n === 'string' ? Number(n) : n);
        }catch(e){ console.error('그래프 데이터 로드 실패', e); }
    }

    if(labels.length !== scores.length){
        const n = Math.min(labels.length, scores.length);
        labels = labels.slice(-n);
        scores = scores.slice(-n);
    }

    if(typeof Chart !== 'undefined'){
        // 양 끝 여백용 더미 라벨/데이터
        if(labels.length){
            labels = ['', '', ...labels, '', ''];
            scores = [null, null, ...scores, null, null];
        }

        // ◀▶ v2/v3 자동 대응
        const major = Number((Chart.version || '3').split('.')[0]);

        const data = {
            labels: labels.length ? labels : ['데이터 없음'],
            datasets:[{
                label:'면접 점수',
                data: scores.length ? scores : [0],
                borderColor:'#4AB2E3',
                backgroundColor:'rgba(74,178,227,.15)',
                borderWidth:2,
                fill:true,
                tension:0.35,
                clip:false,
                spanGaps:true,
                // point 옵션(v3은 elements.point, v2는 dataset에서 읽어도 동작)
                pointRadius: 3.5,
                pointHoverRadius: 5,
                pointBackgroundColor: '#1f70a1',
                pointBorderColor: '#1f70a1',
                hitRadius: 6
            }]
        };

        // v3+ 옵션
        const optionsV3 = {
            responsive:true,
            maintainAspectRatio:false,
            layout:{ padding:{ top:22, right:48, bottom:24, left:28 } },
            interaction:{ mode:'index', intersect:false },
            plugins:{
                legend:{display:false},
                title:{display:false},
                tooltip:{displayColors:false},
                valueLabel:{ color:'#3a4a5a', font:'12px "Noto Sans KR", system-ui, Arial' }
            },
            scales:{
                y:{
                    min:0, max:100,
                    ticks:{ stepSize:25, padding:8, color:'#7b8a9a' },
                    grid:{ color:'#b3daff', lineWidth:1, drawBorder:false }
                },
                x:{
                    offset:true,
                    ticks:{ maxRotation:0, padding:10, color:'#7b8a9a' },
                    grid:{ display:false },
                    border:{ display:true, color:'#c5d2de', width:1.5 }
                }
            }
        };

        // v2 옵션
        const optionsV2 = {
            responsive:true,
            maintainAspectRatio:false,
            tooltips:{ mode:'index', intersect:false, displayColors:false },
            legend:{ display:false },
            title:{ display:false },
            scales:{
                yAxes:[{
                    ticks:{ beginAtZero:true, min:0, max:100, stepSize:25, padding:8, fontColor:'#7b8a9a' },
                    gridLines:{ color:'#b3daff', lineWidth:1, drawBorder:false }
                }],
                xAxes:[{
                    offset:true,
                    ticks:{ maxRotation:0, padding:10, fontColor:'#7b8a9a' },
                    gridLines:{ display:false }
                }]
            }
        };

        const config = {
            type:'line',
            data,
            options: major >= 3 ? optionsV3 : optionsV2,
            plugins:[ valueLabelPlugin ]
        };

        new Chart(canvas, config);
    }else{
        renderScoreChartFallback(canvas, labels, scores);
    }
}

/* Fallback */
function renderScoreChartFallback(canvas, labels, scores){
    const ctx=canvas.getContext('2d');
    const W=canvas.width = canvas.offsetWidth || 560;
    const H=canvas.height= 240;
    ctx.clearRect(0,0,W,H);
    if(!labels.length || !scores.length){ ctx.font='14px sans-serif'; ctx.fillText('데이터가 없습니다',10,20); return; }
    const left=48,right=28,top=12,bottom=34, w=W-left-right, h=H-top-bottom, maxY=100, minY=0;
    ctx.strokeStyle='#ddd'; ctx.beginPath(); ctx.moveTo(left,top); ctx.lineTo(left,top+h); ctx.lineTo(left+w,top+h); ctx.stroke();
    ctx.strokeStyle='#2b6cb0'; ctx.lineWidth=2; ctx.beginPath();
    scores.forEach((y,i)=>{ const x=left+(w*i/Math.max(1,labels.length-1)); const yp=top+h-h*((y-minY)/(maxY-minY)); if(i===0)ctx.moveTo(x,yp); else ctx.lineTo(x,yp); }); ctx.stroke();
    ctx.fillStyle='#2b6cb0'; ctx.font='11px sans-serif';
    const step=Math.ceil(labels.length/6);
    scores.forEach((y,i)=>{ const x=left+(w*i/Math.max(1,labels.length-1)); const yp=top+h-h*((y-minY)/(maxY-minY)); ctx.beginPath(); ctx.arc(x,yp,3,0,Math.PI*2); ctx.fill(); if(i%step===0) ctx.fillText(labels[i],x-18,top+h+20); });
}

/* 목록 */
async function initLists(){
    try{
        const res = await fetch('/api/mypage',{credentials:'include'});
        const {data:main} = await res.json();

        const sel = document.querySelector('#interviewList') ? '#interviewList' : '.interview-history ul';
        renderInterviews(sel, main?.interviews || []);
        renderScraps('#scrapList', main?.scraps || []);
    }catch(e){ console.error('목록 로드 실패', e); }
}

/* 커뮤니티 상세 링크 보정: link가 없거나 postId만 있을 때 안전 복구 */
function buildCommunityDetailLink(item) {
    const raw = (item && item.link) || '';
    if (raw && /^https?:\/\//i.test(raw)) return raw;   // 절대 URL
    if (raw && raw.trim().length > 0) return raw;       // 상대 경로
    const pid = item && (item.postId || item.id);
    return pid != null ? `/community/posts/${pid}/detail` : '#';
}

/* 1번 이미지 레이아웃 */
function renderInterviews(sel, items){
    const box = document.querySelector(sel);
    if(!box) return;
    box.innerHTML='';
    if(!items.length){ box.innerHTML='<li class="empty">면접 기록이 없습니다.</li>'; return; }

    const frag = document.createDocumentFragment();
    items.forEach((it)=>{
        // ✅ detailUrl 우선 사용, 없으면 기존 정적 템플릿 경로로 폴백(화이트라벨 방지)
        const url = it.detailUrl || (`/result/interview-result.html?interviewId=${it.interviewId}`);

        const typeRaw  = typeof it.interviewType==='string'? it.interviewType : (it.interviewType&&it.interviewType.name)||'';
        const pill     = it.jobPosition ? `${it.jobPosition} 면접` : (typeToKr(typeRaw) || '면접');

        const gradeRaw = typeof it.grade==='string'? it.grade : (it.grade&&it.grade.name)||'';
        const gradeTxt = formatGrade(gradeRaw);
        const gCls     = gradeClass(gradeRaw);

        const title = (it.title && it.title.trim())
            || (it.jobPosition ? `${it.jobPosition} 면접` : `${typeToKr(typeRaw)} 면접`);

        const li = document.createElement('li');
        li.className='interview-item interview-row';
        li.innerHTML = `
      <div class="item-left">
        <div class="meta-line">
          <span class="pill-dark">${escapeHtml(pill)}</span>
          <span class="dot">•</span>
          <span class="date">${escapeHtml(formatDate(it.interviewDate))}</span>
        </div>
        <h4 class="iv-title">${escapeHtml(title)}</h4>
      </div>
      <div class="item-right">
        <div class="score-big">${Number(it.score ?? 0)}</div>
        <div class="grade-txt ${gCls}">${escapeHtml(gradeTxt)}</div>
        <button type="button" class="btn small outline detail-btn">상세 보기</button>
      </div>
    `;
        li.querySelector('.detail-btn').addEventListener('click',()=>{ location.href=url; });
        frag.appendChild(li);
    });
    box.appendChild(frag);
}

function renderScraps(sel, items){
    const box = document.querySelector(sel);
    if(!box) return;
    box.innerHTML='';
    if(!items.length){ box.innerHTML='<li class="empty">스크랩한 글이 없습니다.</li>'; return; }

    const frag=document.createDocumentFragment();
    items.forEach((it)=>{
        const title = (it && it.title) || '';
        // ✅ DTO의 link 우선, 없으면 postId로 복구
        const href  = buildCommunityDetailLink(it);
        const likes = Number((it && it.likes) || 0);
        const comments = Number((it && it.comments) || 0);

        const li=document.createElement('li');
        li.className='scrap-item';
        li.innerHTML=`
      <a class="link" href="${escapeHtml(href)}">
        <span class="title">${escapeHtml(title)}</span>
        <span class="meta">
          <span class="likes">👍 ${likes}</span>
          <span class="comments">💬 ${comments}</span>
        </span>
      </a>
    `;
        frag.appendChild(li);
    });
    box.appendChild(frag);
}

/* 헬퍼 */
function formatDate(iso){ if(!iso) return ''; const m=String(iso).match(/^(\d{4})[.\-\/](\d{2})[.\-\/](\d{2})/); return m?`${m[1]}.${m[2]}.${m[3]}`:iso; }
function typeToKr(t){ const map={PRACTICE:'연습 면접', REAL:'실전 면접'}; return map[String(t||'').toUpperCase()]||t||''; }
function formatGrade(g){ if(!g) return '- 등급'; const norm=String(g).toUpperCase().replace(/_PLUS/g,'+').replace(/_MINUS/g,'-'); return `${norm} 등급`; }
function gradeClass(g){ const s=String(g||'').toUpperCase(); if(s.startsWith('A'))return'g-a'; if(s.startsWith('B'))return'g-b'; if(s.startsWith('C'))return'g-c'; return'g-etc'; }
function parseJSONSafe(s){ try{return JSON.parse(s||'[]')}catch{return[]} }
function setText(s,v){ const el=document.querySelector(s); if(el) el.textContent=v; }
function escapeHtml(v){ return v==null?'':String(v).replace(/[&<>"']/g,m=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[m])); }
