# SecureOffice Communication Hub

A professional office platform built with Spring Boot and React for secure team collaboration.

## 🏗️ Architecture

- **Backend**: Spring Boot 3.2 + MySQL + JWT Authentication
- **Frontend**: React 18 + Vite + Material-UI
- **Real-time**: WebSocket for notifications
- **Architecture**: Monolithic application

## 🚀 Features

### 1. Authentication & User Management
- JWT login/logout with refresh tokens
- User CRUD operations
- Role-based access (ADMIN, MANAGER, EMPLOYEE)
- Password reset via email
- Security features (rate limiting, validation)

### 2. Document Sharing
- File upload/download
- Folder structure management
- File sharing with permissions (view/edit)
- File search functionality
- Access logging

### 3. Team Communication
- Team/department management
- Messaging system
- Announcement board
- Real-time notifications via WebSocket

### 4. Analytics Dashboard
- User activity tracking
- File access statistics
- Admin dashboard with charts
- Usage reports

## 📁 Project Structure

```
secure-office/
├── backend/                 # Spring Boot application
│   ├── src/main/java/
│   ├── src/main/resources/
│   └── pom.xml
├── frontend/                # React application
│   ├── src/
│   ├── public/
│   └── package.json
├── database/               # SQL scripts
│   └── schema.sql
└── README.md
```

## 🛠️ Development Setup

### Prerequisites
- Java 17+
- Node.js 18+
- MySQL 8.0+
- Maven 3.6+

### Backend Setup
```bash
cd backend
mvn spring-boot:run
```

### Frontend Setup
```bash
cd frontend
npm install
npm run dev
```

### Database Setup
```bash
mysql -u root -p < database/schema.sql
```

## 🔧 Configuration

- Backend runs on: `http://localhost:8080`
- Frontend runs on: `http://localhost:5173`
- Database: `localhost:3306/secure_office`

## 📊 Database Schema

Core tables:
- `users` - User accounts and authentication
- `departments` - Team/department organization
- `files` - Document storage metadata
- `messages` - Team communication
- `notifications` - Real-time alerts
- `activity_logs` - User activity tracking

## 🔐 Security Features

- JWT token authentication
- Role-based access control
- Rate limiting
- Input validation
- Secure file upload
- Activity logging

## 📈 Technology Stack

**Backend:**
- Spring Boot 3.2
- Spring Security
- Spring Data JPA
- MySQL
- JWT
- WebSocket

**Frontend:**
- React 18
- Vite
- Material-UI
- Axios
- React Router
- Socket.io Client

## 🚀 Deployment

Production deployment instructions will be added here.

## 📝 License

This project is for educational/demonstration purposes.
