# API 문서
1. [사용자](#사용자)
    - [🔐 로그인](#🔐-로그인)
    - [🔐 OAuth 로그인](#🔐-OAuth-로그인)
    - [🚪 로그아웃](#🚪-로그아웃)
    - [📝 회원가입](#📝-회원가입)
    - [🔑 비밀번호 재설정](#🔑-비밀번호-재설정)
    - [👤 현재 사용자 조회](#👤-현재-사용자-조회)
    - [✏️ 현재 사용자 정보 수정](#✏️-현재-사용자-정보-수정)
    - [🗑️ 현재 사용자 삭제](#🗑️-현재-사용자-삭제)
    - [🔑 현재 사용자의 비밀번호 변경](#🔑-현재-사용자의-비밀번호-변경)

2. [노트](#노트)
    - [📝 모든 노트 조회](#📝-모든-노트-조회)
    - [📝 노트 생성](#📝-노트-생성)
    - [🗑️ 노트 삭제](#🗑️-노트-삭제)
    - [🔍 노트 조회](#🔍-노트-조회)
    - [✏️ 노트 수정](#✏️-노트-수정)
    - [🤖 AI 요약](#🤖-AI-요약)
    - [🔍 노트 내 검색](#🔍-노트-내-검색)

3. [폴더](#폴더)
    - [📂 모든 폴더 조회](#📂-모든-폴더-조회)
    - [📂 폴더 조회](#📂-폴더-조회)
    - [✏️ 폴더 수정](#✏️-폴더-수정)
    - [🗑️ 폴더 삭제](#🗑️-폴더-삭제)

4. [캘린더](#캘린더)
    - [📅 날짜 범위 내 노트 조회](#📅-날짜-범위-내-노트-조회)

## 엔드포인트

### 사용자

- **[🔐 로그인](#🔐-로그인)**
    - **POST** `/users/login`
    - 설명: 시스템에 로그인
    - Request Body:
        - `email`: string (이메일)
        - `password`: string (비밀번호)
    - Responses:
        - `200`: 로그인 성공, JWT 토큰 발급
        - `400`: 잘못된 요청 (잘못된 입력)
        - `401`: 인증 실패

- **[🔐 OAuth 로그인](#🔐-OAuth-로그인)**
    - **POST** `/oauth/login`
    - 설명: OAuth를 이용한 시스템 로그인
    - Request Body:
        - `oauth_provider`: string
        - `oauth_provider_id`: string
    - Responses:
        - `200`: 로그인 성공, JWT 토큰 발급
        - `400`: 잘못된 요청 (잘못된 입력)
        - `401`: 인증 실패

- **[🚪 로그아웃](#🚪-로그아웃)**
    - **POST** `/users/logout`
    - 설명: 시스템에서 로그아웃
    - Responses:
        - `200`: 로그아웃 성공
        - `401`: 인증 실패

- **[📝 회원가입](#📝-회원가입)**
    - **POST** `/users/register`
    - 설명: 새로운 사용자 회원가입
    - Request Body:
        - `username`: string
        - `email`: string (이메일)
        - `password`: string (비밀번호)
    - Responses:
        - `201`: 회원가입 성공
        - `400`: 잘못된 요청 (잘못된 입력)

- **[🔑 비밀번호 재설정](#🔑-비밀번호-재설정)**
    - **POST** `/users/reset-password`
    - 설명: 사용자의 비밀번호 재설정
    - Request Body:
        - `reset_token`: string
        - `new_password`: string (비밀번호)
    - Responses:
        - `200`: 비밀번호 재설정 성공
        - `400`: 잘못된 요청 (잘못된 입력)
        - `401`: 인증 실패

- **[👤 현재 사용자 조회](#👤-현재-사용자-조회)**
    - **GET** `/users/me`
    - 설명: JWT 토큰을 이용해 인증된 사용자 정보 조회
    - Responses:
        - `200`: 사용자 객체
        - `401`: 인증 실패

- **[✏️ 현재 사용자 정보 수정](#✏️-현재-사용자-정보-수정)**
    - **PATCH** `/users/me`
    - 설명: 인증된 사용자 정보 일부 수정
    - Request Body:
        - `username`: string
        - `email`: string (이메일)
    - Responses:
        - `200`: 사용자 정보 수정 성공
        - `400`: 잘못된 요청 (잘못된 입력)
        - `401`: 인증 실패

- **[🗑️ 현재 사용자 삭제](#🗑️-현재-사용자-삭제)**
    - **DELETE** `/users/me`
    - 설명: 인증된 사용자 계정 삭제
    - Responses:
        - `200`: 사용자 삭제 성공
        - `401`: 인증 실패
        - `404`: 사용자 찾을 수 없음

- **[🔑 현재 사용자의 비밀번호 변경](#🔑-현재-사용자의-비밀번호-변경)**
    - **PUT** `/users/me/password`
    - 설명: JWT 토큰을 사용해 인증된 사용자가 비밀번호 변경
    - Request Body:
        - `current_password`: string (현재 비밀번호)
        - `new_password`: string (새 비밀번호)
    - Responses:
        - `200`: 비밀번호 변경 성공
        - `400`: 잘못된 요청 (잘못된 입력)
        - `401`: 인증 실패
        - `403`: 금지됨 (현재 비밀번호가 틀림)

### 노트

- **[📝 모든 노트 조회](#📝-모든-노트-조회)**
    - **GET** `/notes`
    - 설명: 인증된 사용자가 생성한 모든 노트 조회
    - Responses:
        - `200`: 노트 객체 배열
        - `401`: 인증 실패

- **[📝 노트 생성](#📝-노트-생성)**
    - **POST** `/notes`
    - 설명: 인증된 사용자가 노트 생성
    - Request Body:
        - `title`: string
        - `folder`: string
        - `duration`: integer
        - `speaker`: string 배열
        - `script`: 객체 배열
    - Responses:
        - `201`: 노트 생성 성공
        - `400`: 잘못된 요청 (잘못된 입력)
        - `401`: 인증 실패

- **[🗑️ 노트 삭제](#🗑️-노트-삭제)**
    - **DELETE** `/notes/{noteId}`
    - 설명: ID로 특정 노트 삭제
    - Parameters:
        - `noteId`: integer
    - Responses:
        - `200`: 노트 삭제 성공
        - `404`: 노트 찾을 수 없음

- **[🔍 노트 조회](#🔍-노트-조회)**
    - **GET** `/notes/{noteId}`
    - 설명: 인증된 사용자가 생성한 특정 노트 조회
    - Parameters:
        - `noteId`: integer
    - Responses:
        - `200`: 노트 객체
        - `401`: 인증 실패
        - `404`: 노트 찾을 수 없음

- **[✏️ 노트 수정](#✏️-노트-수정)**
    - **PATCH** `/notes/{noteId}`
    - 설명: 인증된 사용자가 생성한 특정 노트 일부 수정
    - Parameters:
        - `noteId`: integer
    - Request Body:
        - `title`: string
        - `folder`: string
    - Responses:
        - `200`: 노트 수정 성공
        - `400`: 잘못된 요청 (잘못된 입력)
        - `401`: 인증 실패
        - `404`: 노트 찾을 수 없음

- **[🤖 AI 요약](#🤖-AI-요약)**
    - **GET** `/notes/{noteId}/ai-summary`
    - 설명: 인증된 사용자가 생성한 노트의 AI 요약 조회
    - Parameters:
        - `noteId`: integer
    - Responses:
        - `200`: AI 요약 객체
        - `401`: 인증 실패
        - `404`: 노트 찾을 수 없음

- **[🔍 노트 내 검색](#🔍-노트-내-검색)**
    - **GET** `/notes/{noteId}/search`
    - 설명: 노트 내용, AI 요약 및 메모에서 특정 텍스트 검색
    - Parameters:
        - `noteId`: integer
        - `query`: string
    - Responses:
        - `200`: 노트 내 검색 결과
        - `404`: 노트 또는 쿼리 찾을 수 없음
        - `401`: 인증 실패

### 폴더

- **[📂 모든 폴더 조회](#📂-모든-폴더-조회)**
    - **GET** `/folders`
    - 설명: 인증된 사용자가 생성한 모든 폴더 조회
    - Responses:
        - `200`: 폴더 객체 배열
        - `401`: 인증 실패

- **[📂 폴더 조회](#📂-폴더-조회)**
    - **GET** `/folders/{folderId}`
    - 설명: 인증된 사용자가 생성한 특정 폴더 조회
    - Parameters:
        - `folderId`: integer
    - Responses:
        - `200`: 폴더 객체
        - `401`: 인증 실패
        - `404`: 폴더 찾을 수 없음

- **[✏️ 폴더 수정](#✏️-폴더-수정)**
    - **PATCH** `/folders/{folderId}`
    - 설명: 인증된 사용자가 생성한 폴더의 일부 수정
    - Parameters:
        - `folderId`: integer
    - Request Body:
        - `name`: string
    - Responses:
        - `200`: 폴더 수정 성공
        - `400`: 잘못된 요청 (잘못된 입력)
        - `401`: 인증 실패
        - `404`: 폴더 찾을 수 없음

- **[🗑️ 폴더 삭제](#🗑️-폴더-삭제)**
    - **DELETE** `/folders/{folderId}`
    - 설명: 인증된 사용자가 생성한 폴더 삭제
    - Parameters:
        - `folderId`: integer
    - Responses:
        - `200`: 폴더 삭제 성공
        - `401`: 인증 실패
        - `404`: 폴더 찾을 수 없음

### 캘린더

- **[📅 날짜 범위 내 노트 조회](#📅-날짜-범위-내-노트-조회)**
    - **GET** `/calendar`
    - 설명: 인증된 사용자가 특정 날짜 범위 내에서 생성한 모든 노트 조회
    - Request Body:
        - `start_date`: string (날짜)
        - `end_date`: string (날짜)
    - Responses:
        - `200`: 노트 객체 배열
        - `401`: 인증 실패
        - `404`: 노트 찾을 수 없음
