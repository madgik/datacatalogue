package ebrainv2.mip.datacatalogue.unittests;

import ebrainsv2.mip.datacatalogue.datamodel.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DataModelConverterTest {

    private DataModelDAO dataModelDAO;
    private DataModelDTO dataModelDTO;

    @BeforeEach
    void setUp() {
        UUID uuid = UUID.randomUUID();
        String variablesJson = "[{\"code\":\"var1\",\"label\":\"Variable 1\",\"description\":\"Desc\",\"sql_type\":\"INTEGER\",\"isCategorical\":false,\"enumerations\":[],\"minValue\":1,\"maxValue\":10,\"type\":\"Type\",\"methodology\":\"Methodology\",\"units\":\"Units\"}]";
        String groupsJson = "[{\"code\":\"grp1\",\"label\":\"Group 1\",\"variables\":[],\"groups\":[]}]";
        dataModelDAO = new DataModelDAO(uuid, "testCode", "1.0", "Test Label", true, variablesJson, groupsJson, true);

        List<CommonDataElementDTO> variables = List.of(new CommonDataElementDTO("var1", "Variable 1", "Desc", "INTEGER", false, Arrays.asList(), 1, 10, "Type", "Methodology", "Units"));
        List<DataModelMetadataGroupDTO> groups = List.of(new DataModelMetadataGroupDTO("grp1", "Group 1", Arrays.asList(), Arrays.asList()));
        dataModelDTO = new DataModelDTO(uuid, "testCode", "1.0", "Test Label", true, variables, groups, true);
    }

    @Test
    void convertToDataModelDTOTest() {
        DataModelDTO convertedDTO = DataModelConverter.convertToDataModelDTO(dataModelDAO);

        assertEquals(dataModelDTO.uuid(), convertedDTO.uuid());
        assertEquals(dataModelDTO.code(), convertedDTO.code());
        assertEquals(dataModelDTO.version(), convertedDTO.version());
        assertEquals(dataModelDTO.label(), convertedDTO.label());
        assertEquals(dataModelDTO.longitudinal(), convertedDTO.longitudinal());
        assertEquals(dataModelDTO.variables().size(), convertedDTO.variables().size());
        assertEquals(dataModelDTO.groups().size(), convertedDTO.groups().size());
        assertEquals(dataModelDTO.released(), convertedDTO.released());
    }

    @Test
    void convertToDataModelDAOTest() {
        DataModelDAO convertedDAO = DataModelConverter.convertToDataModelDAO(dataModelDTO);

        assertEquals(dataModelDAO.getUuid(), convertedDAO.getUuid());
        assertEquals(dataModelDAO.getCode(), convertedDAO.getCode());
        assertEquals(dataModelDAO.getVersion(), convertedDAO.getVersion());
        assertEquals(dataModelDAO.getLabel(), convertedDAO.getLabel());
        assertEquals(dataModelDAO.getLongitudinal(), convertedDAO.getLongitudinal());
        assertNotNull(convertedDAO.getVariables());
        assertNotNull(convertedDAO.getGroups());
        assertEquals(dataModelDAO.getReleased(), convertedDAO.getReleased());
    }

    @Test
    void convertToDTOWithEmptyListsTest() {
        dataModelDAO.setVariables("[]");
        dataModelDAO.setGroups("[]");

        DataModelDTO convertedDTO = DataModelConverter.convertToDataModelDTO(dataModelDAO);

        assertTrue(convertedDTO.variables().isEmpty());
        assertTrue(convertedDTO.groups().isEmpty());
    }

    @Test
    void convertToDAOWithEmptyListsTest() {
        dataModelDTO = new DataModelDTO(dataModelDTO.uuid(), dataModelDTO.code(), dataModelDTO.version(), dataModelDTO.label(), dataModelDTO.longitudinal(), List.of(), List.of(), dataModelDTO.released());

        DataModelDAO convertedDAO = DataModelConverter.convertToDataModelDAO(dataModelDTO);

        assertEquals("[]", convertedDAO.getVariables());
        assertEquals("[]", convertedDAO.getGroups());
    }

    @Test
    void convertWithNullValuesTest() {
        dataModelDAO.setLongitudinal(null);
        dataModelDAO.setReleased(null);

        DataModelDTO convertedDTO = DataModelConverter.convertToDataModelDTO(dataModelDAO);

        assertNull(convertedDTO.longitudinal());
        assertNull(convertedDTO.released());
    }

    @Test
    void deserializationErrorHandlingTest() {
        dataModelDAO.setVariables("malformed");

        assertThrows(RuntimeException.class, () -> DataModelConverter.convertToDataModelDTO(dataModelDAO));
    }

    @Test
    void convertToDTOWithNonEmptyListsTest() {
        CommonDataElementDTO variable = new CommonDataElementDTO("varCode", "Variable Label", "A description", "TEXT", true, List.of(new CommonDataElementDTO.EnumerationDTO("enumCode", "Enum Label")), 1, 100, "Type", "Methodology", "Units");
        DataModelMetadataGroupDTO group = new DataModelMetadataGroupDTO("grpCode", "Group Label", List.of(variable), List.of());

        dataModelDTO = new DataModelDTO(dataModelDTO.uuid(), dataModelDTO.code(), dataModelDTO.version(), dataModelDTO.label(), dataModelDTO.longitudinal(), List.of(variable), List.of(group), dataModelDTO.released());

        DataModelDAO convertedDAO = DataModelConverter.convertToDataModelDAO(dataModelDTO);

        assertFalse(convertedDAO.getVariables().isEmpty());
        assertFalse(convertedDAO.getGroups().isEmpty());
    }

    @Test
    void convertToDTOWithNestedGroupsTest() {
        CommonDataElementDTO variable = new CommonDataElementDTO("varCode", "Variable Label", "A description", "TEXT", true, List.of(new CommonDataElementDTO.EnumerationDTO("enumCode", "Enum Label")), 1, 100, "Type", "Methodology", "Units");

        DataModelMetadataGroupDTO innerGroup = new DataModelMetadataGroupDTO("innerGrpCode", "Inner Group Label", List.of(variable), List.of());

        DataModelMetadataGroupDTO outerGroup = new DataModelMetadataGroupDTO("outerGrpCode", "Outer Group Label", List.of(variable), List.of(innerGroup));

        dataModelDTO = new DataModelDTO(dataModelDTO.uuid(), dataModelDTO.code(), dataModelDTO.version(), dataModelDTO.label(), dataModelDTO.longitudinal(), List.of(variable), List.of(outerGroup), dataModelDTO.released());

        DataModelDAO convertedDAO = DataModelConverter.convertToDataModelDAO(dataModelDTO);

        assertFalse(convertedDAO.getGroups().isEmpty());
    }

    @Test
    void convertToDAOWithNestedGroupsTest() {
        String nestedGroupsJson = "[{\"code\":\"outerGrpCode\",\"label\":\"Outer Group Label\",\"variables\":[],\"groups\":[{\"code\":\"innerGrpCode\",\"label\":\"Inner Group Label\",\"variables\":[{\"code\":\"varCode\",\"label\":\"Variable Label\",\"description\":\"A description\",\"sql_type\":\"TEXT\",\"isCategorical\":true,\"enumerations\":[{\"code\":\"enumCode\",\"label\":\"Enum Label\"}],\"minValue\":1,\"maxValue\":100,\"type\":\"Type\",\"methodology\":\"Methodology\",\"units\":\"Units\"}],\"groups\":[]}]}]";
        dataModelDAO.setGroups(nestedGroupsJson);

        DataModelDTO convertedDTO = DataModelConverter.convertToDataModelDTO(dataModelDAO);

        assertFalse(convertedDTO.groups().isEmpty());
        assertEquals(1, convertedDTO.groups().size());
        assertEquals(1, convertedDTO.groups().get(0).groups().size());
    }

}
