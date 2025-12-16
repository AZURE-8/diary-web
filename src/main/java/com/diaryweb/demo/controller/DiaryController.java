package com.diaryweb.demo.controller;

import com.diaryweb.demo.common.ApiResponse;
import com.diaryweb.demo.dto.DiaryDTO;
import com.diaryweb.demo.entity.Diary;
import com.diaryweb.demo.entity.Visibility;
import com.diaryweb.demo.repository.DiaryLikeRepository;
import com.diaryweb.demo.service.DiaryService;
import com.diaryweb.demo.service.StorageService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.diaryweb.demo.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import com.diaryweb.demo.repository.CommentRepository;
import com.diaryweb.demo.dto.DiaryDetailDTO;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/diaries")
public class DiaryController {

    private final DiaryService diaryService;
    private final StorageService storageService;
    private final DiaryLikeRepository diaryLikeRepository;
    private final CommentRepository commentRepository;
    
    public DiaryController(DiaryService diaryService,
                           StorageService storageService,
                           DiaryLikeRepository diaryLikeRepository,
                           CommentRepository commentRepository) {
        this.diaryService = diaryService;
        this.storageService = storageService;
        this.diaryLikeRepository = diaryLikeRepository;
        this.commentRepository = commentRepository;
    }

    public static class CreateDiaryRequest {
        public String title;
        public String content;
        public Visibility visibility;
        public Set<String> tags;
    }

    public static class UpdateDiaryRequest {
        public String title;
        public String content;
        public Visibility visibility;
        public Set<String> tags;
    }

    @PostMapping("/create")
    public ApiResponse<DiaryDTO> create(@RequestBody CreateDiaryRequest req) {
        Diary saved = diaryService.createDiary(req.title, req.content, null, req.visibility, req.tags);
        long likeCount = diaryLikeRepository.countByDiaryId(saved.getId());
        return ApiResponse.ok(DiaryDTO.from(saved, likeCount));
    }

    @PostMapping("/createWithImage")
    public ApiResponse<DiaryDTO> createWithImage(@RequestParam String title,
                                                 @RequestParam(required = false) String content,
                                                 @RequestParam(defaultValue = "PRIVATE") Visibility visibility,
                                                 @RequestParam(required = false) String tags,
                                                 @RequestPart(required = false) MultipartFile image) {
        String imageUrl = storageService.saveImage(image);

        Set<String> tagSet = null;
        if (tags != null && !tags.isBlank()) {
            tagSet = Set.of(tags.split(","));
        }

        Diary saved = diaryService.createDiary(title, content, imageUrl, visibility, tagSet);
        long likeCount = diaryLikeRepository.countByDiaryId(saved.getId());
        return ApiResponse.ok(DiaryDTO.from(saved, likeCount));
    }

    @GetMapping("/mine")
    public ApiResponse<List<DiaryDTO>> mine() {
        List<Diary> diaries = diaryService.listMyDiaries();
        List<DiaryDTO> dtoList = diaries.stream()
                .map(d -> DiaryDTO.from(d, diaryLikeRepository.countByDiaryId(d.getId())))
                .toList();
        return ApiResponse.ok(dtoList);
    }

    @GetMapping("/{userId}")
    public ApiResponse<List<DiaryDTO>> listUserPublic(@PathVariable Long userId) {
        List<Diary> diaries = diaryService.listUserPublicDiaries(userId);
        List<DiaryDTO> dtoList = diaries.stream()
                .map(d -> DiaryDTO.from(d, diaryLikeRepository.countByDiaryId(d.getId())))
                .toList();
        return ApiResponse.ok(dtoList);
    }

    @PutMapping("/{diaryId}")
    public ApiResponse<DiaryDTO> update(@PathVariable Long diaryId, @RequestBody UpdateDiaryRequest req) {
        Diary updated = diaryService.updateDiary(diaryId, req.title, req.content, req.visibility, req.tags);
        long likeCount = diaryLikeRepository.countByDiaryId(updated.getId());
        return ApiResponse.ok(DiaryDTO.from(updated, likeCount));
    }

    @DeleteMapping("/{diaryId}")
    public ApiResponse<String> delete(@PathVariable Long diaryId) {
        diaryService.deleteDiary(diaryId);
        return ApiResponse.ok("deleted");
    }

