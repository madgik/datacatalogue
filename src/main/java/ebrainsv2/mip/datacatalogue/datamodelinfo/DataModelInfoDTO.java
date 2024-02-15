package ebrainsv2.mip.datacatalogue.datamodelinfo;

public record DataModelInfoDTO(
        String code,
        String description,
        String datasetLocations,
        int records

) {
}