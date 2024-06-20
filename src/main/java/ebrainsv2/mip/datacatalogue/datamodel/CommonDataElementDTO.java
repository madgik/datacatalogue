package ebrainsv2.mip.datacatalogue.datamodel;


import java.util.List;

public record CommonDataElementDTO(
        String code,
        String label,
        String description,
        String sql_type,
        Boolean isCategorical,
        List<EnumerationDTO> enumerations,
        Integer minValue,
        Integer maxValue,
        String type,
        String methodology,
        String units
) {
    
    public record EnumerationDTO(String code, String label) {
    }

}

