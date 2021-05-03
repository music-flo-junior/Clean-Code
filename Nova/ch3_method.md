# 3장. 함수

## 이해하기 어려운 함수

##### HtmlUtil.java

<details>
<summary>HtmlUtil.java</summary>
<div markdown="1">

```java
public static String testableHtml (
    PageData pageData,
    boolean includeSuiteSetup
) throws Exception {
    WikiPage wikiPage = pageData.getWikiPage();
    StringBuffer buffer = new StringBuffer();
    if (pageData.hasAttribute("Test")) {
        if (includeSuiteSetup) {
            WikiPage suiteSetup =
                PageCrawlerImpl.getInheritedPage(
                        SuiteResponder.SUITE_SETUP_NAME, wikiPage
                );
            if (suiteSetup != null) {
                WikiPagePath pagePath  = suiteSetup.getPageCrawler().getFullPath(suiteSetup);
                String pagePathName = PathParser.render(pagePath);
                buffer.append("!include -setup .")
                    .append(pagePathName)
                    .append("\n");
            }
        }
        WikiPage setup = PageCrawlerImpl.getInheritedPage("SetUp", wikiPage);
        if (setup != null) {
            WikiPagePath setupPath = wikiPage.getPageCrawler().getFullPath(setup);
            String setupPathName = PathParser.render(setupPath);
            buffer.append("!include -setup .")
                .append(setupPathName)
                .append("\n");
        }
    }
    buffer.append(pageData.getContent());
    if (pageData.hasAttribute("Test")) {
        WikiPage teardown = PageCrawlerImpl.getInheritedPage("TearDown", wikiPage);
        if (teardown != null) {
            WikiPagePath tearDownPath = wikiPage.getPageCrawler().getFullPath(teardown);
            String tearDownPathName = PathParser.render(tearDownPath);
            buffer.append("\n")
                .append("!include -teardown .")
                .append(tearDownPathName)
                .append("\n");
        }
        if (includeSuiteSetup) {
            WikiPage suiteTeardown = PageCrawlerImpl.getInheritedPage(SuiteResponder.SUITE_TEARDOWN_NAME, wikiPage);
            if (suiteTeardown != null) {
                WikiPagePath pagePath = suiteTeardown.getPageCrawler().getFullPath (suiteTeardown);
                String pagePathName = PathParser.render(pagePath);
                buffer.append("!include -teardown .")
                    .append(pagePathName)
                    .append("\n");
            }
        }
    }
    pageData.setContent(buffer.toString());
    return pageData.getHtml();
}
```

위 코드는 추상화 수준도 너무 다양하고, 코드도 너무 길다. 두 겹으로 중첩된 if문은 이상한 플래그를 확인하고, 이상한 문자열을 사용하며, 이상한 함수를 호출한다.

너무 이해하기 어렵다!!

위 코드를 리팩터링해보자.

##### HtmlUtil.java (리팩터링한 버전)
```java
public static String renderPageWithSetupsAndTeardowns (
    PageData pageData, boolean isSuite
) throws Exception {
    boolean isTestPage = pageData.hasAttribute("Test");
    if (isTestPage) {
        WikiPage testPage = pageData.getWikiPage();
        StringBuffer newPageContent = new StringBuffer();
        includeSetupPages(testPage, newPageContent, isSuite);
        pageData.setContent(newPageContent.toString());
    }
    return pageData.getHtml();
}
```

위 코드를 보고 100% 이해하지는 못해도, 함수가 설정(setup) 페이지와 해제(teardown) 페이지를 테스트 페이지에 넣은 후 해당 테스트 페이지를 HTML로 렌더링한다는 사실은 짐작할 수 있다.

위 함수가 이해하기 쉬운 이유가 무엇일까? 의도를 분명히 표현하는 함수를 어떻게 구현할 수 있을까? 함수에 어떤 속성을 부여해야 처음 읽는 사람이 프로그램 내부를 직관적으로 파악할 수 있을까?
</dev>
</details>

## 함수를 읽기 쉽고 이해하기 쉽게 짜는 방법

### 1. 작게 만들어라
- 함수는 짧고 간결할수록 좋다.
<details>
<summary>HtmlUtil.java re-refactoring</summary>
<div markdown="1">

