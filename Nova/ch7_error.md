# 7장. 오류 처리

## 오류 코드와 깨끗한 코드
오류 처리는 중요하다. 하지만 오류 처리 코드로 이해 프로그램 논리를 이해하기 어려워진다면 깨끗한 코드라 부르기 어렵다. 이 장에서는 고상하게 오류를 처리하는 기법과 고려사항 몇 가지를 소개한다.

## 1. 오류 코드보다 예외를 사용하라
이전에는 예외를 지원하지 않는 프로그래밍 언어가 많았다. 따라서 오류 플래스를 설정하거나 호출자에게 오류 코드를 반환하는 방법이 전부였다.

##### DeviceController.java (오류코드 사용)
```java
public class DeviceController {
    ...
    public void sendShutdown(){
        DeviceHandle handle = getHandle(DEV1);
        // 디바이스 상태를 점검한다.
        if (handle != DeviceHandle.INVALID) {
            // 레코드 필드에 디바이스 상태를 저장한다.
            retrieveDeviceRecord(handle);
            // 디바이스가 일시정지 상태가 아니라면 종료한다.
            if (record.getStatus() != DEVICE_SUSPENDED) {
                pauseDevice(handle);
                clearDeviceWorkQueue(handle);
                closeDevice(handle);
            } else {
                logger.log("Device suspended. Unable to shut down");
            }
        } else {
            logger.log("Invalid handle for: " + DEV1.toString());
        }
    }
    ...
}
```

위와 같은 방법을 사용하면 함수를 호출한 즉시 오류 코드를 확인해야 하기 때문에 호출자 코드가 복잡해진다. 불행히도 이 단계는 잊어버리기 쉽기 때문에 오류가 발생하면 예외를 던지는 편이 낫다.

##### DeviceController.java (예외 사용)
```java
public class DeviceController {
    ...
    public void sendShutDown() {
        try {
            tryToShutDown();
        } catch (DeviceShutDownError e) {
            logger.log(e);
        }
    }

    private void tryToShutDown() throws DeviceShutDownError {
        DeviceHandle handle = getHandle(DEV1);
        DeviceRecord record = retrieveDeviceREcord(handle);

        pauseDevice(handle);
        clearDeviceWorkQueue(handle);
        closeDevice(handle);
    }

    private DeviceHandle getHandle(DeviceID id) {
        ...
        throw new DeviceShutDownError("Invalid handle for: " + id.toString());
        ...
    }
    ...
}
```
앞서 뒤섞였던 디바이스를 종료하는 알고리즘과 오류를 처리하는 알고리즘을 분리하여 각 개념을 독립적으로 살펴보고 이해할 수 있다.

## 2. Try-Catch-Finally 문부터 작성하라

- try-catch-finally 문에서 try 블록에 들어가는 코드를 실행하면 어느 시점이든 실행이 중단된 후 catch 블록으로 넘어갈 수 있다.
- 어떤 면에서 try 블록은 트랜잭션과 비슷하다. try 블록에서 무슨 일이 생기든 catch 블록은 프로그램 상태를 일관성 있게 유지해야 한다.
- try-catch-finally 문으로 작성하면 try 블록에서 무슨일이 생기든지 호출자가 기대하는 상태를 정의하기 쉬워진다.

다음은 파일이 없으면 예외를 던지는지 알아보는 단위 테스트다.
```java
@Test(expected = StorageException.class)
public void retrieveSectionShouldThrowOnInvalidFileName(){
    sectionStore.retrieveSection("invalid - file");
}
```

단위 테스트에 맞춰 다음 코드를 구현했다.
```java
public List<RecordedGrip> retrieveSection(String sectionName) {
    // 실제로 구현할 때까지 비어있는 더미를 반환한다.
    return new ArrayList<RecordedGrip>();
}
```

그런데 코드가 예외를 던지지 않으므로 단위 테스트는 실패한다. 잘못된 파일 접근을 시도하게 구현을 변경하자. 아래 코드는 예외를 던진다.

```java
public List<RecordedGrip> retrieveSection(String sectionName) {
    try {
        FileInputStream stream = new FileInputStream(sectionname);
    } catch (Exception e) {
        throw new StorageException("retrieval error", e);
    }
    return new ArrayList<RecordedGrip>();
}
```

