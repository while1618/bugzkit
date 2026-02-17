package org.bugzkit.api.auth.redis.repository;

import org.bugzkit.api.auth.redis.model.ResetPasswordTokenStore;
import org.springframework.data.repository.CrudRepository;

public interface ResetPasswordTokenStoreRepository
    extends CrudRepository<ResetPasswordTokenStore, String> {}
