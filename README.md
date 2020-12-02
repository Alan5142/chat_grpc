# Chat gRPC

---

## Status

Servidor

| Proyecto | Estado                                                                                                                                                                                                   |
|----------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Servidor | [![Build Status](https://dev.azure.com/alan5142/chat_grpc/_apis/build/status/Alan5142.chat_grpc?branchName=main)](https://dev.azure.com/alan5142/chat_grpc/_build/latest?definitionId=6&branchName=main) |
| Cliente  | [![Build Status](https://dev.azure.com/alan5142/chat_grpc/_apis/build/status/Alan5142.chat_grpc%20(1)?branchName=refs%2Fpull%2F10%2Fmerge)](https://dev.azure.com/alan5142/chat_grpc/_build/latest?definitionId=7&branchName=refs%2Fpull%2F10%2Fmerge) |

---

## Descripción
Servidor gRPC y app para Android para aplicación de chat.

## Para utilizar
Se necesita un servidor MinIO y un servidor PostgreSQL, MinIO puede ser reemplazado por S3.
El servidor MinIO/S3 debe tener un bucket llamado «images»
Basta con compilar y definir las siguientes variables de entorno.

| Variable         | Descripción                                                                                                                                        |
|------------------|----------------------------------------------------------------------------------------------------------------------------------------------------|
| DB_USERNAME      | Usuario de la base de datos a la que se va a conectar                                                                                              |
| DB_PASSWORD      | Contraseña del usuario de la base de datos a la que se va a conectar                                                                               |
| MINIO_ENDPOINT   | Endpoint para conectarse a un servidor minio, debe tener el siguiente formato: http[s]://[IP o nombre de dominio][:puerto] o un endpoint de AWS S3 |
| MINIO_ACCESS_KEY | Clave de acceso a MinIO/Clave para AWS S3                                                                                                          |
| MINIO_SECRET_KEY | Clave secreta asociada a la clave de acceso de MinIO/Clave de API para AWS S3                                                                      |
