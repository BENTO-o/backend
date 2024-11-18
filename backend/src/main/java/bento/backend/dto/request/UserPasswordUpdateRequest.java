package bento.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserPasswordUpdateRequest {
    @NotBlank(message = "Password is mandatory")
    @Size(min = 8, max = 20, message = "Password should be between 8 and 20 characters")
    private String currentPassword;

    @NotBlank(message = "Password is mandatory")
    @Size(min = 8, max = 20, message = "Password should be between 8 and 20 characters")
    private String newPassword;
}