```java
public static String renderPageWithSetupsAndTeardowns(
    PageData pageData, boolean isSuite) throws Exception {
        if (isTestPage(pageData))
            includeSetupAndTeardownPages(pageData, isSuite);
        return pageData.getHtml();
    }
)
```

HtmlUtils를 리팩토링한 코드도 위와 같이 더 간결하게 만들 수 있다!

</div>
</details>

#### 1.1 함수의 길이와 가로 글자 수
- 요즘은 한 화면에 글꼴을 조절하면 가로 150자 세로 100줄도 들어간다. 따라서 가로 150자를 넘어서는 안된다. (반드시 한 화면에 들어가야 한다.)
- 함수는 100줄을 넘어서는 안된다. 아니, 20줄도 길다!

#### 1.2 블록과 들여쓰기
- `If 문 / else 문 / while문에 들어가는 블록은 한 줄이어야 한다.`
- 블록 안에서 호출하는 함수 이름을 적절히 짓는다면, 코드를 이해하기도 쉬워진다.
- `중첩 구조가 생길만큼 함수가 커져서는 안된다.` 그러므로 함수에서 들여쓰기 수준은 1단이나 2단을 넘어서면 안된다. 그래야 함수는 읽고 이해하기 쉬워진다.

### 2. 한 가지만 해라
- `함수는 한 가지를 해야 한다. 그 한 가지를 잘 해야 한다. 그 한 가지만을 해야 한다.`
- 어떻게 함수가 한 가지만 하고 있는지 판단할 수 있을까?
    - 단순히 다른 표현이 아니라 의미 있는 이름으로 다른 함수를 추출할 수 있다면 그 함수는 여러 작업을 하는 셈이다.
    - 한 가지 작업만 하는 함수는 자연스럽게 여러 섹션으로 나누기 어렵다.

### 3. 함수 당 추상화 수준은 하나로
- 함수가 확실히 '한 가지' 작업만 하려면 함수 내 모든 문장의 추상화 수준이 동일해야 한다.
- 한 함수 내 에 추상화 수준을 섞으면 코드를 읽는 사람이 헷갈린다. 특정 표현이 근본 개념인지 아니면 세부 사항인지 구분하기 어려운 탓이다.
- 근본 개념과 세부 사항을 뒤섞기 시작하면, 깨어진 창문처럼 사람들이 함수에 세부사항을 점점 더 추가한다.

#### 위에서 아래로 코드 읽기: 내려가기 규칙
- 코드는 위에서 아래로 이야기처럼 읽혀야 좋다. 한 함수 다음에는 추상화 수준이 한 단계 낮은 함수가 온다. 즉, 위에서 아래로 프로그램을 읽으면 함수 추상화 수준이 한 번에 한 단계씩 낮아진다.

### 4. Switch 문
- Switch 문은 작게 만들기 어렵다.
  - case 분기가 단 두 개인 switch 문도 길고, 단일 블록이나 함수가 좋다.(작가의 취향) 
  - 또한 '한 가지' 작업만 하는 switch 문도 만들기 어렵다. 본질적으로 switch 문은 N가지를 처리하기 때문이다.
  - 각 switch 문을 숨기고 절대로 반복하지 않는 방법은 있다. 물론 다형성을 이용한다.
    ##### 직원 유형에 따라 다른 값을 계산해 반환하는 함수
    ```java
        public Money calculatePay(Employee e)
        throws InvalidEmployeeType {
            switch (e.type) {
                case COMMISSIONED:
                    return calculateCommissionedPay(e);
                case HOURLY:
                    return calculateHourlyPay(e);
                case SALARIED:
                    return calculateSalariedPay(e);
                default:
                    throw new InvalidEmployeeType(e.type);
            }
        }
    ```
    위 함수는 `함수가 길고`, `한 가지 작업만 수행하지 않고`, `SRP(Single Responsibility Principle)를 위반`하고, `OCP(Open Closeed Principle)`를 위반한다는 문제가 있다.
    즉, 새로운 직원 유형을 추가할 때마다 코드를 변경해야 하고 구조가 동일한 함수가 무한정 존재할 수 있다는 문제가 있다. (ex. isPayday(Employee e, Date date), deliverPay(Employee e, Mone pay) 등)
    이를 해결하기 위해서는 switch 문을 추상 팩토리에 꽁꽁 숨긴다. 팩토리는 switch 문을 사용해 적절한 Employee 파생 클래스의 인스턴스를 생성한다. calculatePay, isPayday, deliverPay 등과 같은 함수는 Employee 인터페이스를 거쳐 호출된다.

    ##### Employee and Factory with switch 문
    ```java
    public abstract class Employee {
        public abstract boolean isPayday();
        public abstract Money calculatePay();
        public abstract void deliverPay(Money pay);
    }
    ```
    ```java
    public interface EmployeeFactory {
        public Employee makeEmployee(EmployeeRecord r) throws InvalidEmployeeType;
    }
    ```
    ```java
    public class EmployeeFactoryImpl {
        public Employee makeEmployee(EmployeeRecord r) throws InvalidEmployeeType {
            switch (r.type) {
                case COMMISSIONED:
                    return new CommissionedEmployee(r);
                case HOURLY:
                    return new HourlyEmployee(r);
                case SALARIED:
                    return new SalariedEmployee(r);
                default:
                    throw new InvalidEmployeeType(r.type);
            }
        }
    }
    ```

