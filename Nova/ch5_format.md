# 5강. 형식 맞추기 

프로그래머라면 형식을 깔끔하게 맞춰 코드를 짜야 한다. `코드 형식을 맞추기위한 간단한 규칙을 정의`하고 그 규칙을 착실히 따라야 한다. 팀으로 일한다면 팀이 합의해 규칙을 정하고 모두가 그 규칙을 따라야 한다. 필요하다면 규칙을 자동으로 적용하는 도구를 활용한다.

### 1. 형식을 맞추는 목적

- 코드 형식은 중요하다! 그러나 융통성 없이 맹목적으로 따르면 안된다. 
- 코드 형식은 의사소통의 일환이다. 오늘 구현한 기능이 다음 버전에서 바뀔 확률은 굉장히 높다. 따라서 오늘 구현한 코드의 가독성은 앞으로 바뀔 코드의 품질에 지대한 영향을 미친다.
- 맨 처음 잡아놓은 구현 스타일과 가독성 수준은 유지보수 용이성과 확장성에 계속 영향을 미친다. 원래 코드는 사라지더라도 개발자의 스타일과 규율은 사라지지 안흔다.

그렇다면 원활한 소통을 장려하는 코드의 형식은 무엇일까?

### 2. 적절한 행 길이를 유지하라 

소스의 세로 길이는 얼마나 길어야 적당할까? 자바에서의 파일 크기는 클래스 크기와 밀접하다. 대다수 자바 소스 파일은 크기가 어느 정도일까?

- 책에서 소개하는 여러 프로젝트의 평균 파일크기를 통해 우리는 대부분 `200줄 ~ 500줄인 파일`로도 커다란 시스템을 구축할 수 있다는 사실을 알 수 있다. 일반적으로 큰 파일보다 작은 파일이 이해하기 쉽다.

#### 2.1. 신문 기사처럼 작성하라

- 소스파일도 신문 기사와 비슷하게 작성한다. 이름은 간단하면서 설명이 가능하게 짓는다.
- 이름만 보고도 올바른 모듈을 살펴보고 있는지 아닌지를 판단할 정도로 신경 써서 짓는다. 
- 소스 파일 첫 부분은 고차원 개념과 알고리즘을 설명한다.
- 아래로 내려갈수록 세세하게 묘사한다.
- 마지막에는 가장 저차원 함수와 세부 내역이 나온다.

#### 2.2. 개념은 빈 행으로 분리하라

- 거의 모든 코드는 왼쪽에서 오른쪽으로 그리고 위에서 아래로 읽힌다. 각 행은 수식이나 절을 나타내고, 일련의 행 묶음은 완결된 생각 하나를 표현한다. 생각 사이는 빈 행을 넣어 분리해야 마땅하다.

#### 2.3. 세로 밀집도

- 줄바꿈이 개념을 분리한다면 세로 밀집도는 연관성을 의미한다. 즉, 서로 밀집한 코드 행은 세로로 가까이 놓여야 한다는 뜻이다.
- 의미 없는 주석으로 관련있는 변수를 떨어뜨려 놓는 것보다, 의미 있는 이름으로 두 변수의 이름을 정하고 가까이 두는 것이 훨씬 직관적이다.

#### 2.4. 수직 거리
- 서로 밀접한 개념은 세로로 가까이 둬야 한다.
- 물론 두 개념이 서로 다른 파일에 속한다면 규칙이 통하지 않는다. 하지만 타당한 근거가 없다면 서로 밀접한 개념은 한 파일에 속해야 마땅하다. 이게 바로 protected 변수를 피해야하는 이유 중 하나다.
- 같은 파일에 속할 정도로 밀접한 두 개념은 세로 거리로 연관성을 표현한다. 여기서 연관성이란 한 개념을 이해하는 데 다른 개념이 중요한 정도다. 연관성이 깊은 두 개념이 멀리 떨어져 있으면 코드를 읽는 사람이 소스 파일과 클래스를 여기저기 뒤지게 된다.

##### 변수선언
- 변수는 사용하는 위치에 최대한 가까이 선언한다. 우리가 만든 함수는 매우 짧으므로 지역 변수를 각 함수 맨 처음에 선언한다.
- 아주 드물지만 다소 긴 함수에서 블록 상단이나 루프 직전에 변수를 선언하는 사례도 있다.

