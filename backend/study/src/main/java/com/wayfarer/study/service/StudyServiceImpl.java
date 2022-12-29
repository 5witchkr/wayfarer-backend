package com.wayfarer.study.service;

import com.wayfarer.study.dto.*;
import com.wayfarer.study.entity.StudyArticle;
import com.wayfarer.study.entity.enummodel.StudyArticleEnum;
import com.wayfarer.study.entity.enummodel.StudyStatus;
import com.wayfarer.study.entity.vo.StudyInfo;
import com.wayfarer.study.entity.vo.StudyPosition;
import com.wayfarer.study.mapper.StudyMapper;
import com.wayfarer.study.repository.StudyArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;


@Primary
@Service
@RequiredArgsConstructor
@Transactional
public class StudyServiceImpl implements StudyService {

    private final StudyArticleRepository studyArticleRepository;
    private final StudyMapper studyMapper;

    @Override
    public MultiResponseDto<StudyArticleResponseDto> readAllStudyArticles(int page, Boolean status) {
        Page<StudyArticle> studyArticleList = null;
        if (status) {
             studyArticleList = studyArticleRepository
                    .findByEnabledAndStudyInfo(true, new StudyInfo(StudyStatus.PROCEED), PageRequest.of(page - 1, 10, Sort.by(StudyArticleEnum.STUDY_ARTICLE_ID.getValue()).descending()));
        }

        if (!status) {
            studyArticleList = studyArticleRepository
                    .findByEnabled(true, PageRequest.of(page - 1, 10, Sort.by(StudyArticleEnum.STUDY_ARTICLE_ID.getValue()).descending()));
        }
        return new MultiResponseDto<>(studyMapper.studyArticleListToStudyArticleResponseDtoList(studyArticleList.getContent()), studyArticleList);
    }

    @Override
    public MultiResponseDto<StudyArticleResponseDto> readStudyArticlesWithPosition(int page, String positionName, Boolean status) {
        Page<StudyArticle> studyArticleList = null;
        if (status) {
            studyArticleList = studyArticleRepository
                    .findByStudyPositionAndEnabledAndStudyInfo(
                            new StudyPosition(positionName),
                            true,
                            new StudyInfo(StudyStatus.PROCEED),
                            PageRequest.of(page - 1, 10, Sort.by(StudyArticleEnum.STUDY_ARTICLE_ID.getValue()).descending()));
        }
        if (!status) {
            studyArticleList = studyArticleRepository
                    .findByStudyPositionAndEnabled(
                            new StudyPosition(positionName),
                            true,
                            PageRequest.of(page - 1, 10, Sort.by(StudyArticleEnum.STUDY_ARTICLE_ID.getValue()).descending()));
        }
        return new MultiResponseDto<>(studyMapper.studyArticleListToStudyArticleResponseDtoList(studyArticleList.getContent()), studyArticleList);
    }

    @Override
    public MultiResponseDto<StudyArticleResponseDto> readStudyArticlesWithTag(int page, String tag, Boolean status) {
        Page<StudyArticle> studyArticleListWithTag = null;
        if (status) {
            studyArticleListWithTag = studyArticleRepository
                    .findByStudyTagsContainsAndEnabledAndStudyInfo(tag, true,
                            new StudyInfo(StudyStatus.PROCEED),
                            PageRequest.of(page - 1, 10, Sort.by(StudyArticleEnum.STUDY_ARTICLE_ID.getValue()).descending()));
        }
        if (!status) {
            studyArticleListWithTag = studyArticleRepository
                    .findByStudyTagsContainsAndEnabled(tag, true,
                            PageRequest.of(page - 1, 10, Sort.by(StudyArticleEnum.STUDY_ARTICLE_ID.getValue()).descending()));
        }
        return new MultiResponseDto<>(studyMapper.studyArticleListToStudyArticleResponseDtoList(studyArticleListWithTag.getContent()), studyArticleListWithTag);
    }

    @Override
    public StudyArticleDetailResponseDto readStudyArticle(Long studyId) {
        StudyArticle studyArticle = studyArticleRepository.findById(studyId).orElseThrow(() -> new NullPointerException());
        return studyMapper.studyArticleToStudyDetailResponseDto(studyArticle);
    }

