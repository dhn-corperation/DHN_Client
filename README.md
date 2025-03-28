# 자바 DHN_CLIENT (차세대)국민연금공단 데이터 넣어주는 에이전트 (결과 각각 처리)
## Service
- Oracle DB

## Java version
- 1.8

## Issue
- 각 발송 프로세스 예외처리 추가
- 대형 발송테이블 -> 발송요청 -> 공단 결과처리 -> 대형 결과처리
- Active-Standby 로 작동 (10초마다 체크 1분이상 경과시 변경)
- 각 프로세스 쓰레드 처리 (알림톡 5개, 문자 각각 3개씩)
- 결과처리 건 by 건 update insert delete (공단은 update만)