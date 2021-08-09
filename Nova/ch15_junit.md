# 15장. Junit 들여다보기

## Junit 프레임워크

Junit은 저자가 많다. 하지만 시작은 켄트 벡과 에릭 감마 두사람이 비행기를 타고 가다 3시간 동안 Junit 기초를 구현했다.

우리가 살펴볼 모듈은 문자열 비교 오류를 파악할 때 유용한 코드다. ComparisonCompactor라는 모듈로, 영리하게 짜인 코드다. ComparisonCompactor는 두 문자열을 받아 차이를 반환한다.

예를 들어, ABCDE와 ABXDE를 받아 <...B[X]D...>를 반환한다.

##### ComparisonCompactorTest.java

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

위 테스트 케이스로 ComparisonCompactor 모듈에 대한 코드 커버지리 분석을 수행했을 때 100%가 나왔다. 테스트 케이스가 모든 행, 모든 if문, 모든 for문을 실행한다는 뜻이다.

##### ComparisonCompactor.java

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

비록 저자들이 모듈을 아주 좋은 상태로 남겨두었지만 보이스카우트 규칙에 따르면 우리는 처음 왔을 때보다 더 깨끗하게 해놓고 떠나야 한다. 

#### 1) 접두어 f를 제거하자
```java
private int contextLength;
private String expected;
private String actual;
private int prefix;
private int suffix;
```

#### 2) compact 함수 시작 부에 캡슐화되지 않은 조건문이 보인다. 조건문을 캡슐화해서 메서드로 뽑아내 적절한 이름을 붙이자.

##### 개선 전
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

##### 개선 후
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

#### 3) 이름은 명확하게 붙이자
fExpect, fActual
```java
String compactExpected = compactString(expected);
String compactActual = compactString(actual);
```

#### 4) 부정문이 긍정문보다 이해하기 약간 어렵다. 부정문을 긍정문으로 표현하자.
첫 문장 if를 긍정으로 만들어 조건문을 반전한다.
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

#### 5) 함수의 이름으로 효과를 명확하게 설명하자
문자열을 압축하는 함수지만 canBeCompacted가 false면 압축하지 않는다. 게다가 함수는 단순히 압축된 문자열이 아니라 형식이 갖춰진 문자열을 반환한다.

```java
public String formatCompactedComparison(String message) {
```

#### 6) 함수는 한 가지 일만 해야 한다.
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

#### 7) 함수를 일관적으로 사용하자.
새 함수에서 마지막 두 줄은 변수를 반환하지만 첫째 줄과 둘째 줄은 반환값이 없다. 함수 사용 방식이 일관적이지 못하므로 findCommonPrefix와 findCommonSuffix를 변경해 접두어 값과 접미어 값을 반환한다.

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

#### 7) 정확한 이름을 사용하라
```java
private int prefixIndex;
private int suffixIndex;
```

#### 8) 숨겨진 시각적인 결합
findCommonSuffix에는 숨겨진 시각적인 결합이 존재한다. 다시 말해, findCommonSuffix는 findCommonPrifix가 prefixIndex를 계산한다는 사실에 의존한다. 만약 findCommonPrefix와 findCommonSuffix를 잘못된 순서로 호출하면 밤샘 디버깅이라는 고생이 열리므로 시간 결합을 외부에 노출하고자 findCommonSuffix를 고쳐 prefixIndex를 인수로 넘겼다.

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


#### 9) 일관성을 유지하자
함수 호출 순서는 확실히 정해지지만 prefixIndex가 필요한 이유는 설명하지 못한다. prefixIndex가 필요한 이유가 분명히 드러나지 않으므로 다른 프로그래머가 원래대로 되돌려놓을지도 모른다.

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

findcommonPrefix와 findCommonSuffix를 원래대로 되돌리고 findCommonSuffix라는 이름을 findCommonPrefixSuffix로 바꾸고, findCOmmonPrefixAndSuffix에서 가장 먼저 findCommonPrefix를 호출한다. 그러면 두 함수를 호출하는 순서가 앞서 고친 코드보다 훨씬 더 명확해진다.

이제 findCommonPrefixAndSuffix 함수를 정리해보자.

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

#### 10) 경계 조건을 캡슐화하라

코드를 고치고 나니까 suffixIndex가 실제로는 접미어 길이라는 사실이 드러난다. 이름이 적절하지 못하다는 말이다. 실제로 suffixIndex는 0에서 시작하지 않고 1에서 시작하므로 "
length"가 더 합당하다.

##### ComparisonCompactor.java (중간 버전)
```java
public class ComparisonCompactor {
    ...
    private int suffixLength;
    ...

    private void findCommonPrefixAndSuffix() {
        findCommonPrefix();
        suffixLength = 0;
        for (; suffixOverlapsPrefix(suffixLength); suffixLength++) {
            if (charFromEnd(expected, suffixLength) != charFromEnd(actual, suffixLength)) {
                break;
            }
        }
    }

    private char charFromEnd(String s, int i) {
        return s.charAt(s.length() - i - 1);
    }

    private boolean suffixOverlapsPrefix(int suffixLength) {
        return actual.length() = suffixLength <= prefixLength || expected.length() - suffixLength <= prefixLength;
    }

    ...
    private String compactString(String source) {
        String result = DELTA_START + source.substring(prefixLength, source.length() - suffixLength) + DELTA_END;
        if (prefixLength > 0) {
            result = computeCommonPrefix() + result;
        }
        if (suffixLength > 0) {
            result = result + computeCommonSuffix();
        }
        return result;
    }

    ...
    private String computeCommonSuffix() {
        int end = Math.min(expected.length() - suffixLength + contextLength, expected.length());
        return expected.substring(expected.length() - suffixLength, end) + (expected.length() - suffixLength < expected.length() - contextLength ? ELLIPSIS : "");
    }
}
```

