# 4장. 주석

### 주석이 나쁜 이유
> 나쁜 코드에 주석을 달지 마라. 새로 짜라.
> - 브라이언 W. 커니헨, P.J. 플라우거

잘 달린 주석은 그 어떤 정보보다 유용하다. 
경솔하고 근거 없는 주석은 코드를 이해하기 어렵게 만든다.
오래되고 조잡한 주석은 거짓과 잘못된 정보를 퍼트려 해약을 미친다.

우리가 프로그래밍 언어를 치밀하게 사용해 의도를 표현할 능력이 있다면 주석은 필요하지 않다.

`우리는 코드로 의도를 표현하지 못해, 그러니까 실패를 만회하기 위해 주석을 사용한다.` 때때로 주석 없이는 자신을 표현할 방법을 찾지 못해 할 수 없이 주석을 사용한다.

그러므로 주석이 필요하면 상황을 역전해 코드로 의도를 표현할 방법은 없는지 생각하자!

`주석은 오래될수록 코드에서 멀어진다. 오래될수록 완전히 그릇될 가능성도 커진다.` 프로그래머들이 주석을 유지하고 보수하기란 현실적으로 불가능하다.

코드는 변화하고 진화하면서 일부가 여기저기 옮겨진다. 이 때 주석이 언제나 코드를 따라가지 않고 부정확한 고아로 변하는 사례가 빈번하다.

`부정확한 주석은 아예 없는 주석보다 훨씬 더 나쁘다.` 부정확한 주석은 독자를 현혹하고 오도한다. 더 이상 지킬 필요가 없는 규칙이나 지켜서는 안 되는 규칙을 명시한다.

코드는 자신이 하는 일을 진실되게 말한다. 코드만이 정확한 정보를 제공하는 유일한 출처다. 그러므로 우리는 (간혹 필요할지라도) 주석을 가능한 줄이도록 꾸준히 노력해야 한다.

### 주석은 나쁜 코드를 보완하지 못한다.

코드에 주석을 추가하는 일반적인 이유는 코드 품질이 나쁘기 때문이다. 모듈을 짜다보니 짜임새가 엉망이고 알아먹기 어렵다. 

표현력이 풍부하고 깔끔하며 주석이 거의 없는 코드가 복잡하고 어수선하며 주석이 많은 코드보다 훨씬 좋다.

### 코드로 의도를 표현하라!

```java
// 직원에게 복지 혜택을 받을 자격이 있는지 검사한다.
if ((employee.flags & HOURLY_FLAG) && 
    (employee.age > 65))
```

```java
if (employee.isEligibleForFullBenefits())
```

위 두 예시 중 어떤 코드가 더 나은가?
몇 초만 더 생각하면 코드로 대다수 의도를 표현할 수 있다. 많은 경우 주석으로 달리는 설명을 함수로 만들어 표현해도 충분하다.

### 좋은 주석

어떤 주석은 필요하거나 유익하다.

#### 법적인 주석

때로는 회사가 정립  한 구현 표준에 맞춰 법적인 이유로 특정 주석을 넣으라고 명시한다. 예를 들어, 각 소스 파일 첫머리에 주석으로 들어가는 저작권 정보와 소유권 정보는 필요하고도 타당하다.

```java
// Copyright (C) 2003, 2004, 2005 by Object Mentor, Inc. All rights reserved. 
// GND General Public License 버전 2 이상을 따르는 조건으로 배포한다.
```

소스 파일 첫머리에 들어가는 주석이 반드시 계약 조건이나 법적인 정보일 필요는 없다. 모든 조항과 조건을 열거하는 대신에, 가능하다면, 표준 라이선스나 외부 문서를 참고해도 된다.

#### 정보를 제공하는 주석

때로는 기본적인 정보를 주석으로 제공하면 편하다.
예를 들어, 다음 주석은 추상 메서드가 반환할 값을 설명한다.

```java
// 테스트 중인 Responder 인스턴스를 반환한다.
protected abstract Responder responderInstance();
```

때때로 위와 같은 주석이 유용하다 할지라도, 가능하면 함수 이름에 정보를 담아 두는 편이 좋다.

```java
// kk:mm:ss EEE, MMM dd, yyyy 형식이다.
Pattern timeMatcher = Pattern.compile(
    "\\d*:\\d*:\\d* \\w*, \\w* \\d*, \\d*");
)
```

