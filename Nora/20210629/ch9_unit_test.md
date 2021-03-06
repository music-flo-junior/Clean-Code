# 단위 테스트 (Unit Test)

### TDD 법칙 세 가지
TDD의 세가지 법칙을 을 지키고 30초 주기로 묶어서 개발을 진행한다.

1. 실패하는 단위 테스트 를 작성할 때까지 실제 코드를 작성하지 않는다.
2. 컴파일은 실패하지 않으면서 실행이 실패하는 정도로만 단위 테스트를 작성한다.
3. 현재 실패하는 테스트를 통과할 정도로만 실제 코드를 작성한다.

테스트 코드와 실제 코드가 함께 나올뿐더러 테스트 코드가 실제 코드보다 불과 몇 초 전에 나온다.

이렇게 일하면 실제 코드를 사실상 전부 테스트하는 테스트 케이스가 나오게 된다. 
이는 심각한 관리 문제를 유발하기도 한다.

### 깨끗한 테스트 코드 유지하기
테스트 코드가 깨끗하지 않을 경우 아래와 같은 일이 일어날 수 있다.

1. 새 버전을 출시할 때마다 팀이 테스트 케이스를 유지하고 보수하는 비용이 늘어남.
2. 개발자 사이에서 테스트 코드가 가장 큰 불만으로 자리 잡는다.
3. 관리자가 예측값이 너무 큰 이유를 물어보면 팀은 테스트 코드를 비난한다.
4. 테스트 슈트를 폐기한다.
5. 테스트 코드의 부재로 개발자는 수정한 코드가 제대로 도는지 확인할 방법이 없다.
6. 결함율이 높아진다.
7. 의도하지 않은 결함 수가 많아지면 개발자는 변경을 주저한다.
8. 변경하면 득보다 해가 크다 생각해 더 이상 코드를 정리하지 않는다.
9. 코드가 망가진다.
10. 테스트 슈트도 없고 얼기설기 뒤섞인 코드에 좌절한 고객과 테스트에 쏟아 부은 노력이 허사였다는 실망감만 남는다.

테스트 코드는 실제 코드 못지 않게 중요하다. 실제 코드 못지 않게 깨끗하게 짜야 합니다.

### 테스트는 유연성, 유지보수성, 재사용성을 제공한다.

테스트 코드를 깨끗하게 유지하지 않으면 결국은 잃어버리게 된다.

테스트 코드가 지저분하면 코드를 변경하는 능력이 떨어지며 코드 구조를 개선하는 능력도 떨어짐
테스트 케이스가 없으면 실제 코드를 유연하게 만드는 버팀목도 사라지게 된다.

테스트 케이스가 없으면 모든 변경이 잠정적인 버그이다.

즉, 테스트 케이스는 변경을 쉽게 할 수 있도록 도와주어 개발에 유연성, 유지보수성, 재사용성을 제공한다.

### 깨끗한 테스트 코드

가독성은 실제 코드보다 테스트 코드에 더더욱 중요하다.

> 명료성, 단순성, 풍부한 표현력

테스트 코드는 최소의 표현으로 많은 것을 나타내야 한다.

```java
public void testGetPageHieratchyAsXml() throws Exception {
	crawler.addPage(root, PathParser.parse("PageOne"));
	crawler.addPage(root, PathParser.parse("PageOne.ChildOne"));
	crawler.addPage(root, PathParser.parse("PageTwo"));

	request.setResource("root");
	request.addInput("type", "pages");
	Responder responder = new SerializedPageResponder();
	SimpleResponse response =
		(SimpleResponse) responder.makeResponse(new FitNesseContext(root), request);
	String xml = response.getContent();

	assertEquals("text/xml", response.getContentType());
	assertSubString("<name>PageOne</name>", xml);
	assertSubString("<name>PageTwo</name>", xml);
	assertSubString("<name>ChildOne</name>", xml);
}

public void testGetPageHieratchyAsXmlDoesntContainSymbolicLinks() throws Exception {
	WikiPage pageOne = crawler.addPage(root, PathParser.parse("PageOne"));
	crawler.addPage(root, PathParser.parse("PageOne.ChildOne"));
	crawler.addPage(root, PathParser.parse("PageTwo"));

	PageData data = pageOne.getData();
	WikiPageProperties properties = data.getProperties();
	WikiPageProperty symLinks = properties.set(SymbolicPage.PROPERTY_NAME);
	symLinks.set("SymPage", "PageTwo");
	pageOne.commit(data);

	request.setResource("root");
	request.addInput("type", "pages");
	Responder responder = new SerializedPageResponder();
	SimpleResponse response =
		(SimpleResponse) responder.makeResponse(new FitNesseContext(root), request);
	String xml = response.getContent();

	assertEquals("text/xml", response.getContentType());
	assertSubString("<name>PageOne</name>", xml);
	assertSubString("<name>PageTwo</name>", xml);
	assertSubString("<name>ChildOne</name>", xml);
	assertNotSubString("SymPage", xml);
}

public void testGetDataAsHtml() throws Exception {
	crawler.addPage(root, PathParser.parse("TestPageOne"), "test page");

	request.setResource("TestPageOne"); request.addInput("type", "data");
	Responder responder = new SerializedPageResponder();
	SimpleResponse response =
		(SimpleResponse) responder.makeResponse(new FitNesseContext(root), request);
	String xml = response.getContent();

	assertEquals("text/xml", response.getContentType());
	assertSubString("test page", xml);
	assertSubString("<Test", xml);
}
```