### 5. 서술적인 이름을 사용하라
- ex) `testableHtml` -> `SetupTeardownIncluder.render`
    - 함수가 하는 일을 더 잘 표현하는 것이 훨씬 좋은 이름이다.
- 코드를 읽으면서 짐작했던 기능을 각 루틴이 그대로 수행한다면 깨끗한 코드라 불러도 되겠다.
- 길고 서술적인 이름이 짧고 어려운 이름보다 좋다. 
- 길고 서술적인 이름이 길고 서술적인 주석보다 좋다.
- 이름을 붙일 때는 일관성이 있어야 한다. 모듈 내에서 함수 이름은 같은 문구, 명사, 동사를 사용한다.
  - 예) includeSetupAndTeardownPages, includeSetupPages, includeSuiteSetupPage, includeSetupPage 등
  
### 6. 함수 인수
- 함수에서 이상적인 인수 개수는 0개 (무항)이다. 다음은 1개(단항), 다음은 2개(이항)다. 3개(삼항)은 가능한 피하는 편이 좋다. 4개 이상(다항)은 특별한 이유가 필요하다. 특별한 이유가 있어도 사용하면 안된다.
- 테스트 관점에서 보면 인수는 더 어렵다. 갖가지 인수 조합으로 함수를 검증하는 테스트 케이스를 작성한다고 했을 때, 인수가 없으면 간단하지만 인수가 2개, 3개 넘어가면 인수마다 유효한 값으로 모든 조합을 작성해야하므로 테스트하기 상당히 부담스러워 진다.

#### 6.1 많이 쓰는 단항 형식
- 함수에 인수 1개를 넘기는 이유로 가장 흔한 경우는 두 가지다.
  - 1. 인수에 질문을 던지는 경우
    - 예) `boolean fileExists("MyFile")`
  - 2. 인수를 뭔가로 변환해 결과를 반환하는 경우
    - 예) `InputStream fileOpen("Myfile")`
- 함수에 인수 1개를 넘기지만 출력 인수가 없는 경우 (이벤트)
  - `passwordAttemptFailedNtimes(int attempts)`
  - 이런 이벤트 성이 아닌 경우 단항 함수인데 반환 값이 없는 경우는 가급적 피한다.

#### 6.2 플래그 인수
- 플래그 인수를 넘기는 관례는 함수가 한꺼번에 여러 가지를 처리한다고 대놓고 얘기하는 거라 좋지 않다.

#### 6.3 이항 함수
- 인수가 2개인 함수는 인수가 1개인 함수보다 이해하기 어렵다.
- 물론 이항 함수가 적절한 경우도 있다. 예) `Point p = new Point(0, 0)`
  - 즉, 인수 2개가 한 값을 표현하는 두 요소일 경우 적합하다.
- 이항 함수가 무조건 나쁘다는 소리는 아니다. 불가피한 경우도 생기지만 그만큼 위험이 따른다는 사실을 이해하고 가능하면 단항함수로 바꾸도록 애써야 한다.

#### 6.4 삼항 함수
- 인수가 3개인 함수는 인수가 2개인 함수보다 훨씬 더 이해하기 어렵다.
- 순서, 주춤하는 경우, 무시로 야기되는 문제가 두 배 이상 늘어난다.

