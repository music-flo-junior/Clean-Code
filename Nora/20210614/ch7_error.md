# 오류 처리

깨끗한 코드와 오류 처리는 확실히 연관성이 있다.

상당 수 코드 기반은 여기저기 흩어진 오류 처리 코드 때문에 실제 코드가 하는 일을 파악하기가 거의 불가능하다.

오류 처리는 중요하지만, 오류 처리 코드로 인해 프로그램 논리를 이해하기 어려워진다면 깨끗한 코드가 아니다.

비지니스 논리와 오류 처리가 잘 분리된 코드를 작성해야 한다.

> 오류 코드보다 예외를 사용하라

잘못된 방법 - 오류 플래그를 설정하거나 호출자에게 오류 코드를 반환하는 방법

	public class DeviceController {
        DeviceHandle handle = getHandle(DEV1);
        if (handle != DeviceHandle.INVALID) {
            retrieveDeviceRecord(handle);
            if (record.getStatus() != DEVICE_SUSPENDED) {
                closeDevice(handle);
            } else {
                logger.log("Device suspended. Unable to shut down");
            }
        } else {
            logger.log("Invalid handle");
        }
	}
	
→ 호출자 코드가 복잡해 진다. 함수를 호출한 즉시 오류를 확인해야 하기 때문이다. 예외를 던지는 게 낫다.

해결 - 예외 사용

	public class DeviceController {
        public void sendShutDown() {
            try {
                tryToShutDown();
            }
            catch (DeviceShutDownError e) {
                logger.log(e);
            }
        }

        private void tryToShutDown() {
            DeviceHandle handle = getHandle(DEV1);
            DeviceRecord record = retrieveDeviceRecord(handle);
    
            pauseDevice(handle);
            clearDeviceWorkQueue(handle);
            closeDevice(handle);
        }

        private DeviceHandle getHandle(DeviceId id) {
            ...
            throw new DeviceShutDownError("Invalid handle for: " + id.toString());
            ...
        }
	}

→ 보기 좋아졌을 뿐 아니라, 코드 품질도 나아졌다.

디바이스를 종료하는 알고리즘과 오류를 처리하는 알고리즘을 분리했기 때문이다.

> Try-Catch-Finally 문부터 작성하라

try문 에서 무슨 일이 생기든 catch 블록은 프로그램 상태를 일관성 있게 유지해야 한다.

→ 예외가 발생할 코드를 짤 때는 try-catch-finally 문으로 시작하는 편이 낫다.

1. 파일이 없으면 예외를 던지는지 알아보는 단위 테스트


	@Test(expected = StorageException.class)
    public void retrieveSectionShouldThrowOnInvalidFileName() {
    	sectionStore.retrieveSection("invalid - file");
    }
         
2. 단위 테스트에 맞춰 구현한 코드


	public List<RecordedGrip> retrieveSection(String sectionName) {
      try{
        FileInputStream stream = new FileInputStream(sectionName);
        stream.close();
      } catch (FileNotFoundException e) {
        throw new StorageException("retrieval error", e);
      }
      return new ArrayList<RecordedGrip>();
    }
    
강제로 예외를 일으키는 테스트 케이스를 작성한 후 테스트를 통과하게 코드를 작성한다.

try-catch 구조로 범위를 정의하고, TDD를 사용해 필요한 나머지 논리를 추가한다.

→ try 블록의 트랜잭션 범위부터 구현하게 되므로 트랜잭션 본질을 유지하기 쉬워진다.

→ TDD(Test Driven Development) 테스트 주도 개발 : 테스트가 개발을 이끌어 나간다.

> 미확인 예외(Unchecked Exception)를 사용하라

Checked Exception은 선언부의 수정을 필요로 하기 때문에 모듈의 캡슐화를 깨버린다.

안정적인 소프트웨어를 제작하는 요소로 확인된 예외가 반드시 필요하지 않다는 사실이 분명해졌다.

예전에는 메서드를 선언할 때 메서드가 반환할 예외를 모두 열거했다.

그러나 비용이 상응하는 이익을 제공하지 못했고 확인된 예외는 OCP를 위반한다.

→ OCP(Open Closed Principle) :
기존의 코드를 변경하지 않으면서 기능을 추가할 수 있도록 설계가 되어야 한다.

> 예외에 의미를 제공하라

예외를 던질 때는 전후 상황을 충분히 덧붙인다. 그러면 오류가 발생한 원인과 위치를 찾기가 쉬워진다.

