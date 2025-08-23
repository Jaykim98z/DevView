<summary>1️⃣ 로그인 & 회원가입</summary>
<p align="center">
  <img width="800" alt="Image" src="https://github.com/user-attachments/assets/e8859ec0-182e-4359-af70-d6ecf30ca2d9" />
</p>

* **기본 로그인/회원가입**
  * 이메일 + 비밀번호 등록(BCrypt 암호화)
  * CSRF, 세션 보안 적용  

* **소셜 로그인**  
<p align="center">
  <img width="800" alt="Image" src="https://github.com/user-attachments/assets/31cff2af-82a1-4b9b-a0ab-24cd1bda0192" />
</p>

  * Google OAuth2 지원, 최초 로그인 시 provider 저장  

* **에러 처리**
  * 중복 이메일 가입 예외, 인증 실패 시 CustomException 적용  

---

<summary>2️⃣ 메인 페이지</summary>
<p align="center">
  <img width="800" alt="Image" src="https://github.com/user-attachments/assets/428d8b77-d844-46f1-8467-13f0cbe692a6" />
</p>

* **Hero 배너** — 플랫폼 핵심 가치 강조(모의면접)  
* **서비스 소개 섹션** — 면접 세션, 피드백, 랭킹, 커뮤니티  
* **후기 섹션** — 커뮤니티 게시글 일부 노출  

---

<summary>3️⃣ 면접 세션</summary>
<p align="center">
  <img width="800" alt="Image" src="https://github.com/user-attachments/assets/2c6b7297-9e27-4c92-b607-f1f62f713fab" />
</p>

* **질문 생성** — Gemini API 기반 직무 맞춤형 질문  
* **답변 입력** — 텍스트 입력(추후 음성 확장 가능)  
* **실시간 피드백** — 앨런 AI API 활용(점수/강점/개선점/예시)  

---

<summary>4️⃣ 마이페이지</summary>
<p align="center">
  <img width="800" alt="Image" src="https://github.com/user-attachments/assets/34aacb25-ebc9-4a51-bf0d-381a2768a2e8" />
</p>

* **프로필 관리** — 이름, 직무, 프로필 이미지  
* **면접 기록 조회** — 점수·피드백 이력  
* **시각화 차트** — 점수 변화 라인 차트  
* **내 활동** — 작성 글·댓글 모아보기  

---

<summary>5️⃣ 커뮤니티</summary>
<p align="center">
  <img width="800" alt="Image" src="https://github.com/user-attachments/assets/ff2f576e-2f33-4086-8583-3d2e746a5d72" />
</p>

* 후기 작성/수정/삭제, 댓글·좋아요·스크랩  
* 직무/키워드 검색, 페이징  

---

<summary>6️⃣ 랭킹</summary>
<p align="center">
  <img width="800" alt="Image" src="https://github.com/user-attachments/assets/49581107-87a4-4336-8be8-2e5e136cc5bf" />
</p>

* **Top 3 시상대 UI**  
* **Top 20 테이블** — 닉네임/평균 점수/순위  
* **내 순위 강조** — 로그인 사용자 하이라이트  

---

<summary>7️⃣ 결과 리포트</summary>
<p align="center">
  <img width="800" alt="Image" src="https://github.com/user-attachments/assets/f4429aa1-257c-4f13-9ca4-cae0baf86466" />
</p>

* **구성** — 질문·답변, AI 피드백(점수/강점/개선점), 예시 답변, 전체 평균 점수  
