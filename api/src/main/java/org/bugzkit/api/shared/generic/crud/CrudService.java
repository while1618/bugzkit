package org.bugzkit.api.shared.generic.crud;

import org.bugzkit.api.shared.payload.dto.PageableDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public interface CrudService<T, U> {
  T create(U saveRequest);

  PageableDTO<T> findAll(Pageable pageable);

  T findById(Long id);

  T update(Long id, U saveRequest);

  void delete(Long id);
}
