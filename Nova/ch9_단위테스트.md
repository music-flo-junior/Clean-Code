# 9장. 단위 테스트

기존에는 단위 테스트란 자기 프로그램이 '돌아간다'는 사실만 확인하는 일회성 코드에 불과했다. 클래스와 메서드를 공들여 구현한 후 임시 코드를 급조해 테스트를 수행했는데 대개는 간단한 드라이버 프로그램을 구현해 자신이 짠 프로그램을 수동으로 실행했다.

- `TDD(Test Driven Development)`
  - 테스트 주도 개발 (실제 코드를 짜기 전에 단위 테스트부터 짜라)
  - 애자일과 TDD 덕택에 단위 테스트를 자동화하는 프로그래머들이 이미 많아졌으며 점점 늘어나는 추세이다. 그러나 많은 프로그래머들이 제대로 된 테스트 케이스를 작성해야 한다는 좀 더 중요한 사실을 놓쳐버렸다.

## TDD 법칙 세 가지
#### 1. 첫 번째 법칙
`실패하는 단위 테스트를 작성할 때까지 실제 코드를 작성하지 않는다.`
#### 2. 두 번째 법칙
`컴파일을 실행하지 않으면서 실행이 실패하는 정도로만 단위 테스트를 작성한다.`
#### 3. 세 번째 법칙
`현재 실패하는 테스트를 통과할 정도로만 실제 코드를 작성한다.`

- 위 세 가지 규칙에 따르면 개발과 테스트가 대략 30초 주기로 묶인다.
- 테스트 코드와 실제 코드가 함께 나올뿐더러 테스트 코드가 실제 코드보다 불과 몇 초 전에 나온다.
- 이렇게 일하면 매일 수십 개, 매달 수백 개, 매년 수천 개에 달하는 테스트 케이스가 나오지만 실제 코드와 맞먹을 정도로 방대한 테스트 코드는 심각한 관리 문제를 유발하기도 한다.

## 깨끗한 테스트 코드 유지하기

### 테스트 코드를 깨끗하게 짜야 하는 이유
테스트 코드를 '지저분 해도 빨리'하기 위해 변수 이름을 신경쓰지 않고, 테스트 함수를 간결하거나 서술적이지 않고 테스트 코드를 잘 설계하거나 주의해서 분리하지 않으면 테스트를 하나 안하나 오십보 백보 혹은 짜지 않는 것보다 더 못하다.

`실제 코드가 진화하면 테스트 코드도 변해야 한다. 그런데 테스트 코드가 지저분하면 변경하기 어렵다. 테스트 코드가 복잡할 수록 실제 코드를 짜는 시간보다 테스트 케이스를 추가하는 시간이 더 걸린다.`
실제 코드를 변경해 기존 테스트 케이스가 실패하기 시작하면, 지저분한 코드로 인해 실패하는 테스트 케이스를 점점 더 통과시키기 어려워진다.
새 버전을 출시할 때마다 테스트 케이스를 유지보수하는 비용도 늘어나고 점차 개발자들의 큰 불만으로 자리잡는다.
하지만 테스트 케이스가 없으면 개발자는 자신이 수정한 코드가 제대로 도는지 확인할 방법이 없다. 이쪽을 수정해도 저쪽이 안전하다는 사실을 검증하지 못한다. 그래서 결함률은 높아지기 시작한다.

`의도하지 않은 결함 수가 많아지면 개발자는 변경을 주저한다. 변경하면 득보다 해가 크다 생각해 더 이상 코드를 정리하지 않는다. 그러면서 코드가 망가지기 시작한다.`
결국 테스트 슈트도 없고, 얼기설기 뒤섞인 코드에 좌절한 고객과, 테스트에 쏟아 부은 노력이 허사였다는 실망감만 남는다.

실패를 초래한 원인은 뭘까? 바로 테스트 코드를 막 짜도 좋다고 허용한 결정이었다. `테스트 코드는 실제 코드 못지 않게 중요하다.` 테스트 코드는 사고와 설계와 주의가 필요하다. 실제 코드 못지 않게 깨끗하게 짜야 한다.


