# 자바 DHN_CLIENT (주택도시보증공사) DB2 -> Oracle
## Service
- Oracle DB

## Java version
- 1.8

## Issue
- DB2 쿼리문 Oracle으로 변경
- 결과처리 프로세스 고도화 및 수정
- get_crypto 사용 업체
- config 설정 암호화 사용 했나?
- MMS_IMAGE 리턴값 image_group or image group
- DB 컬럼 CHAR 타입 남는부분 공백처리되므로 #{} -> ${} 처리
- 로그파일 별도 저장위치 변경 (주택도시보증공사 요청)
- - /logs/DHNClient/로그파일
- - /app/DHNClient/에이전트파일