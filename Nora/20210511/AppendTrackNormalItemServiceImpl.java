/*
 * Copyright (c) 2020 DREAMUS COMPANY.
 * All right reserved.
 *
 * This software is the confidential and proprietary information of DREAMUS COMPANY.
 * You shall not disclose such Confidential Information and
 * shall use it only in accordance with the terms of the license agreement
 * you entered into with DREAMUS COMPANY.
 */

package com.sktechx.godmusic.search.rest.service.impl.item.normal;

import com.sktechx.godmusic.lib.domain.GMContext;
import com.sktechx.godmusic.search.common.code.BugsMusic.SearchType;
import com.sktechx.godmusic.search.common.code.ResultStyleType;
import com.sktechx.godmusic.search.common.code.YnType;
import com.sktechx.godmusic.search.common.util.VersionUtils;
import com.sktechx.godmusic.search.rest.model.dto.TrackDto;
import com.sktechx.godmusic.search.rest.model.type.TopItemType;
import com.sktechx.godmusic.search.rest.model.vo.AI.AISearchResponse;
import com.sktechx.godmusic.search.rest.model.vo.AI.AISearchResultAllVo;
import com.sktechx.godmusic.search.rest.model.vo.AI.AISearchResultTrackVo;
import com.sktechx.godmusic.search.rest.model.vo.search.SearchResultItemVo;
import com.sktechx.godmusic.search.rest.service.AppendNormalItemService;
import com.sktechx.godmusic.search.rest.service.TasteMixService;
import com.sktechx.godmusic.search.rest.service.TrackService;
import com.sktechx.godmusic.search.rest.util.SearchConvertUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 설명 : Track Normal Item 세팅
 *
 * @author groot
 * @since 2020. 01. 03
 */
@SuppressWarnings("rawtypes")
@RequiredArgsConstructor
@Slf4j
@Service
public class AppendTrackNormalItemServiceImpl implements AppendNormalItemService {

    private final TrackService trackService;

    private final TasteMixService tasteMixService;

    @Override
    public String handleItemType() {
        return "track";
    }

    @Override
    public void appendNormalItemByItemType(TopItemType topItemType,
                                           List<SearchResultItemVo> itemList,
                                           AISearchResponse<AISearchResultAllVo> searchAll) {
        AISearchResponse<List<AISearchResultTrackVo>> aiTrackList = searchAll.getData().getTrack();
        List<TrackDto> resultTrackList = aiTrackList.getData().stream()
                .map(SearchConvertUtils::convertAISearchResultTrackVoToTrackDto)
                .collect(Collectors.toList());
        ResultStyleType styleType = ResultStyleType.TITLED;

        // 하위버전 호환 > 5.2 이하인 경우, TITLED
        GMContext gmContext = GMContext.getContext();
        String appVer = gmContext.getAppVer();
        boolean isSameOrAbove520Version = VersionUtils.isSameOrAboveVersion("5.2.0", appVer);

        /* 해당 부분
        GMContext gmContext = GMContext.getContext();
        String appVer = gmContext.getAppVer();
        boolean isTitled = VersionUtils.isSameOrAboveVersion("5.2.0",appVer); 로 변경하여
        // 하위버전 호환 > 5.2 이하인 경우, TITLED 해당 주석 삭제 가능
        */

        // 내 취향 MIX 버튼 노출 여부 체크 > 로그인 및 곡 검색 결과 5개 초과인 경우
        if (isLoggedIn(gmContext) && resultTrackList.size() > 5 && isSameOrAbove520Version) {
            styleType = ResultStyleType.TITLED_MIX;
        }

        /* 해당 부분
        if (isDisplayMixButton()) {
        {
            styleType = ResultStyleType.TITLED_MIX;
        }

         private boolean isDisplayMixButton(GMContext gmContext, List<TrackDto> resultTrackList, boolean isSameOrAbove520Version) {
             if (isLoggedIn(gmContext) && resultTrackList.size() > 5 && isSameOrAbove520Version) {
                return true;
            }
         }

        로 변경하여 // 내 취향 MIX 버튼 노출 여부 체크 > 로그인 및 곡 검색 결과 5개 초과인 경우 주석 삭제 가능
        */

        List<TrackDto> resultOriginTrackList = searchAll.getData().getOriginalTrack().getData().stream()
                .map(SearchConvertUtils::convertAISearchResultTrackVoToTrackDto)
                .collect(Collectors.toList());

        boolean isTopItemOriginTrack = this.isTopItemOriginTrack(topItemType, searchAll);
        boolean isOriginTrackExistAndNoTopItem = !CollectionUtils.isEmpty(resultOriginTrackList) && !isTopItemOriginTrack;

        if (!CollectionUtils.isEmpty(resultTrackList)) {
            resultTrackList = this.convertTrackTypeList(
                    topItemType,
                    resultTrackList,
                    isOriginTrackExistAndNoTopItem,
                    resultOriginTrackList
            );
            itemList.add(this.createSubResultItemVo(SearchType.TRACK, styleType, resultTrackList));
        }

        // 원곡 Cell 영역 만들기 (원곡이 TopItem이 아닐 경우)
        if (TopItemType.TRACK == topItemType && !isTopItemOriginTrack) {
            if (!CollectionUtils.isEmpty(resultOriginTrackList)) {
                resultOriginTrackList = this.convertOriginTrackTypeList(resultOriginTrackList);
                itemList.add(this.createOriginResultItemVo(SearchType.TRACK, resultOriginTrackList));
            }
        }


        /* 해당 부분
        if (isTopItemOriginTrack()) {
             if (!CollectionUtils.isEmpty(resultOriginTrackList)) {
                resultOriginTrackList = this.convertOriginTrackTypeList(resultOriginTrackList);
                itemList.add(this.createOriginResultItemVo(SearchType.TRACK, resultOriginTrackList));
            }
        }

         private boolean isTopItemOriginTrack(TopItemType topItemType, boolean isTopItemOriginTrack) {
             if (TopItemType.TRACK == topItemType && !isTopItemOriginTrack) {return true;}
         }

        로 변경하여  // 원곡 Cell 영역 만들기 (원곡이 TopItem이 아닐 경우 체크 > 로그인 및 곡 검색 결과 5개 초과인 경우 주석 삭제 가능
        */
    }

