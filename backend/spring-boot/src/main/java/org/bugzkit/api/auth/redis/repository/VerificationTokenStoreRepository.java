package org.bugzkit.api.auth.redis.repository;

import org.bugzkit.api.auth.redis.model.VerificationTokenStore;
import org.springframework.data.repository.CrudRepository;

public interface VerificationTokenStoreRepository
    extends CrudRepository<VerificationTokenStore, String> {}
