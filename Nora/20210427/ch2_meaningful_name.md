#의미있는 이름(Meaningful Name)

- 변수, 함수, 인수와 클래스, 소스파일, 소스파일이 담긴 디렉터리, jar 파일, war 파일, ear 파일에도 이름을 붙인다.

- 이름을 잘 지으면 여러모로 편하다. 이름을 잘 짓는 간단한 규칙 몇가지를 알아보자.

## 1. 의도를 분명이 밝혀라

변수나 함수 클래스 이름은 의도가 분명한 이름이어야 한다.

- 변수(함수, 클래스)의 존재 이유
- 변수(함수, 클래스)의 수행 기능
- 변수(함수, 클래스)의 사용 방법

// 변경 전 <br/>

`int d; //경과 시간 (단위: 날짜);`


// 변경 후 <br/>

`int daysSinceCreation;`

코드의 단순성이 아닌 코드의 함축성을 고려해야한다 -> 코드 맥락을 코드 자체에 명시적으로 드러나야한다.

코드 맥락 정보 제공 방법 : 개념에 이름을 붙인다

// 변경 전 <br/>

    public List<int[]> getThem(){
        List<int[]> list1 = new ArrayList<int[]>();
            for(int[] x : the List)
                if(x[0] == 4)
                    list1.add(x);
       return list1;
    }

// 변경 후 <br/>

각 칸을 Cell 클래스로 표현

Cell 필드 값 flagged 확인 메서드로 변경


    public List<Cell> getFlaggedCells{
        List<Cell> flaggedCells = new ArrayList<Cell>();
            for(Cell cell: gameBoard)
                if(cell.isFlagged())
                    flaggedCells.add(cell);
       return flaggedCells;
    }

##2. 그릇된 정보를 피하라

여러 계정을 그룹으로 묶을 때, 실제 List가 아니면 accountList라 명명하지 않는다.

accountGroup, bunchOfAccounts, Accounts 서로 흡사한 이름을 사용하지 않도록한다.

XYZControllerForEfficientHandlingOfStrings <-> XYZControllerForEfficientStorageOfStrings

이름만 보고 객체를 선택할 수 있게, 유사한 개념은 유사한 표기법을 사용해 일관성을 준다.

주의 ) l O 1 0 조심히 사용하기!

##3. 의미있게 구분해라

연속된 숫자를 덧붙인 이름은 아무런 정보를 제공하지 못한다.

    public static void copyChars(char a1[], char a2[]){ // source, destination으로 고치자
        for (int i =0; i < a1.length; i++){
            a2[i] = a1[i];
        }
    }

불용어를 추가한 이름은 아무런 정보도 제공하지 않는다. 

불용어는 중복이기 때문이다.

- Product; ProductInfo; ProductData // 의미가 불분명하다.
- NameString; Name;    
- Customer; CustomerObejct;     // 차이를 알 수가 없다.

읽는 사람이 차이를 알 수 있도록 이름을 지어라

- moneyAmount; money;
- customerInfo; customer;
- accountData; account;
- theMessage; message;
 
## 4. 발음하기 쉬운 이름을 사용하라

발음하기 쉬운 단어를 사용할 때 대화가 편해진다.

// 변경 전
- private Date genymdhms;
- private Date modymdhms;
- private final String pszqint = "102";

// 변경 후
- private Date generationTimeStamp;
- private Date modificationTimeStamp;
- private final String recordId = "102";
 
## 5. 검색하기 쉬운 이름을 사용하라

문자 하나를 사용하는 이름이나 상수는 검색이 어렵다.

검색의 관점에서 긴 이름이 짧은 이름보다 좋다.

간단한 메서드의 로컬변수 : 한 문자 (이름 길이는 범위 크기에 비례)

상수의 의미를 나타내는 변수로 바꾸어주자.

// 변경 전

    for(int j=0; j>34; j++){
        s += (t[j]*4)/5;
    }

// 변경 후 : 검색도 가능하고, 이름의 의미를 쫒아 코드 파악이 가능하다.

    int realDaysPerIdealDay = 4;
    const int WORK_DAYS_PER_WEEK = 5;
    int sum = 0;
    for(int j=0; j < NUMBER_OF_TASKS; j++){
        int realTaskDays = taskEstimate[j] * realDaysPerIdealDay;
        int realTaskWeeks = (realTaskDays / WORK_DAYS_PER_WEEK);
        sum += realTaskWeeks;
    }
 
## 6. 인코딩을 피하라

6-1. 헝가리식 표기법

이전에 컴파일러가 타입을 점검하지 않아 타입을 기억할 단서로 헝가리식 표기법을 사용했다.

그러나 지금은 변수 이름에 타입을 인코딩할 필요가 없다.

