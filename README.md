<div align="center">
    <img src="src/main/resources/static/img/logo.svg" alt="devview logo" style="height: 100px;"/>
<h2>AI 기반 개발자 모의 면접 시뮬레이터DevView</h2>
</div>

<div align="center">

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.4-green?logo=spring-boot)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange?logo=java)](https://www.oracle.com/java/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Latest-blue?logo=postgresql)](https://www.postgresql.org/)
[![Thymeleaf](https://img.shields.io/badge/Thymeleaf-3.0-005F0F?logo=thymeleaf)](https://www.thymeleaf.org/)

**개발자를 위한 AI 기반 모의 면접 플랫폼**

[🚀 시작하기](#-설치-및-실행) • [✨ 주요 기능](#-주요-기능) • [🛠 기술 스택](#-기술-스택) • [👥 팀원](#-팀원)

</div>

---

# 📑 프로젝트 개요 & 차별성

## 🎯 프로젝트 개요

**Deview**는 신입 개발자 취업 준비생을 위한 **AI 면접 시뮬레이터 플랫폼**입니다.
취업 시장에서는 단순한 코딩 실력뿐만 아니라, **논리적 사고·문제 해결 능력·커뮤니케이션 스킬**까지 종합적으로 평가합니다. 그러나 대부분의 취준생은 실제 면접 경험이 부족하고, 피드백을 받을 기회가 제한적입니다.

이러한 문제를 해결하기 위해, Deview는 **AI 기반의 모의 면접 기능**을 제공하여 사용자가 실전과 유사한 환경에서 면접을 연습하고, **즉각적인 피드백**을 받을 수 있도록 설계되었습니다.

📅 **진행 기간** : 2025.07.29 \~ 2025.08.25 (총 4주)


🅰️ **팀명** : 올인원 (All-in-One)

👨‍💻 **역할 방식** : 프론트엔드·백엔드 영역 구분 없이 **풀스택으로 협업**, 필요한 경우 역할을 유연하게 분담


## 🚀 프로젝트 차별성

Deview는 단순한 연습 도구를 넘어, **학습·분석·경험 공유까지 연결되는 플랫폼**을 목표로 합니다.

1. **실시간 AI 피드백**

    * 답변에 대한 **점수**, **강점·개선점**, **예시 답변**을 즉시 제공
    * 피드백은 AI 모델(Gemini & 앨런 AI)을 통해 자동 생성되어, 반복 학습 효과 극대화


2. **맞춤 질문 생성**

    * 사용자의 **자기소개서·희망 직무·기술 스택**을 분석
    * 개인별로 취약한 부분을 집중 공략하는 **맞춤형 질문** 생성


3. **리포트 아카이빙**

    * 면접 기록을 **PDF 리포트**로 다운로드 가능
    * 이전 기록과 점수를 비교하여 성장 곡선 확인
    * 포트폴리오·취업 준비 자료로도 활용 가능


4. **커뮤니티 & 랭킹 시스템**

    * 사용자끼리 면접 경험·후기를 공유
    * 점수 기반 랭킹으로 경쟁심과 동기부여 유도
    * Top 3는 시상대 형태로 시각화하여 성취감 강화


## 📌 기대 효과

* 취업 준비생 : **실전 면접 감각** 향상 & **객관적 피드백** 확보
* 교육 기관 : 학생들의 **학습 추세 데이터** 확보 & 관리
* 기업 : 지원자의 **기술·커뮤니케이션 능력 검증 도구**로 활용 가능


---

# 👥  팀원 소개 & 역할 분담

## 🔹 팀 구성

본 프로젝트는 **올인원(All-in-One)** 팀이 진행했으며, 팀원 모두가 프론트엔드와 백엔드를 가리지 않고 **풀스택 개발**을 경험했습니다. 다만 효율적인 진행을 위해 **주요 담당 도메인**을 분배하여 책임감을 가지고 작업을 수행했습니다.


## 🧑‍💻 팀원 소개

| <img src="src/main/resources/static/img/진욱.svg" width="120px"/> | <img src="src/main/resources/static/img/나.svg" width="120px"/> | <img src="src/main/resources/static/img/예은.svg" width="190px"/> | <img src="src/main/resources/static/img/권호.svg" width="120" style="border-radius: 50%;"/> |
|:---------------------------------------------------------------:|:--------------------------------------------------------------:|:---------------------------------------------------------------:|:-------------------------------------------------------------:|
|                          **김진욱 (팀장)**                           |                            **이소연**                             |                             **김예은**                             |                            **조권호**                            |
|                  로그인/회원가입, 보안, OAuth2, 랭킹, 배포                   |                      메인/마이페이지, 공통 UI, 문서화                      |                         커뮤니티, DB 설계, 발표                         |                  면접 세션, AI 연동, Swagger, 리포트                   |


## 📆 역할 분담 

| 이름  | 주요 도메인   | 상세 작업                                                                                                  |
| --- | -------- | ------------------------------------------------------------------------------------------------------ |
| 김진욱 | 인증·랭킹·배포 | - 로그인/회원가입 API & UI<br>- Google OAuth2 연동<br>- Spring Security 적용<br>- 랭킹 페이지 구현<br>- Docker 기반 배포 자동화 |
| 이소연 |  메인/마이페이지·문서화   | - 메인 페이지 & 마이페이지 UI<br>- Header/Footer 및 공통 UI 컴포넌트<br>- README·Notion 관리<br>- 발표 자료 제작                |
| 김예은 | 커뮤니티     | - 게시글/댓글/좋아요/스크랩 CRUD<br>- ERD 설계 및 DB 마이그레이션<br>- Swagger API 문서화<br>- 최종 발표                          |
| 조권호 | 면접 세션·AI | - Gemini API 기반 질문 생성<br>- 앨런 AI API 기반 실시간 피드백<br>- 면접 결과 PDF 리포트 생성<br>- Swagger 작성 및 시연 영상 제작       |

## ⚖️ 협업 방식

* 모든 팀원은 **프론트엔드 + 백엔드 전 영역**을 경험
* WBS를 기반으로 **기간별 마일스톤**을 설정하고 진행 상황을 점검
* Notion을 통해 **문서화 & 진행 현황 관리**, Discord를 통한 **실시간 소통**
* 코드 리뷰와 브랜치 전략을 적극 활용하여 **품질 관리 및 협업 생산성** 향상


---

# 🗄️ ERD & 데이터베이스 구조

## 📌 설계 개요

Deview 프로젝트는 **사용자 중심 데이터 흐름**과 **면접 세션·피드백 데이터 관리**에 초점을 두어 데이터베이스를 설계했습니다.
핵심 목표는 다음과 같았습니다:

1. **확장성** – 커뮤니티·랭킹·AI 피드백 등 다양한 기능이 추가되어도 유연하게 확장 가능해야 함
2. **무결성** – 면접 기록, 사용자 정보, 피드백 간의 관계를 일관성 있게 유지
3. **효율성** – 대량의 질문/답변 데이터를 빠르게 조회할 수 있는 구조

---

## 🗺️ ERD 다이어그램
<p align="center">
  <img src="src/main/resources/static/img/ERD.svg" alt="ERD Diagram" width="900"/>
</p>

---
## 🏗️ 주요 테이블 구조

<details>
<summary>👤 Users (회원 정보)</summary>

* 회원 기본 정보 및 OAuth2 계정 연동
* 커뮤니티·랭킹·프로필과 연계

| 컬럼명        | 타입        | 설명                      |
| ------------- | --------- | ----------------------- |
| user_id       | SERIAL PK | 고유 회원 ID                 |
| username      | VARCHAR   | 사용자 이름 (Unique)          |
| email         | VARCHAR   | 이메일 (Unique)             |
| password      | VARCHAR   | 암호화 비밀번호                |
| created_at    | TIMESTAMP | 가입일                     |
| provider      | VARCHAR   | OAuth2 제공자 (Google 등)     |
| provider_id   | VARCHAR   | OAuth2 제공자 식별자           |

</details>

---

<details>
<summary>💬 Interviews (면접 세션)</summary>

* 면접 단위의 세션 기록
* 질문/답변/결과와 연계

| 컬럼명            | 타입        | 설명                                  |
| ----------------- | --------- | ----------------------------------- |
| interview_id      | SERIAL PK | 세션 ID                               |
| user_id           | SERIAL FK | Users 참조                            |
| interview_type    | ENUM      | TECHNICAL, PRACTICAL, BEHAVIORAL, COMPREHENSIVE |
| job_position      | VARCHAR   | 직무 (백엔드, 프론트엔드 등)                 |
| career_level      | ENUM      | JUNIOR, MID, SENIOR                   |
| question_count    | INT       | 질문 개수                               |
| duration_minutes  | INT       | 소요 시간(분)                           |
| created_at        | TIMESTAMP | 시작 시각                               |
| ended_at          | TIMESTAMP | 종료 시각                               |

</details>

---

<details>
<summary>📝 InterviewQuestions (면접 질문)</summary>

* AI가 생성한 질문 저장

| 컬럼명        | 타입        | 설명            |
| ------------- | --------- | ------------- |
| question_id   | SERIAL PK | 질문 ID         |
| text          | TEXT      | 질문 본문        |
| category      | VARCHAR   | 질문 카테고리      |
| interview_id  | SERIAL FK | Interviews 참조 |

</details>

---

<details>
<summary>🗨️ InterviewAnswers (면접 답변)</summary>

* 사용자가 입력한 답변 기록

| 컬럼명       | 타입        | 설명               |
| ------------ | --------- | ---------------- |
| answer_id    | SERIAL PK | 답변 ID            |
| question_id  | SERIAL FK | InterviewQuestions 참조 |
| answer_text  | TEXT      | 사용자 답변           |
| submitted_at | TIMESTAMP | 제출 시각            |

</details>

---

<details>
<summary>📊 InterviewResults (면접 결과/피드백)</summary>

* AI 피드백, 점수, 등급 기록
* PDF 리포트 생성에 활용

| 컬럼명               | 타입        | 설명            |
| -------------------- | --------- | ------------- |
| result_id            | SERIAL PK | 결과 ID         |
| interview_id         | SERIAL FK | Interviews 참조 |
| total_score          | INT       | 총점            |
| grade                | VARCHAR   | 등급 (A+, A, B 등) |
| feedback             | TEXT      | 피드백           |
| recommended_resource | TEXT      | 추천 학습 리소스     |
| created_at           | TIMESTAMP | 생성일           |

</details>

---

<details>
<summary>🧑‍💻 UserProfiles (회원 프로필)</summary>

* 회원별 프로필 정보 (자기소개, 경력 수준 등)

| 컬럼명           | 타입        | 설명           |
| ---------------- | --------- | ------------ |
| profile_id       | SERIAL PK | 프로필 ID       |
| user_id          | SERIAL FK | Users 참조     |
| job_position     | VARCHAR   | 직무           |
| career_level     | ENUM      | JUNIOR/MID/SENIOR |
| profile_image    | URL       | 프로필 이미지     |
| self_introduction| TEXT      | 자기소개         |

</details>

---

<details>
<summary>🏆 UserRankings (회원 랭킹)</summary>

* 사용자별 평균 점수 기반 랭킹

| 컬럼명          | 타입        | 설명       |
| --------------- | --------- | -------- |
| ranking_id      | SERIAL PK | 랭킹 ID    |
| user_id         | SERIAL FK | Users 참조 |
| average_score   | FLOAT     | 평균 점수    |
| total_interviews| INT       | 총 인터뷰 횟수 |
| current_rank    | INT       | 현재 순위    |
| created_at      | TIMESTAMP | 생성일      |
| updated_at      | TIMESTAMP | 수정일      |

</details>

---

<details>
<summary>📌 CommunityPosts (커뮤니티 게시글)</summary>

* 면접 후기 공유 게시글

| 컬럼명       | 타입        | 설명       |
| ------------ | --------- | -------- |
| post_id      | SERIAL PK | 게시글 ID   |
| user_id      | SERIAL FK | 작성자     |
| title        | VARCHAR   | 제목       |
| content      | TEXT      | 본문       |
| created_at   | TIMESTAMP | 작성일      |

</details>

---

<details>
<summary>💬 Comments (댓글)</summary>

| 컬럼명       | 타입        | 설명       |
| ------------ | --------- | -------- |
| comment_id   | SERIAL PK | 댓글 ID    |
| user_id      | SERIAL FK | 작성자     |
| post_id      | SERIAL FK | 게시글 참조  |
| content      | TEXT      | 댓글 본문    |
| created_at   | TIMESTAMP | 작성일      |

</details>

---

<details>
<summary>❤️ Likes (좋아요)</summary>

| 컬럼명       | 타입        | 설명       |
| ------------ | --------- | -------- |
| user_id      | BIGINT FK | Users 참조  |
| post_id      | BIGINT FK | CommunityPosts 참조 |
| created_at   | TIMESTAMP | 생성일      |
| (user_id, post_id) | UNIQUE | 중복 방지 |

</details>

---

<details>
<summary>📌 Scraps (스크랩)</summary>

| 컬럼명       | 타입        | 설명       |
| ------------ | --------- | -------- |
| scrap_id     | SERIAL PK | 스크랩 ID   |
| post_id      | BIGINT FK | 게시글 참조  |
| user_id      | BIGINT FK | Users 참조  |

</details>


## ✅ 설계 포인트

1. **User 중심**으로 모든 엔티티가 연결되어 확장성 확보
2. **면접 데이터 → 피드백 → 리포트**까지 흐름이 자연스럽게 이어지도록 구성
3. **커뮤니티·랭킹**은 학습 동기 부여 요소로 독립적이면서도 User와 관계 유지
4. Flyway 기반 **DB 마이그레이션 관리**로 이력 추적 및 협업 용이

---

# ⚙️ 주요 기능 소개

➡️[상세 ERD/테이블 구조 보러가기 (Notion)](https://www.notion.so/25780666e06880e7b2cec00b635cc279?source=copy_link)

<details>
<summary>1️⃣ 로그인 & 회원가입</summary>

* **기본 로그인/회원가입**
  * 이메일 + 비밀번호 등록 (BCrypt 암호화)
  * CSRF, 세션 보안 적용

* **소셜 로그인**
  * Google OAuth2 지원
  * 최초 로그인 시 User 테이블에 provider 저장

* **에러 처리**
  * 중복 이메일 가입 시 예외 반환
  * 인증 실패 시 CustomException 적용

</details>

---

<details>
<summary>2️⃣ 메인 페이지</summary>

* **Hero 배너** – 플랫폼 핵심 가치 강조 (AI 기반 모의면접)
* **서비스 소개 섹션** – 기능 요약 (면접 세션, 피드백, 랭킹, 커뮤니티)
* **후기 섹션** – 실제 사용자 후기(Community 게시글 일부) 노출

</details>

---

<details>
<summary>3️⃣ 면접 세션</summary>

* **질문 생성** – Gemini API 기반, 직무 맞춤형 질문 제공
* **답변 입력** – 사용자 텍스트 입력 (추후 음성 입력 확장 가능)
* **실시간 피드백** – 앨런 AI API 활용
  * 점수 (0~100)
  * 강점
  * 개선점
  * 예시 답변

* **리포트 다운로드** – 면접 결과를 PDF로 저장

</details>

---

<details>
<summary>4️⃣ 마이페이지</summary>

* **프로필 관리** – 이름, 직무, 프로필 이미지 수정
* **면접 기록 조회** – 과거 면접별 점수·피드백 확인
* **시각화 차트** – 점수 변화를 라인차트로 표시
* **내 커뮤니티 활동** – 작성 글·댓글 모아보기

</details>

---

<details>
<summary>5️⃣ 커뮤니티</summary>

* **CRUD 기능**
  * 후기 작성, 수정, 삭제
  * 댓글/좋아요/스크랩 지원

* **검색/필터링**
  * 직무별, 키워드별 검색

* **페이징 처리**
  * 무한 스크롤 적용 (REST API 기반)

</details>

---

<details>
<summary>6️⃣ 랭킹</summary>

* **Top 3 시상대 UI**
  * 점수 기반 상위 3명 시각화

* **Top 20 테이블**
  * 닉네임, 평균 점수, 순위 표시

* **내 순위 강조**
  * 로그인한 사용자의 위치 표시

</details>

---

<details>
<summary>7️⃣ 리포트 PDF</summary>

* **내용 구성**
  * 질문 & 답변
  * AI 피드백 (점수·강점·개선점)
  * 예시 답변
  * 전체 평균 점수

* **기술 스택**
  * iTextPDF (Java 기반 PDF 생성)
  * 다운로드 버튼 제공

</details>

---

<details>
<summary>8️⃣ 관리자 기능 (추가 고려)</summary>

* **사용자 관리**
  * 계정 차단, 역할 변경

* **커뮤니티 관리**
  * 신고 게시물 삭제

* **AI API 모니터링**
  * 호출 수 제한, 응답 지연 모니터링

</details>

---


# 🗂️  디렉토리 구조

본 프로젝트는 **Spring Boot 백엔드 + Thymeleaf 기반 프론트엔드 + AWS 인프라 배포**라는 풀스택 구조를 갖추고 있습니다.
개발, 빌드, 배포 과정에서 모듈별 역할이 명확하게 구분되도록 디렉토리 구조를 설계했습니다.

---

## 📁 전체 구조

```bash

DevView/
├── src/
│   ├── main/
│   │   ├── java/com/allinone/devview/
│   │   │   ├── common/     
│   │   │   ├── community/     
│   │   │   ├── feedback/      
│   │   │   ├── interview/    
│   │   │   ├── mypage/        
│   │   │   ├── ranking/    
│   │   │   ├── security/     
│   │   │   ├── user/           
│   │   │   └── DevViewApplication.java  
│   │   └── resources/
│   │       ├── static/       
│   │       │   ├── css/
│   │       │   ├── js/
│   │       │   └── images/
│   │       ├── templates/     
│   │       ├── db/migration/
│   │       └── application.yml 
│   └── test/                
├── build.gradle / pom.xml    
├── Dockerfile               
├── docker-compose.yml          
└── README.md                  

```

---

## 📂 주요 디렉토리 설명

### 1) `common/`

* **GlobalExceptionHandler** : 공통 예외 처리
* **ResponseDto** : API 응답 표준화
* **CustomErrorCode** : 에러 코드 Enum 관리

### 2) `community/`

* 게시판, 댓글, 좋아요, 스크랩 등 CRUD
* API + Controller + Repository 분리

### 3) `feedback/`

* 면접 피드백 로직
* PDF 변환 유틸리티 (`iTextPDF`)
* AI API 응답 처리

### 4) `interview/`

* Gemini API → 질문 생성
* Alan AI → 답변 피드백
* Interview 엔티티 (질문, 답변, 점수 저장)

### 5) `mypage/`

* 사용자 프로필 관리
* 면접 기록 조회 + 차트 시각화
* 내 커뮤니티 활동 조회

### 6) `ranking/`

* 평균 점수 기반 Top 20 정렬
* 사용자 점수 업데이트 이벤트 처리

### 7) `security/`

* Spring Security 기반 & OAuth2
* BCryptPasswordEncoder 적용
* CustomUserDetailsService

### 8) `user/`

* 회원가입 / 로그인
* OAuth2 연동 사용자 저장
* UserRole (ROLE\_USER, ROLE\_ADMIN)

---

## 📦 배포 관련 파일

* **Dockerfile** : Spring Boot 컨테이너화
* **docker-compose.yml** : PostgreSQL + Redis 연동
* **application.yml** : 환경변수 분리 (dev, prod 프로필 구분)
* **Nginx 설정파일** : EC2 Reverse Proxy


---

## 🌿 브랜치 전략 & 컨벤션

### 🔹 브랜치 전략 (Branch Strategy)

프로젝트는 **GitHub Flow**를 기반으로, **안정성**과 **개발 속도**를 균형 있게 유지하기 위해 다음과 같은 브랜치 전략을 사용했습니다:

* **`main`**

    * 항상 **배포 가능한 상태**를 유지하는 브랜치
    * 코드 리뷰 후, 안정성이 검증된 경우에만 머지(Merge)
    * AWS EC2 서버와 연결되어 **자동 배포 (CI/CD)** 트리거

* **`feature/*`**

    * 새로운 기능 개발을 위한 브랜치
    * 예시: `feature/login-api`, `feature/community-crud`
    * 단위 기능이 완성되면 **Pull Request → 코드 리뷰 → main 병합**

* **`fix/*`**

    * 버그 수정이나 핫픽스를 위한 브랜치
    * 예시: `fix/oauth-token-error`
    * 긴급 수정 후 빠르게 main으로 병합

* **`docs/*`**

    * README, 위키, 발표자료, Swagger 문서 등 **문서 작업 전용 브랜치**

* **`hotfix/*`**

    * 운영 배포 환경에서 **즉시 수정**이 필요한 경우 사용
    * 예시: `hotfix/deployment-nginx`

### 🔹 커밋 컨벤션 (Commit Convention)

Git 커밋 메시지는 **일관된 규칙**을 지켜 코드 히스토리를 이해하기 쉽게 관리했습니다.

```
<type>(<scope>): <subject>
```

* **type 종류**

    * `feat` : 새로운 기능 추가
    * `fix` : 버그 수정
    * `docs` : 문서 수정 (README, 주석 등)
    * `refactor` : 코드 리팩토링 (기능 변화 없음)
    * `test` : 테스트 코드 추가/수정
    * `style` : 코드 스타일 변경 (세미콜론, 들여쓰기 등)
    * `chore` : 빌드/CI, 패키지 관리 작업

* **예시**

    * `feat(auth): Google OAuth2 로그인 기능 추가`
    * `fix(interview): Gemini API 응답 null 처리`
    * `docs(readme): 기술 스택 및 ERD 다이어그램 추가`

### 🔹 코드 컨벤션 (Code Convention)

* **Java (Spring Boot)**

    * 클래스명: `PascalCase`
    * 메서드/변수명: `camelCase`
    * 상수: `UPPER_SNAKE_CASE`
    * 패키지명: 소문자, 기능 단위로 분리 (`community`, `feedback`, `interview`)

* **JavaScript (Frontend)**

    * 함수/변수명: `camelCase`
    * 이벤트 핸들러: `onClickButton`
    * 파일명: 소문자-kebab-case

* **CSS/SCSS**

    * 클래스명: `kebab-case`
    * 공통 색상/폰트는 `:root` 변수로 관리

* **데이터베이스 (PostgreSQL)**

    * 테이블명: 소문자 + 언더스코어(`user_profile`, `interview_history`)
    * 컬럼명: 소문자 + 언더스코어(`created_at`, `updated_at`)
    * PK/FK 명시적으로 설정 (`user_id`, `interview_id`)

### 🔹 Pull Request (PR) 규칙

* **PR 제목 형식**

  ```
  [FEAT] 기능 요약
  [FIX] 버그 수정 요약
  [DOCS] 문서 추가/수정
  ```
* **PR 본문 필수 항목**

    * 작업 내용
    * 스크린샷 (UI 작업 시)
    * 테스트 결과
    * 관련 이슈 번호

### 🔹 협업 흐름

1. 기능별 `feature/*` 브랜치 생성
2. 작업 완료 후 `Pull Request` 생성
3. **팀원 1명 이상 리뷰** → 리뷰어 코멘트 반영
4. CI/CD 빌드 성공 시 → `main` 병합 → AWS 자동 배포

---


## 🛠 트러블슈팅 사례 (Troubleshooting)

### ① 로그인 API 500 오류

* **문제 상황**
  회원가입/로그인 시 특정 사용자에서 500 에러 발생
  (세션 직렬화 불일치로 Redis에 저장 불가)
* **원인 분석**

    * User 객체 직렬화 시 Jackson 매퍼와 Redis 직렬화 설정 충돌
    * DTO/Entity 구분이 불명확하여 세션에 직접 Entity 저장
* **해결 방법**

    * `UserDetails` 구현체를 DTO로 분리
    * Redis 직렬화 방식 JSON으로 통일 (`GenericJackson2JsonRedisSerializer`)
* **배운 점**
  인증 객체는 가볍게 유지하고, 세션 저장 시 DTO/Entity 분리를 반드시 고려해야 함

---

### ② 이미지 업로드 실패 (415 Unsupported Media Type)

* **문제 상황**
  면접 후기 작성 시 이미지 업로드가 실패, 클라이언트 측에서 415 응답
* **원인 분석**

    * Spring Controller의 `@PostMapping`에 `consumes` 속성이 누락됨
    * 클라이언트에서 `multipart/form-data`로 전송했으나 서버에서 처리 불가
* **해결 방법**

    * `@PostMapping(value="/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)` 추가
    * AWS S3 업로드 로직에 MIME 타입 검증 강화
* **배운 점**
  클라이언트-서버 간 Content-Type은 반드시 명확히 맞춰야 하며, 업로드 처리 로직 테스트 자동화를 추가해야 함

---

### ③ Gemini API 응답 지연

* **문제 상황**
  면접 세션에서 질문 생성 및 피드백 요청 시 7\~10초 지연 발생
* **원인 분석**

    * 외부 API 호출이 동기 방식으로 처리
    * 서버 응답 대기 동안 클라이언트 UI 멈춤
* **해결 방법**

    * `@Async` 기반 비동기 처리 적용
    * 프론트엔드에 로딩 스피너 추가 → UX 개선
    * 캐싱 전략 적용 (동일 질문 요청 시 DB/Redis에서 재사용)
* **배운 점**
  외부 API 호출은 반드시 비동기 + 캐싱 고려가 필요, UX 대응책(UI 로딩 처리)도 필수

---

## 🧰 기술 스택 (Tech Stack)

### 📌 프론트엔드 (Frontend)

* **언어 & 프레임워크**

    * **HTML5, SCSS, JavaScript(ES6+)**
    * **Thymeleaf** (Spring Boot와 서버 사이드 렌더링 연동)
* **라이브러리**

    * Chart.js (점수 변화 그래프 시각화)
    * Axios (REST API 비동기 호출)
* **특징**

    * SPA 수준의 인터랙션 + SSR 기반 SEO 대응
    * 반응형 UI 설계 (모바일/데스크탑 공통 지원)

---

### 📌 백엔드 (Backend)

* **언어 & 프레임워크**

    * **Java 17**
    * **Spring Boot 3.x** (REST API, DI, AOP 지원)
* **주요 모듈**

    * **Spring Security + OAuth2** : 세션, Google 로그인 지원
    * **Spring Data JPA** : ORM 기반 DB 매핑
    * **Spring Validation** : 요청 검증, 예외 처리
    * **Flyway** : DB 마이그레이션 자동화

---

### 📌 데이터베이스 (DB)

* **RDBMS** : PostgreSQL 14
* **ERD 설계 기준**

    * 정규화 + 관계 최적화
    * Interview/Feedback/Raking/Community 중심 도메인
* **특징**

    * Flyway Migration으로 형상 관리
    * Redis 캐시 서버 추가 (세션 및 반복 질문 캐싱)

---

### 📌 인프라 & 배포 (Infra & Deployment)

* **AWS EC2** : 백엔드 서버 (Ubuntu 22.04)
* **AWS S3** : 이미지/리포트 업로드 스토리지
* **Nginx** : Reverse Proxy & SSL 인증서 적용
* **Docker** : Spring Boot 컨테이너화
* **CI/CD** : GitHub Actions → EC2 배포 자동화

---

### 📌 AI 서비스 (AI Integration)

* **Gemini API (Google)** : 질문 생성 엔진
* **앨런 AI API** : 답변 피드백 엔진 (점수·강점·개선점·예시 답변)
* **전략**

    * 비동기 호출로 UX 개선
    * AI API 요청/응답 로깅 & 캐싱 (Redis)

---

### 📌 협업 툴 (Collaboration)

* **GitHub** : 코드 관리, Issue/PR 기반 협업
* **Notion** : WBS, 산출물 관리
* **Discord** : 실시간 음성 회의
* **Google Docs/Slides** : 발표 자료 공동 작성
* **Figma** : UI/UX 프로토타이핑

---

### 📌 설계 툴 (Design & Docs)

* **ERDCloud / Draw\.io** : ERD 및 시스템 다이어그램 설계
* **Swagger** : REST API 명세 자동화
* **ReportLab** : PDF 리포트 생성
* **Lombok** : 반복 코드 제거

---

## 💬 팀원별 회고 (Retrospective)

### 👨‍💻 김진욱 (팀장 / 인증·랭킹·배포 담당)

* 처음에는 단순한 로그인 기능부터 시작했지만, OAuth2와 보안, 사용자 개인화까지 아우르는 종합적인 사용자 경험을 설계하면서 많이 성장할수있었던것 같습니다. 특히 자기소개 기반 AI 면접 개인화와 실시간 랭킹 시스템을 통해 단순한 CRUD를 넘어 실제 서비스 수준의 복잡한 비즈니스 로직을 구현할 수 있어 뿌듯했습니다.


**한 줄 소감**
  > “운영까지 고려한 개발 경험을 할 수 있어서 큰 자신감을 얻었습니다.”

---

### 👩‍💻 이소연 (마이페이지·메인페이지·문서화 담당)

* 메인/마이페이지 UI와 Header/Footer 공통 컴포넌트를 제작하며
  서비스 전반에 걸친 UI/UX 일관성 유지의 중요성을 몸소 느꼈습니다.
  또한 README와 Notion 등 협업 문서화를 담당하면서
  단순한 기록이 아닌 팀 생산성을 높이는 자산임을 배울 수 있었습니다.

**한 줄 소감**
> “협업 문서화를 통해 팀 전체의 생산성을 높일 수 있음을 깨달았습니다.”

---

### 👩‍💻 김예은 (커뮤니티 담당)

* 커뮤니티 페이지를 만들면서 단순히 글만 보여주는게 아니라 좋아요, 댓글 같은 상호작용까지 붙으니까 진짜 서비스같은 느낌이 들어서 재밌었습니다.
 
**한 줄 소감**

  > “백엔드 CRUD 구현을 완벽히 경험하며 자신감을 얻었고, 발표 능력도 함께 성장했습니다.”

---

### 👨‍💻 조권호 (면접 세션·AI 담당)


* 개발 처음에는 간단한 Ai 프롬프트 작성부터 시작했지만, 진행하면서 실제 서비스 수준의 고민을 담아내는 좋은 경험이 된 것 같습니다. 그리고 이전 프로젝트의 경험과 피드백들이 많은 도움이 된 것 같습니다. 다른 팀원분들 없이 혼자 모든 프로젝트를 진행했다면 아직까지도 끙끙대며 설계했을 것 같아 다시한번 협업의 중요성을 깨달았습니다.

**한 줄 소감**

  > “AI와 백엔드, 사용자 경험까지 연결하는 흐름을 직접 설계한 경험이 가장 값졌습니다.”


---
<div align="center">
  <strong>🎯 DevView와 함께 면접 준비의 새로운 기준을 만들어가요!</strong>
</div>
