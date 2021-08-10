# JUnit 들여다보기

JUnit은 자바 프레임워크 중에서 가장 유명하다.
 
개념은 단순하고 정의는 정밀하고 구현은 우아하다. 

하지만 실제 코드는 어떨까? 이 장에서는 JUnit 프레임 워크에서 가져온 코드를 평가한다.

#### JUnit 프레임워크

JUnit은 저자가 많다. 지만 시작은 켄트벡과 에릭 감마, 두 사람이다. 하

두 사람이 함께 아틀란타 행 비행기를 타고 가다 JUnit을 만들었다. 

켄트는 자바를 배우고 싶었고 에릭은 켄트의 스몰토크 테스트 프레임워크를 배우고 싶었다. 

"비좁은 기내에서 엔지니어 둘이 랩탑을 꺼내 코드를 짜는 일 밖에는 다른 무엇을 하겠는가?" 공중에서 세 시간 정도 일한 끝에 두 사람은 JUnit 기초를 구현했다.

> JUnit 이란?    

Java의 단위테스트를 수행해주는 대표적인 Testing Framework로써 JUnit4와 그 다음 버전인 JUnit5를 대게 사용한다. 

JUnit5는 다음과 같은 구조를 가지고 있다.

#### JUnit5 = JUnit Platform + JUnit Jupiter + JUnit Vintage

1. JUnit Platform : 테스트를 발견하고 테스트 계획을 생성하는 TestEngine 인터페이스를 가지고 있다. 
Platform은 TestEngine을 통해서 테스트를 발견하고 실행하고 결과를 보고한다.

2. JUnit Juptier : TestEngine의 실제 구현체는 별도 모듈인데 그중 하나.
JUnit5에 새롭게 추가된 Jupiter API를 사용하여 테스트 코드를 발견하고 실행한다.

3. JUnit Vintage : 기존에 JUnit4 버전으로 작성한 테스트 코드를 실행할때 vintage-engine 모듈을 사용한다.

> spring-boot-starter-test를 사용하여 JUnit5 환경을 구성하여 보았다.

spring-boot-starter-test는 다음 라이브러리들을 포함하고 있다.

- JUnit5 + JUnit4의 호환성을 위한 vintage-engine
- Spring test & SpringBoot Test
- AssertJ : Assertion을 제공하는 Java 라이브러리. 대표적으로 assertEquals와 같은 메소드를 사용하여 테스트.
- Hamcrest : Java Matcher 라이브러리. 대표적으로 assertThat과 같은 메소드를 사용하여 테스트.
- Mockito : 가짜 객체인 Mock을 사용하게 해주는 Java 프레임워크.
- JSONassert : JSON 용 Assertion Java 라이브러리.
- JsonPath : JSON 용 XPath

JUnit에서 사용하는 어노테이션은 다음과 같은 것들이 있다. (JUnit5기준)

- @ExtendWtih(SpringExtenstion.class) : JUnit4의 @RunWith(SpringRunner.class) 와 같은 어노테이션으로 JUnit5부터 사용.
- @SpringBootTest : SpringBoot의 테스트 환경에서 의존성을 주입해주는 중요한 어노테이션으로써 위 코드는 Mock의 의존성을 주입해 주었음. classes라는 parameter를 통해 Bean을 주입할 수 있음.
- @Test : 해당 메소드가 Test 대상임을 알려줌.
- @BeforeAll : 해당 클래스의 모든 Test가 수행되기 전에 딱 한번 호출됨. static 메소드의 형태. 보통 설정등에 활용. 
- @AfterAll : 해당 클래스의 모든 Test가 수행된 후에 딱 한번 호출됨. static 메소드의 형태.
- @BeforeEach : 각 Test가 수행되기 전에 매번 호출됨. 로깅등에 활용.
- @AfterEach : 각 Test가 수행된 후에 매번 호출됨.

위의 mock을 사용한 단위 테스트 말고도 Bean을 주입하여 service 및 respository를 assert함수들을 이용하여 테스트하는 것이 일반적이다.

