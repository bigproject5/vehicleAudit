# CHECKAR :: Vehicle Audit

AI를 활용하여 차량의 손상 여부를 점검하고, RAG(검색 증강 생성) 모델을 통해 수리 가이드를 제안하는 지능형 차량 감사 시스템입니다.

## 주요 기능

- **AI 기반 자동 점검**: 업로드된 차량 이미지를 분석하여 손상 부품, 결함 종류 등을 자동으로 탐지합니다.
- **이벤트 기반 아키텍처**: Kafka를 사용하여 이미지 분석과 같은 시간이 소요되는 작업을 비동기적으로 처리하여 시스템의 응답성과 확장성을 높였습니다.
- **RAG 기반 가이드 제안**: LLM과 내부 문서 데이터베이스를 결합한 RAG 모델을 통해 특정 손상에 대한 최적의 수리 절차와 가이드를 제공합니다.
- **RESTful API**: 감사 생성, 결과 조회, RAG 제안 등 시스템의 모든 기능을 외부에서 쉽게 연동하고 사용할 수 있도록 RESTful API를 제공합니다.
- **문서 관리**: RAG 모델이 참고하는 기술 문서(.docx)를 시스템에 쉽게 추가하고 관리할 수 있는 기능을 제공합니다.

## 시스템 아키텍처

이 시스템은 다음과 같은 주요 구성 요소로 이루어져 있습니다.

1.  **Spring Boot Application**:
    -   **Web/API Layer**: 외부 요청을 처리하는 RESTful API 엔드포인트입니다.
    -   **Service Layer**: 감사 생성, 상태 업데이트, RAG 제안 등 핵심 비즈니스 로직을 처리합니다.
    -   **Kafka Producer/Consumer**: AI 워커와 비동기 메시지를 주고받습니다.
2.  **Kafka**: 이미지 분석 요청과 그 결과를 비동기적으로 전달하는 메시지 큐입니다.
3.  **AI Worker**: Kafka로부터 이미지 분석 요청을 받아 AI 모델을 통해 작업을 수행하고 결과를 다시 Kafka로 전송하는 외부 프로세스입니다.
4.  **Databases**:
    -   **MySQL**: 감사, 점검 내역 등 시스템의 핵심 데이터를 저장하는 주 데이터베이스입니다.
    -   **PostgreSQL (pgvector)**: RAG 모델의 지식 기반이 되는 문서 임베딩 벡터를 저장하고 검색하는 데 사용됩니다.
5.  **AWS S3**: 사용자가 업로드하는 차량 이미지를 저장하는 클라우드 스토리지입니다.
6.  **OpenAI**: 자연어 처리 및 임베딩 생성을 위해 `gpt-4o-mini`와 `text-embedding-3-small` 모델을 사용합니다.

## API Documentation

