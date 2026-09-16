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

---

# 자바웹프로그래밍(2) 3주차 - 포트폴리오 작성하기

TemplateMo 578 First Portfolio 원본 템플릿을 스프링 부트 프로젝트에 넣고, 웹/AI/보안/앱 기술 영역으로 수정했다. 강의 자료에서 안내한 것처럼 상세 페이지는 `resources/public` 폴더에 넣어서 컨트롤러를 새로 만들지 않고 바로 접근하게 했다.

20231028 최민

## 개발 환경

| 항목 | 내용 |
|---|---|
| IDE | Visual Studio Code |
| 빌드 도구 | Maven |
| Spring Boot | 4.1.1 |
| Java | 21 (LTS) |
| 템플릿 엔진 | Thymeleaf |
| 프론트엔드 | HTML, CSS, Bootstrap 5, Bootstrap Icons |
| 정적 페이지 위치 | `src/main/resources/public` |

가져온 `templatemo_578_first_portfolio.zip`에서 `css`, `js`, `images`, `fonts` 폴더를 `static`으로 옮기고 원본 `index.html`을 `templates`로 옮겼다. 제공된 `detailed_web.html` 소스를 기반으로 AI/보안/앱 상세 페이지도 추가했다. 기본 프로필 이미지는 `static/images/profile.png`에 넣고, 오른쪽 히어로 이미지는 `static/images/hero-monkey.png`로 따로 넣었다.

## 추가한 파일

```
src/
 └ main/
    └ resources/
       ├ templates/
       │   └ index.html                  # TemplateMo 원본 기반 포트폴리오 메인 화면
       ├ static/
       │   ├ css/                        # TemplateMo 원본 CSS
       │   ├ js/                         # TemplateMo 원본 JS
       │   ├ fonts/                      # Bootstrap Icons 폰트
       │   └ images/
       │      ├ profile.png              # 프로필 이미지
       │      ├ hero-monkey.png          # 오른쪽 히어로 이미지
       │      └ ...                      # TemplateMo 원본 이미지
       └ public/
          ├ detailed_web.html            # 웹 기술 상세 페이지
          ├ detailed_ai.html             # AI 기술 상세 페이지
          ├ detailed_security.html       # 보안 기술 상세 페이지
          └ detailed_app.html            # 앱 기술 상세 페이지
```

## 실행 방법

```bash
./mvnw spring-boot:run
```

접속 주소는 일곱 개다.

- <http://localhost:8080/> 메인 페이지
- <http://localhost:8080/hello> 2주차 Thymeleaf 예제
- <http://localhost:8080/hello2> 2주차 연습문제
- <http://localhost:8080/detailed_web.html> 3주차 웹 기술 상세 페이지
- <http://localhost:8080/detailed_ai.html> 3주차 AI 기술 상세 페이지
- <http://localhost:8080/detailed_security.html> 3주차 보안 기술 상세 페이지
- <http://localhost:8080/detailed_app.html> 3주차 앱 기술 상세 페이지

## 실습하면서 정리한 것

**포트폴리오 메인** - TemplateMo 578 원본 `index.html`을 기준으로 사용했다. 네비게이션 메뉴는 홈페이지, 소개, 기술, 프로젝트, 연락처로 한글화했고 각 메뉴는 `#section_1`부터 `#section_5`까지 같은 페이지 안에서 이동한다.

**프로필 이미지** - PDF에서 안내한 방식대로 초록 외계인 이미지를 `profile.png`로 준비해서 `static/images` 폴더에 넣었다. 메인 화면의 왼쪽 작은 프로필과 소개 영역 프로필은 `/images/profile.png`를 불러온다. 오른쪽 큰 히어로 이미지는 원숭이 사진인 `/images/hero-monkey.png`를 사용한다.

**기술 영역** - Services 영역을 기술 영역으로 보고 웹, AI, 보안, 앱 네 가지로 구성했다. 각 카드에는 Bootstrap Icons 아이콘과 간단한 설명, 상세 페이지로 이동하는 버튼을 넣었다.

**정적 페이지 위치** - `resources/public` 안에 넣은 파일은 컨트롤러 없이 바로 접근할 수 있다. 그래서 `/detailed_web.html`, `/detailed_ai.html`, `/detailed_security.html`, `/detailed_app.html` 요청은 `DemoController.java`에 `@GetMapping`을 추가하지 않아도 열린다.

**자원 경로** - HTML에서 CSS와 JS 경로가 맞지 않으면 화면은 열려도 디자인이 깨진다. 원본 템플릿의 `css`, `js`, `images`, `fonts`를 `static` 아래에 넣고 `/css/...`, `/js/...`, `/images/...`처럼 스프링 부트 정적 경로로 불러오게 했다.

**새 창 링크 보안** - 메인 페이지에서 상세 페이지를 새 창으로 열 때 `target="_blank"`와 함께 `rel="noopener noreferrer"`를 붙였다. 새 창이 원래 페이지를 조작하지 못하게 막고, 이전 페이지 주소 노출도 줄인다.

**Bootstrap 사용** - 그리드(`container`, `row`, `col-*`)로 화면을 나누고 카드, 버튼, 아이콘 클래스를 조합해서 반응형 상세 페이지를 만들었다. 모바일에서는 열이 자동으로 아래로 내려가서 작은 화면에서도 읽기 좋다.

## 연습문제

가져온 `detailed_web.html`을 `public` 폴더에 넣고, 같은 형식을 재활용해 `detailed_ai.html`, `detailed_security.html`, `detailed_app.html`도 추가했다.

강의 자료의 핵심은 포트폴리오 화면을 스프링 부트 프로젝트 안에 넣고, 웹/AI/보안/앱 같은 기술 영역별 상세 페이지를 구성하는 것이다. 메인 페이지의 각 기술 카드에서 상세 페이지가 새 창으로 열리도록 `target="_blank"`와 `rel="noopener noreferrer"`를 함께 사용했다.

## 성능 확인 - 원인 분석

Lighthouse 보고서를 PC와 모바일 조건으로 각각 생성했다. 결과 파일은 `docs/lighthouse/desktop.json`, `docs/lighthouse/mobile.json`에 저장했다.

| 구분 | Performance | Accessibility | Best Practices | SEO |
|---|---:|---:|---:|---:|
| PC | 96 | 90 | 100 | 91 |
| Mobile | 69 | 92 | 100 | 91 |

모바일 점수가 더 낮은 이유는 Lighthouse 모바일 검사가 느린 네트워크와 낮은 CPU 성능을 가정하기 때문이다. 같은 페이지라도 모바일 조건에서는 이미지 표시와 JavaScript 실행 시간이 더 길게 잡혀 LCP와 TTI가 크게 늘어난다.

가장 성능을 감소시키는 항목 2개는 다음과 같다.

1. **사용하지 않는 CSS** - Bootstrap 전체 CSS, Bootstrap Icons, TemplateMo CSS를 한 번에 불러오므로 실제 화면에서 쓰지 않는 CSS가 많이 남는다. 모바일 기준 약 223KB 절감 가능으로 표시됐다.
2. **사용하지 않는 JavaScript** - Bootstrap JS와 jQuery를 원본 템플릿 그대로 불러오지만 첫 화면에서 전부 필요하지는 않다. 모바일 기준 약 85KB 절감 가능으로 표시됐다.

추가로 favicon 404 오류를 없애고, 프로필 이미지를 압축해서 모바일 LCP를 줄였다. 다만 원본 TemplateMo 모양을 유지해야 하므로 CSS/JS를 잘라내는 최적화는 이번 과제 범위에서는 적용하지 않았다.