이 장에서 살펴볼 모듈은 문자열 비교 오류를 파악할 때 유용한 ComparisonCompactor라는 모듈로 영리하게 짜인 코드이다. 

두 문자열을 받아 차이를 반환하는데, 예를 들어 ABCDE와 ABXDE를 받아서 <...B[X]D...> 를 반환한다.

설명을 듣기 보다는 우선 테스트 코드를 읽는 편이 훨씬 낫다. 

아래 목록 15-1을 보면 모듈에 대한 기능이 상세히 드러난다. 

테스트 구조를 분석해보고 좀 더 단순하게 혹은 좀 더 명백하게 개선할 수 있는지 알아보자.

#### 목록 15-1 ComparisonCompactorTest.java
```java
package junit.tests.framework;

import junit.framework.ComparisonCompactor;
import junit.framework.TestCase;

public class ComparisonCompactorTest extends TestCase {

    public void testMessage() {
        String failure = new ComparisonCompactor(0, "b", "c").compact("a");
        assertTrue("a expected:<[b]> but was:<[c]>".equals(failure));
    }

    public void testStartSame() {
        String failure = new ComparisonCompactor(1, "ba", "bc").compact(null);
        assertEquals("expected:<b[a]> but was:<b[c]>", failure);
    }

    public void testEndSame() {
        String failure = new ComparisonCompactor(1, "ab", "cb").compact(null);
        assertEquals("expected:<[a]b> but was:<[c]b>", failure);
    }

    public void testSame() {
        String failure = new ComparisonCompactor(1, "ab", "ab").compact(null);
        assertEquals("expected:<ab> but was:<ab>", failure);
    }

    public void testNoContextStartAndEndSame() {
        String failure = new ComparisonCompactor(0, "abc", "adc").compact(null);
        assertEquals("expected:<...[b]...> but was:<...[d]...>", failure);
    }

    public void testStartAndEndContext() {
        String failure = new ComparisonCompactor(1, "abc", "adc").compact(null);
        assertEquals("expected:<a[b]c> but was:<a[d]c>", failure);
    }

    public void testStartAndEndContextWithEllipses() {
        String failure = new ComparisonCompactor(1, "abcde", "abfde").compact(null);
        assertEquals("expected:<...b[c]d...> but was:<...b[f]d...>", failure);
    }

    public void testComparisonErrorStartSameComplete() {
        String failure = new ComparisonCompactor(2, "ab", "abc").compact(null);
        assertEquals("expected:<ab[]> but was:<ab[c]>", failure);
    }

    public void testComparisonErrorEndSameComplete() {
        String failure = new ComparisonCompactor(0, "bc", "abc").compact(null);
        assertEquals("expected:<[]...> but was:<[a]...>", failure);
    }

    public void testComparisonErrorEndSameCompleteContext() {
        String failure = new ComparisonCompactor(2, "bc", "abc").compact(null);
        assertEquals("expected:<[]bc> but was:<[a]bc>", failure);
    }

    public void testComparisonErrorOverlappingMatches() {
        String failure = new ComparisonCompactor(0, "abc", "abbc").compact(null);
        assertEquals("expected:<...[]...> but was:<...[b]...>", failure);
    }

    public void testComparisonErrorOverlappingMatchesContext() {
        String failure = new ComparisonCompactor(2, "abc", "abbc").compact(null);
        assertEquals("expected:<ab[]c> but was:<ab[b]c>", failure);
    }

    public void testComparisonErrorOverlappingMatches2() {
        String failure = new ComparisonCompactor(0, "abcdde", "abcde").compact(null);
        assertEquals("expected:<...[d]...> but was:<...[]...>", failure);
    }

    public void testComparisonErrorOverlappingMatches2Context() {
        String failure = new ComparisonCompactor(2, "abcdde", "abcde").compact(null);
        assertEquals("expected:<...cd[d]e> but was:<...cd[]e>", failure);
    }

    public void testComparisonErrorWithActualNull() {
        String failure = new ComparisonCompactor(0, "a", null).compact(null);
        assertEquals("expected:<a> but was:<null>", failure);
    }

    public void testComparisonErrorWithActualNullContext() {
        String failure = new ComparisonCompactor(2, "a", null).compact(null);
        assertEquals("expected:<a> but was:<null>", failure);
    }

    public void testComparisonErrorWithExpectedNull() {
        String failure = new ComparisonCompactor(0, null, "a").compact(null);
        assertEquals("expected:<null> but was:<a>", failure);
    }

    public void testComparisonErrorWithExpectedNullContext() {
        String failure = new ComparisonCompactor(2, null, "a").compact(null);
        assertEquals("expected:<null> but was:<a>", failure);
    }

    public void testBug609972() {
        String failure = new ComparisonCompactor(10, "S&P500", "0").compact(null);
        assertEquals("expected:<[S&P50]0> but was:<[]0>", failure);
    }
}
```
위 테스트 케이스로 ComparisonCompactor 모듈에 대한 코드 커버리지를 분석해 보면 100%가 나온다. 