##### 인스턴스 변수
- 반면 인스턴스 변수는 맨 처음에 선언한다. 변수 간에 세로로 거리를 두지 않는다.잘 설계한 클래스는 많은 클래스 메서드가 인스턴스 변수를 사용하기 때문이다.
- 자바에서는 일반적으로 클래스 맨 처음에 인스턴스 변수를 선언한다. (메서드 끝이나, 메서드 사이에 위치하면 변수를 찾기 어려워진다.)

##### 종속 함수
- 한 함수가 다른 함수를 호출한다면 두 함수는 세로로 가까이 배치한다. 또한 가능하다면 호출하는 함수를 호출되는 함수보다 먼저 배치한다. (그러면 프로그램이 자연스럽게 읽힌다.)
- 규칙을 일관적으로 적용한다면 독자는 방금 호출한 함수가 잠시 후에 정의되리라는 사실을 예측한다.

##### 개념의 유사성
- 어떤 코드는 서로 끌어당긴다. 개념적인 친화도가 높기 때문이다. 친화도가 높을 수록 코드를 가까이 배치한다.
- 친화도가 높은 요인은 여러가지다.
  - 1. 한 함수가 다른 함수를 호출해 생기는 직접적인 종속성
  - 2. 변수와 변수를 사용하는 함수
  - 3. 비슷한 동작을 수행하는 함수

#### 2.5. 세로 순서
- 일반적으로 함수 호출 종속성은 아래 방향으로 유지한다. 즉, 호출되는 함수를 호출하는 함수보다 나중에 배치한다. 그러면 소스 코드 모듈이 고차원에서 저차원으로 자연스럽게 내려간다.

### 3. 가로 형식 맞추기
- 한 행은 가로로 얼마나 길어야 적당할까? 일반적인 프로그램에서의 한 행의 길이는 10자 미만이 30프로, 20자~60자가 40%, 80자 이후 부터 행 수는 급격하게 감소한다.
- 그러므로 짧은 행이 바람직하다. 저자의 추천은 120자 정도로 행 길이를 제한하는 것이 좋다고 한다.

#### 3.1. 가로 공백과 밀집도

가로로는 공백을 사용해 밀접한 개념과 느슨한 개념을 표현한다.
```java
private void measureLine(String line) {
  lineCount++;
  int lineSize = line.length();
  totalChars += lineSize;
  lineWidthHistogram.addLine(lineSize, lineCount);
  recordWidestLine(lineSize);
}
```

- 할당 연산자를 강조하려고 앞 뒤에 공백을 줬다. 고앱ㄱ을 넣으면 두 가지 주요 요소가 확실히 나뉜다는 사실이 더욱 분명해진다.
- 반면, 함수 이름과 이어지는 괄호 사이에는 공백을 넣지 않았다. 함수와 인수는 서로 밀접하기 때문이다.
- 연산자 우선순위를 강조하기 위해서도 공백을 사용한다.


```java
public class Quadratic {
  public static double root1(double a, double b, double c) {
    double determinant = determinant(a, b, c);
    return (-b + Math.sqrt(determinant)) / (2*a);
  }

  public static double root2(int a, int b, int c) {
    double determinant = determinant(a, b, c);
    return (-b - Math.sqrt(determinant)) / (2*a);
  }

  private static double determinant(double a, double b, double c) {
    return b*b - 4*a*c;
  }
}
```
수식을 읽기 편하다. 승수 사이에는 공백이 없다. 곱셈은 우선순위가 가장 높기 때문이다. 항 사이에는 공백이 들어간다. 덧셈과 뺄셈은 우선순위가 곱셈보다 낮기 때문이다.

불행히도, 코드 형식을 자동으로 맞춰주는 도구는 대다수가 연산자 우선순위를 고려하지 못하므로, 수식에 똑같은 간격을 적용한다. 따라서 위와 같이 공백을 넣어줘도 나중에 도구에서 없애는 경우가 흔하다.

#### 3.2. 가로 정렬

```java
public class FitNesseExpediter implements ResponseSender
{
  private   Socket          socket;
  private   InputStream     input;
  private   OutputStream    output;
  private   Request         request;
  private   Response        response;
  private   FitnesseContext context;
  private   long            requestParsingTimeLimit;
  private   long            requestProgress;
  private   long            requestParsingDeadline;
  private   boolean         hasError;

  public FitNessExpediter(Socket          s,
                          FitNesseContext context) throws Exception
  {
    this.context =        context;
    socket =                  s;
    input =                   s.getInputStream();
    output =                  s.getOutputStream();
    requestParsingTimeLinit = 10000;
  }
}
```