코드가 예외를 던지므로 이제는 테스트가 성공한다. 이 시점에서 리팩터링이 가능하다. catch 블록에서 예외 유형을 좁혀 실제로 FileInputStream 생성자가 던지는 FileNotFoundException을 잡아낸다.

```java
public List<RecordedGrip> retrieveSection(String sectionName) {
    try {
        FileInputStream stream = new FileInputStream(sectionName);
        stream.close();
    } catch (FileNotFoundException e) {
        throw new StorageException("retrieval error", e);
    }
    return new ArrayList<RecordedGrip>();
}
```

- try-catch 구조로 범위를 정했으므로 TDD를 사용해 필요한 나머지 논리를 추가한다. 
- 나머지 논리는 FileInputStream을 생성하는 코드와 close 호출문 사이에 넣으며 오류나 예외가 전혀 발생하지 않는다고 가정한다.
- 먼저 강제로 예외를 일으키는 테스트 케이스를 작성한 후 테스트를 통과하게 코드를 작성하는 방법을 권장한다. 그러면 자연스럽게 try 블록의 트랜잭션 범위부터 구현하게 되므로 범위 내에서 트랜잭션 본질을 유지하기 쉬워진다.

## 3. 미확인(unchecked) 예외를 사용하라


- 확인된(checked) 예외
  - 메서드를 선언할 때는 메서드가 반환할 예외를 모두 열거
  - 메서드가 반환하는 예외는 메서드 유형의 일부
  - 코드가 메서드를 사용하는 방식이 메서드 선언과 일치하지 않으면 아예 컴파일도 못한다

자바에서 처음으로 Checked Exception을 지원한 당시는 확인된 예외를 멋진 아이디어라 생각했다. 실제로도 확인된 예외는 몇가지 장점을 제공한다. 하지만 지금은 안정적인 소프트웨어를 제작하는 요소로 확인된 예외가 반드시 필요하지는 않다는 사실이 분명해졌다. 
  - c#, c++, 파이썬, 루비는 Checked Exception을 지원하지 않지만 안정적인 소프트웨어를 구현하기에 무리가 없다. 
  - 그리고 확인된 예외는 OCP(Open Closed Principle)을 위반한다. 
    - 메서드에서 확인된 예외를 던졌는데 catch 블록이 세 단계 위에 있다면 그 사이 메서드 모두가 선언부에 해당 예외를 정의해야 한다. 즉, 하위 단계에서 코드를 변경하면 상위 단계 메서드 선언부를 전부 고쳐야 한다는 말이다. 모듈과 관련된 코드가 전혀 바뀌지 않았더라도 (선언부가 바뀌었으므로) 모듈을 다시 빌드한 다음 배포해야 한다는 말이다.
    - 대규모 시스템에서 호출이 일어나는 방식을 상상해보라. 최상위 함수가 아래 함수를 호출한다. 아래 함수는 그 아래 함수를 호출한다. 단계를 내려갈수록 호출하는 함수 수는 늘어난다. 이제 최하위 함수를 변경해서 새로운 오류를 던진다고 가정하자. 확인된 오류를 던진다면 함수는 선언부에 throws 절을 추가해야 한다. 그러면 변경한 함수를 호출하는 함수 모두가 1) catch 블록에서 새로운 예외를 처리하거나 2) 선언부에 throw 절을 추가해야 한다. 결과적으로 최하위 단계에서 최상위 단계까지 연쇄적인 수정이 일어난다! throws 경로에 위치하는 모든 함수가 최하위 함수에서 던지는 예외를 알아야 하므로 캡슐화가 깨진다.

때로는 확인된 예외도 유용하다. 아주 중요한 라이브러리를 작성한다면 모든 예외를 잡아야 한다. 하지만 일반적인 애플리케이션은 의존성이라는 비용이 이익보다 크다.

## 4. 예외에 의미를 제공하라

