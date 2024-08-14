package study.datajpa.repository;

import lombok.Getter;

@Getter
public class UsernameOnlyDto {

    private String username;

    public UsernameOnlyDto(String username) {
        this.username = username;
    }

}
