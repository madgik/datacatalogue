package ebrainsv2.mip.datacatalogue.datamodelinfo;

import ebrainsv2.mip.datacatalogue.utils.Exceptions.DataModelNotFoundException;
import ebrainsv2.mip.datacatalogue.utils.UserActionLogger;
import org.springframework.stereotype.Service;

@Service
public class DataModelInfoService {
    private final DataModelInfoRepository dataModelInfoRepository;

    public DataModelInfoService(DataModelInfoRepository dataModelInfoRepository) {
        this.dataModelInfoRepository = dataModelInfoRepository;
    }

    public DataModelInfoDTO createDataModelInfo(DataModelInfoDTO dataModelInfoDTO, UserActionLogger logger) {
        logger.info("Starting to create DataModelInfo with code: " + dataModelInfoDTO.code());
        DataModelInfoDAO dataModelInfoDAO = DataModelInfoConverter.dtoToDao(dataModelInfoDTO);
        DataModelInfoDAO savedDataModelInfoDAO = dataModelInfoRepository.save(dataModelInfoDAO);
        DataModelInfoDTO createdDataModelInfoDTO = DataModelInfoConverter.daoToDto(savedDataModelInfoDAO);
        logger.info("DataModelInfo with code: " + dataModelInfoDTO.code() + " created successfully.");
        return createdDataModelInfoDTO;
    }

    public DataModelInfoDTO getDataModelInfoByCode(String code, UserActionLogger logger) {
        logger.info("Retrieving DataModelInfo with code: " + code);
        DataModelInfoDAO dataModelInfoDAO = dataModelInfoRepository.findByCode(code)
                .orElseThrow(() -> {
                    logger.error("DataModelInfo not found with code: " + code);
                    return new DataModelNotFoundException("DataModelInfo not found with code: " + code);
                });
        DataModelInfoDTO dataModelInfoDTO = DataModelInfoConverter.daoToDto(dataModelInfoDAO);
        logger.info("DataModelInfo with code: " + code + " retrieved successfully.");
        return dataModelInfoDTO;
    }

    public DataModelInfoDTO updateDataModelInfo(String code, DataModelInfoDTO dataModelInfoDTO, UserActionLogger logger) {
        logger.info("Updating DataModelInfo with code: " + code);
        DataModelInfoDAO existingDataModelInfoDAO = dataModelInfoRepository.findByCode(code)
                .orElseThrow(() -> {
                    logger.error("DataModelInfo not found with code: " + code);
                    return new DataModelNotFoundException("DataModelInfo not found with code: " + code);
                });

        existingDataModelInfoDAO.setDescription(dataModelInfoDTO.description());
        existingDataModelInfoDAO.setDatasetLocations(dataModelInfoDTO.datasetLocations());
        existingDataModelInfoDAO.setRecords(dataModelInfoDTO.records());

        DataModelInfoDAO savedDataModelInfoDAO = dataModelInfoRepository.save(existingDataModelInfoDAO);
        DataModelInfoDTO updatedDataModelInfoDTO = DataModelInfoConverter.daoToDto(savedDataModelInfoDAO);
        logger.info("DataModelInfo with code: " + code + " updated successfully.");
        return updatedDataModelInfoDTO;
    }

    public void deleteDataModelInfo(String code, UserActionLogger logger) {
        logger.info("Deleting DataModelInfo with code: " + code);
        if (!dataModelInfoRepository.existsByCode(code)) {
            logger.error("DataModelInfo with code: " + code + " does not exist and cannot be deleted.");
            throw new DataModelNotFoundException("DataModelInfo with code: " + code + " not found.");
        }
        dataModelInfoRepository.deleteByCode(code);
        logger.info("DataModelInfo with code: " + code + " deleted successfully.");
    }
}
