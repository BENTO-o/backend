package bento.backend.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserUpdateRequest {
    @Size(max = 20, message = "Username should be less than 20 characters")
    private String username;
}
