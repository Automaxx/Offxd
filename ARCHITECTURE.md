# System Architecture

Below is the system architecture in Mermaid format.

```mermaid
graph LR
    subgraph Context
        User["User"]
    end
    
    subgraph Frontend
        ReactApp["React Frontend"]
        style ReactApp fill:#e1f5fe
    end

    subgraph Backend
        SpringBootApp["Spring Boot Backend"]
        style SpringBootApp fill:#f3e5f5
        SpringDataJPA["Spring Data JPA"]
    end

    subgraph DataStores
        Database["SQL Database"]
        style Database fill:#e8f5e8
    end

    User --> ReactApp
    ReactApp -->|HTTP/REST| SpringBootApp
    SpringBootApp -->|DB Driver| Database
    SpringBootApp -.-> SpringDataJPA
```
