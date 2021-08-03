/*
 * Copyright (c) 2020 DREAMUS COMPANY.
 * All right reserved.
 * This software is the confidential and proprietary information of DREAMUS COMPANY.
 * You shall not disclose such Confidential Information and
 * shall use it only in accordance with the terms of the license agreement
 * you entered into with DREAMUS COMPANY.
 */

package com.dreamuscompany.floadmin.display.service;

import com.dreamuscompany.floadmin.display.domain.dto.*;
import com.dreamuscompany.floadmin.display.mapper.DisplayGenreArtistMapper;
import com.dreamuscompany.floadmin.display.mapper.DisplayPreferMapper;
import com.dreamuscompany.floadmin.module.infra.security.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@Slf4j
/*
* DisplayGenreArtistService : 전시관리 > 장르별 아티스트
* - 장르별 아티스트
* - 선호도 파악 장르
* - 장르별 유사 아티스트
* 세 가지의 기능을 다 하고 있는데 이를 따 쪼개서 3개의 클래스를 만드는 것이 깨끗한 클래스라는 생각이 든다.
*/
public class DisplayGenreArtistService {

    // 장르별 아티스트 Mapper
	@Autowired
	private DisplayGenreArtistMapper displayGenreArtistMapper;

    // 선호도 파악 > 장르 Mapper
	@Autowired
	private DisplayPreferMapper displayPreferMapper;

	// 장르별 아티스트 조회
	public List<TbGenreArtistDto> selectGenreArtist(Long preferGenreId) {
		return displayGenreArtistMapper.selectGenreArtist(preferGenreId);
    }

    // 장르별 유사 아티스트 조회
	public List<TbSimilarArtistDto> selectGenreSimilarArtist(TbGenreArtistDto artist) {
		return displayGenreArtistMapper.selectGenreSimilarArtist(artist);
    }

    // 아티스트 조회
	public List<TbGenreArtistDto> searchArtist(String keyword, Pageable pageable) {
		return displayGenreArtistMapper.searchArtist(keyword, pageable);
	}

    // 아티스트 건 수 조회
	public Integer searchArtistCount(String keyword) {
		return displayGenreArtistMapper.searchArtistCount(keyword);
	}

    // 장르별 유사 아티스트 검색
	public List<TbGenreArtistDto> searchSimilarArtist(Long artistId, Pageable pageable) {
		return displayGenreArtistMapper.searchSimilarArtist(artistId, pageable);
	}

    // 장르별 유사 아티스트 건 수 검색
	public Integer searchSimilarArtistCount(Long artistId) {
		return displayGenreArtistMapper.searchSimilarArtistCount(artistId);
	}

