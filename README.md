# 자바웹프로그래밍(2) 2주차 - 스프링 부트 개발환경 설정 및 테스트

VS Code에 스프링 부트 개발환경을 세팅하고 Thymeleaf로 첫 화면을 띄운 뒤 URL 맵핑과 컨트롤러까지 만들어 봤다. 강의 자료 순서를 그대로 따라가며 실습했고 마지막 연습문제도 풀어서 넣었다.

20231028 최민

## 개발 환경

| 항목 | 내용 |
|---|---|
| IDE | Visual Studio Code |
| 확장 | Extension Pack for Java, Spring Boot Extension Pack |
| 빌드 도구 | Maven |
| Spring Boot | 4.1.1 |
| Java | 21 (LTS) |
| 패키징 | Jar |
| 템플릿 엔진 | Thymeleaf 3.1.5 |
| WAS | 내장 Apache Tomcat 11, 8080 포트 |

강의 자료는 Java 25를 권장하지만 최소 요구 버전이 17이라 노트북에 이미 깔려 있던 21로 진행했다.

## 선택한 의존성

Spring Initializr에서 7가지를 골랐다. Spring Web, Thymeleaf, Spring Boot DevTools, Lombok, Spring Configuration Processor까지 다섯 개는 바로 쓰고 있고 Spring Data JPA와 MySQL Driver는 `pom.xml`에서 주석 처리해 뒀다.

DB를 아직 안 붙였는데 JPA가 살아 있으면 실행하자마자 데이터소스를 못 찾아서 죽는다. 강의 자료 17페이지에서 알려준 대로 해당 의존성 세 덩어리를 주석으로 막으니 8080 포트가 정상적으로 떴다. DB 실습에 들어가는 주차에 주석만 풀면 된다.

## 폴더 구조

```
src/
 └ main/
    ├ java/com/example/demo/
    │   ├ DemoApplication.java   # @SpringBootApplication, 프로그램 시작점
    │   └ DemoController.java    # URL 맵핑 컨트롤러
    └ resources/
       ├ templates/              # Thymeleaf 뷰
       │   ├ index.html          # 메인 페이지
       │   ├ hello.html          # /hello
       │   └ hello2.html         # /hello2, 연습문제
       ├ static/                 # css, js, images
       └ application.properties  # 포트, 캐시 설정
pom.xml                          # 빌드 및 종속성 설정
```

## 실행 방법

VS Code에서 `DemoApplication.java`를 열고 디버그 없이 실행(Ctrl + F5)하면 내장 톰캣이 뜬다. 터미널에서 직접 돌려도 된다.

```bash
./mvnw spring-boot:run
```

접속 주소는 셋이다.

- <http://localhost:8080/> 메인 페이지
- <http://localhost:8080/hello> 컨트롤러가 넘긴 값을 출력
- <http://localhost:8080/hello2> 연습문제 페이지

## 실습하면서 정리한 것

**index.html** — `resources/templates` 안에 만들었다. 기존 HTML5 문법을 그대로 쓰되 `<html>` 태그에 `xmlns:th`를 선언해야 Thymeleaf 문법이 먹는다. 처음에 링크를 `/hello.html`로 걸었더니 404가 났다. 파일 이름으로 직접 접근하는 방식이 아니라 컨트롤러가 매핑한 주소로 들어가야 해서였다. `/hello`로 고치니 연결됐다.

**DemoController.java** — `@Controller`를 붙이고 `@GetMapping("/hello")`으로 GET 요청을 받는다. `model.addAttribute("data", ...)`로 값을 담고 `"hello"`를 리턴하면 리턴한 이름과 같은 `hello.html`을 찾아서 렌더링한다. 파일 이름과 리턴값의 대소문자가 어긋나면 바로 에러가 나니 조심해야 한다.

**동작 순서** — 브라우저가 `/hello`를 요청하면 DispatcherServlet이 프론트 컨트롤러 역할로 받아서 컨트롤러에 넘긴다. 컨트롤러는 Model에 데이터를 담고 뷰 이름을 돌려준다. 그러면 Thymeleaf가 서버에서 HTML을 완성해 응답한다. MVC로 보면 Model은 페이지 정보, View는 Thymeleaf 화면, Controller는 흐름 제어를 맡는다.

## 연습문제

`/hello2` 맵핑을 새로 추가하고 속성 다섯 개를 출력했다.

컨트롤러의 `hello2()` 메서드에서 학과, 학번, 이름, 과목, 주차를 Model에 담고 `hello2.html`에서 `th:text="${...}"`로 하나씩 찍었다. `hello.html`의 링크도 `/hello2`로 바꿔서 메인에서 두 번째 페이지까지 클릭만으로 넘어간다.

실행해서 다섯 값이 전부 화면에 나오는지 확인했고 `./mvnw test`도 통과했다.

### 실습 결과

`/hello`의 링크를 눌러 `/hello2`로 넘어가는 흐름이다. 오른쪽 화면에 컨트롤러가 담은 다섯 속성이 그대로 찍혔다.

| `/hello` | `/hello2` |
|---|---|
| ![/hello 실행 결과](docs/images/hello-result.png) | ![/hello2 실행 결과](docs/images/hello2-result.png) |