테스트 케이스가 모든 행, 모든 if, for문을 실행한다는 말이다. 따라서 모듈이 올바로 동작한다고 믿을 수 있게 되었다.

아래 목록 15-2는 Comparison Compactor 모듈이다. 시간을 들여 코드를 살펴보고 하나하나 따져보자.

#### 목록 15-2 ComparisonCompactor.java
```java
package junit.framework;

public class ComparisonCompactor {

    private static final String ELLIPSIS = "...";
    private static final String DELTA_END = "]";
    private static final String DELTA_START = "[";

    private int fContextLength;
    private String fExpected;
    private String fActual;
    private int fPrefix;
    private int fSuffix;

    public ComparisonCompactor(int contextLength, String expected, String actual) {
        fContextLength = contextLength;
        fExpected = expected;
        fActual = actual;
    }

    public String compact(String message) {
        if (fExpected == null || fActual == null || areStringsEqual()) {
            return Assert.format(message, fExpected, fActual);
        }

        findCommonPrefix();
        findCommonSuffix();
        String expected = compactString(fExpected);
        String actual = compactString(fActual);
        return Assert.format(message, expected, actual);
    }

    private String compactString(String source) {
        String result = DELTA_START + source.substring(fPrefix, source.length() - fSuffix + 1) + DELTA_END;
        if (fPrefix > 0) {
            result = computeCommonPrefix() + result;
        }
        if (fSuffix > 0) {
            result = result + computeCommonSuffix();
        }
        return result;
    }

    private void findCommonPrefix() {
        fPrefix = 0;
        int end = Math.min(fExpected.length(), fActual.length());
        for (; fPrefix < end; fPrefix++) {
            if (fExpected.charAt(fPrefix) != fActual.charAt(fPrefix)) {
                break;
            }
        }
    }

    private void findCommonSuffix() {
        int expectedSuffix = fExpected.length() - 1;
        int actualSuffix = fActual.length() - 1;
        for (; actualSuffix >= fPrefix && expectedSuffix >= fPrefix; actualSuffix--, expectedSuffix--) {
            if (fExpected.charAt(expectedSuffix) != fActual.charAt(actualSuffix)) {
                break;
            }
        }
        fSuffix = fExpected.length() - expectedSuffix;
    }

    private String computeCommonPrefix() {
        return (fPrefix > fContextLength ? ELLIPSIS : "") + fExpected.substring(Math.max(0, fPrefix - fContextLength), fPrefix);
    }

    private String computeCommonSuffix() {
        int end = Math.min(fExpected.length() - fSuffix + 1 + fContextLength, fExpected.length());
        return fExpected.substring(fExpected.length() - fSuffix + 1, end) + (fExpected.length() - fSuffix + 1 < fExpected.length() - fContextLength ? ELLIPSIS : "");
    }

    private boolean areStringsEqual() {
        return fExpected.equals(fActual);
    }
}
```

보이스카우트 규칙에 따라 처음 왔을 때 보다 더 깨끗하게 해 놓고 떠나야한다. 그럼 어떻게 개선하면 좋을까?

