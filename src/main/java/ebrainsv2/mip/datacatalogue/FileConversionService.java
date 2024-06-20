package ebrainsv2.mip.datacatalogue;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Service
public class FileConversionService {

    @Value("${services.data_quality_tools.json2excel}")
    private String json2excel;

    @Value("${services.data_quality_tools.excel2json}")
    private String excel2json;


    private final RestTemplate restTemplate = new RestTemplate();

    public File convertMultipartFileToFile(MultipartFile file) throws Exception {
        File convFile = new File(System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename());
        file.transferTo(convFile);
        return convFile;
    }

    public ByteArrayResource convertJsonToExcel(String json) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(json, headers);
        byte[] excelData = restTemplate.postForObject(json2excel, requestEntity, byte[].class);
        assert excelData != null;
        return new ByteArrayResource(excelData);
    }

    public String convertExcelToJson(File file) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(file));
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        return restTemplate.postForEntity(excel2json, requestEntity, String.class).getBody();
    }
}
