# 동시성

## 클라이언트 / 서버 예제

간단한 클라이언트 / 서버 예제로, 서버는 소켓을 열어놓고 클라이언트가 연결하기를 기다린다. 클라이언트는 소켓에 연결해 요청을 보낸다.

### 단일 스레드 버전의 서버와 클라이언트
#### 서버
서버 어플리케이션의 단순화 버전. (전체 코드는 클라이언트 / 서버 - 단일 스레드 버전 참고)

```java
ServerSocket serverSocket = new ServerSocket(8009);

while (keepProcessing) {
    try {
        Socket socket = serverSocket.accept();
        process(socket);
    } catch (Exception e) {
        handle(e);
    }
}
```

위 서버는 연결을 기다리다, 들어오는 메세지를 처리한 후, 다음 클라이언트 요청을 기다린다. 다음은 위 서버를 연결하는 클라이언트 코드다.

#### 클라이언트
```java
private void connectSendReceive(int i) {
    try {
        Socket socket = new Socket("localhost", PORT);
        MessageUtils.sendMessage(socket, Integer.toString(i));
        MessageUtils.getMessage(socket);
        socket.close();
    } catch (Exception e) {
        e.printStackTrace();
    }
}
```

위 클라이언트 / 서버의 성능은 어떨까? 성능이 만족스러운지 확인하는 테스트를 짜보자.

#### 테스트 케이스
```java
@Test(timeout = 10000)
public void shouldRunInUnder10Seconds() throws Exception {
    Thread[] threads = createThreads();
    startAllThreads(threads);
    waitForAllThreadsToFinish(threads);
}
```

위 테스트 케이스는 테스트가 10,000 밀리초 내에 끝나는지 검사한다.
만약 테스트가 실패한다면? 단일 스레드 환경에서 속도를 끌어들일 방법은 거의 없다. 어플리케이션은 어디서 시간을 많이 보낼까? 가능성은 두 가지다.
1) I/O - 소켓 사용, 데이터베이스 연결, 가상 메모리 스와핑 기다리기 등
2) 프로세서 - 수치 계산, 정규 표현식 처리, 가비지 컬렉션 등

대개 시스템은 둘 다 하느라 시간을 보내지만, 특정 연산을 살폅조면 대개 하나가 지배적이다.

`만약 프로그램이 주로 프로세서 연산에 시간을 보낸다면 새로운 하드웨어를 추가해 성능을 높여 테스트를 통과하는 방식이 적합하다.` 
- 프로세서 연산에 시간을 보내는 프로그램은 스레드를 늘린다고 빨라지지 않는다. CPU 사이클은 한계가 있기 때문이다.

`반면 프로그램이 주로 I/O 연산에 시간을 보낸다면 동시성이 성능을 높여주기도 한다.` 
- 시스템 한쪽이 I/O를 기다리는 동안에 다른 쪽이 뭔가를 처리해 노는 CPU를 효과적으로 활용할 수 있다.

### 스레드 추가하기

여기서 성능 테스트가 실패했다고 가정하자. 자료 처리량을 높여 테스트를 통과할 방법은 무엇일까? 서버의 process 함수가 주로 I/O 연산에 시간을 보낸다면, 한가지 방법으로, 다음처럼 쓰레드를 추가한다. (process 함수만 변경한다.)

```java
void process(final Socket socket) {
    if (socket == null)
        return;
    
    Runnable clientHandler = new Runnable() {
        public void run() {
            try {
                String message = MessageUtils.getMessage(socket);
                MessageUtils.sendMessage(socket, "Processed: " + message);
                closeIgnoringException(socket);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    Thread clientConnection = new Thread(clientHandler);
    clientConnection.start();
}
```

이렇게 코드를 고치니 테스트는 통과하지만, 새로 고친 코드는 다소 부실해서 새로운 문제를 일으킨다.

새 서버가 만드는 스레드 수는 몇 개일까? 코드에서 명시하지 않았으므로 이론상으로 JVM이 허용하는 수까지 가능하다. 대다수 간단한 시스템은 괜찮지만 공용 네트워크에 연결된 수많은 사용자를 지원하는 시스템이라면? 너무 많은 사용자가 한꺼번에 몰린다면 시스템이 동작을 멈출지도 모른다.

