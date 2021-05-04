# 함수(Method)

- 어떤 프로그램이든 가장 기본적인 단위는 함수이다.

- 의도를 분명히 표현하는 함수는 어떻게 구현할 수 있을까?

- 함수에 어떤 속성을 부여해야 처음 읽는 사람이 프로그램 내부를 직관적으로 파악할 수 있을까?

## 1. 작게 만들어라

함수를 만드는 첫째 규칙은 `작게`이다.

일반적으로 5줄 이내로 줄여야 마땅하다.

## 2. 블록과 들여쓰기

if 문, else, 문, while문 등에 들어가는 블록은 한 줄이어야 한다.

대게 거기서 함수를 호출한다.

## 3. 한 가지만 해라

함수는 한 가지만을 잘해야 한다.

한 가지 작업만 하는 함수는 자연스럽게 섹션으로 나누기 어렵다.

## 4. 함수 당 추상화 수준은 하나로

함수가 확실히 한 가지 작업만 하려면 함수 내 모든 문장의 추상화 수준이 동일해야 한다.

한 함수 내에 추상화 수준을 섞으면 코드를 읽는 사람이 헷갈릴 수 있는데, 특정 표현이 근본 개념인지 아니면 세부사항인지 구분하기 어렵기 때문이다.

내려가기 규칙(위에서 아래로 코드 읽기): 한 함수 다음에는 추상화 수준이 한 단계 낮은 함수가 온다. 즉, 위에서 아래로 프로그램을 읽으면 함수 추상화 수준이 한 번에 한 단계씩 낮아진다.
 
## 5. Switch 문

switch 문은 작게 만들기 어렵다.

다형성을 이용해 각 switch 문을 저차원 클래스(추상 팩토리)에 숨기고 절대로 반복하지 않는 방법이 있다.

`
public abstract class Employee {
      public abstract boolean isPayday();
      public abstract Money calculatePay();
      public abstract void deliverPay(Money pay);
}
`
-----------------
`public interface EmployeeFactory {
      Employee makeEmployee(EmployeeRecord r) throws InvalidEmployeeType;
}`
-----------------
`public class EmployeeFactoryImpl implements EmployeeFactory {
      public Employee makeEmployee(EmployeeRecord r) throws InvalidEmployeeType {
		switch (r.type) {
			case COMMISSIONED:
				return new CommissionedEmployee(r);
			case HOURLY:
				return new HourlyEmployee(r);
			case  SALARIED:
				return new SalariedEmployee(r);
			default:
				throw new InvalidEmployeeType(r.type);
        }
    }
}`

swich 문을 추상 팩토리에 꽁꽁 숨긴다.

팩토리는 switch 문을 사용해 적절한 Employee 파생 클래스의 인스턴스를 생성한다.

calculatePay, isPayday, deliverPay 등과 같은 함수는 Employee 인터페이스를 거쳐 호출된다.

다형성으로 인해 실제 파생 클래스의 함수가 실행된다.
 
## 6. 서술적인 이름을 사용해라

서술적인 이름을 사용하면 개발자 머릿속에서도 설계가 뚜렷해지므로 코드를 개선하기 쉬워진다.

## 7. 함수 인수

최선은 입력 인수가 없는 경우(무항)이며, 차선은 입력 인수가 1개뿐인 경우(단항)이다.

- 많이 쓰는 단항 형식: 인수에 질문을 던지는 경우, 인수를 뭔가로 변환해 결과를 반환하는 경우, 이벤트
- 이항 함수: 직교 좌표계 점과 같이 일반적으로 인수 2개를 취하는 경우에는 적절하다.
- 플래그 인수는 함수가 한꺼번에 여러 가지를 처리한다고 공표하는 것이므로 피해야 한다.
- 출력 인수는 피해야 한다. 함수에서 상태를 변경해야 한다면 함수가 속한 객체 상태를 변경하는 방식을 택한다.
 
## 8. 부수 효과를 일으키지 마라

함수에서 한 가지를 하겠다고 약속하고선 남몰래 다른 작업을 하기도 한다.

 ## 9. 명령과 조회를 분리하라

함수는 뭔가를 수행하거나 뭔가에 답하거나 둘 중 하나만 해야 한다.

객체 상태를 변경하거나 아니면 객체 정보를 반환하거나 둘 중 하나다.

## 10. 오류 코드보다 예외를 사용하라

명령 함수에서 오류 코드를 반환하는 방식은 명령/조회 분리 규칙을 미묘하게 위반한다.

if (deletePage(page) == E_OK)

반면 오류 코드 대신 예외를 사용하면 오류 처리 코드가 원래 코드에서 분리되므로 코드가 깔끔해진다.

`try {
	deletePage(page);
	registry.deleteReference(page.name);
	configKeys.deleteKey(page.name.makeKey());
}
catch(Exception e) {
	logger.log(e.getMessage());
}`
 
## 11. Try/Catch 블록 뽑아내기

코드 구조에 혼란을 일으키며 정상 동작과 오류 처리 동작을 뒤섞는다.

그러므로 try/catch 블록을 별도 함수로 뽑아내는 편이 좋다.

## 12. 반복하지 마라

중복을 없애면 모듈 가독성이 크게 높아진다.

## 13. 구조적 프로그래밍

함수가 작다면 return, break. continue를 여러 차례 사용해도 괜찮다.

goto 문은 큰 함수에서만 의미가 있으므로 작은 함수에서는 피해야 한다.

## 14. 어떻게 함수를 짜나요?

처음에는 길고 복잡하다.

후에 코드를 다듬고, 함수를 만들고, 이름을 바꾸고, 중복을 제거하고, 메서드를 줄이고 순서를 바꾼다.

때로는 전체 클래스를 쪼개기도 한다.

최종적으로 앞서 설명한 규칙을 따르는 함수가 얻어진다.

## 결론

프로그래머는 시스템을 프로그램이 아니라 이야기로 여긴다.

프로그래밍 언어라는 수단을 사용해 좀 더 풍부하고 표현력이 강한 언어를 만들어 이야기를 풀어간다.

작성하는 함수가 분명하고 정확한 언어로 깔끔하게 맞아떨어져야 이야기를 풀어가기가 쉬워진다.