// 헝가리식 표기법

PhoneNumber phoneString;    // 타입 바뀌어도 이름은 안 바뀐다.

// 헝가리식 표기법 적용 X

PhoneNumber phonenumber;
 
6-2. 멤버변수 접두어

접두어를 무시하고 이름을 해독하자. 

멤버 변수를 눈에 띄게 보여주는 IDE를 사용하자.

// 멤버변수를 의미하는 접두어 : m_

    public class Part{
        private String m_dsc;  
    }

// 접두어가 필요없을 정도로 작아야한다.

    public class Part{
        private String description;
    }
 
6-3. 인터페이스 클래스와 구현 클래스

인코딩이 필요한 경우가 있다.

인터페이스 클래스 : ShapeFactory.class

구현 클래스 : ShapeFactoryImpl.class
 
##7. 자신의 기억력을 자랑하지 마라

문자 하나만 사용하는 변수이름은 문제가 있다. (루프에서 반복횟수를 세는 변수는 괜찮다.)

전문가 프로그래머는 명료함이 최고다 : 남들이 이해하는 코드를 내놓는다.

URL에서 r이라는 변수를 과시하는 것처럼 기억력을 과시하지말자.

##8. 클래스와 메서드 이름

> 클래스 이름 : 명사구

Customer, WikiPage, Account, AddressParser, Manager, Processor, Data, Info 단어는 피하자.

> 메서드 이름 : 동사나 동사구

postPaymet, deletePage, save

> 접근자 (Accessor) : getVerb();

> 변경자 (Mutator) : setVerb();

> 조건자 (Predicate) : isVerb();

> 생성자 중복정의 : 정적 팩토리 메서드 - 인수를 설명한다.

Complex fulcrumPoint = Complex.FromRealNumber(23.0);

생성자 사용 제한을 위해 private으로 생성자를 선언한다.
 
##9. 기발한 이름은 피하라

재미난 이름보단 명료한 이름  : HolyHandGrenade -> DeleteItems

구어체나 속어의 이름보단 의도를 분명하게 하는 이름 whack() -> kill()

 
##10. 한 개념에 한 단어 & 한 단어 한 목적

메서드 이름은 문맥에 독자적이고 일관적이어야한다.

추상적인 개념 하나에 여러 단어를 선택하지 말자

// 어떤 클래스에서 어떤 걸 사용했는지 혼란스럽지 않게 일관성을 유지한다.

fetch, retrieve, get    
controller, manager, driver

한 단어를 두 가지 목적으로 사용하지 마라! 
같은 맥락일 경우에만 '일관성'을 고려하자.

// 기존 : 기존 값 두개를 더하거나 이어서 새로운 값 만듬

add();

// 새로운 것 : 집합에 값 하나 추가 (다른 맥락)

insert(); append();
 
## 11. 해법 영역 & 문제 영역에서 가져온 이름을 사용하라

해법 영역 (Solution Domain) : 개발자라면 당연히 알고 있을 전산용어, 알고리즘 이름, 패턴 이름, 수학 용어 등은 사용하자.

> JobQueue, AccountVisitor(Visitor pattern)등

문제 영역 (Problem Domain) : 실제 도메인의 전문가에게 의미를 물어 파악할 수 있도록 문제 영역에서 이름을 가져오자.

## 12. 의미있는 맥락을 추가하라 & 불필요한 맥락을 없애라

의미가 분명한 이름이 있게 하자 : 클래스, 함수, 이름 공간에 넣어 맥락을 부여한다.

// 하나만 사용하면 주소 관련 변수임을 모른다.
> String firstName, lastName, street, houseNumber, city, state, zipcode;

// 접두어를 사용하면 맥락이 좀 더 분명해진다.
> String addrFirstName, addrLastName, addrStreet, addrHouseNumber, addrCity, addrState, addrZipcode;

// 클래스를 생성 : 변수가 큰 개념임이 컴파일러에도 분명해진다.

    class Address{
        String addrFirstName;
        String addrLastName;
        String addrStreet;
        String addrHouseNumber;
        String addrCity;
        String addrState;
        String addrZipcode
    }
    
맥락을 개선하여 클래스로 만들면 함수를 쪼개기도 쉬워진다.

의미가 분명하다면 일반적으로 짧은 이름이 긴 이름보다 좋다 : 불필요한 맥락을 추가하지 말자.

- 고급 휘발유 충전소 (Gas Station Deluxe) 애플리케이션을 짠다고, 모든 클래스 이름을 GSD로 시작하지 말자
- accountAddress, customerAddress : 클래스 이름 X, 인스턴스 이름 O
- PostalAddress, MAC, URI : 클래스 이름 O