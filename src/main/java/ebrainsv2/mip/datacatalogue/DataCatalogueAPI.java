package ebrainsv2.mip.datacatalogue;

import ebrainsv2.mip.datacatalogue.datamodel.DataModelDTO;
import ebrainsv2.mip.datacatalogue.datamodel.DataModelService;
import ebrainsv2.mip.datacatalogue.datamodelinfo.DataModelInfoDTO;
import ebrainsv2.mip.datacatalogue.datamodelinfo.DataModelInfoService;
import ebrainsv2.mip.datacatalogue.utils.UserActionLogger;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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





    @PreAuthorize("@securityConfiguration.hasPermission(authentication, 'DC_DOMAIN_EXPERT')")
    @PostMapping("/datamodels/import")
    public ResponseEntity<?> importDataModelViaExcel(Authentication authentication, @RequestParam("file") MultipartFile file) {
        UserActionLogger logger = new UserActionLogger(authentication, "POST /datacatalogue/datamodels/import");
        try {
            DataModelDTO createdDataModel = dataModelService.importDataModel(file, logger);
            return new ResponseEntity<>(createdDataModel, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/datamodels/{uuid}/export")
    public ResponseEntity<ByteArrayResource> exportDataModelAsExcel(Authentication authentication, @PathVariable String uuid) {
        UserActionLogger logger = new UserActionLogger(authentication, "GET /datacatalogue/datamodels/" + uuid + "/export");
        try {
            // Fetch your data model by id
            ByteArrayResource resource = dataModelService.exportDataModel(authentication, uuid, logger);
            // Return the Excel file
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=data-model.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(resource);
        } catch (Exception e) {
            logger.error(e.getMessage());
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

    @PreAuthorize("@securityConfiguration.hasPermission(authentication, 'DC_DOMAIN_EXPERT')")
    @PostMapping("/datamodels")
    public ResponseEntity<DataModelDTO> createDataModel(Authentication authentication, @RequestBody DataModelDTO dataModelDTO) {
        UserActionLogger logger = new UserActionLogger(authentication, "POST /datacatalog/datamodels");
        DataModelDTO createdDataModel = dataModelService.createDataModel(dataModelDTO, logger);
        return new ResponseEntity<>(createdDataModel, HttpStatus.CREATED);
    }

    @PreAuthorize("@securityConfiguration.hasPermission(authentication, 'DC_DOMAIN_EXPERT')")
    @PutMapping("/datamodels/{uuid}")
    public ResponseEntity<DataModelDTO> updateDataModel(Authentication authentication, @PathVariable String uuid, @RequestBody DataModelDTO dataModelDTO) {
        UserActionLogger logger = new UserActionLogger(authentication, "PUT /datacatalog/datamodels/" + uuid);
        DataModelDTO updatedDataModel = dataModelService.updateDataModel(uuid, dataModelDTO, logger);
        return updatedDataModel != null ? new ResponseEntity<>(updatedDataModel, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PreAuthorize("@securityConfiguration.hasPermission(authentication, 'DC_DOMAIN_EXPERT')")
    @DeleteMapping("/datamodels/{uuid}")
    public ResponseEntity<Void> deleteDataModel(Authentication authentication, @PathVariable String uuid) {
        UserActionLogger logger = new UserActionLogger(authentication, "DELETE /datacatalog/datamodels/" + uuid);
        dataModelService.deleteDataModel(uuid, logger);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PreAuthorize("@securityConfiguration.hasPermission(authentication, 'DC_DOMAIN_EXPERT')")
    @PostMapping("/datamodels/{uuid}/release")
    public ResponseEntity<Void> releaseDataModel(Authentication authentication, @PathVariable String uuid) {
        UserActionLogger logger = new UserActionLogger(authentication, "POST /datacatalog/datamodels/" + uuid + "/release");
        dataModelService.releaseDataModel(uuid, logger);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PreAuthorize("@securityConfiguration.hasPermission(authentication, 'DC_ADMIN')")
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


    @PreAuthorize("@securityConfiguration.hasPermission(authentication, 'DC_ADMIN')")
    @PutMapping("/datamodelinfos/{code}")
    public ResponseEntity<DataModelInfoDTO> updateDataModelInfo(Authentication authentication, @PathVariable String code, @RequestBody DataModelInfoDTO dataModelInfoDTO) {
        UserActionLogger logger = new UserActionLogger(authentication, "PUT /datacatalogue/datamodelinfos/" + code);
        DataModelInfoDTO updatedDataModelInfo = dataModelInfoService.updateDataModelInfo(code, dataModelInfoDTO, logger);
        return new ResponseEntity<>(updatedDataModelInfo, HttpStatus.OK);
    }

    @PreAuthorize("@securityConfiguration.hasPermission(authentication, 'DC_ADMIN')")
    @DeleteMapping("/datamodelinfos/{code}")
    public ResponseEntity<Void> deleteDataModelInfo(Authentication authentication, @PathVariable String code) {
        UserActionLogger logger = new UserActionLogger(authentication, "DELETE /datacatalogue/datamodelinfos/" + code);
        dataModelInfoService.deleteDataModelInfo(code, logger);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