호출 스택으로는 오류에 대한 정보가 부족하다.

오류 메세지에 정보(실패한 연산 이름, 실패 유형 등)를 충분히 추가한다.

> 호출자를 고려해 예외 클래스를 정의하라

오류를 정의해 분류하는 방법은 프로그래머에게 오류를 잡아내는 방법이 되어야 한다.

잘못된 예) 오류를 형편없이 분류한 사례

	ACMEPort port = new ACMEPort(12);
    
    try{
    	  port.open();
    } catch (DeviceResponseException e) {
    		reportPortError(e);
    } catch (ATM1212UnlockedException e) {
    		reportPortError(e);
    } catch (GMXError e) {
    		reportPortError(e);
    } finally {
    	...
    }
    
해결 - 호출하는 라이브러리의 API를 감싸면서 예외 유형을 하나 반환한다.

	LocalPort port = new LocalPort(12);
    try {
      port.open();
    } catch (PortDeviceFailure e) {
      reportError(e);
      logger.log(e.getMessage(), e);
    } finally {
      ...
    }
    
    public class LocalPort {
      private ACMEPort innerPort;
      
      public LocalPort(int portNumber) {
        innerPort = new ACMEPort(portNumber);
      }
      
      public void open() {
        try{
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
    
LocalPort는 ACMEPort 클래스가 던지는 예외를 잡아 변환하는 Wrapper 클래스이다.

* Wrapper 클래스로 예외 호출 라이브러리 API를 감싸면 좋은 점

1. 외부 API를 사용할 때는 Wrapper 클래스 기법이 최선이다.

2. 외부 API를 감싸면 외부 라이브러리와 프로그램 사이에 의존성이 크게 줄어든다.

3. 나중에 다른 라이브러리로 갈아타도 비용이 적다.

4. Wrapper 클래스에서 외부 API를 호출하는 대신 테스트 코드를 넣어주면 테스트 하기도 쉽다.

5. 특정 업체가 API를 설계한 방식에 국한되지 않는다. 프로그램이 사용하기 편리한 API를 정의할 수 있다.

> 정상 흐름을 정의하라

중단이 적합하지 않은 때도 있다. "특수 사례 패턴"으로 클래스를 만들거나 객체를 조작해 특수사례를 처리한다.

→ 클라이언트 코드가 예외적인 상황을 처리할 필요가 없어진다.

before)

	try {
     	MealExpenses expenses = expenseReportDAO.getMeals(employee.getID());
     	m_total += expenses.getTotal();
     } catch(MealExpensesNotFound e) {
     	m_total += getMealPerDiem();
     }

after) 클래스나 객체가 예외적인 상황을 캡슐화 해서 처리한다

	MealExpenses expenses = expenseReportDAO.getMeals(employee.getID());
	m_total += expenses.getTotal();
	
ExpenseReportDAO를 고쳐 언제나 MealExpense 객체를 반환하게 한다.

청구한 식비가 없다면 일일 기본 식비를 반환하는 MealExpense 객체를 반환한다.

	public class PerDiemMealExpenses implements MealExpenses {
    	public int getTotal() {
    		// 기본값으로 일일 기본 식비를 반환한다.
    	}
    }

> null을 반환하지 마라

null을 반환하고 이를 if(object != null)으로 확인하는 방식은 나쁘다.

null 대신 예외를 던지거나 특수 사례 객체(ex. Collections.emptyList())를 반환하라!

사용하려는 외부 API가 null을 반환한다면 Wrapper 를 구현해 예외를 던지거나 특수 사례 객체를 반환하라.

before)

	List<Employee> employees = getEmployees();
    if (employees != null) {
    	for(Employee e : employees) {
    		totalPay += e.getPay();
    	}
    }

after) getEmployees가 null 대신 빈 리스트를 반환한다.

	List<Employee> employees = getEmployees();
    for(Employee e : employees) {
    	totalPay += e.getPay();
    }
    
- 자바의 Collections.emptyList()

	public List<Employee> getEmployees() {
    	if ( ...직원이 없다면... ) {
    		return Collections.emptyList();
    	}
    }

: 코드도 깔끔해지며 NullPointerException이 발생할 가능성도 줄어든다.

> null을 전달하지 마라

메서드에서 null을 반환하는 방식도 나쁘지만 null을 전달하는 방식은 더 나쁘다.

예외를 던지거나 assert 문을 사용할 수는 있다.

하지만 애초에 null을 전달하는 경우는 금지하는 것이 바람직하다.