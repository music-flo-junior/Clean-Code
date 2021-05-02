# 3장. 함수

## 함수를 읽기 쉽고 이해하기 쉽게 짜는 방법

### 1. 작게 만들어라
- 함수는 짧고 간결할수록 좋다.
#### 1.1 함수의 길이와 가로 글자 수
- 요즘은 한 화면에 글꼴을 조절하면 가로 150자 세로 100줄도 들어간다. 따라서 가로 150자를 넘어서는 안된다. (반드시 한 화면에 들어가야 한다.)
- 함수는 100줄을 넘어서는 안된다. 아니, 20줄도 길다!

#### 1.2 블록과 들여쓰기
- `If 문 / else 문 / while문에 들어가는 블록은 한 줄이어야 한다.`
- 블록 안에서 호출하는 함수 이름을 적절히 짓는다면, 코드를 이해하기도 쉬워진다.
- `중첩 구조가 생길만큼 함수가 커져서는 안된다.` 그러므로 함수에서 들여쓰기 수준은 1단이나 2단을 넘어서면 안된다. 그래야 함수는 읽고 이해하기 쉬워진다.

### 2. 한 가지만 해라
- `함수는 한 가지를 해야 한다. 그 한 가지를 잘 해야 한다. 그 한 가지만을 해야 한다.`
- 어떻게 함수가 한 가지만 하고 있는지 판단할 수 있을까?
    - 단순히 다른 표현이 아니라 의미 있는 이름으로 다른 함수를 추출할 수 있다면 그 함수는 여러 작업을 하는 셈이다.
    - 한 가지 작업만 하는 함수는 자연스럽게 여러 섹션으로 나누기 어렵다.

### 3. 함수 당 추상화 수준은 하나로
- 함수가 확실히 '한 가지' 작업만 하려면 함수 내 모든 문장의 추상화 수준이 동일해야 한다.
- 한 함수 내 에 추상화 수준을 섞으면 코드를 읽는 사람이 헷갈린다. 특정 표현이 근본 개념인지 아니면 세부 사항인지 구분하기 어려운 탓이다.
- 근본 개념과 세부 사항을 뒤섞기 시작하면, 깨어진 창문처럼 사람들이 함수에 세부사항을 점점 더 추가한다.

#### 위에서 아래로 코드 읽기: 내려가기 규칙
- 코드는 위에서 아래로 이야기처럼 읽혀야 좋다. 한 함수 다음에는 추상화 수준이 한 단계 낮은 함수가 온다. 즉, 위에서 아래로 프로그램을 읽으면 함수 추상화 수준이 한 번에 한 단계씩 낮아진다.

### 4. Switch 문


### 5. 서술적인 이름을 사용하라

### 6. 함수 인수

### 7. 부수 효과를 일으키지 마라

### 8. 명령과 조회를 분리하라

### 9. 오류 코드보다 예외를 사용하라

### 10. 반복하지 마라

### 11. 구조적 프로그래밍

### 12. 함수를 어떻게 짜죠?

## 결론

## 예시 코드

### 리팩토링 전 upsertChannel

```java
    public boolean upsertChannel(ContentsUpdateChannelParam updateChannelParam) throws Exception {

        ContentsSearchTrackParam searchTrackParam = new ContentsSearchTrackParam();
        searchTrackParam.setPaging(false);
        searchTrackParam.setTrackIdList(updateChannelParam.getTrackIdList());

        updateChannelParam.setChnlPlayTm(searchTrackParam.getTrackIdList().size() > 0 ? this.getPlayTm(channelMapper.selectTrackList(searchTrackParam)) : "0");

        if (null == updateChannelParam.getChnlId()) {
            // 채널 등록
            LocalDateTime renewDate = LocalDateTime.now();

            updateChannelParam.setRenewDtime(renewDate);
            updateChannelParam.setRenewTrackCnt(0);

            channelMapper.insertChannel(updateChannelParam);
            this.insertChannelMultiLink(updateChannelParam);

            if (ChnlType.AFLO == updateChannelParam.getChnlType()) {
                channelMapper.insertAfloChnl(updateChannelParam);
            }

            if (ChnlType.AFLO != updateChannelParam.getChnlType() && !ObjectUtils.isEmpty(updateChannelParam.getOrgGenreId())) {
                // 채널 장르 맵핑 데이터 추가
                channelMapper.deleteMapSvcGenreChannel(updateChannelParam);
                // 완료
                channelMapper.insertMapSvcGenreChannel(updateChannelParam);
            }

            // 채널 트랙 등록
            insertChannelTrack(updateChannelParam);
        } else {

            updateChannelParam.setRenewTrackCnt(0);

            //채널 수정
            channelMapper.updateChannel(updateChannelParam);
            this.updateChannelMultiLink(updateChannelParam);

            if (ChnlType.AFLO != updateChannelParam.getChnlType() && !ObjectUtils.isEmpty(updateChannelParam.getOrgGenreId())) {
                // 채널 장르 맵핑 데이터 추가
                channelMapper.deleteMapSvcGenreChannel(updateChannelParam);
                channelMapper.insertMapSvcGenreChannel(updateChannelParam);
            } else {
                channelMapper.deleteMapSvcGenreChannel(updateChannelParam);
            }

            //채널 트랙 수정
            updateChannelTrack(updateChannelParam);
        }

        // 이미지 처리
        updateChannelImg(updateChannelParam);

        // 유효한 트랙 갯수로 업데이트
        channelMapper.updateChannelTrackCnt(updateChannelParam.getChnlId());

        //채널 타이틀곡 업데이트
        Long titleTrackId = updateChannelParam.getTitleTrackId();
        if (updateChannelParam.getTrackIdList().size() > 0) {
            if (titleTrackId == null) {
                //첫번째 trackId 로
                titleTrackId = updateChannelParam.getTrackIdList().get(0);
            }
            channelMapper.updateInitChannelTitleYn(updateChannelParam.getChnlId());
            channelMapper.updateChannelTitleYn(updateChannelParam.getChnlId(), titleTrackId);
        }

        // 이력 생성
        insertChannelHistory(updateChannelParam);

        // 레디스 캐시 삭제
        String channelRedisKey = String.format(godmusicChanneRedislKeyFormat, updateChannelParam.getChnlId());
        redisService.delWithPrefix(channelRedisKey); // 체널 캐시 삭제

        for (String key : godmusicRecommendRedislKey) { // 추천 캐시 삭제
            redisService.delWithPrefix(key);
        }

        return true;
    }
```

### 참고 자료
- Clean Code
