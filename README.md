# SocketInterceptor

## 프로젝트 소개

SocketInterceptor는 Java 애플리케이션의 소켓 통신을 가로채고 모니터링하는 Java Agent 프로젝트입니다. ByteBuddy를 사용하여 런타임에 클래스 바이트코드를 조작하여 소켓의 입출력 스트림을 인터셉트하고, 통신 데이터를 로그 파일에 기록합니다.

## 주요 기능

- **소켓 통신 모니터링**: Socket 클래스의 getInputStream()과 getOutputStream() 메소드를 가로채어 데이터 송수신을 추적
- **실시간 로깅**: 통신 데이터를 실시간으로 파일에 기록
- **비침투적 모니터링**: 애플리케이션 코드 수정 없이 Java Agent로 동작
- **유연한 설정**: 로그 파일 경로, JAR 파일 경로 등을 런타임에 설정 가능

## 프로젝트 구조

```
SocketInterceptorAgent/
├── src/main/java/com/example/
│   ├── agent/                    # Java Agent 관련 클래스들
│   │   ├── SocketAgent.java      # 메인 Agent 클래스
│   │   ├── BufferedReaderAdvice.java
│   │   ├── GetInputStreamAdvice.java
│   │   ├── GetOutputStreamAdvice.java
│   │   └── GetRabbitAdvice.java
│   ├── app/                      # 테스트용 애플리케이션
│   │   ├── MyApplication.java    # 클라이언트 애플리케이션
│   │   ├── MyEchoServer.java     # 서버 애플리케이션
│   │   └── PullOut.java
│   └── interceptor/              # 인터셉터 관련 유틸리티
│       ├── AgentConfig.java      # Agent 설정 관리
│       ├── InterceptedInputStream.java
│       ├── InterceptedOutputStream.java
│       └── RecordToFileUtil.java # 파일 로깅 유틸리티
├── pom.xml                       # Maven 설정
└── README.md
```

## 기술 스택

- **Java 11**: 기본 개발 언어
- **ByteBuddy**: 런타임 바이트코드 조작 라이브러리
- **Javassist**: 바이트코드 조작 지원
- **Maven**: 빌드 도구

## 동작 원리

1. **Agent 로드**: JVM 시작 시 `-javaagent` 옵션으로 Agent JAR 파일 로드
2. **클래스 변환**: ByteBuddy를 사용하여 Socket 클래스의 메소드를 조작
3. **데이터 인터셉트**: getInputStream()과 getOutputStream() 호출 시 Advice 클래스 실행
4. **로깅**: 인터셉트된 데이터를 지정된 로그 파일에 기록

## 사용 방법

### 1. 저장소 클론
```bash
git clone <이 저장소 주소>
cd SocketInterceptorAgent
```

### 2. 빌드 (Java 11, Maven 필요)
```bash
mvn clean package
```

### 3. 서버(MyEchoServer) 실행 (새 터미널에서 백그라운드로 실행 권장)
```bash
java -classpath target/classes com.example.app.MyEchoServer
```

### 4. 클라이언트(MyApplication) + 에이전트 실행
아래 명령어를 사용하세요:
```bash
java --add-opens=java.base/java.io=ALL-UNNAMED \
-Xbootclasspath/a:target/SocketInterceptorAgent-1.0-SNAPSHOT-all.jar \
-javaagent:target/SocketInterceptorAgent-1.0-SNAPSHOT-all.jar=logFile=agentlog.txt,jarFile=$(pwd)/target/SocketInterceptorAgent-1.0-SNAPSHOT-all.jar \
-classpath target/classes com.example.app.MyApplication
```

### 5. 결과 확인
- `agentlog.txt` 파일에 송수신 데이터가 기록됩니다.
- 서버/클라이언트 표준 출력도 확인 가능합니다.

## 로그 형식

Agent가 생성하는 로그 파일의 형식:
```
2025-07-10T22:24:59.175374 [SEND_DATA] Hello Server
2025-07-10T22:24:59.187326 [RECV_DATA] echo>> Hello Server
2025-07-10T22:24:59.187563 [SEND_DATA] Another Message
2025-07-10T22:24:59.187989 [RECV_DATA] echo>> Another Message
```

- `[SEND_DATA]`: 클라이언트에서 서버로 전송하는 데이터
- `[RECV_DATA]`: 서버에서 클라이언트로 수신하는 데이터
- 타임스탬프는 ISO 8601 형식으로 기록

## Agent 옵션

- `logFile`: 로그 파일 경로 (기본값: agentlog.txt)
- `jarFile`: Agent JAR 파일 경로 (필수)

## 주의사항

- Java 11 이상, Maven 필요
- 서버(MyEchoServer)는 클라이언트 실행 전에 반드시 실행되어 있어야 합니다.
- Agent는 JVM 시작 시에만 로드되므로, 실행 중인 애플리케이션에는 적용할 수 없습니다.
- 일부 보안 정책이나 클래스로더 제한으로 인해 모든 환경에서 동작하지 않을 수 있습니다.

## 개발 환경

- **JDK**: 11 이상
- **Maven**: 3.6 이상
- **OS**: Linux (테스트 완료), Windows, macOS에서도 동작 가능

## 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다.

## 기여하기

버그 리포트, 기능 요청, 풀 리퀘스트 등 모든 기여를 환영합니다.

## 문의

추가 옵션이나 환경이 필요한 경우, README를 참고하거나 이슈를 통해 문의하세요.