#### 6.5 인수 객체
- 인수가 2-3개 필요하다면 일부를 독자적인 클래스 변수로 선언할 가능성을 짚어본다.
- 예
  - `Circle makeCircle(double x, double y, double radius);`
  - `Circle makeCircle(Point center, double radius);`

#### 6.6. 인수 목록
- 때로는 인수 개수가 가변적인 함수도 필요하다. String.format 메서드가 좋은 예다.
  - `String.format("%s worked %.2f hours.", name, hours);`

#### 6.7 동사와 키워드
- 함수의 의도나 인수의 순서와 의도를 제대로 표현하려면 좋은 함수 이름이 필수다.
- 단항 함수는 함수와 인수가 동사/명사 쌍을 이뤄야 한다.
  - 예) `write(name)`, `writeField(name)`
- 함수 이름에 키워드를 추가하는 형식 (함수 이름에 인수 이름을 넣는다.)
  - 예) `assertEquals` 보다 `assertExpectedEqualsActual(expected, actual)`이 더 좋다. (인수 순서를 기억할 필요가 없어진다.)

### 7. 부수 효과를 일으키지 마라
- 부수 효과는 거짓말이다.
- 함수에서 한 가지를 하겠다고 약속하고선 남몰래 다른 짓을 하는 것은 해로운 거짓말이다.
  - 함수로 넘어온 인수나 시스템 전역 변수를 수정하는 등
  - 많은 경우 시간적인 결합(temporal coupling)이나 순서 종속성(order dependency)를 초래한다.

##### UserValidator.java
```java
public class UserValidator {
    private Cryptographer cryptographer;

    public boolean checkPassword(String userName, String password) {
        User user = UserGateway.findByName(userName);
        if (user != User.NULL) {
            String codedPhrase = user.getPhraseEncodedByPassword();
            String phrase = cryptographer.decrypt(codedPhrase, password);
            if ("Valid Password".equals(phrase)) {
                Session.initialize();
                return true;
            }
        }
        return false;
    }
}
```
여기서 함수가 일으키는 부수 효과는 `Session.initialize()` 호출이다. 이 함수를 호출하는 사용자는 사용자를 인증하면서 기존 세션 정보를 지워버릴 위험에 처한다.
이런 부수효과가 시간적인 결합을 초래한다. (즉, 세션을 초기화해도 괜찮은 경우에만 호출이 가능하다.)
따라서 `checkPassword` 메서드는 `checkPasswordAndInitializeSession`이라는 이름이 훨씬 좋다.

#### 출력 인수
- 인수를 출력으로 사용하는 함수
  - 예) `appendFooter(s);` : 인수 s에 바닥글을 첨부한다. (return void)
- 객체 지향 언어에서는 출력 인수를 사용할 필요가 거의 없다. 출력 인수로 사용하라고 설계된 변수가 `this`이기 때문이다. 다시 말해, 위 함수는 `report.appendFooter()`로 호출하는 방식이 더 좋다.
- 일반적으로 출력 인수를 피해야 한다. 함수에서 상태를 변경해야 한다면 함수가 속한 객체 상태를 변경하는 방식을 택한다.

### 8. 명령과 조회를 분리하라
- 함수는 뭔가를 수행하거나 뭔가에 답하거나 둘 중 하나만 해야 한다.
- 객체 상태를 변경하거나 아니면 객체 정보를 반환하거나 둘 중 하나다. 둘 다 하면 혼란을 초래한다.
  - 예) `public boolean set(String attribute, String value)`: 이름이 attribute인 속성을 찾아 값을 value로 설정한 후 성공하면 true를 반환하고 실패하면 false를 반환한다.
    - 실제로 함수를 사용할 때 `if (set("username", "unclebob"))...` 와 같은 괴상한 형식으로 사용한다. 마치 if문과 함께 읽혀서 username 속성이 unclebob으로 설정되어 있다면 이라는 내용으로 해석된다.
    - 아얘 명령와 조회를 분리하는 것이 좋다.
    ```java
    if (attributeExists("username")) {
        setAttribute("username", "unclebob");
    }
    ```

