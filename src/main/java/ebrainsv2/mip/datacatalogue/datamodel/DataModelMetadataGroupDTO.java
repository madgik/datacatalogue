package ebrainsv2.mip.datacatalogue.datamodel;


import java.util.List;

public record DataModelMetadataGroupDTO(
        String code,
        String label,
        List<CommonDataElementDTO> variables,
        List<DataModelMetadataGroupDTO> groups
) {
}
