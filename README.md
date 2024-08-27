# 자바 DHN_CLIENT 건강보험심사평가원 (hira)
## Database
- oracle

## Java version
- 1.8

## Function
- 미완성 (아직 심사평가원 테이블이 아님 테스트로 기술대로 함)
- JSON Validation (json화 -> json풀기 후 기존객체와 비교)
- 개별 json validation 오류시 상태코드 별도로 저장 (문제있는 데이터라고 판단)
- HttpStatusCode = NOT_FOUND -> 데이터 전송 시 패킷로스 (데이터 손상) -> 재발송
- 각각의 스케쥴러가 5개의 쓰레드를 가짐