    // 장르별 아티스트 저장하기
	@Transactional
	public void saveGenreStatus(User user, List<TbGenreArtistDto> genreArtist, List<TbGenreArtistDto> delGenreArtist) {

		List<TbGenreArtistDto> newGenreArtist = new ArrayList<>();
		List<TbGenreArtistDto> modifyGenreArtist = new ArrayList<>();
		List<TbGenreArtistDto> modifySimilarArtist = new ArrayList<>();
		
		for(int i=0; i<genreArtist.size(); i++) {
			TbGenreArtistDto artistDto = genreArtist.get(i);
			String inputStatus = artistDto.getInputStatus();
			
			if("new".equals(inputStatus)) {	
				newGenreArtist.add(artistDto);
			} else  {
				
				if ("modify".equals(inputStatus)) {
					modifyGenreArtist.add(artistDto);
					modifySimilarArtist.add(artistDto);
				} else {
					boolean isSAModify = false;
					List<TbSimilarArtistDto> similarArtist = artistDto.getSimilarArtist();
					for(int j=0; j<similarArtist.size(); j++) {
						String sInStatus = similarArtist.get(j).getInputStatus();
						
						if("new".equals(sInStatus) || "modify".equals(sInStatus)) {
							isSAModify = true;
							break;
						}
					}
					if(isSAModify) {
						modifySimilarArtist.add(artistDto);
					}	
				}
			}
		}
		
		for(int i=0; i<delGenreArtist.size(); i++) {
			TbGenreArtistDto genreArtistDto = delGenreArtist.get(i);
			displayGenreArtistMapper.deleteSimilarGenreArtistByArtistId(genreArtistDto);
			displayGenreArtistMapper.deleteGenreArtist(genreArtistDto);
			log.info("saveGenreStatus DELETE ["+genreArtistDto.getPreferGenreId()+"]["+genreArtistDto.getArtistNm()+"]");

			//삭제이력추가
			TbPreferGenreChgHistDto genreChgHist = new TbPreferGenreChgHistDto();
			genreChgHist.init();
			genreChgHist.setPreferGenreId(genreArtistDto.getPreferGenreId());
			genreChgHist.setCreateUserNo(user.getUserNo());
			displayPreferMapper.insertPreferGenreChgHist(genreChgHist);
			
			TbPreferGenreArtistChgHistDto artChgHist = new TbPreferGenreArtistChgHistDto();
			artChgHist.setPreferGenreChgHistId(genreChgHist.getPreferGenreChgHistId());
			artChgHist.setPreferGenreId(genreArtistDto.getPreferGenreId());
			artChgHist.setArtistId(genreArtistDto.getArtistId().intValue());
			artChgHist.setChgAfArtistId(0);
			artChgHist.setCreateUserNo(user.getUserNo());
			displayGenreArtistMapper.insertPreferGenreArtistChgHist(artChgHist);
		}
		
		for(int i=0; i<newGenreArtist.size(); i++) {
			TbGenreArtistDto genreArtistDto = newGenreArtist.get(i);
			List<TbSimilarArtistDto> similarArtistList = genreArtistDto.getSimilarArtist();
			displayGenreArtistMapper.insertGenreArtist(genreArtistDto);
			if(similarArtistList.size() != 0) {
				displayGenreArtistMapper.insertSimilarGenreArtist(similarArtistList);	
			}
			
			log.info("saveGenreStatus NEW ["+genreArtistDto.getPreferGenreId()+"]["+genreArtistDto.getArtistNm()+"]");
			//신규이력추가
			TbPreferGenreChgHistDto genreChgHist = new TbPreferGenreChgHistDto();
			genreChgHist.init();
			genreChgHist.setPreferGenreId(genreArtistDto.getPreferGenreId());
			genreChgHist.setCreateUserNo(user.getUserNo());
			displayPreferMapper.insertPreferGenreChgHist(genreChgHist);
			
			TbPreferGenreArtistChgHistDto artChgHist = new TbPreferGenreArtistChgHistDto();
			artChgHist.setPreferGenreChgHistId(genreChgHist.getPreferGenreChgHistId());
			artChgHist.setPreferGenreId(genreArtistDto.getPreferGenreId());
			artChgHist.setArtistId(0);
			artChgHist.setChgAfArtistId(genreArtistDto.getArtistId().intValue());
			artChgHist.setCreateUserNo(user.getUserNo());
			displayGenreArtistMapper.insertPreferGenreArtistChgHist(artChgHist);
		}
		
		for(int i=0; i<modifyGenreArtist.size(); i++) {
			TbGenreArtistDto genreArtistDto = modifyGenreArtist.get(i);
			displayGenreArtistMapper.updateGenreArtist(genreArtistDto);
			
			log.info("saveGenreStatus MODIFY ["+genreArtistDto.getPreferGenreId()+"]["+genreArtistDto.getArtistNm()+"]");
		}
		
		for(int i=0; i<modifySimilarArtist.size(); i++) {
			TbGenreArtistDto genreArtistDto = modifySimilarArtist.get(i);
			List<TbSimilarArtistDto> similarArtistList = genreArtistDto.getSimilarArtist();
			List<TbSimilarArtistDto> dbSimilarArtistList = displayGenreArtistMapper.selectGenreSimilarArtist(genreArtistDto);
			displayGenreArtistMapper.deleteSimilarGenreArtistByArtistId(genreArtistDto);
			if(similarArtistList.size() != 0) {
				displayGenreArtistMapper.insertSimilarGenreArtist(similarArtistList);	
			}
			
			log.info("saveGenreStatus MODIFY SIMILAR ARTIST ["+genreArtistDto.getPreferGenreId()+"]["+genreArtistDto.getArtistNm()+"]");
			
			
			HashMap<Long, TbSimilarArtistDto> newSimArtMap = listToMap(similarArtistList);
			HashMap<Long, TbSimilarArtistDto> dbSimArtMap  = listToMap(dbSimilarArtistList);
			
			ArrayList<Integer> addArtId = new ArrayList<>();
			ArrayList<Integer> removeArtId = new ArrayList<>();
			
			
			//수정된 아티스트 로그기록..
			for(int k=0; k<similarArtistList.size(); k++) {
				TbSimilarArtistDto newSimArt = similarArtistList.get(k);
				if(dbSimArtMap.get(newSimArt.getSimilarArtistId()) == null) {
					addArtId.add(newSimArt.getSimilarArtistId().intValue());
				}
			}
			for(int k=0; k<dbSimilarArtistList.size(); k++) {
				TbSimilarArtistDto dbSimArt = dbSimilarArtistList.get(k);
				if(newSimArtMap.get(dbSimArt.getSimilarArtistId()) == null) {
					removeArtId.add(dbSimArt.getSimilarArtistId().intValue());
				}
			}
			
			if(addArtId.size() != 0) {
				TbPreferGenreChgHistDto genreChgHist = null;
				for(int k=0; k<addArtId.size(); k++) {
					if(genreChgHist == null) {
						genreChgHist = new TbPreferGenreChgHistDto();
						genreChgHist.init();
						genreChgHist.setPreferGenreId(genreArtistDto.getPreferGenreId());
						genreChgHist.setCreateUserNo(user.getUserNo());
						displayPreferMapper.insertPreferGenreChgHist(genreChgHist);	
					}
					
					try {
						TbPreferSimilarArtistChgHistDto simArtChgHist = new TbPreferSimilarArtistChgHistDto();
						simArtChgHist.setPreferGenreId(genreChgHist.getPreferGenreId());
						simArtChgHist.setPreferGenreChgHistId(genreChgHist.getPreferGenreChgHistId());
						simArtChgHist.setArtistId(genreArtistDto.getArtistId().intValue());
						simArtChgHist.setSimilarArtistId(removeArtId.get(k));
						simArtChgHist.setChgAfSimilarArtistId(addArtId.get(k));
						simArtChgHist.setCreateUserNo(user.getUserNo());
						displayGenreArtistMapper.insertPreferSimilarArtistChgHist(simArtChgHist);	
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

    // 유사아티스트 목록을 맵으로 변경
	private HashMap<Long, TbSimilarArtistDto> listToMap(List<TbSimilarArtistDto> list) {
		HashMap<Long, TbSimilarArtistDto> map = new HashMap<>();
		for(int i=0; i<list.size(); i++) {
			TbSimilarArtistDto simArt = list.get(i);
			map.put(simArt.getSimilarArtistId(), simArt);
		}
		return map;
	}
}