- 그러나 이런 정렬은 별로 유용하지 않다. 코드가 엉뚱한 부분을 강조해 진짜 의도가 가려지기 때문이다.
예를 들어, 위 선언부를 읽다보면 변수 유형은 무시하고 변수 이름부터 읽게 된다.
- 그리고 코드 형식을 자동으로 맞춰주는 도구는 대다수가 위와 같은 정렬을 무시한다.

그러므로 아래 코드와 같이 선언문과 할당문을 별도로 정렬하지 않고, 선언부가 길다면 정렬을 하는 것이 아니라 클래스를 쪼개는 것이 맞다.
```java
public class FitNesseExpediter implements ResponseSender
{
  private Socket socket;
  private InputStream input;
  private OutputStream output;
  private Request request;
  private Response response;
  private FitnesseContext context;
  private long requestParsingTimeLimit;
  private long requestProgress;
  private long requestParsingDeadline;
  private boolean hasError;

  public FitNessExpediter(Socket s, FitNesseContext context) throws Exception
  {
    this.context = context;
    socket = s;
    input = s.getInputStream();
    output = s.getOutputStream();
    requestParsingTimeLinit = 10000;
  }
}
```

#### 3.3. 들여쓰기

- 범위로(scope)로 이뤄진 계층을 표현하기 위해 우리는 코드를 들여쓴다.
- 들여쓰는 정도는 계층에서 코드가 자리잡은 수준에 비례한다. 
  - 클래스 정의처럼 파일 수준인 문장은 들여쓰지 않는다.
  - 클래스 내 메서드는 클래스보다 한 수준 들여쓴다.
  - 메서드 코드는 메서드 선언보다 한 수준 들여쓴다.
  - 블록 코드는 블록을 포함하는 코드보다 한 수준 들여쓴다.
- 들여쓰기가 없다면 인간이 코드를 읽기란 거의 불가능하리라. 들여쓰기한 파일은 구조가 한눈에 들어온다. 변수, 생성자 함수, 접근자 함수, 메서드가 금방 보인다. 반면 들여쓰기 하지 않은 코드는 열심히 분석하지 않는 한 거의 불가능하다.

##### 들여쓰기 무시하기

- 때로는 간단한 if문, 짧은 while문, 짧은 함수에서 들여쓰기 규칙을 무시하고픈 유혹이 생긴다. 그러나 이런 코드에도 들여쓰기를 제대로 표현하는 것이 좋다.

```java
public class CommentWidget extends TextWidget {
  public static final String REGEXP = "^#[^\r\n]*(?:?:\r\n)|\n\r)?";
  
  public commentWidget(ParentWidget parent, String text){ super(parent, text); }
  public String render() throws Exception { return ""; }
}
```

```java
public class CommentWidget extends TextWidget {
  public static final String REGEXP = "^#[^\r\n]*(?:(?:\r\n)|\n|\r)?";

  public CommentWidget(ParentWidget parent, String text) {
    super(parent, text);
  }

  public String render() throws Exception {
    return "";
  }
}
```

#### 3.4. 가짜 범위

때로는 빈 while문이나 for 문을 접한다. 이런 구조는 가능하면 피하고 피하지 못할 때는 빈 블록을 올바로 들여쓰고 괄호로 감싼다. while문 끝에 세미콜론(;) 하나를 살짝 덧붙이지 말고, 새 행에다 제대로 들여써서 넣어주자.

```java
while (dis.read(buf, 0, readBufferSize) != -1)
;
```

### 4. 팀 규칙

- 팀에 속한다면 자신이 선호해야할 규칙은 바로 팀 규칙이다. 
- 팀은 한 가지 규칙에 합의해야 한다. 그리고 모든 팀원은 그 규칙을 따라야 한다. 그래야 소프트웨어가 일관적인 스타일을 보인다. 개개인이 따로국밥처럼 맘대로 짜대는 코드는 피해야 한다.
- 어디에 괄호를 넣어야 할지, 들여쓰기는 몇 자로 할지, 클래스와 변수와 메서드 이름은 어떻게 지을지 등을 정하고, IDE 코드 형식기로 설정한 후 사용하는 것이 좋다.
- 좋은 소프트웨어 시스템은 읽기 쉬운 문서로 이뤄진다는 사실을 기억하자. 스타일은 일관적이고 매끄러워야 한다. 한 소스파일에서 봤던 형식이 다른 소스 파일에서도 쓰이라는 신뢰감을 독자들에게 줘야 한다. 온갖 스타일을 뒤섞어 소스 코드를 필요 이상으로 복잡하게 만드는 실수는 반드시 피한다.

