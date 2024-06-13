## Data quality tool service

### Build docker image

To build a new image you must be on folder `datacatalogue/data_quality_tool`, then

```
docker build -t <USERNAME>/data_quality_tool:<IMAGETAG> .
Example: 
    docker build -t madgik/data_quality_tool:latest .

```


Then start the container with:

```
docker run -d -p 8000:8000 --name <CONTAINER_NAME> <USERNAME>/data_quality_tool:<IMAGETAG>
Example:
    docker run -d  -p 8000:8000 --name data_quality_tool madgik/data_quality_tool:latest
```