- 예외를 던질 때는 전후 상황을 충분히 덧붙인다. 그러면 오류가 발생한 원인과 위치를 찾기가 쉬워진다. 자바는 모든 예외에 호출 스택을 제공한다. 하지만 실패한 코드의 의도를 파악하려면 호출 스택만으로 부족하다.
- 오류 메세지에 정보를 담아 예외와 함께 던진다. 실패한 연산 이름과 실패 유형도 언급한다. 어플리케이션이 로깅 기능을 사용한다면 catch 블록에서 오류를 기록하도록 충분한 정보를 넘겨준다.

## 5. 호출자를 고려해 예외 클래스를 정의하라

- 오류를 분류하는 방법은 수없이 많다. 오류가 발생한 위치로 분류가 가능하다.
    - 예를 들어, 오류가 발생한 컴포넌트로 분류한다. 아니면 유형으로도 분류가 가능하다. 예를 들어 디바이스 실패, 네트워크 실패, 프로그래밍 오류 등으로 분류한다. 하지만 어플리케이션에서 오류를 정의할 때 프로그래머에게 가장 중요한 관심사는 오류를 잡아내는 방법이 되어야 한다.

다음은 오류를 형편없이 분류한 사례다. 외부 라이브러리를 호출하는 try-catch-finally 문을 포함한 코드로, 외부 라이브러리가 던질 예외를 모두 잡아낸다.

```java
ACMEPort port = new ACMEPort(12);

try {
    port.open();
} catch (DeviceResponseException e) {
    reportPortError(e);
    logger.log("Device response exception", e);
} catch (ATM1212UnlockedException e) {
    reportPortError(e);
    logger.log("Unlock exception", e);
} catch (GMXError e) {
    reportPortError(e);
    logger.log("Device response exception", e);
} finally {
    ...
}
```

위 코드는 중복이 심하지만 그리 놀랍지 않다. 대다수 상황에서 우리가 오류를 처리하는 방식은 (오류를 일으킨 원인과 무관하게) 비교적 일정하다. 1) 오류를 기록한다. 2) 프로그램을 계속 실행해도 좋은지 확인한다.

위 경우 예외에 대응하는 방식이 예외 유형과 무관하게 거의 동일하다. 그래서 코드를 간결하게 고치기가 아주 쉽다. `호출하는 라이브러리 API를 감싸면서 예외 유형 하나를 반환`하면 된다.

```java
LocalPort port = new LocalPort(12);
try {
    port.open();
} catch (PortDeviceFailure e) {
    reportError(e);
    logger.log(e.getMessage(), e);
} finally {
    ...
}
```

여기서 LocalPort는 단순히 ACMEPort 클래스가 던지는 예외를 잡아 변환하는 `wrapper 클래스`일 뿐이다.

```java
public class LocalPort {
    private ACMEPort innerPort;

    public LocalPort(int portNumber) {
        innerPort = new ACMEPort(portNumber);
    }

    public void open() {
        try {
            innerPort.open();
        } catch (DeviceResponseException e) {
            throw new PortDeviceFailure(e);
        } catch (ATM1212UnlockedException e) {
            throw new PortDeviceFailure(e);
        } catch (GMXError e) {
            throw new PortDeviceFailure(e);
        }
    }
    ...
}
```

LocalPort 클래스처럼 ACMEPort를 감싸는 클래스는 매우 유용하다. `실제로 외부 API를 사용할 때는 wrapper 기법이 최선`이다. 

  1. 외부 API를 감싸면 외부 라이브러리와 프로그램 사이에서 `의존성이 크게 줄어든다.` 
  2. `나중에 다른 라이브러리로 갈아타도 비용이 적다.`
  3. wrapper 클래스에서 외부 API를 호출하는 대신 `테스트 코드`를 넣어주는 방법으로 프로그램을 테스트하기도 쉬워진다.
  4. 마지막 장점으로 감싸기 기법을 사용하면 `특정 업체가 API를 설계한 방식에 발목 잡히지 않는다.` 프로그램이 사용하기 편리한 API를 정의하면 그만이다. 위 예제에서 우리는 port 디바이스 실패를 표현하는 예외 유형 하나를 정의했는데, 그랬더니 프로그램이 훨씬 깨끗해졌다.

흔히 예외 클래스 하나만 있어도 충분한 코드가 많다. 예외 클래스에 포함된 정보로 오류를 구분해도 괜찮은 경우가 그렇다. 한 예외는 잡아내고 다른 예외는 무시해도 괜찮은 경우라면 여러 예외 클래스를 사용한다.

