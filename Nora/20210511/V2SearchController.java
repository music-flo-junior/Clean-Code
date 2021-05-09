/*
 * Copyright (c) 2019 DREAMUS COMPANY.
 * All right reserved.
 *
 * This software is the confidential and proprietary information of DREAMUS COMPANY.
 * You shall not disclose such Confidential Information and
 * shall use it only in accordance with the terms of the license agreement
 * you entered into with DREAMUS COMPANY.
 */

package com.sktechx.godmusic.search.rest.controller.v2;

import com.sktechx.godmusic.lib.domain.CommonApiResponse;
import com.sktechx.godmusic.lib.domain.CommonConstant;
import com.sktechx.godmusic.lib.domain.GMContext;
import com.sktechx.godmusic.lib.domain.exception.CommonBusinessException;
import com.sktechx.godmusic.lib.domain.exception.CommonErrorDomain;
import com.sktechx.godmusic.search.common.util.VersionUtils;
import com.sktechx.godmusic.search.rest.model.vo.AI.AIIntegrationSearchRequest;
import com.sktechx.godmusic.search.rest.model.vo.AI.AISearchRequest;
import com.sktechx.godmusic.search.rest.model.vo.AI.AISpecifySearchRequest;
import com.sktechx.godmusic.search.rest.model.vo.keyword.SearchKeywordItemVo;
import com.sktechx.godmusic.search.rest.model.vo.keyword.SearchKeywordRisingVo;
import com.sktechx.godmusic.search.rest.model.vo.keyword.SearchKeywordVo;
import com.sktechx.godmusic.search.rest.model.vo.search.SearchResultItemVo;
import com.sktechx.godmusic.search.rest.model.vo.search.SearchResultVo;
import com.sktechx.godmusic.search.rest.model.vo.search.SpecifySearchResultVo;
import com.sktechx.godmusic.search.rest.service.InstantSearchService;
import com.sktechx.godmusic.search.rest.service.KeywordSearchService;
import com.sktechx.godmusic.search.rest.service.SearchService;
import com.sktechx.godmusic.search.rest.service.TasteMixService;
import com.sktechx.godmusic.search.rest.util.SearchKeywordUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.sktechx.godmusic.search.common.util.CommonUtils.getValueOrDefault;

@SuppressWarnings("rawtypes")
@RequiredArgsConstructor
@Slf4j
@Api(tags = "검색 API", value = "검색")
@RequestMapping(value = "search/v2/search")
@RestController
public class V2SearchController {

    private final SearchService searchService;
    private final TasteMixService tasteMixService;
    private final KeywordSearchService keywordSearchService;
    private final InstantSearchService instantSearchService;

