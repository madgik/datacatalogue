package ebrainv2.mip.datacatalogue.unittests;

import com.fasterxml.jackson.databind.ObjectMapper;
import ebrainsv2.mip.datacatalogue.FileConversionService;
import ebrainsv2.mip.datacatalogue.datamodel.DataModelDAO;
import ebrainsv2.mip.datacatalogue.datamodel.DataModelDTO;
import ebrainsv2.mip.datacatalogue.datamodel.DataModelRepository;
import ebrainsv2.mip.datacatalogue.datamodel.DataModelService;
import ebrainsv2.mip.datacatalogue.utils.UserActionLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static ebrainsv2.mip.datacatalogue.datamodel.DataModelConverter.convertToDataModelDAO;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mock.web.MockMultipartFile;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class DataModelServiceTest {

    @Mock
    private DataModelRepository dataModelRepository;

    @Mock
    private FileConversionService fileConversionService;

    @Mock
    private UserActionLogger logger;

    @InjectMocks
    private DataModelService dataModelService;

    @BeforeEach
    void setUp() {
        // Setup mock behavior here
    }

    @Test
    void testImportDataModel() throws Exception {
        // Setup
        MockMultipartFile multipartFile = new MockMultipartFile("file", "filename.xlsx", "text/plain", "Some dataset".getBytes());
        File convFile = new File("somePath");
        String jsonResponse = """
                {"uuid": "4111af95-c5c2-4821-b77e-2f271419c609", "code":"Minimal Example","groups":[{"code":"Example Group","groups":[{"code":"Nested Group","label":"Nested Group","variables":[{"code":"nested_group_variable","description":"A nested group variable","enumerations":[{"code":"nested_enum1","label":"Nested Enumeration 1"}],"isCategorical":true,"label":"Nested Group Variable","methodology":"nested methodology","sql_type":"text","type":"nominal"}]}],"label":"Example Group","variables":[{"code":"group_variable","description":"A variable within a group","isCategorical":false,"label":"Group Variable","maxValue":100,"methodology":"group methodology","minValue":0,"sql_type":"int","type":"integer","units":"years"}]}],"label":"Minimal Example","variables":[{"code":"dataset","description":"An example variable description","enumerations":[{"code":"enum1","label":"Enumeration 1"}],"isCategorical":true,"label":"Dataset Variable","methodology":"example methodology","sql_type":"text","type":"nominal","units":"unit"}],"version":"to be defined"}
                """;

        ObjectMapper objectMapper = new ObjectMapper();
        DataModelDTO dataModelDTO = objectMapper.readValue(jsonResponse, DataModelDTO.class);
        when(dataModelRepository.save(any(DataModelDAO.class))).thenReturn(convertToDataModelDAO(dataModelDTO));
        when(fileConversionService.convertMultipartFileToFile(any(MultipartFile.class))).thenReturn(convFile);
        when(fileConversionService.convertExcelToJson(any(File.class))).thenReturn(jsonResponse);

        // Execute
        DataModelDTO result = dataModelService.importDataModel(multipartFile, logger);

        // Verify
        assertNotNull(result);
        verify(fileConversionService, times(1)).convertMultipartFileToFile(any(MultipartFile.class));
        verify(fileConversionService, times(1)).convertExcelToJson(any(File.class));
        // Add more verifications based on what you assert
    }

    @Test
    void testExportDataModelWithValidAuthentication() throws Exception {
        // Assuming there's a method to setup Authentication mock
        Authentication authentication = mock(Authentication.class);

        // Assume setupDataModel() creates a mock DataModelDTO with UUID and other necessary details
        DataModelDTO dataModel = new DataModelDTO();
        when(dataModelRepository.findByUuid(any(UUID.class))).thenReturn(convertToDataModelDAO(dataModel));

        // Execution
        ByteArrayResource result = dataModelService.exportDataModel(authentication, dataModel.uuid().toString(), logger);

        // Verification
        assertNotNull(result);
        verify(dataModelRepository, times(1)).findByUuid(any(UUID.class));
        // More verifications based on method logic
    }

//    @Test
//    void testCreateDataModel() {
//        DataModelDTO dataModelDTO = new DataModelDTO(UUID.randomUUID(), "code", "version", "label", false, List.of(), List.of(), true);
//
//        when(dataModelRepository.save(any(DataModelDAO.class))).thenAnswer(i -> i.getArguments()[0]);
//
//        // Execute
//        DataModelDTO created = dataModelService.createDataModel(dataModelDTO, logger);
//
//        // Verify
//        assertNotNull(created);
//        verify(dataModelRepository, times(1)).save(any(DataModelDAO.class));
//    }
//
//    @Test
//    void testListDataModelsAsDomainExpert() {
//        // Assuming you have a setup method to mock domain expert authentication
//        Authentication authentication = mockAuthentication(true); // true for domain expert
//
//        List<DataModelDAO> allDataModels = List.of(new DataModelDAO(), new DataModelDAO()); // Simulate finding two data models
//        when(dataModelRepository.findAll()).thenReturn(allDataModels);
//
//        List<DataModelDTO> result = dataModelService.listDataModels(authentication, null, logger);
//
//        assertEquals(2, result.size());
//        verify(dataModelRepository, times(1)).findAll();
//    }
//
//    @Test
//    void testListDataModelsAsNonDomainExpert() {
//        Authentication authentication = mockAuthentication(false); // false for non-domain expert
//
//        List<DataModelDAO> releasedDataModels = List.of(new DataModelDAO()); // Simulate finding one released data model
//        when(dataModelRepository.findByReleased(true)).thenReturn(releasedDataModels);
//
//        List<DataModelDTO> result = dataModelService.listDataModels(authentication, true, logger);
//
//        assertEquals(1, result.size());
//        verify(dataModelRepository, times(1)).findByReleased(true);
//    }
//
//    @Test
//    void testUpdateDataModelUnreleased() {
//        String uuid = UUID.randomUUID().toString();
//        DataModelDTO dataModelDTO = mockDataModelDTO(false); // false for unreleased
//
//        DataModelDAO existingDataModelDAO = mockDataModelDAO(uuid, false); // false for unreleased
//        when(dataModelRepository.findByUuid(UUID.fromString(uuid))).thenReturn(Optional.of(existingDataModelDAO));
//        when(dataModelRepository.save(any(DataModelDAO.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//        DataModelDTO updatedDataModelDTO = dataModelService.updateDataModel(uuid, dataModelDTO, logger);
//
//        assertNotNull(updatedDataModelDTO);
//        verify(dataModelRepository, times(1)).save(any(DataModelDAO.class));
//    }
//
//    @Test
//    void testUpdateDataModelReleasedThrowsException() {
//        String uuid = UUID.randomUUID().toString();
//        DataModelDTO dataModelDTO = mockDataModelDTO(true); // true for released
//
//        DataModelDAO existingDataModelDAO = mockDataModelDAO(uuid, true); // true for already released
//        when(dataModelRepository.findByUuid(UUID.fromString(uuid))).thenReturn(existingDataModelDAO);
//
//        assertThrows(DataIntegrityViolationException.class, () -> {
//            dataModelService.updateDataModel(uuid, dataModelDTO, logger);
//        });
//    }
//
//    @Test
//    void testDeleteDataModelUnreleased() {
//        String uuid = UUID.randomUUID().toString();
//        DataModelDAO dataModelDAO = mockDataModelDAO(uuid, false); // false for unreleased
//        when(dataModelRepository.findByUuid(UUID.fromString(uuid))).thenReturn(dataModelDAO);
//
//        assertDoesNotThrow(() -> dataModelService.deleteDataModel(uuid, logger));
//        verify(dataModelRepository, times(1)).deleteByUuid(UUID.fromString(uuid));
//    }
//
//    @Test
//    void testDeleteDataModelReleasedThrowsException() {
//        String uuid = UUID.randomUUID().toString();
//        DataModelDAO dataModelDAO = mockDataModelDAO(uuid, true); // true for released
//        when(dataModelRepository.findByUuid(UUID.fromString(uuid))).thenReturn(dataModelDAO);
//
//        assertThrows(DataIntegrityViolationException.class, () -> dataModelService.deleteDataModel(uuid, logger));
//    }

}