### 테스트는 유연성, 유지보수성, 재사용성을 제공한다.
테스트 코드를 깨끗하게 유지하지 않으면 결국 잃어버린다. 테스트 케이스가 없으면 실제 코드를 유연하게 만드는 버팀목도 사라진다. `코드의 유연성, 유지보수성, 재사용성을 제공하는 버팀목이 바로 단위테스트다.`
`테스트 케이스가 있으면 변경이 두렵지 않다!` 테스트 케이스가 없다면 모든 변경이 잠정적인 버그다. 아키텍처가 아무리 유연하더라도, 설계를 아무리 잘 나눠놨더라도, 테스트 케이스가 없으면 개발자는 버그가 두려워 변경을 주저한다.

테스트 커버리자가 높을수록 공포는 줄어든다. 아키텍처가 부실한 코드나 설계가 모호하고 엉망인 코드라도 별다른 우려 없이 변경할 수 있다.
실제 코드를 점검하는 자동화된 단위 테스트는 설계와 아키텍처를 최대한 깨끗하게 보존하는 열쇠다. 테스트 케이스가 있으면 변경이 쉬워지기 때문이다.

따라서 테스트 코드가 지저분해지면 코드를 변경하는 능력이 떨어지며 코드 구조를 개선하는 능력도 떨어진다. 테스트 코드가 지저분할수록 실제 코드도 지저분해진다. 결국 테스트 코드를 잃어버리고 실제 코드도 망가진다.

### 깨끗한 테스트 코드
깨끗한 테스트 코드를 만들려면 세 가지가 필요하다. `가독성, 가독성, 가독성`이다. 
테스트 코드에서 가독성을 높이려면 명료성, 단순성, 풍부한 표현력이 필요하다. 테스트 코드는 최소한의 표현으로 많은 것을 나타내야 한다.

##### SerializedPageResponderTest.java
```java
public void testGetPageHieratchyAsXml() throws Exception
{
    crawler.addPage(root, PathParser.parse("PageOne"));
    crawler.addPage(root, PathParser.parse("PageOne.ChildOne"));
    crawler.addPage(root, PathParser.parse("PageTwo"));

    request.setResource("root");
    request.addInput("type", "pages");
    Responder responder = new SerializedPageResponder();
    SimpleResponse response = (SimpleResponse) responder.makeResponse(
        new FitNesseContext(root), request);
    
    String xml = response.getContext();

    assertEquals("test/xml", response.getContextType());
    assertSubString("<name>PageOne</name>", xml);
    assertSubString("<name>PageTwo</name>", xml);
    assertSubString("<name>ChildOne</name>", xml);
}

public void testGetPageHieratchyAsXmlDoesntContainSymbolicLinks() throws Exception {
    WikiPage pageOne = crawler.addPage(root, PathParser.parse("PageOne"));
    crawler.addPage(root, PathParser.parse("PageOne.ChildOne"));
    PageData data = pageOne.getData();
    WikiPageProperties properties = data.getProperties();
    WikiPageProperty symLinks = properties.set(SymbolicPage.PROPERTY_NAME);
    symLinks.set("SymPage", "PageTwo");
    pageOne.commit(data);

    request.setResource("root");
    request.addInput("type", "pages");
    Responder responder = new SerializedPageResponder();
    SimpleResponse response = (SimpleResponse) responder.makeResponse(new FitNesseContext(root), request);
    String xml = response.getContent();

    assertEquals("text/xml", response.getContentType());
    assertSubString("<name>PageOne</name>", xml);
    assertSubString("<name>PageTwo</name>", xml);
    assertSubString("<name>ChildOne</name>", xml);
    assertNotSubString("SymPage", xml);
}

public void testGetDataAsHtml() throws Exception {
    crawler.addPage(root, PathParser.parse("TestPageOne", "test page");

    request.setREsource("TestPageOne");
    request.addInput("type", "data");
    Responder responder = new SerializedPageResponder();
    SimpleResponse response = (SimpleResponse) responder.makeResponse(new FitNesseContext(root), request);
    String xml = response.getContext();

    assertEquals("text/xml", response.getContextType());
    assertSubString("test page", xml);
    assertSubString("<Test", xml);
}
```

- PathParser: 문자열을 pagePath 인스턴스로 변환. pagePath는 웹 로봇(crawler)이 사용하는 객체
  - 테스트와 무관하며 테스트 코드의 의도만 흐린다.
- responder 객체를 생성하는 코드와 response를 수집해 변환하는 코드 역시 잡음에 불과하다.
  