    @ApiOperation(value = "통합 검색", httpMethod = "GET", notes = "음원 검색 <br>AI Cell version")
    @GetMapping("/integration")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "x-gm-app-version", paramType = "header", value = "하위버전 체크 app-version param", defaultValue = "4.3.0"),
            @ApiImplicitParam(name = CommonConstant.X_GM_APP_NAME, paramType = "header", value = "앱 이름", type = "string"),
            @ApiImplicitParam(name = CommonConstant.X_GM_MEMBER_NO, paramType = "header", value = "회원 번호", type = "long"),
            @ApiImplicitParam(name = CommonConstant.X_GM_CHARACTER_NO, paramType = "header", value = "캐릭터 번호", type = "long")
    })
    public CommonApiResponse<SearchResultVo> queryIntegration(@Valid AIIntegrationSearchRequest searchRequest) {
        GMContext gmContext = GMContext.getContext();

        String searchKeyword = SearchKeywordUtils.getSearchKeyword(searchRequest.getKeyword());

        log.debug("searchKeyword={}", searchKeyword);

        SearchResultVo result = searchService.queryIntegration(gmContext, searchKeyword, searchRequest.getSortType());

        this.underVersionRemoveDimYItem(gmContext, result);

        return new CommonApiResponse<>(result);
    }

    @ApiOperation(value = "검색 (NEW)", httpMethod = "GET", notes = "음원 검색 <br>AI Cell version")
    @GetMapping
    @ApiImplicitParams({
            @ApiImplicitParam(name = "x-gm-app-version", paramType = "header", value = "하위버전 체크 app-version param", defaultValue = "4.3.0"),
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query", value = "페이지 번호 (1..N)", defaultValue = "1"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query", value = "페이지 크기", defaultValue = "10"),
            @ApiImplicitParam(name = CommonConstant.X_GM_APP_NAME, paramType = "header", value = "앱 이름", type = "string"),
            @ApiImplicitParam(name = CommonConstant.X_GM_MEMBER_NO, paramType = "header", value = "회원 번호", type = "long"),
            @ApiImplicitParam(name = CommonConstant.X_GM_CHARACTER_NO, paramType = "header", value = "캐릭터 번호", type = "long")
    })
    public CommonApiResponse<SearchResultVo> query(@ApiIgnore @PageableDefault(page = 1) Pageable pageable,
                                                   @Valid AISearchRequest searchRequest) {
        GMContext gmContext = GMContext.getContext();

        String searchKeyword = SearchKeywordUtils.getSearchKeyword(searchRequest.getKeyword());

        log.debug("searchKeyword={}", searchKeyword);

        SearchResultVo result;

        /* 해당 부분
        // 내 취향 MIX 주석 삭제하고
        searchRequest.isMixable() > searchRequest.isMyTasteMixSearch()로 변하하여
        주석 없이 코드만 보고 파악 가능하게 수정한다.
        */

        // 내 취향 MIX
        if (searchRequest.isMixable()) {
            result = tasteMixService.query(
                    gmContext,
                    pageable,
                    searchKeyword,
                    searchRequest.getSearchType(),
                    searchRequest.getSortType(),
                    searchRequest.getMixYn()
            );
        } else {
            result = searchService.query(
                    gmContext,
                    pageable,
                    searchKeyword,
                    searchRequest.getSearchType(),
                    searchRequest.getSortType()
            );
        }

        this.underVersionRemoveDimYItem(gmContext, result);

        return new CommonApiResponse<>(result);
    }

    @ApiOperation(value = "필드 지정 검색 (track, artist, album)")
    @GetMapping("/specify")
    public CommonApiResponse<SpecifySearchResultVo> specifySearch(AISpecifySearchRequest request) {
        String trackKeyword = request.getTrackKeyword();
        String artistKeyword = request.getArtistKeyword();
        String albumKeyword = request.getAlbumKeyword();

        log.info("keyword :: track='{}', artist='{}', album='{}'", trackKeyword, artistKeyword, albumKeyword);

        // 검색 키워드가 아무것도 입력되지 않았을 경우 예외 처리


        /* 해당 부분
       // 검색 키워드가 아무것도 입력되지 않았을 경우 예외 처리
       라는 주석 없이 checkBlankKeywords()라는 함수를 구현하여 주석 없이 코드만 보고 파악 가능하게 수정한다.
        */

        if (StringUtils.isAllBlank(trackKeyword, artistKeyword, albumKeyword)) {
            throw new CommonBusinessException(CommonErrorDomain.BAD_REQUEST);
        }

        SpecifySearchResultVo result = searchService.querySpecify(request);
        return new CommonApiResponse<>(result);
    }

    @ApiOperation(value = "자동완성(순간 검색)", httpMethod = "GET", notes = "자동완성 위한 순간 검색")
    @GetMapping(value = "/instant")
    public CommonApiResponse<SearchKeywordVo> instantSearch(@RequestParam(name = "keyword") String keyword) {
        Long characterNo = GMContext.getContext().getCharacterNo();
        Long memberNo = GMContext.getContext().getMemberNo();

        SearchKeywordVo searchKeywordVo = SearchKeywordVo.builder()
                .list(Collections.singletonList(instantSearchService.searchInstantV2(memberNo, characterNo, keyword)))
                .build();

        return new CommonApiResponse<>(searchKeywordVo);
    }

    @ApiOperation(value = "키워드 조회", httpMethod = "GET", notes = "인기/급상승 키워드 검색")
    @GetMapping(value = "/keyword")
    public CommonApiResponse<SearchKeywordVo> getKeywords() {

        // 인기 키워드 >> 어드민 개발이 이루어 지지 않아 운영되지 않고 있는 상태, 어드민 개발 완료되면 추가 될 예정

        /* 해당 부분은 코드의 히스토리 파악에 필요한 주석으로 유지되어야 한다고 생각함 */

        SearchKeywordItemVo<SearchKeywordRisingVo> keywordRisingVo = keywordSearchService.getKeywordRising();
        List<SearchKeywordRisingVo> keywordRisingVoList = keywordRisingVo.getList();

        if (CollectionUtils.isEmpty(keywordRisingVoList)) {
            throw new CommonBusinessException(CommonErrorDomain.EMPTY_DATA);
        }

        List<SearchKeywordItemVo> list = new ArrayList<>();

        if (keywordRisingVoList.size() > 0) {
            list.add(keywordRisingVo);
        }

        SearchKeywordVo searchKeywordVo = SearchKeywordVo.builder().list(list).build();
        return new CommonApiResponse<>(searchKeywordVo);
    }

    /**
     * 5.4.0 보다 하위버전 이거나 빅스비인 경우, DIM 처리된 검색 결과 값 제외 처리
     */

    /* underVersionRemoveDimYItem > checkDimYItemAndRemoveOfResult(?)
    * 빅스비와 하위버전을 함수명에 나타내야하는데 어려움..논의필요! -- 쪼개는 걸로 처리.
    * isBixbyOrBelow540Version
    * RemoveDimYItem
    * */

    public void underVersionRemoveDimYItem(GMContext gmContext, SearchResultVo result) {
        String appVer = gmContext.getAppVer();
        String appName = getValueOrDefault(gmContext.getAppName(), "");
        boolean isBelow540Version = VersionUtils.isBelowVersion("5.4.0", appVer);
        if (!CollectionUtils.isEmpty(result.getList()) && (isBelow540Version || appName.startsWith("BIXBY"))) {
            for (SearchResultItemVo item : result.getList()) {
                item.removeDimYItem();
            }
        }
    }
}