# 창발성 (創發性)

하위 체계로부터 생겨나지만, 그 하위 체계로 환원되지 않는 속성

창발성이란, 창발된 성질이나 실체와 같은 대상들이 더 근본적인 대상들로 부터 발생하지만 근본적인 대상으로 환원되지 않으며 그 자체로 특수한 지위를 차지하는 것을 의미.

즉, 하위 요소를 잘 결합하여 전혀 다른 결과를 얻어 내는 것을 말한다.

#### 창발적 설계로 깔끔한 코드를 구현하자.

착실하게 따르기만 하면 우수한 설계가 나오는 간단한 규칙 네 가지가 있다면?
네 가지 규칙이 우수한 설계의 창발성을 촉진한다면?

우리들 대다수는 켄트 백이 제시한 단순한 설계 규칙 네 가지가 소프트웨어 설계 품질을 크게 높여준다고 믿는다.

> 소프트웨어 설계 품질을 높여주는 켄트 벡의 단순한 설계 규칙 4가지 

1. 모든 테스트를 실행한다.
2. 중복을 없앤다.
3. 프로그래머 의도를 표현한다.
4. 클래스와 메소드 수를 최소로 줄인다.

위 목록은 중요도 순이다.

#### 단순한 설계 규칙 1 : 모든 테스트를 실행하라.

테스트를 철저히 거쳐 모든 테스트 케이스를 항상 통과하는 시스템은 '테스트가 가능한 시스템'이다.

검증이 불가능한 시스템은 절대 출시하면 안 된다.

다행스럽게도, 테스트가 가능한 시스템을 만들려고 애쓰면 설계 품질이 더불어 높아진다.

왜냐하면

- 단일 책임 원칙을 지키는 클래스가 테스트하기 편하다.
- 테스트 케이스가 많을수록 개발자는 코드를 작성하기 쉬워진다.
- 결합도가 높으면 테스트를 작성하기 어렵다. 
의존 관계 역전 법칙을 준수하고 추상화 기법과 같은 방법을 활용하면 결합도를 낮출 수 있다.

#### 단순한 설계 규칙 2~4 : 리팩터링

테스트 케이스를 모두 작성했다면, 구체적으로는 코드를 점진적으로 리팩터링 해나간다.

테스트 케이스가 작성되어 있기 때문에 코드를 정리하면서 시스템이 깨질까 걱정할 필요가 없다.

리팩토링 단계에선 소프트웨 설계 품질을 높이는 기범이라면 무엇이든 적용해도 괜찮다.

응집도를 높이고, 결합도를 낮추고, 관심사를 분리하고, 시스템 관심사를 모듈로 나누고, 
함수와 클래스 크기를 줄이고, 더 나은 이름을 선택하는 등 다양한 기법을 동원한다.

또한 이 단계는 단순한 설계 규칙 중 나머지 3개를 적용해 중복을 제거하고, 프로그래머 의도를 표현하고,
클래스와 메서드 수를 최소로 줄이는 단계이기도 하다.

#### 중복을 없애라

우수한 설계에서 중복은 커다란 적이다. 중복은 추가작업, 추가위험, 불필요한 복잡도를 뜻하기 때문이다.

```java
public void scaleToOneDimension(args...) {
    ... // 생략

    Image newImage = ImageUtils.getScaledImage(image, scalingFactor, scalingFactor);
    image.dispose();
    System.gc();
    image = newImage;
}

public synchronized void rotate(int degrees) {
    Image newImage = ImageUtils.getScaledImage(image, degrees);
    image.dispose();
    System.gc();
    image = newImage;
}
```

코드를 작성하고 보니 일부가 중복이다.
정리하여 중복을 제거한다.

```java
public void scaleToOneDimension(args...) {
    ... // 생략
    replaceImage(ImageUtils.getScaledImage(image, scalingFactor, scalingFactor));
}

public synchronized void rotate(int degrees) {
    replaceImage(ImageUtils.getScaledImage(image, degrees));
}

private void replaceImage(Image newImage) {
    image.dispose();
    System.gc();
    image = newImage;
}
```
아주 적은 양이지만 공통적인 코드를 새 매서드로 뽑고보니 클래스가 SRP 를 위반한다.
그러므로 새로 만든 replaceImage 메소드를 다른 클래스로 옮겨도 좋겠다.

그러면 새 메서드의 가시성이 높아진다. 
따라서 다른 팀원이 새 메서드를 좀 더 추상화해 다른 맥락에서 재사용할 기회를 포착할지도 모른다.

'소규모 재사용'은 시스템 복잡도를 극적으로 줄여준다. 
소규모 재사용을 제대로 익혀야 대규모 재사용이 가능하다.

TEMPLATE METHOD 패턴은 고차원 중복을 제거할 목적으로 자주 사용하는 기법이다.

```java
public class ImageReplacer {

    private Image image;
    public ImageReplacer() {}

    public static ImageReplacer builder() {
        return new ImageReplacer();
    }

    public ImageReplacer image(Image image) {
        this.image = image;
        return this;
    }

    public Image replace() {
        return image;
    }
}
public void scaleToOneDimension(args...) {
    ... // 생략

    image = ImageReplacer.builder()
                .image(ImageUtils.getScaledImage(image, scalingFactor, scalingFactor))
                .replace();
}

public synchronized void rotate(int degrees) {
    image = ImageReplacer.builder()
            .image(ImageUtils.getScaledImage(image, degrees))
            .replace();
}
```

