# Data Quality Tool Service

Welcome to the Data Quality Tool Service! This tool allows you to easily convert between Excel and JSON formats via a simple web service. Follow the steps below to build and run the Docker container, and to use the conversion features.

## Building the Docker Image

To build a new Docker image, navigate to the `datacatalogue/data_quality_tool` directory and execute the following command:

```sh
docker build -t <USERNAME>/data_quality_tool:<IMAGETAG> .
```

### Example:
```sh
docker build -t madgik/data_quality_tool:latest .
```

## Running the Docker Container

Once the image is built, you can start the container with the following command:

```sh
docker run -d -p 8000:8000 --name <CONTAINER_NAME> <USERNAME>/data_quality_tool:<IMAGETAG>
```

### Example:
```sh
docker run -d -p 8000:8000 --name data_quality_tool madgik/data_quality_tool:latest
```

This command will run the container in detached mode and map port 8000 of the container to port 8000 on your host machine.

## Using the Conversion Features

### Convert Excel to JSON

To convert an Excel file to JSON, use the following `curl` command:

```sh
curl -X POST -F "file=@<ABSOLUTE_PATH_OF_EXCEL_FILE>" http://127.0.0.1:8000/excel-to-json -o <OUTPUT_JSON_FILE>
```

### Example:
```sh
curl -X POST -F "file=@/opt/data/data.xlsx" http://127.0.0.1:8000/excel-to-json -o data.json
```

Replace `<ABSOLUTE_PATH_OF_EXCEL_FILE>` with the absolute path to your Excel file and `<OUTPUT_JSON_FILE>` with the desired name for the output JSON file.

### Convert JSON to Excel

To convert a JSON file to Excel, use the following `curl` command:

```sh
curl -X POST -H "Content-Type: application/json" -d @<ABSOLUTE_PATH_OF_JSON_FILE> http://127.0.0.1:8000/json-to-excel -o <OUTPUT_EXCEL_FILE>
```

### Example:
```sh
curl -X POST -H "Content-Type: application/json" -d @/opt/data/data.json http://127.0.0.1:8000/json-to-excel -o data.xlsx
```

Replace `<ABSOLUTE_PATH_OF_JSON_FILE>` with the absolute path to your JSON file and `<OUTPUT_EXCEL_FILE>` with the desired name for the output Excel file.

## Summary

1. **Build the Docker Image**:
   - `docker build -t <USERNAME>/data_quality_tool:<IMAGETAG> .`
2. **Run the Docker Container**:
   - `docker run -d -p 8000:8000 --name <CONTAINER_NAME> <USERNAME>/data_quality_tool:<IMAGETAG>`
3. **Convert Excel to JSON**:
   - `curl -X POST -F "file=@<ABSOLUTE_PATH_OF_EXCEL_FILE>" http://127.0.0.1:8000/excel-to-json -o <OUTPUT_JSON_FILE>`
4. **Convert JSON to Excel**:
   - `curl -X POST -H "Content-Type: application/json" -d @<ABSOLUTE_PATH_OF_JSON_FILE> http://127.0.0.1:8000/json-to-excel -o <OUTPUT_EXCEL_FILE>`
