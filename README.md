# 자바 DHN_CLIENT 국민연금공단 차세대
## Service
- Oracle DB

## Java version
- 1.8

## Issue
- 템플릿 제거 (API로 변경)
- 각 발송 프로세스 예외처리 추가
- 공단 발송테이블 -> 대형 발송테이블 -> 발송요청 -> 공단 결과처리 -> 대형 결과처리
- Active-Standby 로 작동 (10초마다 체크 1분이상 경과시 변경)