동작 문제를 미뤄두고, 새로 고친 서버는 깨끗한 코드와 구조라는 관점에서도 문제가 있다.
서버 코드가 지는 책임은 몇 개일까?
- 소켓 연결 관리
- 클라이언트 처리
- 쓰레드 정책
- 서버 종료 정책

위 모든 책임을 process 함수가 진다. 게다가 코드 추상화 수준도 다양하다. process 함수가 작지만 확실히 분할할 필요가 있다.
SRP를 위반하고 있고, 다중 스레드 프로그램을 깨끗하게 유지하려면 스레드를 관리하는 코드는 스레드만 관리해야 한다.
왜냐? 비동시성 문제까지 뒤섞지 않더라도 동시성 문제는 그 자체만으로 추적하기 어려운 탓이다.

스레드 관리 책임을 분리한다면, 스레드 관리 전략이 변할 때 전체 코드에 미치는 영향이 작아지며 다른 책임을 간섭하지 않는다. 더구나 스레드를 걱정하지 않고서 다른 책임을 테스트하기가 훨씬 더 쉬워진다. 다음은 각 책임을 클래스로 분할한 서버 코드다.

```java
public void run() {
    while (keepProcessing) {
        try {
            ClientConnection clientConnection = connectionManager.awaitClient();
            ClientRequestProcessor requestProcessor = new ClientRequestProcessor(clientConnection);
            clientScheduler.schedule(requestProcessor);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    connectionManager.shutdown();
}
```

스레드와 관련된 코드는 ClientScheduler 클래스에 위치한다. 서버에서 동시성 문제가 발생한다면 살펴볼 코드는 단 한 곳이다.

```java
public interface ClientScheduler {
    void schedule(ClientRequestProcessor requestProcessor);
}
```

동시성 정책을 구현하기 쉽다.

```java
public class ThreadPerRequestScheduler implements ClientScheduler {
    public void schedule(final ClientRequestProcessor requestProcessor) {
        Runnable runnable = new Runnable() {
            public void run() {
                requestProcessor.process();
            }
        };

        Thread thread = new Thread(runnable);
        thread.start();
    }
}
```

스레드 관리를 한 곳으로 모았으니 스레드를 제어하는 동시성 정책을 바꾸기도 쉽다. 예를들어 자바 5 Executor 프레임워크로 옮긴다면 새 클래스를 작성해 대체하면 그만이다.

```java
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ExecutorClientScheduler implements ClientScheduler {
    Executor executor;

    public ExecutorClientScheduler(int availableThreads) {
        executor = Executors.newFixedThreadPool(availableThreads);
    }

    public void schedule(final ClientRequestProcessor requestProcessor) {
        Runnable runnable = new Runnable() {
            public void run() {
                requestProcessor.process();
            }
        };
        executor.execute(runnable);
    }
}
```

지금까지 살펴본 예제는 단일 스레드 시스템을 다중 스레드 시스템으로 변환해 시스템 성능을 높이는 방법과 테스트 프레임워크에서 시스템 성능을 검증하는 방법을 소개했다. 동시성과 관련된 코드를 몇몇 클래스로 집중해 단일 책임 원칙도 지켰다. 동시성은 그 자체가 복잡한 문제이므로 다중 스레드 프로그램에서는 단일 책임 원칙이 특히 중요하다.

## 가능한 실행 경로

다음 incrementValue 메서드를 살펴보자. incrementValue는 루프나 분기가 없는, 한 줄짜리 자바 메서드다.

```java
public class IdGenerator {
    int lastIdUsed;

    public int incrementValue() {
        return ++lastIdUsed;
    }
}
```

스레드 하나가 IdGenerator 인스턴스 하나를 사용한다고 가정한다. 그렇다면 가능한 실행 경로는 단 하나다. 가능한 결과도 단 하나다.

- 반환 값은 lastIdUsed 값과 동일하다. 두 값 모두 메서드를 호출하기 전보다 1이 크다.

만약 IdGenerator 인스턴스는 그대로지만 스레드가 두 개라면? 각 스레드가 incrementValue 메서드를 한 번씩 호출한다면 가능한 결과는 무엇일까? 가능한 실행 경로는? lastIdUsed 초깃값을 93으로 가정할 때, 가능한 결과는 다음과 같다.


- 스레드 1이 94를 얻고, 스레드 2가 95를 얻고, lastIdUsed가 95가 된다.
- 스레드 1이 95를 얻고, 스레드 2가 94를 얻고, lastIdUsed가 95가 된다.
- 스레드 1이 94를 얻고, 스레드 2가 94를 얻고, lastIdUsed가 94가 된다.

