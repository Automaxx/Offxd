# System Architecture

Below is the system architecture in Mermaid format.

```mermaid
flowchart LR
    subgraph Frontend
        A[React App<br/>(Material UI, React Router)] --> B(HTTP/REST);
    end
    subgraph Backend/Services
        B[Spring Boot API<br/>(Java, Spring Data JPA)] --> C(DB Driver);
        B --> D((Lazy Loading));
    end
    subgraph Data Stores
        C[SQL Database];
    end
    subgraph CI/CD
        style CI/CD fill:#f9f,stroke:#333,stroke-width:2px
        subgraph "No CI/CD Pipeline"
            Z[Manual Deployment]
        end
    end
    
    A --> B;
    
    
    style A fill:#ccf,stroke:#333,stroke-width:2px
    style B fill:#ccf,stroke:#333,stroke-width:2px
    style C fill:#ccf,stroke:#333,stroke-width:2px
```