API에 대한 상세한 명세는 아래 Postman 문서를 참고해 주십시오.
- [Vehicle Audit API Documentation](https://documenter.getpostman.com/view/23672162/2sB3B7QaSm)

## 시작 가이드

### 요구 사항

- Java 21
- Gradle 8.1.4+

### 환경 변수 설정

프로젝트를 실행하기 전에 `src/main/resources/application.properties` 파일에 정의된 다음 환경 변수를 설정해야 합니다.

```properties
# Database
DB_URL=jdbc:mysql://<your-db-host>:<port>/<database>
DB_USERNAME=<your-db-username>
DB_PASSWORD=<your-db-password>

# Kafka
KAFKA_BOOTSTRAP_SERVERS=<your-kafka-bootstrap-servers>

# AWS S3
S3_ACCESS_KEY=<your-s3-access-key>
S3_SECRET_KEY=<your-s3-secret-key>
S3_BUCKET=<your-s3-bucket-name>

# OpenAI
OPENAI_API_KEY=<your-openai-api-key>
```

### 빌드 및 실행

1.  **프로젝트 빌드**
    ```shell
    ./gradlew build
    ```

2.  **애플리케이션 실행**
    ```shell
    ./gradlew bootRun
    ```

## 사용 방법

1.  **가이드 문서 주입 (Ingestion)**
    - RAG 모델이 참고할 차량 수리 가이드(.docx)를 `/api/rag/ingest` 엔드포인트를 통해 시스템에 업로드합니다. 이 과정에서 문서는 텍스트로 변환되고 임베딩 벡터로 만들어져 ~~PostgreSQL~~(현재는 테스트용으로 mysql에 저장되고 있으며, 단순 텍스트 검색을 통해 가져오고 있습니다.) 에 저장됩니다.

2.  **감사 생성**
    - `/api/audits` 엔드포인트로 차량 정보와 점검할 이미지 파일들을 전송하여 새로운 감사를 생성합니다.
    - 요청이 성공하면 각 이미지에 대한 점검 작업이 비동기적으로 시작됩니다.

3.  **결과 조회**
    - `/api/audits/{id}` 엔드포인트를 통해 특정 감사의 진행 상태와 AI의 점검 결과를 확인할 수 있습니다.

4.  **RAG 제안 받기**
    - 점검이 필요한 특정 부위에 대해 더 자세한 정보가 필요할 경우, `/api/rag/suggest` 엔드포인트에 이미지와 점검 종류를 전송하여 RAG 모델로부터 수리 가이드를 제안받을 수 있습니다.

## CI/CD 및 배포 (CI/CD & Deployment)

이 프로젝트는 Jenkins, Docker, Kubernetes를 사용하여 CI/CD 파이프라인을 구축하고 배포를 자동화합니다.

### CI/CD 파이프라인 (Jenkins)

`Jenkinsfile`에 정의된 파이프라인은 Git 리포지토리에 새로운 변경 사항이 푸시될 때 자동으로 실행됩니다. 주요 단계는 다음과 같습니다.

1.  **Checkout**: Git 리포지토리에서 최신 소스 코드를 가져옵니다.
2.  **Build**: `./gradlew build` 명령을 실행하여 애플리케이션을 빌드하고 JAR 파일을 생성합니다.
3.  **Build and Push Docker Image**: `Dockerfile`을 사용하여 애플리케이션을 컨테이너화합니다. 빌드된 Docker 이미지는 Jenkins에 설정된 인증 정보를 사용하여 지정된 Docker 레지스트리(`my-docker-registry`)에 푸시됩니다.
4.  **Deploy to K8s**: Kubernetes의 `deployment.yaml` 파일에 정의된 이미지 이름을 새로 빌드한 Docker 이미지로 업데이트한 후, `kubectl apply` 명령을 통해 클러스터에 배포합니다.

### 컨테이너화 (Dockerfile)

`Dockerfile`은 효율적인 애플리케이션 이미지를 생성하기 위해 멀티 스테이지 빌드 전략을 사용합니다.

-   **빌드 단계**: `gradle:8.7.0-jdk21` 이미지를 사용하여 소스 코드를 컴파일하고 의존성을 다운로드합니다.
-   **실행 단계**: 경량화된 `amazoncorretto:21-alpine` 이미지를 기반으로, 빌드 단계에서 생성된 JAR 파일만 복사하여 최종 이미지를 만듭니다. 이를 통해 이미지 크기를 최소화하고 보안을 강화합니다.

### 쿠버네티스 배포 (Kubernetes)

`kubernetes` 디렉토리의 YAML 파일들은 애플리케이션을 쿠버네티스 클러스터에 배포하고 외부에 노출하는 방법을 정의합니다.

-   **`deployment.yaml`**: 애플리케이션의 배포 사양을 정의합니다.
    -   애플리케이션은 단일 복제본(Replica)으로 실행됩니다.
    -   데이터베이스 접속 정보, Kafka 주소, API 키 등 민감한 설정은 `vehicle-audit-secret`이라는 쿠버네티스 시크릿(Secret)을 통해 컨테이너 환경 변수로 주입됩니다. 이는 민감 정보를 코드나 이미지에 직접 포함하지 않기 위한 보안 모범 사례입니다.
-   **`service.yaml`**: 배포된 애플리케이션을 네트워크에 노출하는 서비스를 정의합니다.
    -   `LoadBalancer` 타입으로 설정되어 있어, 클라우드 환경에서 외부 로드 밸런서를 통해 애플리케이션을 인터넷에 자동으로 노출시킵니다.
    -   외부 포트 `80`번으로 들어오는 트래픽을 애플리케이션 컨테이너의 `8080` 포트로 전달합니다.