#### 1. 인코딩을 피하라[N6]
가장 먼저 거슬리는 부분은 멤버 변수 앞에 붙인 접두어 f다.

오늘날 사용하는 개발 환경에서는 이처럼 변수 이름에 범위를 명시할 필요가 없다.
```java
private int contextLength;
private String expected;
private String actual;
private int prefix;
private int suffix;
```
#### 2. 조건을 캡슐화하라[G28]
다음으로 compact 함수 시작부에 캡슐화되지 않은 조건문이 보인다.

```java
public String compact(String message) {
    if (expected == null || actual == null || areStringsEqual()) {
        return Assert.format(message, expected, actual);
    }

    findCommonPrefix();
    findCommonSuffix();
    String expected = compactString(this.expected);
    String actual = compactString(this.actual);
    return Assert.format(message, expected, actual);
}
```
의도를 명확히 표현하려면 조건문을 메서드로 뽑아내 적절한 이름을 붙여 캡슐화 한다.

```java
public String compact(String message) {
    if (shouldNotCompact()) {
        return Assert.format(message, expected, actual);
    }

    findCommonPrefix();
    findCommonSuffix();
    String expected = compactString(this.expected);
    String actual = compactString(this.actual);
    return Assert.format(message, expected, actual);
}

private boolean shouldNotCompact() {
    return expected == null || actual == null || areStringsEqual();
}
```
#### 3. 가능하다면 표준 명명법을 사용하라[N3]

Compact 함수에서 사용하는 this.expected와 this.actual도 이미 지역변수가 있기 때문에 눈에 거슬린다. 

이는 fExpected에서 f를 빼버리는 바람에 생긴 결과다. 

함수에서 멤버 변수와 이름이 똑같은 변수를 사용하는 이유는 무엇인가? 

서로 다른 의미라면 이름은 명확하게 붙인다.
```java
String compactExpected = compactString(expected);
String compactActual = compactString(actual);
```

#### 4. 부정 조건은 피하라[G29]

부정문은 긍정문보다 이해하기 약간 더 어렵다. 

그러므로 첫 문장 if를 긍정으로 만들어 조건문을 반전한다.
```java
public String compact(String message) {
    if (canBeCompacted()) {
        findCommonPrefix();
        findCommonSuffix();
        String compactExpected = compactString(expected);
        String compactActual = compactString(actual);
        return Assert.format(message, compactExpected, compactActual);
    } else {
        return Assert.format(message, expected, actual);
    }       
}

private boolean canBeCompacted() {
    return expected != null && actual != null && !areStringsEqual();
}
```

#### 5. 이름으로 부수 효과를 설명하라[N7]

함수 이름이 이상하다. 

문자열을 압축하는 함수라지만 실제로 canBeCompacted가 false이면 압축하지 않는다. 

오류 점검이라는 부가 단계가 숨겨지는 것이다. 

그리고 단순한 압축 문자열이 아닌 형식이 갖춰진 문자열을 반환하기에 실제로는 formatCompactedComparison이라는 이름이 적합하다. 

인수를 고려하면 가독성이 훨씬 좋아진다.
```java
public String formatCompactedComparison(String message) {
```

#### 6. 함수는 한 가지만 해야 한다[G30]

if 문 안에서 예상 문자열과 실제 문자열을 진짜로 압축한다. 

이부분을 빼내 compactExpectedAndActual이라는 메서드로 만들고 형식을 맞추는 작업은 formatCompactedComparison에게 맡긴다. 

그리고 compacteExpectedAndActual은 압축만 수행한다.
```java
...

private String compactExpected;
private String compactActual;

...

public String formatCompactedComparison(String message) {
    if (canBeCompacted()) {
        compactExpectedAndActual();
        return Assert.format(message, compactExpected, compactActual);
    } else {
        return Assert.format(message, expected, actual);
    }       
}

private compactExpectedAndActual() {
    findCommonPrefix();
    findCommonSuffix();
    compactExpected = compactString(expected);
    compactActual = compactString(actual);
}
```