코드에서 사용한 정규표현식이 시각과 날짜를 뜻한다고 설명한다. 이왕이면 시각과 날짜를 변환하는 클래스를 만들어 코드를 옮겨주면 더 좋고 더 깔끔하겠다. 그러면 주석이 필요 없어 진다.

#### 의도를 설명하는 주석

때로는 주석은 구현을 이해하게 도와주는 선을 넘어 결정에 깔린 의도까지 설명한다. 다음은 주석으로 흥미로운 결정을 기록한 예제다. 두 객체를 비교할 때 저자는 다른 어떤 객체보다 자기 객체에 높은 순위를 주기로 결정했다.

```java
public int compare(Object o){
    if (o instanceof WikiPagePath){
        WikiPaghPath p = (WikiPagePath) o;
        String compressedName = StringUtil.join(names, "");
        String compressedArgumentName = StringUtil.join(p.names, "");
        return compressedName.compareTo(compressedArgumentName);
        }
        return 1; // 오른쪽 유형이므로 정렬 순위가 더 높다.
}
```

#### 의미를 명료하게 밝히는 주석

때때로 모호한 인수나 반환값은 그 의미를 읽기 좋게 표현하면 더 이해하기 쉬워진다. 일반적으로 인수나 반환값 자체를 명확하게 만들면 더 좋겠지만, 인수나 반환값이 표준 라이브러리나 변경하지 못하는 코드에 속한다면 의미를 명료하게 밝히는 주석이 유용하다.

이 때 그릇된 주석을 달아놓을 위험이 상당히 높으므로 더 나은 방법이 없는지 고민하고 정확히 달도록 각별히 주의한다.

#### 결과를 경고하는 주석

때로 다른 프로그래머에게 결과를 경고할 목적으로 주석을 사용한다.

주석으로 메서드나 클래스 사용 시 주의해야 할 점을 남기거나 @Ignore 속성에 문자열로 넣어준다. `@Ignore("실행이 너무 오래 걸린다.")`

#### TODO 주석

TODO 주석은 프로그래머가 필요하다 여기지만 당장 구현하기 어려움 업무를 기술한다. 더 이상 필요 없는 기능을 삭제하라는 알림, 누군가에게 문제를 봐달라는 요청, 더 좋은 이름을 떠올려달라는 부탁, 앞으로 발생할 이벤트에 맞춰 코드를 고치라는 주의 등에 유용하다.

하지만 어떤 용도로 사용하든 시스템에 나쁜 코드를 남겨 놓는 핑계가 되어서는 안된다. 그래도 TODO로 떡칠된 코드는 바람직하지 않으므로 주기적으로 TODO 주석을 점검해 없애도 괜찮은 주석은 없애라고 권한다.

#### 중요성을 강조하는 주석

자칫 대수롭지 않다고 여겨질 뭔가의 중요성을 강조하기 위해서도 주석을 사용한다.

#### 공개 API에서 Javadocs

설명이 잘 된 공개 API는 참으로 유용하고 만족스럽다. 표준 자바 라이브러리에서 사용한 javadocs가 좋은 예다.

공개 API를 구현한다면 반드시 훌륭한 javadocs를 작성한다. 하지만 이 장에서 제시하는 나머지 충고도 명심하기 바란다. 여느 주석과 마찬가지로 Javadocs 역시 독자가 오도하거나, 잘못 위치하거나, 그릇된 정보를 전달할 가능성이 존재한다.

### 나쁜 주석

대다수 주석이 이 범주에 속한다. 일반적으로 대다수 주석은 허술한 코드를 지탱하거나, 엉상한 코드를 변명하거나, 미숙한 결정을 합리화하는 등 프로그래머가 주절거리는 독백에 크게 벗어나지 못한다.

#### 주절거리는 주석

특별한 이유 없이 의무감으로 혹은 프로세스에서 하라고 하니까 마지못해 주석을 단다면 전적으로 시간 낭비다. 주석을 달기로 결정했따면 충분한 시간을 들여 최고의 주석을 달도록 노력한다.

#### 같은 이야기를 중복하는 주석

헤더에 달린 주석이 같은 코드 내용을 그대로 중복한다. 자칫하면 코드보다 주석을 읽는 시간이 더 오래 걸린다.

```java
/**
 * 컨테이너와 관련된 Loader 구현
 */
 protected Loader loader = null;

```
#### 오해할 여지가 있는 주석

