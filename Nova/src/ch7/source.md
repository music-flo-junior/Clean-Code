```java
    public boolean updateChannel(ContentsUpdateChannelParam updateChannelParam) throws Exception {
        if (Objects.isNull(updateChannelParam.getChnlId())) throw new FloAdminException(CommonErrorDomain.BAD_REQUEST);
        setUpdateParamChannelPlayTimeAndTrackCnt(updateChannelParam);
        channelMapper.updateChannel(updateChannelParam);
        afterUpdate(updateChannelParam);
        return true;
    }
```


```java
    @Transactional
    public void updateChannelAndSendMq(ContentsUpdateChannelParam param) throws Exception {
        param.setPrevChannelDispStatusType(channelMapper.selectChannelDispStatusType(param.getChnlId()));
        if (this.updateChannel(param)) {
            // MQ 이벤트 전송
            this.sendMqChannelUpdateEvent(param);
        } else {
            log.info("Message >>> Nothing amqpService");
        }
    }
```

```java
    public void updateChannel(ContentsUpdateChannelParam updateChannelParam) {
        if (Objects.isNull(updateChannelParam.getChnlId())) throw new FloAdminException(CommonErrorDomain.BAD_REQUEST);
        setUpdateParamChannelPlayTimeAndTrackCnt(updateChannelParam);
        channelMapper.updateChannel(updateChannelParam);
        afterUpdate(updateChannelParam);
    }
```

```java
    @Transactional
    public void updateChannelAndSendMq(ContentsUpdateChannelParam param){
        param.setPrevChannelDispStatusType(channelMapper.selectChannelDispStatusType(param.getChnlId()));
        updateChannel(param);
        sendMqChannelUpdateEvent(param);
    }
```