## 6. 정상 흐름을 정의하라

앞 절에서 충고한 지침을 충실히 따른다면 비즈니스 논리와 오류 처리가 잘 분리된 코드가 나온다. 코드 대부분이 깨끗하고 간결한 알고리즘으로 보이기 시작한다. 하지만 그러다 보면 오류 감지가 프로그램 언저리로 밀려난다. 외부 API를 감싸 독자적인 예외를 던지고, 코드 위에 처리기를 정의해 중단된 계산을 처리한다. 대개는 멋진 처리 방식이었지만, 때로는 중단이 적합하지 않은 때도 있다. 예제를 살펴보자. 다음은 비용 청구 어플리케이션에서 총계를 계산하는 허술한 코드다.

```java
try {
    MealExpenses expenses = expenseReportDAO.getMeals(employee.getID());
    m_total += expenses.getTotal();
} catch (MealExpensesNotFound e) {
    m_total += getMealPerdiem();
}
```

위에서 식비를 비용으로 청구했다면 직원이 청구한 식비를 총계에 더한다. 식비를 비용으로 청구하지 않았다면 일일 기본 식비를 총계에 더한다. 그런데 예외가 논리를 따라가기 어렵게 만든다. 특수 상황을 처리할 필요가 없다면 더 좋지 않을까? 그러면 코드가 훨씬 더 간결해지리라. 다음을 살펴보자.

```java
MealExpenses expenses = expenseReportDAO.getMeals(employee.getId());
m_total += expenses.getTotal();
```

위처럼 간결한 코드가 가능할까? 가능하다. ExpenseReportDAO를 고쳐 언제나 MealExpense 객체를 반환한다. 청구한 식비가 없다면 일일 기본 식비를 반환하는 MealExpense 객체를 반환한다.

```java
public class PerDiemMealExpenses implements MealExpenses {
    public int getTotal() {
        // 기본값으로 일일 기본 식비를 반환한다.
    }
}
```

이를 `특수 사례 패턴(Special case pattern)`이라 부른다. `클래스를 만들거나 객체를 조작해 특수 사례를 처리하는 방식`이다. 그러면 클라이언트 코드가 예외적인 상황을 처리할 필요가 없어진다. 클래스나 객체가 예외적인 상황을 캡슐화해서 처리하므로.

## 7. null을 반환하지 마라

오류 처리를 논하는 장이라면 우리가 흔히 저지르는 바람에 오류를 유발하는 행위도 언급해야 한다고 생각한다. 그 중 첫째가 `null을 반환하는 습관`이다. 한줄 건너 하나씩 null을 확인하는 코드로 가득한 어플리케이션을 지금까지 수도 없이 봤다. 다음이 한 예다.

```java
public void registerItem(Item item) {
    if (item != null) {
        ItemRegistry registry = peristentStore.getItemRegistry();
        if (registry != null) {
            Item existing = reistry.getItem(item.getId());
            if (existing.getBillingPeriod().hasRetailOwner()) {
                existing.register(item);
            }
        }
    }
}
```

위 코드는 나쁜 코드다! `null을 반환하는 코드는 일거리를 늘릴 뿐만 아니라 호출자에게 문제를 떠넘긴다.` 누구 하나라도 null 확인을 빼먹으면 애플리케이션이 통제 불능에 빠질지도 모른다. 위 코드에서 둘째 행에 null 확인이 빠졌다는 사실을 눈치챘는가? 만약 per-sistentStore가 null이라면 실행 시 어떤 일이 벌어질까? NullPointerException이 발생하리라. 위쪽 어디선가 NullPointerException을 잡을지도 모르고 아닐지도 모른다. 어느 쪽이든 나쁘다. 애플리케이션 저 아래서 날린 NullPointerException을 도대체 어떻게 처리하란 말인가?

위 코드는 null 확인이 누락된 문제라 말하기 쉽다. 하지만 실상은 `null 확인이 너무 많아 문제`다. 메서드에서 null을 반환하고 싶은 유혹이 든다면 그 대신 예외를 던지거나 특수 사례 객체를 반환한다. 사용하려는 외부 API가 null을 반환한다면 감싸기 메서드를 구현해 예외를 던지거나 특수 사례 객체를 반환하는 방식을 고려한다.