### 5. 밥 아저씨의 형상 규칙
```java
public class CodeAnalyzer implements JavaFileAnalysis {
  private int lineCount;
  private int maxLineWidth;
  private int widestLineNumber;
  private LineWidthHistogram lineWidthHistogram;
  private int totalChars;

  public CodeAnalyzer() {
    lineWidthHistogram  new LineWidthHistogram();
  }

  public static List<File> findJavaFiles(File parentDirectory) {
    List<File> files = new ArrayList<File>();
    findJavaFiles(parentDirectory, files);
    return files;
  }

  private static void findJavaFiles(File parentDirectory, List<File> files) {
    for (File file : parentDirectory.listFiles()) {
      if (file.getName().endWith(".java"))
        files.add(file);
      else if (file.isDirectory())
        findJavaFiles(file, files);
    }
  }

  public void analyzeFile(File javaFile) throws Exception {
    BufferedReader br  new BufferedReader(new FileReader(javaFile));
    String line;
    while ((line = br.readLine()) != null)
      measureLine(line);
  }

  private void measureLine(String line) {
    lineCount++;
    int lineSize = line.length();
    totalChars += lineSize;
    lineWidthHistogram.addLine(lineSize, lineCount);
    recordWidestLine(lineSize);
  }

  private void recordWidestLine(int lineSize) {
    if (lineSize > maxLineWidth) {
      maxLineWidth = lineSize;
      widestLineNumber = lineCount;
    }
  }

  public int getLineCount() {
    return lineCount;
  }

  public int getMaxLineWidth() {
    return maxLineWidth;
  }

  public int getWidestLineNumber() {
    return widestLineNumber;
  }

  public LineWidthHistogram getLineWidthHistogram() {
    return lineWidthHistogram;
  }

  public double getMeanLineWidth() {
    return (double)totalChars/lineCount;
  }

  public int getMedianLineWidth() {
    Integer[] sortedWidths = getSortedWidths();
    int cumulativeLineCoutn = 0;
    for (int width : sortedWidths) {
      cumulativeLineCount += lineCountForWidth(width);
      if (cmulativeLineCount > lineCount/2)
        return width;
    }
    throw new Error("Cannot get here");
  }

  private int lineCountForWidth(int width) {
    return lineWidthHistogram.getLinesforWidth(width).size();
  }

  private Integer[] getSortedWidths() {
    Set<Integer> widths = lineWidthHistogram.getWidths();
    Integer[] sortedWidths = (widths.toArray(new Integer[0]));
    Arrays.sort(sortedWidths);
    return sortedWidths;
  }
}
```


### 6. 노바의 형상 규칙 (이라 쓰고 인텔리제이 자동 정렬이라 읽는다.)

`CurationController.java`
```java
    @ApiOperation(value = "등록")
    @PostMapping
    public CurationVo create(@RequestBody @Valid CurationCreateParamsVo payload,
                       @AuthenticationPrincipal @ApiIgnore User currentUser) {
        log.info("큐레이션 등록: {} by {}", payload.getTitle(), currentUser.getUserId());
        return curationService.create(payload, currentUser);
    }

    @ApiOperation(value = "복제")
    @PostMapping("{curationId}")
    public CurationVo duplicate(@PathVariable Long curationId,
                          @AuthenticationPrincipal @ApiIgnore User currentUser) {
        log.info("큐레이션 복제: {} by {}", curationId, currentUser.getUserId());
        return curationService.duplicate(curationId, currentUser);
    }
```

```java
    @ApiOperation(value = "등록")
    @PostMapping
    public CurationVo create(@RequestBody @Valid CurationCreateParamsVo payload,
                             @AuthenticationPrincipal @ApiIgnore User currentUser) {
        log.info("큐레이션 등록: {} by {}", payload.getTitle(), currentUser.getUserId());
        return curationService.create(payload, currentUser);
    }

    @ApiOperation(value = "복제")
    @PostMapping("{curationId}")
    public CurationVo duplicate(@PathVariable Long curationId,
                                @AuthenticationPrincipal @ApiIgnore User currentUser) {
        log.info("큐레이션 복제: {} by {}", curationId, currentUser.getUserId());
        return curationService.duplicate(curationId, currentUser);
    }

```

