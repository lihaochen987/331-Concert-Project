package proj.concert.service.mapper;

import proj.concert.common.dto.PerformerDTO;
import proj.concert.common.dto.UserDTO;
import proj.concert.service.domain.Performer;
import proj.concert.service.domain.User;

public class UserMapper {
    public static User toDomainModel(UserDTO dtoUser) {
        return new User(dtoUser.getUsername(), dtoUser.getPassword());
    }
    }