좀 더 테스트에 관련된 중요한 내용이 보이도록 작성하는 것이 좋다.
여기서는 BUILD-OPERATE-CHECK 패턴이 적합하다.

1. 테스트 자료를 만든다.
2. 테스트 자료를 조작한다.
3. 조작한 결과가 올바른지 확인한다.

```java
public void testGetPageHierarchyAsXml() throws Exception {
	makePages("PageOne", "PageOne.ChildOne", "PageTwo");

	submitRequest("root", "type:pages");

	assertResponseIsXML();
	assertResponseContains(
		"<name>PageOne</name>", "<name>PageTwo</name>", "<name>ChildOne</name>");
}

public void testSymbolicLinksAreNotInXmlPageHierarchy() throws Exception {
	WikiPage page = makePage("PageOne");
	makePages("PageOne.ChildOne", "PageTwo");

	addLinkTo(page, "PageTwo", "SymPage");

	submitRequest("root", "type:pages");

	assertResponseIsXML();
	assertResponseContains(
		"<name>PageOne</name>", "<name>PageTwo</name>", "<name>ChildOne</name>");
	assertResponseDoesNotContain("SymPage");
}

public void testGetDataAsXml() throws Exception {
	makePageWithContent("TestPageOne", "test page");

	submitRequest("TestPageOne", "type:data");

	assertResponseIsXML();
	assertResponseContains("test page", "<Test");
}
```

아래는 테스트를 수정하면서 코드를 추가하는 예시입니다.

```java
// Bad
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

// Good
@Test
public void turnOnLoTempAlarmAtThreshold() throws Exception {
  wayTooCold();
  assertEquals("HBchL", hw.getState()); 
}
```

### 테스트 당 assert 하나
assert 문을 단 하나만 사용 해야 한다고 주장하는 학파가 있다.

```java
public void testGetPageHierarchyAsXml() throws Exception { 
	givenPages("PageOne", "PageOne.ChildOne", "PageTwo");

	whenRequestIsIssued("root", "type:pages");

	thenResponseShouldBeXML(); 
}

public void testGetPageHierarchyHasRightTags() throws Exception { 
	givenPages("PageOne", "PageOne.ChildOne", "PageTwo");

	whenRequestIsIssued("root", "type:pages");

	thenResponseShouldContain(
		"<name>PageOne</name>", "<name>PageTwo</name>", "<name>ChildOne</name>"
	); 
}
```

given-when-then이라는 관례를 사용해서 테스트 코드를 더 읽기 쉽게 만든 것을 볼 수 있다.

하지만 위와 같이 1개의 assert문을 주도록 강제할 경우에는 중복되는 코드가 많아질 위험이 있다.

> TEMPLATE METHOD 패턴을 사용하여 해결 할 수 있다.
>
given/when 부분을 부모 클래스에 두고
then 부분을 자식 클래스에 두는 방식으로
하지만 공통 부분을 별도의 method로 끄집어 내는데 들어가는 노력이 더 클 수 있다.

결국, 필요한 경우에는 assert를 여러개를 두되 assert 문 개수는 최대한 줄이려고 노력하는 것이 좋습니다.

> 테스트 당 개념 하나

위의 테스트 당 assert 하나 보다는 테스트 함수마다 한 개념만 테스트하라라는 규칙이 더 낫다.

잡다한 개념을 연속으로 테스트하는 것은 피해야한다.