### 9. 오류 코드보다 예외를 사용하라
- 명령 함수에서 오류 코드를 반환하는 방식은 명령/조회 분리 규칙을 미묘하게 위반한다. 자칫하면 if문에서 명령을 표현식으로 사용하기 쉬운 탓이다.
- 예) if (deletePage(page) == E_OK)`
  - 위 코드는 동사/형용사 혼란을 일으키지 않는 대신 여러 단계로 중첩되는 코드를 야기한다. 오류코드를 반환하면 호출자는 오류 코드를 곧바로 처리해야 한다는 문제에 부딪힌다.
  - 따라서 아래와 같이 오류 코드 대신 예외를 사용하면 오류 처리 코드가 원래 코드에서 분리되므로 코드가 깔끔해진다.
  ```java
  try {
      deletePage(page);
      registry.deleteReference(page.name);
      configKeys.deleteKey(page.name.makeKey());
  } catch (Exception e) {
      logger.log(e.getMessage());
  }
  ```

#### try/catch 블록 뽑아내기
- try/catch 블록은 코드 구조에 혼란을 일으키며, 정상 동작과 오류 처리 동작을 뒤섞는다. 그러므로 try/catch 블록을 별도 함수로 뽑아내는 편이 좋다.

```java
public void delete(Page page) {
    try {
        deletePageAndAllReferences(page);
    } catch (Exception e) {
        logError(e);
    }
}

private void deletePageAndAllReferences(Page page) throws Exception {
    deletePage(page);
    registry.deleteReference(page.name);
    configKeys.deleteKey(page.name.makeKey());
}

private void logError(Exception e) {
    logger.log(e.getMessage());
}
```

#### 오류 처리도 한 가지 작업이다.
- 함수는 '한 가지' 작업만 해야 한다. 오류 처리도 '한 가지' 작업에 속한다. 그러므로 오류를 처리하는 함수는 오류만 처리해야 마땅하다. (위 예제에서 보았듯이)
- 함수에 키워드 try가 있다면 함수는 try 문으로 시작해 catch/finally 문으로 끝나야 한다.
  
#### Error.java 의존성 자석
- 오류 코드를 반환한다는 이야기는, 클래스든 열거형 변수든, 어디선가 오류 코드를 정의한다는 뜻이다.

```java
public enum Error {
    OK,
    INVALID,
    NO_SUCH,
    LOCKED,
    OUT_OF_RESOURCES,
    WAITING_FOR_EVENT;
}
```
따라서, 오류 코드가 바뀌면 참조하고 있는 다른 클래스 전부를 다시 컴파일하고 다시 배치해야 한다. 그래서 새 오류 코드를 추가하는 대신 기존 오류 코드를 재사용한다.
- 오류 코드 대신 예외를 사용하면 새 예외는 Exception 클래스에서 파생된다. 따라서 재컴파일/재배치 없이도 새 예외 클래스를 추가할 수 있다.

### 10. 반복하지 마라
- 어쩌면 중복은 소프트웨어에서 모든 악의 근원이다. 많은 원칙과 기법이 중복을 없애거나 제어할 목적으로 나왔다.
- 예를 들어, 중복을 제거할 목적으로 관계형 데이터베이스에 정규 형식도 있고, 객체 지향 프로그래밍은 코드를 부모 클래스로 몰아 중복을 없앤다. 구조적 프로그래밍, AOP(Aspect Oriented Programming), COP(Component Oriented Programming) 모두 어떤 면에서 중복 제거 전략이다.

### 11. 구조적 프로그래밍
- 구조적 프로그래밍: 모든 함수와 함수 내 모든 블록에 입구와 출구가 하나만 존재해야 한다. 즉, 함수의 return 문이 하나여야 한다. 루프 안에서 break이나 continue를 사용해선 안되며 goto는 절대로, 절대로 안된다.
- 함수가 작을 때는 간혹 return, break, continue를 여러 차례 사용해도 괜찮다. 오히려 때로는 단일 입/출구 규칙보다 의도를 표현하기 쉬워진다.

### 12. 함수를 어떻게 짜죠?
- 함수는 처음에는 길고 복잡하고, 들여쓰기 단계도 많고 중복된 루프도 많고 인수 목록도 아주 길 수 있다. 이름은 즉흥적이고 코드는 중복된다. 그러나 이 서투른 코드를 빠짐없이 테스트하는 단위 테스트 케이스를 만든다.
- 그러면 코드를 다듬고, 함수를 만들고, 이름을 바꾸고, 중복을 제거해도 단위 테스트를 통과하기 때문에 부담이 없다.
- 최종적으로 이 장에서 설명한 규칙을 따르는 함수가 얻어진다. 처음부터 딱 만드는 사람은 없으리라.

## 결론

## 예시 코드

<details>
<summery>SetupTeardownIncluder.java</summery>
<div markdown="1">

```java
import fitnesse.responders.run.SuiteResponder;
import fitnesse.wiki.*;

