# Spring Batch 샘플 프로젝트 생성
jojoldu님의 Spring Batch 가이드를 따라 치는중입니다.
- https://jojoldu.tistory.com/326?category=635883


## mysql 연결 후 실행
  
application.yml을 작성한 후  
schema-mysql.sql파일의 쿼리문으로 mysql에 메타테이블을 생성했습니다.

![run_success_1.jpg](./image/run_success_1.jpg)

이제 프로그램이 정상 실행된 것을 볼 수 있습니다.

### 주요 메타 테이블

1. BATCH_JOB_INSTANCE  
    - 배치에서 제일 처음 쓰는 테이블일까? 아닐듯
    - Job Parameter에 따라 생성되는 테이블
    - Job을 한 번 성공시키면 같은 Parameter로는 더 실행이 불가능하다.
    - 네이밍 이해: 마치 Java의 class로 여러 instance를 생성하는 것과 비슷하다.
    
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

2. BATCH_JOB_EXECUTION
    - JOB_INSTANCE가 성공/실패한 모든 내역을 가지고 있음.
    - 컬럼: 1번 테이블과 JOB_INSTANCE_ID를 공유하며, 인스턴스 id로 성공, 실패를 확인 가능.  
      (STATUS, EXIT_CODE, EXIT_MESSAGE)
    - 성공한 기록이 있을때만 같은 param으로 재수행이 안된다.

3. BATCH_JOB_EXECUTION_PARAMS
    - job이 실행될 때 입력받은 param을 저장하는 테이블
    - param이 null인 job은 들어가있지 않음.

4. 기타 테이블
    - Spring Batch 재시도/SKIP 전략편에서 자세하게 소개

- 나중엔 테스트 코드를 가지고 spring batch 예제를 작성하게 될 것인데,
  지금 당장 그렇게 하지 않는 이유는, metaData가 남지 않기 때문입니다.
  (Job Instance Context 문제를 겪는다 함.)
  후반엔 H2를 이용한 테스트 코드를 작성할 것.
  
  

### Next
step을 순차적으로 연결시킬 때 사용된다.

#### 원하는 Job만 실행시키려면
1. application.yml에 내용 추가
    ```
    spring.batch.job.names: ${job.name:NONE}
    ```
    - job.name이 있으면 그걸 할당하고, 없으면 NONE을 할당한다는 뜻
    - NONE이 할당되면 어떤 배치도 실행하지 않겠다는 의미(배치 실행을 막는 역할)

2. program argument에 아래 내용 추가
    ```    
    --job.name=stepNextJob version=2
    ``` 

* 실제 운영 환경에서는 java -jar batch-application.jar --job.name=simpleJob 과 같이 배치를 실행합니다.

### 조건별 흐름 제어(Flow)
- next는 앞의 step에서 오류가 나면 나머지 뒤에 있는 step 들은 실행되지 못한다.
- 상황에 따라 오류/정상에 따라 행동분기를 나누고 싶을 때 사용한다.

1. on()
    - 캐치할 ExitStatus 지정
    - * 일 경우 모든 ExitStatus가 지정
2. to()
    - 다음으로 이동할 Step 지정
3. from()
    - 일종의 이벤트 리스너 역할
    - 상태값을 보고 일치하는 상태라면 to()에 포함된 `step`을 호출
    - step1의 이벤트 캐치가 FAILED로 되있는 상태에서 추가로 이벤트 캐치하려면 from을 써야 함
4. end()
    - end는 FlowBuilder를 반환하는 end와 FlowBuilder를 종료하는 end 2개가 있음
    - on("*")뒤에 있는 end는 FlowBuilder를 반환하는 end
    - build() 앞에 있는 end는 FlowBuilder를 종료하는 end
    - FlowBuilder를 반환하는 end 사용시 계속해서 from을 이어갈 수 있음
    
- on이 캐치하는 상태값은 BatchStatus가 아닌 ExitStatus라는 점
- 분기 처리를 위해 상태값 조정이 필요하다면 ExitStatus를 조정해야 한다.


### 번외 2. Batch Status vs. Exit Status
- Batch Status는 Job 또는 Step의 실행 결과를 Spring에서 기록할 때 사용하는 Enum
    - 사용되는 값 : COMPLETED, STARTING, STOPPING, STOPPED, FAILED, ABANDONED, UNKNOWN
    ```
    .on("FAILED").to(stepB())
    ```
    - 위 FAILED는 BatchStatus가 아니라 Step의 ExitStatus이다.
    - exitCode가 FAILED로 끝나게 되면 Step B로 가라는 뜻.
    
