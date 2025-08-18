
---

## **3. Backend README (team-access-manager-service)**

```markdown
# Team Access Manager - Backend (Spring Boot)

This is the **Spring Boot backend service** for Team Access Manager.  
It provides REST APIs for authentication, access management, audit trails, and email notifications.

---

## 🔹 Features
- **JWT Authentication** (login + token refresh)
- **Role-based Access Control** (Admin/User)
- **Request Access Flow**
  - New users submit request
  - Admin approves
  - Temporary password sent via email
- **Forgot Password Flow** (OTP validation + reset)
- **Audit Trail Logging**
- **Email Notifications** via Gmail SMTP

---

## 🏗️ Tech Stack
- Java 24
- Spring Boot 3.x
- Spring Security (JWT)
- Spring Data JPA
- PostgreSQL
- Gmail SMTP (JavaMailSender)

---

## 🚀 Setup
1. Clone repo:
   ```bash
   git clone https://github.com/yashwant340/team-access-manager-service.git
   cd team-access-manager-service
```
2. Configure Database

Edit src/main/resources/application.properties:

spring.datasource.url=jdbc:postgresql://localhost:5432/teamaccess
spring.datasource.username=your_user
spring.datasource.password=your_password

3. Configure Email (Gmail Example)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your_app_password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true


⚠️ Use an App Password for Gmail, not your regular password.

4. Run Backend
mvn clean install
mvn spring-boot:run


Backend runs at: http://localhost:8080
