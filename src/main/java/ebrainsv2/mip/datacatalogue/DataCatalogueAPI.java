package ebrainsv2.mip.datacatalogue;

import com.fasterxml.jackson.databind.ObjectMapper;
import ebrainsv2.mip.datacatalogue.datamodel.DataModelDTO;
import ebrainsv2.mip.datacatalogue.datamodel.DataModelService;
import ebrainsv2.mip.datacatalogue.datamodelinfo.DataModelInfoDTO;
import ebrainsv2.mip.datacatalogue.datamodelinfo.DataModelInfoService;
import ebrainsv2.mip.datacatalogue.utils.UserActionLogger;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/datacatalogue", produces = APPLICATION_JSON_VALUE)
public class DataCatalogueAPI {
    private final DataModelService dataModelService;
    private final DataModelInfoService dataModelInfoService;


    public DataCatalogueAPI(DataModelService dataModelService, DataModelInfoService dataModelInfoService) {
        this.dataModelService = dataModelService;
        this.dataModelInfoService = dataModelInfoService;
    }


    @PostMapping("/datamodels/import")
    public ResponseEntity<?> importDataModelViaExcel(Authentication authentication, @RequestParam("file") MultipartFile file) {
        try {
            UserActionLogger logger = new UserActionLogger(authentication, "POST /datacatalogue/datamodels/import");

            // Convert MultipartFile to File
            File convFile = new File(System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename());
            file.transferTo(convFile);

            // Setup the request to Flask API
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new FileSystemResource(convFile));

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            RestTemplate restTemplate = new RestTemplate();
            String flaskApiUrl = "http://localhost:8000/excel-to-json";
            ResponseEntity<String> response = restTemplate.postForEntity(flaskApiUrl, requestEntity, String.class);

            // Assuming the JSON structure matches your DataModelDTO structure
            ObjectMapper objectMapper = new ObjectMapper();
            DataModelDTO dataModelDTO = objectMapper.readValue(response.getBody(), DataModelDTO.class);

            DataModelDTO createdDataModel = dataModelService.createDataModel(dataModelDTO, logger);
            return new ResponseEntity<>(createdDataModel, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/datamodels/{uuid}/export")
    public ResponseEntity<ByteArrayResource> exportDataModelAsExcel(Authentication authentication, @PathVariable String uuid) {
        try {
            // Fetch your data model by id
            UserActionLogger logger = new UserActionLogger(authentication, "GET /datacatalogue/datamodels/" + uuid + "/export");
            DataModelDTO dataModel = dataModelService.getDataModel(authentication, uuid, logger);

            // Convert data model to JSON
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(dataModel);

            // Setup the request to Flask API
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> requestEntity = new HttpEntity<>(json, headers);

            RestTemplate restTemplate = new RestTemplate();
            String flaskApiUrl = "http://localhost:8000/json-to-excel";
            byte[] excelData = restTemplate.postForObject(flaskApiUrl, requestEntity, byte[].class);

            // Return the Excel file
            ByteArrayResource resource = new ByteArrayResource(excelData);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=data-model.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(resource);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/datamodels")
    public ResponseEntity<List<DataModelDTO>> listDataModels(
            Authentication authentication,
            @RequestParam(name = "released", required = false) Boolean released
    ) {
        UserActionLogger logger = new UserActionLogger(authentication, "GET /datacatalogue/datamodels/");
        List<DataModelDTO> dataModels = dataModelService.listDataModels(authentication, released, logger);
        return new ResponseEntity<>(dataModels, HttpStatus.OK);
    }

    @GetMapping("/datamodels/{uuid}")
    public ResponseEntity<DataModelDTO> getDataModel(Authentication authentication, @PathVariable String uuid) {
        UserActionLogger logger = new UserActionLogger(authentication, "GET /datacatalogue/datamodels/" + uuid);
        DataModelDTO dataModel = dataModelService.getDataModel(authentication, uuid, logger);
        return new ResponseEntity<>(dataModel, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('DC_DOMAIN_EXPERT')")
    @PostMapping("/datamodels")
    public ResponseEntity<DataModelDTO> createDataModel(Authentication authentication, @RequestBody DataModelDTO dataModelDTO) {
        UserActionLogger logger = new UserActionLogger(authentication, "POST /datacatalog/datamodels");
        DataModelDTO createdDataModel = dataModelService.createDataModel(dataModelDTO, logger);
        return new ResponseEntity<>(createdDataModel, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('DC_DOMAIN_EXPERT')")
    @PutMapping("/datamodels/{uuid}")
    public ResponseEntity<DataModelDTO> updateDataModel(Authentication authentication, @PathVariable String uuid, @RequestBody DataModelDTO dataModelDTO) {
        UserActionLogger logger = new UserActionLogger(authentication, "PUT /datacatalog/datamodels/" + uuid);
        DataModelDTO updatedDataModel = dataModelService.updateDataModel(uuid, dataModelDTO, logger);
        return updatedDataModel != null ? new ResponseEntity<>(updatedDataModel, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PreAuthorize("hasAuthority('DC_DOMAIN_EXPERT')")
    @DeleteMapping("/datamodels/{uuid}")
    public ResponseEntity<Void> deleteDataModel(Authentication authentication, @PathVariable String uuid) {
        UserActionLogger logger = new UserActionLogger(authentication, "DELETE /datacatalog/datamodels/" + uuid);
        dataModelService.deleteDataModel(uuid, logger);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PreAuthorize("hasAuthority('DC_DOMAIN_EXPERT')")
    @PostMapping("/datamodels/{uuid}/release")
    public ResponseEntity<Void> releaseDataModel(Authentication authentication, @PathVariable String uuid) {
        UserActionLogger logger = new UserActionLogger(authentication, "POST /datacatalog/datamodels/" + uuid + "/release");
        dataModelService.releaseDataModel(uuid, logger);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('DC_ADMIN')")
    @PostMapping("/datamodelinfos")
    public ResponseEntity<DataModelInfoDTO> createDataModelInfo(Authentication authentication, @RequestBody DataModelInfoDTO dataModelInfoDTO) {
        UserActionLogger logger = new UserActionLogger(authentication, "POST /datacatalogue/datamodelinfos");
        DataModelInfoDTO createdDataModelInfo = dataModelInfoService.createDataModelInfo(dataModelInfoDTO, logger);
        return new ResponseEntity<>(createdDataModelInfo, HttpStatus.CREATED);
    }

    @GetMapping("/datamodelinfos/{code}")
    public ResponseEntity<DataModelInfoDTO> getDataModelInfo(Authentication authentication, @PathVariable String code) {
        UserActionLogger logger = new UserActionLogger(authentication, "GET /datacatalogue/datamodelinfos/" + code);
        DataModelInfoDTO dataModelInfo = dataModelInfoService.getDataModelInfoByCode(code, logger);
        return new ResponseEntity<>(dataModelInfo, HttpStatus.OK);
    }


    @PreAuthorize("hasAuthority('DC_ADMIN')")
    @PutMapping("/datamodelinfos/{code}")
    public ResponseEntity<DataModelInfoDTO> updateDataModelInfo(Authentication authentication, @PathVariable String code, @RequestBody DataModelInfoDTO dataModelInfoDTO) {
        UserActionLogger logger = new UserActionLogger(authentication, "PUT /datacatalogue/datamodelinfos/" + code);
        DataModelInfoDTO updatedDataModelInfo = dataModelInfoService.updateDataModelInfo(code, dataModelInfoDTO, logger);
        return new ResponseEntity<>(updatedDataModelInfo, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('DC_ADMIN')")
    @DeleteMapping("/datamodelinfos/{code}")
    public ResponseEntity<Void> deleteDataModelInfo(Authentication authentication, @PathVariable String code) {
        UserActionLogger logger = new UserActionLogger(authentication, "DELETE /datacatalogue/datamodelinfos/" + code);
        dataModelInfoService.deleteDataModelInfo(code, logger);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