    @Override
    public void createStudyArticle(StudyArticleRequestDto studyArticleRequestDto) {
        //todo: 코드 단축시키기 init을 mapper default로
        StudyArticle studyArticle = studyMapper.studyRequestDtoToStudyArticle(studyArticleRequestDto);
        studyArticle.initStudyArticle();
        studyArticleRepository.save(studyArticle);
    }

    @Override
    public void updateStudyArticle(Long studyId, StudyArticleUpdateRequestDto studyArticleUpdateRequestDto) {
        StudyArticle studyArticle = studyArticleRepository.findById(studyId).orElseThrow();
        String target = studyArticleUpdateRequestDto.getTarget();

        if (updateTitle(studyArticleUpdateRequestDto, studyArticle, target)) return;
        if (updateContent(studyArticleUpdateRequestDto, studyArticle, target)) return;
        if (updateTotalMember(studyArticleUpdateRequestDto, studyArticle, target)) return;
        if (updateCountMember(studyArticleUpdateRequestDto, studyArticle, target)) return;
        if (updateDeadLine(studyArticleUpdateRequestDto, studyArticle, target)) return;
        if (updateActive(studyArticleUpdateRequestDto, studyArticle, target)) return;
        if (updateStudyTags(studyArticleUpdateRequestDto, studyArticle, target)) return;

    }

    @Override
    public void deleteStudyArticle(Long studyId) {
        StudyArticle studyArticle = studyArticleRepository.findById(studyId).orElseThrow(NullPointerException::new);
        studyArticle.changeEnabled(false);
        studyArticleRepository.save(studyArticle);
    }

    private boolean updateTitle(StudyArticleUpdateRequestDto studyArticleUpdateRequestDto, StudyArticle studyArticle, String target) {
        if (target.equals(StudyArticleEnum.TITLE.getValue())) {
            studyArticle.changeTitle(studyArticleUpdateRequestDto.getTitle());
            studyArticleRepository.save(studyArticle);
            return true;
        }
        return false;
    }

    private boolean updateContent(StudyArticleUpdateRequestDto studyArticleUpdateRequestDto, StudyArticle studyArticle, String target) {
        if (target.equals(StudyArticleEnum.CONTENT.getValue())) {
            studyArticle.updateStudyContent(studyArticleUpdateRequestDto.getContent());
            studyArticleRepository.save(studyArticle);
            return true;
        }
        return false;
    }

    private boolean updateStudyTags(StudyArticleUpdateRequestDto studyArticleUpdateRequestDto, StudyArticle studyArticle, String target) {
        if (target.equals(StudyArticleEnum.STUDY_TAGS.getValue())) {
            studyArticle.setStudyTags(studyArticleUpdateRequestDto.getStudyTags());
            studyArticleRepository.save(studyArticle);
            return true;
        }
        return false;
    }

    private boolean updateTotalMember(StudyArticleUpdateRequestDto studyArticleUpdateRequestDto, StudyArticle studyArticle, String target) {
        if (target.equals(StudyArticleEnum.STUDY_TOTAL_MEMBER.getValue())) {
            studyArticle.updateStudyTotalMember(studyArticleUpdateRequestDto.getTotalMember());
            studyArticleRepository.save(studyArticle);
            return true;
        }
        return false;
    }

    private boolean updateCountMember(StudyArticleUpdateRequestDto studyArticleUpdateRequestDto, StudyArticle studyArticle, String target) {
        if (target.equals(StudyArticleEnum.STUDY_COUNT_MEMBER.getValue())) {
            studyArticle.updateStudyCountMember(studyArticleUpdateRequestDto.getCountMember());
            studyArticleRepository.save(studyArticle);
            return true;
        }
        return false;
    }

    private boolean updateDeadLine(StudyArticleUpdateRequestDto studyArticleUpdateRequestDto, StudyArticle studyArticle, String target) {
        if (target.equals(StudyArticleEnum.DEAD_LINE.getValue())) {
            studyArticle.changeDeadLine(studyArticleUpdateRequestDto.getDeadLine());
            studyArticleRepository.save(studyArticle);
            return true;
        }
        return false;
    }

    private boolean updateActive(StudyArticleUpdateRequestDto studyArticleUpdateRequestDto, StudyArticle studyArticle, String target) {
        if (target.equals(StudyArticleEnum.ACTIVE.getValue())) {
            studyArticle.changeStatus(studyArticleUpdateRequestDto.getActive());
            studyArticleRepository.save(studyArticle);
            return true;
        }
        return false;
    }

}