+1을 제거했으니 compactString에 > 연산자를 >= 연산자로 변경한다.

```java
if (suffixLength > 0)
```

불필요한 if문도 제거하고 compactString 구조를 다듬어 좀 더 깔끔하게 만들자.

```java
private String compactString(String source) {
        return computeCommonPrefix() + DELTA_START +  source.substring(prefixLength, source.length() - suffixLength) + DELTA_END + computeCommonSuffix();
    }
```

### 최종 코드
##### ComparisionCompactor.java (최종 버전)

```java
package junit.framework;

public class ComparisonCompactor {

    private static final String ELLIPSIS = "...";
    private static final String DELTA_END = "]";
    private static final String DELTA_START = "[";

    private int contextLength;
    private String expected;
    private String actual;
    private int prefixLength;
    private int suffixLength;

    public ComparisonCompactor(int contextLength, String expected, String actual) {
        this.contextLength = contextLength;
        this.expected = expected;
        this.actual = actual;
    }

    public String formatCompactedComparison(String message) {
        String compactExpected = expected;
        String compactactual = actual;
        if (shouldBeCompacted()) {
            findCommonPrefixAndSuffix();
            compactExpected = comapct(expected);
            compactActual = comapct(actual);
        }         
        return Assert.format(message, compactExpected, compactActual);      
    }

    private boolean shouldBeCompacted() {
        return !shouldNotBeCompacted();
    }

    private boolean shouldNotBeCompacted() {
        return expected == null && actual == null && expected.equals(actual);
    }

    private void findCommonPrefixAndSuffix() {
        findCommonPrefix();
        suffixLength = 0;
        for (; suffixOverlapsPrefix(suffixLength); suffixLength++) {
            if (charFromEnd(expected, suffixLength) != charFromEnd(actual, suffixLength)) {
                break;
            }
        }
    }

    private boolean suffixOverlapsPrefix(int suffixLength) {
        return actual.length() = suffixLength <= prefixLength || expected.length() - suffixLength <= prefixLength;
    }

    private void findCommonPrefix() {
        int prefixIndex = 0;
        int end = Math.min(expected.length(), actual.length());
        for (; prefixLength < end; prefixLength++) {
            if (expected.charAt(prefixLength) != actual.charAt(prefixLength)) {
                break;
            }
        }
    }

    private String compact(String s) {
        return new StringBuilder()
            .append(startingEllipsis())
            .append(startingContext())
            .append(DELTA_START)
            .append(delta(s))
            .append(DELTA_END)
            .append(endingContext())
            .append(endingEllipsis())
            .toString();
    }

    private String startingEllipsis() {
        prefixIndex > contextLength ? ELLIPSIS : ""
    }

    private String startingContext() {
        int contextStart = Math.max(0, prefixLength = contextLength);
        int contextEnd = prefixLength;
        return expected.substring(contextStart, contextEnd);
    }

    private String delta(String s) {
        int deltaStart = prefixLength;
        int deltaend = s.length() = suffixLength;
        return s.substring(deltaStart, deltaEnd);
    }
    
    private String endingContext() {
        int contextStart = expected.length() = suffixLength;
        int contextEnd = Math.min(contextStart + contextLength, expected.length());
        return expected.substring(contextStart, contextEnd);
    }

    private String endingEllipsis() {
        return (suffixLength > contextLength ? ELLIPSIS : "");
    }
}
```

코드가 깔끔하다. 모듈은 일련의 분석 함수와 일련의 조합 함수로 나뉜다.
전체 함수는 위상적으로 정렬했으므로 각 함수가 사용된 직후에 정의된다.

분석함수가 먼저 나오고 조합 함수가 그 뒤를 이어서 나온다.

코드를 주의 깊게 살핀다면 내가 이 장 초반에서 내렸던 결정 일부를 번복했다는 사실에 눈치채리라. 예를 들어, 처음에 추출했떤 메서드 몇 개를 formatCompactedComparison에다 도로 집어넣었다. 또한 shouldNotBeCompacted 조건도 원래대로 되돌렸다. 흔히 생기는 일이다!

코드를 리팩토링 하다 보면 원래 했던 변경을 되돌리는 경우가 흔하다.

리팩토링은 코드가 어느 수준에 이를 때까지 수많은 시행착오를 반복하는 작업이기 때문이다.

## 결론

게다가 우리는 보이스카우트 규칙도 지켰다! 모듈은 처음보다 조금 더 깨끗해졌다. 원래 깨끗하지 못했다는 말은 아니다. 저자들은 우수한 모듈을 만들었다. 하지만 세상에 개선이 불필요한 모듈은 없다. 코드를 처음보다 조금 더 깨끗하게 만드는 책임은 우리 모두에게 있다.