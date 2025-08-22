document.addEventListener('DOMContentLoaded', () => {
    void initSummary();
    void initScoreChart();
    void initLists();
    // 회원탈퇴 모달 초기화 추가
    void initWithdrawalModal();
});

async function initSummary(){
    try{
        const res = await fetch('/api/mypage',{credentials:'include'});
        if (!res.ok) {
            console.error('프로필 통계 API 호출 실패:', res.status);
            return;
        }

        const data = await res.json(); // 🎯 수정: data 직접 사용
        setText('#interviewCount', data?.totalInterviews ?? 0);
        setText('#avgScore',       data?.avgScore ?? 0);
        setText('#bestGrade',      data?.grade ?? '-');
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
        if (!res.ok) {
            console.error('API 호출 실패:', res.status, res.statusText);
            return;
        }

        const data = await res.json(); // 🎯 수정: data 직접 사용
        console.log('API 응답 데이터:', data); // 디버깅용

        const sel = document.querySelector('#interviewList') ? '#interviewList' : '.interview-history ul';
        renderInterviews(sel, data?.interviews || []); // 🎯 수정: data.interviews
        renderScraps('#scrapList', data?.scraps || []); // 🎯 수정: data.scraps
    }catch(e){
        console.error('목록 로드 실패', e);
    }
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

        // 🎯 새로운 title 로직: "면접타입 - 경력레벨" 형태
        const interviewTypeKr = interviewTypeToKr(typeRaw); // 기술면접, 실무면접 등
        const careerLevelRaw = typeof it.careerLevel==='string'? it.careerLevel : (it.careerLevel&&it.careerLevel.name)||'';
        const careerLevelDisplay = formatCareerLevel(careerLevelRaw); // JUNIOR, MID-LEVEL, SENIOR

        const title = interviewTypeKr && careerLevelDisplay
            ? `${interviewTypeKr} - ${careerLevelDisplay}`
            : (interviewTypeKr || '면접');

        const li = document.createElement('li');
        li.className='interview-item interview-row';
        li.innerHTML = `
      <div class="item-left">
        <div class="meta-line">
          <span class="pill-dark">${escapeHtml(pill)}</span>
          <span class="dot">•</span>
          <span class="date">${escapeHtml(formatDate(it.interviewDate))}</span>
          <span class="dot">•</span>
          <span class="result-id">ID: ${it.resultId || '-'}</span>
        </div>
        <h4 class="iv-title">${escapeHtml(title)}</h4>
      </div>
      <div class="item-right">
        <div class="score-big">${Number(it.totalScore ?? 0)}</div>
        <div class="grade-txt ${gCls}">${escapeHtml(gradeTxt)}</div>
        <button type="button" class="btn small outline detail-btn">상세 보기</button>
      </div>
    `;
        li.querySelector('.detail-btn').addEventListener('click',()=>{ location.href=url; });
        frag.appendChild(li);
    });
    box.appendChild(frag);
}

/* 스크랩 렌더링 함수 - 라우팅 로직 단순화 */
function renderScraps(sel, items){
    const box = document.querySelector(sel);
    if(!box) return;
    box.innerHTML='';

    if(!items.length){
        box.innerHTML = `
            <li class="empty-state">
                <div class="empty-content">
                    <i class="fa-regular fa-bookmark"></i>
                    <p>아직 스크랩한 글이 없습니다</p>
                    <a href="/community" class="empty-link">커뮤니티 둘러보기</a>
                </div>
            </li>
        `;
        return;
    }

    const frag = document.createDocumentFragment();
    items.forEach((item) => {
        const title = (item && item.title) || '';
        const likes = Number((item && item.likes) || 0);
        const writer = (item && item.writerName) || '익명';
        const preview = (item && item.preview) || '';

        // 🎯 라우팅 로직 단순화: postId만 사용
        const postId = item && (item.postId || item.id);
        const href = postId ? `/community/posts/${postId}/detail` : '#';

        const li = document.createElement('li');
        li.innerHTML = `
            <a href="${escapeHtml(href)}" class="scrap-item">
                <div class="scrap-header">
                    <div class="scrap-author">
                        <i class="fa-solid fa-user"></i>
                        <span>${escapeHtml(writer)}</span>
                    </div>
                    <div class="scrap-stats">
                        <span class="like-count">
                            <i class="fa-solid fa-heart"></i>
                            <span>${likes}</span>
                        </span>
                    </div>
                </div>

                <h4 class="scrap-title">${escapeHtml(title)}</h4>
                <p class="scrap-preview">${escapeHtml(preview)}</p>

                <div class="scrap-footer">
                    <span class="scrap-tag">스크랩됨</span>
                </div>
            </a>
        `;
        frag.appendChild(li);
    });
    box.appendChild(frag);
}

