# second commit 필기

## mysql 연결 후 실행
application.yml을 작성한 후  
schema-mysql.sql파일의 쿼리문으로 mysql에 메타테이블을 생성했습니다.

![run_success_1.jpg](./image/run_success_1.jpg)

이제 프로그램이 정상 실행된 것을 볼 수 있습니다.

메타 테이블들의 의미를 하나씩 살펴보면,

1. BATCH_JOB_INSTANCE  
Job Parameter에 따라 생성되는 테이블  
Job Parameter란, Spring Batch가 실행될 때 외부에서 받을 수 있는 파라미터  
(예: 특정 날짜를 잡 파라미터로 넘기면 해당 데이터를 조회/가공/입력 등의 작업을 할 수 있다.)  

- parameter를 다르게 받은 경우

![run_success_2.jpg](./image/run_success_2.jpg)

![run_success_3.jpg](./image/run_success_3.jpg)

파라미터가 다른 값으로 실행할 경우 메타 테이블에 새로운 job instance가 추가 됩니다.
 
 ![run_success_4.jpg](./image/run_success_4.jpg)

같은 파라미터로는 Job을 실행할 수 없습니다.

Job Instance 테이블 네이밍이 적절한 것이
마치 Java의 Class로 여러 Instance가 생성되는 것과 비슷해 보입니다.

 