ImageReplacer는 Builder 패턴을 유사하게 사용하여 image 를 반환한다.
이제 이미지 변환의 책임은 ImageReplacer 이 갖게 된다.

> Template Method 패턴

Template Method 패턴은 고차원 중복을 제거할 목적으로 자주 사용하는 기법이다.

템플릿 메소드 패턴의 정의로 GoF Design Patterns 의 정의가 가장 깔끔한 것 같다.

> 알고리즘의 구조를 메소드에 정의하고, 하위 클래스에서 알고리즘 구조의 변경없이 알고리즘을 재정의 하는 패턴이다. 알고리즘이 단계별로 나누어 지거나, 같은 역할을 하는 메소드이지만 여러곳에서 다른형태로 사용이 필요한 경우 유용한 패턴이다.

토비의 스프링에서는 아래와 같이 정의한다.

> 상속을 통해 슈퍼클래스의 기능을 확장할 때 사용하는 가장 대표적인 방법. 변하지 않는 기능은 슈퍼클래스에 만들어두고 자주 변경되며 확장할 기능은 서브클래스에서 만들도록 한다. 

예제를 살펴보자.

```java
public class VacationPolicy {
  public void accrueUSDDivisionVacation() {
    // 지금까지 근무한 시간을 바탕으로 휴가 일수를 계산하는 코드
    // ...
    // 휴가 일수가 미국 최소 법정 일수를 만족하는지 확인하는 코드
    // ...
    // 휴가 일수를 급여 대장에 적용하는 코드
    // ...
  }
  
  public void accrueEUDivisionVacation() {
    // 지금까지 근무한 시간을 바탕으로 휴가 일수를 계산하는 코드
    // ...
    // 휴가 일수가 유럽연합 최소 법정 일수를 만족하는지 확인하는 코드
    // ...
    // 휴가 일수를 급여 대장에 적용하는 코드
    // ...
  }
}
```
최소 법정 일수를 계산하는 코드만 제외하면 두 메서드는 거의 동일하다.

최소 법정 일수를 계산하는 알고리즘은 직원 유형에 따라 살짝 변한다.

여기에 Template Method 패턴을 적용시켜보자.

```java
abstract public class VacationPolicy {
  public void accrueVacation() {
    caculateBseVacationHours();
    alterForLegalMinimums();
    applyToPayroll();
  }
  
  private void calculateBaseVacationHours() { /* ... */ };
  abstract protected void alterForLegalMinimums();
  private void applyToPayroll() { /* ... */ };
}
 
public class USVacationPolicy extends VacationPolicy {
  @Override protected void alterForLegalMinimums() {
    // 미국 최소 법정 일수를 사용한다.
  }
}
 
public class EUVacationPolicy extends VacationPolicy {
  @Override protected void alterForLegalMinimums() {
    // 유럽연합 최소 법정 일수를 사용한다.
  }
}
```
하위 클래스는 중복되지 않는 정보만 제공해 accrueVacation 알고리즘에서 빠진 '구멍'을 메운다.

#### 표현하라

자신이 이해하는 코드를 짜기는 쉽다. 
하지만 나중에 코드를 유지보수할 사람이 코드를 짜는 사람만큼이나 문제를 깊이 이해할 가능성은 희박하다.

- 좋은 이름을 선택한다 > 이해하기 쉬운 이름을 붙이자.
- 클래스와 메소드 크기를 가능한 줄인다.
- 표준명칭 = 업계에서 일반적으로 사용되는 명칭을 활용한다. 

예를 들어, 디자인 패턴은 의사소통과 표현력 강화가 주요 목적이다.
클래스가 COMMAND 나 VISITOR와 같은 표준 패턴을 사용해 구현된다면 클래스 이름에 패턴 이름을 넣어준다.

=> Decorator 패턴을 활용한 경우 *Decorator로 붙인다거나 Queue인 경우 *Queue로 짓는 식이다.

- 단위 테스트 케이스를 꼼꼼히 작성한다. 

테스트 케이스는 예제를 통해 코드를 검증하는 절차이다. 
잘 만든 테스트 케이스는 클래스의 기능을 짐작하기 쉽게 만든다.

무엇보다 가장 중요한 것은 표현하고자 하는 노력이다.
나중에 읽을 사람을 위해 약간의 시간을 투자하자.

#### 클래스와 메소드 수를 최소로 줄여라

중복을 제거하고, 의도를 표현하고, SRP를 준수한다는 기본적인 개념도 극단으로 치달으면 득보다 실이 많아진다.

즉, 무엇이든 끝까지 파헤치려는 노력은 보기 좋지만, 그 하나에만 매달리며 다른 것을 등한시한다면 득보다 실이 많을 수도 있다.

클래스와 메서드 크기를 줄이자고 조그만 클래스와 메서드를 수없이 만드는 사례도 없지 않다. 그래서 이 규칙은 함수와 클래스 수를 가능한 줄이라고 제안한다.

왜냐하면 정도를 넘어서서 클래수 수와 메소드 수가 셀 수 없을 만큼 많아지면 오히려 관리가 어려울 수도 있다.

가능한 독단적인 견해는 멀리하고 실용적인 방식을 택한다.

이 규칙은 간단한 설계 규칙 네 개 중 우선순위가 가장 낮다. 다시 말해, 클래스와 함수 수를 줄이는 작업도 중요하지만 상위 3가지 작업이 더 중요하다는 뜻이다.

#### 결론

경험을 대신할 단순한 개발 기법은 없다. 우수한 기법과 원칙을 단번에 활용할 수 있다.
