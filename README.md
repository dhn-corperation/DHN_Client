# 자바 DHN_CLIENT 강원랜드 - 중계서버
## Service
- mssql

## Java version
- 1.8

## Send System
- CMS
- RMS
- CXM
- ERP
- WEB(홈페이지)

## 변경사항
- 서버 1대(중계서버-이중화일듯) 에서 운영
- DB는 Ms-sql 사용 (17로 예상)
- OS는 Linux
- 공통컬럼은 동일, 시스템별 추가 컬럼 있음
- MMS 발송 시 마지막 / 기준으로 파일명 추출 및 시스템별 yml에 설정된 경로 + 파일명 으로 이미지 발송
- 발송테이블 -> 로그테이블 이동 시 insert into select * 으로 전체 이동 (ERP는 컬럼명 지정)
- 발송 프로세스 주기 : 1초
- 결과처리 프로세스 주기 : 1초