    private List<TrackDto> convertTrackTypeList(TopItemType topItemType,
                                                List<TrackDto> list,
                                                boolean isOriginTrackExistAndNoTopItem,
                                                List<TrackDto> resultOriginTrackList) {
        if (TopItemType.TRACK == topItemType) {
            list.remove(0);
        }

        if (!CollectionUtils.isEmpty(list)) {
            list = trackService.extractExistTrackWithYnForIntegration(list);

            if (isOriginTrackExistAndNoTopItem) {
                TrackDto originTrack = resultOriginTrackList.get(0);
                for (int i = 0; i < list.size(); ++i) {
                    if (originTrack.getTrackId().equals(list.get(i).getTrackId())) {
                        list.remove(i);
                        break;
                    }
                }
            }

            log.debug("convertTrackTypeList={}", list);
        }

        if (list.size() > 5) {
            list = list.subList(0, 5);
        }

        return list;
    }

    private boolean isTopItemOriginTrack(TopItemType topItemType, AISearchResponse<AISearchResultAllVo> searchAll) {
        if (TopItemType.TRACK == topItemType) {
            Optional<TrackDto> trackDto = searchAll.getData().getTrack().getData().stream()
                    .map(SearchConvertUtils::convertAISearchResultTrackVoToTrackDto)
                    .findFirst();

            return trackDto.isPresent() && (YnType.Y == trackDto.get().getOriginTrackYn());
        }
        return false;
    }


    @Override
    public SearchResultItemVo externalNormalItemByItemType(AISearchResponse<AISearchResultAllVo> searchAll,
                                                           Integer pageCount) {
        AISearchResponse<List<AISearchResultTrackVo>> aiTrackList = searchAll.getData().getTrack();
        List<TrackDto> resultTrackList = aiTrackList.getData().stream()
                .map(SearchConvertUtils::convertAISearchResultTrackVoToTrackDto)
                .collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(resultTrackList)) {
            resultTrackList = this.convertTrackTypeList(
                    resultTrackList,
                    pageCount
            );
            return this.createSubResultItemVo(SearchType.TRACK, resultTrackList);
        }
        return null;
    }

    private List<TrackDto> convertTrackTypeList(List<TrackDto> list, Integer pageCount) {

        if (!CollectionUtils.isEmpty(list)) {
            list = trackService.extractExistTrackWithYnForIntegration(list);

            log.debug("convertTrackTypeList={}", list);
        }

        if (list.size() > pageCount) {
            list = list.subList(0, pageCount);
        }

        return list;
    }

    private List<TrackDto> convertOriginTrackTypeList(List<TrackDto> list) {
        TrackDto trackDto = list.get(0);
        if (YnType.Y == trackDto.getOriginTrackYn()) {
            list = trackService.extractExistTrackWithYnForIntegration(Collections.singletonList(trackDto));
            log.debug("convertOriginTrackTypeList={}", list);
            return list;
        }
        return Collections.emptyList();
    }

    private SearchResultItemVo<TrackDto> createOriginResultItemVo(SearchType searchType, List<TrackDto> list) {
        return SearchResultItemVo.<TrackDto>builder()
                .type(searchType)
                .style(ResultStyleType.ORIGIN)
                .list(list)
                .build();
    }

    private boolean isLoggedIn(GMContext gmContext) {
        return gmContext.getMemberNo() != null && gmContext.getCharacterNo() != null;
    }

}