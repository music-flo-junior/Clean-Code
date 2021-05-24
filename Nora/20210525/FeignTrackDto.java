/*
 * Copyright (c) 2020 DREAMUS COMPANY.
 * All right reserved.
 *
 * This software is the confidential and proprietary information of DREAMUS COMPANY.
 * You shall not disclose such Confidential Information and
 * shall use it only in accordance with the terms of the license agreement
 * you entered into with DREAMUS COMPANY.
 */

package com.sktechx.godmusic.search.rest.model.dto.base;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sktechx.godmusic.search.rest.util.ValueUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

/**
 * 설명 : Feign TRACK DTO
 * 검색에서 meta-mgo 를 호출하여 track 정보를 가져올때 사용하는 DTO
 *
 * DTO는 클린코드에서
 *
 * DTO의 특수한 형태다.
 * 공개 변수가 있거나 비공개 변수에 getter/setter가 있는 자료 구조지만, 대게 save나 find와 같은 탐색 함수도 제공한다.
 *
 * 라고 정의되는데 FeignTrackDto에는 잡종코드?가 보이는 것 같아 찾아봤다!
 *
 * @author Groot(조민국) / dev.mingood@sk.com
 * @since 2020. 06. 15
 */
@Getter
@ToString
public class FeignTrackDto {
    @JsonProperty("id")
    private Long trackId;
    private Long agencyId;

    private String name;
    private String playTime;
    private String subtractQty;

    private String holdbackYn;
    private String originTrackYn;
    private String adultAuthYn;
    private String displayYn;
    private String downloadYn;
    private String likeYn;
    private String svcStreamingYn;
    private String svcDrmYn;

    private String freeYn;
    private String svcFlacYn;
    private String titleYn;
    private String unReleasedYn;

    private TrackAlbumDto album;

    private TrackArtistDto representationArtist;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime createDateTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime dispStartDtime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime updateDateTime;

    private List<TrackArtistDto> artistList;
    private List<TrackArtistDto> trackArtistList;

    public List<TrackArtistDto> getArtistListOrEmpty() {
        return ValueUtils.getListOrEmpty(artistList);
    }

    public List<TrackArtistDto> getTrackArtistListOrEmpty() {
        return ValueUtils.getListOrEmpty(trackArtistList);
    }

    public TrackAlbumDto getAlbumOrEmpty() {
        return album == null ? TrackAlbumDto.empty() : album;
    }

    public boolean hasAlbum() {
        return album != null;
    }

    public boolean haveNoAlbum() {
        return !hasAlbum();
    }

    public boolean isValidTrack() {
        return StringUtils.isNotBlank(svcStreamingYn);
    }

    // 아래 메소드를 DTO 에서 하는게 맞나? 따로 데이터를 가져가서 DTO가 아닌 service 단에 함수 정의를 하는 게 맞지 않나 생각이 듬!
    public long getPlayTimeAsSeconds() {

        String[] splitRunningTime = StringUtils.defaultIfBlank(playTime, "").split(":");
        long oneMinuteSeconds = 60L,
                baseSecondsToBeMultiplied = (long) Math.pow(oneMinuteSeconds, splitRunningTime.length - 1);

        try {

            long playTimeAsSeconds = 0L;

            for (String time : splitRunningTime) {

                playTimeAsSeconds += Long.parseLong(time) * baseSecondsToBeMultiplied;
                baseSecondsToBeMultiplied /= oneMinuteSeconds;
            }

            return playTimeAsSeconds;

        } catch (NumberFormatException ignore) {
        }

        return 0L;
    }

    @SuppressWarnings("WeakerAccess")
    @Getter
    @ToString
    public static class TrackAlbumDto {

        @JsonProperty("id")
        private Long albumId;

        private String title;
        private String releaseYmd;
        private String genreStyle;
        private String albumType;
        private String albumTypeStr;

        private List<FeignImageDto> imgList;

        @Getter(AccessLevel.NONE)
        private Map<Integer, FeignImageDto> imgSizeImageMap;

        public List<FeignImageDto> getImgListOrEmpty() {
            return ValueUtils.getListOrEmpty(imgList);
        }

        // 아래 메소드를 DTO 에서 하는게 맞나? 차라리 Util을 하나 만드는게 나을 것 같다는 생각이 든다.
        public String getImageUrlBySize(Integer size) {

            if (imgSizeImageMap == null) {
                imgSizeImageMap =
                        getImgListOrEmpty().stream().collect(toMap(FeignImageDto::getSize, Function.identity(), (dupA, dupB) -> dupA));
            }

            return imgSizeImageMap
                    .getOrDefault(size, imgSizeImageMap.isEmpty() ? EMPTY_IMG : imgSizeImageMap.values().iterator().next())
                    .getUrl();
        }

        public static TrackAlbumDto empty() {
            return EMPTY;
        }

        private static TrackAlbumDto EMPTY = new TrackAlbumDto();
        private static FeignImageDto EMPTY_IMG = new FeignImageDto();
    }

    // 이거는 퍼펙트한 DTO ^_^
    @Getter
    @ToString
    public static class TrackArtistDto {

        @JsonProperty("id")
        private Long artistId;

        private String name;
        private String roleName;
        private String roleCd;

    }
}