때때로 의도는 좋았으나 프로그래머가 딱 맞을 정도로 엄밀하게 주석을 달지 못하기도 한다.

(코드보다도 읽기 어려운) 주석에 담긴 '살짝 잘못된 정보'로 인해 프로그래머가 코드가 이상하게 돌아가는 이유를 찾다가 골머리를 앓을 수 있다.

```java
// this.closed가 true일 때 반환되는 유틸리티 메서드다.
// 타임아웃에 도달하면 예외를 던진다.
public synchronized void waitForClose(final long timeoutMills) throws Exception {
    if (!closed){
        wait(timeoutMills);
        if (!closed)
            throw new Exception("MockResponseSender could not be closed");
    }
}
```

#### 의무적으로 다는 주석
모든 함수에 Javadocs를 달거나 모든 변수에 주석을 달아야 한다는 규칙은 어리석기 그지없다. 이런 주석은 코드를 복잡하게 만들며, 거짓말을 퍼뜨리고, 혼동과 무질서를 초래한다.

#### 이력을 기록하는 주석
때때로 사람들은 모듈을 편집할 때마다 모듈 첫머리에 주석을 추가한다. 그리하여 모듈 첫머리 주석은 지금까지 모듈에 가한 변경을 모두 기록하는 일조의 일지 혹은 로그가 된다.

예전에는 모든 모듈 첫머리에 변경 이력을 기록하고 관리하는 관례가 바람직했다. 당시에는 소스코드 관리 시스템이 없었으니까. 하지만 이제는 혼란만 가중할 뿐이다. 완전히 제거하는 편이 좋다.

#### 있으나 마나 한 주석
때때로 있으나 마나 한 주석을 접한다. 쉽게 말해, 너무 당연한 사실을 언급하며 새로운 정보를 제공하지 못하는 주석이다.

이러한 주석은 지나친 참견이라 개발자가 무시하는 습관에 빠진다. 코드를 읽으며 자동으로 주석을 건너뛴다. 결국은 코드가 바뀌면서 주석은 거짓말로 변한다.

있으나 마나 한 주석을 달려는 유혹에서 벗어나 코드를 정리하라. 더 낫고, 행복한 프로그래머가 되는 지름길이다.

#### 무서운 잡음
때로는 Javadocs도 잡음이다. 주석에 코드의 변수 이름을 복붙할 생각이라면 그냥 적지 않는 편이 좋다. 복붙하다 실패하면 골치만 아프다.

#### 함수나 변수로 표현할 수 있다면 주석을 달지 말라

주석을 없애고 함수나 변수로 표현할 수 있는지 보자.

#### 위치를 표시하는 주석

때때로 프로그래머는 소스 파일에서 특정 위치를 표시하려 주석을 사용한다.

```java
// Actions ///////////////////////////////////////////
```

극히 드물지만 위와 같은 배너 아래 특정 기능을 모아놓으면 유용한 경우도 있긴 하다. 하지만 일반적으로 위와 같은 주석은 가독성을 낮추므로 제거해야 마땅하다. 특히 뒷부분에 슬래시(/)로 이어지는 잡음은 제거하는 편이 좋다.

너무 자주 사용하지 않는다면 배너는 눈에 띄며 주의를 환기한다. 그러므로 반드시 필요할 때만, 아주 드물게 사용하는 편이 좋다. 배너를 남용하면 독자가 흔한 잡음으로 여겨 무시한다.

#### 닫는 괄호에 다는 주석

때때로 프로그래머들이 닫는 괄호에 특수한 주석을 달아놓는다. 중첩이 심하고 장황한 함수라면 의미가 있을지도 모르지만 작고 캡슐화된 함수에는 잡음일 뿐이다. 그러므로 닫는 괄호에 주석을 달아야 겠다는 생각이 든다면 대신에 함수를 줄이려 시도하자.

```java
public class wc {
    public static void main(String[] args) {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String line;
        int lineCount = 0;
        int charCount = 0;
        int wordCount = 0;
        try {
            while ((line = in.readLine()) != null) {
                lineCount++;
                charCount += line.length();
                String words[] = line.split("\\W");
                wordCount += words.length;
            } // while
            System.out.println("wordCount = " + wordCount);
            System.out.println("lineCount = " + lineCount);
            System.out.println("charCount = " + charCount);
        } // try
        catch (IOException e) {
            System.err.println("Error:" + e.getMessage());
        } // catch
    } // main
}
```

