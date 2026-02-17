package org.bugzkit.api.auth.redis.repository;

import org.bugzkit.api.auth.redis.model.AccessTokenBlacklist;
import org.springframework.data.repository.CrudRepository;

public interface AccessTokenBlacklistRepository
    extends CrudRepository<AccessTokenBlacklist, String> {}