#### 7.일관성 부족[G11]

위에서 compactExpected와 compactActual을 멤버 변수로 승격했다는 사실에 주의한다. 

새함수에서 마지막 두 줄은 변수를 반환하지만 첫째 줄과 둘째 줄은 반환 값이 없다. 

그래서 findCommonPrefix와 findCommonSuffix를 변경해 접두어 값과 접미어 값을 반환한다.
```java
private compactExpectedAndActual() {
    prefixIndex = findCommonPrefix();
    suffixIndex = findCommonSuffix();
    String compactExpected = compactString(expected);
    String compactActual = compactString(actual);
}

private int findCommonPrefix() {
    int prefixIndex = 0;
    int end = Math.min(expected.length(), actual.length());
    for (; prefixIndex < end; prefixIndex++) {
        if (expected.charAt(prefixIndex) != actual.charAt(prefixIndex)) {
            break;
        }
    }
    return prefixIndex;
}

private int findCommonSuffix() {
    int expectedSuffix = expected.length() - 1;
    int actualSuffix = actual.length() - 1;
    for (; actualSuffix >= prefixIndex && expectedSuffix >= prefix; actualSuffix--, expectedSuffix--) {
        if (expected.charAt(expectedSuffix) != actual.charAt(actualSuffix)) {
            break;
        }
    }
    return expected.length() - expectedSuffix;
}
```
#### 8.서술적인 이름을 사용하라[N1]

위 7) 과정에서 생략되었지만 멤버 변수의 이름이 색인 위치를 나타내기 때문에 각각 prefix, suffix에서 prefixIndex, suffixIndex로 수정되었다.

private int prefixIndex;
private int suffixIndex;

#### 9.숨겨진 시간적인 결합[G31]

findCommonSuffix를 주의 깊게 살펴보면 숨겨진 시간적인 결합(hidden temporal coupling)이 존재한다.
 
다시 말해, findCommonSuffix는 findCommonPrefix가 prefixIndex를 계산한다는 사실에 의존한다. 
 
만약 잘못된 순서로 호출하면 밤샘 디버깅이라는 고생문이 열린다. 
 
그래서 시간 결합을 외부에 노출하고자 findCommonPrefix를 고쳐 prefixIndex를 인수로 넘겼다.
```java 
private compactExpectedAndActual() {
    prefixIndex = findCommonPrefix();
    suffixIndex = findCommonSuffix(prefixIndex);
    String compactExpected = compactString(expected);
    String compactActual = compactString(actual);
}

private int findCommonSuffix(int prefixIndex) {
    int expectedSuffix = expected.length() - 1;
    int actualSuffix = actual.length() - 1;
    for (; actualSuffix >= prefixIndex && expectedSuffix >= prefix; actualSuffix--, expectedSuffix--) {
        if (expected.charAt(expectedSuffix) != actual.charAt(actualSuffix)) {
            break;
        }
    }
    return expected.length() - expectedSuffix;
}
```

#### 10.일관성을 유지하라[G32]

위 9)의 방식이 썩 내키지는 않는다. 

prefixIndex를 인수로 전달하는 방식은 다소 자의적이다. 

함수 호출 순서는 확실히 정해지지만 인수가 필요한 이유가 분명히 드러나지 않는다.
 
따라서 다른 프로그래머가 원래대로 되돌려 놓을지도 모른다. 
 
이번에는 다른 방식을 고안해 보자
 
findCommonPrefix와 findCommonSuffix를 원래대로 되돌리고 findCommonSuffix라는 이름을 findCommonPrefixAndSuffix로 바꾸고, 

findCommonPrefixAndSuffix에서 가장 먼저 findComonPrefix를 호출한다. 
 
이 경우 호출하는 순서가 앞서 고친 코드 보다 훨씬 더 분명해진다. 
 
