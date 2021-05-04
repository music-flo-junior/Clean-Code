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
import com.sktechx.godmusic.search.rest.model.vo.keyword.SearchKeywordItemVo;
import com.sktechx.godmusic.search.rest.model.vo.keyword.SearchKeywordRisingVo;
import com.sktechx.godmusic.search.rest.model.vo.keyword.SearchKeywordVo;
import com.sktechx.godmusic.search.rest.model.vo.search.SearchResultItemVo;
import com.sktechx.godmusic.search.rest.model.vo.search.SearchResultVo;
import com.sktechx.godmusic.search.rest.model.vo.search.SpecifySearchResultVo;
import com.sktechx.godmusic.search.rest.service.KeywordSearchService;
import com.sktechx.godmusic.search.rest.service.SearchService;
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
    // 내 취향 믹스를 더 명확하게 표현하기위해 My를 추가했다.
    private final MyTasteMixService myTasteMixService;
    private final KeywordSearchService keywordSearchService;
    private final KeywordAutoCompletionService keywordAutoCompletionService;

    @ApiOperation(value = "통합 검색", httpMethod = "GET", notes = "음원 검색 <br>AI Cell version")
    @GetMapping("/integration")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "x-gm-app-version", paramType = "header", value = "하위버전 체크 app-version param", defaultValue = "4.3.0"),
            @ApiImplicitParam(name = CommonConstant.X_GM_APP_NAME, paramType = "header", value = "앱 이름", type = "string"),
            @ApiImplicitParam(name = CommonConstant.X_GM_MEMBER_NO, paramType = "header", value = "회원 번호", type = "long"),
            @ApiImplicitParam(name = CommonConstant.X_GM_CHARACTER_NO, paramType = "header", value = "캐릭터 번호", type = "long")
    })
    // queryIntegration > integrationSearch 가 더 명확하게 해당 매소드의 기능을 표현한다.
    // AIIntegrationSearchRequest > AI가 AI검색셀을 의미하는 건데, 굳이 필요한가 해서 제거
    public CommonApiResponse<SearchResultVo> integrationSearch(@Valid IntegrationSearchRequest searchRequest) {
        GMContext gmContext = GMContext.getContext();

        // getSearchKeyword > replaceSearchKeywordByLength : 키워드를 50자 이내로 자르는 기능을 하는 메소드로 정확한 기능을 의미하는 이름으로 변경한다.
        String searchKeyword = SearchKeywordUtils.replaceSearchKeywordByLength(searchRequest.getKeyword());

        log.debug("searchKeyword={}", searchKeyword);

        // queryIntegration > integrationSearch : integrationSearch 가 더 명확한 표현이다.
        SearchResultVo result = searchService.integrationSearch(gmContext, searchKeyword, searchRequest.getSortType());

        // underVersionRemoveDimYItem > checkUnderVersionAndRemoveDimYItem : 누구든 보고 이해할 수 있는 이름을 사용해야한다.
        this.checkUnderVersionAndRemoveDimYItem(gmContext, result);

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
    // query > searchByType / tabSearch 가 더 명확한 이름이라고 생각한다.
    // AIIntegrationSearchRequest > AI가 AI검색셀을 의미하는 건데, 굳이 필요한가 해서 제거
    public CommonApiResponse<SearchResultVo> searchByType(@ApiIgnore @PageableDefault(page = 1) Pageable pageable,
                                                                @Valid SearchRequest searchRequest) {
        GMContext gmContext = GMContext.getContext();

        String searchKeyword = SearchKeywordUtils.replaceSearchKeywordByLength(searchRequest.getKeyword());

        log.debug("searchKeyword={}", searchKeyword);

        SearchResultVo result;

        // 내 취향 MIX
        if (searchRequest.isMixable()) { //isMixable은 잘 작명한 것 같다.
            // query > searchBySearchType : 더 명확하게 검색 타입에 따른 검색 메소드를 의미한다.
            result = myTasteMixService.searchByType(
                    gmContext,
                    pageable,
                    searchKeyword,
                    searchRequest.getSearchType(),
                    searchRequest.getSortType(),
                    searchRequest.getMixYn()
            );
        } else {
            result = searchService.searchByType(
                    gmContext,
                    pageable,
                    searchKeyword,
                    searchRequest.getSearchType(),
                    searchRequest.getSortType()
            );
        }

        this.checkUnderVersionAndRemoveDimYItem(gmContext, result);

        return new CommonApiResponse<>(result);
    }

    @ApiOperation(value = "필드 지정 검색 (track, artist, album)")
    @GetMapping("/specify")
    // specify > fieldDesignationSearch : 필드지정이라는 의미를 명확하게 하도록 이름을 변경했다.
    public CommonApiResponse<FieldDesignationSearchResultVo> fieldDesignationSearch(FieldDesignationSearchRequest request) {

        String trackKeyword = request.getTrackKeyword();
        String artistKeyword = request.getArtistKeyword();
        String albumKeyword = request.getAlbumKeyword();

        log.info("keyword :: track='{}', artist='{}', album='{}'", trackKeyword, artistKeyword, albumKeyword);

        // 검색 키워드가 아무것도 입력되지 않았을 경우 예외 처리
        if (StringUtils.isAllBlank(trackKeyword, artistKeyword, albumKeyword)) {
            throw new CommonBusinessException(CommonErrorDomain.BAD_REQUEST);
        }

        // specify > fieldDesignationSearch : 필드지정이라는 의미를 명확하게 하도록 이름을 변경했다.
        SpecifySearchResultVo result = searchService.fieldDesignationSearch(request);
        return new CommonApiResponse<>(result);
    }

    @ApiOperation(value = "자동완성(순간 검색)", httpMethod = "GET", notes = "자동완성 위한 순간 검색")
    @GetMapping(value = "/autoCompletion")
    // 순간 검색의 의미로 instant(즉시)를 사용하는 것 보다는 실제 키워드 자동완성 기능을 의미하는 keywordAutoCompletion 이 더 명확한 것 같다.
    public CommonApiResponse<SearchKeywordVo> keywordAutoCompletion(@RequestParam(name = "keyword") String keyword) {

        Long characterNo = GMContext.getContext().getCharacterNo();
        Long memberNo = GMContext.getContext().getMemberNo();

        SearchKeywordVo searchKeywordVo = SearchKeywordVo.builder()
                // instantSearchService 보다는 keywordAutoCompletionService 가 더 명확한 것 같다.
                // searchInstantV2 > searchKeywordAutoCompletion : 더 명확한 키워드 자동완성 검색 메소드의 이름을 지정했다.
                .list(Collections.singletonList(keywordAutoCompletionService.searchKeywordAutoCompletion(memberNo, characterNo, keyword)))
                .build();

        return new CommonApiResponse<>(searchKeywordVo);
    }

    @ApiOperation(value = "키워드 조회", httpMethod = "GET", notes = "인기/급상승 키워드 검색")
    // 인기/급상승 키워드 차트이므로 keywordChart 로 정확하게 명시를 하고 싶다.
    @GetMapping(value = "/keywordChart")
    // getKeywords > getKeyWordCharts 로 변경
    public CommonApiResponse<SearchKeywordVo> getKeyWordCharts() {

        // 인기 키워드 >> 어드민 개발이 이루어 지지 않아 운영되지 않고 있는 상태, 어드민 개발 완료되면 추가 될 예정

        // 급상승 키워드를 가져오기 때문에 SearchKeywordRisingVo > RisingSearchKeywordVo 로 변경한다.
        // getKeywordRising > getRisingKerword 가 더 바로 이해가 되고 쉽게 읽혀진다.
        SearchKeywordItemVo<RisingSearchKeywordVo> RisingKeywordVo = keywordSearchService.getRisingKerword();

        // keywordRisingVoList > risingKeywordVoList 가 더 바로 이해가 되고 쉽게 읽혀진다.
        List<SearchKeywordRisingVo> risingKeywordVoList = RisingKeywordVo.getList();

        if (CollectionUtils.isEmpty(risingKeywordVoList)) {
            throw new CommonBusinessException(CommonErrorDomain.EMPTY_DATA);
        }

        List<SearchKeywordItemVo> list = new ArrayList<>();

        if (risingKeywordVoList.size() > 0) {
            list.add(risingKeywordVo);
        }

        // searchKeywordVo > searchChartKeywordVo 인기/급상승 키워드 차트이므로
        SearchKeywordVo searchChartKeywordVo = SearchKeywordVo.builder().list(risingKeywordVoList).build();
        return new CommonApiResponse<>(searchKeywordVo);
    }

    /**
     * 5.4.0 보다 하위버전 이거나 빅스비인 경우, DIM 처리된 검색 결과 값 제외 처리
     */
    public void checkUnderVersionAndRemoveDimYItem(GMContext gmContext, SearchResultVo result) {
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