##### SerializedPageResponderTest.java (리팩터링한 코드)
```java
public void testGetPageHierarchyAsXml() throws Exception {
    makePages("PageOne", "PageOne.ChildOne", "PageTwo");

    submitRequest("root", "type:pages");

    assertResponseIsXml();
    assertResponseContains(
        "<name>PageOne</name>", "<name>PageTwo</name>", "<name>ChildOne</name>"
    );
}

public void testSymbolicLinksAreNotInXmlPageHierarchy() throws Exception {
    WikiPage page = makePage("PageOne");
    makePages("PageOne.ChildOne", "PageTwo");
    
    addLinkTo(page, "PageTwo", "SymPage");

    submitRequest("root", "type:pages");

    assertResponseIsXml();
    assertResponseContains(
        "<name>PageOne</name>", "<name>PageTwo</name>", "<name>ChildOne</name>"
    );
    assertResponseDoesNotContain("SymPage");
}

public void testGetDataAsXml() throws Exception {
    makePageWithContent("TestPageOne", "test page");
    submitRequest("TestPageOne", "type:data");

    assertResponseIsXml();
    assertResponseContains("test page", "<Test");
}
```

`BUILD-OPERATE-CHECK 패턴`이 위와 같은 테스트 구조에 적합하다. 
각 테스트는 명확히 세 부분으로 나눠진다.
1) 테스트 자료를 만든다.
2) 테스트 자료를 조작한다.
3) 조작한 결과가 올바른지 확인한다.

테스트 코드는 본론에 돌입해 진짜 필요한 자료 유형과 함수만 사용한다.

#### 도메인에 특화된 테스트 언어(DSL)

- 도메인 특화 언어 (DSL)로 테스트 코드를 구현하는 기법
  - 시스템 조작 API를 사용하는 대신 API 위에다 함수와 유틸리티를 구현한 후 그 함수와 유틸리티를 사용한다. (테스트 코드를 짜기도 읽기도 쉬워진다.)
  - 구현한 함수와 유틸리티는 테스트 코드에서 사용하는 특수 API가 된다.

#### 이중 표준

테스트 API 코드에 적용하는 표준은 실제 코드에 적용하는 표준과 확실히 다르다. 단순하고, 간결하고 표현력이 풍부해야 하지만, 실제 코드만큼 효율적일 필요는 없다. 실제 환경이 아니라 테스트 환경에서 돌아가는 코드이기 때문이다. 실제 환경과 테스트 환경은 요구사항이 판이하게 다르다.

##### EnvironmentControllerTest.java
```java
@Test
public void turnOnLoTempAlarmAtThreashold() throws Exception {
    hw.setTemp(WAY_TOO_COLD);
    controller.tic();
    assertTrue(hw.heaterState());
    assertTrue(hw.blowerState());
    assertFalse(hw.coolerState());
    assertFalse(hw.hiTempAlarm());
    assertTrue(hw.loTempAlarm());
}
```

##### EnvironmentControllerTest.java (리팩터링)
```java
@Test
public void turnOnLoTempAlarmThreshold() throws Exception {
    wayTooCold();
    assertEquals("HBchL", hw.getState());
}
```
```java
public String getState() {
    String state = "";
    state += heater ? "H" : "h";
    state += blower ? "B" : "b";
    state += coolear ? "C" : "c";
    state += hiTempAlarm ? "H" : "h";
    state += loTempAlarm ? "L" : "l";
    return state;
}
```

- HBchL: (heater, blower, coolear, hi-temp-alarm, lo-temp-alarm) / 대문자는 켜짐, 소문자는 꺼짐
- 테스트 코드이기 때문에 성능을 고려하지 않아도 되어 StringBuffer가 아닌 String을 직접 append하는 방식으로 사용했다. (코드의 깔끔함이 더 중요하다)

## 테스트 당 assert 하나

`테스트 함수마다 한 개념만 테스트하라`