또한 findCommonPrefixAndSuffix 함수가 얼마나 지저분한지도 드러난다.
```java 
private compactExpectedAndActual() {
    findCommonPrefixAndSuffix();
    String compactExpected = compactString(expected);
    String compactActual = compactString(actual);
}

private void findCommonPrefixAndSuffix() {
    findCommonPrefix();
    int expectedSuffix = expected.length() - 1;
    int actualSuffix = actual.length() - 1;
    for (; actualSuffix >= prefixIndex && expectedSuffix >= prefix; actualSuffix--, expectedSuffix--) {
        if (expected.charAt(expectedSuffix) != actual.charAt(actualSuffix)) {
            break;
        }
    }
    suffixIndex = expected.length() - expectedSuffix;
}

private void findCommonPrefix() {
    int prefixIndex = 0;
    int end = Math.min(expected.length(), actual.length());
    for (; prefixIndex < end; prefixIndex++) {
        if (expected.charAt(prefixIndex) != actual.charAt(prefixIndex)) {
            break;
        }
    }
}
```
지저분한 findCommanPrefixAndsuffix함수를 정리해 보자. 

책에 내용에서는 자연스로 index라는 용어를 length로 수정하였는데 이는 다음 단계에서 보자.
```java 
private void findCommonPrefixAndSuffix() {
    findCommonPrefix();
    int suffixLength = 1;
    for (; suffixOverlapsPrefix(suffixLength); suffixLength++) {
        if (charFromEnd(expected, suffixLength) != charFromEnd(actual, suffixLength)) {
            break;
        }
    }
    suffixIndex = suffixLength;
}

private char charFromEnd(String s, int i) {
    return s.charAt(s.length() - i);
}

private boolean suffixOverlapsPrefix(int suffixLength) {
    return actual.length() = suffixLength < prefixLength || expected.length() - suffixLength < prefixLength;
}
```
#### 11.경계 조건을 캡슐화하라[G33]

prefixIndex도 마찬가지로 이 경우 "index"와 "length"가 동의어다. 

비록 그렇다고 하더라구 "length"가 더 합당하다. 

실제로 suffixIndex는 0이 아닌 1에서 시작하므로 진정한 길이가 아니다. 

computeCommSuffix에 +1 하는 것이 곳곳에 등장하는 이유도 여기 있다.

computeCommonSuffix에서 +1을 없애고 charFromEnd에 -1을 추가하고 suffixOverlapsPrefix에 <=를 사용했다.

그 다음 suffixIndex를 suffixLength로 바꾸었다. 

그런데 문제가 하나 생겼다. +1을 제거하던 중 compactString에서 다음행을 발견하였다.

if (suffixLength > 0) {

suffixLength가 이제 1씩 감소했으므로 당연히 > 연산자를 >= 연산자로 고쳐야 마땅하다. 

하지만 >= 연산자는 말이 안되므로 그대로 > 연산자가 맞다! 즉 원래 코드가 틀렸으며 필경 버그라는 말이다.  

아니, 엄밀하게 버그는 아니다. 코드를 좀 더 분석해 보면 이제 if 문은 길이가 0인 접미어를 걸러내 첨부하지 않는다. 

원래 코드는 언제나 suffixIndex가 1 이상이었으므로 if 문이 무의미 했다.

#### 12.죽은 코드[G9]

이후에 compactString에 있는 if 문이 둘다 의심스러워 진다. 

살펴보면 둘 다 필요가 없다.
 
두 문장 다 주석처리 한 후 테스트를 돌려보아도 통과하므로 불필요한 부분을 제거하고 구조를 좀 더 다듬어보자
```java 
private String compactString(String source) {
        return computeCommonPrefix() + DELTA_START +  source.substring(prefixLength, source.length() - suffixLength) + DELTA_END + computeCommonSuffix();
    }
```    
훨씬 나아진 모습을 볼 수 있다.  

#### 결론
보이스카우트 규칙을 지키며 모듈은 처음보다 조금 더 깨끗해 졌다. 

원래 깨끗하지 못했다는 말이 아니라 저자들은 우수한 모듈을 만들었다. 

하지만 세상에 불필요한 모듈은 없다. 

코드를 처음보다 조금 더 깨끗하게 만드는 책임은 우리 모두에게 있다.