## 2장. 의미 있는 이름

소프트웨어에서 이름은 어디나 쓰인다. 이름을 잘 지으면 여러모로 편하다. 이 장에서는 이름을 잘 짓는 간단한 규칙을 몇 가지 소개한다.

### 1. 의도를 분명히 밝혀라
- 의도가 분명한 이름은 정말로 중요하다.
- 변수나 함수 그리고 클래스 이름은 `존재 이유`와 `수행 기능`, `사용 방법`을 답해야 한다.

#### 1.1 변수

##### 나쁜 예시
```java
int p; // 사람 수 (단위: 명)
```

이름 d는 아무 의미도 드러나지 않는다. 경과 시간이나 날짜라는 느낌이 안든다. 측정하려는 값과 단위를 표현하는 이름이 필요하다.

##### 좋은 예시
```java
int patientsCount;
int loginFailUserCount;
```

#### 1.2 코드

##### 나쁜 예시
```java
public List<int[]> getThem(){
    List<int[]> list1 = new ArrayList<int[]>();
    for (int[] x : theList)
        if (x[0] == 4)
            list1.add(x);
    return list1;
}
```
##### 좋은 예시
```java
public List<int[]> getFlaggedCells(){
    List<int[]> flaggedCells = new ArrayList<int[]>();
    for (int[] cell : gameBoard)
        if (cell[STATUS_VALUE] == FLAGGED)
            flaggedCells.add(cell);
    return flaggedCells;
}
```

##### 더 좋은 예시
```java
public List<Cell> getFlaggedCells(){
    List<Cell> flaggedCells = new ArrayList<Cell>();
    for (Cell cell : gameBoard)
        if (cell.isFlagged())
            flaggedCells.add(cell);
    return flaggedCells;
}
```

- 코드에 정보 제공을 충분히 하자

#### 2. 그릇된 정보를 피하라

- 나름대로 널리 쓰이고 있는 의미가 있는 단어를 다른 의미로 사용하면 안된다.
- 여러 계정을 그룹으로 묶을 때 실제 List가 아니라면 accountList와 같이 명명하지 않는다.
- 서로 흡사한 이름을 사용하지 않도록 주의한다.
- 유사한 개념은 유사한 표기법을 사용한다.
- 소문자 L이나 대문자 O는 숫자 1이나 0과 혼동하기 쉬우니 주의한다.

#### 3. 의미 있게 구분하라

- 동일 범위 안에서 다른 두 개념에 같은 이름을 사용하지 못한다고 철자를 틀리게 적지 말자
- 연속된 숫자를 덧붙이거나 불용어(noise word)를 추가하는 방식은 적절하지 못하다. 이름이 달라야 한다면 의미도 달라져야 한다.

##### 나쁜 예시 (연속된 숫자)
```java
public static void copyChars(char a1[], char a2[]) {
    for (int i = 0; i< a1.length; i++) {
        a2[i] = a1[i];
    }
}
```

##### 좋은 예시 (연속된 숫자 -> 의미 있는 이름)
```java
public static void copyChars(char source[], char destination[]) {
    for (int i = 0; i< source.length; i++) {
        destination[i] = source[i];
    }
}
```

#### 4. 발음하기 쉬운 이름을 사용하라

##### 나쁜 예시
```java
class DtaRcrd102 {
    private Date genymdhms;
    private Date modymdhms;
    private final String pszqint = "102";
    /* ... */
};
```
##### 좋은 예시
```java
class Customer {
    private Date generationTimeStamp;
    private Date modificationTimeStamp;
    private final String recordId = "102";
};
```

#### 5. 검색하기 쉬운 이름을 사용하라

- 문자 하나를 사용하는 이름과 상수는 텍스트 코드에서 쉽게 눈에 띄지 않는다.
    - `7` -> `MAX_CLASSES_PER_STUDENT`

#### 6. 인코딩을 피하라
- 인코딩한 이름은 발음하기 어려우며 오타가 생기기도 쉽다.
#### 6.1 헝가리식 표기법 (타입을 변수 이름에 쓰는 방식)
- 자바는 변수 이름에 타입을 인코딩할 필요가 없다.

##### 나쁜 예시
```java
PhoneNumber phoneString; // 타입이 바뀌어도 이름은 바뀌지 않는다.
```

#### 6.2 멤버 변수 접두어
- 이제 멤버 변수에 m_이라는 접두어를 붙일 필요도 없다.
##### 나쁜 예시
```java
public class Part {
    private String m_dec; // 설명 문자열
    void setName(PString name) {
        m_dsc = name;
    }
}
```

##### 좋은 예시
```java
public class Part {
    private String description;
    void setDescription(String description) {
        this.description = description;
    }
}
```

#### 6.3 인터페이스 클래스와 구현 클래스
- 인터페이스 클래스와 구현 클래스의 관계라면 클래스 사용자가 쉽게 접근할 수 있는 이름으로 하자.
    - `ShapeFactory` implements `IShapeFactory` 보다
    - `ShapeFactoryImpl` implements `ShapeFactory`가 낫다.

#### 7. 자신의 기억력을 자랑하지 마라
- 자신이 아는 이름으로 변환하는 것이 아니라 명료한 단어를 선택하자

