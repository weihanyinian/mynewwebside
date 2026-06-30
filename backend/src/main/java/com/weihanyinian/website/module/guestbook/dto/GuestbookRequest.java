package com.weihanyinian.website.module.guestbook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class GuestbookRequest {

    @NotBlank(message = "昵称不能为空")
    @Size(max = 50, message = "昵称不超过50字")
    private String nickname;

    @Size(max = 100, message = "邮箱不超过100字")
    private String email;

    @NotBlank(message = "内容不能为空")
    @Size(max = 2000, message = "内容不超过2000字")
    private String content;
}
