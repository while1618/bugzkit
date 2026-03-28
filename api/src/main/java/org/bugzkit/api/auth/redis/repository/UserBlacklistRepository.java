package org.bugzkit.api.auth.redis.repository;

import org.bugzkit.api.auth.redis.model.UserBlacklist;
import org.springframework.data.repository.CrudRepository;

public interface UserBlacklistRepository extends CrudRepository<UserBlacklist, Long> {}