놀랄 수도 있지만 마지막 결과도 가능하다. 이처럼 다양한 결과가 나오는 알려면 가능한 실행 경로 수와 JVM 동작 방식을 알아야 한다.

### 경로 수

가능한 경로 수를 계산하기 위해 자바 컴파일러가 생성한 바이트 코드를 살펴보자. return ++lastIdUsed 자바 코드 한 줄은 바이트 코드 명령 8개에 해당한다. 즉, 두 스레드가 명령 8개를 뒤섞어 실행할 가능성이 충분하다.

루프나 분기가 없는 명령 N개를 스레드 T개가 차례로 실행한다면 가능한 경로 수는 다음과 같다.

$${(NT)!} \over N!^T$$

우리가 예제로 사용한 자바 코드 한 줄은 N=8이고 T=2이다. 계산하면 가능한 경로수는 12,870개다. 만약 lastIdUsed가 long 정수라면 읽기/쓰기 명령은 한 단계가 아니라 두 단계로 실행되어 가능한 경로수는 2,704,156개로 늘어난다.

만약 메서드를 이렇게 변경한다면 어떨까?

```java
public synchronized void incrementValue() {
    ++lastIdUsed;
}
```

가능한 경로 수는 (스레드가 2개일 때) 2개로 줄어든다! 스레드가 N개라면 가능한 경로수는 N!이다.

### 심층 분석

앞서 두 쓰레드가 메서드를 한 번씩 호출해 같은 값을 얻는 시나리오가 가능하다고 했다. 어떻게 그럴 수 있을까?
> 스레드 1이 94를 얻고, 스레드 2가 94를 얻고, lastIdUsed가 94가 된다.

원자적 연산(automic operation)이란 무엇일까? 우리는 중단이 불가능한 연산을 원자적 연산으로 정의한다. 예를 들어, 다음 코드에서 5행 lastid에 0을 할당하는 연산은 원자적이다. 자바 메모리 모델에 따르면 32비트 메모리에 값을 할당하는 연산은 중단이 불가능하기 때문이다.

```java
public class Exameple {
    int lastId;

    public void resetId() {
        lastId = 0;
    }

    public int getNextId() {
        ++lastId;
    }
}
```

lastId를 int에서 long으로 바꾼다면 여전히 원자적 연산일까? JVM 명세에 따르면 아니다. 어떤 프로세서는 원자적 연산으로 처리할지도 모르지만, JVM 명세에 따르면 64비트 값을 할당하는 연산은 32비트 값을 할당하는 연산 두 개로 나뉘어진다.

9행의 전처리 증가 연산 ++은 어떨까? 이 연산도 중단이 가능하며 원자적 연산이 아니다. 이해를 위해 할당 연산과 증가 연산의 바이트 코드를 살펴보자.

바이트 코드를 보기 전에 다음 정의 세 개를 명심한다.

- 프레임(frame): 모든 메서드 호출에는 프레임이 필요하다. 프레임은 반환 주소, 메서드로 넘어온 매개변수, 메서드가 정의하는 지역 변수를 포함한다. 프레임은 호출 스택을 정의할 때 사용하는 표준 기법이다. 현대 언어는 호출 스택으로 기본 함수 / 메서드 호출과 재귀적 호출을 지원한다.

- 지역 변수(local variables) - 메서드 범위 내에 정의되는 모든 변수를 가리킨다. 정적 메서드를 제외한 모든 메서드는 기본적으로 this라는 지역변수를 갖는다. this는 현재 객체, 즉 현재 스레드에서 가장 최근에 메시지를 받아 메서드를 호출한 객체를 가리킨다.

- 피연산자 스택(operand stack) - JVM이 지원하는 명령 대다수는 매개변수를 받는다. 피연산자 스택은 이런 매개변수를 저장하는 장소다. 피연산자 스택은 표준 LIFO 자료구조다.

