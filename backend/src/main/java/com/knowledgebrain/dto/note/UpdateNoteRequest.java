package com.knowledgebrain.dto.note;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateNoteRequest {

    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    private String content;
}
