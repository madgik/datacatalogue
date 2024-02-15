package ebrainsv2.mip.datacatalogue.datamodelinfo;

public class DataModelInfoConverter {

    public static DataModelInfoDTO daoToDto(DataModelInfoDAO dataModelInfoDAO) {
        return new DataModelInfoDTO(
                dataModelInfoDAO.getCode(),
                dataModelInfoDAO.getDescription(),
                dataModelInfoDAO.getDatasetLocations(), // Assuming this is a Map or similar structure
                dataModelInfoDAO.getRecords()
        );
    }

    public static DataModelInfoDAO dtoToDao(DataModelInfoDTO dataModelInfoDTO) {
        return new DataModelInfoDAO(
                dataModelInfoDTO.code(),
                dataModelInfoDTO.description(),
                dataModelInfoDTO.datasetLocations(), // Assuming this is a Map or similar structure
                dataModelInfoDTO.records()
        );
    }
}