![image](https://user-images.githubusercontent.com/37948906/131416299-9b452861-0b1d-464d-a146-b25a0a349f4b.png)
![image](https://user-images.githubusercontent.com/37948906/131416325-ae852d29-5981-48f0-a696-999335592839.png)

getNextId() 메서드를 synchronized로 선언하면 문제는 해결된다.

### 결론
스레드가 서로의 작업을 덮어쓰는 과정을 이해하기 위해 바이트 코드를 속속이 이해할 필요는 없다. 위 예제 하나를 이해했다면 여러 스레드가 서로를 훼방놓는 시나리오가 어느 정도 머릿속에 그려지리라 생각한다. 그 정도 지식이면 충분하다.

그렇기는 하지만, 앞서 간단한 예제가 보여주듯, 어떤 연산이 안전하고 안전하지 못한지 파악할 만큼 메모리 모델을 이해하고 있어야 한다. (전처리 연산과 후처리 연산 모두) ++ 연산은 원자적 연산이라 오해하는 사람이 많은데, ++ 연산은 분명히 원자적 연산이 아니다. 즉 다음을 알아야 한다는 말이다.

- 공유 객체/값이 있는 곳
- 동시 읽기/수정 문제를 일으킬 소지가 있는 코드
- 동시성 문제를 방지하는 방법

## 라이브러리를 이해하라

### Executor 프레임워크
처음 소개한 Executor 프레임워크는 스레드 풀링으로 정교한 실행을 지원한다. Executor는 java.util.concurrent 패키지에 속하는 클래스다.

어플리케이션에서 스레드는 생성하나 스레드 풀을 사용하지 않는다면 혹은 직접 생성한 스레드 풀을 사용한다면 Executor 클래스를 고려하자. 코드가 깔끔해지고, 이해하기 쉬워지고, 크기는 작아진다.

Executor 프레임워크는 스레드 풀을 관리하고, 풀 크기를 자동으로 조정하며, 필요하다면 스레드를 재사용한다. 게다가 다중 스레드 프로그래밍에서 많이 사용하는 Future도 지원한다. 또한 Executor 프레임워크는 Runnable 인터페이스를 구현한 클래스는 물론 Callable 인터페이스를 구현한 클래스도 지원한다. Callable 인터페이스는 Runnable 인터페이스와 유사하지만 결과 값을 반환한다. 결과 값은 다중 스레드 환경에서 흔히 요구되는 사항이다.

Future는 독립적인 연산 여럿을 실행한 후 모두가 끝나기를 기다릴 때 유용하다.

```java
public String processRequest(String message) throws Execption {
    Callable<String> makeExternalCall = new Callable<String>() {
        public String call() throws Exception {
            String result = "";
            // 외부에 요청한다.
            return result;
        }
    };

    Future<String> result = executorService.submit(makeExternalCall);
    String partialResult = doSomeLocalProcessing();
    return result.get() + partialResult;
}
```

위 예제에서 메서드는 makeExternalCall 객체를 실행한 후 다른 작업을 계속한다. 마지막 행에서 호출하는 result.get()은 Future가 끝나기를 기다린다.

### 스레드를 차단하지 않는 방법

최신 프로세서는 차단하지 않고도 안정적으로 값을 갱신한다. 자바 5 VM은 이를 이용한다. 예를 들어 다음 클래스는 다중 스레드 환경에서 값을 안전하게 갱신하기 위해 동기화를 수행한다. 즉, 스레드를 차단한다는 말이다.

```java
public class ObjectWithValue {
    private int value;
    public void synchronized incrementValue() { ++value; }
    public int getValue() { return value; }
}
```

자바 5는 위와 같은 상황에 적잡한 새로운 클래스를 제공한다. AtomicBoolean, AutomicInteger, AutomicReference를 포함한 여러 클래스가 있다. 차단하지 않도록 위 코드를 새로운 클래스로 다시 짜면 다음과 같다.

```java
public class ObjectWithValue {
    private AutomicInteger value = new AtomicInteger(0);

    public void incrementValue() {
        value.incrementAndGet();
    }

    public int getValue() {
        return value.get();
    }
}
```

기본 유형이 아니라 객체를 사용하고 ++연산자가 아니라 incrementAndGet() 메서드를 사용하지만, 새 ObjectWithValue 클래스는 이전 ObjectWithValue 클래스보다 거의 항상 더 빠르다. 엇비슷한 경우도 있지만 더 느린 경우는 사싨항 없다고 말해도 무방하다.

왜 성능이 좋을까? 현대 프로세서는 흔히 `CAS(Compare and Swap)` 이라는 연산을 지원한다. `CAS는 데이터베이스 분야에서 낙관적 잠금(optimistic locking)` 이라는 개념과 유사하다. 반면 `동기화 버전은 비관적 잠금(perssimistic locking)`이라는 개념과 유사하다.

`synchronized 키워드는 언제나 락을 건다.` 둘째 스레드가 같은 값을 갱신하지 않더라도 무조건 락부터 건다. 자바 버전이 올라갈 때마다 내장 락의 성능이 좋아지기는 했지만 그래도 `락을 거는 대가는 여전히 비싸다.`

스레드를 차단하지 않는 버전은 여러 스레드가 같은 값을 수정해 문제를 일으키는 상황이 그리 잦지 않다는 가정에서 출발한다. 그래서 `그런 상황이 발생했는지 효율적으로 감지해 갱신이 성공할 때까지 재차 시도`한다.

스레드 몇 개가, 아니 많은 스레드가 경쟁하는 상황에라도 락을 거는 쪽보다 문제를 감지하는 쪽이 거의 항상 더 효율적이다.

VM은 이를 어떻게 달성할까? CAS 연산은 원자적이다. 논리적으로 CAS 연산은 다음과 비슷하다.

```java
int variableBeingSet;

void simulateNonBlockingSet(int newValue) {
    int currentValue;
    do {
        currentValue = variableBeingSet
    } while (currentValue != compareAndSwap(currentValue, newValue));
}

int synchronized compareAndSwap(int currentValue, int newValue) {
    if (variableBeingSet == currentValue) {
        variableBeingSet = newValue;
        return currentValue;
    }
    return variableBeingSet;
}
```

메서드가 공유 변수를 갱신하려 든다면 `CAS 연산은 현재 변수 값이 최종으로 알려진 값인지 확인한다. 그렇다면 변수 값을 갱신`한다. 아니라면 다른 스레드가 끼어들었다는 뜻이므로 변수 값을 갱신하지 않는다. (CAS 연산으로) 값을 변경하려던 메서드는 값이 변경되지 않았다는 사실을 확인하고 다시 시도한다.

### 다중 스레드 환경에서 안전하지 않은 클래스

본질적으로 다중 스레드 환경에서 안전하지 않은 클래스가 있다.

몇 가지 예는 다음과 같다.

- SimpleDataFormat
- 데이터베이스 연결
- java.util 컨테이너 클래스
- 서블렛

몇몇 collection 클래스는 스레드에 안전한 메서드를 제공한다. 하지만 그런 메서드 여럿을 호출하는 작업은 스레드에 안전하지 않다. 예를 들어 HashTable에 이미 있는 항목을 건드리지 않으려고 다음 코드를 작성했다.

```java
if (!hashTable.containsKey(someKey)) {
    hashTable.put(someKey, new SomeValue());
}
```

각 메서드는 스레드에 안전하다. 하지만 containesKey와 put 사이에 다른 스레드가 끼어들어 HashTable에 값을 추가할지도 모른다. 해결 방안은 여러 가지다.

- 먼저 HashTable을 잠근다. HashTable을 사용하는 클라이언트 모두가 클라이언트 - 기반 잠금 매커니즘을 구현한다.

```java
synchronized(map) {
    if (!map.containsKey(key)){
        map.put(key, value);
    }
}
```

- HashTable을 객체로 감싼 후 다른 API를 사용한다. ADAPTER 패턴을 사용해 서버-기반 잠금 매커니즘을 구현한다.

```java
public class WrappedHAshTable<K, V> {
    privaet Map<K, V> map = new Hashtable<K, V>();

    public synchronized void putIfAbsent(K key, V value) {
        if (map.containsKey(key)){
            map.put(key, value);
        }
    }
}
```

- 스레드에 안전한 Collection 클래스를 사용한다.

```java
ConcurrentHashMap<Integer, String> map = new ConcurrentHashMap<Integer, String>();
map.putIfAbsent(key, value);
```

java.util.concurrent 패키지가 제공하는 Collection 은 putIfAbsent() 등과 같이 스레드에 안전한 메서드를 제공한다.

### 메서드 사이에 존재하는 의존성을 조심하라.

```java
public class IntegerIterator implements Iterator<Integer> {
   private Integer nextValue = 0;
   
   public synchronized boolean hasNext() {
      return nextValue < 100000;
   }
   
   public synchronized Integer next() {
      if (nextValue == 100000)
         throw new IteratorPastEndException();
      return nextValue++;
   }
   
   public synchronized Integer getNextValue() {
      return nextValue;
   }
}
```

```java
IntegerIterator iterator = new IntegerIterator();

while(iterator.hasNext()) {
   int nextValue = iterator.next();
   // nextValue로 뭔가를 한다.
}
```

스레드가 하나면 문제 없다. 스레드가 두개 이상이고 IntegerIterator 인스턴스를 공유한다면?

스레드 1과 2가 서로 간섭해 nextValue가 100000을 넘어 문제가 발생할 수 있다.
(둘다 hasNext를 true로 얻는 경우)

이 경우 해결방안은 세 가지다

- 실패를 용인한다
- 클라이언트를 바꿔 문제를 해결한다. 즉, 클라이언트 - 기반 잠금 메커니즘을 구현한다.
- 서버를 바꿔 문제를 해결한다. 서버에 맞춰 클라이언트도 바꾼다. 즉, 서버-기반 잠금 메커니즘을 구현한다.


#### 실패를 용인한다.

때로는 실패해도 괜찮도록 프로그램을 조정한다. 예를 들어, 위에서 클라이언트가 예외를 받아 처리해도 되겠다. 솔직히 말해 다소 조잡한 방법이다. 한밤중에 시스템을 재부팅해 메모리 누수를 해결하는 방법과 비슷하다 하겠다.

#### 클라이언트 - 기반 잠금

다중 스레드 환경에서도 안전한 IntegerIterator를 만들려면 (모든) 클라이언트를 다음과 같이 변경한다.

```java
IntegetIterator iterator = new IntegerIterator();

while(true) {
   int nextValue;
   synchronized (iterator) {
      if(!iterator.hasNext())
         break;
      nextValue = iterator.next();
   }
   doSometingWith(nextValue);
}
```

각 클라이언트는 synchronized 키워드를 사용해 IntegerIterator 객체에 락을 건다. 비록 DRY(Don't Repeat Yourself) 원칙을 위반하지만 다중 스레드 환경에 안전하지 못한 욉주 도구를 사용하는 경우라면 필요할지도 모른다.

하지만 서버를 사용하는 모든 프로그래머가 락을 기억해 객체에 걸었다 풀어야하므로 다소 위험한 전략이다.

#### 서버-기반 잠금

IntegerIterator를 다음과 같이 변경하면 클라이언트에서 중복해서 락을 걸 필요가 없어진다.
```java
public class IntegerIteratorServerLocked {
   private Integer nextValue = 0;
   public synchronized Integer getNextOrNull() {
      if (nextValue < 100000)
         return nextValue++;
      else
         return null;
   }
}
```

클라이언트 코드도 다음과 같이 변경한다.
```java
while(true) {
   Integer nextValue = iterator.getNextorNull();
   if (next == null)
      break;
   // 뭔가를 한다.
}
```

일반적으로 서버-기반 잠금이 더 바람직하다. 

- 코드 중복이 줄어든다. 
- 성능이 좋아진다.
- 오류가 발생할 가능성이 줄어든다. 잠금을 잊어버리는 바람에 오류가 발생할 위험은 프로그래머 한 명으로 제한된다.
- 스레드 정책이 하나다. 서버 한곳에서 정책을 구현한다.
- 공유 변수 범위가 줄어든다. 모두가 서버에 숨겨진다. 
- 문제가 생기면 살펴볼 곳이 적다. 

서버 코드에 손대지 못한다면? 
- 어댑터 패턴을 사용해 API를 변경한 후 잠금을 추가한다.

```java
public class ThreadSafeIntegerIterator {
   private IntegerIterator iterator = new IntegerIterator();
   
   public synchronized Integer getNextOrNull() {
      if(iterator.hasNext())
         return iterator.next();
      return null;
   }
}
```
혹은 스레드에 안전하며 인터페이스가 확장된 Collection 클래스를 사용한다.

### 작업 처리량 높이기

- 페이지를 읽어오는 평균 I/O 시간 : 1초
- 페이지를 분석하는 평균 처리 시간 : 0.5초
- 처리는 CPU 100% 사용, I/O는 CPU 0% 사용

스레드 하나로 처리할 경우) 총 실행시간 1.5초 * N
스레드 세개로 처리할 경우) 일초마다 두페이지 처리 가능. 처리율 세 배

### 데드락
개수가 한정된 자원 풀 두 개를 공유하는 웹 어플리케이션이 있다고 가정하자.
- 로컬 임시 데이터베이스 연결 풀
- 중앙 저장소 MQ 연결 풀
 
애플리케이션은 생성과 갱신이라는 연산 두 개를 수행한다.

- 생성 - 중앙 저장소 연결을 확보한 후 임시 데이터베이스 연결을 얻는다. 중앙 저장소와 통신한 후 임시 데이터베이스에 작업을 저장한다.
- 갱신 - 임시 데이터베이스 연결을 확보한 후 중앙 저장소 연결을 얻는다. 임시 데이터베이스에서 작업을 읽어 중앙 저장소로 보낸다.

만약 풀 크기보다 사용자 수가 많다면 어떤 일이 벌어질까? 두 풀 크기를 각각 10이라 가정하자.

사용자 10명이 생성 10개, 사용자 10명이 갱신이 10개를 요청해 각각의 자원을 확보할 경우 데드락에 빠질 수 있다. 시스템을 결코 복구되지 못한다.

흔히 시도하는 해결책은 디버깅 문을 추가해 사태를 파악하는 방법이다.

데드락을 근본적으로 해결하려면 원인을 이해해야 한다. 데드락은 다음 네 가지 조건을 만족하면 발생한다.

- 상호 배제
- 잠금 & 대기
- 선점 불가
- 순환 대기

#### 상호 배제
여러 스레드가 한 자원을 공유하나 그 자원은
- 여러 스레드가 동시에 사용하지 못하며
- 개수가 제한적이라면

상호 배제 조건을 만족한다.
> 데이터베이스 연결, 쓰기용 파일 열기, 레코드 락, 세마포어 등과 같은 자원

#### 잠금 & 대기

일단 스레드가 자원을 점유하면 필요한 나머지 자원까지 모두 점유해 작업을 마칠 때까지 이미 점유한 자원을 내놓지 않는다.

#### 선점 불가
한 스레드가 다른 스레드로부터 자원을 빼앗지 못한다. 자원을 점유한 스레드가 스스로 내놓지 않는 이상 다른 스레드는 그 자원을 점유하지 못한다.

#### 순환 대기
죽음의 포옹이라고도 한다. T1, T2라는 스레드 두 개가 있으며 R1, R2라는 자원 두 개가 있다고 가정하자. T1이 R1을 점유하고, T2가 R2를 점유한다. 또한 T1은 R2가 필요하고 T2도 R2가 필요하다. 그림으로 표현하면 아래와 같다.

![image](https://user-images.githubusercontent.com/37948906/131420269-d728e1c2-6f0e-44d0-9c3a-96f3a0619193.png)

네 조건 모두를 충족해야 데드락이 발생한다. 네 조건 중 하나라도 깨버리면 데드락이 발생하지 않는다.

#### 상호 배제 조건 깨기
- 동시에 사용해도 괜찮은 자원을 사용한다. 예를 들어, AtomicInteger를 사용한다.
- 스레드 수 이상으로 자원을 늘린다.
- 자원을 점유하기 전에 필요한 자원이 모두 있는지 확인한다.

대다수 자원은 불행히도 그 수가 제한적인 데다 동시에 사용하기도 어렵다. 첫 번째 자원을 사용하고 나서야 두 번째로 필요한 자원이 밝혀지는 경우도 없지 않다.

#### 잠금 & 대기 조건 깨기

대기하지 않으면 데드락이 발생하지 않는다. 각 자원을 점유하기 전에 확인한다. 만약 어느 하나라도 점유하지 못한다면 지금까지 점유한 자원을 몽땅 내놓고 처음부터 다시 시작한다.

하지만, 이 방법은 잠재적인 문제가 몇 가지 있다.

- 기아 - 하나 스레드가 계속해서 필요한 자원을 점유하지 못한다. ( 점유하려는 자원이 한꺼번에 확보하기 어려운 조합일지도 모른다 )
- 라이브락 Livelock - 여러 스레드가 한꺼번에 잠금 단계로 진입하는 바람에 계속해서 자원을 점유했다 내놨다를 반복한다. 단순한 CPU 스케줄링 알고리즘에서 특히 쉽게 발생한다.

두 경우 모두가 자칫하면 작업 처리량을 크게 떨어뜨린다. 기아는 CPU 효율을 저하시키는 반면, 라이브락은 CPU만 많이 사용한다.

비효율적으로 보일지도 모르지만, 아무 대책이 없는 경우보다는 좋다.

#### 선점 불가 조건 깨기

데드락을 피하는 또 다른 전략은 다른 스레드로부터 지원을 뺏어오는 방법이다. 일반적으로 간단한 요청 메커니즘으로 처리한다. 

필요한 자원이 잠겼다면 자원을 소유한 스레드에게 풀어달라 요청한다. 소유 스레드가 다른 자원을 기다리던 중이었다면 자신이 소유한 자원을 모두 풀어주고 처음부터 다시 시작한다. 

앞서 언급한 전략과 비슷하지만, 스레드가 자원을 기다려도 괜찮다는 이점이 있다. 그러면 처음부터 다시 시작하는 횟수가 줄어든다. 하지만 이 모든 요청을 관리하기가 그리 간단하지 않다.

#### 순환 대기 조건 깨기
데드락을 방지하는 가장 흔한 전략이다. 대다수 시스템에서는 모든 스레드가 동의하는 간단한 규약이면 충분하다. 

R1을 점유한 T1이 R2를 기다리고 R2를 점유한 T2가 R1을 기다리는 앞서 예제에서 T1과 T2가 자원을 똑같은 순서로 할당하게 만들면 순환 대기는 불가능해진다.

좀 더 일반적으로 말해, 모든 스레드가 일정 순서에 동의하고 그 순서로만 자원을 할당한다면 데드락을 불가능하다. 하지만 이 전략 역시 문제를 일으킬 소지가 있다.

- 자원을 할당하는 순서와 자원을 사용하는 순서가 다를지도 모른다. 그래서 맨 처음 할당한 자원을 아주 나중에야 쓸지도 모른다. 즉, 자원을 필요한 이상으로 오랫동안 점유한다.
- 때로는 순서에 따라 자원을 할당하기 어렵다. 첫 자원을 사용한 후에야 둘째 자원 ID를 얻는다면 순서대로 할당하기란 불가능하다.

이렇게 데드락을 피하는 전략을 많다. 어떤 전략은 기아를 일으키고, 어떤 전략은 CPU를 심하게 사용해 응답도를 낮춘다.

> TANSTAAFL! - There ain't no such thing as a free lunch, 공짜 점심은 없다.

프로그램에서 스레드 관련 코드를 분리하면 조율과 실험이 가능하므로 통찰력이 높아져 최적의 전략을 찾기 쉬워진다.

### 다중 스레드 코드 테스트

다중 스레드에서의 문제는 너무 드물게 발생하는 바람에 테스트로 발견하기가 어렵다.

그렇다면 이렇게 간단한 실패를 증명할 방법은 무엇일까? 

다음은 몇 가지 아이디어다.

- 몬테 카를로 테스트 - 조율이 가능하게 유연한 테스트를 만든다. 임의로 값을 조율하면서 반복해 돌린다. 테스트가 실패한 조건은 신중하게 기록한다.
- 시스템을 배치할 플랫폼 전부에서 테스트를 돌린다. 반복해서 돌린다. 테스트가 실패 없이 오래 돌아갈수록 두 가지 중 하나일 확률이 높아진다. 
- 실제 코드가 올바르다.
- 테스트가 부족해 문제를 드러내지 못한다.
- 부하가 변하는 장비에서 테스트를 돌린다. 실제 환경과 비슷하게 부하를 걸어 줄 수 있다면 그렇게 한다.

하지만, 가능성은 매우 낮다. 십억 번에 한 번씩만 일어나는 희귀한 문제가 가장 골치 아프다.

### 스레드 코드 테스트를 도와주는 도구
IBM은 ConTest라는 도구를 내놓았다. 스레드에 안전하지 않는 코드에 보조 코드를 더해 실패할 가능성을 높여주는 도구다.

## 결론
- 이 장에서는 동시성 프로그래밍을 공부하고 다중 스레드 코드를 깨끗하게 유지하는 방법을 익혔다.
- 하지만 다중 스레드 시스템을 구현하려면 알아야 하는 내용이 아주 많다.
- 이 장에서는 동시 갱신을 논했으며, 동시 갱신을 방지하는 깨끗한 동기화/잠금 기법을 소개했다. 스레드가 I/O 위주 시스템의 처리율을 높여주는 이유와 실제로 처리율을 높이는 방법을 살펴봤다.
- 데드락을 논했으며, 깔끔하게 데드락을 방지하는 방법도 열거했다.
- 마지막으로 보조코드를 추가해 동시성 문제를 사전에 노출하는 전략을 소개했다.