// InterviewType을 한국어로 변환
function interviewTypeToKr(type) {
    const map = {
        'TECHNICAL': '기술면접',
        'PRACTICAL': '실무면접',
        'BEHAVIORAL': '인성면접',
        'COMPREHENSIVE': '종합면접'
    };
    return map[String(type || '').toUpperCase()] || type || '';
}

// CareerLevel 포맷팅
function formatCareerLevel(level) {
    const map = {
        'JUNIOR': 'JUNIOR',
        'MID_LEVEL': 'MID-LEVEL',
        'SENIOR': 'SENIOR'
    };
    return map[String(level || '').toUpperCase()] || level || '';
}

/* 헬퍼 */
function formatDate(iso){ if(!iso) return ''; const m=String(iso).match(/^(\d{4})[.\-\/](\d{2})[.\-\/](\d{2})/); return m?`${m[1]}.${m[2]}.${m[3]}`:iso; }
function typeToKr(t){ const map={PRACTICE:'연습 면접', REAL:'실전 면접'}; return map[String(t||'').toUpperCase()]||t||''; }
function formatGrade(g){ if(!g) return '- 등급'; const norm=String(g).toUpperCase().replace(/_PLUS/g,'+').replace(/_MINUS/g,'-'); return `${norm} 등급`; }
function gradeClass(g){ const s=String(g||'').toUpperCase(); if(s.startsWith('A'))return'g-a'; if(s.startsWith('B'))return'g-b'; if(s.startsWith('C'))return'g-c'; return'g-etc'; }
function parseJSONSafe(s){ try{return JSON.parse(s||'[]')}catch{return[]} }
function setText(s,v){ const el=document.querySelector(s); if(el) el.textContent=v; }
function escapeHtml(v){
    return v == null ? '' : String(v)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

// ===== 회원탈퇴 모달 기능 (추가된 부분) =====

function initWithdrawalModal() {
    console.log('회원탈퇴 모달 초기화 시작');

    // 탈퇴 폼 찾기 - 여러 방법으로 시도
    const withdrawalForm = document.querySelector('#withdrawalForm') ||
                          document.querySelector('form[action="/mypage/delete"]');

    if (withdrawalForm) {
        console.log('탈퇴 폼 찾음:', withdrawalForm);

        // 기존 onsubmit 속성 제거
        withdrawalForm.removeAttribute('onsubmit');

        // 새로운 이벤트 리스너 추가
        withdrawalForm.addEventListener('submit', function(e) {
            console.log('탈퇴 버튼 클릭됨');
            e.preventDefault(); // 기본 제출 방지

            // 커스텀 확인 모달 표시
            showWithdrawalConfirmModal(withdrawalForm);
        });
    } else {
        console.error('탈퇴 폼을 찾을 수 없습니다');
    }
}

/**
 * 회원탈퇴 확인 모달 표시
 * @param {HTMLFormElement} form 제출할 폼 엘리먼트
 */
function showWithdrawalConfirmModal(form) {
    console.log('모달 표시');

    // 모달 HTML 생성
    const modalHTML = `
        <div id="withdrawalModal" class="withdrawal-modal-overlay">
            <div class="withdrawal-modal">
                <div class="modal-header">
                    <h3>회원탈퇴 확인</h3>
                </div>
                <div class="modal-body">
                    <p><strong>정말로 탈퇴하시겠습니까?</strong></p>
                    <p>탈퇴 시 다음 데이터가 모두 삭제됩니다:</p>
                    <ul>
                        <li>프로필 정보</li>
                        <li>면접 기록 및 결과</li>
                        <li>커뮤니티 글 및 댓글</li>
                        <li>랭킹 정보</li>
                    </ul>
                    <p class="warning">⚠️ 이 작업은 되돌릴 수 없습니다.</p>
                </div>
                <div class="modal-footer">
                    <button type="button" class="modal-btn cancel-btn" onclick="closeWithdrawalModal()">
                        취소
                    </button>
                    <button type="button" class="modal-btn confirm-btn" onclick="confirmWithdrawal()">
                        탈퇴하기
                    </button>
                </div>
            </div>
        </div>
    `;

    // CSS 스타일 추가 (한 번만)
    if (!document.getElementById('withdrawalModalStyles')) {
        const styles = `
            <style id="withdrawalModalStyles">
                .withdrawal-modal-overlay {
                    position: fixed;
                    top: 0;
                    left: 0;
                    width: 100%;
                    height: 100%;
                    background: rgba(0, 0, 0, 0.5);
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    z-index: 10000;
                }

                .withdrawal-modal {
                    background: white;
                    border-radius: 12px;
                    width: 90%;
                    max-width: 450px;
                    box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
                    animation: modalAppear 0.2s ease-out;
                }

                @keyframes modalAppear {
                    from {
                        opacity: 0;
                        transform: scale(0.9) translateY(-20px);
                    }
                    to {
                        opacity: 1;
                        transform: scale(1) translateY(0);
                    }
                }

                .modal-header {
                    padding: 20px 24px 0;
                    border-bottom: none;
                }

                .modal-header h3 {
                    margin: 0;
                    font-size: 1.25rem;
                    font-weight: 600;
                    color: #dc3545;
                }

                .modal-body {
                    padding: 16px 24px;
                }

                .modal-body p {
                    margin: 0 0 12px 0;
                    line-height: 1.5;
                }

                .modal-body ul {
                    margin: 12px 0;
                    padding-left: 20px;
                }

                .modal-body li {
                    margin: 4px 0;
                    color: #666;
                }

                .modal-body .warning {
                    color: #dc3545;
                    font-weight: 500;
                    background: #fff5f5;
                    padding: 8px 12px;
                    border-radius: 6px;
                    border-left: 4px solid #dc3545;
                }

                .modal-footer {
                    padding: 16px 24px 24px;
                    display: flex;
                    gap: 12px;
                    justify-content: flex-end;
                }

                .modal-btn {
                    padding: 10px 20px;
                    border-radius: 8px;
                    font-size: 0.9rem;
                    font-weight: 500;
                    cursor: pointer;
                    transition: all 0.2s;
                    border: none;
                }

                .cancel-btn {
                    background: #f8f9fa;
                    color: #495057;
                    border: 1px solid #dee2e6;
                }

                .cancel-btn:hover {
                    background: #e9ecef;
                }

                .confirm-btn {
                    background: #dc3545;
                    color: white;
                }

                .confirm-btn:hover {
                    background: #c82333;
                }
            </style>
        `;
        document.head.insertAdjacentHTML('beforeend', styles);
    }

    // 모달을 body에 추가
    document.body.insertAdjacentHTML('beforeend', modalHTML);

    // 폼 엘리먼트를 전역 변수에 저장
    window.withdrawalForm = form;

    // ESC 키로 모달 닫기
    document.addEventListener('keydown', handleModalKeydown);
}

/**
 * 모달 닫기
 */
function closeWithdrawalModal() {
    console.log('모달 닫기');
    const modal = document.getElementById('withdrawalModal');
    if (modal) {
        modal.remove();
    }
    window.withdrawalForm = null;
    document.removeEventListener('keydown', handleModalKeydown);
}

/**
 * 탈퇴 확인
 */
function confirmWithdrawal() {
    console.log('탈퇴 확인 버튼 클릭');

    const form = window.withdrawalForm;
    closeWithdrawalModal();

    if (form) {
        console.log('폼 제출 시작:', form);
        form.submit();
    } else {
        console.error('폼을 찾을 수 없습니다');
    }
}

/**
 * ESC 키 처리
 */
function handleModalKeydown(e) {
    if (e.key === 'Escape') {
        closeWithdrawalModal();
    }
}