- Spring Batch는 기본적으로 ExitStatus의 exitCode는 Step의 BatchStatus와 같도록 설정이 되어있다.
- 커스컴한 exitCode가 필요하다면?
```
.start(step1())
    .on("FAILED")
    .end()
.from(step1())
    .on("COMPLETED WITH SKIPS")
    .to(errorPrint1())
    .end()
.from(step1())
    .on("*")
    .to(step2())
    .end()
```
- `COMPLETED WITH SKIPS`는 ExitStatus에 없는 코드
- 해당 exitCode를 반환하는 별도의 로직이 필요하다.
```
public class SkipCheckingListener extends StepExecutionListenerSupport {

    public ExitStatus afterStep(StepExecution stepExecution) {
        String exitCode = stepExecution.getExitStatus().getExitCode();
        if (!exitCode.equals(ExitStatus.FAILED.getExitCode()) && 
              stepExecution.getSkipCount() > 0) {
            return new ExitStatus("COMPLETED WITH SKIPS");
        }
        else {
            return null;
        }
    }
}
```

- 먼저 Step이 성공적으로 수행되었는지 확인한 후,
- StepExecution의 skip 횟수가 0보다 클 경우 위 exitCode를 갖는 ExitStatus를 반환

### Decide
분기처리만을 담당

위에서 알아본 방식은 문제가 2개 존재
- Step이 담당하는 역할이 2가지
- 다양한 분기 로직 처리에 어려움
    - ExitStatus를 커스텀하게 고치기 위해서 Listener 생성하고 JobFlow에 등록하는 등 복잡

- `JobExecutionDecider`


- 예제코드 : DeciderJobConfiguration
- Step과 명확히 역할과 책임이 분리되어있음.
- ExitStatus가 아닌 FlowExecutionStatus로 상태를 관리.
- 그걸 `.from().on()` 에서 사용



# JobScope, StepScope

Spring Batch Scope & Job Parameter

@StepScope, @JobScope

스프링 배치는 내,외부에서 파라미터를 받아 여러 Batch 컴포넌트에서 사용할 수 있게 지원해주는데, 

이 파라미터를 Job Parameter라고 하며, 

Spring Batch 전용 어노테이션을 선언해야 사용 가능합니다.

이 때 쓰이는 것이 위의 두 어노테이션입니다.

사용법은 SpEL로 선언합니다.

    @Value("#{jobParameters[파라미터명]}")

```
@Bean
public Job scopeJob() {
        return jobBuilderFactory.get("scopeJob")
                .start(scopeStep1(null))
                .next(scopeStep2())
                .build();
}

@Bean
@JobScope
public Step scopeStep1(@Value("#{jobParameters[requestDate]}") String requestDate) {
        return stepBiulderFactory.get("scopeStep1")
                .tasklet((contribution, chunkContext) -> {
                        log.info(">>>>> This is scopeStep1");
                        log.info(">>>>> requestDate = {}", requestDate);
                        return RepeatStatus.FINISHED;
                })
                .build();
}
```

```
@Bean
public Step scopeStep2() {
        return stepBuilderFactory.get("scopeStep2")
                .tasklet(scopeStep2Tasklet(null)) 
// null인 이유는 JobParameter의 할당이 어플리케이션 실행시에 이뤄지지 않기 때문
                .build();
}

@Bean
@StepScope
public Tasklet scopeStep2Tasklet(@Value("#{jobParameters[requestDate]}") String requestDate {
        return (contribution, chunkContext) -> {
                log.info(">>>>> This is scopeStep2");
                log.info(">>>>> requestDate = {}", requestDate);
                return RepeatStatus.FINISHED;
        };
}
```

두 어노테이션이 사용 가능한 상황이 다르다.

- @JobScope : Step 선언문
- @StepScope : Tasklet, ItemReader, ItemWriter, ItemProcessor

JobParameter의 타입

- Double, Long
- Date
- String
- ~~LocalDate, LocalTime~~ 대신 String으로 받아 타입 변환 필요

JobScope > StepScope

https://jojoldu.tistory.com/330?category=902551 진행중..