##### addMonths() 메서드를 테스트하는 장황한 코드
```java
public void testAddMonths() {
    SerialDate d1 = SerialDate.createInstance(31, 5, 2004);

    SerialDate d2 = SerialDate.addMonths(1, d1);
    assertEquals(30, d2.getDayOfMonth());
    assertEquals(6, d2.getMonth());
    assertEquals(2004, d2.getYYYY());

    SerialDate d3 = SerialDate.addMonths(2, d1);
    assertEquals(31, d3.getDayOfMonth());
    assertEquals(7, d3.getMonth());
    assertEquals(2004, d3.getYYYY());
    
    SerialDate d4 = SerialDate.addMonths(1, SerialDate.addMonths(1, d1));
    assertEquals(30, d4.getDayOfMonth());
    assertEquals(7, d4.getMonth());
    assertEquals(2004, d4.getYYYY());
}
```

셋으로 분리한 테스트 함수는 각각 다음 기능을 수행한다.
- (5월처럼) 31일로 끝나는 마지막 날짜가 주어지는 경우
  - (6월처럼) 30일로 끝나는 한 달을 더하면 날짜는 30일이 되어야지 31일이 되어서는 안된다.
  - 두 달을 더하면 그리고 두 번째 달이 31일로 끝나면 날짜는 31일이 되어야 한다.
- (6월처럼) 30일로 끝나는 달의 마지막 날짜가 주어지는 경우
  - 31일로 끝나는 한 달을 더하면 날짜는 30일이 되어야지 31일이 되면 안된다.

위 테스트 코드는 여러 개념을 테스트하고 있다.
`개념 당 assert문 수를 최소로 줄여라`와 `테스트 함수 하나는 개념 하나만 테스트하라`


## F.I.R.S.T.

### 1. Fast (빠르게)
테스트는 빨리 돌아야 한다. 테스트가 느리면 자주 돌릴 엄두를 못 낸다. 자주 돌리지 않으면 초반에 문제를 찾아내 고치지 못한다. 코드를 마음껏 정리하지도 못한다. 결국 코드 품질이 망가지기 시작한다.

### 2. Indepenent (독립적으로)
각 테스트는 서로 의존하면 안 된다. 한 테스트가 다음 테스트가 실행될 환경을 준비해서는 안 된다. 각 테스트는 독립적으로 그리고 어떤 순서로 실행해도 괜찮아야 한다. 테스트가 서로 의존하면 하나가 실패할 때 나머지도 잇달아 실패하므로 원인을 진단하기 어려워지며 후반 테스트가 찾아내야 할 결함이 숨겨진다.

### 3. Repeatable (반복가능하게)
테스트는 어떤 환경에서도 반복 가능해야 한다. 실제 환경, QA 환경, 버스를 타고 집으로 가는 (네트워크에 연결되지 않은) 노트북 환경에서도 실행할 수 있어야 한다. 테스트가 돌아가지 않는 환경이 하나라도 있다면 테스트가 실패한 이유를 둘러댈 변명이 생긴다. 게다가 환경이 지원되지 않기에 테스트를 수행하지 못하는 상황에 직면한다.

### 4. Self-Validating (자가 검증하는)
테스트는 부울 값으로 결과를 내야 한다. 성공 아니면 실패다. 통과 여부를 알려고 로그 파일을 읽게 만들어서는 안 된다. 통과 여부를 보려고 텍스트 파일 두 개를 수작업으로 비교하게 만들어서도 안 된다. 테스트가 스스로 성공과 실패를 가늠하지 않는다면 판단이 주관적이 되며 지루한 수작업 평가가 필요하게 된다.

### 5. Timely (적시에)
테스트는 적시에 작성해야 한다. 단위 테스트는 테스트하려는 실제 코드를 구현하기 직전에 구현한다. 실제 코드를 구현한 다음에 테스트 코드를 만들면 실제 코드가 테스트하기 어렵다는 사실을 발견할지도 모른다. 어떤 실제 코드는 테스트하기 너무 어렵다고 판명날지 모른다. 테스트가 불가능하도록 실제 코드를 설계할지도 모른다.

## 결론
- 테스트 코드는 실제 코드만큼이나 프로젝트 건강에 중요하다.
- 테스트 코드는 실제 코드의 유연성, 유지보수성, 재사용성을 보존하고 강화한다.
- 테스트 코드는 지속적으로 깨끗하게 관리하자. 표현력을 높이고 간결하게 정리하자.
- 테스트 API를 구현해 도메인 특화 언어(Domain Specific Language, DSL)를 만들자. 그러면 테스트 코드를 짜기가 쉬워진다.
- 테스트 코드가 방치되어 망가지면 실제 코드도 망가진다. 테스트 코드를 깨끗하게 유지하자!