#### 8. 클래스 이름
- 클래스 이름과 객체 이름은 명사나 명사구가 적합하다.
- Customer, WikiPage, Account, AddressParser 등은 좋은 예다.
- Manager, Processor, Date, Info와 같은 단어는 피하자
- 동사는 사용하지 않는다.

#### 9. 메서드 이름
- 메서드 이름은 동사나 동사구가 적합하다.
- postPayment, deletePage, save 등이 좋은 예다
- 접근자, 변경자, 조건자는 javabean 표준에 따라 값 앞에 get, set, is를 붙인다.
- 생성자를 중복정의할 때는 정적 팩토리 메서드를 사용한다.
    - 좋은 예시) `Complex fulcrumPoint = Complex.FromRealNumber(23.0);`
    - 나쁜 예시) `Complex fulcrumPoint = new Complex(23.0);`
- 생성자 사용을 제한하려면 해당 생성자를 private으로 선언한다.

#### 10. 기발한 이름은 피하라
- 특정 문화에서만 사용하는 농담은 피하고 의도를 분명하고 솔직하게 표현하자

#### 11. 한 개념에 한 단어만 사용하라
- 추상적인 개념 하나에 단어 하나를 선택해 이를 고수한다.
    - 예) 똑같은 메서드를 클래스마다 fetch, retrieve, get으로 제각각 부르면 혼란스럽다. 
    - 마찬가지로 동일 코드에 controller, manager, driver를 섞어 쓰면 혼란스럽다.
- 일관성 있게 단어를 사용하자.

#### 12. 말장난을 하지 마라
- 한 단어를 두 가지 목적으로 사용하지 마라.

#### 13. 해법 영역에서 가져온 이름을 사용하라
- 코드를 읽는 사람도 프로그래머이므로 전산 용어, 알고리즘 이름, 패턴 이름, 수학 용어 등을 사용해도 괜찮다.
- 기술 개념에는 기술 이름이 가장 적합한 선택이다.

#### 14. 문제 영역에서 가져온 이름을 사용하라
- 적절한 '프로그래밍 용어'가 없다면 문제 영역(도메인 관련 전문 이름)에서 이름을 가져온다.
- 문제 영역 개념과 관련이 깊은 코드라면 문제 영역에서 이름을 가져와야 한다.

#### 15. 의미 있는 맥락을 추가하라
- 클래스, 함수, 이름 공간에 넣어 맥락을 부여한다. 모든 방법이 실패하면 마지막 수단으로 접두어를 붙인다.

##### 맥락이 불분명한 변수
```java
private void printGuessStatistics(char candidate, int count) {
    String number;
    String verb;
    String pluralModifier;
    if (count == 0) {
        number = "no";
        verb = "are";
        pluralModifier = "s";
    } else if (count == 1) {
        number = "1";
        verb = "is";
        pluralModifier = "";
    } else {
        number = Integer.toString(count);
        verb = "are";
        pluralModifier = "s";
    }
    String guessMessage = String.format( 
        "There %s %s %s%s", verb, number, candidate, pluralModifier);
    print(guessMessage);
}
```

##### 맥락이 분명한 변수
```java
public class GuessStatisticsMessage {
    private String number;
    private String verb;
    private String pluralModifier;

    public String make(char candidate, int count) {
        createPluralDependentMessageParts(count);
        return String.format("There %s %s %s%s", verb, number, candidate, pluralModifier);
    }

    private void createPluralDependentMessageParts(int count) {
        if (count == 0) {
            thereAreNoLetters();
        } else if (count == 1) {
            thereIsOneLetter();
        } else {
            thereAreManyLetters(count);
        }
    }

    private void thereAreManyLetters(int count) {
        number = Integer.toString(count);
        verb = "are";
        pluralModifier = "s";
    }

    private void thereIsOneLetter(){
        number = "1";
        verb = "is";
        pluralModifier = "";
    }

    private void thereAreNoLetter(){
        number = "no";
        verb = "are";
        pluralModifier = "s";
    }
}
```

#### 16. 불필요한 맥락을 없애라
- 일반적으로 짧은 이름이 긴 이름보다 좋다. 단, 의미가 분명한 경우에 한해서다. 이름에 불필요한 맥락을 추가하지 않도록 주의한다.
    - accountAddress와 customerAddress는 Address 클래스 인스턴스로는 좋은 이름이나 클래스 이름으로는 적합하지 못하다. Address는 클래스 이름으로 적합하다.
    - 포트 주소, MAC 주소, 웹 주소를 구분해야 한다면 PostalAddress, MAC, URI라는 이름도 괜찮다.

#### 마치면서
- 좋은 이름을 선택하려면 설명 능력이 뛰어나야 하고 문화적인 배경이 같아야 한다.
- 사람들이 이름을 바꾸지 않으려는 이유 하나는 다른 개발자가 반대할까 두려워서다. 우리들 생각은 다르다. 오히려 (좋은 이름으로 바꿔주면) 반갑고 고맙다.
- 이장에서 소개한 규칙 몇 개를 적용해 코드 가독성이 높아지는지 살펴보자

### 참고 자료
- Clean Code