public class SetupTeardownIncluder {
    private PageData pageData;
    private boolean isSuite;
    private WikiPage testPage;
    private StringBuffer newPageContent;
    private PageCrawler pageCrawler;

    public static String render(PageData pageData) throws Exception {
        return render(pageData, false);
    }

    public static String render(PageData pageData, boolean isSuite) throws Exception {
        return new SetupTeardownIncluder(pageData).render(isSuite);
    }

    private SetupTeardownIncluder(PageData pageData) {
        this.pageData = pageData;
        testPage = pageData.getWikiPage();
        pageCrawler = testPage.getPageCrawler();
        newPageContent = new StringBuffer();
    }

    private String render(boolean isSuite) throws Exception {
        this.isSuite = isSuite;
        if (isTestPage())
        includeSetupAndTeardownPage();
        return pageData.getHtml();
    }

    private boolean isTestPage() throws Exception {
        return pageData.hasAttribute("Test");
    }
    
    private void includeSetupAndTeardownPage() throws Exception {
        includeSetupPage();
        includePageContent();
        includeTeardownPages();
        updatePageContent();
    }

    private void includeSetupPage() throws exception {
        if (isSuite)
            includeSuiteSetupPage();
        includeSetupPage();
    }

    private void includeSuiteSetupPage() throws Exception {
        include(SuiteResponder.SUITE_SETUP_NAME, "-setup");
    }

    private void includeSetupPage() throws Exception {
        include("SetUp", "-setup");
    }

    private void includePageContent() throws Exception {
        newPageContent.append(pageData.getContent());
    }

    private void includeTeardownPages() throws Exception {
        includeTeardownPage();
        if (isSuite)
            includeSuiteTeardownPage();
    }

    private void includeTeardownPage() throws Exception {
        include("TearDown", "-teardown");
    }

    private void includeSuiteTeardownPage() throws Exception {
        include(SuiteREsponder.SUITE_TEARDOWN_NAME, "-teardown");
    }

    private void updatePageContent() throws Exception {
        pageData.setContent(newPageContent.toString());
    }

    private void include(String pageName, String arg) throws Exception {
        WikiPage inheritedPage = findInheritedPage(pageName);
        if (inheritedPage != null) {
            String pagePathName = gePathNameForPage(inheritedPage);
            buildIncludeDirective(pagePathName, arg);
        }
    }

    private WikiPage findInheritedPage(String pageName) throws Exception {
        WikiPagePath pagePath = pageCrawler.getFullPath(page);
        return PathParser.render(pagePath);
    }

