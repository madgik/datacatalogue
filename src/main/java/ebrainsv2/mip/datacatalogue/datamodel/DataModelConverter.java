package ebrainsv2.mip.datacatalogue.datamodel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class DataModelConverter {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static DataModelDTO convertToDataModelDTO(DataModelDAO dataModelDAO) {
        try {
            List<CommonDataElementDTO> variables = objectMapper.readValue(
                    dataModelDAO.getVariables(), new TypeReference<>() {
                    }
            );
            List<DataModelMetadataGroupDTO> groups = objectMapper.readValue(
                    dataModelDAO.getGroups(), new TypeReference<>() {
                    }
            );

            return new DataModelDTO(
                    dataModelDAO.getUuid(),
                    dataModelDAO.getCode(),
                    dataModelDAO.getVersion(),
                    dataModelDAO.getLabel(),
                    dataModelDAO.getLongitudinal(),
                    variables,
                    groups,
                    dataModelDAO.getReleased()
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error in deserializing data from DataModelDAO: " + e.getMessage(), e);
        }
    }

    public static DataModelDAO convertToDataModelDAO(DataModelDTO dataModelDTO) {
        DataModelDAO dataModelDAO = new DataModelDAO();
        try {
            String variablesJson = objectMapper.writeValueAsString(dataModelDTO.variables());
            String groupsJson = objectMapper.writeValueAsString(dataModelDTO.groups());

            dataModelDAO.setUuid(dataModelDTO.uuid());
            dataModelDAO.setCode(dataModelDTO.code());
            dataModelDAO.setVersion(dataModelDTO.version());
            dataModelDAO.setLabel(dataModelDTO.label());
            dataModelDAO.setLongitudinal(Boolean.parseBoolean(String.valueOf(dataModelDTO.longitudinal())));
            dataModelDAO.setVariables(variablesJson);
            dataModelDAO.setGroups(groupsJson);
            dataModelDAO.setReleased(dataModelDTO.released() != null && dataModelDTO.released());

            return dataModelDAO;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error in serializing data to DataModelDAO: " + e.getMessage(), e);
        }
    }

}