### 7. 기타
##### 7.1. 인텔리제이 auto indent 단축키
`Option + Command + L`
`Shift + Option + Command + L`

##### 7.2 인텔리제이 Google Java Style Guide 적용하기
<details>
<div markdown="1">

(Eclipse 사용할 때는 포맷터가 별로여서 GoogleStyle을 설정해두고 사용했는데 intellij의 default도 이미 훌륭해서 바꿀 필요가 없을 것 같다. 그래도 방법은 일단 찾아본다.)

1. Google java style - Intellij indent 파일 다운받기
  - https://github.com/google/styleguide 에서 `intellij-java-google-style.xml` 파일 다운받기.

2. Intellij 환경설정 (Cmd + ,)에서 Editor - Code Style - 톱니바퀴 - `Import Scheme` - `IntellJ IDEA code style XML` 선택

![스크린샷 2021-05-17 오후 9 06 31](https://user-images.githubusercontent.com/37948906/118487064-4e60c500-b755-11eb-8a7b-7e50409fd172.png)

3. 다운받은 파일 `intellij-java-google-style.xml` 선택
![스크린샷 2021-05-17 오후 9 14 48](https://user-images.githubusercontent.com/37948906/118487060-4d2f9800-b755-11eb-8d71-04c6ac2165ee.png)

4. Google Style 적용
![스크린샷 2021-05-17 오후 9 14 57](https://user-images.githubusercontent.com/37948906/118487059-4c970180-b755-11eb-9abd-1f9049471c6c.png)

1. Google Style의 기본 Tab size, Indent가 2로 설정되어 있으므로 4로 변경
![스크린샷 2021-05-17 오후 9 16 04](https://user-images.githubusercontent.com/37948906/118487045-499c1100-b755-11eb-9ff9-c3f3a1f5fbb0.png)

</div>
</details>

##### 7.3. 저장 시 자동 정렬 설정하는 방법 (매크로)

<details>
<div markdown="1">

단축키를 누르면 자동 정렬이 되지만, 가끔 까먹고 안할 때가 있다. 자동으로 저장할 때 정렬해주면 편하지 않을까?
(가끔 정렬하면 더 구린 경우 팀원들에게 욕을 먹을 수 있으니 주의한다. 그래도 일단 궁금하니까 찾아본다.)

1. Save Action 플러그인을 설치한다.
  
![스크린샷 2021-05-17 오후 9 42 14](https://user-images.githubusercontent.com/37948906/118490428-e3b18880-b758-11eb-9dad-a7ccd2f5bd8b.png)

2. Save Action 플러그인의 옵션을 킨다

![스크린샷 2021-05-17 오후 9 46 54](https://user-images.githubusercontent.com/37948906/118491057-941f8c80-b759-11eb-92ce-970c55ab093b.png)

[General]

- Activate save actions on save (before saving each file, performs the configured actions below)
  - 저장시 활성화 (각 파일을 저장하기 전에 아래에 구성된 작업 수행)
- Activate save actions on shorcut (default "CTRL + SHIFT + S")
  - 단축키로 저장했을 때 활성화 (기본값 "CTRL + SHIFT + S")
- Activate save actions on batch
  - 일괄 작업 시 활성화
- No action if compile errors(applied per file)
  - 컴파일 오류 발생시 조치 없음 (파일별로 적용)

[Formatting Actions]
- Optimize imports
  - import 최적화
- Reformat file
  - file 재정렬

1. java 파일만 자동 정렬하도록 설정한다. (필요하면 다른 것 추가)

![스크린샷 2021-05-17 오후 9 47 17](https://user-images.githubusercontent.com/37948906/118491036-908c0580-b759-11eb-9411-487263e8f342.png)

![스크린샷 2021-05-17 오후 9 47 07](https://user-images.githubusercontent.com/37948906/118491052-9386f600-b759-11eb-8c09-9439f068e8f3.png)

</div>
</details>


## 참고자료

- 클린 코드
- https://velog.io/@injoon2019/IntelliJ%EC%97%90-Google-Java-Style-Guide-%EC%A0%81%EC%9A%A9%ED%95%98%EA%B8%B0
- https://nesoy.github.io/articles/2018-09/Intellij-Auto-Convention
- https://google.github.io/styleguide/javaguide.html
- https://github.com/spring-projects/spring-framework/wiki/Code-Style