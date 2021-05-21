package org.bootstrapbugz.api.user.mapper;

import org.bootstrapbugz.api.user.dto.SimpleUserDto;
import org.bootstrapbugz.api.user.dto.UserDto;
import org.bootstrapbugz.api.user.model.User;
import java.util.List;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;

@Mapper(injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface UserMapper {
  SimpleUserDto userToSimpleUserDto(User user);

  List<SimpleUserDto> usersToSimpleUserDtos(List<User> users);

  List<UserDto> usersToUserDtos(List<User> users);
}