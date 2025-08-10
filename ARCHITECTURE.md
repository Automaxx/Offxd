# System Architecture

Below is the system architecture in Mermaid format.

```mermaid
graph LR
    subgraph Frontend
        React["React App (MUI, React Router)"]
        style React fill:#e1f5fe
    end

    subgraph Backend
        SpringBoot["Spring Boot (Java)"]
        JPA["Spring Data JPA"]
        style SpringBoot fill:#f3e5f5
        style JPA fill:#f3e5f5
    end

    subgraph DataStores
        SQLDB["SQL Database"]
        style SQLDB fill:#e8f5e8
    end

    subgraph CI_CD
        CI_CD_System["(No CI/CD)"]
        style CI_CD_System fill:#f0f0f0,stroke:#ccc,stroke-width:1px
    end


    React -->|HTTP/REST| SpringBoot
    SpringBoot -->|DB driver| SQLDB
```
