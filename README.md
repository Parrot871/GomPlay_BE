# 🐻GomPlay - 개인 맞춤형 운동 파트너 매칭 플랫폼
> 단국대학교 학생을 대상으로 운동 성향과 공강 시간대에 맞는 파트너를 매칭해주는 운동 파트너 플랫폼

&nbsp;
## 📌 프로젝트 소개
* GomPlay는 대학생들이 운동을 꾸준히 지속할 수 있도록 지원하는 운동 파트너 매칭 서비스입니다. <br>
* 혼자 운동할 때 겪는 동기 부족과 파트너 탐색의 어려움을 해결하기 위해, 학교 이메일 인증 기반의 신뢰성 있는 사용자 환경과 운동 성향·시간표 기반의 맞춤형 매칭을 핵심 가치로 설계하였습니다. <br> 
* 1:1 퀵 매칭부터 그룹 운동 모집까지, 대학생들이 부담 없이 함께 운동할 수 있는 환경을 제공합니다.

&nbsp;
## ⏳ 프로젝트 기간
2026.3 ~ 2026.6

&nbsp;
## 👥 팀원 소개
| 이름  | 역할      | 주요 담담 기능|
|-----|---------| ------|
| 신동은 | Backend | 사용자 인증, 퀵 매치, 1:1 채팅|
| 한상윤 | Backend | 추천 알고리즘, 운동 모집, 그룹 채팅|

&nbsp;
## ✨ 주요 기능
1️⃣ **사용자 인증**
* 학교 이메일 인증 기반 회원가입
* JWT 기반 로그인 / 로그아웃
* 리프레시 토큰을 통한 자동 로그인

2️⃣ **퀵 매치**
* 공강 시간, 운동 성향, 선호 종목, 매너온도 기반 1:1 파트너 매칭
* 양방향 매칭 점수 산출로 상호 선호도 반영
* 매칭 성사 시 1:1 채팅방 자동 생성

3️⃣ **운동 모집**
* 운동 종목, 장소, 일정, 난이도 기반 그룹 모집글 작성
* 참여 신청 수락 시 그룹 채팅방 자동 생성
* 포인트 기반 모집글 부스트(24시간 상단 노출) 기능

4️⃣ **추천 알고리즘**
* 자카드 유사도 기반 종목 및 공강 시간 유사도 계산
* 퀵 매치: 시간표(30%) · 종목(25%) · 성향(20%) · 매너온도(15%) · 학과/학번(각 5%)
* 운동 모집: 종목(35%) · 시간표(25%) · 난이도(15%) · 매너온도(10%) · 마감일(10%) · 남은 인원(5%)

5️⃣ **장소 추천**
* Google Places API 기반 주변 운동 시설 조회
* Haversine 공식을 활용한 정확한 거리 계산
* 평점순 / 거리순 정렬 제공

6️⃣ **포인트 및 출석 체크**
* 출석 체크, 운동 완료, 평가 작성 등 활동 기반 포인트 적립
* 포인트 소모를 통한 서비스 내 기능 이용 (퀵 매치, 부스트 등)

7️⃣ **평가 및 매너온도**
* 운동 완료 후 긍정·부정 태그 기반 파트너 평가
* 매너온도 시스템으로 신뢰도 관리
* 노쇼 신고 및 차등 페널티 적용

&nbsp;
## 🛠️ 기술 스택
| 분류           | 기술                          |
|--------------|-----------------------------|
| Language     | Java 21                     |
| Framework    | Spring Boot 4.0.5           |
| Database     | MySQL (AWS RDS)             |
| ORM          | JPA / Hibernate             |
| Auth         | JWT                         |
| Storage      | AWS S3                      |
| Infra        | AWS EC2, Docker Compose     |
| CI/CD        | GitHub Actions              |
| Real-time    | WebSocket, STOMP            |
| External API | Google Places API, Groq API |

&nbsp;
## 🏗️ 시스템 아키텍처
<img width="1132" height="471" alt="image" src="https://github.com/user-attachments/assets/cfc47d8b-7cba-487a-97cd-f3f82dad1118" />


## 🏗️ CI/CD 파이프라인
<img width="1132" height="471" alt="image" src="https://github.com/user-attachments/assets/094689f3-eed2-4a7a-a528-8226d18884fb" />


## 📂 프로젝트 구조
```
src/main/java/com/example/gomplay
├── domain
│   ├── auth          # 인증 (JWT, 이메일 인증)
│   ├── user          # 사용자 프로필
│   ├── survey        # 운동 성향 설문
│   ├── matching      # 퀵 매칭
│   ├── team          # 운동 모집
│   ├── chat          # 1:1 채팅
│   ├── groupchat     # 그룹 채팅
│   ├── notification  # 알림
│   ├── review        # 평가
│   ├── report        # 신고
│   ├── point         # 포인트
│   └── attendance    # 출석 체크
├── global
│   ├── common        # 공통 응답 객체
│   ├── config        # 설정 (Security, WebSocket 등)
│   ├── exception     # 예외 처리
│   ├── filter        # JWT 인증
│   ├── mail          # 이메일 발송
│   ├── places        # Google Places API 연동
│   ├── s3            # AWS S3 파일 관리
│   ├── util          # 유틸리티
│   └── websocket     # WebSocket 설정 및 실시간 메시지 처리
└── GomplayApplication.java
```

