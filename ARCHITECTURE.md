# System Architecture

Below is the system architecture in Mermaid format.

```mermaid
graph LR
    subgraph Users
        User["Users/Clients"]
    end

    subgraph Frontend
        ReactApp["React Frontend"]
        style ReactApp fill:#e1f5fe
    end

    subgraph Backend
        SpringBootAPI["Spring Boot API"]
        style SpringBootAPI fill:#f3e5f5
        SpringDataJPA["Spring Data JPA"]
        style SpringDataJPA fill:#f3e5f5
    end

    subgraph DataStores
        Database["SQL Database"]
        style Database fill:#e8f5e8
    end

    User -->|HTTP| ReactApp
    ReactApp -->|HTTP/REST| SpringBootAPI
    SpringBootAPI -->|DB Driver| Database
    SpringBootAPI -.-> SpringDataJPA
```