#### 공로를 돌리거나 저자를 표시하는 주석

소스 코드 관리 시스템은 누가 언제 무엇을 추가했는지 귀신처럼 기억한다. 저자 이름으로 코드를 오염시킬 필요가 없다. 주석이 있으면 다른 사람들이 코드에 관해 누구한테 물어볼지 아니까 위와 같은 주석이 유용하다 여길지도 모르겠다. 하지만 현실적으로 이런 주석은 그냥 오랫동안 코드에 방치되어 점차 부정확하고 쓸모없는 정보로 변하기 쉽다.

다시 한 번 강조하지만, 위와 같은 정보는 소스 코드 관리 시스템에 저장하는 편이 좋다.

#### 주석으로 처리한 코드

주석으로 처리한 코드만큼 밉살스런 관행도 드물다. 다음과 같은 코드는 작성하지 마라!

```java
InputStreamResponse response = new InputStreamResponse();
response.setBody(formatter.getResultStream(), formatter.getByteCount());
// InputStream resultStream = formatter.getResultStream();
// StreamReader reader = new StreamReader(resultsStream);
// response.setContent(reader.read(formatter.getByteCount()));
```

주석으로 처리된 코드는 다른 사람들이 지우기를 주저한다. 이유가 있어 남겨놓았지 않을까? 중요하니까 지우면 안 되지 않을까? 그래서 쓸모 없는 코드가 점차 쌓여간다.

우리는 오래 전부터 우수한 소스 코드 관리 시스템을 사용해왔다. 소스 코드 관리 시스템이 우리를 대신해 코드를 기억해준다. 이제는 주석으로 처리할 필요가 없다. 그냥 코드를 삭제하라. 잃어버릴 염려는 없다. 약속한다.

#### HTML 주석

소스 코드에서 HTML 주석은 혐오 그 자체다. 주석을 뽑아 웹 페이지에 올릴 작정이라면 주석에 HTML 태그를 삽입하는 책임은 프로그래머가 아니라 도구가 져야 한다.

#### 전역 정보

주석을 달아야 한다면 근처에 있는 코드만 기술하라. 코드 일부에 주석을 달면서 시스템의 전반적인 정보를 기술하지 마라. 

```java
/**
 * 적합성 테스트가 동작하는 포트: 기본값은 <b>8282</b>.
 *
 * @param fitnessPort
 */
 public void setFitnessPort(int fitnessPort){
     this.fitnessPort = fitnessPort;
 }
```

위 코드는 주석에 기본 포트 정보를 기술하지만 함수나 변수에서 포트 기본 값을 전혀 통제하지 못한다. 즉, 포트 기본값을 설정하는 코드가 변해도 아래 주석이 변하리라는 보장은 전혀 없다.

#### 너무 많은 정보

주석에다 흥미로운 역사나 관련없는 정보를 장황하게 늘여놓지 마라.

#### 모호한 관계

주석과 주석이 설명하는 코드는 둘 사이 관계가 명확해야 한다.

```java
/**
 * 모든 픽셀을 담을 만큼 충분한 배열로 시작한다(여기에 필터 바이트를 더한다).
 * 그리고 헤더 정보를 위해 200바이트를 더한다.
 */
 this.pngBytes = new byte[((this.width + 1) * this.height * 3) + 200];
```

여기서 필터 바이트란 무엇일까? +1과 관련 있을 까? * 3과 관련이 있을까? 아니면 둘 다? 한 픽셀이 한 바이트인가? 200을 추가한 이유는 뭘까? 주석을 다는 목적은 코드만으로 설명이 부족해서다. 주석 자체가 다시 설명을 요구하니 안타깝기 그지없다.

#### 함수 헤더

짧은 함수는 긴 설명이 필요 없다. 짧고 한 가지만 수행하여 이름을 잘 붙인 함수가 주석으로 헤더를 추가한 함수보다 훨씬 좋다.

#### 비공개 코드에서 Javadocs

공개 API는 javadocs가 유용하지만 공개하지 않을 코드라면 javadocs는 쓸모가 없다. 시스템 내부에 속한 클래스와 함수에 Javadocs를 생성할 필요는 없다. 유용하지 않을 뿐만 아니라 Javadocs 주석이 요구하는 형식으로 인해 코드만 보기 싫고 산만해질 뿐이다.

### 예제