    private void buildIncludeDirective(String pagePathName, String arg) {
        newPageContent
            .append("\n!include ")
            .append(arg)
            .append(" .")
            .append(pagePathName)
            .append("\n");
    }
}
```
</div>
</details>

### 리팩토링 전 upsertChannel

```java
/**
* 채널 수정 - 2021.02.10 현재 채널 생성 로직 분리로 인해 update 만을 처리함
*
* @param updateChannelParam
* @return
* @throws Exception
*/
public boolean upsertChannel(ContentsUpdateChannelParam updateChannelParam) throws Exception {

    ContentsSearchTrackParam searchTrackParam = new ContentsSearchTrackParam();
    searchTrackParam.setPaging(false);
    searchTrackParam.setTrackIdList(updateChannelParam.getTrackIdList());

    updateChannelParam.setChnlPlayTm(searchTrackParam.getTrackIdList().size() > 0 ? this.getPlayTm(channelMapper.selectTrackList(searchTrackParam)) : "0");

    if (null == updateChannelParam.getChnlId()) {
        // 채널 등록
        LocalDateTime renewDate = LocalDateTime.now();

        updateChannelParam.setRenewDtime(renewDate);
        updateChannelParam.setRenewTrackCnt(0);

        channelMapper.insertChannel(updateChannelParam);
        this.insertChannelMultiLink(updateChannelParam);

        if (ChnlType.AFLO == updateChannelParam.getChnlType()) {
            channelMapper.insertAfloChnl(updateChannelParam);
        }

        if (ChnlType.AFLO != updateChannelParam.getChnlType() && !ObjectUtils.isEmpty(updateChannelParam.getOrgGenreId())) {
            // 채널 장르 맵핑 데이터 추가
            channelMapper.deleteMapSvcGenreChannel(updateChannelParam);
            // 완료
            channelMapper.insertMapSvcGenreChannel(updateChannelParam);
        }

        // 채널 트랙 등록
        insertChannelTrack(updateChannelParam);
    } else {
        updateChannelParam.setRenewTrackCnt(0);

        //채널 수정
        channelMapper.updateChannel(updateChannelParam);
        this.updateChannelMultiLink(updateChannelParam);

        if (ChnlType.AFLO != updateChannelParam.getChnlType() && !ObjectUtils.isEmpty(updateChannelParam.getOrgGenreId())) {
            // 채널 장르 맵핑 데이터 추가
            channelMapper.deleteMapSvcGenreChannel(updateChannelParam);
            channelMapper.insertMapSvcGenreChannel(updateChannelParam);
        } else {
            channelMapper.deleteMapSvcGenreChannel(updateChannelParam);
        }

        //채널 트랙 수정
        updateChannelTrack(updateChannelParam);
    }

    // 이미지 처리
    updateChannelImg(updateChannelParam);

    // 유효한 트랙 갯수로 업데이트
    channelMapper.updateChannelTrackCnt(updateChannelParam.getChnlId());

    //채널 타이틀곡 업데이트
    Long titleTrackId = updateChannelParam.getTitleTrackId();
    if (updateChannelParam.getTrackIdList().size() > 0) {
        if (titleTrackId == null) {
            //첫번째 trackId 로
            titleTrackId = updateChannelParam.getTrackIdList().get(0);
        }
        channelMapper.updateInitChannelTitleYn(updateChannelParam.getChnlId());
        channelMapper.updateChannelTitleYn(updateChannelParam.getChnlId(), titleTrackId);
    }

    // 이력 생성
    insertChannelHistory(updateChannelParam);

    // 레디스 캐시 삭제
    String channelRedisKey = String.format(godmusicChanneRedislKeyFormat, updateChannelParam.getChnlId());
    redisService.delWithPrefix(channelRedisKey); // 체널 캐시 삭제

    for (String key : godmusicRecommendRedislKey) { // 추천 캐시 삭제
        redisService.delWithPrefix(key);
    }

    return true;
}
```

### 리팩토링

```java
/**
* 채널 수정 서비스
*
* @param updateChannelParam
* @return
* @throws Exception
*/
public boolean updateChannel(ContentsUpdateChannelParam updateChannelParam) throws Exception {
    beforeUpdate(updateChannelParam);
    channelMapper.updateChannel(updateChannelParam);
    afterUpdate(updateChannelParam);
    return true;
}

public void beforeUpdate(ContentsUpdateChannelParam updateChannelParam) {
    ContentsSearchTrackParam searchTrackParam = new ContentsSearchTrackParam();
    searchTrackParam.setPaging(false);
    searchTrackParam.setTrackIdList(updateChannelParam.getTrackIdList());

    updateChannelParam.setChnlPlayTm(searchTrackParam.getTrackIdList().size() > 0 ? this.getPlayTm(channelMapper.selectTrackList(searchTrackParam)) : "0");

    if (Objects.isNull(updateChannelParam.getChnlId())) throw new FloAdminException(CommonErrorDomain.BAD_REQUEST);
    updateChannelParam.setRenewTrackCnt(0);

}

public void afterUpdate(ContentsUpdateChannelParam updateChannelParam) throws ParseException {
    updateChannelMultiLink(updateChannelParam);
    updateMapSvcGenreChannel(updateChannelParam);
    updateChannelTrack(updateChannelParam);
    updateChannelImg(updateChannelParam);
    updateValidChannelTrackCnt(updateChannelParam);
    updateChannelTitle(updateChannelParam);
    insertChannelHistory(updateChannelParam);
    deleteRedisCache(updateChannelParam);
}