아래 코드는 3가지 개념을 한 테스트 함수에서 테스트하는 예시다.

이러한 경우 각 절이 존재하는 이유와 테스트 하는 개념을 다 이해해야 해서 좋지 않다.

```java
/**
 * addMonth() 메서드를 테스트하는 장황한 코드
 */
public void testAddMonths() {
	SerialDate d1 = SerialDate.createInstance(31, 5, 2004);
	
	// (6월처럼) 30일로 끝나는 한 달을 더하면 날짜는 30일이 되어야지 31일이 되어서는 안된다.
	SerialDate d2 = SerialDate.addMonths(1, d1); 
	assertEquals(30, d2.getDayOfMonth()); 
	assertEquals(6, d2.getMonth()); 
	assertEquals(2004, d2.getYYYY());

	// 두 달을 더하면 그리고 두 번째 달이 31일로 끝나면 날짜는 31일이 되어야 한다.
	SerialDate d3 = SerialDate.addMonths(2, d1); 
	assertEquals(31, d3.getDayOfMonth()); 
	assertEquals(7, d3.getMonth()); 
	assertEquals(2004, d3.getYYYY());

	// 31일로 끝나는 한 달을 더하면 날짜는 30일이 되어야지 31일이 되어서는 안된다.
	SerialDate d4 = SerialDate.addMonths(1, SerialDate.addMonths(1, d1)); 
	assertEquals(30, d4.getDayOfMonth());
	assertEquals(7, d4.getMonth());
	assertEquals(2004, d4.getYYYY());
}
```

결국 테스트 함수와 assert문에 관련된 규칙은 아래 2개가 가장 좋다고 하겠다.

> 개념 당 assert문 수를 최소로 줄여라

> 테스트 함수 하나는 개념 하나만 테스트하라

### F.I.R.S.T

깨끗한 테스트는 다음 다섯 가지 규칙을 따르는데, 각 규칙에서 첫 글자를 따오면 FIRST 가 된다.

1.빠르게(Fast) : 테스트는 빨라야 한다.

테스트는 빨리 돌아야 한다.

테스트가 느리면 자주 돌릴 엄두를 못낸다.

자주 돌리지 않으면 초반에 문제를 찾아내 고치지 못한다.

코드를 마음껏 정리하지도 못한다.

결국 코드 품질이 망가지기 시작한다.

2.독립적으로(Independent) : 각 테스트는 서로 의존하면 안 된다.

한 테스트가 다음 테스트가 실행될 환경을 준비해서는 안된다.

각 테스트는 독립적으로 그리고 어떤 순서로 실행해도 괜찮아야 한다.

테스트가 서로에게 의존하면 하나가 실패할 때 나머지도 잇달아 실패하므로 원인을 진단하기 어려워지며 후반 테스트가 찾아내야 할 결함이 숨겨진다.

3.반복가능하게(Repeatable) : 테스트는 어떤 환경에서도 반복 가능해야 한다.

실제 환경, QA 환경, 버스를 타고 집으로 가는 길에 사용하는 노트북 환경에서도 실행할 수 있어야 한다.

테스트가 돌아가지 않는 환경이 하나라도 있다면 테스트가 실패한 이유를 둘러댈 변명이 생긴다.

환경이 지원되지 않기에 테스트를 수행하지 못하는 상황에 직면할 수 있다.

4.자가검증하는(Self-Validating) : 테스트는 Bool 값으로 결과를 내야 한다. 성공 아니면 실패다.

통과 여부를 알려고 로그 파일을 읽게 만들어서는 안된다.

통과 여부를 보려고 텍스트 파일 두 개를 수작업으로 비교하게 만들어서도 안 된다.

테스트가 스스로 성공과 실패를 가늠하지 않는다면 판단은 주관적이 되며 지루한 수작업 평가가 필요하게 된다.

5.적시에(Timely) : 테스트는 적시에 작성해야 한다.

단위 테스트는 테스트하려는 실제 코드를 구현하기 직전에 구현한다.

실제 코드를 구현한 다음에 테스트 코드를 만들면 실제 코드가 테스트하기 어렵다는 사실을 발견할지도 모른다.

어떤 실제 코드는 테스트하기 너무 어렵다고 판명날지도 모른다.

테스트가 불가능하도록 실제 코드를 설계할지도 모른다.

### 결론

테스트 코드가 방치되어 망가지면 실제 코드도 망가지기 때문에 꼭 반드시 테스트 코드를 깨끗하게 유지하자.