`리팩토링 전 GeneratePrimes.java`
```java
/**
 * This class Generates prime numbers up to a user specified
 * maximum. The algorithm used is the Sieve of Eratosthenes.
 * <p>
 * Eratosthenes of Cyrene, b. c. 276 BC, Cyrene, Libya --
 * d. c. 194, Alexandria.  The first man to calculate the
 * circumference of the Earth. Also known for working on
 * calendars with leap years and ran the library at Alexandria.
 * <p>
 * The algorithm is quite simple. Given an array of integers
 * starting at 2. Cross out all multiples of 2. Find the next
 * uncrossed integer, and cross out all of its multiples.
 * Repeat untilyou have passed the square root of the maximum
 * value.
 *
 * @author Alphonse
 * @version 13 Feb 2002 atp
 */

public class GeneratePrimes
{
  /**
   * @param maxValue is the generation limit.
   */
  public static int[] generatePrimes(int maxValue)
  {
    if (maxValue >= 2) // the only valid case
    {
      // declarations
      int s = maxValue + 1; // size of array
      boolean[] f = new boolean[s];
      int i;
      // initialize array to true.
      for (i = 0; i < s; i++)
        f[i] = true;

      // get rid of known non-primes
      f[0] = f[1] = false;

      // sieve
      int j;
      for (i = 2; i < Math.sqrt(s) + 1; i++)
      {
        if (f[i]) // if i is uncrossed, cross its multiples.
        {
          for (j = 2 * i; j < s; j += i)
            f[j] = false; // multiple is not prime
        }
      }

      // how many primes are there?
      int count = 0;
      for (i = 0; i < s; i++)
      {
        if (f[i])
          count++; // bump count.
      }

      int[] primes = new int[count];

      // move the primes into the result
      for (i = 0, j = 0; i < s; i++)
      {
        if (f[i])             // if prime
          primes[j++] = i;
      }

      return primes; // return the primes
    }
    else // maxValue < 2
    return new int[0]; // return null array if bad input.
  }

  public static void main(String[] args) {
    for (int i: generatePrimes(50)) {
      System.out.print(i + " ");
    }
  }
}
```

`리팩토링 후 PrimeGenerator.java`

```java
/**
 * This class Generates prime numbers up to a user specified
 * maximum.  The algorithm used is the Sieve of Eratosthenes.
 * Given an array of integers starting at 2:
 * Find the first uncrossed integer, and cross out all its
 * multiples.  Repeat until there are no more multiples
 * in the array.
 */

public class PrimeGenerator {
  private static boolean[] crossedOut;
  private static int[] result;

  public static int[] generatePrimes(int maxValue) {
    if (maxValue < 2)
      return new int[0];
    else {
      uncrossIntegersUpTo(maxValue);
      crossOutMultiples();
      putUncrossedIntegersIntoResult();
      return result;
    }
  }

  private static void uncrossIntegersUpTo(int maxValue) {
    crossedOut = new boolean[maxValue + 1];
    for (int i = 2; i < crossedOut.length; i++)
      crossedOut[i] = false;
  }

  private static void crossOutMultiples() {
    int limit = determineIterationLimit();
    for (int i = 2; i <= limit; i++)
      if (notCrossed(i))
        crossOutMultiplesOf(i);
  }

  private static int determineIterationLimit() {
    // Every multiple in the array has a prime factor that
    // is less than or equal to the root of the array size,
    // so we don't have to cross out multiples of numbers
    // larger than that root.
    double iterationLimit = Math.sqrt(crossedOut.length);
    return (int) iterationLimit;
  }

  private static void crossOutMultiplesOf(int i) {
    for (int multiple = 2 * i;
         multiple < crossedOut.length;
         multiple += i)
      crossedOut[multiple] = true;
  }

  private static boolean notCrossed(int i) {
    return crossedOut[i] == false;
  }

  private static void putUncrossedIntegersIntoResult() {
    result = new int[numberOfUncrossedIntegers()];
    for (int j = 0, i = 2; i < crossedOut.length; i++)
      if (notCrossed(i))
        result[j++] = i;
  }

  private static int numberOfUncrossedIntegers() {
    int count = 0;
    for (int i = 2; i < crossedOut.length; i++)
      if (notCrossed(i))
        count++;

    return count;
  }

  public static void main(String[] args) {
    for (int i : generatePrimes(50)) {
      System.out.print(i + " ");
    }
  }
}
```

### 참고 자료

- Clean Code 애자일 소프트웨어 장인 정신