/**
* 채널 장르 매핑 데이터 추가
* AFLO 타입일 경우 장르 값 삭제
*
* @param updateChannelParam
*/
private void updateMapSvcGenreChannel(ContentsUpdateChannelParam updateChannelParam) {
    if (ChnlType.AFLO != updateChannelParam.getChnlType() && !ObjectUtils.isEmpty(updateChannelParam.getOrgGenreId())) {
        // 채널 장르 맵핑 데이터 추가
        channelMapper.deleteMapSvcGenreChannel(updateChannelParam);
        channelMapper.insertMapSvcGenreChannel(updateChannelParam);
    } else {
        channelMapper.deleteMapSvcGenreChannel(updateChannelParam);
    }
}

/**
* 유효한 채널 트랙 갯수로 업데이트
* @param updateChannelParam
*/
private void updateValidChannelTrackCnt(ContentsUpdateChannelParam updateChannelParam) {
    channelMapper.updateChannelTrackCnt(updateChannelParam.getChnlId());
}

/**
* 업데이트 후 레디스 캐시 삭제
* @param updateChannelParam
*/
private void deleteRedisCache(ContentsUpdateChannelParam updateChannelParam){
    // 레디스 캐시 삭제
    String channelRedisKey = String.format(godmusicChanneRedislKeyFormat, updateChannelParam.getChnlId());
    redisService.delWithPrefix(channelRedisKey); // 체널 캐시 삭제

    for (String key : godmusicRecommendRedislKey) { // 추천 캐시 삭제
        redisService.delWithPrefix(key);
    }
}

/**
* 채널 타이틀곡 업데이트
* titleTrackId가 없을 경우 첫 번째 트랙 아이디로 설정
*
* @param updateChannelParam
*/
private void updateChannelTitle(ContentsUpdateChannelParam updateChannelParam){
    Long titleTrackId = updateChannelParam.getTitleTrackId();
    if (updateChannelParam.getTrackIdList().size() > 0) {
        if (titleTrackId == null) {
            titleTrackId = updateChannelParam.getTrackIdList().get(0);
        }
        channelMapper.updateInitChannelTitleYn(updateChannelParam.getChnlId());
        channelMapper.updateChannelTitleYn(updateChannelParam.getChnlId(), titleTrackId);
    }
}

/**
* 채널 이미지 업데이트
* @param updateChannelParam
*/
private void updateChannelImg(ContentsUpdateChannelParam updateChannelParam) {
    if (ChnlType.AFLO.getCode().equals(updateChannelParam.getChnlType().getCode())) {
        Long chnlId = updateChannelParam.getChnlId();
        if (!CollectionUtils.isEmpty(updateChannelParam.getChnlImages())) {
            channelMapper.deleteChnlImage(updateChannelParam.getChnlId());
            for (ContentsChnlImage chnlImage : updateChannelParam.getChnlImages()) {
                chnlImage.setChnlId(updateChannelParam.getChnlId());
                chnlImage.setCreateUserNo(updateChannelParam.getCreateUserNo());
                channelMapper.insertChnlImage(chnlImage);
            }
        } else {
            channelMapper.deleteChnlImage(updateChannelParam.getChnlId());
        }
        channelMapper.updateAfloChnl(chnlId, updateChannelParam.getAfloArtistId());
    } else {
        if (null != updateChannelParam.getImgUrl() && !("").equals(updateChannelParam.getImgUrl())) {
            channelMapper.deleteChnlImage(updateChannelParam.getChnlId());
            channelMapper.insertChnlImage(ContentsChnlImage.builder()
                    .chnlId(updateChannelParam.getChnlId()).imgUrl(updateChannelParam.getImgUrl()).chnlImgType(ChnlImgType.PANNEL)
                    .createUserNo(updateChannelParam.getCreateUserNo()).build());
        } else {
            channelMapper.deleteChnlImage(updateChannelParam.getChnlId());
        }
    }
}

/**
* 채널 히스토리 이력 생성
* @param updateChannelParam
*/
private void insertChannelHistory(ContentsUpdateChannelParam updateChannelParam) {

    ContentsChannelHistory channelHistory = new ContentsChannelHistory();
    channelHistory.setChnlChgType("TRACK");
    channelHistory.setChnlId(updateChannelParam.getChnlId());
    channelHistory.setCreateUserNo(updateChannelParam.getUpdateUserNo());
    channelHistory.setCreateDtime(updateChannelParam.getCreateDtime());

    channelMapper.insertChannelHistory(channelHistory);
    channelMapper.insertChannelTrackHistory(channelHistory);

}
```



### 참고 자료
- Clean Code
