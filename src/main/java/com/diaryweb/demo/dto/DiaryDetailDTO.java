package com.diaryweb.demo.dto;

public class DiaryDetailDTO {

    private DiaryDTO diary;
    private boolean liked;
    private long commentCount;
    private boolean editable;

    public DiaryDetailDTO() {}

    public DiaryDetailDTO(DiaryDTO diary, boolean liked, long commentCount, boolean editable) {
        this.diary = diary;
        this.liked = liked;
        this.commentCount = commentCount;
        this.editable = editable;
    }

    public DiaryDTO getDiary() {
        return diary;
    }

    public void setDiary(DiaryDTO diary) {
        this.diary = diary;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    public long getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(long commentCount) {
        this.commentCount = commentCount;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }
}
