package ebrainsv2.mip.datacatalogue.datamodelinfo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.Optional;

@RestResource(exported = false)
public interface DataModelInfoRepository extends CrudRepository<DataModelInfoDAO, String> {
    Optional<DataModelInfoDAO> findByCode(String code);

    void deleteByCode(String code);
    boolean existsByCode(String code);
}