많은 경우에 특수 사례 객체가 손쉬운 해결책이다. 다음과 같은 코드를 생각해보자.

```java
List<Employee> employee = getEmployees();
if (employee != null) {
    for (Employee e : employees) {
        totalPay += e.getPay();
    }
}
```

위에서 getEmployees는 null도 반환한다. 하지만 반드시 null을 반환할 필요가 있을까? getEmployees를 변경해 빈 리스트를 반환한다면 코드가 훨씬 깔끔해진다.

```java
List<Employee> employees = getEmployees();
for (Employee e : employees) {
    totalPay += e.getPay();
}
```

다행스럽게 자바에는 `Collections.emptyList()`가 있어 미리 정의된 읽기 전용 리스트를 반환한다. 우리 목적에 적합한 리스트다.

```java
public List<Employee> getEmployees() {
    if (... 직원이 없다면...)
        return Collections.emptyList();
}
```

이렇게 코드를 변경하면 코드도 깔끔해질뿐더러 NullPointerException이 발생할 가능성도 줄어든다.

## 8. null을 전달하지 마라

`메서드에서 null을 반환하는 방식도 나쁘지만 메서드로 null을 전달하는 방식은 더 나쁘다.` 정상적인 인수로 null을 기대하는 API가 아니라면 메서드로 null을 전달하는 코드는 최대한 피한다.

예제를 살펴보면 이유가 드러난다. 다음은 두 지점 사이의 거리를 계산하는 간단한 메서드이다.

```java
public class MetricsCalculator {
    public double xProjection(Point p1, Point p2) {
        return (p2.x - p1.x) * 1.5;
    }
    ...
}
```
누가 인수로 null을 전달하면 어떤 일이 벌어질까?

calculator.xProjection(null, new Point(12, 13));

당연히 NullPointerException이 발생한다.

어떻게 고치면 좋을까? 다음과 같이 새로운 예외 유형을 만들어 던지는 방법이 있다.

```java
public class MetricsCalculator {
    public double xProjection(Point p1, Point p2) {
        if (p1 == null || p2 == null) {
            throw InvalidArgumentException("Invalid argument for MetricsCalculator.xProjection");
        }
        return (p2.x - p1.x) * 1.5;
    }
}
```

위 코드가 원래 코드보다 나을까? NullPointException보다는 조금 나을지도 모르겠다. 하지만 위 코드는 InvalidArgumentException을 잡아내는 처리기가 필요하다. 처리기는 InvalidArgumentException 예외를 어떻게 처리해야 좋을까? 좋은 방법이 있을까?

다음은 또 다른 대안이다. `assert문을 사용하는 방법`도 있다.

```java
public class MetricsCalculator {
    public double xProjection(Point p1, Point p2) {
        assert p1 != null : "p1 should not be null";
        assert p2 != null : "p2 should not be null";
        return (p2.x - p1.x) * 1.5;
    }
}
```

문서화가 잘 되어 코드 읽기는 편하지만 문제를 해결하지는 못한다. 누군가 null을 전달한다면 여전히 실행 오류가 발생한다. 대다수 프로그래밍 언어는 호출자가 실수로 넘기는 null을 적절히 처리하는 방법이 없다. 그렇다면 애초에 null을 넘기지 못하도록 금지하는 정책이 합리적이다. 즉, 인수로 null이 넘어오면 코드에 문제가 있다는 말이다. 이런 정책을 따르면 그만큼 부주의한 실수를 저지를 확률도 작아진다.

## 결론

깨끗한 코드는 읽기도 좋아야 하지만 안정성도 높아야 한다. 이 둘은 상충하는 목표가 아니다. 오류 처리를 프로그램 논리와 분리해 독자적인 사안으로 고려하면 튼튼하고 깨끗한 코드를 작성할 수 있다. 오류 처리를 프로그램 논리와 분리하면 독립적인 추론이 가능해지며 코드 유지보수성도 크게 높아진다.

## 참고자료
- 클린코드 애자일 소프트웨어 장인 정신