    @GetMapping("/public/searchByTag")
    public ApiResponse<List<DiaryDTO>> searchPublicByTag(@RequestParam String tag) {
        List<Diary> diaries = diaryService.searchPublicByTag(tag);
        List<DiaryDTO> dtoList = diaries.stream()
                .map(d -> DiaryDTO.from(d, diaryLikeRepository.countByDiaryId(d.getId())))
                .toList();
        return ApiResponse.ok(dtoList);
    }
    
    @GetMapping("/mine/page")
    public ApiResponse<PageResponse<DiaryDTO>> minePage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        Sort s = parseSort(sort);
        Page<Diary> p = diaryService.pageMyDiaries(PageRequest.of(page, size, s));

        PageResponse<DiaryDTO> dtoPage = PageResponse.of(
                p.map(d -> DiaryDTO.from(d, diaryLikeRepository.countByDiaryId(d.getId())))
        );

        return ApiResponse.ok(dtoPage);
    }
    
    @GetMapping("/{userId}/page")
    public ApiResponse<PageResponse<DiaryDTO>> userPublicPage(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        Sort s = parseSort(sort);
        Page<Diary> p = diaryService.pageUserPublicDiaries(userId, PageRequest.of(page, size, s));

        PageResponse<DiaryDTO> dtoPage = PageResponse.of(
                p.map(d -> DiaryDTO.from(d, diaryLikeRepository.countByDiaryId(d.getId())))
        );

        return ApiResponse.ok(dtoPage);
    }

    @GetMapping("/public/searchByTag/page")
    public ApiResponse<PageResponse<DiaryDTO>> publicByTagPage(
            @RequestParam String tag,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        Sort s = parseSort(sort);
        Page<Diary> p = diaryService.pagePublicByTag(tag, PageRequest.of(page, size, s));

        PageResponse<DiaryDTO> dtoPage = PageResponse.of(
                p.map(d -> DiaryDTO.from(d, diaryLikeRepository.countByDiaryId(d.getId())))
        );

        return ApiResponse.ok(dtoPage);
    }

    @GetMapping("/public/search/page")
    public ApiResponse<PageResponse<DiaryDTO>> publicSearchPage(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        Sort s = parseSort(sort);
        Page<Diary> p = diaryService.pagePublicByKeyword(keyword, PageRequest.of(page, size, s));

        PageResponse<DiaryDTO> dtoPage = PageResponse.of(
                p.map(d -> DiaryDTO.from(d, diaryLikeRepository.countByDiaryId(d.getId())))
        );

        return ApiResponse.ok(dtoPage);
    }
    private Sort parseSort(String sort) {
        // sort 格式：createdAt,desc 或 title,asc
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        String[] parts = sort.split(",");
        String field = parts[0].trim();
        String dir = parts.length > 1 ? parts[1].trim() : "desc";

        Sort.Direction direction = "asc".equalsIgnoreCase(dir)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return Sort.by(direction, field);
    }
    

    @GetMapping("/{diaryId}/detail")
    public ApiResponse<DiaryDetailDTO> detail(@PathVariable Long diaryId) {

    // 1) 取出 diary（Service 内部完成可见性权限控制）
    Diary diary = diaryService.getDiaryDetail(diaryId);

    // 2) 统计点赞数（DTO from 需要）
    long likeCount = diaryLikeRepository.countByDiaryId(diary.getId());
    DiaryDTO diaryDTO = DiaryDTO.from(diary, likeCount);

    // 3) 当前用户
    String username = SecurityContextHolder.getContext().getAuthentication().getName();
    boolean editable = diary.getUser() != null && diary.getUser().getUsername().equals(username);

    // 4) 是否点赞过（liked）
    Long meId = diaryService.currentUserId(); // 如果你没有这个方法，可以在 service 增加一个，见下方说明
    boolean liked = diaryLikeRepository.existsByDiaryIdAndUserId(diary.getId(), meId);

    // 5) 评论数
    long commentCount = commentRepository.countByDiaryId(diary.getId());

    DiaryDetailDTO detail = new DiaryDetailDTO(diaryDTO, liked, commentCount, editable);
    return ApiResponse.ok(detail);
    }

}
